/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.CargoVehicle;
import org.cougaar.glm.ldm.policy.ShipPolicy;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

public class MCCAllocatorPlugin extends SimplePlugin
{
	 

    // Convenience Class for handling of pairs of Assets/AllocationResults
    static class AssetTuple
    {
	private Asset myAsset;
	private AllocationResult myAR;

	public AssetTuple( Asset asset, AllocationResult ar ) {
	    this.myAsset = asset;
	    this.myAR = ar;
	}

	public Asset getAsset() { return myAsset; }
	public AllocationResult getAllocationResult() { return myAR; }
    }



    private IncrementalSubscription transportAssets;
    private IncrementalSubscription policies;
    private IncrementalSubscription myTasks;

    private Vector waitingTasks = new Vector();
	
    private static long ONE_DAY = 86400000L;

    
    // Predicate for our transportation Assets (ships, HETS, and the like)...
    private static UnaryPredicate assetPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  return ( o instanceof CargoVehicle );
	}
      };
    }
    
    // Predicate for this Cluster's Policies
    private static UnaryPredicate policyPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  return ( o instanceof ShipPolicy );
	}
      };
    }

    // Predicate for _ALL_ TRANSPORTMISSION Tasks
    private static UnaryPredicate taskPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  if ( o instanceof Task ) {
	    Task t = (Task)o;
	    if ( t.getVerb().equals( Constants.Verb.TRANSPORTATIONMISSION ))
	      return true;
	  }
	  return false;
	}
      };
    }

    /**
     * Set up the initial subscriptions of the Plugin; 
     * Currently interested in allocatable Workflows of 
     * Transport Tasks.
     */
    public void setupSubscriptions() {

	// Setup Subscription for Cluster transport Assets
	transportAssets = (IncrementalSubscription)subscribe( assetPred() );

	// Setup Subscription for Cluster's Policies
	policies = (IncrementalSubscription)subscribe( policyPred() );

	// Setup Subscription for Tasks to be allocated
	myTasks = (IncrementalSubscription)subscribe( taskPred() );

    }
    
    
    /**
     * Called when there are changes to our subscriptions
     */
    public void execute() {
	try {
	    // New Cluster Assets available
	    if ( transportAssets.hasChanged() ) {
		Enumeration newassets = transportAssets.getAddedList();
		checkNewAssets( newassets );
	    }
	    
	    // We have to be kind of careful here; We're maintaining a Container
	    // of _ALL_ Tasks (not just the expandable kind).  We have to check to
	    // make sure that the _NEW_ Tasks are actually expandable (BTW, we actually
	    // pass over this step and go straight to Allocation here; we can only do this
	    // because we are replacing a pass-through Expander - how's that for 
	    // deterministic?) - they should be, but one can never tell...
	    if ( myTasks.hasChanged() ) {
		Enumeration newTasks = myTasks.getAddedList();
		while ( newTasks.hasMoreElements() ) {
		    MPTask newTask = (MPTask)newTasks.nextElement();
		    //if (( newTask.getWorkflow() == null ) &&
		    //	( newTask.getPlanElement() == null ))
		    allocate( newTask );
		}
		Enumeration changedTasks = myTasks.getChangedList();
		while ( changedTasks.hasMoreElements() ) {
		    MPTask changedTask = (MPTask)changedTasks.nextElement();
		    allocate( changedTask );
		}
	    }

	} catch ( RuntimeException pe ) {
	    pe.printStackTrace();
	}
    }

    private void checkNewAssets( Enumeration newassets ) {
	//while ( newassets.hasMoreElements() ) {
	//CargoVehicle cv = (CargoVehicle)newassets.nextElement();
	// check to see if we have any unallocated Tasks lying about...
	if (!(waitingTasks.isEmpty()) ) {
	    Vector newv = new Vector();
	    Vector tmp = waitingTasks;
	    waitingTasks = newv;
	    Enumeration e = tmp.elements();
	    while ( e.hasMoreElements() ) {
		MPTask t = (MPTask)e.nextElement();
		// No, no, no...need to be a bit more discerning here;
		// Want to employ some scheduling here to match wf with cv,
		// albeit some rather stupid scheduling...
		AssetTuple at = chooseAsset( newassets, t );
		createTheAllocation( t, at );
	    }
	}    
    }
	

    private void createTheAllocation( MPTask t, AssetTuple at )  {
	// make the allocation
	if ( at != null ) {
	    if ( at.getAllocationResult().isSuccess() ) {
		Allocation alloc =
		  theLDMF.createAllocation(theLDMF.getRealityPlan(), 
					   t,
					   at.getAsset(),
					   at.getAllocationResult(),
					   Constants.Role.TRANSPORTER);
                /*
		System.err.println( "\nAllocation succeeded: " + t + "\n" + alloc ); 
                */
		publishAdd( alloc );
	    } else {
	    	// check for auxquery requests
	    	boolean reqfailreason = false;
	    	int[] aqr = t.getAuxiliaryQueryTypes();
	    	for (int q= 0; q < aqr.length; q++) {
	    		int checktype = aqr[q];
	    		if (checktype == AuxiliaryQueryType.FAILURE_REASON ) {
	    			reqfailreason = true;
	    		}
	    	}
	    	if (reqfailreason) {
       		// set a failure reason on the allocation result
        	at.getAllocationResult().addAuxiliaryQueryInfo(AuxiliaryQueryType.FAILURE_REASON, "No HETs available");
	    	}
		PlanElement falloc =
		  theLDMF.createFailedDisposition(theLDMF.getRealityPlan(),
						 t,
						 at.getAllocationResult());
                /*
		System.err.println( "\nAllocation failed: " + t + "\n" + falloc );
		if (reqfailreason) {
    	System.err.println("FAILURE REASON: " + falloc.getEstimatedResult().auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON) );
		}
                */
		publishAdd( falloc );
	    }
	} else
	    throw new RuntimeException( "No Assets to Allocate Against." );
    }
	 
    // If the Task already has an Allocation (PE) associated with it, we don't 
    // want to create a new Allocation, just modify the old one and mark it as
    // changed.
    private void alterTheAllocation( MPTask t, AssetTuple at ) {
	if ( at != null ) {
          PlanElement pe = t.getPlanElement();
	    if ( pe instanceof Allocation ) {
		// want to _alter_ original Allocation and mark as changed;
		Allocation alloc = (Allocation)t.getPlanElement();
		alloc.setEstimatedResult( at.getAllocationResult() );
		publishChange( alloc );
	    } else if ( pe instanceof Disposition ) {
              if (!((Disposition)pe).isSuccess()) {
		publishRemove( pe );
		createTheAllocation( t, at );
              }
	    }
	} else
	    throw new RuntimeException( "No Assets to Allocate Against." );
    } 

					 
    private void allocate( MPTask task )  {
	Collection assets=transportAssets.getCollection();
	if (assets.size() != 0){
	    // find a transport organization asset
          Enumeration thetransassets = new org.cougaar.util.Enumerator(assets);
	    // Find an available mode of transport...
	    AssetTuple at = chooseAsset( thetransassets, task );
	    if ( task.getPlanElement() == null )
		createTheAllocation( task, at );
	    else 
		alterTheAllocation( task, at );
	} else {
	    // if you don't have a transport organization asset - wait for one
	    waitingTasks.addElement( task );
	}            
    }

    private AllocationResult createEstimatedAllocationResult( boolean is_good, Date start, Date end ) {
	// Only interested in START_TIME, END_TIME for now

	// allocate as if you can do it at the "Best" point
	// Going to have to make this more interesting soon...
	AllocationResult myestimate = null;
	if ( is_good ) {
	    int[] aspectarray = new int[2];
	    double[] resultsarray = new double[2];
	    aspectarray[0] = AspectType.START_TIME;
	    aspectarray[1] = AspectType.END_TIME;
			  
	    resultsarray[0] = (double) start.getTime();
	    resultsarray[1] = (double) end.getTime();

	    myestimate = theLDMF.newAllocationResult(1.0, is_good, aspectarray, resultsarray);
	} else 
	    myestimate = theLDMF.newAllocationResult(1.0, is_good, new int[1], new double[1]);
	return myestimate;
    }

    /**
     * Choose the best Asset for the job (in a rather simple way)
     */
    private AssetTuple chooseAsset( Enumeration assets, MPTask t ) {
	// Insert scheduler here...
	// For now, however, just look for first one.
	if ( assets.hasMoreElements() ) {
	    // Get Policy (only expecting one in the Cluster)
	    Enumeration myPolicies = policies.elements();
	    ShipPolicy sp = null;
	    if ( myPolicies != null ) {
		if ( myPolicies.hasMoreElements() )
		    sp = (ShipPolicy)myPolicies.nextElement();
	    }
	    Asset myAsset = null;
	    AllocationResult myAR = null;
	    Date start = null;
	    Date end = null;
	    Enumeration preferences = t.getPreferences();
	    if ( preferences != null && preferences.hasMoreElements() ) {
		// Get start/end times
		while ( preferences.hasMoreElements() ) {
		    Preference pref = (Preference)preferences.nextElement();
		    int at = pref.getAspectType();
		    if ( at == AspectType.START_TIME )
			start = new Date( pref.getScoringFunction().getBest().getAspectValue().longValue());
		    if ( at == AspectType.END_TIME )
			end = new Date( pref.getScoringFunction().getBest().getAspectValue().longValue());
		}
	    }
	    int policy_value = 1;
	    if ( sp != null )
		policy_value = sp.getShipDays();
	    if ( (start != null) && (end != null) ) {
		while ( assets.hasMoreElements() ) {
		    myAsset = (Asset)assets.nextElement();
		    //System.err.print( "\nTrying Asset " + myAsset + "..." );
		    myAR = ruAvailable( myAsset.getRoleSchedule(), start, end, (policy_value * ONE_DAY) );
		    if ( myAR.isSuccess() == true ) {
			return new AssetTuple( myAsset, myAR );
		    }
		}
	    }
	    return new AssetTuple( myAsset, myAR );
	}
	return null;
    }

    private AllocationResult ruAvailable( RoleSchedule rs, Date start, Date end, long rt ) {
	if ( start.after( end ) || start.equals( end ))
	    return createEstimatedAllocationResult( false, start, end );
	//System.err.println( "\nTrying between " + start + " and " + new Date( start.getTime() + rt ) );
	int size = rs.getOverlappingRoleSchedule( start.getTime(), start.getTime() + rt).size();
	if ( size == 0 )
	    return createEstimatedAllocationResult( true, start, new Date( start.getTime() + rt ) );
	else {
	    return ruAvailable( rs, new Date( start.getTime() + rt ), end, rt );
	}
    }
	
}
