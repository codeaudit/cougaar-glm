/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.society.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.AbstractPrinter;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;
import org.cougaar.lib.util.UTILAsset;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;
import org.cougaar.domain.glm.util.AssetUtil;

/** need this for tracing itineraries allocated to other clusters... **/
import org.cougaar.domain.planning.ldm.plan.AllocationforCollections;

/**
 * This is a PSP that serves up UIItinerary objects for scheduled tasks 
 * from a given cluster.
 */

public class PSP_Itinerary extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   *  required by Class.newInstance()
   **/
  public PSP_Itinerary() {
    super();
    setDebug();
  }

  /*************************************************************************
   * 
   **/
  public PSP_Itinerary( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /**************************************************************************
   * 
   **/
  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /** minimal debug for potential errors **/
  public static final boolean MINI_DEBUG = false;

  /** DEBUG forced to off! **/
  public static final boolean DEBUG = false;
  protected boolean setDebug() {return DEBUG;}

  /****************************************************************************
   * Main execute method for PSP : Dispatch based on mode parameter
   **/
  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu)
    throws Exception 
  {
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);

    System.out.println("WILL: PSP_Itinerary Invoked..." + myState.mode);

    if (myState.mode == null) {
      displayUsage(myState, out);
      return;
    }

    try {
      AbstractPrinter pr = 
        AbstractPrinter.createPrinter(myState.format, out);
	  
      Vector results = generateData(myState);
      pr.printObject(results);
    } catch (Exception topLevelException) {
      System.err.println("PSP_Itinerary: Exception processing PSP_Itinerary: "+
         topLevelException);
      topLevelException.printStackTrace();
    }
  }

  /**
   * Define 'mode' argument tag<br>
   * Define the 'mode' constants : what kind of data source are you pulling from
   */
  public final static String MODE_TAG = "MODE";
  /** Retrieve from full TOPS Society **/
  public final static String LIVE = "LIVE";
  /** Retrive from simple GSS/SimpleMultileg Transporation Society **/
  public final static String GSS = "GSS";
  /** Return canned itineraries **/
  public final static String CANNED = "CANNED";
      
  private static Vector generateData(
      MyPSPState myState)
    throws Exception 
  {
    String mode = myState.mode;
    if (mode.equals(LIVE)) {
      return easyGenerateLiveData(myState);
      //      return generateLiveData(myState);
    } else if (mode.equals(GSS)) {
      return generateGSSData(myState);
    } else if (mode.equals(CANNED)) {
      return generateCannedData(myState);
    } else {
      System.err.println("PSP_Itinerary: Unknown mode : " + mode);
      return null;
    }
  }

  /** BEGIN HTML USAGE **/

  protected static void displayUsage(MyPSPState myState, PrintStream out) 
  {
    out.print(
     "<HTML><HEAD><TITLE>Task Itinerary PSP</TITLE></HEAD>\n"+
     "<BODY BGCOLOR=\"#DDDDDD\">\n"+
     "<H2><CENTER>Task Itineraries for Cluster ");
    out.print(myState.clusterID);
    out.print(
      "</CENTER></H2><P>\n"+
      "<TABLE>\n"+
      "<FORM METHOD=\"POST\" ACTION=\"");
    out.print(myState.cluster_psp_url);
    out.print(
      "?POST\">\n"+
      "<TR>\n"+
      "<TD align=right>Format</TD>\n"+
      "<TD>\n"+
      "<SELECT NAME=\"format\">\n"+
      "  <OPTION SELECTED VALUE=\"prettyXML\">"+
      "XML"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"prettyString\">"+
      "toString"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"XML\">"+
      "Short XML"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"Data\">"+
      "Raw Data"+
      "</OPTION>\n"+
      "</SELECT>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Mode</TD>\n"+
      "<TD>\n"+
      "<SELECT NAME=\"mode\">\n"+
      "  <OPTION SELECTED VALUE=\"");
    out.print(LIVE);
    out.print("\">"+
      "Live"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"");
    out.print(GSS);
    out.print("\">"+
      "GSS"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"");
    out.print(CANNED);
    out.print("\">"+
      "CANNED"+
      "</OPTION>\n"+
      "</SELECT>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Interpolate</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"checkbox\" NAME=\"interpolate\" VALUE=\"true\"><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Sort</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"checkbox\" NAME=\"sort\" VALUE=\"true\"><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Include UNLOAD</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"checkbox\" NAME=\"includeUnload\" VALUE=\"true\"><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Ignore Org Itineraries</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"checkbox\" NAME=\"ignoreOrgItineraryElements\" VALUE=\"true\"><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Ignore Carrier Itineraries</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"checkbox\" NAME=\"ignoreCarrierItineraryElements\" VALUE=\"true\"><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Optional ForUnit filter</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"text\" NAME=\"TransportedUnitName\" SIZE=20><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD align=right>Optional TaskUID filter</TD>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"text\" NAME=\"InputTaskUID\" SIZE=20><br>\n"+
      "</TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD>\n"+
      "<INPUT TYPE=\"submit\" NAME=\"FETCH\" VALUE=\"GET ITINERARIES!\">\n"+
      "</TD>\n"+
      "</TR>\n"+
      "</FORM>\n"+
      "</TABLE>\n"+
      "</BODY></HTML>\n");
  }

  /** END HTML USAGE **/

  /** BEGIN UTILITIES **/

  /** Constant for msec in a single day **/
  private final static long ONE_DAY = 86400000;

  private static GeolocLocation createGeolocLocation(
      ClusterObjectFactory cof, 
      String geoloc_name) 
  {
    String geoloc_code = "";
    String installation_code = "";
    String state_code = "USA";
    String state_name = "USA";
    String icao = "";
    double latitude = 40.0;
    double longitude = -70.0;

    if (geoloc_name.equals("TAA")) { // TAA
      geoloc_code = "KJAZ";
      latitude = 25.910556;
      longitude = 49.588889;
    } else if (geoloc_name.equals("HOME")) { // 3-69-ARBN
      geoloc_code = "HKUZ";
      latitude = 31.85;
      longitude = -81.6;
    } else if (geoloc_name.equals("APOD")) { // DHAHRAN (APOD)
      geoloc_code = "FFTJ";
      latitude = 26.26389;
      longitude = 50.15833;
    } else if (geoloc_name.equals("SPOD")) { // JUBAIL (SPOD)
      geoloc_code = "LWEV";
      latitude = 27;
      longitude = 49.6667;
    } else if (geoloc_name.equals("APOE")) { // Hunter AAF  (APOE)
      geoloc_code = "LEXG";
      latitude = 32.0097;
      longitude = -81.1461;
      icao = "KSVN";
    } else if (geoloc_name.equals("SPOE")) { // Savannah (SPOE)
      geoloc_code = "UZXJ";
      latitude = 32.0836;
      longitude = -81.1167;
    } else {
      System.err.println("PSP_Itinerary: Unrecognized geoloc_name : "+
          geoloc_name);
    }

    NewGeolocLocation loc = GLMFactory.newGeolocLocation();
    loc.setGeolocCode(geoloc_code);
    loc.setInstallationTypeCode(installation_code);
    loc.setCountryStateCode(state_code);
    loc.setCountryStateName(state_name);
    loc.setIcaoCode(icao);
    loc.setLatitude(Latitude.newLatitude(latitude));
    loc.setLongitude(Longitude.newLongitude(longitude));

    return loc;
  }

  // Create a UITaskItinineraryElement from given components
  private static UITaskItineraryElement createUITaskItineraryElement(
      GeolocLocation source_loc,
      GeolocLocation dest_loc,
      Date departure,
      Date arrival,
      int transportation_mode,
      String carrier_type,
      String carrier_name) 
  {
    UITaskItineraryElementCarrier leg = new UITaskItineraryElementCarrier();

    leg.setVerbRole(Constants.Verb.Transport); // Should this be the transportation mode?
    leg.setStartLocation(source_loc);
    leg.setEndLocation(dest_loc);
    leg.setStartDate(departure);
    leg.setEndDate(arrival);
    leg.setCarrierUID("Cluster/777");
    leg.setCarrierTypeNomenclature(carrier_type);
    leg.setCarrierItemNomenclature(carrier_name);
    leg.setTransportationMode(transportation_mode);

    return leg;
  }

  // Create a UITaskItinerary object from given components
  private static UITaskItinerary createUITaskItinerary(
      ClusterObjectFactory cof,
      String source_geoloc_name,
      String unit_name, 
      String carrier_type_name, 
      String carrier_item_name, 
      String transported_uid, 
      String transported_name,
      String transported_asset_type_id,
      String transported_asset_item_id,
      int transported_asset_quantity,
      double transported_tons,
      int[] transported_asset_classes,
      Date departure,
      int transportation_mode) 
  {
    UITaskItinerary itin = new UITaskItinerary();        
        
    itin.setAllocTaskUID("");
    itin.setClusterID("");
    itin.setInputTaskUID("");
    itin.setScheduleElementType(ScheduleElementType.LOCATIONRANGE);
    itin.setScheduleType(ScheduleType.OTHER);
    GeolocLocation source_loc = createGeolocLocation(cof, source_geoloc_name);
    GeolocLocation dest_loc = createGeolocLocation(cof, "TAA");
    GeolocLocation poe = createGeolocLocation(cof, "SPOE");
    GeolocLocation pod = createGeolocLocation(cof, "SPOD");

    Date poe_arrival = new Date(departure.getTime() + 1l*ONE_DAY);
    Date poe_departure = new Date(poe_arrival.getTime() + 1l*ONE_DAY);
    Date pod_arrival = new Date(poe_departure.getTime() + 15l*ONE_DAY);
    Date pod_departure = new Date(pod_arrival.getTime() + 1l*ONE_DAY);
    Date arrival = new Date(pod_departure.getTime() + 1l*ONE_DAY);
    if (transportation_mode == UITaskItineraryElement.AIR_MODE) {
      poe_departure = poe_arrival;
      pod_arrival = new Date(poe_departure.getTime() + 1l*ONE_DAY);
      pod_departure = pod_arrival;
      arrival = new Date(pod_departure.getTime() + 1l*ONE_DAY);
      poe = createGeolocLocation(cof, "APOE");
      pod = createGeolocLocation(cof, "APOD");
    }

    UITaskItineraryElement leg1 = 
        createUITaskItineraryElement(source_loc, poe, departure, poe_arrival, 
                                     UITaskItineraryElement.GROUND_MODE, 
                                     "Truck", "VIN-1");
    itin.getScheduleElements().addElement(leg1);

    UITaskItineraryElement leg2 = 
        createUITaskItineraryElement(poe, pod, poe_departure, pod_arrival,
                                     transportation_mode,
                                     carrier_type_name, carrier_item_name);
    itin.getScheduleElements().addElement(leg2);

    UITaskItineraryElement leg3 = 
        createUITaskItineraryElement(pod, dest_loc, pod_departure, arrival,
                                     UITaskItineraryElement.GROUND_MODE,
                                     "Truck", "VIN-2");
    itin.getScheduleElements().addElement(leg3);

    itin.setAnnotation("Annotation");

    itin.TransportedUnitName = unit_name;

    itin.setUITAssetInfoVector(
      getUITAssetInfoVector(
        transported_uid,
        transported_name,
        transported_asset_type_id,
        transported_asset_item_id,
        transported_asset_quantity,
	transported_tons,
	transported_asset_classes));

    itin.fromRequiredLocation = source_loc;
    itin.toRequiredLocation = dest_loc;
    itin.earliestPickupDate = departure;
    itin.latestDropoffDate = arrival;

    return itin;
  }

  private static boolean setElementLocations(
      UITaskItineraryElement legStart, 
      UITaskItineraryElement legEnd, 
      Task task) {
    // from
    PrepositionalPhrase prepFrom=task.getPrepositionalPhrase(Constants.Preposition.FROM);
    legStart.setStartLocation((GeolocLocation)prepFrom.getIndirectObject());
    // to
    PrepositionalPhrase prepTo = task.getPrepositionalPhrase(Constants.Preposition.TO);
    legEnd.setEndLocation((GeolocLocation)prepTo.getIndirectObject());
    return true;
  }

  private static boolean setElementDates(
      UITaskItineraryElement legStart, 
      UITaskItineraryElement legEnd, 
      Task task) {
    // estimated dates
    PlanElement pe = task.getPlanElement();
    if (pe != null) {
      AllocationResult est = pe.getEstimatedResult();
      if (est != null) {
        if (!est.isDefined(AspectType.START_TIME)) {
          System.err.println("PSP_Itinerary: Task with UID: "+
              (task.getUID().toString())+" missing START_TIME; ignored!");
          return false;
        }
        legStart.setStartDate(
          new Date((long)est.getValue(AspectType.START_TIME)));
        if (!est.isDefined(AspectType.END_TIME)) {
          System.err.println("PSP_Itinerary: Task with UID: "+
              (task.getUID().toString())+" missing END_TIME; ignored!");
          return false;
        }
        legEnd.setEndDate(
          new Date((long)est.getValue(AspectType.END_TIME)));
      }
    }
    return true;
  }

  private static boolean setElementDatesRange(
      UITaskItineraryElement legStart, 
      UITaskItineraryElement legEnd, 
      Task task) {
    // preference dates
    Enumeration enpref = task.getPreferences();
    while (enpref.hasMoreElements()) {
      Preference pref = (Preference)enpref.nextElement();
      int type = pref.getAspectType();
      if (type == AspectType.START_TIME) {
        ScoringFunction sf = pref.getScoringFunction();
        AspectScorePoint bestP = sf.getBest();
        legStart.setStartEarliestDate(new Date((long)bestP.getValue()));
      } else if (type == AspectType.END_TIME) {
        ScoringFunction sf = pref.getScoringFunction();
        AspectScorePoint bestP = sf.getBest();
        legEnd.setEndBestDate(new Date((long)bestP.getValue()));
        try {
          Enumeration rangeEn = sf.getValidRanges(null, null);
          AspectScoreRange range =
            (AspectScoreRange)rangeEn.nextElement();
          AspectScorePoint startP = range.getRangeStartPoint();
          AspectScorePoint endP = range.getRangeEndPoint();
          legEnd.setEndEarliestDate(new Date((long)startP.getValue()));
          legEnd.setEndLatestDate(new Date((long)endP.getValue()));
        } catch (Exception e) {
          System.err.println(
            "PSP_Itinerary: End eariest/latest preference problem: "+e+
            " from task: "+task.getUID().getUID()+" scoring function: "+
            sf.getClass().getName()+" is "+sf);
        }
      }
    }
    return true;
  }

  private static boolean setElementSchedule(
      UITaskItineraryElement legStart, 
      UITaskItineraryElement legEnd, 
      Task task,
      boolean definitelyNotDirect,
      boolean definitelyOverlap) {

      // Since there are no intinerary elements for this stuff
      // these values were determined directly from the 
      // task
      if (definitelyNotDirect) {
	  legStart.setIsDirectElement(false);
	  legEnd.setIsDirectElement(false);
      }
      else {
	  legStart.setIsDirectElement(true);
	  legEnd.setIsDirectElement(true);
      }
      if (definitelyOverlap) {
	  legStart.setIsOverlapElement(true);
	  legEnd.setIsOverlapElement(true);
      }
      else {
	  legStart.setIsOverlapElement(false);
	  legEnd.setIsOverlapElement(false);
      }
      
    return 
      (setElementLocations(legStart, legEnd, task) &&
       setElementDates(legStart, legEnd, task) &&
       setElementDatesRange(legStart, legEnd, task));
  }

  private static boolean setElementSchedule(
      UITaskItineraryElement leg, 
      Task task,
      boolean definitelyNotDirect,
      boolean definitelyOverlap) {
    return setElementSchedule(leg, leg, task,
			      definitelyNotDirect,
			      definitelyOverlap);
  }

  /** This approach to finding the Mode is incorrect! **/
  private static int getFakedMode(Task task) {
    PrepositionalPhrase prepMode;
    Object modeIndObj;
    if (((prepMode = 
          task.getPrepositionalPhrase("TOPSGLOBALMODE_MODE")) != null) &&
        ((modeIndObj = 
          prepMode.getIndirectObject()) instanceof String)) {
      String sMode = (String)modeIndObj;
      return
        (sMode.equals("SEA") ?
         UITaskItineraryElement.SEA_MODE : 
         (sMode.equals("AIR") ?
          UITaskItineraryElement.AIR_MODE : 
          (sMode.equals("GROUND") ?
           UITaskItineraryElement.GROUND_MODE :
           UITaskItineraryElement.NONE_MODE)));
    } else {
      Verb taskVerb = task.getVerb();
      return
        (taskVerb.equals("TransportBySea") ?
         UITaskItineraryElement.SEA_MODE : 
         (taskVerb.equals("TransportByAir") ?
          UITaskItineraryElement.AIR_MODE : 
          (taskVerb.equals("TransportByGround") ?
           UITaskItineraryElement.GROUND_MODE :
           UITaskItineraryElement.NONE_MODE)));
    }
  }

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

  private static int[] getAssetClasses(Asset a){
    if(a instanceof AggregateAsset){
      a=((AggregateAsset)a).getAsset();
    }

    if (AssetUtil.isPallet(a)){
      AssetGroup ag = (AssetGroup) ((GLMAsset)a).getScheduledContentPG().getAsset();
      if(ag == null){
	int[] ret = new int[1];
	ret[0]=0;
	return ret;
      }
      Vector assets = ag.getAssets();
      BitSet b = new BitSet(11);
      for(int i=0;i<assets.size();i++){
	int[] ac = getAssetClasses((Asset)assets.get(i));
	for(int j=0;j<ac.length;j++)
	  b.set(ac[j]);
      }
      if(b.length()==0){
	int[] ret=new int[1];
	ret[0]=0;
	return ret;
      }else{
	int[] ret=new int[b.length()];
	int j=0;
	for(int i=0;i<b.length();i++){
	  if(b.get(i))
	    ret[j++]=i;
	}
      }
    }
    int[] ret=new int[1];
    ret[0]=(a instanceof ClassISubsistence)?1:
      (a instanceof ClassIIClothingAndEquipment)?2:
      (a instanceof ClassIIIPOL)?3:
      (a instanceof ClassIVConstructionMaterial)?4:
      (a instanceof ClassVAmmunition)?5:
      (a instanceof ClassVIPersonalDemandItem)?6:
      (a instanceof Container)?12:
      (a instanceof org.cougaar.domain.glm.ldm.asset.Package)?13:
      (a instanceof ClassVIIMajorEndItem)?7:
      (a instanceof ClassVIIIMedical)?8:
      (a instanceof ClassIXRepairPart)?9:
      (a instanceof ClassXNonMilitaryItem)?10:
      (((GLMAsset)a).hasPersonPG())?11:
      0;
    return ret;
  }

  /**
   * How can one tell "Ground/Sea/Air" from an Organization?<br>
   * Roles are <i>not</i> standardized!
   * <p>
   * <b>Hack</b>: These roles are not standardized!
   */
  private static HashMap rolenameToMode;
  static {
    // table of {{Mode, Roles}, {Mode, Roles}, ...}
    Object[][] rolenameToModeTable = {
      { // ground
        new Integer(UITaskItineraryElement.GROUND_MODE),
        new String[] {
          "CommercialGroundTransportationProvider",
          "GroundTransportationProvider",
          "TheaterGroundTransportationProvider",
          "TheaterStrategicTransportationProvider", // <-- ASSUME GROUND!
          "TranscapProvider"
        },
      },
      { // sea
        new Integer(UITaskItineraryElement.SEA_MODE),
        new String[] {
          "AmmunitionSeaPort",
          "GenericSeaPort",
          "SeaTransportationProvider",
          "TheaterSeaTransportationProvider"
        },
      },
      { // air
        new Integer(UITaskItineraryElement.AIR_MODE),
        new String[] {
          "AirCrewProvider",
          "AirTransportationProvider",
          "C-141TransportProvider",
          "C-17TransportProvider",
          "C-5TransportProvider",
          "OrganicAirTransportationProvider",
          "TheaterAirTransportationProvider",
          "VirtualAirTransportationProvider"
        },
      },
      { // none -- not descriptive enough
        new Integer(UITaskItineraryElement.NONE_MODE),
        new String[] {
        },
      },
      { // ignore -- these don't indicate a mode
        new Integer(-1),
        new String[] {
          "TransportationProvider",
          "StrategicTransportationProvider",
          "TheaterTransportationProvider"
        },
      }
    };
    // put all in map
    rolenameToMode = new HashMap();
    for (int i = 0; i < rolenameToModeTable.length; i++) {
      Integer mode = (Integer)rolenameToModeTable[i][0];
      String[] rolenames = (String[])rolenameToModeTable[i][1];
      for (int j = 0; j < rolenames.length; j++) {
        rolenameToMode.put(rolenames[j].intern(), mode);
      }
    }
  }

  /**
   * Use roleToMode hashmap for now.
   */
  private static int getOrganizationMode(Organization org) {
    Iterator roleIter = org.getOrganizationPG().getRoles().iterator();
    while (roleIter.hasNext()) {
      Role role = (Role)roleIter.next();
      Object mode = rolenameToMode.get(role.getName());
      if (mode instanceof Integer) {
        int i = ((Integer)mode).intValue();
        if (i >= 0) {
          if (DEBUG) {
            System.out.println("getOrganizationMode(org "+
              org.getItemIdentificationPG().getItemIdentification()+
              " uid: "+org.getUID().toString()+
              " role: "+role+") --> mode: "+i);
          }
          return i;
        }
      } else {
        System.err.println(
            "PSP_Itinerary: Unknown Role: "+role+
            " in org: "+
            org.getItemIdentificationPG().getItemIdentification()+
            " uid: "+org.getUID().toString());
        // maybe use sync map and put(role, -1) to be quiet?
      }
    }
    System.err.println(
        "PSP_Itinerary: Organization \"MODE\" not known for org: "+
        org.getItemIdentificationPG().getItemIdentification()+
        " uid: "+org.getUID().toString());
    return UITaskItineraryElement.NONE_MODE;
  }

  /**
   * How can one tell "Ground/Sea/Air" from a physical asset?<br>
   * PG usage is <i>not</i> consistent!
   * <p>
   * <b>Hack</b>: Classes can be subclassed or replaced!
   */
  private static HashMap classnameToMode;
  static {
    // table of {{Mode, classnames}, {Mode, classnames}, ...}
    Object[][] classnameModeTable = {
      { // ground
        new Integer(UITaskItineraryElement.GROUND_MODE),
        new String[] {
          "org.cougaar.domain.glm.ldm.asset.RailCar",
          "org.cougaar.domain.glm.ldm.asset.SelfPropelledGroundWeapon",
          "org.cougaar.domain.glm.ldm.asset.Trailer",
          "org.cougaar.domain.glm.ldm.asset.Train",
          "org.cougaar.domain.glm.ldm.asset.TrainEngine",
          "org.cougaar.domain.glm.ldm.asset.Truck"
        },
      },
      { // sea
        new Integer(UITaskItineraryElement.SEA_MODE),
        new String[] {
          "org.cougaar.domain.glm.ldm.asset.CargoShip",
          "org.cougaar.domain.glm.ldm.asset.FightingShip"
        },
      },
      { // air
        new Integer(UITaskItineraryElement.AIR_MODE),
        new String[] {
          "org.cougaar.domain.glm.ldm.asset.CargoFixedWingAircraft",
          "org.cougaar.domain.glm.ldm.asset.CargoRotaryWingAircraft"
        },
      },
      { // none -- not descriptive enough
        new Integer(UITaskItineraryElement.NONE_MODE),
        new String[] {
        },
      },
      { // ignore -- these don't indicate a mode
        new Integer(-1),
        new String[] {
          "org.cougaar.domain.planning.ldm.asset.Asset",
          "org.cougaar.domain.glm.ldm.asset.CargoVehicle",
          "org.cougaar.domain.glm.ldm.asset.ClassVIIMajorEndItem",
          "org.cougaar.domain.glm.ldm.asset.PhysicalAsset"
        },
      }
    };
    // put all in map
    classnameToMode = new HashMap();
    for (int i = 0; i < classnameModeTable.length; i++) {
      Integer mode = (Integer)classnameModeTable[i][0];
      String[] classnames = (String[])classnameModeTable[i][1];
      for (int j = 0; j < classnames.length; j++) {
        classnameToMode.put(classnames[j], mode);
      }
    }
  }

  /**
   * First try classname hashmap, then PGs.
   */
  private static int getPhysicalAssetMode(Asset phys) {
    // try hashmap
    String classname = phys.getClass().getName();
    Object hashedMode = classnameToMode.get(classname);
    int mode;
    if (hashedMode instanceof Integer) {
      mode = ((Integer)hashedMode).intValue();
      if (mode >= 0) {
        return mode;
      }
    }
    if (phys instanceof GLMAsset) {
      GLMAsset pa = (GLMAsset) phys;
      // look at PGs
      if (pa.hasGroundVehiclePG()) {
        mode = UITaskItineraryElement.GROUND_MODE;
      } else if (pa.hasRailVehiclePG()) {
        mode = UITaskItineraryElement.GROUND_MODE;
      } else if (pa.hasWaterVehiclePG()) {
        mode = UITaskItineraryElement.SEA_MODE;
      } else if (pa.hasAirVehiclePG()) {
        mode = UITaskItineraryElement.AIR_MODE;
      } else if (pa.hasMovabilityPG()) {
        // isn't movability for cargo usage, as opposed to carrier usage?
        char c;
        try {
          c = pa.getMovabilityPG().getCargoCategoryCode().charAt(0);
        } catch (Exception e) {
          System.err.println("PSP_Itinerary: Invalid MovabilityPG: "+
                             pa.getUID().toString());
          c = '_';
        }
        if ((c == 'A') || (c == 'R'))
          mode = UITaskItineraryElement.GROUND_MODE;
        else if (c == 'C')
          mode = UITaskItineraryElement.SEA_MODE;
        else if (c == 'B')
          mode = UITaskItineraryElement.AIR_MODE;
        else {
          System.err.println(
                             "PSP_Itinerary: Physical Asset \"MODE\" not known for asset: "+
                             pa.getItemIdentificationPG().getItemIdentification()+
                             " uid: "+pa.getUID().toString());
          mode = UITaskItineraryElement.NONE_MODE;
        }
      } else {
        System.err.println(
                           "PSP_Itinerary: Physical Asset \"MODE\" not known for asset: "+
                           pa.getItemIdentificationPG().getItemIdentification()+
                           " uid: "+pa.getUID().toString());
        mode = UITaskItineraryElement.NONE_MODE;
      }
      if (DEBUG) {
        System.out.println("getPhysicalAssetMode(assetID: "+
                           pa.getItemIdentificationPG().getItemIdentification()+
                           " uid: "+pa.getUID().toString()+
                           ") --> mode: "+mode);
      }
    } else {
      mode = UITaskItineraryElement.NONE_MODE;
    }
    // can't really hash this mode...
    return mode;
  }

  private static UITaskItineraryElement createUITaskItineraryElement(
      MyPSPState myState, Allocation carrierAlloc) {
    Asset carrierAsset;
    try {
      carrierAsset = carrierAlloc.getAsset();
    } catch (Exception eBadAlloc) {
      System.err.println(
        "PSP_Itinerary has null allocation? alloc: "+carrierAlloc);
      return null;
    }
    if (carrierAsset == null) {
      // ? Not allocated...
      if (MINI_DEBUG) {
        System.out.println("TaskItinerary allocated to null? Task: "+
          (carrierAlloc.getTask().getUID().toString()));
      }
    } else if (carrierAsset instanceof Organization) {
      if (myState.ignoreOrgLegs) {
        // user doesn't want these Org legs!
        return null;
      }
      // handed off to an Organization
      AllocationforCollections traceableAlloc = 
        (AllocationforCollections)carrierAlloc;
      Task at = (Task)traceableAlloc.getAllocationTask();
      if (at != null) {
        if (DEBUG) {
          System.out.println("Allocated to Org Asset: "+
            carrierAsset.getUID().toString());
        }
        UITaskItineraryElementOrg leg = new UITaskItineraryElementOrg();
        // be hyper about exceptions
        // set allocated cluster name
        try {
          // PSP_TransportTaskTrace "verifies" the clusterName ... needed?
          leg.setAllocatedClusterName(at.getDestination().cleanToString());
        } catch (Exception eBadOrgAllocDest) {
          System.err.println(
            "PSP_Itinerary found bad Org allocation Destination: "+
            eBadOrgAllocDest+" from Task: "+at);
          return null;
        }
        // set allocated task UID
        try {
          leg.setAllocatedTaskID(at.getUID().toString());
        } catch (Exception eBadOrgAllocUID) {
          System.err.println(
            "PSP_Itinerary found bad Org allocation UID: "+
            eBadOrgAllocUID+" from Task: "+at);
          return null;
        }
        // set mode
        try {
          leg.setTransportationMode(
            getOrganizationMode((Organization)carrierAsset));
        } catch (Exception eBadOrgMode) {
          System.err.println(
            "PSP_Itinerary found bad Org allocation Mode: "+
            eBadOrgMode+" from org: "+carrierAsset);
          return null;
        }
        // Valid org leg
        return leg;
      } else {
        System.err.println("PSP_Itinerary: Task with UID: "+
          (carrierAlloc.getTask().getUID().toString())+
          " allocated to Org but allocationTask is null");
      }
    } else {
      if (myState.ignoreCarrierLegs) {
        // user doesn't want these Carrier legs!
        return null;
      }
      if (DEBUG) {
        System.out.println("Allocated to Carrier Asset: "+
          carrierAsset.getUID().toString());
      }
      // allocated to a physical asset
      UITaskItineraryElementCarrier leg = 
        new UITaskItineraryElementCarrier();
      // be hyper about exceptions
      // set UID
      try {
        leg.setCarrierUID(carrierAsset.getUID().toString());
      } catch (Exception eBadPhysUID) {
        System.err.println("PSP_Itinerary unable to get carrier UID: "+
            eBadPhysUID+" from asset "+carrierAsset);
        return null;
      }
      // set type ID
      try {
        leg.setCarrierTypeNomenclature(
          carrierAsset.getTypeIdentificationPG().getTypeIdentification());
	//	System.out.println ("Carrier asset " + carrierAsset);
	//	if (!((GLMAsset)carrierAsset).hasPhysicalPG ())
	//	    System.out.println ("!!! Not Physical Asset !!!");
      } catch (Exception eBadPhysTypeID) {
        System.err.println("PSP_Itinerary unable to get carrier TypeID: "+
            eBadPhysTypeID+" from asset "+carrierAsset);
        return null;
      }
      // set item ID
      try {
        leg.setCarrierItemNomenclature(
          carrierAsset.getItemIdentificationPG().getItemIdentification());
      } catch (Exception eBadPhysItemID) {
        System.err.println("PSP_Itinerary unable to get carrier ItemID: "+
            eBadPhysItemID+" from asset "+carrierAsset);
        return null;
      }
      // set mode
      try {
        leg.setTransportationMode(
          getPhysicalAssetMode(carrierAsset));
      } catch (Exception eBadPhysMode) {
        System.err.println("PSP_Itinerary unable to get carrier MODE: "+
            eBadPhysMode+" from asset "+carrierAsset);
        return null;
      }
      // Valid carrier leg
      return leg;
    }
    if (MINI_DEBUG) {
      System.out.println("RETURNING NULL!");
    }
    return null;
  }

  /** 
   * Make leg from ItineraryElement
   */
  protected static boolean addLegFromItineraryElement(
      List toLegsList, 
      UITaskItineraryElement leg, 
      ItineraryElement ie,
      boolean isDefinitelyNotDirect,
      boolean isDefinitelyOverlap)
  {
    try {
      // take the info from the itinerary element
      leg.setStartLocation((GeolocLocation)ie.getStartLocation());
      leg.setStartAllDates(ie.getStartDate());
      leg.setEndLocation((GeolocLocation)ie.getEndLocation());
      leg.setEndAllDates(ie.getEndDate());

      if (isDefinitelyNotDirect)
	  leg.setIsDirectElement(false);
      else if (ie.getRole() == Constants.Verb.Transit)
	  leg.setIsDirectElement(false);
      else
	  leg.setIsDirectElement(true);
	
      if (isDefinitelyOverlap)
	  leg.setIsOverlapElement(true);
      // If individual legs are ever marked for overlap
      // put the marking in here!!
      //else if (ie.getRole() == Constants.Verb.Overlap)
      //leg.setIsOverlap(true);
      else
	  leg.setIsOverlapElement(false);

      // add to list
     
      toLegsList.add(leg);
    } catch (Exception eBadIE) {
      System.err.println("PSP_Itinerary: Itinerary Element: "+ie+
         " is broken: "+eBadIE);
      return false;
    }
    return true;
  }

  /** Should be in TransportationLink! **/
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

  /**
   * Given UITaskItineraryElement, add UITaskItinElems based on the template leg
   * and interpolated with task Route information.
   * @returns true if successful
   */
  protected static boolean addLegsInterpolateRoute(
      List toLegsList, UITaskItineraryElement templateLeg,
      Date startDate, Date endDate, TransportationRoute transRoute,
      boolean definitelyNotDirect,
      boolean definitelyOverlap) {
    long startTime;
    double totalTime;
    try {
      // get dates and total duration
      startTime = startDate.getTime();
      totalTime = (double)(endDate.getTime() - startTime);
    } catch (Exception e) {
      System.err.println("PSP_Itinerary: Interpolate route: "+transRoute+
          " can't get dates: "+e); 
      return false;
    }
    // get links
    Vector routeLinks;
    try {
      routeLinks = transRoute.getLinks();
      if (DEBUG) {
        System.out.println("ROUTE NLINKS: "+routeLinks.size()+
          " VALUE: "+routeLinks);
      }
    } catch (Exception e) {
      System.err.println("PSP_Itinerary: Interpolate route: "+transRoute+
          " can't get links: "+e);
      return false;
    }
    int linkIndexMax = routeLinks.size() - 1;
    if (linkIndexMax <= 0) {
      if (linkIndexMax == 0) { 
        // one link
        TransportationLink transLink = 
          (TransportationLink)routeLinks.elementAt(0);
        UITaskItineraryElement leg = templateLeg;
        leg.setStartLocation(transLink.getOrigin().getGeolocLocation());
        leg.setStartAllDates(startDate);
        leg.setEndLocation(transLink.getDestination().getGeolocLocation());
        leg.setEndAllDates(endDate);

      // Since there are no intinerary elements for this stuff
      // these values were determined directly from the 
      // task
	if (definitelyNotDirect) {
	  leg.setIsDirectElement(false);
	}
	else {
	    leg.setIsDirectElement(true);
	}
	if (definitelyOverlap) {
	    leg.setIsOverlapElement(true);
	}
	else {
	    leg.setIsOverlapElement(false);
	}

        // interpolatedInfo is (by default) INTERPOLATED_NEITHER
        toLegsList.add(leg);
        return true;
      } else {
        if (MINI_DEBUG) {
          System.out.println("Route has no links!  route: "+transRoute);
        }
        return false;
      }
    }
    // (have more than one link)
    int linkIndex;
    // sum distance
    double totalDistance = 0.0;
    Enumeration en;
    try {
      linkIndex = 0;
      do {
        TransportationLink transLink = 
          (TransportationLink)routeLinks.elementAt(linkIndex);
        totalDistance += getLinkLength(transLink);
      } while (++linkIndex <= linkIndexMax);
    } catch (Exception e) {
      System.err.println("PSP_Itinerary: Interpolate route: "+transRoute+
          " can't sum distance: "+e);
      return false;
    }
    // create legs, time separated by factor of distance
    try {
      // I unrolled a loop here to make object cloning and information 
      // taken from start/end dates v.s. route clear.  Could be clever
      // with (prev_end_loc == start_next_loc) but instead verify route.
      TransportationLink transLink;
      UITaskItineraryElement leg;
      double currentDistance;
      Date prevDate;
      // first leg
      {
        transLink = (TransportationLink)routeLinks.elementAt(0);
        leg = templateLeg.copy();
        leg.setStartLocation(transLink.getOrigin().getGeolocLocation());
        leg.setStartAllDates(startDate);
        currentDistance = getLinkLength(transLink);
        long currentTime = startTime + 
           (long)((currentDistance/totalDistance)*totalTime);
        leg.setEndLocation(transLink.getDestination().getGeolocLocation());
        leg.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_END);
        prevDate = new Date(currentTime);
        leg.setEndAllDates(prevDate);
	if (definitelyNotDirect) {
	  leg.setIsDirectElement(false);
	}
	else {
	    leg.setIsDirectElement(true);
	}
	if (definitelyOverlap) {
	    leg.setIsOverlapElement(true);
	}
	else {
	    leg.setIsOverlapElement(false);
	}


        toLegsList.add(leg);
      }
      // middle legs
      for (linkIndex = 1; linkIndex < linkIndexMax; linkIndex++) {
        transLink = (TransportationLink)routeLinks.elementAt(linkIndex);
        leg = templateLeg.copy();
        leg.setStartLocation(transLink.getOrigin().getGeolocLocation());
        leg.setStartAllDates(prevDate);
        currentDistance += getLinkLength(transLink);
        long currentTime = startTime + 
           (long)((currentDistance/totalDistance)*totalTime);
        leg.setEndLocation(transLink.getDestination().getGeolocLocation());
        leg.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_BOTH);
        prevDate = new Date(currentTime);
        leg.setEndAllDates(prevDate);
	if (definitelyNotDirect) {
	  leg.setIsDirectElement(false);
	}
	else {
	    leg.setIsDirectElement(true);
	}
	if (definitelyOverlap) {
	    leg.setIsOverlapElement(true);
	}
	else {
	    leg.setIsOverlapElement(false);
	}

        toLegsList.add(leg);
      }
      // last leg
      {
        transLink = (TransportationLink)routeLinks.elementAt(linkIndexMax);
        leg = templateLeg;
        leg.setStartLocation(transLink.getOrigin().getGeolocLocation());
        leg.setStartAllDates(prevDate);
        leg.setEndLocation(transLink.getDestination().getGeolocLocation());
        leg.setEndAllDates(endDate);
        leg.setInterpolatedInfo(UIItineraryElement.INTERPOLATED_START);
	if (definitelyNotDirect) {
	  leg.setIsDirectElement(false);
	}
	else {
	    leg.setIsDirectElement(true);
	}
	if (definitelyOverlap) {
	    leg.setIsOverlapElement(true);
	}
	else {
	    leg.setIsOverlapElement(false);
	}

        toLegsList.add(leg);
      }
    } catch (Exception e) {
      System.err.println("PSP_Itinerary: Interpolate route: "+transRoute+
         " unable to interpolate: "+e+
         " link number: "+linkIndex+" of "+linkIndexMax);
      return false;
    }
    return true;
  }

  /** 
   * Interpolate and patch itinerary from ItineraryElement.
   * Note that this method steals the templateLeg!
   */
  protected static boolean addLegsInterpolate(
      List toLegsList, 
      UITaskItineraryElement templateLeg, 
      Task task, 
      ItineraryElement transIE,
      boolean definitelyNotDirect,
      boolean definitelyOverlap)
  {
    // get task's route -- only one route per task, so
    // the PSP is expecting that only one interpolate is
    // used.
    TransportationRoute route = getTransportationRoute(task);
    if (route == null) {
      // just take the itineraryElement
      return 
        addLegFromItineraryElement(
          toLegsList, templateLeg, transIE, 
	  definitelyNotDirect,
	  definitelyOverlap);
    }
    // use task's route to interpolate legs
    // discard all other non-transit itinerary stages
    int oldsize = toLegsList.size();
    if (!(addLegsInterpolateRoute(
            toLegsList, templateLeg,
            transIE.getStartDate(), transIE.getEndDate(), route,
	    definitelyNotDirect,
	    definitelyOverlap))) {
      return false;
    }
    // check that legs were added
    int newsize = toLegsList.size();
    if (newsize <= oldsize) {
      // no legs added?
      if (MINI_DEBUG) {
        System.out.println(
          "TaskItinerary tried interpolate but no legs?");
      }
      return false;
    }
    // patch start earliest and end earliest/best/last
    // NOTE: This is somewhat fake!  Might hide meaningful
    // non-transit itineraries that take time (e.g. load)!
    UITaskItineraryElement firstLeg = 
      (UITaskItineraryElement)toLegsList.get(oldsize);
    UITaskItineraryElement lastLeg = 
      (UITaskItineraryElement)toLegsList.get(newsize-1);
    if (!(setElementDatesRange(
            firstLeg,
            lastLeg,
            task))) {
      if (MINI_DEBUG) {
        System.out.println(
          "TaskItinerary unable to set leg date ranges?");
      }
      return false;
    }
    // success
    return true;
  }

  /** 
   * Convert allocation to leg(s) and add to List.
   * <p>
   * This method is called once all Allocations have been found
   * by the <code>searchForLegs</code> method.
   * <p>
   * @param toLegsList add UITaskItinElems to this List
   * @param alloc Allocation that has Schedule info for leg
   */
  protected static void addLegsAlloc(
      MyPSPState myState,
      List toLegsList, 
      Allocation alloc) {
    // create template leg
    UITaskItineraryElement leg;
    try {
      leg = createUITaskItineraryElement(myState, alloc);
    } catch (RuntimeException badAlloc) {
      leg = null;
    }
    if (leg == null) {
      // leg was still being calculated or broken.  silently ignore it.
      if (DEBUG) {
        System.out.println("Ignore leg for alloc: "+
          alloc.getUID().getUID());
      }
      return;
    }
    Task task = alloc.getTask();

    // look for itinerary info.

    // BEGIN "IMPLIED" FIX

    // find "Transport" schedule elements
    int nLegs = 0;
    PrepositionalPhrase prepItinerary =
      task.getPrepositionalPhrase(
         Constants.Preposition.ITINERARYOF);
    if (prepItinerary != null) {
      Object itinIndObj = prepItinerary.getIndirectObject();
      if (itinIndObj instanceof Schedule) {
        Enumeration schedElems = 
           ((Schedule)itinIndObj).getAllScheduleElements();
        while (schedElems.hasMoreElements()) {
	  ItineraryElement ie = 
	    (ItineraryElement)schedElems.nextElement();
	  Verb v = ie.getRole();
	  if ((Constants.Verb.Transport).equals(v)) {
            // found a transport leg
            //
            // "interpolate" forced OFF!!!
            nLegs++;
            addLegFromItineraryElement(
              toLegsList, leg.copy(), ie,
              false, false);
	  } else if ((Constants.Verb.Fuel).equals(v)) {
            // found a fuel leg
            nLegs++;
            addLegFromItineraryElement(
              toLegsList, leg.copy(), ie,
              false, false);
          } else {
            // ignore; we only want "transport" and "fuel"
          }
        }
      }
    }

    if (nLegs == 0) {
      // no legs in the itinerary
      //
      // at least make an "overall" leg
      if (setElementSchedule(
            leg, task, 
            false, false)) {
        toLegsList.add(leg);
      }
    }

    // done.  ignore the "ancient" code below...
    return;

    /*
     * DISABLE FOR "IMPLIED" FIX

    // check for simple case
    if (!myState.interpolate && !myState.includeUnload) {
      // build non-interpolated leg from task
      if (setElementSchedule(leg, task, false, false)) {
        toLegsList.add(leg);
      }
      return;
    }

    // currently we're only interested in transit and unload
    ItineraryElement transIE = null;
    ItineraryElement unloadIE = null;
    ItineraryElement loadIE = null;

    // If tasks ever get marked for indirect, but the check in here
    // Check the task for the indirect intinerary Preposition
    boolean isDefinitelyNotDirect = false;

    // IF tasks ever get marked for overlap, put the check in here
    // Tasks that have passed our filter, and have verb of supply
    // get marked as overlap
    boolean isDefinitelyOverlap =  false;
  
    if (prepItinerary != null) {
      Object itinIndObj = prepItinerary.getIndirectObject();
      if (itinIndObj instanceof Schedule) {

        // will use itinerary and task route for interpolation
        // make sure that schedElems includes both Transport and Load
        Enumeration schedElems = 
           ((Schedule)itinIndObj).getAllScheduleElements();

	// check if it's an air itinerary, in which case, we probably want
	// all the legs
	List elems = new ArrayList ();
        while (schedElems.hasMoreElements())
	  elems.add(schedElems.nextElement());
		
       if (elems.size () > 2) {
	  for (int i = 0; i < elems.size (); i++) {
	    ItineraryElement ie = (ItineraryElement) elems.get(i);
	    Verb v = ie.getRole();
	    addLegFromItineraryElement(toLegsList, leg.copy(), ie, 
				       isDefinitelyNotDirect,
				       isDefinitelyOverlap);
	  }
	  return;
	}
	
       while (schedElems.hasMoreElements()) {
	   ItineraryElement ie = 
	       (ItineraryElement)schedElems.nextElement();
	   Verb v = ie.getRole();
	   if ((Constants.Verb.Load).equals(v)) {
	       loadIE = ie;
	   } else if ((Constants.Verb.Transport).equals(v)) {
	       if (myState.interpolate) {
		   transIE = ie;
	       }
	   } else if ((Constants.Verb.Unload).equals(v)) {
	       if (myState.includeUnload) {
		   unloadIE = ie;
	       }
	   }
       }
      }
    }

    boolean needsOverallLeg = true;
    if (unloadIE != null) {
	// add an unload leg
	System.out.println("Adding unload leg");
	if (addLegFromItineraryElement(toLegsList, leg.copy(), 
				       unloadIE, isDefinitelyNotDirect,
				       isDefinitelyOverlap)) {
	    // still want the overall leg
	    System.out.println("Added unload leg");
	} else {
	    // couldn't add the unload.  at least take the overall leg
	}
    }
    if (transIE != null) {
	if (loadIE != null) {
	    if (addLegsInterpolate(toLegsList, leg, task, transIE,
				   isDefinitelyNotDirect,
				   isDefinitelyOverlap)) {
		// added interpolated legs.  don't add the overall leg,
		// partially because the interpolate stole it.
		needsOverallLeg = false;
		System.out.println("added interpolated leg");
	    } else {
		// couldn't interpolate.  at least take the overall leg
	    }
	} else {
	    // lacks a load, so transit is ignored!
	    System.err.println("PSP_Itinerary: TRANSIT but no LOAD for task: "+
			       task.getUID().getUID());
	}
    }
    if (needsOverallLeg) {
	// add the overall leg
	if (setElementSchedule(leg, task, isDefinitelyNotDirect,
			       isDefinitelyOverlap)) {
	    //System.out.println(leg);
	    toLegsList.add(leg);
	    //System.out.println("Adding overall leg");
	} else {
	    // overall is bad?
	}
    }
      
    */

  }


  /**
   * Structure used for search.<br>
   * (HashMap)seenAssets maps Asset to AssetEntry
   */
  protected static class AssetEntry {

    /** points to task that first entered cluster.<br>
     * null if this asset didn't enter the cluster itself (e.g.
     * an AssetGroup composed of incoming assets).
     **/
    public Task rootTask;

    /** <code>Allocation<code>s where the asset equals the 
     * <code>Task.getDirectObject()</code>
     * @see #*Alloc*()
     **/
    private Set allocs;

    /** asset grouped with other assets.<p><pre>
     * example:
     * <i>TO Agg:</i><code>Asset "A" gets aggregated with "B" into "A+B"</code>
     * another example:
     * <i>FROM Agg:</i><code>"C+D" is an aggregation of "C" and "D"</code>
     * another example:
     * <i>TO Exp:</i><code>"E+F" is expanded to "E" and "F"</code>
     * another example:
     * <i>FROM Exp:</i><code>"G" is expanded from "G+H"</code>
     * </pre>
     * @see #*Group*()
     **/
    private Set groups;

    /**
     * for unit information
     */
    private String forUnit;


    /**
     * @see #getSelfLegs()
     **/
    protected Vector selfLegsCache;

    public AssetEntry() { }
    public AssetEntry(Task xrootTask) {
      rootTask = xrootTask;
    }

    public void addAlloc(Allocation alloc) {
      if (allocs == null)
        allocs = new HashSet();
      allocs.add(alloc); 
    }
    public Iterator getAllocs() {
      return ((allocs != null) ? allocs.iterator() : null);
    }
    public void emptyAllocs() {
      allocs = null;
    }

    public void addGroup(Asset asset) {
      if (groups == null)
        groups = new HashSet();
      groups.add(asset); 
    }
    public Iterator getGroups() {
      return ((groups != null) ? groups.iterator() : null); 
    }
    public void emptyGroups() {
      groups = null;
    }

    public void setForUnit (String forSt) {
      forUnit = forSt;

    }

    public String getForUnit () {
      return forUnit;
    }

  }

    /*
  private static void searchForLegs(
      MyPSPState myState,
      Task task) {
    // switch by planElement
    Object pe = task.getPlanElement();
    if (DEBUG) {
      System.out.println("Task: "+(task.getUID().toString())+
          " has PE "+((pe != null) ? pe.getClass().getName() : "null"));
    }
    // hand-ordered by usage frequency!
    if (pe instanceof Allocation) {
      // easy case
      Allocation alloc = (Allocation)pe;
      Asset asset = task.getDirectObject();
      Object o = myState.seenAssets.get(asset);
      if (o == null) {
        System.err.println("Task: "+(task.getUID().toString())+
          " has Allocation to unknown Asset: "+asset);
      } else {
        ((AssetEntry)o).addAlloc(alloc);
      }
    } else if (pe instanceof Expansion) {
      // tricky -- usually top-level tasks and aggregated tasks
      Expansion exp = (Expansion)pe;
      if (myState.seenExpansions.add(exp)) {
        // not seen before
        Asset parentAsset = task.getDirectObject();
        AssetEntry parentAE = null;
        Task childRootTask = null;
        Workflow wf = ((Expansion)pe).getWorkflow();
        Enumeration childTasksEn = wf.getTasks();
        while (childTasksEn.hasMoreElements()) {
          Task childTask = (Task)childTasksEn.nextElement();
          Asset childAsset = childTask.getDirectObject();
          if (!(childAsset.equals(parentAsset))) {
            // breaking up group
            if (parentAE == null) {
              // first need for parentAE
              if (!((parentAsset instanceof AssetGroup) ||
                    (parentAsset instanceof AggregateAsset))) {
                throw new RuntimeException(
                  "Task: "+(task.getUID().toString())+
                  " has Expansion of non-group/agg Asset: "+parentAsset);
              }
              Object parentO = myState.seenAssets.get(parentAsset);
              if (parentO == null) {
                throw new RuntimeException(
                  "Task: "+(task.getUID().toString())+
                  " has Expansion to unseen Asset: "+parentAsset);
              }
              parentAE = (AssetEntry)parentO;
              childRootTask = parentAE.rootTask;
              parentAE.rootTask = null;
            }
            // link (childAsset --> parentAsset)
            Object childO = myState.seenAssets.get(childAsset);
            AssetEntry childAE;
            if (childO == null) {
              // need to make sure child is listed
              childAE = new AssetEntry(childRootTask);
              myState.seenAssets.put(childAsset, childAE);
            } else {
              // set rootTask if missing
              childAE = (AssetEntry)childO;
              if (childAE.rootTask == null)
                childAE.rootTask = childRootTask;
            }
            childAE.addGroup(parentAsset);
          }
          try {
            // recurse! search for legs
            searchForLegs(myState, childTask);
          } catch (RuntimeException eBadLeg) {
            System.err.println("PSP_Itinerary: Ill-formed subTask: "+childTask+
                               " Exception: "+eBadLeg);
            eBadLeg.printStackTrace();
            System.err.println("will continue");
          }
        }
      } else {
        if (DEBUG) {
          System.out.println("Task: "+(task.getUID().toString())+
              " has previously seen Expansion");
        }
      }
    } else if (pe instanceof Aggregation) {
      // skip to the combined task
      try {
        Composition comp = ((Aggregation)pe).getComposition();
        MPTask mptask = comp.getCombinedTask();
        Asset parentAsset = task.getDirectObject();
        Asset childAsset = mptask.getDirectObject();
        if (!(parentAsset.equals(childAsset))) {
          // creating a group
          Object parentO = myState.seenAssets.get(parentAsset);
          if (parentO == null) {
            throw new RuntimeException(
              "Task: "+(task.getUID().toString())+
              " has Aggregation to unseen Asset: "+parentAsset);
          }
          AssetEntry parentAE = (AssetEntry)parentO;
          // link (parentAsset --> childAsset)
          parentAE.addGroup(childAsset);
          Object childO = myState.seenAssets.get(childAsset);
          if (childO == null) {
            // need to make sure child is listed
            AssetEntry childAE = new AssetEntry(null);
            myState.seenAssets.put(childAsset, childAE);
          } 
        }
        // recurse!  (note: mptask pe always Expansion in TOPS)
        searchForLegs(myState, mptask);
      } catch (Exception mpE) {
        System.err.println("PSP_Itinerary: Ill-formed Aggregation Task: "+
            task.getUID().getUID()+" Exception: "+mpE);
        mpE.printStackTrace();
        System.err.println("will continue");
      }
    } else if (pe == null) {
      // usually caused by unfinished task allocation.
      System.err.println("Task with UID: "+(task.getUID().toString())+
          " has null PE!");
    } else {
      // shouldn't happen!
      System.err.println("PSP_Itinerary: Task with UID: "+
          (task.getUID().toString())+
          " has PE of unexpected type: "+pe.getClass().getName());
    }
  }
    */

  /**
   * Take <code>seenAssets</code> and generate UITaskItineraries
   */
  private static void addItineraries(
      List toItinList,
      MyPSPState myState) {
    Iterator iter = myState.seenAssets.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry elem = (Map.Entry)iter.next();
      AssetEntry currentAE = (AssetEntry)elem.getValue();
      if (currentAE.rootTask != null) {
        // this asset hasn't been "handed-off" by any expansions
        // to other assets!
        Asset currentAsset = (Asset)elem.getKey();
        UITaskItinerary itin = new UITaskItinerary();
        // create the basic itinerary w/o leg information.
        try {
          setDefaultInformation(itin, myState.clusterID);
          if (!setTaskInformation(itin, currentAE.rootTask))
            continue;
          itin.setUITAssetInfoVector(
            getUITAssetInfoVector(
              currentAsset));
        } catch (Exception e) {
          System.err.println("PSP_Itinerary: Ill-formed task: "+
            currentAE.rootTask.getUID().getUID()+
            " Basic Exception: "+e);
          e.printStackTrace();
          continue;
        }
        // generate the itinerary legs.
        try {
          itin.setScheduleElements(
            getAllLegs(myState, currentAE));
        } catch (Exception e) {
          System.err.println("PSP_Itinerary: Ill-formed task: "+
              currentAE.rootTask.getUID().getUID()+
              " Legs Exception: "+e);
          e.printStackTrace();
          continue;
        }
        toItinList.add(itin);
      }
    }
  }

  /**
   * For use by getAllLegs!
   * <p>
   * Get "self" itinerary legs from an asset entry.  If not cached in the
   * entry, compute the legs from the allocations.
   */
  private static Vector getSelfLegs(
      MyPSPState myState, AssetEntry fromAE) {
    // check cache
    Vector l = fromAE.selfLegsCache;
    if (l == null) {
      // turn allocs into legs
      Iterator allocIter = fromAE.getAllocs();
      if (allocIter == null) {
        // empty list
        l = new Vector(0);
      } else {
        // create from Allocations
        l = new Vector();
        while (allocIter.hasNext()) {
          Allocation alloc = (Allocation)allocIter.next();
          addLegsAlloc(myState, l, alloc);
        }
        // release allocs for GC
        fromAE.emptyAllocs();
        if (myState.sortItins) {
          // sort
          Collections.sort(l, UI_TASK_ITINERARY_ELEMENT_DATE_ORDER);
        }
      }
      // set cache
      fromAE.selfLegsCache = l;
    }
    // return legs
    return l;
  }

  /**
   * Get all itinerary legs from an asset entry.
   */
  private static Vector getAllLegs(
      MyPSPState myState,
      AssetEntry fromAE) {
    Vector l;
    Iterator groupIter = fromAE.getGroups();
    if (groupIter == null) {
      // self legs only
      l = getSelfLegs(myState, fromAE);
    } else {
      l = new Vector();
      // self legs
      l.addAll(getSelfLegs(myState, fromAE));
      // shared legs via groups
      while (groupIter.hasNext()) {
        Asset as = (Asset)groupIter.next();
        Object o = myState.seenAssets.get(as);
        if (o == null) {
          throw new RuntimeException(
            "Unseen Asset: "+as.getUID().getUID()+
            " from rootTask: "+fromAE.rootTask.getUID().getUID());
        }
        l.addAll(getSelfLegs(myState, (AssetEntry)o));
      }
      // release groups for GC
      fromAE.emptyGroups();
      if (myState.sortItins) {
        // sort
        Collections.sort(l, UI_TASK_ITINERARY_ELEMENT_DATE_ORDER);
      }
    }
    // return legs
    return l;
  }

  private static final Comparator UI_TASK_ITINERARY_ELEMENT_DATE_ORDER = 
    new Comparator() {
      public final int compare(Object o1, Object o2) {
        Date d1 = ((UITaskItineraryElement)o1).getStartDate();
        Date d2 = ((UITaskItineraryElement)o2).getStartDate();
        if ((d1 != null) && (d2 != null))
          return d1.compareTo(d2);
        return 0; // error?
      }
    };




  /**
   * Simple GSS ItineraryElements.
   * @see #generateLiveData
   */
  private static UITaskItineraryElement createUITaskItineraryElement(Task task) 
  {
    UITaskItineraryElementCarrier leg = new UITaskItineraryElementCarrier();
 
    /* CARRIER */
    Asset carrierAsset;
    PlanElement pe = task.getPlanElement();
    if (pe instanceof Allocation) {
      carrierAsset = ((Allocation)pe).getAsset();
    } else if (pe == null) {
      carrierAsset = null;
    } else {
      System.err.println(
          "PSP_Itinerary: Unable to handle complex tasks in GSS mode.  "+
          pe.getClass().getName()+" plan element not handled!");
      carrierAsset = null;
    }
    if (carrierAsset != null) {
      leg.setCarrierUID(carrierAsset.getUID().toString());
      leg.setCarrierTypeNomenclature(
        carrierAsset.getTypeIdentificationPG().getTypeIdentification());
      leg.setCarrierItemNomenclature(
        carrierAsset.getItemIdentificationPG().getItemIdentification());
    }
    leg.setTransportationMode(getFakedMode(task));
    leg.setVerbRole(Constants.Verb.Transport);
    setElementSchedule(leg, task,false,false);
    return leg;
  }

  protected static Collection searchUsingPredicate(
      MyPSPState myState, UnaryPredicate pred) 
  {
    return myState.sps.queryForSubscriber(pred);
  }

  private static UITAssetInfo makeUITAssetInfo(
      String transported_uid,
      String transported_name,
      String transported_asset_type_id,
      String transported_asset_item_id,
      int transported_asset_quantity,
      double transported_tons,
      int[] asset_classes) {
    UITAssetInfo tai = new UITAssetInfo();
    tai.setUID(transported_uid);
    tai.setTypeNomenclature(transported_name);
    tai.setTypeID(transported_asset_type_id);
    tai.setUID(transported_asset_item_id);
    tai.setQuantity(transported_asset_quantity);
    tai.setTons(transported_tons);
    tai.setAssetClasses(asset_classes);
    return tai;
  }

  private static UITAssetInfo makeUITAssetInfo(
      Asset transAsset) {
    UITAssetInfo tai = new UITAssetInfo();
    int quantity = 1;
    double tons = 0.0;
    UID myUID = transAsset.getUID();
    String myItemID = transAsset.getItemIdentificationPG().getItemIdentification();
    // Let's be paranoid
    if (transAsset instanceof GLMAsset && 
	((GLMAsset)transAsset).hasPhysicalPG() &&
	((GLMAsset)transAsset).getPhysicalPG() != null &&
	((GLMAsset)transAsset).getPhysicalPG().getMass() != null) {
	tons += ((GLMAsset)transAsset).getPhysicalPG().getMass().getTons();
    }
    while (transAsset instanceof AggregateAsset) {
      AggregateAsset agg = (AggregateAsset)transAsset;
      quantity *= (int)agg.getQuantity();
      transAsset = agg.getAsset();
      if (transAsset instanceof GLMAsset && 
	  ((GLMAsset)transAsset).hasPhysicalPG() &&
	  ((GLMAsset)transAsset).getPhysicalPG() != null &&
	  ((GLMAsset)transAsset).getPhysicalPG().getMass() != null) {
	tons += quantity * 
	  ((GLMAsset)transAsset).getPhysicalPG().getMass().getTons();
      }
    } 
    tai.setQuantity(quantity);
    tai.setTons(tons);
    TypeIdentificationPG transTypePG = transAsset.getTypeIdentificationPG();
    tai.setTypeID(transTypePG.getTypeIdentification());
    tai.setTypeNomenclature(transTypePG.getNomenclature());
    UID uid = myUID; //transAsset.getUID();
    tai.setUID((uid != null) ? uid.toString() : "");
    tai.setItemID(myItemID);
    //      transAsset.getItemIdentificationPG().getItemIdentification());
    tai.setAssetClasses(getAssetClasses(transAsset));
    return tai;
  }

  /**
   * recursive!
   * @param toTAIV Vector of UITAssetInfo instances
   */
  private static void addUITAssetInfoVector(
     Vector toTAIV, Object dirObj) {
    if (dirObj instanceof AssetGroup) {
      Enumeration assetsEn = ((AssetGroup)dirObj).getAssets().elements();
      while (assetsEn.hasMoreElements()) {
        addUITAssetInfoVector(toTAIV, assetsEn.nextElement());
      }
    } else if (dirObj instanceof Asset) {
      toTAIV.addElement(makeUITAssetInfo((Asset)dirObj));
    } else {
      throw new RuntimeException("Unexpected task directObject: "+dirObj);
    }
  }

  private static Vector getUITAssetInfoVector(
      String transported_uid,
      String transported_name,
      String transported_asset_type_id,
      String transported_asset_item_id,
      int transported_asset_quantity,
      double transported_tons,
      int[] asset_classes) {
    // can't we just make this field an Object????
    Vector v = new Vector();
    v.addElement(
      makeUITAssetInfo(
        transported_uid,
        transported_name,
        transported_asset_type_id,
        transported_asset_item_id,
        transported_asset_quantity,
        transported_tons,
	asset_classes));
    return v;
  }

  private static Vector getUITAssetInfoVector(Object dirObj) {
    if ((!(dirObj instanceof AssetGroup)) && (dirObj instanceof Asset)) {
      // can't we just make this field an Object????
      Vector vOneElem = new Vector(1);
      vOneElem.addElement(makeUITAssetInfo((Asset)dirObj));
      return vOneElem;
    }

    Vector v = new Vector();
    addUITAssetInfoVector(v, dirObj);
    return v;
  }

  /** END UTILITIES **/

  /** BEGIN LIVE MODE **/

  /** 
   * Simple task filter.
   * <p>
   * Note: changed filter from "startsWith" to "equals"
   * <p>
   * @param task the Task we're testing
   * @param inputTaskFilter if non-null: task UID (equals String)/(in Set)
   * @param forUnitFilter if non-null: FOR equals this unit name 
   * @return true if match
   */
  protected static final boolean taskFitsFilters(
     Task task, 
     final Object inputTaskFilter,
     final String forUnitFilter) {
    if (inputTaskFilter != null) {
      String taskId = task.getUID().toString();
      if (inputTaskFilter instanceof String) {
        if (!(((String)inputTaskFilter).equals(taskId)))
          return false;
      } else  {
        if (!(((Set)inputTaskFilter).contains(taskId)))
          return false;
      }
    }
    if (forUnitFilter != null) {
      String forUnit = getForUnit(task);
      if ((forUnit == null) ||
          (!(forUnit.equals(forUnitFilter))))
        return false;
    }
    return true;
  }

  /**
   * GSS root tasks are the same as live root tasks
   */
  protected static Collection searchForGSSRootTasks(
      MyPSPState myState) {
    return searchForLiveRootTasks(myState);
  }

  /** 
   * <i>LIVE mode</i> Root tasks have:<br>
   * <ol>
   *   <li>source != destination</li>
   *   <li>verb == Transport</li>
   * </ol>
   */
  protected static UnaryPredicate getLiveRootTasksPred() {
    return new UnaryPredicate() 
      {
        public boolean execute(Object o) {
          if (o instanceof Task) {
            Task task = (Task)o;
            if (!(task.getSource().equals(task.getDestination()))) {
              Verb v = task.getVerb();
              if ((Constants.Verb.Transport).equals(v) ||
                  (Constants.Verb.TransportationMission).equals(v) ||
                  (Constants.Verb.Supply).equals(v)) {
                // BEGIN "IMPLIED" FIX
                if (task.getPrepositionalPhrase("IMPLIED") == null) {
                  return true;
                }
              }
            }
          }
          return false;
        }
      };
  }

  protected static UnaryPredicate getLiveRootTasksPred(
      final Object inputTaskFilter,
      final String forUnitFilter) {
    return new UnaryPredicate() 
      {
        protected UnaryPredicate liveRootsPred = getLiveRootTasksPred();
        public boolean execute(Object o) {
          return (liveRootsPred.execute(o) &&
                  taskFitsFilters((Task)o, inputTaskFilter, forUnitFilter));
        }
      };
  }

  /** 
   * Interesting "Itinerary" Tasks.
   * <ol>
   *   <li>is a Task</li>
   *   <li>the plan element is an allocation</li>
   *   <li>the allocation is to either a PhysicalAsset or Person</li>
   *   <li>the task verb is Transport, TransportationMission, or Supply</li>
   *   <li>the task doesn't have an "IMPLIED" preposition</li>
   * </ol>
   */
  protected static UnaryPredicate getInterestingTasksPred() {
    return new UnaryPredicate() 
      {
        public boolean execute(Object o) {
          if (o instanceof Task) {
            Task task = (Task)o;
            Object pe = task.getPlanElement();
            // BEGIN "IMPLIED" FIX
            if (pe instanceof Allocation) {
              Asset a = ((Allocation)pe).getAsset();
              if ((a instanceof PhysicalAsset) || (a instanceof Person)) {
//                   ((a instanceof ALPAss) &&
//                    (((GLMAsset)a).hasPersonPG()))) {
                Verb v = task.getVerb();
                if ((Constants.Verb.Transport).equals(v) ||
                    (Constants.Verb.TransportationMission).equals(v) ||
                    (Constants.Verb.Supply).equals(v)) {
                  if (task.getPrepositionalPhrase("IMPLIED") == null) {
                    // an interesting task!
                    return true;
                  }
                }
              }
            }
            /*
             * DISABLE FOR "IMPLIED" FIX
            if (pe instanceof Allocation) {
              Allocation alloc = (Allocation)pe;
              if (alloc.getAsset()  instanceof PhysicalAsset) 
                return true;
	      else if (alloc.getAsset() instanceof GLMAsset &&
		       ((GLMAsset)alloc.getAsset()).hasPersonPG())
		return true;
            }
            */
          }
          return false;
        }
      };
  }

  protected static UnaryPredicate getInterestingTasksPred(
      final Object inputTaskFilter,
      final String forUnitFilter) {
    return new UnaryPredicate() 
      {
        protected UnaryPredicate liveRootsPred = getInterestingTasksPred();
        public boolean execute(Object o) {
          return (liveRootsPred.execute(o) &&
                  taskFitsFilters((Task)o, inputTaskFilter, forUnitFilter));
        }
      };
  }

  /**
   * filter the root tasks
   */
  protected static Collection searchForLiveRootTasks(
      MyPSPState myState) {
    UnaryPredicate pred;
    if ((myState.inputTaskFilter == null) &&
        (myState.forUnitFilter == null)) {
      pred = getLiveRootTasksPred();
    } else {
      pred = getLiveRootTasksPred(
               myState.inputTaskFilter, myState.forUnitFilter);
    }
    return searchUsingPredicate(myState, pred);
  }

  /**
   * filter the  tasks
   */
  protected static Collection searchForInterestingTasks(
      MyPSPState myState) {
    UnaryPredicate pred;
    if ((myState.inputTaskFilter == null) &&
        (myState.forUnitFilter == null)) {
      pred = getInterestingTasksPred();
    } else {
      pred = getInterestingTasksPred(
               myState.inputTaskFilter, myState.forUnitFilter);
    }
    return searchUsingPredicate(myState, pred);
  }


  private static Vector easyGenerateLiveData(
       MyPSPState myState)
    throws IOException {
    AssetEntry ae;

    if (DEBUG) {
      System.out.println("PSP_Itinerary.easyGenerateLiveData - Find Easy Live Itineraries");
    }
    // Set up the hash map
    myState.seenAssets = new HashMap();


    // Get all the interesting tasks -- i.e. tasks in this cluster
    // that have a plan element that is an allocation AND
    // that the asset allocated to is a physical asset
    Iterator interIter = searchForInterestingTasks(myState).iterator();
    while (interIter.hasNext()) {
      Allocation alloc = null;
      Task currTask = (Task)interIter.next();
      if (DEBUG) {
        System.out.println("PSP_Itinerary.easyGenerateLiveData - " + 
			   "Task w/ physical asset: "+ 
			   currTask.getUID().toString() );
      }
      Object pe = currTask.getPlanElement();
      // Since we are filtering the tasks before we get here, the pe's
      // will presumably always be allocations -- but we'll check
      // anyway to be sure
      if (pe instanceof Allocation) {
        alloc = (Allocation)pe;
      }

      // Record the assets and their allocations
      Asset dirObj = currTask.getDirectObject();
      // If it's a group, record each individual asset,
      // because it will eventually have to be broken up
      // to send out to the aggregation server, and  the
      // group may be broken up by an expansion

      // Find the FOR information
      String forString = getForUnit(currTask);

      if (dirObj instanceof AssetGroup ) {
        Vector groupVec = UTILAsset.expandAssetGroup((AssetGroup) dirObj);
        Enumeration gEnum = groupVec.elements();
        while (gEnum.hasMoreElements()) {
          Asset gAsset = (Asset) gEnum.nextElement();
          ae = getAssetEntry(myState,gAsset);
          ae.rootTask = currTask;
          ae.addAlloc(alloc);
	  ae.setForUnit(forString);
	  if (DEBUG)
	    System.out.println("PSP_Itinerary.easyGenerateLiveData" +
			       " - adding physical asset: "+ 
			       gAsset);
	}
      }
      else {
        ae = getAssetEntry(myState, dirObj);
        ae.rootTask = currTask;
        ae.addAlloc(alloc);
	ae.setForUnit(forString);
	if (DEBUG)
	  System.out.println("PSP_Itinerary.easyGenerateLiveData -"+
			     "adding phys asset from d.o.: "+ 
			     dirObj);
      }
    }

    // Get all the root tasks -- these will have the FOR information,
    // which might get lost along the way in all the expansions, etc.
    // Match up the FOR information with the assets that we already
    // decided were interesting
    /*
    Iterator rtIter = searchForLiveRootTasks(myState).iterator();

    while (rtIter.hasNext()) {
      Task rootTask = (Task)rtIter.next();
      if (DEBUG) {
        System.out.println("PSP_Itinerary.easyGenerateLiveData - ROOT Task: "
			   + rootTask.getUID().toString());
      }
      // Find the FOR information
      String forString = getForUnit(rootTask);
      if (DEBUG) {
        if (forString == null) {
          System.out.println("PSP_Itinerary.easyGenerateLiveData : " +
			     "Root Task does not have " +
                             " FOR preposition: " + 
                             rootTask.getUID().toString());
        }
      }

      // Record the assets, and put the FOR string in there
      Asset dirObj = rootTask.getDirectObject();
      // If it's a group, record each individual asset,
      // because it will eventually have to be broken up
      // to send out to the aggregation server, and  the
      // group may be broken up by an expansion
      if (dirObj instanceof AssetGroup ) {
        Vector groupVec = UTILAsset.expandAssetGroup((AssetGroup)dirObj);
        Enumeration gEnum = groupVec.elements();
        while (gEnum.hasMoreElements()) {
          Asset gAsset = (Asset) gEnum.nextElement();
          ae = findAssetEntry(myState,gAsset);
          if (ae != null)
            ae.setForUnit(forString);
	  if (DEBUG)
	    System.out.println("PSP_Itinerary.easyGenerateLiveData"+
			       "- adding physical asset: "+ 
			       gAsset);
        }
      }
      else {
        ae = getAssetEntry(myState, dirObj);
        if (ae != null) 
          ae.setForUnit(forString);
	if (DEBUG)
	  System.out.println("PSP_Itinerary.easyGenerateLiveData - " +
			     "adding phys. asset from d.o.: "+ 
			     dirObj);
      }

    }
    */

    // create the itineraries from the seenAssets data.
    Vector itins = new Vector();
    findItineraries(itins, myState);
    if (DEBUG) {
      System.out.println("done.");
    }
    return itins;

  }


  /**
   * Take <code>seenAssets</code> and generate UITaskItineraries
   */
  private static void findItineraries(
                                      List toItinList,
                                      MyPSPState myState) {

    Iterator iter = myState.seenAssets.entrySet().iterator();
    // Just loop through all our tasks, and get their
    // itineraries
    while (iter.hasNext()) {
      Map.Entry elem = (Map.Entry)iter.next();
      AssetEntry currentAE = (AssetEntry)elem.getValue();
        Asset currentAsset = (Asset)elem.getKey();
	if (currentAE == null || currentAsset == null)
	  continue;
        UITaskItinerary itin = new UITaskItinerary();
        // create the basic itinerary w/o leg information.
        try {
          setDefaultInformation(itin, myState.clusterID);

          if (!setTaskInformation(itin, currentAE.rootTask,
                                  currentAE.forUnit))
            continue;
	  
	  if (currentAsset.getUID() == null)
	    System.err.println("PSP_Itinerary.findItinerary - " + currentAsset + " has no uid?");
	      
	  itin.setAssetUID(currentAsset.getUID().getUID());
          itin.setUITAssetInfoVector(
            getUITAssetInfoVector(
              currentAsset));
        } catch (Exception e) {
	  if (currentAE == null) {
	    System.err.println("PSP_Itinerary.findItinerary - " +
			       "Ill-formed task, but no current "+
			       "Asset Entry.\n"+
			       " Basic Exception: "+e);
	  }
	  else if (currentAE.rootTask == null) {
	    System.err.println("PSP_Itinerary.findItinerary " +
			       "- Ill-formed task,"+
			       "but no root task for asset entry : " +
			       currentAE + "\nAsset was " + currentAsset +
			       " Basic Exception: "+e);
	  }
	  else {
	    System.err.println("PSP_Itinerary.findItinerary - " +
			       "Ill-formed root task: " +
			       currentAE.rootTask.getUID().getUID() +
			       " Basic Exception: " +e);
		  }
          e.printStackTrace();
          continue;
        }
        // generate the itinerary legs.
        try {
          itin.setScheduleElements(
            getLegs(myState, currentAE));
        } catch (Exception e) {
          System.err.println("PSP_Itinerary: Ill-formed task: "+
              currentAE.rootTask.getUID().getUID()+
              " Legs Exception: "+e);
          e.printStackTrace();
          continue;
        }
        toItinList.add(itin);
    }
  }




  /**
   * For use by getAllLegs!
   * <p>
   * Get "self" itinerary legs from an asset entry.  If not cached in the
   * entry, compute the legs from the allocations.
   */
  private static Vector getLegs(
      MyPSPState myState, AssetEntry fromAE) {
  
    Vector legs = new Vector();
    // Get all the allocations associted with the direct object
    Iterator allocIter = fromAE.getAllocs();
    while (allocIter.hasNext()) {
      Allocation alloc = (Allocation)allocIter.next();
      if (DEBUG) {
        System.out.println("Generate leg for alloc: "+
                           alloc.getUID().getUID());
      }
      addLegsAlloc(myState, legs, alloc);
    }
    if (myState.sortItins) {
      // sort
      Collections.sort(legs, UI_TASK_ITINERARY_ELEMENT_DATE_ORDER);
    }

    return legs;
  }

  // Get makes an AssetEntry if one can't be found
  //
  private static AssetEntry getAssetEntry(MyPSPState myState, Asset asset) {
    if (myState.seenAssets != null) {
      AssetEntry ae = findAssetEntry(myState, asset);        
      if (ae == null) {
        ae = new AssetEntry();
        myState.seenAssets.put(asset, ae);
      } 
      return ae;
    }
    else
      return null;

  }

  // Just looks for an AssetEntry -- returns null if there is not
  // one
  private static AssetEntry findAssetEntry(MyPSPState myState, Asset asset) {
    if (myState.seenAssets != null) {
      AssetEntry ae = (AssetEntry) myState.seenAssets.get(asset);
      return ae;
    }
    else 
      return null;
  }


    /* BOZO - defunct! */
    /*

  private static Vector generateLiveData(
      MyPSPState myState)
    throws  IOException
  {
    if (DEBUG) {
      System.out.println("Find Live Itineraries");
    }
    // find root tasks
    Iterator rtIter = searchForLiveRootTasks(myState).iterator();

    // make map of (asset --> legs) data
    myState.seenAssets = new HashMap();
    myState.seenExpansions = new HashSet();
    while (rtIter.hasNext()) {
      Task rootTask = (Task)rtIter.next();
      if (DEBUG) {
        System.out.println("ROOT Task: "+(rootTask.getUID().toString()));
      }
      // associate the direct object with the root task
      Asset dirObj = rootTask.getDirectObject();
      Object o = myState.seenAssets.get(dirObj);
      if (o == null) {
        myState.seenAssets.put(
          dirObj,
          new AssetEntry(rootTask));
      } else {
        AssetEntry ae = (AssetEntry)o;
        if (ae.rootTask == null) {
          // odd ... likely not possible in TOPS, since groups of assets
          // within TOPS are always broken apart.
          ae.rootTask = rootTask;
        }
      }
      // search for (asset, legs) data
      searchForLegs(myState, rootTask);
    }
    // release for GC
    myState.seenExpansions = null;

    // create the itineraries from the seenAssets data.
    Vector itins = new Vector();
    addItineraries(
      itins,
      myState);
    if (DEBUG) {
      System.out.println("done.");
    }
    return itins;
  }
    */

  /** END LIVE MODE **/

  /** BEGIN GSS MODE **/

  /** 
   * Create data based on contents of log plan based on 
   * GSS/SimpleMultilegSchedulerPlugIn behavior.  <br>
   * Assumes that there are root TRANSPORT tasks, which have one of two 
   * configurations:<br>
   * GROUND-SEA-GROUND and GROUND-AIR-GROUND
   */
  private static Vector generateGSSData(
      MyPSPState myState)
    throws  IOException
  {
    Vector v = new Vector();
    Iterator iter = searchForGSSRootTasks(myState).iterator();
    while (iter.hasNext()) {
      Task task = (Task)iter.next();
      UITaskItinerary itin = null;
      try {
        itin = generateGSSTaskItinerary(task, myState.psc);
      } catch (RuntimeException any_exception) {
        // Any bad tasks, we ignore them
      }
      v.addElement(itin);
    }
    return v;
  }

  private static void setDefaultInformation(
      UITaskItinerary itin, String clusterID) 
  {
    itin.setClusterID(clusterID);
    itin.setScheduleElementType(ScheduleElementType.LOCATIONRANGE);
    itin.setScheduleType(ScheduleType.OTHER);
    itin.setAnnotation("");
  }


  private static boolean setTaskInformation(UITaskItinerary itin, Task task)
  { 
    String forSt = getForUnit(task);
    if (forSt == null)
      return false;
    else
      return setTaskInformation(itin,task,forSt);
  }

  /**
   * Fills in the TaskItinerary with info from the task : task UID, FROM, TO
   * preps, start and end time aspects, and the unit the task is from.     <p>
   *
   * We expect the plan element of the task to have a start and end time
   * aspect on its allocation result.  If either is missing, return false. <p>
   *
   * If there is no task whatsoever, we just skip setting the itinerary... <p>
   *
   * @param itin - the itinerary to populate with info from the task
   * @param task - the task to get the info from
   * @param forSt - the unit the task is for
   * @return true if task is well formed
   */
  private static boolean setTaskInformation(UITaskItinerary itin,
                                            Task task,
                                            String forSt) {

    // uid
    if (task == null) {
	  //      System.out.println("set task information -- task is null!");
	  return false;
	}
    String taskUIDString = task.getUID().toString(); 
    itin.setAllocTaskUID(taskUIDString); 
    itin.setInputTaskUID(taskUIDString); 
   
    // to and from
    PrepositionalPhrase prepTo = 
      task.getPrepositionalPhrase(Constants.Preposition.TO);
    PrepositionalPhrase prepFrom = 
      task.getPrepositionalPhrase(Constants.Preposition.FROM);
    itin.toRequiredLocation = (GeolocLocation)prepTo.getIndirectObject();
    itin.fromRequiredLocation = (GeolocLocation)prepFrom.getIndirectObject();
    
    // dates
    PlanElement pe = task.getPlanElement();
    if (pe != null) {
      AllocationResult est = pe.getEstimatedResult();
      if (est != null) {
        if (!est.isDefined(AspectType.START_TIME)) {
          System.err.println("PSP_Itinerary: Itinerary for Task UID: "+
              taskUIDString+" missing START_TIME; ignored!");
          return false;
        }
        itin.earliestPickupDate = 
          new Date((long)est.getValue(AspectType.START_TIME));
        if (!est.isDefined(AspectType.END_TIME)) {
          System.err.println("PSP_Itinerary: Itinerary for Task UID: "+
              taskUIDString+" missing END_TIME; ignored!");
          return false;
        }

        itin.latestDropoffDate = 
          new Date((long)est.getValue(AspectType.END_TIME));
		if (DEBUG)
		  System.out.println("setTaskInformation -- alloc result : start " + itin.earliestPickupDate +
							 " end " + itin.latestDropoffDate);
		  
      }
    }

    // for
    itin.TransportedUnitName = forSt;

    return true;
  }

    private static String getForUnit(Task task) {
	String unit = grabUnit((Asset)task.getDirectObject());
	if (unit == null) System.err.println("WARNING: Bad For Unit on asset: "+task.getDirectObject());
	if (unit != null) unit = unit.trim();
	return unit;
    }
    // returns null if two units are included in task
    private static String grabUnit(Asset asset) {
	String retval = null;
	String tempval = null;
	if (asset instanceof AssetGroup) {
	    Vector assetList = ((AssetGroup)asset).getAssets();
	    for (int i = 0; i < assetList.size(); i++) {
		tempval = grabUnit((Asset)assetList.elementAt(i));
		if (tempval == null || (retval != null &&
					!(tempval.equals(retval)))) {
		    System.out.println("WARNING: Mismatched for information in Asset Group: "+tempval+" / "+retval);
		    return null;
		}
		retval = tempval;
	    }
	} else {
	    PropertyGroup forunit = asset.searchForPropertyGroup(ForUnitPG.class);
	    if (forunit == null) retval = null;
	    else retval = ((ForUnitPG)forunit).getUnit();
	}
// 	} else if (asset instanceof AggregateAsset) {
// 	    PropertyGroup 
// 	    retval =
// 	    retval = grabUnit(((AggregateAsset)asset).getAsset());
// 	} else { 
// 	    if (!((GLMAsset)asset).hasForUnitPG()) retval = null;
// 	    else retval = ((GLMAsset)asset).getForUnitPG().getUnit();
// 	}
	return retval;
    }


