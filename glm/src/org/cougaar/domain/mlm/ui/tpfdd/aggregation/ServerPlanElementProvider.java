/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/aggregation/Attic/ServerPlanElementProvider.java,v 1.8 2001-10-17 19:06:55 gvidaver Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.aggregation;


import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

import org.cougaar.domain.mlm.ui.tpfdd.util.VectorHashtable;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.tpfdd.gui.view.PassAll;
import org.cougaar.domain.mlm.ui.tpfdd.gui.view.TaskNode;
import org.cougaar.domain.mlm.ui.tpfdd.gui.view.QueryData;

import org.cougaar.domain.mlm.ui.tpfdd.producer.UnitHierarchy;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;
import org.cougaar.domain.mlm.ui.tpfdd.producer.AssetManifest;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UIItineraryElement;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElement;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElementCarrier;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITAssetInfo;

import org.cougaar.domain.mlm.ui.tpfdd.transit.*;
import org.cougaar.domain.mlm.ui.tpfdd.transit.UnitChronicle.UnitTransitData;
import org.cougaar.domain.mlm.ui.tpfdd.transit.AssetClassChronicle.AssetClassTransitData;
import org.cougaar.domain.mlm.ui.tpfdd.transit.CarrierUtilizationChronicle.CarrierUtilizationTransitData;
import org.cougaar.domain.mlm.ui.tpfdd.transit.AssetFlowChronicle.AssetFlowTransitData;
import org.cougaar.domain.mlm.ui.tpfdd.transit.ArrivalDeviationChronicle.ArrivalDeviationTransitData;

public class ServerPlanElementProvider extends PlanElementProvider
{
    private Hashtable structurePool;
    private VectorHashtable byUnit;
    // each itinerary is cloned for each different carrier involved; accounting system: as
    // legs filter in, need to be copy out to each one
    private VectorHashtable itineraryBrothers;
    private UnitHierarchy unitTree;
    private Vector itineraryPool;
    private Vector allowOrgItinsFrom;
    private static int count = 0;

  // Set to record asset/unit info
  // and set to record Itins missing unit info
  private Hashtable seenAssetUnit;
  private VectorHashtable itinsMissingUnit;
  
  /**Maps assetIds to lists of UnitTransitData items**/
  protected Map assetIdToUTDList = new HashMap();
  /**Maps assetIds to lists of AssetClassTransitData items**/
  protected Map assetIdToACTDList = new HashMap();
  /**Maps assetIds to lists of CarrierUtilizationTransitData items**/
  protected Map assetIdToCUTDList = new HashMap();
  /**Maps assetIds to lists of AssetFlowTransitData items**/
  protected Map assetIdToAFTDList = new HashMap();
  /**Maps assetIds to lists of ArrivalDeviationTransitData items**/
  protected Map assetIdToADTDList = new HashMap();

  protected boolean useTestData = false;

    public ServerPlanElementProvider(ClusterCache clusterCache, boolean cannedMode)
    {
	super(clusterCache, new PassAll(), cannedMode);
	allowOrgItinsFrom = clusterCache.getallowOrgNames();
	structurePool = new Hashtable();
	itineraryPool = new Vector();
	byUnit = new VectorHashtable();
	itineraryBrothers = new VectorHashtable();
	unitManifests = new Hashtable();
	unitTree = new UnitHierarchy(clusterCache.getHost());
	seenAssetUnit = new Hashtable();	 
	itinsMissingUnit = new VectorHashtable();
        count ++;

	if(useTestData)
	  testPopulateTransitData();
    }

    public UnitHierarchy getUnitTree()
    {
	return unitTree;
    }

    public TaskNode getRollupNode(String unitName)
    {
// 	System.out.println("getRollupNode: "+unitName);
	TaskNode rollupNode;

	if ( (rollupNode = (TaskNode)(UUIDPool.get("ROLLUP:" + 
						   unitName))) == null ) {
	    rollupNode = new TaskNode(this, unitName, false);
	    AssetManifest newManifest = new AssetManifest();
	    synchronized(unitManifests) {
		unitManifests.put(unitName, newManifest);
	    }
	    handleNewTaskNode(rollupNode);
	}
	return rollupNode;
    }

    public TaskNode getEquipmentNode(String unitName)
    {
// 	System.out.println("getEquipmentNode: "+unitName);
	TaskNode equipNode;

	if ( (equipNode = (TaskNode)(UUIDPool.get("EQUIP:" + 
						  unitName))) == null ) {
	    equipNode = new TaskNode(this, unitName);
	    handleNewTaskNode(equipNode);
	}
	return equipNode;
    }

  public TaskNode getByCarrierNode(String unitName)
  {
    TaskNode byCarrierNode;
    if ( (byCarrierNode =
	  (TaskNode)(UUIDPool.get("EQUIP/" + TaskNode.BY_CARRIER_TYPE + 
				  ":" + unitName))) == null ) {
      byCarrierNode = new TaskNode(this, unitName, TaskNode.BY_CARRIER_TYPE);
      handleNewTaskNode(byCarrierNode);
    }
    return byCarrierNode;
  }
  
  public TaskNode getByCargoNode(String unitName)
  {
    TaskNode byCargoNode;
    if ( (byCargoNode =
	  (TaskNode)(UUIDPool.get("EQUIP/" + TaskNode.BY_CARGO_TYPE + 
				  ":" + unitName))) == null ) {
      byCargoNode = new TaskNode(this, unitName, TaskNode.BY_CARGO_TYPE);
      handleNewTaskNode(byCargoNode);
    }
    return byCargoNode;
  }
  
  public TaskNode getCarrierNode(String unitName, String carrierType)
  {
    TaskNode carrierNode;
    if ( (carrierNode = (TaskNode)(UUIDPool.get("CARRIER_TYPE/" + 
						carrierType + ":" + 
						unitName))) == null ) {
      carrierNode = new TaskNode(this, unitName, carrierType, 
				 TaskNode.BY_CARRIER_TYPE);
      handleNewTaskNode(carrierNode);
    }
    return carrierNode;
  }

