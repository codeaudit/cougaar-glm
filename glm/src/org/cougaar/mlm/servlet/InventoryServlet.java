/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.mlm.servlet;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.ClassNotFoundException;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.LoggingService;

import org.cougaar.glm.ldm.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.policy.*;
import org.cougaar.mlm.ui.data.UIInventoryImpl;
import org.cougaar.mlm.ui.data.UIQuantityScheduleElement;
import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.mlm.ui.data.UISimpleNamedScheduleNames;

public class InventoryServlet
  extends HttpServlet 
{

  private SimpleServletSupport support;
  private AlarmService         alarmService;
  private LoggingService       logger;


  public void setSimpleServletSupport(SimpleServletSupport support) {
    this.support = support;
  }

  public void setLoggingService(LoggingService loggingService) {
    this.logger = loggingService;
  }

  public void setAlarmService(AlarmService anAlarmService) {
    this.alarmService = anAlarmService;
  }

  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "InventoryGetter" context per request
    InventoryGetter ig = new InventoryGetter(support,alarmService,logger);
    ig.execute(request, response);  
  }

  public void doPut(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "InventoryGetter" context per request
    InventoryGetter ig = new InventoryGetter(support,alarmService,logger);
    try {
      //System.out.println("\n\n\n\n\n\n********* BEGIN PUT");
    ig.execute(request, response);
    //System.out.println("\n\n\n\n\n\n\n****** DID PUT");
    } catch (Exception e) {
      //System.out.println("\n\n\n\n\n********* FAILED PUT!!  Exception: "+e);
      e.printStackTrace();
    }
  }
  
  /**
   * This inner class does all the work.
   * <p>
   * A new class is created per request, to keep all the
   * instance fields separate.  If there was only one
   * instance then multiple simultaneous requests would
   * corrupt the instance fields (e.g. the "out" stream).
   * <p>
   * This acts as a <b>context</b> per request.
   */
  private static class InventoryGetter {
    
    private String myID;
    public String desiredAssetName = "";
    ServletOutputStream out;
    
    /* since "InventoryGetter" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that InventoryGetter only uses
     * the "support" from the outer class.
     */    
    SimpleServletSupport support;
    AlarmService         alarmService;
    LoggingService       logger;
    
    
    final public static String ASSET = "ASSET";
    final public static String ASSET_AND_CLASSTYPE = ASSET + ":" + "CLASS_TYPE:";
    
    public InventoryGetter(SimpleServletSupport aSupport,
			   AlarmService         anAlarmService,
			   LoggingService        aLoggingService) {
      this.support = aSupport;
      this.alarmService = anAlarmService;
      this.logger = aLoggingService;
    }
    
    /*
      Called when a request is received from a client.
      Either gets the command ASSET to return the names of all the assets
      that contain a ScheduledContentPG or
      gets the name of the asset to plot from the client request.
    */
    public void execute( 
			HttpServletRequest req, 
			HttpServletResponse res) throws IOException
    {
      
      this.out = res.getOutputStream();
      
      //this.out = response.getWriter();
      /** The query data is one of:
	  ASSET -- meaning return list of assets
	  nomenclature:type id -- return asset matching nomenclature & type id
	  UID: -- return asset with matching UID
	  
	  desiredAssetName = "";
	  
	  if (query_parameters.hasBody()) {
	  desiredAssetName = query_parameters.getBodyAsString();
	  desiredAssetName = desiredAssetName.trim();
	  System.out.println("POST DATA: " + desiredAssetName);
	  } else {
	  System.out.println("WARNING: No asset to plot");
	  return;
	  }
      */
      
      int len = req.getContentLength();
      if (len > 0) {
	System.out.println("READ from content-length["+len+"]");
	InputStream in = req.getInputStream();
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        desiredAssetName = bin.readLine();
        bin.close();
	desiredAssetName = desiredAssetName.trim();
	System.out.println("POST DATA: " + desiredAssetName);
      } else {
	System.out.println("WARNING: No asset to plot");
	return;
      }
      
      // return list of asset names
      if (desiredAssetName.equals(ASSET)||
	  desiredAssetName.startsWith(ASSET_AND_CLASSTYPE)) {
	
	DemandObjectPredicate assetNamePredicate;
	//AssetPredicate assetNamePredicate;
	
	if(desiredAssetName.startsWith(ASSET_AND_CLASSTYPE)) {
	  String desiredClassType = desiredAssetName.substring(ASSET_AND_CLASSTYPE.length());
	  assetNamePredicate = new DemandObjectPredicate(desiredClassType);
	  //assetNamePredicate = new AssetPredicate(desiredClassType);
	}
	else {
	  assetNamePredicate = new DemandObjectPredicate();
	  //assetNamePredicate = new AssetPredicate();
	}
	
	// Asset no demand type handling
	/***
	 **
	 *
	 
	 Vector assetNames = new Vector();
	 Subscription subscription = 
	 psc.getServerPluginSupport().subscribe(this, assetNamePredicate);
	 
	 Collection container = 
	 ((CollectionSubscription)subscription).getCollection();
	 for (Iterator i = container.iterator(); i.hasNext(); ) {
	 GLMAsset asset = (GLMAsset)(i.next());
	 TypeIdentificationPG typeIdPG = 
	 asset.getScheduledContentPG().getAsset().getTypeIdentificationPG();
	 String nomenclature = typeIdPG.getNomenclature();
	 String typeId = typeIdPG.getTypeIdentification();
	 if (nomenclature != null)
	 nomenclature = nomenclature + ":" + typeId;
	 else
	 nomenclature = typeId;
	 assetNames.addElement(nomenclature);
	 }
	 
	 ***
	 * MWD fix and try this out -below
	 * MWD get rid of old commented out above replaced by below
	 * to get demand even where no inventories.
	 ****/
	
	HashSet assetNamesSet = new HashSet();
	Collection container = support.queryBlackboard(assetNamePredicate);
	
	/*	Subscription subscription = 
		psc.getServerPluginSupport().subscribe(this, assetNamePredicate);
		
		Collection container = 
		((CollectionSubscription)subscription).getCollection();
	*/	
	
	for (Iterator i = container.iterator(); i.hasNext(); ) {
	  Asset asset = ((Task)(i.next())).getDirectObject();
	  TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
	  String nomenclature = typeIdPG.getNomenclature();
	  String typeId = typeIdPG.getTypeIdentification();
	  if (nomenclature != null)
	    nomenclature = nomenclature + ":" + typeId;
	  else
	    nomenclature = typeId;
	  assetNamesSet.add(nomenclature);
	}
	
	Vector assetNames = new Vector(assetNamesSet);
	
	// unsubscribe, don't need this subscription any more
	//psc.getServerPluginSupport().unsubscribeForSubscriber(subscription);
	// send the results
	ObjectOutputStream p = new ObjectOutputStream(out);
	p.writeObject(assetNames);
	System.out.println("Sent asset names");
	return;
      } // end returning list of asset names
      
      if (desiredAssetName.startsWith("UID:")) {
	String desiredAssetUID = desiredAssetName.substring(4);
	Collection collection = support.queryBlackboard(new AssetUIDPredicate(desiredAssetUID));
      
	/*Subscription subscription = 
	  psc.getServerPluginSupport().subscribe(this, new AssetUIDPredicate(desiredAssetUID));
	  Collection collection = 
	  ((CollectionSubscription)subscription).getCollection();
	*/
      
	for (Iterator i = collection.iterator(); i.hasNext(); ) {
	  GLMAsset asset = (GLMAsset)(i.next());
	  TypeIdentificationPG typeIdPG = 
	    asset.getScheduledContentPG().getAsset().getTypeIdentificationPG();
	  String nomenclature = typeIdPG.getNomenclature();
	  String typeId = typeIdPG.getTypeIdentification();
	  if (nomenclature == null)
	    return;
	  desiredAssetName = nomenclature + ":" + typeId;
	}
	// unsubscribe, don't need this subscription any more
	//psc.getServerPluginSupport().unsubscribeForSubscriber(subscription);
      } // end getting asset name from UID
      
      Date startDay=getStartDate();

      // get roles and determine if this cluster is a provider (or consumer)
      //System.out.println("\n****** look for roles for agent \""+support.getEncodedAgentName()+"\"");
      RolePredicate rolePred = new RolePredicate(support.getEncodedAgentName());
      Collection roleCollection = support.queryBlackboard(rolePred);
    
      /*Subscription roleSubscription =
	psc.getServerPluginSupport().subscribe(this, 
	new RolePredicate(psc.getServerPluginSupport().getClusterIDAsString()));
	Collection roleCollection =
	((CollectionSubscription)roleSubscription).getCollection();
      */    
      boolean provider = false;
      if (!roleCollection.isEmpty()) {
      
	//Organization asset = (Organization) ((CollectionSubscription)roleSubscription).first();
	//CollectionSubscription collectsub = new CollectionSubscription(rolePred);
	//Organization asset = (Organization) collectsub.first();
	Organization asset = (Organization) roleCollection.iterator().next();
      
	Collection roles = asset.getOrganizationPG().getRoles();
        if (roles != null) {
	  Iterator i = roles.iterator();
	  while (i.hasNext()) {
	    Role role = (Role)i.next();
	    if (role.getName().endsWith("Provider")) {
	      provider = true;
	      break;
	    }
	  }
	}	
      }

      //psc.getServerPluginSupport().unsubscribeForSubscriber(roleSubscription);
    
      // get asset and tasks we need to create the inventory
    
      InventoryPredicate inventoryPredicate = new InventoryPredicate(desiredAssetName, support.getEncodedAgentName());
      Collection collection = support.queryBlackboard(inventoryPredicate);
    
      /*
	InventoryPredicate inventoryPredicate = 
	new InventoryPredicate(desiredAssetName, 
	psc.getServerPluginSupport().getClusterIDAsString());
	Subscription subscription = 
	psc.getServerPluginSupport().subscribe(this, inventoryPredicate);
	Collection collection = 
	((CollectionSubscription)subscription).getCollection();
      */
    
      if (collection.isEmpty()) {
        //System.out.println("\n\n\n\n\n\n\n ************* collection is empty; return no response!");
	return;
      }
    
      // create UIInventory data object from the log plan objects
      UIInventoryImpl inventory = getInventoryFromLogPlan(collection);
    
      // unsubscribe, don't need this subscription any more
      //psc.getServerPluginSupport().unsubscribeForSubscriber(subscription);
    
      // set values in UISimpleInventory, a serializable object
      UISimpleInventory simpleInventory = 
	getInventoryForClient(inventory, provider, startDay);

      // send the UISimpleInventory object
      if (simpleInventory != null) {
	ObjectOutputStream p = new ObjectOutputStream(out);
	//System.out.println("\n\n\n\n sending back a non-null inventory:\n"+simpleInventory);
	p.writeObject(simpleInventory);
	System.out.println("Sent XML document");
      } else {
        //System.out.println("\n\n\n\n  simple-inventory is null!!!  return no response");
      }
    }
    
    /** Called after submitting a subscription; the container
	contains the asset and tasks we need to compute the inventory
	object that we'll return to the client.
    */
    
    private UIInventoryImpl getInventoryFromLogPlan(Collection container) {
      UIInventoryImpl inventory = new UIInventoryImpl();
      Allocation lastAllocation=null;
      Object o=null;
      Iterator i=null;
      
      //first set the asset
      for (i = container.iterator(); i.hasNext(); ) {
	o = i.next();
	if (o instanceof Asset) {
	  inventory.setAsset((Asset)o);
	}
      }
      //next do the allocation
      for (i = container.iterator(); i.hasNext(); ) {
	o = i.next();
	if (o instanceof Allocation) {
	  lastAllocation = (Allocation) o;
	  inventory.addDueInSchedule((Allocation)o);
	  inventory.addRequestedDueInSchedule((Allocation)o);
	}
      }
      //MWD new code if there are no GLMAsset Inventories it's likely
      //the Asset has not been set on this UIInventoryImpl.
      //There are just allocations in this InventoryPredicate returned
      //collection and hence if (o instanceof Asset) has been used.
      if((lastAllocation != null) &&
	 (inventory.getAsset() == null)) {
	Asset directAsset = lastAllocation.getTask().getDirectObject();
	inventory.setAsset(directAsset);	    
      }
      
      return inventory;
    }
    
    /** Called to make a UISimpleInventory object which gets serialized
	and sent to the client.  Note that at the client end, the schedule
	type must be in org.cougaar.planning.ldm.plan.ScheduleType, so we fix any bogus
	schedules here and issue warnings.
    */
    
    private UISimpleInventory getInventoryForClient(UIInventoryImpl inventory,
						    boolean provider,
						    Date startingCDay) {
      UISimpleInventory inv = new UISimpleInventory();
      
      inv.setAssetName(inventory.getAssetName());
      inv.setUnitType(inventory.getUnitType());
      inv.setProvider(provider); // ui uses this to determine chart labels
      String scheduleType = inventory.getScheduleType();
      if (scheduleType.equals(ScheduleType.OTHER)) {
	scheduleType = PlanScheduleType.TOTAL_INVENTORY;
	System.out.println("WARNING: Treating Other schedule as total inventory");
      }
      inv.setScheduleType(scheduleType);
      inv.setBaseCDay(startingCDay);

      Date alpNow = new Date(alarmService.currentTimeMillis());

      logger.debug("Setting Alp now to " + alpNow);

      inv.setAlpNow(alarmService.currentTimeMillis());
      
      if (scheduleType.equals(PlanScheduleType.TOTAL_CAPACITY)) {
	inv.addNamedSchedule(UISimpleNamedScheduleNames.ALLOCATED,   inventory.getDueOutLaborSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.TOTAL_LABOR, inventory.getLaborSchedule());
      } else if (scheduleType.equals(PlanScheduleType.ACTUAL_CAPACITY)) {
	inv.addNamedSchedule(UISimpleNamedScheduleNames.ALLOCATED, inventory.getDueOutLaborSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.ON_HAND,   inventory.getOnHandDailySchedule());
      } else if (scheduleType.equals(PlanScheduleType.TOTAL_INVENTORY)) {
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.ON_HAND,                               inventory.getOnHandDailySchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.ON_HAND_DETAILED,                      inventory.getOnHandDetailedSchedule());
	
	
	/*** MWD Remove
	 **  Generate a false detailed schedule
	 *
	 
	 if(inventory.getOnHandDetailedSchedule() != null) {
	 Vector lotsODetails = new Vector();
	 int divElement=5;
	 Vector onHand = inventory.getOnHandDailySchedule();
	 for(int i=0; i < onHand.size() ; i++) {
	 UIQuantityScheduleElement orig = (UIQuantityScheduleElement) onHand.elementAt(i);
	 long  myStart = orig.getStartTime();
	 long  myEnd = orig.getEndTime();
	 divElement = (int) i%7;
	 if(divElement==0) divElement=1;
	 long  addTime = (long) (myEnd - myStart)/divElement;
	 double  changeQty = 10;
	 for (int j=0; j < divElement; j++) {
	 changeQty = ((changeQty) * j);
	 double elQty = orig.getQuantity() + changeQty;
	 if(elQty != orig.getQuantity()) {
	 System.out.println("Different! I'd say orig: " + orig.getQuantity() + " elQty: " + elQty);
	 }
	 lotsODetails.add(new UIQuantityScheduleElement(myStart, myStart + addTime, elQty));
	 myStart+=addTime;
	 }
	 }
	 inv.addNamedSchedule(ON_HAND_DETAILED,lotsODetails);
	 }
		      
	 **
	 ***/
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.DUE_IN,                                inventory.getDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.UNCONFIRMED_DUE_IN,                    inventory.getUnconfirmedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REQUESTED_DUE_IN,                      inventory.getRequestedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_IN,                      inventory.getProjectedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_IN,            inventory.getProjectedRequestedDueInSchedule());
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.DUE_IN                      +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.UNCONFIRMED_DUE_IN          +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveUnconfirmedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REQUESTED_DUE_IN            +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveRequestedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_IN            +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveProjectedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_IN  +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveProjectedRequestedDueInSchedule());
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.DUE_OUT,                               inventory.getDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REQUESTED_DUE_OUT,                     inventory.getRequestedDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_OUT,                     inventory.getProjectedDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_OUT,           inventory.getProjectedRequestedDueOutSchedule());
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.DUE_OUT                     +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_OUT           +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveProjectedDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REQUESTED_DUE_OUT           +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveRequestedDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_OUT +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveProjectedRequestedDueOutSchedule());
	
	inventory.computeSimulatedProjectionSchedules();
      
	//inv.addNamedSchedule(ON_HAND,                               inventory.getOnHandMockSchedule());
	//inv.addNamedSchedule(PROJECTED_DUE_IN,                      inventory.getProjectedMockDueInSchedule());
	//inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN,            inventory.getProjectedRequestedMockDueInSchedule());
	//inv.addNamedSchedule(PROJECTED_DUE_OUT,                     inventory.getProjectedMockDueOutSchedule());
	//inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT,           inventory.getProjectedRequestedMockDueOutSchedule());
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.ON_HAND_MOCK_PERIOD,                    inventory.getOnHandMockSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_IN_MOCK_PERIOD,           inventory.getProjectedMockDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_IN_MOCK_PERIOD, inventory.getProjectedRequestedMockDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_OUT_MOCK_PERIOD,          inventory.getProjectedMockDueOutSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_OUT_MOCK_PERIOD,inventory.getProjectedRequestedMockDueOutSchedule());
	
	//inv.addNamedSchedule(ON_HAND_MOCK_PERIOD,                    inventory.getOnHandDailySchedule());
	//inv.addNamedSchedule(PROJECTED_DUE_IN_MOCK_PERIOD,           inventory.getProjectedDueInSchedule());
	//inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN_MOCK_PERIOD, inventory.getProjectedRequestedDueInSchedule());
	//inv.addNamedSchedule(PROJECTED_DUE_OUT_MOCK_PERIOD,          inventory.getProjectedDueOutSchedule());
	//inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT_MOCK_PERIOD,inventory.getProjectedRequestedDueOutSchedule());
	
	inv.addNamedSchedule(UISimpleNamedScheduleNames.GOAL_LEVEL,              inventory.getGoalLevelSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REORDER_LEVEL,           inventory.getReorderLevelSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.AVERAGE_DEMAND_LEVEL,    inventory.getAverageDemandSchedule());
      } 
      else if (scheduleType.equals(inventory.NO_INVENTORY_SCHEDULE_JUST_CONSUME)){
	inv.addNamedSchedule(UISimpleNamedScheduleNames.DUE_IN,                              inventory.getDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.UNCONFIRMED_DUE_IN,                  inventory.getUnconfirmedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REQUESTED_DUE_IN,                    inventory.getRequestedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_IN,                    inventory.getProjectedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_IN,          inventory.getProjectedRequestedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.DUE_IN                    +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_DUE_IN          +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveProjectedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.UNCONFIRMED_DUE_IN        +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveUnconfirmedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.REQUESTED_DUE_IN          +UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveRequestedDueInSchedule());
	inv.addNamedSchedule(UISimpleNamedScheduleNames.PROJECTED_REQUESTED_DUE_IN+UISimpleNamedScheduleNames.INACTIVE, inventory.getInactiveProjectedRequestedDueInSchedule());

	System.out.println("getInventoryForClient: returning schedule type: " + scheduleType);
	
      } 
      else {
	System.out.println("WARNING: Unsupportd schedule type: " + 
			   scheduleType);
	return null;
      }
      return inv;
    }
    
    protected Date getStartDate() {
      Date startingCDay=null;
      
      // get oplan
      
      Collection oplanCollection = support.queryBlackboard(oplanPredicate());
      
      /*Subscription oplanSubscription =
	psc.getServerPluginSupport().subscribe(this, oplanPredicate());
	Collection oplanCollection =
	((CollectionSubscription)oplanSubscription).getCollection();
      */    
      
      if (!(oplanCollection.isEmpty())) {
        Iterator iter = oplanCollection.iterator();
        Oplan plan = (Oplan) iter.next();
	//CollectionSubscription collectsub = new CollectionSubscription(oplanPredicate());
	//Oplan plan = (Oplan) collectsub.first();
	//Oplan plan = (Oplan) ((CollectionSubscription)oplanSubscription).first();
	startingCDay = plan.getCday();
	//psc.getServerPluginSupport().unsubscribeForSubscriber(oplanSubscription);     
      }
      return startingCDay;
    }
    
    public boolean returnsXML() {
      return true;
    }
    
    public boolean returnsHTML() {
      return false;
    }
    
    public String getDTD() {
      return "myDTD";
    }
    
    /* The UISubscriber interface.
       This PSP doesn't care if subscriptions change
       because it treats each request as a new request.
    */
    
    public void subscriptionChanged(Subscription subscription) {
    }
    
    private static UnaryPredicate oplanPredicate() {
      return new UnaryPredicate() {
	  public boolean execute(Object o) {
	    return (o instanceof Oplan);
	  }
	};
    }
  }
}
  
