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

package org.cougaar.mlm.plugin.organization;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AbstractAsset;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.plugin.util.PluginHelper;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;


import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Capability;

public class GLSAllocatorPlugin extends SimplePlugin {
	
  private IncrementalSubscription allocatableGLSTask = null;
  private IncrementalSubscription orgAssets;
  private IncrementalSubscription myAllocations;
  private IncrementalSubscription myExpansions;
  private RootFactory ldmf;
  private Workflow origGlsWf;
  private Task waitingForSub;
  private Vector waitingForSelf = new Vector();
  private String me = null;

  private HashMap mySubs = new HashMap();
  
    //Override the setupSubscriptions() in the SimplifiedPlugin.
  protected void setupSubscriptions() {
    ldmf = theLDMF;
    // subscribe for PlanElements with Workflows to allocate
    allocatableGLSTask = (IncrementalSubscription)subscribe(allocGLSPred());

    //subscribe for assets to allocate against
    orgAssets = (IncrementalSubscription) subscribe(assetPred());

    //subscribe to my allocations and expansions in order to catch changes in the allocationresults
    // from the notification process
    myAllocations = (IncrementalSubscription) subscribe(myAllocsPred());
    myExpansions = (IncrementalSubscription) subscribe(myExpsPred());

    if (didRehydrate()) {
      Collection subs = retrieveSubOrgs(orgAssets.getCollection());
      for (Iterator iterator = subs.iterator();iterator.hasNext();) {
        Organization org = (Organization)iterator.next();
        mySubs.put(org.getUID(), org);
      }
    }
  }
    
	
  public synchronized void execute() {
    if (me == null) {
      me = getClusterIdentifier().getAddress();
    }

    // check your asset container for new Subordinates
    if (orgAssets.hasChanged()) {
      Collection newSubs = retrieveSubOrgs(orgAssets.getCollection());

      for (Iterator iterator = newSubs.iterator(); iterator.hasNext();) {
        Organization org = (Organization)iterator.next();
        
        // Remove from set if we already know about it
        if (mySubs.get(org.getUID()) != null) {
          iterator.remove();
        } else {
          mySubs.put(org.getUID(), org);
        }
      }

      if (!(newSubs.isEmpty())) {
        // check to see if this(or these) is the first subordinate
	if (waitingForSub != null ) {
	  expandGLS(waitingForSub);
	  //reset waiting for sub
	  waitingForSub = null;
	} else {
	  // do getlogsupport for the new subordinate

	  glsAdditions(newSubs);
	}
      }
    }

    if (allocatableGLSTask.hasChanged() ) {
      Enumeration newTasks = allocatableGLSTask.getAddedList();
      while (newTasks.hasMoreElements()) {
	allocate((Task)newTasks.nextElement());
      }
    }

		
    if (myAllocations.hasChanged()) {
      Enumeration changedallocs = myAllocations.getChangedList();
      while (changedallocs.hasMoreElements()) {
	PlanElement cpe = (PlanElement) changedallocs.nextElement();
	updateAllocationResult(cpe);
      }
    }
    
    if (myExpansions.hasChanged()) {
      Enumeration changedexps = myExpansions.getChangedList();
      while (changedexps.hasMoreElements()) {
	PlanElement cpe = (PlanElement) changedexps.nextElement();
	updateAllocationResult(cpe);
      }
    }


  } // end of execute

  private static final Verb verbGLS = Verb.getVerb(Constants.Verb.GETLOGSUPPORT);
  private synchronized void allocate(Task t) {
    if (t.getVerb().equals(verbGLS)) {
      expandGLS(t);
    } else {
      System.out.println("!!!!!!!! GLS Alloc - don't know how to allocate subtask");
    }
  }


  private void allocateGLS(Workflow wf) {
    Enumeration tasks = wf.getTasks();
    while ( tasks.hasMoreElements()) {
      Asset a = null;
      Task t = (Task) tasks.nextElement();
      AllocationResult estimatedresult = createEstimatedAllocationResult(t);
      Enumeration pp = t.getPrepositionalPhrases();
      while (pp.hasMoreElements()) {
	PrepositionalPhrase p = (PrepositionalPhrase) pp.nextElement();
	if (p.getPreposition().equals(Constants.Preposition.FOR)) {
	  if (p.getIndirectObject() instanceof Organization) {
	    a = (Asset) p.getIndirectObject();
	  }
	}
      }

      if (a != null) {
				// make the allocation
	Allocation alloc = ldmf.createAllocation(ldmf.getRealityPlan(), t, a, estimatedresult, Role.BOGUS);
	publishMyPlanElement(alloc);
      } else {
	System.err.println("!!!!!!!!! GLS Alloc - problem creating GLS allocation pe");
      }
    }
  }