  public TaskNode getCargoNode(String unitName, String cargoType)
  {
    TaskNode cargoNode;
    if ( (cargoNode = (TaskNode)(UUIDPool.get("CARGO_TYPE/" + 
					      cargoType + ":" + 
					      unitName))) == null ) {
      cargoNode = new TaskNode(this, unitName, cargoType, 
			       TaskNode.BY_CARGO_TYPE);
      handleNewTaskNode(cargoNode);
    }
    return cargoNode;
  }
  
  private boolean isLoad(UITaskItineraryElement elem)
  {
    return elem.getStartLocation().equals(elem.getEndLocation());
  }
  
  private boolean startsInCONUS(UITaskItineraryElement elem)
  {
    return elem.getStartLocation().getLongitude().getDegrees() < 0;
  }
  
  private boolean endsInCONUS(UITaskItineraryElement elem)
  {
    return elem.getEndLocation().getLongitude().getDegrees() < 0;
  }
  
  private boolean startsInTheater(UITaskItineraryElement elem)
  {
    return elem.getStartLocation().getLongitude().getDegrees() > 0;
  }
  
  private boolean endsInTheater(UITaskItineraryElement elem)
  {
    return elem.getEndLocation().getLongitude().getDegrees() > 0;
  }
  
  private boolean isSeaLeg(UITaskItineraryElement elem)
  {
    return elem.getTransportationMode() == TaskNode.MODE_SEA;
  }
  
  private boolean isAirLeg(UITaskItineraryElement elem)
  {
    return elem.getTransportationMode() == TaskNode.MODE_AIR;
  }
  
//   public void printMissingAccount(){
//     Enumeration enum = itinsMissingUnit.elements();
//     int count = 0;
//     while (enum.hasMoreElements()) {
//       Vector vect = (Vector) enum.nextElement();
//       System.out.println(vect.size() + " things sitting around");
//     }     
//     if (count == 0) {
//       System.out.println("Nothing sitting around");
//     }
//   }

//   /**See if this uid has a recorded unit name
//    */
//   private String findStoredUnitName(String uid) {
//     String name = null;
//     synchronized (seenAssetUnit) {
//       name = (String) seenAssetUnit.get(uid);
//     }
//     return name;
//   }

//   /**Stores the unit name for this UID 
//    * Returns true if it gets stored, ie, it's not already in there
//    * Returns false if it's already there
//    **/
//   private boolean storeUnitName(String uid, String name) {
    
//     boolean response = false;
//     synchronized (seenAssetUnit) {
//       if (seenAssetUnit.get(uid) == null) {
// 	seenAssetUnit.put(uid,name);
// 	response = true;
//       }
//     }
//     return response;
//   }

//   /**
//    * Store itineraries that don't have transported unit names
//    * Store them by the UID of the asset in the itinerary
//    * Hopefully, something will eventually come in with the info
//    **/
//   private void storeItinerary(UITaskItinerary itinerary) {
//     //Vector hashtable is already synchronized!
//     itinsMissingUnit.put(itinerary.getAssetUID(),itinerary);
//   }

//   /**
//    * Process the stored itineraries
//    * Retrieve all the itineraries for the UID, and process them
//    * And, take them out of the table
//    **/
//   private void processStoredItineraries(String uid, String name) {
//     Vector storedItins = itinsMissingUnit.findAndRemove(uid);

//     if (storedItins != null) {

//       for (Iterator i = storedItins.iterator(); i.hasNext();) {
// 	UITaskItinerary itin = (UITaskItinerary)i.next();
// 	itin.setTransportedUnitName(name);
//        	handleCompleteItinerary(itin,name);	  
//       }
//     }
//   }

  /**
   * function to process itineraries received
   * Does preliminary work -- based on whether FOR information
   * was in original task -- to match up the asset with
   * transportedUnitName(the FOR information)
   **/
//   private void handleNewItinerary(UITaskItinerary itinerary) {
//       System.out.println("HANDLING: "+itinerary);

//     // If null, no FOR information was attached to the task
//     // Have to see if anything else for this asset came in with
//     // the FOR information
//     if (itinerary.getTransportedUnitName() == null) {
//       String foundName = findStoredUnitName(itinerary.getAssetUID());
//       // Info already recorded in server, so just fill out itinerary
//       // and pass on for processing
//       if (foundName != null) {
// 	itinerary.setTransportedUnitName(foundName);
// 	handleCompleteItinerary(itinerary,foundName);	  
//       }
//       else {
// 	storeItinerary(itinerary);
//       }
//     }
    
//     // unitName included in UITaskItinerary 
//     // We can process this intinerary, but we also need to store
//     // this asset/unitName info if we don't have it already, and
//     // we need to process any intineraries waiting around for this
//     // info
//     else {

//       String unitName = 
// 	PathString.basename(itinerary.getTransportedUnitName());


//       handleCompleteItinerary(itinerary,unitName);
//       if (storeUnitName(itinerary.getAssetUID(),unitName) == true)
// 	processStoredItineraries(itinerary.getAssetUID(),unitName);
//     }
//   } 

  boolean doingOldFiltering = false;