/** Get asset which represents this cluster.
   */
  
class RolePredicate implements UnaryPredicate {
  String myCluster;
    
  public RolePredicate(String myCluster) {
    this.myCluster = myCluster;
  }
    
  public boolean execute(Object o) {
    if (o instanceof Organization) {
      Organization asset = (Organization)o;
      String s = asset.getItemIdentificationPG().getNomenclature();
      if (s != null)
	if (s.equals(myCluster))
	  return true;
    }
    return false;
  }
    
}
  
/** Subscribes to objects that provide quantity on hand, due-ins, & due-outs
      for the specified asset.
      For quantity on hand, get assets with a ScheduledContentPG in which
      the asset.typeIdentificationPG.nomenclature matches the specified asset.
      The ScheduledContentPG schedule indicates the start and end dates and
      quantities.
      
      For due-outs, get allocations with assets with a ScheduledContentPG in which
      the asset.typeIdentificationPG.nomenclature matches the specified asset.
      The allocation's reportedResult aspectTypes and results encode
      the quantity and start date.
      PAS - get these from the inventory role schedule?
      
      For due-ins, get tasks with direct objects in which the
      asset.typeIdentificationPG.nomenclature is the desired asset.
      Get the allocations for these tasks, 
      the allocation's reportedResult encodes
      the quantity, and the start and end dates.
      (Actually obtained by getting the allocations and then checking
      the task in the allocation.)
  */
  
