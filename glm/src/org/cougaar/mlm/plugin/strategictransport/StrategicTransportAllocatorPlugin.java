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

package org.cougaar.mlm.plugin.strategictransport;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

public class StrategicTransportAllocatorPlugin extends SimplePlugin {
        
  private Subscription allocatableWorkflows;
  private IncrementalSubscription orgAssets;
  private Subscription myAllocations;
  private PlanningFactory ldmf;
  private Vector waitingTasks = new Vector();
    
  protected void setupSubscriptions() {
    ldmf = theLDMF;

    // subscribe for PlanElements with Workflows to allocate
    allocatableWorkflows = subscribe(allocWFPred());

    // subscribe for assets to allocate against
    orgAssets = (IncrementalSubscription)subscribe(orgPred());

    // subscribe to my allocations in order to catch changes in the penalties and schedules
    // from the notification process
    myAllocations = subscribe(myAllocsPred());

  }
    
  protected void execute() {
    if (allocatableWorkflows.hasChanged() ) {
      Enumeration newwfs = ((IncrementalSubscription)allocatableWorkflows).getAddedList();
      while (newwfs.hasMoreElements()) {
        Expansion exp = (Expansion) newwfs.nextElement();
        allocate(exp.getWorkflow());
      }
    }
	
    // check the asset container for new StrategicTransportProviders
    // if you find any and there are tasks in waitingTasks, allocate them now.
    if (orgAssets.hasChanged()) {
      checkNewOrgs(orgAssets.getCollection());
    }
	
	
    if (myAllocations.hasChanged()) {
      Enumeration changedallocs = ((IncrementalSubscription)myAllocations).getChangedList();
      while (changedallocs.hasMoreElements()) {
        PlanElement cpe = (PlanElement) changedallocs.nextElement();
        updateAllocationResult(cpe);
      }
    }
	
	
  } // end of execute
    
  private void allocate(Workflow wf) {
    Enumeration tasks = wf.getTasks();
    
    Organization stratTransProvider = 
      findOrganization(Constants.Role.STRATEGICTRANSPORTATIONPROVIDER);

    while (tasks.hasMoreElements()) {
      Task t = (Task)tasks.nextElement();
      if (stratTransProvider != null) {
        createTheAllocation(t, stratTransProvider);
      } else {
        // if you don't have a transport organization asset - wait for one
        waitingTasks.addElement(t);
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

  private void createTheAllocation(Task t, Asset a) {
    // make the allocation
    Allocation alloc = ldmf.createAllocation(ldmf.getRealityPlan(), 
                                            t,
                                            a,
                                            createEstimatedAllocationResult(t),
                                            Constants.Role.TRANSPORTER);
    publishAdd(alloc);
  }
    
  private void checkNewOrgs(Collection orgAssets) {
    if (waitingTasks.isEmpty()) {
      return;
    }
    
    Organization stratTransProvider = 
      findOrganization(Constants.Role.STRATEGICTRANSPORTATIONPROVIDER);
    
    if (stratTransProvider != null) {
      // we only need (and expect) one asset to meet our predicate
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
		
  private void updateAllocationResult(PlanElement cpe) {
    if (cpe.getReportedResult() != null) {
      // for now just compare the allocation result instances
      // if they are different objects pass them back up regardless
      // of their content equalness.
      AllocationResult reportedresult = cpe.getReportedResult();
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      if ( (estimatedresult == null) || (! (estimatedresult == reportedresult) ) ) {
        cpe.setEstimatedResult(reportedresult);
        // Publish the change (let superclass handle transactions)
        publishChange(cpe);
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
    
  private static UnaryPredicate allocWFPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Expansion) {
	  Workflow wf = ((Expansion)o).getWorkflow();
	  Enumeration e = wf.getTasks();
	  Task t = (Task) e.nextElement();
	  if (t.getVerb().toString().equals(Constants.Verb.TRANSPORT)) {
	    Enumeration epp = t.getPrepositionalPhrases();
	    while (epp.hasMoreElements()) {
	      PrepositionalPhrase pp = (PrepositionalPhrase) epp.nextElement();
	      if ( (pp.getPreposition().equals(Constants.Preposition.OFTYPE)) &&
		   ( pp.getIndirectObject() instanceof Asset ) ) {
		String io = ((Asset)pp.getIndirectObject()).getTypeIdentificationPG().getTypeIdentification();
		if ( io.equals("StrategicTransportation") ) {
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
    
  private static UnaryPredicate orgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if ( o instanceof Organization ) {
          return true;
        } else {
          return false;
        }
      }
    };
  }

  private static UnaryPredicate myAllocsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof PlanElement) {
          Task t = ((PlanElement)o).getTask();
          if (t.getVerb().toString().equals(Constants.Verb.TRANSPORT)) {
            Enumeration epp = t.getPrepositionalPhrases();
            while (epp.hasMoreElements()) {
              PrepositionalPhrase pp = (PrepositionalPhrase) epp.nextElement();
              if ( (pp.getPreposition().equals(Constants.Preposition.OFTYPE)) && ( pp.getIndirectObject() instanceof Asset ) ) {
                String io = null;
                io = ((Asset)pp.getIndirectObject()).getTypeIdentificationPG().getTypeIdentification();
                if ( io.equals("StrategicTransportation") ) {
                  // if the PlanElement is for the correct kind of task - make sure its an allocation
                  if (o instanceof Allocation) {
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
}