  /**
   * Actual work horse function that is called once we are sure that
   * the Itinerary has getTransportedUnitName filled out
   * eventually, makes all the unit info match up together
   * One itinerary will generally represent only 1 asset
   */
  private void handleCompleteItinerary(UITaskItinerary itinerary, 
					String unitName) {
      //System.out.println("HANDLING: "+itinerary);
	
    // Store the itineraries -- this is used for saving the state
    // of the aggregation server
    synchronized(itineraryPool) {
      itineraryPool.add(itinerary);
    }    


    addToTransitData(itinerary);

    // Get basic info from the itinerary
    String clusterID = itinerary.getClusterID();
    Vector schedule = itinerary.getScheduleElements();
    int numLegs = schedule.size();
    int numGoodLegs = 0;
    Vector newNodes = new Vector();
    UITaskItineraryElement[] goodLegs = new UITaskItineraryElement[numLegs];
    // set up some variables
    UITaskItineraryElement itinElem = null; 
    UITaskItineraryElement seaLeg = null;
    UITaskItineraryElement unloadLeg = null;
    UITaskItineraryElement airLeg = null;
    
    // Call Static function to figure out the UUID
    String itineraryUUID = TaskNode.deriveUUID(itinerary, "");
    
    UITAssetInfo assetInfo = (UITAssetInfo)
      (itinerary.getUITAssetInfoVector().get(0));
    String cargoName = assetInfo.getItemID();
    String cargoType = assetInfo.getTypeNomenclature();
    Vector carrierItins = itineraryBrothers.vectorGetWithCreate(itineraryUUID);
    
    //if ( numLegs > 10 ) {
    //Debug.out(itinerary.toString());
    //}
    synchronized(carrierItins) {
      boolean alreadyHasAirLeg = false;
      for ( int i = 0; i < numLegs; i++ ) {
	itinElem = (UITaskItineraryElement)(schedule.get(i));
	if (doingOldFiltering) {
	  // filter 1: ignore ships returning to CONUS ports
	  if ( isSeaLeg(itinElem) && startsInTheater(itinElem) && 
		   endsInCONUS(itinElem) ) {
		//Debug.out("handle -- dropping sea return leg");
		continue;
		// filter 2: ignore load tasks at CONUS (they are distinct unlike theater unload tasks)
	  }
	  if ( isLoad(itinElem) && endsInCONUS(itinElem) ) {
		Debug.out("ServerPlanElementProvider.handleCompleteItinerary -- dropping load task b/c it ends in CONUS");	  
		continue;
	  }
	  // filter 3: handle unload tasks in Theater by simply subtracting off unload time from ship leg
	  if ( isLoad(itinElem) && startsInTheater(itinElem) ) {
		//Debug.out("handle -- dropping unload in theater");
		unloadLeg = itinElem;
		continue;
	  }
	  if ( isSeaLeg(itinElem) )
		seaLeg = itinElem;
	  if ( isAirLeg(itinElem) ) {
		if ( alreadyHasAirLeg ) {
		  Debug.out("ServerPlanElementProvider.handleCompleteItinerary -- Warning: throwing away duplicate air leg in " + itineraryUUID + "?");
		  //			continue;
		}
		else {
		  //Debug.out("handle -- Adding air leg");
		  alreadyHasAirLeg = true;
		  airLeg = itinElem;
		}
	  }
	}
	String carrierName;
	String carrierType;
	if ( itinElem instanceof UITaskItineraryElementCarrier ) {
	  UITaskItineraryElementCarrier carrierLeg = 
	    (UITaskItineraryElementCarrier)itinElem;
	  carrierName = carrierLeg.getCarrierItemNomenclature();
	  carrierType = carrierLeg.getCarrierTypeNomenclature();
	  int dash = carrierType.indexOf(" - ");
	  if ( dash != -1 )
	    carrierType = carrierType.substring(dash + 3, 
						carrierType.length());
	}
	else {
	  carrierName = "UNK " + unitName + " carrier";
	  carrierType = "UNK " + unitName + " carrier";
	}
	//Debug.out("handle -- carrierName = " + carrierName);
	TaskNode byCarrierTypeNode = 
	  (TaskNode)(UUIDPool.get(TaskNode.deriveUUID(itinerary, 
						      carrierType)));
	if ( byCarrierTypeNode == null ) {
	  //Debug.out("Making new carrier node " + cargoName);
	  byCarrierTypeNode = new TaskNode(this, 
					   itinerary, 
					   carrierName, 
					   carrierType, 
					   cargoName, 
					   cargoType,
					   TaskNode.BY_CARRIER_TYPE);
	  newNodes.add(byCarrierTypeNode);
	  handleNewTaskNode(byCarrierTypeNode);
	  itineraryBrothers.put(itineraryUUID, byCarrierTypeNode);
	}
	
	TaskNode byCargoTypeNode = 
	  (TaskNode)(UUIDPool.get(TaskNode.deriveUUID(itinerary, cargoType)));
	if ( byCargoTypeNode == null ) {
	  byCargoTypeNode = new TaskNode(this, 
					 itinerary, 
					 carrierName, 
					 carrierType, 
					 cargoName, 
					 cargoType,
					 TaskNode.BY_CARGO_TYPE);
	  newNodes.add(byCargoTypeNode);
	  handleNewTaskNode(byCargoTypeNode);
	  itineraryBrothers.put(itineraryUUID, byCargoTypeNode);
	}
	
	goodLegs[numGoodLegs] = itinElem;
	numGoodLegs++;
      }
      	  if (doingOldFiltering) {
		if ( unloadLeg != null ) {
		  if ( airLeg == null && seaLeg == null )
			OutputHandler.out("SServerPlanElementProvider:hNI Errning: got unloadLeg w/o sea/airLeg: "
							  + itineraryUUID + " " + clusterID);
		  else if ( airLeg != null && seaLeg != null )
			OutputHandler.out("SServerPlanElementProvider:hNI Errning: got unloadLeg w/both sea and airLegs: "
							  + itineraryUUID + " " + clusterID);
		  else if ( airLeg != null ) {
			if ( airLeg.getEndDate().after(unloadLeg.getStartDate()) ) {
			  Debug.out("SServerPlanElementProvider:hNI Moving airLeg of " + itineraryUUID + " from " + airLeg.getEndDate()
						+ " to " + unloadLeg.getStartDate());
			  airLeg.setEndDate(unloadLeg.getStartDate());
			}
		  }
		  else {
			if ( seaLeg.getEndDate().after(unloadLeg.getStartDate()) ) {
			  Debug.out("SServerPlanElementProvider:hNI Moving seaLeg of " + itineraryUUID + " from " + seaLeg.getEndDate()
						+ " to " + unloadLeg.getStartDate());
			  seaLeg.setEndDate(unloadLeg.getStartDate());
			}
		  }
		}
	  }
      
      if ( numGoodLegs == 0 )
	return;
      
      carrierItins = itineraryBrothers.vectorGet(itineraryUUID);
      
      /*  A new asset node is created with first sighting of an
	  asset.  So, there is no previously recorded stuff that
	  is relevant
	  A new carrier node only cares about stuff for the carrier
	  now, so there would not be any previously recorded
	  relevant stuff for it  */
      // copy the old legs over to the new itinerary brothers; 
      // get the new siblings up to speed
      /*      if ( carrierItins.size() > numGoodLegs ) { 
	// if = then just the new guys; no old legs
	TaskNode protoNode = (TaskNode)(carrierItins.get(0));
	for ( Iterator i = newNodes.iterator(); i.hasNext(); ) {
	  TaskNode newSibling = (TaskNode)(i.next());
	  for ( int k = 0; k < protoNode.getChildCount_(); k++ ) {
	    TaskNode childLeg = protoNode.getChild_(k).copy();
	    // fake the constructor; this is crappy but
	    // otherwise we'd have to keep track of the itinerary
	    // leg that generated it.
	    childLeg.setUUID(newSibling.getUUID() + "." + k);
	    childLeg.setParent_(newSibling);
	    childLeg.setParentUUID(newSibling.getUUID());
	    childLeg.setParentCarrierName(newSibling.getCarrierName());
	    childLeg.setParentCarrierType(newSibling.getCarrierType());
	    //if (newSibling.getParent_().getSourceType() == 
	    //TaskNode.CARGO_TYPE) {
	      
	    handleNewTaskNode(childLeg);
	    long earliest = 
	      Math.min(childLeg.getActualStart() != 0 ? 
		       childLeg.getActualStart()
		       : TimeSpan.MAX_VALUE,
		       childLeg.getMinStart() != 0 ? childLeg.getMinStart()
		       : TimeSpan.MAX_VALUE);
	    long latest = 
	      Math.max(childLeg.getActualEnd(), childLeg.getMinEnd());
	    
	    childLeg.getParent_().propagateStart(earliest);
	    childLeg.getParent_().propagateEnd(latest);
	  }
	}
	} */
      
      // copy the new legs over to everybody now, even if just the new guys. 
      // (no one has the newLegs yet.)
      for ( int j = 0; j < numGoodLegs; j++ ) {
	for ( Iterator i = carrierItins.iterator(); i.hasNext(); ) {
	  TaskNode brother = (TaskNode)(i.next());
	  // Implied stuffo -- only add if this is the same carrier
	  // Cargo nodes don't add indirect itinerary elements to them
	  // Carrier nodes only care if the element is for that particular
	  // carrier
	  // Only the parent knows if this is a cargo or carrier node
	  TaskNode childLeg;
	  // Cargo node
	  if (brother.getParent_().getSourceType() == TaskNode.CARGO_TYPE) {
	    if (goodLegs[j].getIsDirectElement() == true) {
	      childLeg = new TaskNode(this, goodLegs[j], brother, clusterID,
				      TaskNode.CARGO_TYPE);
	      childLeg.setDirectTag(TaskNode.DIRECT_TAG);
	      if (goodLegs[j].getIsOverlapElement())
		childLeg.setTripTag(TaskNode.NON_TRIP_TAG);
	      else
		childLeg.setTripTag(TaskNode.TRIP_TAG);
		handleNewTaskNode(childLeg);
	    }
	  }
	  // Carrier node
	  else {

	    // If it's not an instance of that, then I don't know why
	    // it's here.  If things are disappearing, this could be
	    // changed to just add the thing anyway.
	    if ( goodLegs[j] instanceof UITaskItineraryElementCarrier ) {
		UITaskItineraryElementCarrier carrierLeg = (UITaskItineraryElementCarrier)goodLegs[j];
		if (brother.getCarrierType().equals(carrierLeg.getCarrierTypeNomenclature())) {
		childLeg = new TaskNode(this, 
					goodLegs[j], 
					brother, 
					clusterID,
					TaskNode.CARRIER_TYPE);
		// Everything for a carrier is part of it's trip!
		childLeg.setTripTag(TaskNode.TRIP_TAG);
		// Record direct or implied, not so important, but stuff
		// it in here anyway
		if (goodLegs[j].getIsDirectElement() == true) {
		  childLeg.setDirectTag(TaskNode.DIRECT_TAG);
		}
		else
		  childLeg.setDirectTag(TaskNode.IMPLIED_TAG);
		handleNewTaskNode(childLeg);
	      }		  

	    }
	  } // end of else for if cargo
	} // end of for for carrier itins
      } // end of for for goodLegs
    }
    
    
  }

