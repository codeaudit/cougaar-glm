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

package org.cougaar.mlm.plugin.strategictransport;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;

import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewPlanElement;
import org.cougaar.planning.ldm.plan.NewScheduleElement;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Predictor;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.core.util.*;
//import org.cougaar.util.*;

import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;

import java.util.*;


/**
 * This class implements a plugin that allocates strategic
 * transportation tasks to a strategic transportation provider
 * organization.
 **/
public class TaskStrategicTransportAllocatorPlugin  extends SimplePlugin {
        
  private Subscription allocatableTasks;
  private IncrementalSubscription orgAssets;
  private Subscription myAllocations;
  private Vector waitingTasks = new Vector();
    
  protected void setupSubscriptions() {
    // subscribe for PlanElements with Workflows to llocate
    allocatableTasks = subscribe(allocTasksPred());

    //subscribe for assets to allocate against
    orgAssets = (IncrementalSubscription)subscribe(orgPred());

    //subscribe to my allocations in order to catch changes in the penalties and schedules
    // from the notification process
    myAllocations = subscribe(myAllocsPred());

  }
    
  protected void execute() {
    if (allocatableTasks.hasChanged() ) {
      Enumeration newTasks = ((IncrementalSubscription)allocatableTasks).getAddedList();
      while (newTasks.hasMoreElements()) {
        Task myTask = (Task) newTasks.nextElement();
        allocate( myTask );

      }
//        Enumeration changedTasks = ((IncrementalSubscription)allocatableTasks).getChangedList();
//        while (changedTasks.hasMoreElements()) {
//          Task myTask = (Task) changedTasks.nextElement();
//          reallocate(myTask);
//        }
    }
	
    // check the asset container for new StrategicTransportProviders
    // if you find any and there are tasks in waitingTasks, allocate them now.
    if (orgAssets.hasChanged()) {
      checkNewOrgs(orgAssets.elements());
    }
	
    if (myAllocations.hasChanged()) {
      Enumeration changedallocs = ((IncrementalSubscription)myAllocations).getChangedList();
      while (changedallocs.hasMoreElements()) {
        PlanElement cpe = (PlanElement) changedallocs.nextElement();
        updateAllocationResult(cpe);
      }
    }
	
	
  } // end of execute

//    private void reallocate(Task t) {
//      Allocation alloc = (Allocation) t.getPlanElement();
//      if (alloc != null) {
//        publishRemove(alloc);
//      }
//      allocate(t);
//    }

  private void allocate(Task t) {
    Organization stratTransProvider = 
      findOrganization(Constants.Role.STRATEGICTRANSPORTATIONPROVIDER);

    if (stratTransProvider != null){
      // we only expect one
      createTheAllocation(t, stratTransProvider);
    } else {
      // if you don't have a transport organization asset - wait for one
      waitingTasks.addElement(t);
    }                   
  }
        
  private void createTheAllocation(Task t, Organization a) {
      // make the allocation
      Allocation alloc = theLDMF.createAllocation(theLDMF.getRealityPlan(), 
                                            t,
                                            a,
                                            createEstimatedAllocationResult(t, a),
                                            Constants.Role.TRANSPORTER);
      publishAdd(alloc);
  }
    
  private void checkNewOrgs(Enumeration newassets) {
    if (waitingTasks.isEmpty()) {
      return;
    }

    Organization stratTransProvider = 
      findOrganization(Constants.Role.STRATEGICTRANSPORTATIONPROVIDER);
    
    
    if (stratTransProvider != null) {
      Vector newv = new Vector();
      Vector tmp = waitingTasks;
      waitingTasks = newv;
      Enumeration e = tmp.elements();
      while (e.hasMoreElements()) {
        Task t = (Task) e.nextElement();
        createTheAllocation(t, stratTransProvider);
      }
    }           
  }
                

  private Organization findOrganization(Role role) {
    for (Iterator iterator = orgAssets.getCollection().iterator();
         iterator.hasNext();) {
      Organization org = (Organization) iterator.next();
      if (org.isSelf()) {
        Collection transportCollection = 
          org.getRelationshipSchedule().getMatchingRelationships(role);

        if (transportCollection.size() != 0){
          Relationship relationship = 
            (Relationship) transportCollection.iterator().next();
          
          return (Organization) org.getRelationshipSchedule().getOther(relationship);
        } 
      }
    }

    return null;
  }
		
