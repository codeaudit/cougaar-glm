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

package org.cougaar.lib.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.lib.callback.UTILExpandableTaskCallback;
import org.cougaar.lib.callback.UTILExpansionCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.util.UTILExpand;
import org.cougaar.lib.util.UTILPluginException;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.SubTaskResult;
import org.cougaar.planning.ldm.plan.Task;


/**
 * <pre>
 * The UTILExpanderPluginAdapter is a handy base class for TOPS/GLMTrans
 * Expanders.  getSubtasks should be redefined in subclasses.
 *
 * Has hooks for handling various replanning conditions.
 *
 * Defines handleFailedExpansion with the default behavior of reporting a
 * changed expansion.
 *
 * </pre>
 * @see #handleFailedExpansion
 */
public class UTILExpanderPluginAdapter extends UTILBufferingPluginAdapter 
  implements UTILExpanderPlugin {

  protected boolean wantConfidence = false;

  public void localSetup () {
    super.localSetup ();

    expand = new UTILExpand (logger);

    try { 
      if (getMyParams().hasParam ("SimpleExpanderWantConfidence")) 
	wantConfidence = getMyParams().getBooleanParam ("SimpleExpanderWantConfidence"); 
    }
    catch (Exception e) {}
  }

  /****************************************************************
   ** Setup Filters...
   **/

  /**
   * Provide the callback that is paired with the buffering thread, which is a
   * listener.  The buffering thread is the listener to the callback
   *
   * @return an ExpandableTaskCallback with the buffering thread as its listener
   * @see org.cougaar.lib.callback.UTILWorkflowCallback
   */
  protected UTILFilterCallback createThreadCallback (UTILGenericListener bufferingThread) { 
    if (isInfoEnabled())
      info (getName () + " : Filtering for Expandable Tasks...");

    myInputTaskCallback = new UTILExpandableTaskCallback (bufferingThread, logger);  

    return myInputTaskCallback;
  } 

  /**
   * Filter out/listen for expandable tasks
   */
  protected UTILFilterCallback getInputTaskCallback    () { 
    return myInputTaskCallback; 
  }

  /** 
   * Implemented for UTILBufferingPlugin interface
   *
   * filter for tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   * @see UTILBufferingPlugin
   */
  public boolean interestingTask(Task t) { return true; }

  /** 
   * Implemented for UTILBufferingPlugin
   *
   * Got an ill-formed task, now handle it, by
   * publishing a failed expansion for the task.
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    reportIllFormedTask(t);
    blackboard.publishAdd (expand.makeFailedExpansion (null, ldmf, t));
  }

  /**
   * create the expansion callback
   */
  protected UTILFilterCallback createExpansionCallback () { 
    if (isInfoEnabled())
      info (getName () + " : Filtering for Expansions...");
        
    UTILFilterCallback cb = new UTILExpansionCallback (this, logger); 
    return cb;
  }

  /**
   * Implemented for UTILExpansionListener
   *
   * Gives plugin a way to filter out which expanded tasks it's
   * interested in.
   *
   * @param t Task that has been expanded (getTask of Expansion)
   * @return true if task is interesting to this plugin
   */
  public boolean interestingExpandedTask (Task t) { 
    boolean val = interestingTask(t); 
    if (isDebugEnabled())
      debug (getName () + ".interestingExpandedTask - " + 
			  (val ? "interested in " : " NOT interested in ") + t.getUID());
    return val;
  }

  /**
   * <pre>
   * Implemented for UTILExpansionListener
   *
   * An expansion has failed.  It's up to the plugin how to deal with the
   * failure.
   *
   * Just report it to superior, where hopefully it will be dealt with.
   *
   * Dumps to error info about the failed tasks.
   * </pre>
   * @param exp expansion that failed
   * @param failedSubTaskResults - the subtasks of the expansion that failed
   */
  public void handleFailedExpansion(Expansion exp, List failedSubTaskResults) {
    reportChangedExpansion (exp);

    if (failedSubTaskResults.size () == 0)
      error(getName () + " - empty list of failed subtasks?"); 
      
    // Go through the list of failed subtasks
    Iterator failed_it = failedSubTaskResults.iterator();
    while (failed_it.hasNext()) {
      // Get the next failed task
      SubTaskResult str = (SubTaskResult)failed_it.next();
      Task failed_e_task = str.getTask();

      error("\tFailed task : " + failed_e_task + 
	    "\n\twith pe " + failed_e_task.getPlanElement ());
      error("\nPref-Aspect comparison : ");
      expand.showPlanElement (failed_e_task);
    }
  }


  /**
   * At least one constraint has been violated.  It's up to the plugin how to deal 
   * with the violation(s).
   *
   * Ideally, this will not happen very often, and when it does, we should hear about it.
   *
   * @param exp expansion that failed
   * @param violatedConstraints list of Constraints that have been violated
   */
  public void handleConstraintViolation(Expansion exp, List violatedConstraints) {
    throw new UTILPluginException (getName (), 
				   "handleConstraintViolation : expansion " + exp +
				   " has violated constraints that were ignored.");
  }

  /**
   * <pre>
   * Implemented for UTILExpansionListener
   *
   * Does the plugin want to change the expansion?
   *
   * For instance, although no individual preference may have been exceeded,
   * the total score for the expansion may exceed some threshold, and so the
   * plugin may want to alter the expansion.
   *
   * Defaults to FALSE.
   *
   * </pre>
   * @see org.cougaar.lib.util.UTILAllocate#scoreAgainstPreferences
   * @param exp expansion to check
   * @return true if plugin wants to change expansion
   */
  public boolean wantToChangeExpansion(Expansion exp) {
    return false;
  }


  /**
   * The plugin changes the expansion.
   *
   * Default does nothing.
   *
   * @see #wantToChangeExpansion
   * @param exp expansion to change
   */
  public void changeExpansion(Expansion exp) {}

  /**
   * publish the change
   *
   * @see #wantToChangeExpansion
   * @param exp expansion to change
   */
  public void publishChangedExpansion(Expansion exp) {
    publishChange (exp);
  }

  /**
   * Report to superior that the expansion has changed. Usually just a pass
   * through to the UTILPluginAdapter's updateAllocationResult.
   *
   * @param exp Expansion that has changed.
   * @see UTILPluginAdapter#updateAllocationResult
   */
  public void reportChangedExpansion(Expansion exp) { 
    if (isDebugEnabled())
      debug (getName () + 
	    " : reporting changed expansion to superior.");
      
    updateAllocationResult (exp); 
  }

  /**
   * Handle a successful expansion <p>
   *
   * Does nothing by default, unless very verbose output is turned on.
   *
   * @param exp Expansion that has succeeded.
   */
  public void handleSuccessfulExpansion(Expansion exp, List successfulSubtasks) { 
    if (isDebugEnabled()) {
      AllocationResult estAR = exp.getEstimatedResult();
      AllocationResult repAR = exp.getReportedResult();
      String est = "e null/";
      String rep = " r null";
      if (estAR != null)
	est = " e " + (estAR.isSuccess () ? "S" : "F") +  " - " +
	  (int) (estAR.getConfidenceRating ()*100.0) + "% /";
      if (repAR != null)
	rep = " r " + (repAR.isSuccess () ? "S" : "F") + " - " +
	  (int) (repAR.getConfidenceRating ()*100.0) + "%";

      debug (getName () + 
	    " : got successful expansion for task " + exp.getTask ().getUID() + 
	    est + rep);
    }
  }

  /**
   * <pre>
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * Override and call super to add new filters, or override 
   * createXXXCallback to change callback behaviour.
   * </pre>
   */
  public void setupFilters () {
    super.setupFilters ();
              
    addFilter (createExpansionCallback());
  }

  /** 
   * Implemented for UTILBufferingPlugin interface
   *
   * @param tasks that have been buffered up to this point
   * @see UTILBufferingPlugin
   */
  public void processTasks (List tasks) {
    if (isInfoEnabled())
      info (getName () + 
	    ".processTasks - processing " + tasks.size() + " tasks.");
    for (int i = 0; i < tasks.size (); i++)
      handleTask ((Task) tasks.get (i));
  }

  /** 
   * <pre>
   * Implemented for UTILGenericListener interface
   *
   * This method Expands the given Task and publishes the PlanElement.
   * The method expandTask should be implemented by child classes.
   * </pre>
   * @param t the task to be expanded.
   */
  public void handleTask(Task t) {
    if (isDebugEnabled())
      debug (getName () + 
	     ".handleTask : called on - " + t.getUID());

    expand.handleTask(ldmf, 
			 getBlackboardService(), 
			 getName(),
			 wantConfidence, 
			 t, 
			 getSubtasks(t));
  }

  /** react to a rescinded task -- by default does nothing */
  public void handleRemovedTask (Task t) {
    if (isDebugEnabled())
      debug (getName () + 
	    ".handleRemovedTask : ignoring removed task - " + t.getUID());
  }
  
  /**
   * <pre>
   * Implemented for UTILExpanderPlugin interface
   *
   * The guts of the expansion.
   *
   * Default does nothing!  Subclass should override.
   * </pre>
   */
  public Vector getSubtasks(Task t) { 
    warn (getName () + 
	  " : WARNING - getSubtasks should be overriden." +
	  " Default does nothing.");
    return new Vector (); 
  }

  protected UTILExpandableTaskCallback myInputTaskCallback;
  protected UTILExpand expand;
}