  /*  
  private void oldhandleNewItinerary(UITaskItinerary itinerary)
  {
    String unitName = PathString.basename(itinerary.getTransportedUnitName());
    synchronized(itineraryPool) {
      itineraryPool.add(itinerary);
    }
    String clusterID = itinerary.getClusterID();
    //Debug.out("Handle -- working on stuff from cluster " + clusterID);
    //Debug.out(itinerary.toString());
    Vector schedule = itinerary.getScheduleElements();
    int numLegs = schedule.size();
    int numGoodLegs = 0;
    Vector newNodes = new Vector();
    UITaskItineraryElement[] goodLegs = new UITaskItineraryElement[numLegs];
    UITaskItineraryElement itinElem, seaLeg = null, unloadLeg = null, airLeg = null;
    String itineraryUUID = TaskNode.deriveUUID(itinerary, "");
    
    UITAssetInfo assetInfo = (UITAssetInfo)(itinerary.getUITAssetInfoVector().get(0));
    String cargoName = assetInfo.getItemID();
    String cargoType = assetInfo.getTypeNomenclature();
    Vector carrierItins = itineraryBrothers.vectorGetWithCreate(itineraryUUID);
    if ( numLegs > 10 ) {
      Debug.out(itinerary.toString());
    }
    synchronized(carrierItins) {
      boolean alreadyHasAirLeg = false;
      for ( int i = 0; i < numLegs; i++ ) {
	itinElem = (UITaskItineraryElement)(schedule.get(i));
	// filter 1: ignore ships returning to CONUS ports
	if ( isSeaLeg(itinElem) && startsInTheater(itinElem) && endsInCONUS(itinElem) ) {
	  //Debug.out("handle -- dropping sea return leg");
	  continue;
	  // filter 2: ignore load tasks at CONUS (they are distinct unlike theater unload tasks)
	}
	if ( isLoad(itinElem) && endsInCONUS(itinElem) ) {
	  //Debug.out("handel -- dropping load task");
	  
	  continue;
	}
	// filter 3: handle unload tasks in Theater by simply subtracting off unload time from ship leg
	if ( isLoad(itinElem) && startsInTheater(itinElem) ) {
	  //Debug.out("handle -- dropping unload in theater");
	  unloadLeg = itinElem;
	  continue;
	}
	if ( isSeaLeg(itinElem) )
	  seaLeg = itinElem;
	if ( isAirLeg(itinElem) ) {
	  if ( alreadyHasAirLeg ) {
	    Debug.out("SPEP:hNI Warning: throwing away duplicate air leg in " + itineraryUUID + "?");
	    //			continue;
	  }
	  else {
	    //Debug.out("handle -- Adding air leg");
	    alreadyHasAirLeg = true;
	    airLeg = itinElem;
	  }
	}
	String carrierName;
	String carrierType;
	if ( itinElem instanceof UITaskItineraryElementCarrier ) {
	  UITaskItineraryElementCarrier carrierLeg = (UITaskItineraryElementCarrier)itinElem;
	  carrierName = carrierLeg.getCarrierItemNomenclature();
	  carrierType = carrierLeg.getCarrierTypeNomenclature();
	  int dash = carrierType.indexOf(" - ");
	  if ( dash != -1 )
	    carrierType = carrierType.substring(dash + 3, carrierType.length());
	}
	else {
	  carrierName = "UNK " + unitName + " carrier";
	  carrierType = "UNK " + unitName + " carrier";
	}
	//Debug.out("handle -- carrierName = " + carrierName);
	TaskNode byCarrierTypeNode = (TaskNode)(UUIDPool.get(TaskNode.deriveUUID(itinerary, carrierType)));
	if ( byCarrierTypeNode == null ) {
	  //Debug.out("Making new carrier node " + cargoName);
	  byCarrierTypeNode = new TaskNode(this, itinerary, 
					   carrierName, carrierType, cargoName, cargoType,
					   TaskNode.BY_CARRIER_TYPE);
	  newNodes.add(byCarrierTypeNode);
	  handleNewTaskNode(byCarrierTypeNode);
	  itineraryBrothers.put(itineraryUUID, byCarrierTypeNode);
	}
	
	TaskNode byCargoTypeNode = (TaskNode)(UUIDPool.get(TaskNode.deriveUUID(itinerary, cargoType)));
	if ( byCargoTypeNode == null ) {
	  Debug.out("Making new cargo node " + cargoName);
	  byCargoTypeNode = new TaskNode(this, itinerary, 
					 carrierName, carrierType, cargoName, cargoType,
					 TaskNode.BY_CARGO_TYPE);
	  newNodes.add(byCargoTypeNode);
	  handleNewTaskNode(byCargoTypeNode);
	  itineraryBrothers.put(itineraryUUID, byCargoTypeNode);
	}
	
	goodLegs[numGoodLegs] = itinElem;
	numGoodLegs++;
      }
      
      if ( unloadLeg != null ) {
	if ( airLeg == null && seaLeg == null )
	  OutputHandler.out("SServerPlanElementProvider:hNI Errning: got unloadLeg w/o sea/airLeg: "
			    + itineraryUUID + " " + clusterID);
	else if ( airLeg != null && seaLeg != null )
	  OutputHandler.out("SServerPlanElementProvider:hNI Errning: got unloadLeg w/both sea and airLegs: "
			    + itineraryUUID + " " + clusterID);
	else if ( airLeg != null ) {
	  if ( airLeg.getEndDate().after(unloadLeg.getStartDate()) ) {
	    Debug.out("SServerPlanElementProvider:hNI Moving airLeg of " + itineraryUUID + " from " + airLeg.getEndDate()
		      + " to " + unloadLeg.getStartDate());
	    airLeg.setEndDate(unloadLeg.getStartDate());
	  }
	}
	else {
	  if ( seaLeg.getEndDate().after(unloadLeg.getStartDate()) ) {
	    Debug.out("SServerPlanElementProvider:hNI Moving seaLeg of " + itineraryUUID + " from " + seaLeg.getEndDate()
		      + " to " + unloadLeg.getStartDate());
	    seaLeg.setEndDate(unloadLeg.getStartDate());
	  }
	}
      }
      
      if ( numGoodLegs == 0 )
	return;
      
      carrierItins = itineraryBrothers.vectorGet(itineraryUUID);
      
      // copy the old legs over to the new itinerary brothers; get the new siblings up to speed
      if ( carrierItins.size() > numGoodLegs ) { // if = then just the new guys; no old legs
	TaskNode protoNode = (TaskNode)(carrierItins.get(0));
	for ( Iterator i = newNodes.iterator(); i.hasNext(); ) {
	  TaskNode newSibling = (TaskNode)(i.next());
	  for ( int k = 0; k < protoNode.getChildCount_(); k++ ) {
	    TaskNode childLeg = protoNode.getChild_(k).copy();
	    // fake the constructor; this is crappy but otherwise we'd have to keep track of the itinerary
	    // leg that generated it.
	    childLeg.setUUID(newSibling.getUUID() + "." + k);
	    System.out.println("new uuid = " +
			       childLeg.getUUID());
	    childLeg.setParent_(newSibling);
	    childLeg.setParentUUID(newSibling.getUUID());
	    childLeg.setParentCarrierName(newSibling.getCarrierName());
	    childLeg.setParentCarrierType(newSibling.getCarrierType());
	    handleNewTaskNode(childLeg);
	    long earliest = Math.min(childLeg.getActualStart() != 0 ? childLeg.getActualStart()
				     : TimeSpan.MAX_VALUE,
				     childLeg.getMinStart() != 0 ? childLeg.getMinStart()
				     : TimeSpan.MAX_VALUE);
	    long latest = Math.max(childLeg.getActualEnd(), childLeg.getMinEnd());
	    
	    childLeg.getParent_().propagateStart(earliest);
	    childLeg.getParent_().propagateEnd(latest);
	  }
	}
      }
      
      // copy the new legs over to everybody now, even if just the new guys. (no one has the newLegs yet.)
      for ( int j = 0; j < numGoodLegs; j++ ) {
	for ( Iterator i = carrierItins.iterator(); i.hasNext(); ) {
	  TaskNode brother = (TaskNode)(i.next());
	  TaskNode childLeg = new TaskNode(this, goodLegs[j], brother, clusterID);
	  handleNewTaskNode(childLeg);
	}
      }
    }
  }
  */
	
