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

package org.cougaar.domain.mlm.plugin.strategictransport;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewPlanElement;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Allocation;
//Start new imports
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;


import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import java.util.Enumeration;

import org.cougaar.util.UnaryPredicate;

/**
 * The StrategicTransportExpanderPlugIn will deal with tasks to provide
 * strategic transport for other supported clusters. 
 * 
 * Current functionality (MB3.0) allows only for pass-through
 * of Tasks through a Workflow.
 *
 * @author       ALPINE <alpine-software@bbn.com>
 * @version      $Id: StrategicTransportExpanderPlugIn.java,v 1.4 2001-08-22 20:27:42 mthome Exp $
 *
 */

public class StrategicTransportExpanderPlugIn extends SimplePlugIn
{
  private RootFactory ldmf;
  
  /** IncrementalSubscription to hold collection of input tasks **/
  private IncrementalSubscription expandableTasks;
  private IncrementalSubscription myExpansions;
  private IncrementalSubscription changedTasks;
    
    /** Predicate for dealing with Expandable Tasks of strategic transport */
  private static UnaryPredicate expTaskPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if ( o instanceof Task ) {
          Verb verb = ((Task)o).getVerb();
          if ( (((Task)o).getWorkflow() == null )  &&
               (((Task)o).getPlanElement() == null ) &&
               (verb.toString().equals( Constants.Verb.TRANSPORT )) ) {
            Enumeration epp = ((Task)o).getPrepositionalPhrases();
            while (epp.hasMoreElements()) {
              PrepositionalPhrase pp = (PrepositionalPhrase) epp.nextElement();
              if ( (pp.getPreposition().equals(Constants.Preposition.OFTYPE)) && ( pp.getIndirectObject() instanceof Asset ) ) {
                String io = null;
                try {
                  io = ((Asset)pp.getIndirectObject()).getTypeIdentificationPG().getTypeIdentification();
                } catch (Exception excep) {
                  System.err.println("error in StratTransExp predicate trying to access the typeID of an IO");
                  excep.printStackTrace();
                }
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

  private static UnaryPredicate myExpansionsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if ( o instanceof Expansion ) {
          Workflow wf = ((Expansion)o).getWorkflow();
          Enumeration wfTasks = wf.getTasks();
          while ( wfTasks.hasMoreElements() ) {
            Task t = (Task)wfTasks.nextElement();
            Verb verb = ((Task)t).getVerb();
            if ( verb.toString().equals( Constants.Verb.TRANSPORT ) ) {
              Enumeration epp = ((Task)t).getPrepositionalPhrases();
              while (epp.hasMoreElements()) {
                PrepositionalPhrase pp = (PrepositionalPhrase) epp.nextElement();
                if ( (pp.getPreposition().equals(Constants.Preposition.OFTYPE)) && ( pp.getIndirectObject() instanceof Asset ) ) {
                  String io = null;
                  try {
                    io = ((Asset)pp.getIndirectObject()).getTypeIdentificationPG().getTypeIdentification();
                  } catch (Exception excep) {
                    System.err.println("error in StratTransExp predicate trying to access the typeID of an IO");
                    excep.printStackTrace();
                  }
                  if ( io.equals("StrategicTransportation") ) {
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

  private static UnaryPredicate changedTaskPred() {
    return new UnaryPredicate() {
      public boolean execute( Object o ) {
        if ( o instanceof Task ) {
          Verb verb = ((Task)o).getVerb();
          if ( ( verb.equals( Constants.Verb.TRANSPORT )) &&
               (((Task)o).getPlanElement() instanceof Expansion))
            return true;
        }
        return false;
      }
    };
  }
    
  protected void setupSubscriptions() {
    expandableTasks = (IncrementalSubscription) subscribe(expTaskPred());
    myExpansions = (IncrementalSubscription) subscribe(myExpansionsPred());
    changedTasks = (IncrementalSubscription)subscribe( changedTaskPred() );
  }

  protected void execute() {
    if (expandableTasks.hasChanged()) {
      Enumeration newtasks = expandableTasks.getAddedList();
      while (newtasks.hasMoreElements()) {
        Task currentTask = (Task)newtasks.nextElement();
        expand(currentTask);
      }
    }
		  
    if (myExpansions.hasChanged()) {
      Enumeration  changedexps = myExpansions.getChangedList();
      while (changedexps.hasMoreElements()) {
        PlanElement cpe = (PlanElement) changedexps.nextElement();
        updateAllocationResult(cpe);
      }
    }
    
    if ( changedTasks.hasChanged() ) {
      Enumeration chtasks = changedTasks.getChangedList();
      while ( chtasks.hasMoreElements() ) {
        Task task = (Task)chtasks.nextElement();
        updatePreferences( task );
      }
    }

  }
    
  public void expand( Task task ) {
    NewTask subtask = doExpansion( task );
    Workflow wf = buildWorkflow( task, subtask );
    PlanElement pe = buildPlanElement( wf );
    publishAdd(pe);
  }
    
  /**
     * Create the (currently only one - MB3.0) subtasks resulting from the 
     * given parent Task.
     */
  private NewTask doExpansion( Task task ) {
	
    ClusterIdentifier me = getClusterIdentifier();
    NewTask subtask = ldmf.newTask();
	
    // Create copy of parent Task
    subtask.setParentTask( task );
    subtask.setDirectObject( task.getDirectObject() );
    subtask.setPrepositionalPhrases( task.getPrepositionalPhrases() );
    subtask.setVerb( task.getVerb() );
    subtask.setPlan( task.getPlan() );
    //use the same preferences of the parent task for now.
    subtask.setPreferences( task.getPreferences() );
    subtask.setSource( me );
	
    return subtask;
	
  }
    
  /**
     * Create a new Workflow from the subtask(s)
     */
  private Workflow buildWorkflow( Task parent, NewTask subtask ) {
	
    NewWorkflow wf = ldmf.newWorkflow();
    wf.setIsPropagatingToSubtasks(true);
    wf.setParentTask( parent ); // subtask.getParentTask() 
    subtask.setWorkflow( wf );
    wf.addTask( subtask );
    //publishAdd(subtask);
    return wf;
	
  }
    
  /**
     * Create new PlanElement from Workflow
     */
  private PlanElement buildPlanElement( Workflow wf ) {
    Task t = wf.getParentTask();
    PlanElement pe = ldmf.createExpansion(t.getPlan(),
                                              t,
                                              wf, null);
    return pe;
	
  }
 
  private void updateAllocationResult(PlanElement cpe) {
    if (cpe.getReportedResult() != null) {
				// compare the allocationresult objects
      // If they are not == re-set the estimated slot.
      // For now don't worry about the equality of the composition of the result objects.
      AllocationResult reportedresult = cpe.getReportedResult();
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      if ( (estimatedresult == null) || (! (estimatedresult == reportedresult) ) ) {
        cpe.setEstimatedResult(reportedresult);
        publishChange(cpe);
      }
    }
  }
	 

  // N.B. if this PlugIn was running in a more complex society, we may want to
  // incorporate some thread-safe code here...
  private void updatePreferences( Task task ) {
    // We know a few things about this Task from the predicate that caught it; 
    // for instance, we know it has an Expansion as its PE, therefore a Workflow, etc, etc;
    Workflow wf = ((Expansion)task.getPlanElement()).getWorkflow();
    Enumeration subtasks = wf.getTasks();
    while ( subtasks.hasMoreElements() ) {
      Task subtask = (Task)subtasks.nextElement();
      ((NewTask)subtask).setPreferences( task.getPreferences() );
      publishChange( subtask );
    }
  }

  public static void main(String argv[])
  {
    StrategicTransportExpanderPlugIn testplugin = new StrategicTransportExpanderPlugIn();
	
  }
    
    
}
