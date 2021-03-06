/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.plugin.organization;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

/**
 * Handles GLS for subordinates tasks. We expand such tasks into
 * subtasks, one per subordinate. As our subordinates changes we
 * revise the expansion to follow.
 **/
public class GLSAllocatorPlugin extends SimplePlugin implements GLSConstants {
	
  private IncrementalSubscription myAllocatableTasks = null;
  private IncrementalSubscription myOrgAssets;
  private IncrementalSubscription myAllocations;
  private IncrementalSubscription myExpansions;
  private HashMap mySubs = new HashMap();
  private LoggingService myLogger;

  public void setLoggingService(LoggingService ls) {
    myLogger = ls;
  }

    //Override the setupSubscriptions() in the SimplePlugin.
  protected void setupSubscriptions() {
    myLogger = 
      LoggingServiceWithPrefix.add(myLogger, 
				   getMessageAddress().getAddress()+ ": ");

    // subscribe for GLS tasks for subordinates
    myAllocatableTasks = (IncrementalSubscription)subscribe(myAllocatableTaskPredicate);

    //subscribe for assets to allocate against
    myOrgAssets = (IncrementalSubscription)subscribe(myOrgPred);

    // subscribe to my allocations and expansions in order to catch changes in the 
    // allocationresults from the notification process
    myAllocations = (IncrementalSubscription)subscribe(myAllocsPred);
    myExpansions = (IncrementalSubscription)subscribe(myExpsPred);

    if (didRehydrate()) {
      Collection subs = retrieveSubOrgs(myOrgAssets.getCollection());
      for (Iterator iterator = subs.iterator();iterator.hasNext();) {
        Organization org = (Organization)iterator.next();
        mySubs.put(org.getUID(), org);
      }
    }
  }
    
  /**
   * Process new tasks and subordinates as follows: New tasks are
   * expanded to subtasks for each known subordinate.  New
   * subordinates have tasks appended to known expansions.
   * Duplication is avoided when new tasks and new subordinates arrive
   * together because the expansions for the new tasks don't yet
   * exist.
   **/
  public synchronized void execute() {
    if (myAllocatableTasks.hasChanged() ) {
      Collection tasks = myAllocatableTasks.getAddedCollection();

      if (!tasks.isEmpty()) {
	if (myLogger.isDebugEnabled()) {
	  myLogger.debug("Expanding " + tasks.size()
			 + " added tasks");
	}

	Collection subs = retrieveSubOrgs(myOrgAssets.getCollection());
	for (Iterator iterator = tasks.iterator();
	     iterator.hasNext();) {
	  Task task = (Task) iterator.next();
	  expand(task, subs);
	}
      }

      // Should never get changed GLS tasks so don't even try to handle them.
      // Last attempt to process changes led to months of debugging.
      tasks = myAllocatableTasks.getChangedCollection();
      if (!tasks.isEmpty()) {
	for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
	  myLogger.error(getAgentIdentifier() + " ignoring a changed task " +
			 (Task) iterator.next());
	}
      }
    }

    // check the asset container for new Subordinates
    if (myOrgAssets.hasChanged()) {
      /**
       * When a new subordinate appears we must modify the existing
       * expansions to include the new subordinate.
       **/
      Collection newSubs = retrieveSubOrgs(myOrgAssets.getCollection());

      for (Iterator iterator = newSubs.iterator(); iterator.hasNext();) {
        Organization org = (Organization)iterator.next();

        // Remove from set if we already know about it
        if (mySubs.get(org.getUID()) != null) {
          iterator.remove();
        } else {
          mySubs.put(org.getUID(), org);
        }
      }


      for (Enumeration expansions = myExpansions.elements(); 
	   expansions.hasMoreElements(); ) {
	// It's an error if subs show up after we've sent GLS (bug 2990)
	if (newSubs.size() > 0) {
	  myLogger.error(getAgentIdentifier() + 
		       " adding subordinates - " + newSubs + " after initial GLS");
	}

        Expansion exp = (Expansion) expansions.nextElement();

        augmentExpansion(exp, newSubs);
      }
      /** Should handle removed assets, too **/
    }
		
    if (myAllocations.hasChanged()) {
      if (myLogger.isDebugEnabled()) {
	myLogger.debug(getAgentIdentifier() + " updating allocations");
      }
      PluginHelper.updateAllocationResult(myAllocations);
    }
    
