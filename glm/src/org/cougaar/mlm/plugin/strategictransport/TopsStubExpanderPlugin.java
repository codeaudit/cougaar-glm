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

import java.util.Enumeration;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

public class TopsStubExpanderPlugin extends SimplePlugin
{
    PlanningFactory ldmf;
    private IncrementalSubscription expandableTasks;
    private IncrementalSubscription myExpansions;

    private static UnaryPredicate expTaskPred() {
      return new UnaryPredicate() {
	public boolean execute(Object o) {      
	  if ( o instanceof Task ){
	    Task t = (Task) o;
	    if ((t.getVerb().equals(Constants.Verb.TRANSPORT)) &&
		(t.getWorkflow() == null) && 
		(t.getPlanElement() == null)) {
	      return true;
	    }		
	  }
	  return false;
	}
      };
    }

    //since this is a stub this plugin assumes that all the workflows which
    //contain Transport tasks are created by  itself and subscribes to the workflows 
    //containing verb:TRANSPORT
    private static UnaryPredicate myExpansionsPred() {
      return new UnaryPredicate() {
	public boolean execute(Object o) {      
	  if ( o instanceof Expansion ){
	    Workflow wf = ((Expansion)o).getWorkflow();
	    Enumeration myTasks = wf.getTasks();
	    while ( myTasks.hasMoreElements() ) {
	      Task t = (Task)myTasks.nextElement();
	      if ((t.getVerb().equals(Constants.Verb.TRANSPORT))) {
		return true;
	      }		
	    }
	  }
	  return false;
	}
      };
    }

    protected void setupSubscriptions() {
	expandableTasks = (IncrementalSubscription) subscribe(expTaskPred());
	myExpansions = (IncrementalSubscription) subscribe(myExpansionsPred());
    }

    protected void execute(){
	if (expandableTasks.hasChanged()){
	    Enumeration newtasks = expandableTasks.getAddedList();
	    while (newtasks.hasMoreElements()){
		System.out.println("%%%%%%%% TopsStubExpander: New TRANSPORT tasks received");
		Task task = (Task)newtasks.nextElement();

		System.out.println("*****"+task.getVerb());
		NewTask subtask = doExpansion(task);
		Workflow wf = buildWorkflow(task, subtask);
		PlanElement pe = buildPlanElement (wf);
		publishAdd(pe);
		System.out.println("%%%%%%%% TopsStubExpander:Expansion complete");

	    }
	}

	if ( myExpansions.hasChanged() ) {
	    Enumeration changedExps = myExpansions.getChangedList();
	    while ( changedExps.hasMoreElements() ) {
		updateAllocationResult( (Expansion) changedExps.nextElement() );
	    }
	}
    }

    private NewTask doExpansion( Task task ) {
	MessageAddress me = this.getMessageAddress();
	NewTask subtask = theLDMF.newTask();
	
	// Create copy of parent Task
	subtask.setParentTask( task );
	subtask.setDirectObject( task.getDirectObject() );
	subtask.setPrepositionalPhrases( task.getPrepositionalPhrases() );
	subtask.setVerb( task.getVerb() );
	subtask.setPlan( task.getPlan() );
	subtask.setPreferences( task.getPreferences() );
	subtask.setSource( me );     
	return subtask;
    }

    /**
     * Create a new Workflow from the subtask(s)
     */
    private Workflow buildWorkflow( Task task, NewTask subtask ) {

	NewWorkflow wf = theLDMF.newWorkflow();
        wf.setIsPropagatingToSubtasks(true);
	wf.setParentTask( task ); //subtask.getParentTask()
	subtask.setWorkflow( wf );
	wf.addTask( subtask );
        //publishAdd(subtask);
	return wf;
    }

    /**
     * Create new PlanElement from Workflow
     */
    private PlanElement buildPlanElement( Workflow wf ){
	Task t = wf.getParentTask();
	PlanElement pe = theLDMF.createExpansion(t.getPlan(),
						t,
						wf,
						null);
	return pe;
    }

  private void updateAllocationResult(PlanElement cpe) {
    if (cpe.getReportedResult() != null) {
      // compare the allocationresult objects.
      // If they are NOT ==, re-set the estimated result.
      // For now, ignore whether the composition of the results are equal.
      AllocationResult reportedresult = cpe.getReportedResult();
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      if ( (estimatedresult == null) || (! (estimatedresult == reportedresult) ) ) {
        cpe.setEstimatedResult(reportedresult);
       publishChange(cpe);
      }
    }
  }
}