class InventoryPredicate implements UnaryPredicate {
  String desiredAssetName; // nomenclature:type id
  ClusterIdentifier myClusterId;
    
  public InventoryPredicate(String desiredAssetName, String myCluster) {
    this.desiredAssetName = desiredAssetName;
    myClusterId = new ClusterIdentifier(myCluster);
  }
    
  private boolean assetMatch(Asset asset) {
    TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    String nomenclature = typeIdPG.getNomenclature();
    String typeId = typeIdPG.getTypeIdentification();
    if (nomenclature == null)
      return false;
    nomenclature = nomenclature + ":" + typeId;
    return nomenclature.equals(desiredAssetName);
  }
    
  /** Get assets with scheduledContentPG such that
      scheduledContentPG.getAsset().getTypeIdentificationPG().getNomenclature 
      equals desiredAssetName and get Tasks from Allocations such that
      Task.getDirectObject.getTypeIdentificationPG().getNomenclature
      equals desiredAssetName and task.getVerb is SUPPLY and
      Allocation.getAsset is an organization asset.
      Also matches if asset uid is equal to desiredAssetName -- i.e.
      the client can pass in a UID instead of the asset name.
  */
    
  public boolean execute(Object o) {
    if (o instanceof GLMAsset) {
      // looking for Inventory Assets
      GLMAsset asset = (GLMAsset)o;
      ScheduledContentPG scheduledContentPG = asset.getScheduledContentPG();
      if (scheduledContentPG == null)
	return false;
      Asset a1 = scheduledContentPG.getAsset();
      if (a1 == null) {
	System.out.println("WARNING: no asset in scheduledContentPG");
	return false;
      }
      return assetMatch(a1);
    } else if (o instanceof Allocation) {
      // looking for due ins
      Allocation allocation = (Allocation)o;
      if (!(allocation.getAsset() instanceof Organization))
	return false;
      Task task = allocation.getTask();
      if (!((task.getVerb().equals(Constants.Verb.SUPPLY)) ||
	    (task.getVerb().equals(Constants.Verb.PROJECTSUPPLY))))
	return false;
      Object directObject = task.getDirectObject();
      if (directObject == null)
	return false;
      if (!(directObject instanceof Asset))
	return false;
      boolean aMatch = assetMatch((Asset)directObject);
      /** MWD Debug
	  if(aMatch) {
	  System.out.println("PSP_Inventory::InventoryPredicate:Matching allocations task is with Verb: " + task.getVerb());
	  }
      */
      return aMatch;
    }
    return false; 
  }
}
  
