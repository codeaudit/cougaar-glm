/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;

import java.util.List;

/**
 * Listener intended to be used by all expanders.
 *
 * Being an expansion listener now means participating in a 4 step process.
 * When an expansion changes, it can change in one of 4 ways, each of which the 
 * the listener can play a role in.
 *
 * Note that below, when default behavior is mentioned, it is in ExpanderPlugInAdapter.
 *  
 * 1) The expansion can fail.  (handleFailedExpansion)
 * Some downstream allocation can fail, and if this happens, the listener will
 * be called with the expansion and the list of failed sub tasks.  
 *
 * The listener can handle this in one of two ways :
 *  a) rescind the expansion and replan and reallocate OR
 *  b) give up and report the failed expansion to a superior.  An example of this
 *     approach is in UTILSimpleExpanderPlugIn
 *
 * Since this is important, THERE IS NO DEFAULT BEHAVIOR for this option.
 *
 * 2) The expansion's constraints may be violated.  (handleConstraintViolation)
 * No allocation failed, but a workflow constraint was violated.  E.g. Task A is supposed
 * to come before task B, but it wasn't allocated that way.  If this happens, the 
 * listener will be called with the expansion and the list of failed constraints.  
 *
 * The listener can handle this in one of two ways :
 *  a) rescind the expansion and replan and reallocate OR
 *  b) give up and report the expansion to a superior.
 *  
 * This doesn't happen very often, so the default behavior is to throw an exception.
 *
 * 3) The expansion is not good enough, in some way.  (wantToChangeExpansion, 
 *    handleChangedExpansion, publishChangedExpansion)
 * No allocation failed, no workflow constraint was violated, but perhaps the aggregate score
 * of all the allocated subtasks was above some threshold.  First the listener is asked if
 * it wants to change the expansion, and if so, handleChangedExpansion gets called.  Finally,
 * publishChangeExpansion is called.
 *  
 * The listener can handle this in the same way as case #1.
 *  
 * Default behavior is not to want to change the expansion.
 *
 * 4) The expansion is fine.  (reportChangedExpansion)
 * Everything is fine with the expansion, and the listener is asked to report the results
 * of the expansion to its superior.
 *  
 * The listener can handle this by simply reporting the changed expansion by calling
 * updateAllocationResult.
 *  
 * Default behavior is to do this.  
 *
 * Allocators should use/be a GenericListener.
 *
 * @see org.cougaar.lib.callback.UTILExpansionCallback
 * @see org.cougaar.lib.callback.UTILAggregationListener
 * @see org.cougaar.lib.callback.UTILAllocationListener
 */

public interface UTILExpansionListener extends UTILFilterCallbackListener {
  /** 
   * Defines tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingExpandedTask(Task t);
  
  /**
   * An expansion has failed.  It's up to the plugin how to deal with the
   * failure.
   *
   * @param expansion that failed
   * @param List of SubTaskResult objects, all of which have newly failed.
   * @see org.cougaar.domain.planning.ldm.plan.SubTaskResult
   */
  void handleFailedExpansion(Expansion exp, List failedSubTasks);

  /**
   * At least one constraint has been violated.  It's up to the plugin how to deal 
   * with the violation(s).
   *
   * @param expansion that failed
   * @param list of Constraints that have been violated
   */
  void handleConstraintViolation(Expansion exp, List violatedConstraints);

  /**
   * Does the plugin want to change the expansion?
   * For instance, although no individual preference may have been exceeded,
   * the total score for the expansion may exceed some threshold, and so the
   * plugin may want to alter the expansion.
   *
   * @see org.cougaar.lib.util.UTILAllocate#scoreAgainstPreferences
   * @param expansion to check
   * @return true if plugin wants to change expansion
   */
  boolean wantToChangeExpansion(Expansion exp);

  /**
   * The plugin changes the expansion.
   *
   * @see wantToChangeExpansion
   * @param expansion to change
   */
  void changeExpansion(Expansion exp);

  /**
   * publish the change
   *
   * @see wantToChangeExpansion
   * @param expansion to change
   */
  void publishChangedExpansion(Expansion exp);

  /**
   * Updates and publishes allocation result of expansion.
   *
   * @param expansion to report
   */
  void reportChangedExpansion(Expansion exp);

  /**
   * Updates and publishes allocation result of expansion.
   *
   * @param expansion to report
   */
  void handleSuccessfulExpansion(Expansion exp, List successfulSubtasks);
}