  public Vector lookup(QueryData query){
    Vector answers = new Vector();
 
    
    String[] unitNames = query.getUnitNames();
    synchronized(byUnit) {
      for ( int i = 0; i < unitNames.length; i++ ) {
	Vector unitTasks = (Vector)(byUnit.get(unitNames[i]));
       	if ( unitTasks != null && !unitTasks.isEmpty()) {
	  for (Enumeration e = unitTasks.elements(); e.hasMoreElements() ;) {
	    TaskNode node = (TaskNode)e.nextElement();
	    if (query.admits(node))
	      answers.add(node);
	  }
	}
      }
    }
    return answers;
  }
  
  protected void handleNewTaskNode(TaskNode node){
    synchronized(byUnit) {
      byUnit.put(node.getUnitName(), node);
    }
    if ( node.isStructural() )
      synchronized(structurePool) {
	structurePool.put(node.getUUID(), node);
      }
    else {
      AssetManifest manifest = (AssetManifest)
	(unitManifests.get(node.getUnitName()));
      if ( manifest == null )
	OutputHandler.out("ServerPlanElementProvider:hNTN Errning: "+
			  "missing manifest for unit " + node.getUnitName());
      else {
	manifest.addCarrierName(node.getCarrierName(), node.getCarrierType());
	manifest.addCarrierType(node.getCarrierType());
	manifest.addCargoName(node.getDirectObjectName(), node.getDirectObjectType());
	manifest.addCargoType(node.getDirectObjectType());
      }
    }
    super.handleNewTaskNode(node);
  }