  private void expandGLS(Task t) {
    Vector subtasks = new Vector();
    NewWorkflow tmp = ldmf.newWorkflow();
    //tmp.setIsPropagatingToSubtasks();
    for (Iterator iterator = mySubs.values().iterator();
         iterator.hasNext();) {
      Asset orga = (Asset) iterator.next();
      Task newtask = createSubTasks(t, orga);
      ((NewTask)newtask).setWorkflow(tmp);
      subtasks.addElement(newtask);
    }
    tmp.setParentTask(t);
    if ( !(subtasks.isEmpty()) ) {
      tmp.setTasks(subtasks.elements()); 
      // set the original workflow so that you have a handle for additions
      origGlsWf = tmp;
      // package up the workflow in an expansion
      Expansion newexp = ldmf.createExpansion(ldmf.getRealityPlan(), t, tmp, null);
      //publishMyPlanElement(newexp);
      //publish the Expansion and worflow subtasks all in one
      PluginHelper.publishAddExpansion(getBlackboardService(), newexp);
      //now allocate the tasks in the new workflow
      allocateGLS(tmp);
    } else {
      /* put set waitingForSub to this task - when atleast one sub shows
	 up - then make the origGlsWf.
      */
      waitingForSub = t;
      /* also sent the task to allocateMNG to allocate it to a fake manage 
	 assets so that the AllocationResult rollups will work until 
	 there are subs (or in the case where there will never be subs.
      */
      allocateMNG(t);
    }
  }
		
		
		
  public static Collection retrieveSubOrgs(Collection orgs) {
    HashSet subs = new HashSet(3);
    Role cinc = Role.getRole("CINC");

    Iterator iterator = orgs.iterator();
    while (iterator.hasNext()) {
      Organization org = (Organization) iterator.next();

      if (org.isSelf()) {
        RelationshipSchedule schedule = org.getRelationshipSchedule();

        Collection orgCollection = org.getSubordinates(TimeSpan.MIN_VALUE,
                                                       TimeSpan.MAX_VALUE);

        if (orgCollection.size() > 0) {
          for (Iterator relIterator = orgCollection.iterator();
               relIterator.hasNext();) {
            Relationship relationship = (Relationship) relIterator.next();
            HasRelationships sub = schedule.getOther(relationship);
            
            if (!subs.contains(sub)) {
              subs.add(sub);
            }
          }
        }
      
        /** BOZO - how would this relationship ever get created? 
         **/
        orgCollection = 
          schedule.getMatchingRelationships(cinc.getConverse(),
                                            TimeSpan.MIN_VALUE,
                                            TimeSpan.MAX_VALUE);
        if (orgCollection.size() > 0) {
          for (Iterator relIterator = orgCollection.iterator();
               iterator.hasNext();) {
            Relationship relationship = (Relationship) relIterator.next();
            HasRelationships sub = schedule.getOther(relationship);
            
            if (!subs.contains(sub)) {
              subs.add(sub);
            }
          }
        }
      } 
    }

    return subs;
  }
	
  private synchronized void glsAdditions(Collection newSubs) {
    if ((origGlsWf != null) && (newSubs.size() > 0)) {
      for (Iterator iterator = newSubs.iterator();
           iterator.hasNext();) {
	Asset orga = (Asset) iterator.next();
	Task newtask = createSubTasks(origGlsWf.getParentTask(), orga);
	((NewTask)newtask).setWorkflow(origGlsWf);
	allocateAddition(newtask);
	((NewWorkflow)origGlsWf).addTask(newtask);	
	publishAdd(newtask);
      }
    }
  }
	
  private void allocateAddition(Task t) {
    Asset a = null;
    //create a penalty of 1
    //PenaltyValue pv = createPenaltyValue(1.0);
    //ScheduleElement se = createScheduleElement(t);
    AllocationResult estimatedresult = createEstimatedAllocationResult(t);
    Enumeration pp = t.getPrepositionalPhrases();
    while (pp.hasMoreElements()) {
      PrepositionalPhrase p = (PrepositionalPhrase) pp.nextElement();
      if (p.getPreposition().equals(Constants.Preposition.FOR)) {
	if (p.getIndirectObject() instanceof Organization) {
	  a = (Asset) p.getIndirectObject();
	}
      }
    }
		
    if (a != null) {
      // make the allocation
      Allocation alloc = ldmf.createAllocation(ldmf.getRealityPlan(), t, a, estimatedresult, Role.BOGUS);
      publishMyPlanElement(alloc);
    } else {
      System.err.println("!!!!!!!!! GLSDR Alloc - problem creating GLS allocation pe");
    }
  }

		
  private Task createSubTasks(Task t, Asset subasset) {
    Vector prepphrases = new Vector();
    
    ClusterIdentifier me = this.getCluster().getClusterIdentifier();
    NewTask subtask = ldmf.newTask();
    
    // Create copy of parent Task
    subtask.setParentTask(t);
    if (t.getDirectObject() != null) {
      subtask.setDirectObject(ldmf.cloneInstance(t.getDirectObject()));
    } else {
      subtask.setDirectObject(null);
    }


    ContextOfUIDs context = (ContextOfUIDs) t.getContext();
    if (context == null) {
      System.err.println(getClass() + " missing context in " + t);
    }

    // Code removed because oplan is in context
//      // pull out the "with OPlan" prep phrase and store for later
//      Enumeration origpp = t.getPrepositionalPhrases();
//      while (origpp.hasMoreElements()) {
//        PrepositionalPhrase theorigpp = (PrepositionalPhrase) origpp.nextElement();
//        if ( theorigpp.getPreposition().equals(Constants.Preposition.WITH) ) {
//  	prepphrases.addElement(theorigpp);
//        }
//      }
	  
    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(ldmf.cloneInstance(subasset));
    prepphrases.addElement(newpp);
    subtask.setPrepositionalPhrases(prepphrases.elements());
    
    subtask.setVerb(t.getVerb());
    subtask.setPlan(t.getPlan());
    // for now set the preferences the same as the parent task's
    // in a real expander you would want to distribute the parents preferences
    // across the subtasks.
    subtask.setPreferences(t.getPreferences());
    subtask.setSource(me);
    
    return subtask;

  }

