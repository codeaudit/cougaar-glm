/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation;

import java.io.IOException;
import java.io.PrintStream;
import java.text.*;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.society.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

/**
 * This is a PSP that serves up UIItinerary objects for scheduled 
 * tasks from a given cluster. 
 **/

public class PSP_Carrier_Itinerary extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   *  required by Class.newInstance()
   **/
  public PSP_Carrier_Itinerary() {
    super();
    setDebug();
  }

  /*************************************************************************
   * 
   **/
  public PSP_Carrier_Itinerary( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /**************************************************************************
   * 
   **/
  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /** DEBUG forced to off! **/
  public static final boolean DEBUG = false;
  protected boolean setDebug() {return DEBUG;}

  public AbstractPrinter getAbstractPrinter(
      PrintStream out,
      HttpInput query_parameters,
      String defaultFormat) 
    throws Exception 
  {
    String format = defaultFormat;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = ((String)params.nextElement()).toLowerCase();
      if (p.startsWith("format=")) {
        format = p.substring("format=".length()).trim();
        if ((format.length() <= 0) ||
            !AbstractPrinter.isValidFormat(format)) {
          throw new RuntimePSPException("Invalid format!: "+format);
        }
      }
    }
    return AbstractPrinter.createPrinter(format, out);
  }

  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) 
    throws Exception 
  {
    AbstractPrinter pr = getAbstractPrinter(out, query_parameters, "data");
    execute(pr, query_parameters, psc, psu);
  }

  /****************************************************************************
   * Main execute method for PSP : Dispatch based on mode parameter
   * <p>
   * Valid params:
   * <ul>
   *   <li><code>FORMAT=</code>AbstractPrinter format, e.g. "XML"</li>
   *   <li><code>CarrierTypeNomenclature=</code>
   *       <code>java.net.URLEncode</code>(Filter for a String)</li>
   * </ul>
   **/
  public void execute(AbstractPrinter pr,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu)
    throws Exception 
  {
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);
    
    System.out.println("PSP_Carrier_Itinerary Invoked...");

    if (myState.showAssets) {
      myState.seenAssets = new HashMap();
    }

    try {
      Vector results = generateData(myState);
      pr.printObject(results);
    } catch (Exception topLevelException) {
      System.err.println("Exception processing PSP_Carrier_Itinerary: "+
          topLevelException);
      topLevelException.printStackTrace();
    }
  }

  /** 
   * Use instead of "this" to force no instance field usage.
   **/
  private static class MyPSPState extends PSPState {

    /** fields **/
    public boolean ignoreUnscheduled;
    public boolean interpolate;
    public boolean showAssets;
    public String filterNomen;
    public HashMap seenAssets;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
      ignoreUnscheduled = false;
      interpolate = true;
      showAssets = false;
      filterNomen = null;
      seenAssets = null;
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);
      if (name.equalsIgnoreCase("CarrierTypeNomenclature")) {
        filterNomen = value;
      } else if (name.equalsIgnoreCase("ignoreUnscheduled")) {
        ignoreUnscheduled =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("interpolate")) {
        interpolate =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("showAssets")) {
        showAssets =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      }
    }
  }

  protected static Collection searchUsingPredicate(
      MyPSPState myState, UnaryPredicate pred) 
  {
    return myState.sps.queryForSubscriber(pred);
  }

  /** 
   * Carrier Assets are:<br>
   * <ul>
   *   <li>instanceof CargoVehicle</li>
   * </ul>
   * Maybe switch to:<br>
   * <ul>
   *   <li>has all PGs of CargoVehicle</li>
   * </ul>
   */
  protected static UnaryPredicate getCarrierAssetPred() {
    return new UnaryPredicate() 
      {
        public boolean execute(Object o) {
          return (o instanceof CargoVehicle);
        }
      };
  }

  /**
   * Match Nomenclature
   */
  protected static UnaryPredicate getCarrierAssetPred(
      final String filterNomen) {
    return new UnaryPredicate() 
      {
        public boolean execute(Object o) {
          return 
            ((o instanceof CargoVehicle) &&
             matchesCarrierTypeNomenclature(filterNomen, (Asset)o));
        }
      };
  }

  /**
   * find carrier assets.  Optionally filter by Nomenclature.
   * @param filterNomen if non-null, only return assets with this
   *   Nomenclature, otherwise return all assets
   */
  protected static Collection searchForCarrierAssets(MyPSPState myState) {
    return searchUsingPredicate(myState,
        ((myState.filterNomen == null) ? 
         getCarrierAssetPred() :
         getCarrierAssetPred(myState.filterNomen)));
  }

  /**
   * more descriptive typeNomenclature.
   * @see #matchesCarrierTypeNomenclature()
   */
  public static String getCarrierTypeNomenclature(Asset carrierAsset) {
    TypeIdentificationPG tipg = carrierAsset.getTypeIdentificationPG();
    return
      tipg.getTypeIdentification() + 
      " - " + 
      tipg.getNomenclature();
  }

  /**
   * @param s CarrierTypeNomenclature to equal
   * @param carrierAsset get CarrierTypeNomenclature from this asset
   * @return true if match
   * @see #getCarrierTypeNomenclature()
   */
  public static boolean matchesCarrierTypeNomenclature(
      String s, Asset carrierAsset) {
    /*
     // easy way
     return s.equals(getCarrierTypeNomenclature(carrierAsset));
    */
    // try not to allocate anything!  Used in predicate!
    TypeIdentificationPG tipg = 
      carrierAsset.getTypeIdentificationPG();
    String typeID = tipg.getTypeIdentification();
    if (!(s.startsWith(typeID)))
      return false;
    int s_offset = typeID.length();
    int dashLen = " - ".length();
    if (!(s.regionMatches(s_offset, " - ", 0, dashLen)))
      return false;
    s_offset += dashLen;
    String nomen = tipg.getNomenclature();
    if (!(s.regionMatches(s_offset, nomen, 0, 
                          (s.length() - s_offset))))
      return false;
    return true;
  }

  public static void addCarrierItinerary(
      MyPSPState myState,
      Vector toV, 
      Asset carrierAsset) {
    String carrierUID = carrierAsset.getUID().toString();
    if (DEBUG) {
      System.out.println("Carrier Asset: "+carrierUID);
    }
    // get role schedule
    RoleSchedule roleS = carrierAsset.getRoleSchedule();
    if (roleS == null) {
      System.err.println(" No RoleSchedule!");
      return;
    }
    Enumeration planEn = roleS.getRoleScheduleElements();
    boolean hasSchedule = planEn.hasMoreElements();
    if (myState.ignoreUnscheduled && !hasSchedule) {
      return;
    }
    // create carrier itinerary
    UICarrierItinerary ci = new UICarrierItinerary();
    // set identifications
    ci.setClusterID(myState.clusterID);
    ci.setCarrierUID(carrierUID);
    ci.setCarrierTypeNomenclature(
      getCarrierTypeNomenclature(carrierAsset));
    ci.setCarrierItemNomenclature(
        carrierAsset.getItemIdentificationPG().getItemIdentification());
    // use availableSchedule to fake schedule types
    Schedule availS = roleS.getAvailableSchedule();
    if (availS != null) {
      ci.setScheduleType(availS.getScheduleType());
      ci.setScheduleElementType(availS.getScheduleElementType().getName());
    }
    // fill in the schedule
    if (hasSchedule) {
      Vector ciElems = ci.getScheduleElements();
      do {
        try {
          PlanElement pe = (PlanElement) planEn.nextElement();
          Task allocTask = pe.getTask();
          if (!addDetailedItinerary(myState, allocTask, ciElems))
            addDefaultItinerary(myState, allocTask, ciElems);
        } catch (Exception e) {
          // really shouldn't happen!
          System.err.println("CarrierItinerary "+
              ci.getCarrierItemNomenclature()+" has Exception: "+e);
          e.printStackTrace();
          System.err.println("will continue");
        }
      } while (planEn.hasMoreElements());
      ci.setScheduleElements(ciElems);
    }
    
    // ok, add to vector
    toV.addElement(ci);
  }

  /** The next several methods are basically common between this PSP 
   * and PSP_Itinerary.  They should be moved to a helper class.
   */

  /** <b>TAKEN FROM PSP_Itinerary.makeUITAssetInfo()</b><p>
   */
  private static UITAssetInfo makeUITAssetInfo(
      Asset transAsset) {
    UITAssetInfo tai = new UITAssetInfo();
    int quantity = 1;
    double tons = 0.0;
    if (transAsset instanceof GLMAsset &&((GLMAsset)transAsset).hasPhysicalPG()) {
	tons += ((GLMAsset)transAsset).getPhysicalPG().getMass().getTons();
    }
    while (transAsset instanceof AggregateAsset) {
      AggregateAsset agg = (AggregateAsset)transAsset;
      quantity *= (int)agg.getQuantity();
      transAsset = agg.getAsset();
      if (transAsset instanceof GLMAsset && ((GLMAsset)transAsset).hasPhysicalPG()) {
	  tons += quantity * ((GLMAsset)transAsset).getPhysicalPG().getMass().getTons();
      }
    } 
    tai.setQuantity(quantity);
    tai.setTons(tons);
    TypeIdentificationPG transTypePG = transAsset.getTypeIdentificationPG();
    tai.setTypeID(transTypePG.getTypeIdentification());
    tai.setTypeNomenclature(transTypePG.getNomenclature());
    UID uid = transAsset.getUID();
    tai.setUID((uid != null) ? uid.toString() : "");
    tai.setItemID(
      transAsset.getItemIdentificationPG().getItemIdentification());
    return tai;
  }

  /** <b>BASED ON PSP_Itinerary.addUITAssetInfoVector()</b><p>
   * recursive!
   * @param toTAIV Vector of UITAssetInfo instances
   */
  private static void addUITAssetInfoVector(
     Vector toTAIV, Asset dirObj) {
    if (dirObj instanceof AssetGroup) {
      Enumeration assetsEn = ((AssetGroup)dirObj).getAssets().elements();
      while (assetsEn.hasMoreElements()) {
        addUITAssetInfoVector(toTAIV, (Asset)assetsEn.nextElement());
      }
    } else if (dirObj != null) {
      toTAIV.addElement(makeUITAssetInfo(dirObj));
    } else {
      throw new RuntimeException("Unexpected task directObject: null");
    }
  }

  /** <b>BASED ON PSP_Itinerary.getUITAssetInfoVector()</b><p>
   * Note: Stupid waste to have this a Vector when over 90% of
   * the time it's only one element.  Better solution would be
   * to have the field an Object and have user check instance.
   * But the PSP users don't want to change their interface...
   */
  private static Vector getUITAssetInfoVector(Asset dirObj) {
    if (!(dirObj instanceof AssetGroup)) {
      // can't we just make this field an Object????
      Vector vOneElem = new Vector(1);
      if (dirObj != null)
        vOneElem.addElement(makeUITAssetInfo(dirObj));
      return vOneElem;
    }

    Vector v = new Vector();
    addUITAssetInfoVector(v, dirObj);
    return v;
  }

  /** <b>TAKEN FROM PSP_Itinerary.getLinkLength()</b><p>
   * Should be in TransportationLink! 
   **/
  private static double getLinkLength(TransportationLink transLink) {
    org.cougaar.domain.planning.ldm.measure.Distance dist;
    if (transLink instanceof TransportationRoadLink) {
      dist = ((TransportationRoadLink)transLink).getRoadLinkPG().getLinkLength();
    } else if (transLink instanceof TransportationRailLink) {
      dist =  ((TransportationRailLink)transLink).getRailLinkPG().getLinkLength();
    } else if (transLink instanceof TransportationSeaLink) {
      dist =  ((TransportationSeaLink)transLink).getSeaLinkPG().getLinkLength();
    } else if (transLink instanceof TransportationAirLink) {
      dist =  ((TransportationAirLink)transLink).getAirLinkPG().getLinkLength();
    } else {
      throw new RuntimeException("UNKNOWN LINK TYPE: "+transLink);
    }
    return dist.getMeters();
  }

  /** <b>TAKEN FROM PSP_Itinerary.getTransportationRoute()</b><p>
   * Route is held in VIA preposition.
   **/
  private static TransportationRoute getTransportationRoute(Task task) {
    try {
      PrepositionalPhrase prepVia = 
        task.getPrepositionalPhrase(Constants.Preposition.VIA);
      if (prepVia != null) {
        Object viaIndObj = prepVia.getIndirectObject();
        if (viaIndObj instanceof TransportationRoute) {
          return (TransportationRoute)viaIndObj;
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * <b>BASED ON PSP_Itinerary.addLegsInterpolateRoute</b><p>
   * add UICarrierItineraries for an ItineraryElement <code>ie</code>
   * (currently only with role TRANSIT).  Interpolate with given 
   * Route information.
   */
  private static void addInterpolatedElements(
      MyPSPState myState,
      Task task,
      Vector toVector, 
      ItineraryElement ie, 
      TransportationRoute transRoute) {
    // get verb (always TRANSIT?)
    Verb verb = task.getVerb();
    // get dates and total duration
    Date startDate = ie.getStartDate();
    Date endDate = ie.getEndDate();
    long startTime = startDate.getTime();
    double totalTime = (double)(endDate.getTime() - startTime);
    // get links
    Vector vLinks = transRoute.getLinks();
    // sum distance
    int maxIdx = vLinks.size() - 1;
    double totalDistance = 0.0;
    for (int idx = 0; idx <= maxIdx; idx++) {
      TransportationLink transLink = 
        (TransportationLink)vLinks.elementAt(idx);
      totalDistance += getLinkLength(transLink);
    }
    // create legs, time separated by factor of distance
    UICarrierItineraryElement firstCie = null;
    UICarrierItineraryElement lastCie = null;
    Date sDate = startDate;
    double currentDistance = 0.0;
    long currentTime = startTime;
    for (int idx = 0; idx <= maxIdx; idx++) {
      TransportationLink transLink = 
        (TransportationLink)vLinks.elementAt(idx);
      // get fields [s(tart)|e(nd)][Loc(ation)|Date]
      GeolocLocation sLoc = transLink.getOrigin().getGeolocLocation();
      GeolocLocation eLoc = transLink.getDestination().getGeolocLocation();
      currentDistance += getLinkLength(transLink);
      currentTime = startTime + 
         (long)((currentDistance/totalDistance)*totalTime);
      Date eDate = new Date(currentTime);
      // create itinerary element
      UICarrierItineraryElement cie = 
        createUICarrierItineraryElement(
          myState, task, verb,
          sDate, eDate,
          sLoc, eLoc);
      // add to vector
      if (cie != null) {
        cie.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_BOTH);
        if (firstCie == null) {
          firstCie = cie;
        }
        lastCie = cie;
        toVector.addElement(cie);
      }
      // set for next loop
      sDate = eDate;
    }
    // fix interpolated flags
    if (firstCie != null) {
      if (firstCie != lastCie) {
        firstCie.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_END);
        lastCie.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_START);
      } else {
        firstCie.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_NEITHER);
      }
    }
  }

  /**
   * create a <code>UICarrierItineraryElement</code> or subclass, 
   * depending upon <code>MyPSPState.showAssets</code>
   */
  protected static UICarrierItineraryElement createUICarrierItineraryElement(
      MyPSPState myState,
      Task task,
      Verb verbRole,
      Date startDate,
      Date endDate,
      GeolocLocation startLocation,
      GeolocLocation endLocation)
  {
    UICarrierItineraryElement cie;
    if (myState.showAssets) {
      Asset dirObj = task.getDirectObject();
      Vector vAssetInfo;
      // check cache of seen directObjects
      Object oAssetInfo = myState.seenAssets.get(dirObj);
      if (oAssetInfo != null) {
        vAssetInfo = (Vector)oAssetInfo;
      } else {
        vAssetInfo = getUITAssetInfoVector(dirObj);
        myState.seenAssets.put(dirObj, vAssetInfo);
      }
      UICarrierItineraryElementWithAssets ciewa = 
        new UICarrierItineraryElementWithAssets();
      ciewa.setUITAssetInfoVector(vAssetInfo);
      cie = ciewa;
    } else {
      cie = new UICarrierItineraryElement();
    }
    cie.setVerbRole(verbRole);
    cie.setStartDate(startDate);
    cie.setEndDate(endDate);
    cie.setStartLocation(startLocation);
    cie.setEndLocation(endLocation);
    return cie;
  }

  /**
   * Get more detailed TOPS itinerary - if available
   * TOPS associates detailed itinerary with the ITINERARYOF 
   * preposition
   * @return false if task lacks detailed information
   */
  protected static boolean addDetailedItinerary(
      MyPSPState myState,
      Task task,
      Vector toItineraryElementsVector) {
    PrepositionalPhrase pp = 
      task.getPrepositionalPhrase(Constants.Preposition.ITINERARYOF);
    if (pp != null) {
      Object indObj = pp.getIndirectObject();
      if (indObj instanceof Schedule) {
        Schedule schedule = (Schedule)indObj;
        Collection tiling =
          schedule.getEncapsulatedScheduleElements(schedule.getStartTime(),
                                                   schedule.getEndTime());
        if (!tiling.isEmpty()) {
          Enumeration tiles = new Enumerator(tiling);
          do {
            ItineraryElement ie = (ItineraryElement)tiles.nextElement();
           // add carrier itinerary element(s)
            Verb verb = ie.getRole();
            try {
              TransportationRoute route = null;
              if (myState.interpolate && (Constants.Verb.Transit).equals(verb)) {
                // interpolate itinerary with route information
                route = getTransportationRoute(task);
              }
              if (route != null) {
                addInterpolatedElements(
                  myState, task, toItineraryElementsVector, ie, route);
              } else {
                // use itinerary element information
                UICarrierItineraryElement cie =
                  createUICarrierItineraryElement(
                    myState, task, verb,
                    ie.getStartDate(), ie.getEndDate(),
                    (GeolocLocation)ie.getStartLocation(),
                    (GeolocLocation)ie.getEndLocation());
                if (cie != null)
                  toItineraryElementsVector.addElement(cie);
              }
            } catch (Exception e) {
              System.out.println("CARRIER_ITINERARY: Element "+verb+
                " Error: "+e+" from Task: "+task.getUID().getUID());
            }
          } while (tiles.hasMoreElements());
          // success
          return true;
        }
      }
    }
    // failure
    return false;
  }

  /**
   * Get default TOPS carrier itinerary.  
   * @see #addDetailedItinerary()
   */
  protected static void addDefaultItinerary(
      MyPSPState myState,
      Task task,
      Vector toItineraryElementsVector) {
    PlanElement taskPE = task.getPlanElement();
    AllocationResult est = taskPE.getEstimatedResult();
    // assume est.isDefined(AspectType.START_TIME)
    long startTime = (long)est.getValue(AspectType.START_TIME);
    // assume est.isDefined(AspectType.END_TIME)
    long endTime = (long)est.getValue(AspectType.END_TIME);
    PrepositionalPhrase prepFrom =
      task.getPrepositionalPhrase(Constants.Preposition.FROM);
    GeolocLocation fromLoc = (GeolocLocation)prepFrom.getIndirectObject();
    PrepositionalPhrase prepTo = 
      task.getPrepositionalPhrase(Constants.Preposition.TO);
    GeolocLocation toLoc = (GeolocLocation)prepTo.getIndirectObject();
    UICarrierItineraryElement cie = 
      createUICarrierItineraryElement(
        myState, task, task.getVerb(),
        new Date(startTime), new Date(endTime),
        fromLoc, toLoc);
    if (cie != null)
      toItineraryElementsVector.addElement(cie);
  }

  protected static Vector generateData(
      MyPSPState myState) throws  IOException {
    Vector v = new Vector();
    if (DEBUG) {
      System.out.println(myState.clusterID+" find Carrier Assets");
    }
    Iterator carrierAssetsIter = searchForCarrierAssets(myState).iterator();
    while (carrierAssetsIter.hasNext()) {
      Asset carrierAsset = (Asset)carrierAssetsIter.next();
      try {
        addCarrierItinerary(myState, v, carrierAsset);
      } catch (RuntimeException e) {
        System.err.println("CarrierItinerary for Asset UID: "+
           (carrierAsset.getUID().toString()) +
           " Exception: "+e+"; ignore!");
        e.printStackTrace();
      }
    }
    if (DEBUG) {
      System.out.println("done.");
    }
    return v;
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsXML() { return true; }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsHTML() { return false; }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/
  public String getDTD()  {
      return "";
  }

  /***************************************************************************
   * 
   **/
  public void subscriptionChanged(Subscription subscription) {
  }
}