  public void fireItemAdded(Object item){
      if ( item instanceof UITaskItinerary ) {
	  String unitName = ((UITaskItinerary)item).getTransportedUnitName();
	  if (unitName == null) { 
	      System.err.println("Discarding itinerary with bad unit info: "+item); 
	  } else {
	      handleCompleteItinerary((UITaskItinerary)item,
				      PathString.basename(unitName));
	  }
      }
    else if ( item instanceof TaskNode )
      handleNewTaskNode((TaskNode)item);
    else if ( item instanceof Object[] ) {
      Object[] items = (Object[])item;
      for ( int i = 0; i < items.length; i++ )
	fireItemAdded(items[i]);
      return;
    }
    else
      OutputHandler.out("ServerPlanElementProvider:fIA item of type " + 
			item.getClass().getName() + ", ignoring.");
  }

  /** Add to the TransitData lists. **/
  protected void addToTransitData(UITaskItinerary itinerary){
    //Add new UnitChronicle.UnitTransitData to assetIdToUTDList
    synchronized(assetIdToUTDList){
      addToUTDList(itinerary);
    }
    synchronized(assetIdToACTDList){
      addToACTDList(itinerary);
    }
    synchronized(assetIdToCUTDList){
      addToCUTDList(itinerary);
    }
    synchronized(assetIdToAFTDList){
      addToAFTDList(itinerary);
    }
    synchronized(assetIdToADTDList){
      addToADTDList(itinerary);
    }
  }

  /**
   * create new UnitChronicle.UnitTransitData for itinerary and add to
   * assetIdToUTDList
   **/
  protected void addToUTDList(UITaskItinerary itinerary){
    String assetID=itinerary.getAssetUID().intern();
    
    List utdList=(List)assetIdToUTDList.get(assetID);
    if(utdList==null){
      utdList=new ArrayList();
      assetIdToUTDList.put(assetID,utdList);
    }
    //now get the unit and the count:
    String unit=itinerary.getTransportedUnitName();
    if(unit==null||unit=="")
      unit="Unknown";
    List aiv = itinerary.getUITAssetInfoVector();
    int count=1;
    if(aiv!=null && aiv.size() > 0){
      UITAssetInfo ai=(UITAssetInfo)aiv.get(0);
      count=ai.getQuantity();
    }else{
      System.err.println("Could not determine count");
    }
    //now walk the element list:
    List schedElem = itinerary.getScheduleElements();
    for(int i=0;i<schedElem.size();i++){
      UITaskItineraryElement ie=(UITaskItineraryElement)schedElem.get(i);
      GeolocLocation s=ie.getStartLocation();
      GeolocLocation e=ie.getEndLocation();
      //If the task starts and ends at the same place, don't include it.
      if(s. getGeolocCode().equals(e.getGeolocCode()))
	continue;
      //If this is an implied leg (if transport going from home base to pick up
      //or returning from drop off home, then ignore because the material is 
      //not being moved on that leg).
      if(!ie.getIsDirectElement())
	continue;
      UnitTransitData utd=new 
	UnitTransitData(new FixedPosition(s.getGeolocCode(),
					  s.getLatitude().getDegrees(),
					  s.getLongitude().getDegrees()),
			new FixedPosition(e.getGeolocCode(),
					  e.getLatitude().getDegrees(),
					  e.getLongitude().getDegrees()),
			ie.getStartDate().getTime(),
			ie.getEndDate().getTime(),
			unit,count);
      utdList.add(utd);
    }
  }

