/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.sample;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.*;import org.cougaar.glm.ldm.*;import org.cougaar.glm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.Oplan;

import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.measure.Duration;
import org.cougaar.planning.ldm.measure.Volume;
import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.ScheduleUtilities;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.plugin.Annotation;
import org.cougaar.core.plugin.util.AllocatorHelper;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.TimeSpan;
import java.util.*;

/**
 * Plugin to allocate Fuel and Ammunition Load and in theater Transport task.
 * At FSB, Load tasks are allocated to capacity object; Transport tasks are allocated
 * to FuelTransportProvider/AmmunitionTransportProvider aka MSB.
 * 
 * At MSB, Transport tasks are allocated to capacity object. Load tasks are unexpected
 *
 * No tasks receive failed allocations.
 * If capacity is exceded, a SupportRequest task is issued. The resulsts of the SupportRequest
 * task are not checked. The plugin assumes that the task is successfull and the available
 * capacity is increased accordingly
 *
 */

public class LoadAllocatorPlugin extends SimplePlugin {

  // Capacity assets to which tasks are allocated
  private LACapacity fuelHandlingCapacity = null;
  private LACapacity fuelTransportCapacity = null;
  private LACapacity ammoHandlingCapacity = null;
  private LACapacity ammoTransportCapacity = null;

  // Only one plan this year.  This will be a bug soon
  private Plan thePlan;

  // MSB or FSB? (MSB is a transporter)
  private boolean isTransporter = false;