class AssetPredicate implements UnaryPredicate {
    
  private String supplyType;
    
  public AssetPredicate() {
    super();
    supplyType = null;
  }
    
  public AssetPredicate(String theSupplyType) {
    super();
    supplyType = theSupplyType;
  }
    
  public boolean execute(Object o) {
    if (!(o instanceof GLMAsset))
      return false;
    GLMAsset asset = (GLMAsset)o;
    ScheduledContentPG scheduledContentPG = asset.getScheduledContentPG();
    if (scheduledContentPG == null)
      return false;
    Asset a1 = scheduledContentPG.getAsset();
    if (a1 == null) {
      System.out.println("WARNING: no asset in scheduledContentPG");
      return false;
    }
    TypeIdentificationPG typeIdPG = a1.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    //If we care about supply type make sure direct object matches supply type
    if (supplyType != null) {
      SupplyClassPG pg = (SupplyClassPG)a1.searchForPropertyGroup(SupplyClassPG.class);
      if ((pg == null) ||
	  (!(supplyType.equals(pg.getSupplyType())))){
	return false;
      }
      /***
	  if (pg == null) {
	  System.out.println("WARNING: Null Supply type");
	  return false;
	  }
	  else if (!(supplyType.equals(pg.getSupplyType()))){
	  System.out.println("WARNING: The Supply type is: " + pg.getSupplyType());
	  return false;
	  }
	  System.out.println("NO WARNING: SUCCESS got Asset of right type");
      ***/
    }
    return true;
  }
}
  