    if (myExpansions.hasChanged()) {
      if (myLogger.isDebugEnabled()) {
	myLogger.debug(getAgentIdentifier() + " updating allocations");
      }

      PluginHelper.updateAllocationResult(myExpansions);
    }
  } // end of execute

  /**
   * Allocate the unallocated subtasks of a workflow
   **/
  private void allocate(Workflow wf) {
    Enumeration tasks = wf.getTasks();
    while (tasks.hasMoreElements()) {
      Task t = (Task) tasks.nextElement();
      if (t.getPlanElement() != null) continue; // Already allocated
      Enumeration pp = t.getPrepositionalPhrases();
      while (pp.hasMoreElements()) {
	PrepositionalPhrase p = (PrepositionalPhrase) pp.nextElement();
	if (p.getPreposition().equals(FOR_ORGANIZATION)) {
	  if (p.getIndirectObject() instanceof Organization) {
	    Asset a = (Asset) p.getIndirectObject();
            AllocationResult estimatedresult =
              PluginHelper.createEstimatedAllocationResult(t, theLDMF, 0.0, true);
            Allocation alloc = theLDMF.createAllocation(theLDMF.getRealityPlan(),
                                                        t, a, estimatedresult, Role.BOGUS);
            publishAdd(alloc);
            break;              // Terminate processing of prep phrases
	  }
	}
      }
    }
  }

  /**
   * Expand a new task to include the given subordinates.
   **/
  private void expand(Task t, Collection subs) {
    NewWorkflow wf = theLDMF.newWorkflow();
    //wf.setIsPropagatingToSubtasks();
    wf.setParentTask(t);
    
    // Add tasks to workflow but do not publish them. Done later in the call
    // to PluginHelper.publishAddExpansion();
    augmentWorkflow(wf, subs, false);
    // package up the workflow in an expansion

    double confidence = 0.0;
    if (subs.size() == 0) {
      confidence = 1.0;
    }

    AllocationResult estAR = 
      PluginHelper.createEstimatedAllocationResult(t, theLDMF, confidence, true);
    Expansion exp = theLDMF.createExpansion(theLDMF.getRealityPlan(), t, wf, estAR);
    //publish the Expansion and the workflow tasks all in one
    PluginHelper.publishAddExpansion(getSubscriber(), exp);
    //now allocate the tasks in the new workflow
    allocate(wf);
  }

  private void augmentWorkflow(NewWorkflow wf, Collection subs, 
                               boolean publishInPlace) {
    for (Iterator iterator = subs.iterator();
	 iterator.hasNext();) {
      Asset orga = (Asset) iterator.next();
      NewTask newTask = createSubTask(wf.getParentTask(), orga);
      newTask.setWorkflow(wf);
      wf.addTask(newTask);
      if (publishInPlace) {
        publishAdd(newTask);
      }
    }
  }

  private synchronized void augmentExpansion(Expansion exp, Collection subs) {
    if (subs.size() > 0) {

      NewWorkflow wf = (NewWorkflow) exp.getWorkflow();

      // Add tasks to workflow and publish new tasks
      augmentWorkflow(wf, subs, true);

      publishChange(wf);
      allocate(wf);
    }
  }

  /**
   * Create a subtask that matches the given Task plus a FOR
   * Organization phrase.
   **/
  private NewTask createSubTask(Task t, Asset subasset) {
    Vector prepphrases = new Vector(3);
    
    NewTask subtask = theLDMF.newTask();
    
    // Create copy of parent Task
    subtask.setParentTask( t );
    if (t.getDirectObject() != null) {
      subtask.setDirectObject(theLDMF.cloneInstance(t.getDirectObject()));
    } else {
      subtask.setDirectObject(null);
    }

    // Code removed because oplan is in context
    // pull out the "with OPlan" prep phrase and store for later
    Enumeration origpp = t.getPrepositionalPhrases();
    while (origpp.hasMoreElements()) {
      PrepositionalPhrase theorigpp = (PrepositionalPhrase) origpp.nextElement();
      if ( (theorigpp.getPreposition().equals(WITH_C0)) ||
           (theorigpp.getPreposition().equals(FOR_OPLAN_STAGES))) {
 	prepphrases.addElement(theorigpp);
      }
    }
	  
    NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ORGANIZATION);
    newpp.setIndirectObject(theLDMF.cloneInstance(subasset));
    prepphrases.addElement(newpp);
    subtask.setPrepositionalPhrases(prepphrases.elements());
    
    subtask.setVerb(t.getVerb());
    subtask.setPlan(t.getPlan());
    // for now set the preferences the same as the parent task's
    // in a real expander you would want to distribute the parents preferences
    // across the subtasks.
    subtask.setPreferences(t.getPreferences());
    subtask.setSource(this.getMessageAddress());
    
    return subtask;

  }
      
  private static UnaryPredicate myAllocatableTaskPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
        Task t = (Task) o;

	for (int index = 0; index < GLSInitVerbs.length; index++) {
	  if (t.getVerb().equals(GLSInitVerbs[index])) {
	    PrepositionalPhrase app = t.getPrepositionalPhrase(FOR_ORGANIZATION);
	    
	    if (app != null) {
	      Object indObject = app.getIndirectObject();
	      if (indObject instanceof Asset) {
		Asset asset = (Asset) indObject;
		try {
		  return asset.getTypeIdentificationPG().getTypeIdentification().equals("Subordinates");
		} catch (Exception e) {
		  System.out.println("GLSAlloc error while trying to get the TypeIdentification of an asset");
		  e.printStackTrace();
		}
	      }
	    }
          } 
        }
      }
      return false;
    }
  };

  /**
   * Select all Organization assets
   **/
  private  static UnaryPredicate myOrgPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization) {
        return true;
      } else {
        return false;
      }
    }
  };

  public static Collection retrieveSubOrgs(Collection orgs) {
    HashSet subs = new HashSet(3);

    Iterator iterator = orgs.iterator();
    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();

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
      } 
    }
    return subs;
  }
    

  /**
   * Select all the allocations we create. These are allocations of
   * tasks whose parent task is selected by myAllocatableTaskPred
   **/
  private static UnaryPredicate myAllocsPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Allocation) {
	Task t = ((Allocation) o).getTask();

	for (int index =0; index < GLSInitVerbs.length; index++) {
	  if (t.getVerb().equals(GLSInitVerbs[index])) {
	    return true;
	  }
	}
      }
      return false;
    }
  };

  /**
   * Select the expansions we create. These are expansions of the
   * tasks selected by myAllocableTaskPred
   **/
  private static UnaryPredicate myExpsPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Expansion) {
        return myAllocatableTaskPredicate.execute(((Expansion) o).getTask());
      }
      return false;
    }
  };
}