  private void updateAllocationResult(PlanElement cpe) {
    //System.out.println("&&&&&&&&&&&&&&&&& GLSDR: updateAllocationResult called!!!");
    if (cpe.getReportedResult() != null) {
      // compare the allocationresult objects.
      // If they are not ==, re-set the estimated result.
      // For now, ignore whether their composition is equal.
      AllocationResult reportedresult = cpe.getReportedResult();
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      if ( (estimatedresult == null) || (! (estimatedresult == reportedresult) ) ) {
        cpe.setEstimatedResult(reportedresult);
				//System.out.println("&&&&&&&&&&&&&&& GLSDR: setting estimates to reported");
	     // Publish the change (let superclass handle transactions)
	     publishChange(cpe);
       //	System.out.println("&&&&&&&&&&&&&&&&& GLSDR:  published change");
      }
    }
  }
	
    private AllocationResult createEstimatedAllocationResult(Task t, Organization org)
    {
        Predictor predictor = org.getClusterPG().getPredictor();
        //AllocationResult est_ar = null;
        if (predictor != null)
            return predictor.Predict(t, getDelegate());
        else
            return createEstimatedAllocationResult(t);
    }

    private AllocationResult createEstimatedAllocationResult(Task t) {
        Enumeration preferences = t.getPreferences();
        if ( preferences != null && preferences.hasMoreElements() ) {
            // do something really simple for now.
            Vector aspects = new Vector();
            Vector results = new Vector();
            while (preferences.hasMoreElements()) {
                Preference pref = (Preference) preferences.nextElement();
                int at = pref.getAspectType();
                aspects.addElement(new Integer(at));
                ScoringFunction sf = pref.getScoringFunction();
                // allocate as if you can do it at the "Best" point
                double myresult = ((AspectScorePoint)sf.getBest()).getValue();
                results.addElement(new Double(myresult));
            }
            int[] aspectarray = new int[aspects.size()];
            double[] resultsarray = new double[results.size()];
            for (int i = 0; i < aspectarray.length; i++)
                aspectarray[i] = (int) ((Integer)aspects.elementAt(i)).intValue();
            for (int j = 0; j < resultsarray.length; j++ )
                resultsarray[j] = (double) ((Double)results.elementAt(j)).doubleValue();
            
            AllocationResult myestimate = theLDMF.newAllocationResult(0.0, true, aspectarray, resultsarray);
            return myestimate;
        }
        // if there were no preferences...return a null estimate for the allocation result (for now)
        return null;
    }
    
  /**
   * This predicate selects tasks that are TRANSPORT tasks of type
   * Asset where the asset is a StrategicTransportation asset. It
   * ignores whether the task has been allocated or not.
   **/
  private static UnaryPredicate candidateTasksPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task) {
	  Task t = (Task) o;
	  if (t.getVerb().equals(Constants.Verb.TRANSPORT)) {
	    PrepositionalPhrase pp = t.getPrepositionalPhrase(Constants.Preposition.OFTYPE);
	    if (pp != null) {
	      Object indObject = pp.getIndirectObject();
	      if (indObject instanceof Asset) {
		Asset asset = (Asset) indObject;
		String io = asset.getTypeIdentificationPG().getTypeIdentification();
		if (io.equals("StrategicTransportation")) {
		  return true;
		}
	      }
	    }
	  }
	}
	return false;
      }
    };
  }

  /**
   * This predicate selects TRANSPORT tasks (as selected by
   * candidateTasksPred) that have not yet been allocated.
   **/
  private static UnaryPredicate allocTasksPred() {
    return new UnaryPredicate() {
      private UnaryPredicate candidateTasksPred = candidateTasksPred();
      public boolean execute(Object o) {
	if (candidateTasksPred.execute(o)) {
	  Task t = (Task) o;
	  if (t.getPlanElement() == null) {
	    return true;
	  }
	}
	return false;
      }
    };
  }

  private static UnaryPredicate orgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Organization);
      }
    };
  }


  /**
   * This predicate selects allocations that we have created. It uses
   * the candidateTasksPred predicate to test that the task of the
   * allocation is one of ours.
   **/
  private static UnaryPredicate myAllocsPred() {
    return new UnaryPredicate() {
      private UnaryPredicate candidateTasksPred = candidateTasksPred();
      public boolean execute(Object o) {
	if (o instanceof Allocation) {
	  Task t = ((PlanElement)o).getTask();
	  return candidateTasksPred.execute(t);
	}
	return false;
      }
    };
  }
}