  /**
   * create new AssetClassChronicle.AssetClassTransitData for itinerary and 
   * add to assetIdToACTDList
   **/
  protected void addToACTDList(UITaskItinerary itinerary){
    String assetID=itinerary.getAssetUID().intern();
    
    List actdList=(List)assetIdToACTDList.get(assetID);
    if(actdList==null){
      actdList=new ArrayList();
      assetIdToACTDList.put(assetID,actdList);
    }
    //now get the assetClass and the count:
    List aiv = itinerary.getUITAssetInfoVector();
    int count=1;
    int assetClass=0;
    if(aiv!=null && aiv.size() > 0){
      UITAssetInfo ai=(UITAssetInfo)aiv.get(0);
      count=ai.getQuantity();
      //Probably should actually deal with multiple classes in one package
      //at some point....
      int[] classes=ai.getAssetClasses();
      if(classes!=null)
	assetClass=classes[0];
      else{
	System.err.println("getAssetClass() returned null");
      }
    }else{
      System.err.println("Could not determine count or asset class");
    }
    //now walk the element list:
    List schedElem = itinerary.getScheduleElements();
    for(int i=0;i<schedElem.size();i++){
      UITaskItineraryElement ie=(UITaskItineraryElement)schedElem.get(i);
      GeolocLocation s=ie.getStartLocation();
      GeolocLocation e=ie.getEndLocation();
      //If the task starts and ends at the same place, don't include it.
      if(s. getGeolocCode().equals(e.getGeolocCode()))
	continue;
      //If this is an implied leg (if transport going from home base to pick up
      //or returning from drop off home, then ignore because the material is 
      //not being moved on that leg).
      if(!ie.getIsDirectElement())
	continue;
      AssetClassTransitData actd=new 
	AssetClassTransitData(new FixedPosition(s.getGeolocCode(),
						s.getLatitude().getDegrees(),
						s.getLongitude().getDegrees()),
			      new FixedPosition(e.getGeolocCode(),
						e.getLatitude().getDegrees(),
						e.getLongitude().getDegrees()),
			      ie.getStartDate().getTime(),
			      ie.getEndDate().getTime(),
			      assetClass,count);
      actdList.add(actd);
    }
  }

  /**
   * create new CarrierUtilizationChronicle.CarrierUtilizationTransitData 
   * for itinerary and add to assetIdToCUTDList
   **/
  protected void addToCUTDList(UITaskItinerary itinerary){
    String assetID=itinerary.getAssetUID().intern();
    
    List cutdList=(List)assetIdToCUTDList.get(assetID);
    if(cutdList==null){
      cutdList=new ArrayList();
      assetIdToCUTDList.put(assetID,cutdList);
    }
    //now walk the element list:
    List schedElem = itinerary.getScheduleElements();
    for(int i=0;i<schedElem.size();i++){
      UITaskItineraryElement ie=(UITaskItineraryElement)schedElem.get(i);
      String carrierUID="Unknown";
      if(ie instanceof UITaskItineraryElementCarrier){
	UITaskItineraryElementCarrier iec = 
	  (UITaskItineraryElementCarrier)ie;
	if(iec.getCarrierUID()!= null){
	  carrierUID=iec.getCarrierUID();
	}
      }

      GeolocLocation s=ie.getStartLocation();
      GeolocLocation e=ie.getEndLocation();
      //If the task starts and ends at the same place, don't include it.
      if(s. getGeolocCode().equals(e.getGeolocCode()))
	continue;
      //If this is an implied leg (if transport going from home base to pick up
      //or returning from drop off home, then ignore because the material is 
      //not being moved on that leg).
      //if(!ie.getIsDirectElement())
      //	continue;
      CarrierUtilizationTransitData cutd=new 
	CarrierUtilizationTransitData(new 
	  FixedPosition(s.getGeolocCode(),
			s.getLatitude().getDegrees(),
			s.getLongitude().getDegrees()),
				      new 
	  FixedPosition(e.getGeolocCode(),
			e.getLatitude().getDegrees(),
			e.getLongitude().getDegrees()),
				      ie.getStartDate().getTime(),
				      ie.getEndDate().getTime(),
				      carrierUID);
      cutdList.add(cutd);
    }
  }


  /**
   * create new AssetFlowChronicle.AssetFlowTransitData for itinerary and 
   * add to assetIdToAFTDList
   **/
  protected void addToAFTDList(UITaskItinerary itinerary){
    String assetID=itinerary.getAssetUID().intern();
    
    List aftdList=(List)assetIdToAFTDList.get(assetID);
    if(aftdList==null){
      aftdList=new ArrayList();
      assetIdToAFTDList.put(assetID,aftdList);
    }
    //now get the assetClass, count and the weight:
    List aiv = itinerary.getUITAssetInfoVector();
    int count=1;
    int assetClass=0;
    float weight=1f;
    if(aiv!=null && aiv.size() > 0){
      UITAssetInfo ai=(UITAssetInfo)aiv.get(0);
      count=ai.getQuantity();
      weight=(float)ai.getTons();
      //Probably should actually deal with multiple classes in one package
      //at some point....
      int[] classes=ai.getAssetClasses();
      if(classes!=null)
	assetClass=classes[0];
      else{
	System.err.println("getAssetClass() returned null");
      }
    }else{
      System.err.println("Could not determine asset class, count or weight");
    }
    //now walk the element list:
    List schedElem = itinerary.getScheduleElements();
    for(int i=0;i<schedElem.size();i++){
      UITaskItineraryElement ie=(UITaskItineraryElement)schedElem.get(i);
      GeolocLocation s=ie.getStartLocation();
      GeolocLocation e=ie.getEndLocation();
      //If the task starts and ends at the same place, don't include it.
      if(s. getGeolocCode().equals(e.getGeolocCode()))
	continue;
      //If this is an implied leg (if transport going from home base to pick up
      //or returning from drop off home, then ignore because the material is 
      //not being moved on that leg).
      if(!ie.getIsDirectElement())
	continue;
      AssetFlowTransitData aftd=new 
	AssetFlowTransitData(new FixedPosition(s.getGeolocCode(),
					       s.getLatitude().getDegrees(),
					       s.getLongitude().getDegrees()),
			     new FixedPosition(e.getGeolocCode(),
					       e.getLatitude().getDegrees(),
					       e.getLongitude().getDegrees()),
			     ie.getStartDate().getTime(),
			     ie.getEndDate().getTime(),
			     assetClass,
			     count,
			     weight);
      aftdList.add(aftd);
    }
  }