//       PrepositionalPhrase prepFor = 
// 	  task.getPrepositionalPhrase(Constants.Preposition.FOR);
//       if (prepFor != null) {
// 	  Object indObj = prepFor.getIndirectObject();
// 	  if (indObj instanceof String) {
// 	      System.out.println("returning: "+((String)indObj).trim());
// 	      return ((String)indObj).trim();
// 	  } 
// 	  else if (indObj instanceof Asset) {
// 	      String s =
// 		  ((Asset)indObj).getTypeIdentificationPG().getTypeIdentification();
// 	      if (s != null)
// 		  System.out.println("returning: "+s.trim());
// 	      return s.trim();
// 	  }
//       }
//       System.out.println("returning: null");
//       return null;
//   }

  /** Return a UITaskItinerary for a task generated in 'GSS' mode **/
  private static UITaskItinerary generateGSSTaskItinerary(Task task, 
                                                   PlanServiceContext psc) 
  { 
    Expansion expansion = (Expansion) task.getPlanElement();
    if (expansion == null) 
      return null;

    Workflow wf = expansion.getWorkflow();
    if (wf == null)
      return null;

    UITaskItinerary itin = new UITaskItinerary();

    setDefaultInformation(
      itin, 
      psc.getServerPlugInSupport().getClusterIDAsString()); 
    setTaskInformation(itin, task);

    // Grab the three legs of the mission, CONUS_GROUND, SEA/AIR, THEATER_GROUND
    Task conus_ground_leg = null;
    Task sea_air_leg = null;
    Task theater_ground_leg = null;

    for (Enumeration subtasks = wf.getTasks(); subtasks.hasMoreElements();) {
      Task subtask = (Task)subtasks.nextElement();
      if (conus_ground_leg == null) 
        conus_ground_leg = subtask;
      else if (sea_air_leg == null)
        sea_air_leg = subtask;
      else if (theater_ground_leg == null)
        theater_ground_leg = subtask;
    }

    if ((conus_ground_leg == null) || 
        (sea_air_leg == null) || 
        (theater_ground_leg == null)) 
      return null;

    // And fill them in as schedule elements for the itinerary object
    Vector schedElems = itin.getScheduleElements();
    schedElems.addElement(createUITaskItineraryElement(conus_ground_leg));
    schedElems.addElement(createUITaskItineraryElement(sea_air_leg));
    schedElems.addElement(createUITaskItineraryElement(theater_ground_leg));
    itin.setScheduleElements(schedElems);

    return itin;
  }

  /** END GSS MODE **/

  /** BEGIN CANNED MODE **/

  // Create a set of canned UITaskItinerary objects
  private static Vector generateCannedData(
     MyPSPState myState) throws IOException
  {
    Date C0 = new Date();

    try {
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
      C0 = formatter.parse("07/04/2000");
    } catch (ParseException pe) {
      System.out.println("Exception parsing C0");
      pe.printStackTrace();
    }

    Vector v = new Vector();

    int[] ac = new int[1];
    ac[0]=0;

    UITaskItinerary itin = 
        createUITaskItinerary(myState.cof, "HOME", "3-69-ARBN",
                              "SHIP", "BELATRIX", 
                              "3-69-ARBN/121", "M998 (HMMWV) Truck",
                              "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 10.0, ac, 
                              new Date(C0.getTime() + 10l*ONE_DAY),
                                  UITaskItineraryElement.SEA_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin = createUITaskItinerary(myState.cof, "HOME", "3-69-ARBN",
                                 "C-17", "TN/001",
                                 "3-69-ARBN/122", "Filter",
                                 "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 0.5, ac,
                                 new Date(C0.getTime() + 12l*ONE_DAY),
                                 UITaskItineraryElement.AIR_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin = createUITaskItinerary(myState.cof, "HOME", "3-69-ARBN",
                                 "SHIP", "CORONADO",
                                 "3-69-ARBN/123", "C380: 120 MM APFDS",
                                 "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 1.0, ac,
                                 new Date(C0.getTime() + 14l*ONE_DAY),
                                 UITaskItineraryElement.SEA_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin =
        createUITaskItinerary(myState.cof, "HOME", "2-7-INFBN",
                              "SHIP", "ALPHA CENTAURI",
                              "2-7-INFBN/121", "M1038 (HMMWV) Troop Carrier",
                              "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 8.0, ac,
                              new Date(C0.getTime() + 20l*ONE_DAY),
                              UITaskItineraryElement.SEA_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin = createUITaskItinerary(myState.cof, "HOME", "2-7-INFBN",
                                 "C-141", "TN/002",
                                 "2-7-INFBN/122", "Bearing",
                                 "NSN/2320011077155", "203-FSB-2320011077155-4", 1,0.0000007,ac,
                                 new Date(C0.getTime() + 22l*ONE_DAY),
                                 UITaskItineraryElement.AIR_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin = createUITaskItinerary(myState.cof, "HOME", "2-7-INFBN",
                                 "SHIP", "SS SGT SMITH",
                                 "2-7-INFBN/123", "A975: 50 MM",
                                 "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 0.001, ac,
                                 new Date(C0.getTime() + 24l*ONE_DAY),
                                 UITaskItineraryElement.SEA_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin =
        createUITaskItinerary(myState.cof, "HOME", "3-7-INFBN",
                              "SHIP", "SS DUBHE",
                              "2-7-INFBN/121", "M1038 (HMMWV) Troop Carrier",
                              "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 8.2, ac,
                              new Date(C0.getTime() + 23l*ONE_DAY),
                              UITaskItineraryElement.SEA_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin = createUITaskItinerary(myState.cof, "HOME", "3-7-INFBN",
                                 "C-5B", "TN/003",
                                 "2-7-INFBN/122", "Hose",
                                 "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 0.00002, ac,
                                 new Date(C0.getTime() + 25l*ONE_DAY),
                                 UITaskItineraryElement.AIR_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    itin = createUITaskItinerary(myState.cof, "HOME", "3-7-INFBN",
                                 "SHIP", "SS CPL RUSTY",
                                 "2-7-INFBN/123", "A975: 50 MM",
                                 "NSN/2320011077155", "203-FSB-2320011077155-4", 1, 0.005, ac,
                                 new Date(C0.getTime() + 27l*ONE_DAY),
                                 UITaskItineraryElement.SEA_MODE);
    if (DEBUG) {
      System.out.println("Adding itin : " + itin);
    }
    v.addElement(itin);

    // filter
    String forUnitFilter = myState.forUnitFilter;
    if (forUnitFilter != null) {
      Vector filteredV = new Vector();
      Enumeration taskits = v.elements();
      while (taskits.hasMoreElements()) {
        UITaskItinerary it = (UITaskItinerary)taskits.nextElement();
        if (it.getTransportedUnitName().equals(forUnitFilter)) {
            filteredV.addElement(it);
        }
      }
      v = filteredV;
    }
    Object inputTaskFilter = myState.inputTaskFilter;
    if (inputTaskFilter != null) {
      Vector filteredV = new Vector();
      Enumeration taskits = v.elements();
      if (inputTaskFilter instanceof String) {
        String taskStr = (String)inputTaskFilter;
        while (taskits.hasMoreElements()) {
          UITaskItinerary it = (UITaskItinerary)taskits.nextElement();
          if (taskStr.equals(it.getInputTaskUID())) {
            filteredV.addElement(it);
          }
        }
      } else {
        Set taskSet = (Set)inputTaskFilter;
        while (taskits.hasMoreElements()) {
          UITaskItinerary it = (UITaskItinerary)taskits.nextElement();
          if (taskSet.contains(it.getInputTaskUID())) {
            filteredV.addElement(it);
          }
        }
      }
      v = filteredV;
    }

    return v;
  }

  /** END CANNED MODE **/

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

  /** 
   * Use instead of "this" to force no instance field usage.
   **/
  protected static class MyPSPState extends PSPState {

    /** fields **/
    // output
    public String format;
    // for all modes
    public String mode;
    public String forUnitFilter;
    public Object inputTaskFilter;
    // for live mode
    public boolean interpolate;
    public boolean sortItins;
    public boolean includeUnload;
    public boolean ignoreOrgLegs;
    public boolean ignoreCarrierLegs;
    public HashMap seenAssets;
    public HashSet seenExpansions;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
      format = "data";
      mode = null;
      forUnitFilter = null;
      inputTaskFilter = null;
      interpolate = false;
      sortItins = false;
      includeUnload = false;
      ignoreOrgLegs = false;
      ignoreCarrierLegs = false;
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);
      if (name.equalsIgnoreCase("format")) {
        if (AbstractPrinter.isValidFormat(value)) {
          format = value;
        }
      } else if (name.equalsIgnoreCase("TransportedUnitName")) {
        forUnitFilter = value;
      } else if (name.equalsIgnoreCase("InputTaskUID")) {
        // currently only single String filter -- can add java.util.Set
        inputTaskFilter = value;
      } else if (name.equalsIgnoreCase("mode")) {
        mode = value;
      } else if (name.equalsIgnoreCase("interpolate")) {
        interpolate = 
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("sort")) {
        sortItins = 
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("includeUnload")) {
        includeUnload =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("ignoreOrgItineraryElements")) {
        ignoreOrgLegs =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("ignoreCarrierItineraryElements")) {
        ignoreCarrierLegs =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      }
      // some old-style ones
      else if (name.equalsIgnoreCase("nointerpolate")) {
        interpolate = false;
      } else if (name.equalsIgnoreCase("nosort")) {
        sortItins = false;
      } else if (name.equalsIgnoreCase("noincludeUnload")) {
        includeUnload = false;
      } else if (name.equalsIgnoreCase("noignoreOrgItineraryElements")) {
        ignoreOrgLegs = false;
      } else if (name.equalsIgnoreCase("noignoreCarrierItineraryElements")) {
        ignoreCarrierLegs = false;
      }
    }
  }

}