class DemandObjectPredicate implements UnaryPredicate {
    
  private String supplyType;
    
  public DemandObjectPredicate() {
    super();
    supplyType = null;
  }
    
  public DemandObjectPredicate(String theSupplyType) {
    super();
    supplyType = theSupplyType;
  }
    
  public boolean execute(Object o) {
    if (!(o instanceof Task))
      return false;
    Task task = (Task)o;
    if(!((task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) ||
	 (task.getVerb().equals(Constants.Verb.SUPPLY)))) 
      return false;
    Asset asset = task.getDirectObject();
    if (asset == null)
      return false;
    TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    //If we care about supply type make sure direct object matches supply type
    if (supplyType != null) {
      SupplyClassPG pg = (SupplyClassPG)asset.searchForPropertyGroup(SupplyClassPG.class);
      if ((pg == null) ||
	  (!(supplyType.equals(pg.getSupplyType())))){
	return false;
      }
    }
    return true;
  }
}
  
class AssetUIDPredicate implements UnaryPredicate {
  String desiredAssetUID;
    
  public AssetUIDPredicate(String desiredAssetUID) {
    this.desiredAssetUID = desiredAssetUID;
  }
    
  public boolean execute(Object o) {
    if (!(o instanceof GLMAsset))
      return false;
    GLMAsset asset = (GLMAsset)o;
    if (asset.getUID() == null)
      return false;
    if (!asset.getUID().toString().equals(desiredAssetUID))
      return false;
    ScheduledContentPG scheduledContentPG = asset.getScheduledContentPG();
    if (scheduledContentPG == null)
      return false;
    Asset a1 = scheduledContentPG.getAsset();
    if (a1 == null) {
      System.out.println("WARNING: no asset in scheduledContentPG");
      return false;
    }
    TypeIdentificationPG typeIdPG = a1.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    return true;
  }
    
}