  /**
   * create new ArrivalDeviationChronicle.ArrivalDeviationTransitData 
   * for itinerary and add to assetIdToADTDList
   **/
  protected void addToADTDList(UITaskItinerary itinerary){
    String assetID=itinerary.getAssetUID().intern();
    
    List adtdList=(List)assetIdToADTDList.get(assetID);
    if(adtdList==null){
      adtdList=new ArrayList();
      assetIdToADTDList.put(assetID,adtdList);
    }
    //now get the unit and the count:
    String unit=itinerary.getTransportedUnitName();
    if(unit==null||unit=="")
      unit="Unknown";
    List aiv = itinerary.getUITAssetInfoVector();
    int count=1;
    if(aiv!=null && aiv.size() > 0){
      UITAssetInfo ai=(UITAssetInfo)aiv.get(0);
      count=ai.getQuantity();
    }else{
      System.err.println("Could not determine count");
    }
    //now walk the element list:
    List schedElem = itinerary.getScheduleElements();
    for(int i=0;i<schedElem.size();i++){
      UITaskItineraryElement ie=(UITaskItineraryElement)schedElem.get(i);
      GeolocLocation s=ie.getStartLocation();
      GeolocLocation e=ie.getEndLocation();
      //If the task starts and ends at the same place, don't include it.
      if(s. getGeolocCode().equals(e.getGeolocCode()))
	continue;
      //If this is an implied leg (if transport going from home base to pick up
      //or returning from drop off home, then ignore because the material is 
      //not being moved on that leg).
      if(!ie.getIsDirectElement())
	continue;
      ArrivalDeviationTransitData adtd=new 
	ArrivalDeviationTransitData(new 
	  FixedPosition(s.getGeolocCode(),
			s.getLatitude().getDegrees(),
			s.getLongitude().getDegrees()),
	  new FixedPosition(e.getGeolocCode(),
			    e.getLatitude().getDegrees(),
			    e.getLongitude().getDegrees()),
				    ie.getStartDate().getTime(),
				    ie.getEndDate().getTime(),
				    ie.getEndBestDate().getTime(),
				    unit,count);
      adtdList.add(adtd);
    }
  }

  /** for Unit Transit data */
  public Map getAssetIdToUTDMap(){
    return assetIdToUTDList;
  }

  /** for Unit Transit data */
  public Map getAssetIdToACTDMap(){
    return assetIdToACTDList;
  }

  /** for Carrier Utilization Transit data */
  public Map getAssetIdToCUTDMap(){
    return assetIdToCUTDList;
  }

  /** for Asset Flow Transit data */
  public Map getAssetIdToAFTDMap(){
    return assetIdToAFTDList;
  }

  /** for Asset Flow Transit data */
  public Map getAssetIdToADTDMap(){
    return assetIdToADTDList;
  }

  /** for testing transit data */
  private void testPopulateTransitData() 
  {
    //Debug.out ("populating assetIdToUTDList");
    assetIdToUTDList.put("Id1", getUTDListSample1());
    assetIdToUTDList.put("Id2", getUTDListSample2());
  }

  private List getUTDListSample1(){
    Calendar cal = Calendar.getInstance();	
    Date when1 = cal.getTime();
    int day  = cal.get (Calendar.DAY_OF_YEAR);
    cal.set (Calendar.DAY_OF_YEAR, day+1);
    Date when2 = cal.getTime ();
    cal.set (Calendar.DAY_OF_YEAR, day+2);
    Date when3 = cal.getTime ();

    FixedPosition bbn=new FixedPosition("BBN",42f,-72f);
    FixedPosition dammam=new FixedPosition("Dammam", 26.43f, 50.10f);
    FixedPosition somewhere=new FixedPosition("somewhere", -27f, 32f);
    List utdList = new ArrayList();
    UnitChronicle.UnitTransitData utd = new 
      UnitTransitData(bbn,dammam, when1.getTime(), when2.getTime(),"Unit 1", 3);
    utdList.add(utd);
    utd = new 
      UnitTransitData(dammam, somewhere, when2.getTime(), when3.getTime(),"Unit 1", 3);
    utdList.add(utd);
    Collections.sort(utdList);
    return utdList;
  }

  private List getUTDListSample2(){
    Calendar cal = Calendar.getInstance();	
    Date when1 = cal.getTime();
    int day  = cal.get (Calendar.DAY_OF_YEAR);
    cal.set (Calendar.DAY_OF_YEAR, day+1);
    Date when2 = cal.getTime ();
    cal.set (Calendar.DAY_OF_YEAR, day+2);
    Date when3 = cal.getTime ();

    FixedPosition ftStewart=new FixedPosition("Ft.Stewart", 31.85f, -81.60f);
    FixedPosition jubail=new FixedPosition("Jubail", 27f, 49.67f);

    List utdList = new ArrayList();
    UnitChronicle.UnitTransitData utd = new 
      UnitTransitData(ftStewart,jubail, when1.getTime(), when2.getTime(),"Unit 1", 5);
    utdList.add(utd);
    Collections.sort(utdList);
    return utdList;
  }

  /** for unit chronicle*/
  public Object getAssetIdToUTDMapLock(){
    return assetIdToUTDList;
  }

  /** for asset class chronicle*/
  public Object getAssetIdToACTDMapLock(){
    return assetIdToACTDList;
  }

  /** for carrier utilization chronicle*/
  public Object getAssetIdToCUTDMapLock(){
    return assetIdToCUTDList;
  }

  /** for asset flow chronicle*/
  public Object getAssetIdToAFTDMapLock(){
    return assetIdToAFTDList;
  }

  /** for asset flow chronicle*/
  public Object getAssetIdToADTDMapLock(){
    return assetIdToADTDList;
  }
  
  public Object getStructureLock(){
    return structurePool;
  }
  
  public Object getItineraryLock(){
    return itineraryPool;
  }

  public Object getTaskNodeLock(){
    return UUIDPool;
  }
  
  public Object getLookupLock(){
    return byUnit;
  }
  
  public Iterator getItineraryIterator(){
    return itineraryPool.iterator();
  }
  
  public Iterator getTaskNodeIterator(){
    return UUIDPool.values().iterator();
  }
  
  public Iterator getStructureIterator(){
    Collection coll = structurePool.values();
    //Debug.out("SServerPlanElementProvider:gSI size = " + 
    //coll.size() + " " + count);
    return structurePool.values().iterator();
  }
}