  private void allocateMNG(Task t) {
	
    AbstractAsset manageasset = null;
    try {
      RootFactory ldmfactory = getFactory();
      manageasset = (AbstractAsset)ldmfactory.createAsset( AbstractAsset.class );
    } catch (Exception e) {
      System.err.println("GLSDRAllocator - problem creating the abstract manage asset");
      e.printStackTrace();
    }

    AllocationResult estimatedresult = createEstimatedAllocationResult(t);
		
    if (manageasset != null) {
      // make the allocation
      Allocation alloc = ldmf.createAllocation(ldmf.getRealityPlan(), t, manageasset, estimatedresult, Role.BOGUS);
      publishMyPlanElement(alloc);
    } else {
      System.err.println("!!!!!!!!! GLSDR Alloc - problem creating MNG allocation pe");
    }
  }


	
  private Asset findMyself() {
    Iterator iterator = orgAssets.getCollection().iterator();
    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      if (org.isSelf()) {
        return org;
      }
    }  
    return null;
  }

	
  private void publishMyPlanElement(PlanElement pe) {
    publishAdd(pe);
  }
		
  private void updateAllocationResult(PlanElement cpe) {
    //System.out.println("&&&&&&&&&&&&&&&&& GLSDR: updateAllocationResult called!!!");
    if (cpe.getReportedResult() != null) {
      // compare the allocationresult objects.
      // If they are NOT ==, re-set the estimated result.
      // For now, ignore whether the compositions of the results are equal
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
        
      AllocationResult myestimate = ldmf.newAllocationResult(0.0, true, aspectarray, resultsarray);
      return myestimate;
    }
    // if there were no preferences...return a null estimate for the allocation result (for now)
    return null;
  }
      

  public static UnaryPredicate allocGLSPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task) {
	  Task t = (Task) o;
	  if ( t.getPlanElement() == null ) {
	    if (t.getVerb().equals(verbGLS)) {
	      Enumeration pp = t.getPrepositionalPhrases();
	      while (pp.hasMoreElements()) {
		PrepositionalPhrase app = (PrepositionalPhrase) pp.nextElement();
		if ((app.getPreposition().equals(Constants.Preposition.FOR)) && (app.getIndirectObject() instanceof Asset) ) {
		  String name = null;
		  try {
		    name = ((Asset)app.getIndirectObject()).getTypeIdentificationPG().getTypeIdentification();
		  }
		  catch (Exception e) {
		    System.out.println("GLSAlloc error while trying to get the TypeIdentification of an asset");
		    e.printStackTrace();
		  }
		  if (name.equals("Subordinates")) {
		    return true;
		  }
		}
	      }
	    } 
	  }
	}
	return false;
      }
    };
  }


  public static UnaryPredicate assetPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return ( o instanceof Organization );
      }
    };
  }

  public static UnaryPredicate myAllocsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	boolean matched = false;
	if (o instanceof Allocation) {
	  // if the PlanElement is for the correct kind of task - make sure its an allocation
	  Task t = ((Allocation)o).getTask();
	  return (t.getVerb().equals(verbGLS));
	}
	return matched;
      }
    };
  }
  
  public static UnaryPredicate myExpsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Expansion) {
	  Workflow wf = ((Expansion)o).getWorkflow();
	  Enumeration wftasks = wf.getTasks();
	  while (wftasks.hasMoreElements()) {
	    Task t = (Task) wftasks.nextElement();
	    if (t.getVerb().equals(verbGLS)) {
	      Enumeration pp = t.getPrepositionalPhrases();
	      while (pp.hasMoreElements()) {
		PrepositionalPhrase app = (PrepositionalPhrase) pp.nextElement();
		if ((app.getPreposition().equals(Constants.Preposition.FOR)) && (app.getIndirectObject() instanceof Organization) ) {
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
}