  private IncrementalSubscription capacitySubs;
  private static UnaryPredicate capacityPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Capacity) {
          return true;
        }
        return false;
      }
  };

  // Oplan is needed for start date.
  // End date is wrongly assumed to be 181 days + start date.
  // End date should be gleened from OrgActivities
  private long oplanStartTime=0;
  private long oplanEndTime=0;
  private IncrementalSubscription oplanSubs;
  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Oplan) {
          return true;
        }
        return false;
      }
    };

  // Load tasks to be allocated
  private IncrementalSubscription transportTasks;
  private static UnaryPredicate transportTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task t = (Task) o;
	if (t.getVerb().equals(Constants.Verb.TRANSPORT)) {
	  if (AllocatorHelper.isOfType(t, Constants.Preposition.OFTYPE, "TheaterTransportation")) {
	    return true;
	  }
	}
      }
      return false;
    }
  };
    
  // Transport tasks to be allocated
  private IncrementalSubscription loadTasks;
  private static UnaryPredicate loadTasksPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task t = (Task) o;
          if (t.getVerb().equals(Constants.Verb.LOAD)) {
	    Object directObject = t.getDirectObject();
	    if ((directObject instanceof BulkPOL) || (directObject instanceof Ammunition)) {
              return true;
	    }
	  }
	}
        return false;
      }
    };

  // This organization/cluster
  // Used to find default values for capacities in CSSCapabilitiesPG
  private Organization thisOrg = null;
  private IncrementalSubscription thisOrgSubs;
  private static UnaryPredicate thisOrgPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if ( o instanceof Organization ) {
	return ((Organization) o).isSelf();
      }
      return false;
    }
  };


  private IncrementalSubscription supportRequestAllocationSubs;
  private static UnaryPredicate supportRequestAllocationPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Allocation) {
	  Task t = ((Allocation)o).getTask();
	  if (t.getVerb().equals(Constants.Verb.SUPPORTREQUEST)) {
	    return true;
	  }
        }
        return false;
      }
    };

  private Organization getRoleProvider(Role role, boolean selfRole) {
    // This method, like the rest of the plugin, needs to be fixed to support multiple oplans
    
    //getMatchingRelationships screens on other orgs role. We're looking at the
    //self orgs schedule so ... if we want to know if self org provides to other
    //an org, we need to screen on the role converse. Example 
    //self.getMatchingRelationship(STRATEGICTRANSPORTPROVIDER) returns the 
    //strat trans providers to the self org. 
    //get.getMatchingRelationship(STRATEGICTRANSPORTCUSTOMER) returns the
    //orgs that self supports as a strat trans provider
    if (selfRole) {
      role = role.getConverse();
    }
    
    RelationshipSchedule thisOrgSchedule = thisOrg.getRelationshipSchedule();

    Collection providers = 
      thisOrgSchedule.getMatchingRelationships(role,
                                               TimeSpan.MIN_VALUE,
                                               TimeSpan.MAX_VALUE);

    if (selfRole) {
      return (providers.size() > 0) ?
        thisOrg : null;
    } else if (providers.size() > 0) {
      Relationship relationship = (Relationship) providers.iterator().next();
      return (Organization) thisOrgSchedule.getOther(relationship);
    } else {
      return null;
    }
  }
      
  private boolean isMSB() {
    Organization fuelTP = getRoleProvider(Constants.Role.FUELTRANSPORTPROVIDER, 
                                          true);
    if (fuelTP != null) {
      return true;
    } else {
      Organization ammoTP = 
        getRoleProvider(Constants.Role.AMMUNITIONTRANSPORTPROVIDER, true);
      if (ammoTP != null) {
	return true;
      }
    }
    return false;
  }

  public void setupSubscriptions()
  {
    //System.out.println("In LoadAllocatorPlugin.setupSubscriptions");
    // Subscribe for transport tasks
    transportTasks = (IncrementalSubscription)subscribe(transportTasksPredicate);
    loadTasks = (IncrementalSubscription)subscribe(loadTasksPredicate);
    thisOrgSubs = (IncrementalSubscription)subscribe(thisOrgPredicate);
    oplanSubs = (IncrementalSubscription)subscribe(oplanPredicate);
    capacitySubs = (IncrementalSubscription)subscribe(capacityPredicate);
    supportRequestAllocationSubs = (IncrementalSubscription)subscribe(supportRequestAllocationPredicate);

    if (didRehydrate()) {

      for (Enumeration e = thisOrgSubs.elements();e.hasMoreElements();) {
	// only expecting one
	thisOrg = (Organization)e.nextElement();
	// Are we an MSB?
	isTransporter = isMSB();
	break;
      }

      // recreate the local variables
      // Can I assume that the elements of my subscriptions are available now?
      LACapacity capacities[] = new LACapacity[4]; // should only be 2
      int i = 0;
      for (Enumeration e = capacitySubs.elements();e.hasMoreElements();) {
	Capacity c = (Capacity)e.nextElement();
	String typeID = c.getTypeIdentificationPG().getTypeIdentification();
	if (CapacityType.FUELHANDLING.equals(typeID)) {
	    fuelHandlingCapacity = new LACapacity(c);
	    capacities[i++] = fuelHandlingCapacity;
	} else if (CapacityType.AMMUNITIONHANDLING.equals(typeID)) {
	  ammoHandlingCapacity = new LACapacity(c);
	  capacities[i++] = ammoHandlingCapacity;
	} else if (CapacityType.FUELTRANSPORTATION.equals(typeID)) {
	  fuelTransportCapacity = new LACapacity(c);
	  capacities[i++] = fuelTransportCapacity;
	} if (CapacityType.AMMUNITIONTRANSPORTATION.equals(typeID)) {
	  ammoTransportCapacity = new LACapacity(c);
	  capacities[i++] = ammoHandlingCapacity;
	}
      }
      
      for (Enumeration e = supportRequestAllocationSubs.elements(); e.hasMoreElements();) {
	Allocation a = (Allocation)e.nextElement();
	Task t = a.getTask();
	MyAnnotation annot = (MyAnnotation)t.getAnnotation();
	if (annot !=null) {
	  UID uid = annot.getCapacityUID();
	  if (uid != null) {
	    for (int j=0; i<i; j++) {
	      if (uid.equals(capacities[j].getCapacity().getUID())) {
		capacities[j].setSupportRequest(t);
		capacities[j].setSupportRequestAllocation(a);
		capacities[j].setQSE(annot.getQSE());
	      }
	    }
	  }
	}
      }
    }
  }

  public void execute() 
  {
    //System.out.println(getCluster().getMessageAddress() + " In LoadAllocatorPlugin.execute");

    for (Enumeration e = thisOrgSubs.getAddedList();e.hasMoreElements();) {
      // only expecting one
      thisOrg = (Organization)e.nextElement();
      // Are we an MSB?
      isTransporter = isMSB();
//            	    System.out.println(getCluster().getMessageAddress() 
//            			       + " LoadAllocator got self "
//            			       + thisOrg 
//          			       + " isTransporter " + isTransporter);
      break;
    }


    // oplan is reset every time execute() runs
    oplanStartTime = TimeSpan.MAX_VALUE;
    for (Enumeration e = oplanSubs.elements(); e.hasMoreElements(); ) {
      Oplan oplan = (Oplan) e.nextElement();
      oplanStartTime = Math.min(oplanStartTime, oplan.getCday().getTime());
    }
    // We can't do anything without the oplan
    if (oplanStartTime == TimeSpan.MAX_VALUE) return;

    // change this: hard code end date for now
    oplanEndTime = oplanStartTime + (long)(181 *  ScheduleUtilities.millisperday);
	
    // If we get to this point, we have the Oplan times and can create
    // capacity objects. (No Oplan, no start or end dates)
	
    // Create Transport Capacities at MSB
    if (isTransporter) {
      if (fuelTransportCapacity == null) {
        Capacity cap = createCapacity(BulkPOL.class, Constants.Verb.TRANSPORT);
        if (cap != null) {
          fuelTransportCapacity = new LACapacity(cap);
          publishAdd(cap);
        }
      }
      if (ammoTransportCapacity == null) {
        Capacity cap = createCapacity(Ammunition.class, Constants.Verb.TRANSPORT);
        if (cap != null) {
          ammoTransportCapacity = new LACapacity(cap);
          publishAdd(cap);
        }
      }
    }
    // Create Handling Capacities at FSB
    else {
      if (fuelHandlingCapacity == null) {
        Capacity cap = createCapacity(BulkPOL.class, Constants.Verb.LOAD);
        if (cap != null) {
          fuelHandlingCapacity = new LACapacity(cap);
          publishAdd(cap);
        }
      }
      if (ammoHandlingCapacity == null) {
        Capacity cap = createCapacity(Ammunition.class, Constants.Verb.LOAD);
        if (cap != null) {
          ammoHandlingCapacity = new LACapacity(cap);
          publishAdd(cap);
        }
      }
    }


    // Load tasks
    for(Enumeration e = loadTasks.getAddedList();e.hasMoreElements();)  {
      Task task = (Task)e.nextElement();

      if (isTransporter) {
        System.out.println(getCluster().getMessageAddress() +
                           " LoadAllocator: Why do we have LOAD Tasks here? Isn't this the MSB?");
        break;
      }

      Asset asset = null;
      if (task.getDirectObject() instanceof BulkPOL) {
        asset = fuelHandlingCapacity.getCapacity();
        fuelHandlingCapacity.changed(true);
        //  		System.out.println(getCluster().getMessageAddress() + " LoadAllocator got Load fuel task");
      }
      else if (task.getDirectObject() instanceof Ammunition){
        asset = ammoHandlingCapacity.getCapacity();
        ammoHandlingCapacity.changed(true);
        //  		System.out.println(getCluster().getMessageAddress() + " LoadAllocator got Load ammo task");
      } else {
        System.err.println(getCluster().getMessageAddress() + " LoadAllocator got task of unexpected type :" 
                           + task);
        continue;
      }

      //  		System.out.println(getCluster().getMessageAddress() + "Creating AllocationResult for Load task");
      AllocationResult allocation_result = computeAllocationResult(task);
	  
      Allocation allocation = 
        theLDMF.createAllocation(task.getPlan(),
                                 task,
                                 asset,
                                 allocation_result,
                                 Constants.Role.HANDLER);
//          	    System.out.println(getCluster().getMessageAddress() + " Allocating Handle Task " /*+ task*/ + " to " + asset);
      publishAdd(allocation);
      thePlan = task.getPlan();
    }

    // Transport tasks
    for(Enumeration e = transportTasks.getAddedList();e.hasMoreElements();)  {
      Task task = (Task)e.nextElement();

      Role role = null;
      Asset asset = null;
      if (isTransporter) {
        // We are the MSB, allocate to capacity
        Object dobj = task.getDirectObject();
        if (dobj instanceof BulkPOL) {
          asset = fuelTransportCapacity.getCapacity();
          fuelTransportCapacity.changed(true);
	  role = Constants.Role.FUELTRANSPORTPROVIDER;
          //  		    System.out.println(getCluster().getMessageAddress() + " LoadAllocator got Transport fuel task");
        } else if (dobj  instanceof Ammunition){
          asset = ammoTransportCapacity.getCapacity();
          ammoTransportCapacity.changed(true);
          //  		    System.out.println(getCluster().getMessageAddress() + " LoadAllocator got Transport ammo task");
        } else if (dobj instanceof AggregateAsset){
          Object o = ((AggregateAsset)task.getDirectObject()).getAsset();
          if (o instanceof Ammunition) {
            asset = ammoTransportCapacity.getCapacity();
            ammoTransportCapacity.changed(true);
          } else if (o instanceof BulkPOL) {
            asset = fuelTransportCapacity.getCapacity();
            fuelTransportCapacity.changed(true);
          } else{
	    // we don't deal with whatever ends up here
	    continue;
            //System.err.println(getCluster().getMessageAddress() + " LoadAllocator asset of unknown type: " + dobj);
          }
        }
      } 
      // forward from FSB to MSB
      else 
	// Bad! This assumes that either one will do.
        asset = getRoleProvider(Constants.Role.FUELTRANSPORTPROVIDER, false);
      if (asset == null) 
	// try again
        asset = getRoleProvider(Constants.Role.AMMUNITIONTRANSPORTPROVIDER, false);

      if (asset == null) {
        System.err.println(getCluster().getMessageAddress() + " LoadAllocator - no MSB, can't forward the Transport task");
      }
      else {
        // create and publish the allocation
        //  		System.out.println(getCluster().getMessageAddress() + "Creating AllocationResult for Transport task");
        AllocationResult allocation_result = computeAllocationResult(task);
	  
        Allocation allocation = 
          theLDMF.createAllocation(task.getPlan(),
                                   task,
                                   asset,
                                   allocation_result,
                                   Constants.Role.TRANSPORTER);
//    		System.out.println(getCluster().getMessageAddress() + "Allocating Transport Task " /*+ task*/ + " to " + asset);
        publishAdd(allocation);
        thePlan = task.getPlan();
      }
    }



    // calculate capacity overruns
    // if overruns on a given day, ask for more capacity
    // assume we get new capacity, and add a QuantityScheduleElement to 
    // Capacity.ScheduleContentPG.Schedule with added capacity

    if ((fuelHandlingCapacity !=null) && fuelHandlingCapacity.changed()) {
      checkOverruns(fuelHandlingCapacity);
      fuelHandlingCapacity.changed(false);
    }
    if ((ammoHandlingCapacity!=null) && ammoHandlingCapacity.changed()) {
      checkOverruns(ammoHandlingCapacity);
      ammoHandlingCapacity.changed(false);
    }
    if ((fuelTransportCapacity!=null) && fuelTransportCapacity.changed()) {
      checkOverruns(fuelTransportCapacity);
      fuelTransportCapacity.changed(false) ;
    }
    if ((ammoTransportCapacity!=null) && ammoTransportCapacity.changed()) {
      checkOverruns(ammoTransportCapacity);
      ammoTransportCapacity.changed(false);
    }
  }

  // Return an allocation result that gives back a successful/optimistic answer
  // consisting of the best value for every aspect
  private AllocationResult computeAllocationResult(Task task) 
  {
    int num_prefs = 0;
    Enumeration prefs = task.getPreferences();
    while(prefs.hasMoreElements()) {prefs.nextElement(); num_prefs++; }

    int []types = new int[num_prefs];
    double []results = new double[num_prefs];
    prefs = task.getPreferences();

    int index = 0;
    while(prefs.hasMoreElements()) {
      Preference pref = (Preference)prefs.nextElement();
      types[index] = pref.getAspectType();
      results[index] = pref.getScoringFunction().getBest().getValue();
      //      	    System.out.println("Types[" + index + "]= " + types[index] + 
      //        			       " Results[" + index + "]= " + results[index]);
      index++;
    }

    AllocationResult result = theLDMF.newAllocationResult(1.0, // Rating,
                                                          true, // Success,
                                                          types,
                                                          results);
    return result;
  }

  private Capacity createCapacity(Class proto_class, String verb){

    Asset asset = theLDMF.createPrototype(proto_class, "AssetPrototype");

    NewTypeIdentificationPG typeIdProp = null ;
    int unit = Volume.GALLONS;
    try {
      typeIdProp =
        (NewTypeIdentificationPG)theLDMF
        .createPropertyGroup( TypeIdentificationPGImpl.class );
      if (verb.equals(Constants.Verb.LOAD)) {
        if (asset instanceof BulkPOL){
          typeIdProp.setTypeIdentification(CapacityType.FUELHANDLING);
          typeIdProp.setNomenclature(CapacityType.FUELHANDLING);
          unit = Volume.GALLONS;
        }
        else if (asset instanceof Ammunition) {
          typeIdProp.setTypeIdentification(CapacityType.AMMUNITIONHANDLING);
          typeIdProp.setNomenclature(CapacityType.AMMUNITIONHANDLING);
          unit = Mass.TONS;
        }
        else {
          System.err.println(getCluster().getMessageAddress() + " LoadAllocator:createCapacity() got unexpected asset type :" + asset);
          return null;
        }
      }
      else if (verb.equals(Constants.Verb.TRANSPORT)) { 
        if (asset instanceof BulkPOL){
          typeIdProp.setTypeIdentification(CapacityType.FUELTRANSPORTATION);
          typeIdProp.setNomenclature(CapacityType.FUELTRANSPORTATION);
          unit = Volume.GALLONS;
        }
        else if (asset instanceof Ammunition) {
          typeIdProp.setTypeIdentification(CapacityType.AMMUNITIONTRANSPORTATION);
          typeIdProp.setNomenclature(CapacityType.AMMUNITIONTRANSPORTATION);
          unit = Mass.TONS;
        }
        else {
          System.err.println(getCluster().getMessageAddress() + " LoadAllocator:createCapacity() got unexpected asset type :" + asset);
          return null;
        }
      }
      else {
        System.err.println(getCluster().getMessageAddress() + " LoadAllocator:createCapacity() got unexpected verb :" + verb);
        return null;
      }
    } catch (Exception exc) {
      System.out.println(getCluster().getMessageAddress() + " LoadAllocator - problem creating a Capacity asset.");
      exc.printStackTrace();
    }

    asset.setTypeIdentificationPG(typeIdProp);
    //
    // Create a ScheduledContentPG
    //
    Vector cses = new Vector() ;
    // later - fix CapacityScheduleElement and use it instead
    NewQuantityScheduleElement cse = new QuantityScheduleElementImpl();
    // from org CSSCapability
    // e.g., FuelHandling Volume=105000gallons Duration=1days
    CSSCapabilityPG cssPG = thisOrg.getCSSCapabilityPG();
    CSSCapability css = null;
    double qty = 105000d;
    if (cssPG != null) {
      css = getCSSCapabilityType(cssPG, typeIdProp.getTypeIdentification());
    }
    if (css == null) {
      System.out.println(getCluster().getMessageAddress() + "LoadAllocatorPlugin  - No capacity for "
                         + typeIdProp.getTypeIdentification()
                         + " in CSSCapabilityType - using default value");

    } else
      qty = css.getCapacity().getQuantity().getValue(unit);
    //System.out.println(getCluster().getMessageAddress() + " LoadAllocator using " + qty +
    //                   "as capacity for " + typeIdProp.getTypeIdentification());
    cse.setQuantity(qty);

    /*cse.setQuantity(1050d);*/
    cse.setStartTime(oplanStartTime);
    cse.setEndTime(oplanEndTime);
    cses.addElement(cse);
    NewSchedule sched =  GLMFactory.newQuantitySchedule(cses.elements(),
                                                        PlanScheduleType.ACTUAL_CAPACITY);
    // Create a NewScheduledContentPG for the container.
    //
    NewScheduledContentPG nscp =
      (NewScheduledContentPG) new ScheduledContentPGImpl();
    nscp.setAsset(asset);
    nscp.setSchedule(sched);

    Capacity c;
    try {
      c =(org.cougaar.glm.ldm.asset.Capacity) theLDMF.createAsset(Capacity.class);
    } catch (Exception exc) {
      System.out.println(getCluster().getMessageAddress() + " LoadAllocator - problem creating a Capacity asset.");
      exc.printStackTrace();
      c = new Capacity();
    }

    c.setScheduledContentPG(nscp);


    c.setTypeIdentificationPG(typeIdProp);

    //
    // Set the availability on this asset.
    //
    Schedule ss = theLDMF.newSimpleSchedule( new Date(oplanStartTime),
                                             new Date (oplanEndTime));
    ((NewRoleSchedule)c.getRoleSchedule()).setAvailableSchedule( ss );

//      System.out.println(getCluster().getMessageAddress() + " LoadAllocator created Capacity Object "
//                         + c.getUID());
    return c;
  }    


  // Check each day to see if we have allocated more capacity than is available.
  // If so, modify the SupportRequest task to ask for more capacity earlier.
  // There is only one SupportRequest task per capacity. 
  // The results of the SupportRequest task are not checked. This Plugin just assumes
  // that it gets excactly the additional capacity it asks for.
  private void checkOverruns(LACapacity mycapacity) {

    //  	System.out.println(getCluster().getMessageAddress() + " LoadAllocator in checkOverruns");
    Capacity capacity = mycapacity.getCapacity();
    if (capacity == null) {
      // This should never happen
      System.err.println(getCluster().getMessageAddress() + " LoadAllocator:checkOverruns() no capacity object!");
      return;
    }
    RoleSchedule rs = capacity.getRoleSchedule();
    //	System.out.println(getCluster().getMessageAddress() + " LoadAllocator:checkOverruns() Capacity.RoleSchedule:" + rs);
    if (rs == null) {
      // This should never happen
      System.err.println(getCluster().getMessageAddress() + " LoadAllocator:checkOverruns() the capacity has no role schedule");
      return;
    }
    long currentDate = oplanStartTime;
    boolean publishChanges = false;
    boolean publishNew = false;
    Schedule capacitySchedule = capacity.getScheduledContentPG().getSchedule();
    if (capacitySchedule == null) {
      // This should never happen
      System.err.println(getCluster().getMessageAddress() + " LoadAllocator:checkOverruns() the capacity's ScheduleContentPG has no schedule");
      return;
    }

    // Check for overruns on each day
    while(currentDate < oplanEndTime)  {
      double qtyAsked = 0;
      double overrun = 0;
      double qtyAvailable = 0;
      // Find the elements that have allocations for current day
      // Assume that oplan started at midnight.
      Collection allocatedSet = rs.getOverlappingRoleSchedule(currentDate,
							      currentDate +
							   ScheduleUtilities.millisperday);
      if (allocatedSet.size() > 0) {
	//Find out how much we have allocated
        qtyAsked = rs.addAspectValues(allocatedSet, AspectType.QUANTITY);
	Collection capacitySet 
	    = capacitySchedule.getOverlappingScheduleElements( currentDate,
							       currentDate + 
							       ScheduleUtilities.millisperday);
	// Find out how much capacity we actually have
	qtyAvailable = ScheduleUtilities.sumElements(capacitySet);
	// Have we allocated more than we have?
        overrun = qtyAsked - qtyAvailable;

        //       		System.out.println(getCluster().getMessageAddress() 
        //        				   + " LoadAllocator:checkOverruns() overrun=" + overrun
        //       				   + "  qtyAsked=" + qtyAsked 
        //      				   + "  qtyAvaiable=" + qtyAvailable);
      } else {
        //        		System.out.println(getCluster().getMessageAddress() 
        //        				   + " LoadAllocator:checkOverruns() No schedule for " 
        //        				   + currentDate);
      }

      // more capacity allocated than available?
      if (overrun > 0){
	
        // create a new SupportRequest task there isn't one already
        if (mycapacity.getSupportRequest() == null) {
          publishNew = true;

          Task supportRequest = createSupportRequest(capacity, overrun,
                                                     currentDate, oplanEndTime);
          mycapacity.setSupportRequest(supportRequest);

          // add new qty schedule element to capcity 
          // this is the object that represents the optimistic assumption that the 
          // SupportRequest task is successful
          NewQuantityScheduleElement cse = new QuantityScheduleElementImpl();
          cse.setQuantity(overrun);
          cse.setStartTime(currentDate);
          // current date?
          cse.setEndTime(oplanEndTime);
          ((NewSchedule)capacitySchedule).addScheduleElement(cse);
          mycapacity.setQSE((QuantityScheduleElement)cse);

	  // Add the QSE to the annotation on the task
	  ((MyAnnotation)supportRequest.getAnnotation()).setQSE((QuantityScheduleElement)cse);

        } else {
          // reset the start date and quantity preferences of the SupportRequest task
          publishChanges = true;

          QuantityScheduleElement qse = mycapacity.getQSE();
          if (qse == null) {
            // Don't know why this would happen, but...
            System.err.println(getCluster().getMessageAddress() + " LoadAllocator QSE missing");
            NewQuantityScheduleElement cse = new QuantityScheduleElementImpl();
            cse.setQuantity(0d);
            cse.setStartTime(currentDate);
            cse.setEndTime(oplanEndTime);
            ((NewSchedule)capacitySchedule).addScheduleElement(cse);
            mycapacity.setQSE((QuantityScheduleElement)cse);

	    // Add the QSE to the annotation on the task
	    ((MyAnnotation)mycapacity.getSupportRequest().getAnnotation()).setQSE((QuantityScheduleElement)cse);
          }

          // this is null
          double new_qty = qse.getQuantity() +overrun;

          long newDate = qse.getStartDate().getTime();
          if (newDate > currentDate)  {
            ((NewQuantityScheduleElement)qse).setStartTime(currentDate);
            newDate = currentDate;
          }
          modifySupportRequest(mycapacity.getSupportRequest(), 
                               capacity, new_qty, newDate);


          // new quantity should always be more than old
          ((NewQuantityScheduleElement)qse).setQuantity(new_qty);


        }
		

      }
      currentDate += ScheduleUtilities.millisperday;
    }
    if (publishNew || publishChanges) {
      // send SupportRequest task to SupportForceProvider
      publishSupportRequestAllocation(mycapacity);
    }
    if (publishNew) {
      //  	    System.out.println(getCluster().getMessageAddress() + " LoadAllocator publishing changed capacity and new SupportRequest");
      publishChange(capacity);
      publishAdd(mycapacity.getSupportRequest());
      publishNew = false;
    } else if (publishChanges) {
      //  	    System.out.println(getCluster().getMessageAddress() + " LoadAllocator publishing changed capacity and SupportRequest");
      publishChange(capacity);
      publishChange(mycapacity.getSupportRequest());
      publishChanges = false;
    } 
  }

  private Task createSupportRequest(Capacity capacity, double qty,
                                    long startDate, long endDate ) {
    //System.out.println(getCluster().getMessageAddress() + " LoadAllocator in createSupportRequest");

    NewTask t = theLDMF.newTask();
    t.setPlan(/*thePlan*/theLDMF.getRealityPlan());
    t.setVerb(new Verb(Constants.Verb.SUPPORTREQUEST));
    Vector pps = new Vector();
    NewPrepositionalPhrase pp = theLDMF.newPrepositionalPhrase();
    pp.setPreposition(Constants.Preposition.FOR);
    pp.setIndirectObject(thisOrg.getItemIdentificationPG().getItemIdentification());
    pps.add(pp);
    pp = theLDMF.newPrepositionalPhrase();
    t.setPrepositionalPhrases(pps.elements());
	
    Vector prefs = new Vector();
    prefs.add(createDatePreference(AspectType.START_TIME, startDate, 1));
    prefs.add(createDatePreference(AspectType.END_TIME, endDate, 1));
	
    prefs.add(createTypedPreference(capacity, qty));
    t.setPreferences(prefs.elements());

    // mark the Task with the UID of the capacity so we can match them up
    // later in case we rehydrate
    t.setAnnotation(new MyAnnotation(capacity.getUID()));
    return t;
  }

  private void modifySupportRequest(Task supportRequest, Capacity capacity, 
                                    double qty, long newDate) {

    int num_prefs = 0;
    Enumeration prefs = supportRequest.getPreferences();
    Vector newPrefs = new Vector();

    while(prefs.hasMoreElements()) {
      Preference pref = (Preference)prefs.nextElement();
      switch (pref.getAspectType()) {
      case AspectType.START_TIME:
        newPrefs.add(createDatePreference(AspectType.START_TIME, newDate, 1));
        break;
      case AspectType.TYPED_QUANTITY:
        newPrefs.add(createTypedPreference(capacity, qty));
        break;
      default:
        newPrefs.add(pref);
      }
    }
  }

  private Preference createDatePreference(int timeAspectType, long date, 
                                          int scoringFunction) {
    AspectValue timeAV = TimeAspectValue.create(timeAspectType, date);
    ScoringFunction timeSF = ScoringFunction.createPreferredAtValue(timeAV, scoringFunction);
    return theLDMF.newPreference(timeAspectType, timeSF);
  }

  /**
   *  Given CSSCapabilityPG, returns the CSSCapability that matches the given type
   */
  private static CSSCapability getCSSCapabilityType(CSSCapabilityPG cssCapPG, String type){
    Iterator theCaps = cssCapPG.getCapabilities().iterator();
    CSSCapability theCap = null;
    while (theCaps.hasNext()) {
      theCap = (CSSCapability)theCaps.next();
      if (theCap.getType().equals(type))
        return theCap;
    } 
    return null;
	
  }

  private Preference createTypedPreference(Capacity capacity, double qty) {
    NewCSSCapabilityPG cssCap =  
      (NewCSSCapabilityPG) theLDMF.createPropertyGroup(CSSCapabilityPGImpl.class);
    NewAssignedPG assignedPG = 
      (NewAssignedPG) theLDMF.createPropertyGroup(AssignedPGImpl.class);

    org.cougaar.planning.ldm.measure.Capacity measureCapacity;
    CSSCapability theCapability;
    Organization proto = (Organization)theLDMF.createPrototype(Organization.class, "OrgPrototype");
    Role role;

    if (capacity.getScheduledContentPG().getAsset() instanceof BulkPOL) {
      Volume myScalar = new Volume(qty ,Volume.GALLONS);
      measureCapacity = new org.cougaar.planning.ldm.measure.Capacity(myScalar, new Duration(1, Duration.DAYS));
      if (capacity.getTypeIdentificationPG().getTypeIdentification().equals(CapacityType.FUELTRANSPORTATION)) {
        role = Constants.Role.FUELTRANSPORTPROVIDER;
        theCapability= new CSSCapability(CapacityType.FUELTRANSPORTATION, measureCapacity);
      }
      else {
        role = Constants.Role.FUELHANDLINGPROVIDER;
        theCapability = new CSSCapability(CapacityType.FUELHANDLING, measureCapacity);
      }
    }
    else {
      Mass myScalar = new Mass(qty ,Mass.TONS);
      measureCapacity = new org.cougaar.planning.ldm.measure.Capacity(myScalar, new Duration(1, Duration.DAYS));
      if (capacity.getTypeIdentificationPG().getTypeIdentification().equals(CapacityType.AMMUNITIONTRANSPORTATION)) {
        role = Constants.Role.AMMUNITIONTRANSPORTPROVIDER;
        theCapability= new CSSCapability(CapacityType.AMMUNITIONTRANSPORTATION, measureCapacity);
      }
      else {
        role = Constants.Role.AMMUNITIONHANDLINGPROVIDER;
        theCapability = new CSSCapability(CapacityType.AMMUNITIONHANDLING, measureCapacity);
      }

    }

    ArrayList al = new ArrayList(1);
    al.add(theCapability);
    cssCap.setCapabilities(al);
    proto.setPropertyGroup(cssCap);

    al.clear();
    al.add(role);
    assignedPG.setRoles(al);
    proto.setAssignedPG(assignedPG);
	
    TypedQuantityAspectValue tav = new TypedQuantityAspectValue(proto,  qty);
    ScoringFunction sf = ScoringFunction.createPreferredAtValue(tav, 0.5);
    return theLDMF.newPreference(AspectType.TYPED_QUANTITY,sf);
  }

  /** Publish new or changed Allocation of SupportRequest task*/
  private void publishSupportRequestAllocation(LACapacity mycapacity) {
    Organization supportForceProvider = getRoleProvider(Constants.Role.SUPPORTFORCEPROVIDER, false);
    // Can't forward the task if there is no place to send it.
    if (supportForceProvider == null) {
      System.err.println(getCluster().getMessageAddress() + " LoadAllocator:publishSupportRequestAllocation - No SupportForceProvider!");
      return;
    }

    AllocationResult allocation_result = computeAllocationResult(mycapacity.getSupportRequest());
    Allocation allocation = mycapacity.getSupportRequestAllocation();
    if ( allocation == null) {
      allocation = 
        theLDMF.createAllocation(theLDMF.getRealityPlan(),
                                 mycapacity.getSupportRequest(),
                                 supportForceProvider,
                                 allocation_result,
                                 Constants.Role.TRANSPORTER);
      mycapacity.setSupportRequestAllocation(allocation);
      publishAdd(allocation);
      //  	    System.out.println(getCluster().getMessageAddress() + " LoadAllocator Publishing new SupportRequest allocation " + allocation);
    } else {
      allocation.setEstimatedResult(allocation_result);
      publishChange(allocation);
      //  	    System.out.println(getCluster().getMessageAddress() + " LoadAllocator publishChange SupportRequest allocation " + allocation);
    }
  }

  // Inner class that contains a Capacity asset, a SupportRequest task to increase capacity,
  // a SupportRequest allocation, and a dirty flag.
  private static class LACapacity {
    private boolean changed = false;
    private Capacity capacity = null;
    private Task supportRequest = null;
    private QuantityScheduleElement qse = null;
    private Allocation supportRequestAllocation = null;
    public LACapacity(Capacity capacity) { this.capacity = capacity;}
    public Capacity getCapacity() {return capacity;}
    public void changed(boolean value) { changed = value;}
    public boolean changed() {return changed;}
    public void setSupportRequest(Task sr) {supportRequest = sr;}
    public Task getSupportRequest() {return supportRequest;}
    public QuantityScheduleElement getQSE() {return qse;}
    public void setQSE(QuantityScheduleElement element) {qse = element;}
    public void setSupportRequestAllocation(Allocation a) {supportRequestAllocation=a;}
    public Allocation getSupportRequestAllocation() {return supportRequestAllocation;}
  }


  private static class MyAnnotation implements Annotation {
    private UID capacityUID;
    private QuantityScheduleElement qse;
    MyAnnotation(UID capacityuid, QuantityScheduleElement qse) {
      capacityUID = capacityuid;
      this.qse = qse;
    }
    MyAnnotation(UID capacityuid) {capacityUID = capacityuid;}
    public UID getCapacityUID() {return capacityUID;}
    public void setQSE(QuantityScheduleElement qse) {this.qse = qse;}
    public QuantityScheduleElement getQSE() {return qse;}
  }
}
