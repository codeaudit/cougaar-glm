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

package org.cougaar.lib.filter;

import org.cougaar.domain.planning.ldm.plan.Task;


import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.lib.callback.UTILGenericListener;

import java.util.Collection;

/**
 * A buffering thread that is a generic listener.
 * 
 * Paired with an ExpandableTaskCallback, it can be used
 * in a buffering expander plugin.
 *
 * Paired with an WorkflowCallback, it can be used
 * in a buffering allocator plugin.
 * 
 * Assumes that the buffered objects are tasks.
 */

public class UTILListeningBufferingThread 
  extends UTILBufferingThread 
  implements UTILGenericListener {

  /** 
   * Works with a buffering plugin
   */
  public UTILListeningBufferingThread (UTILBufferingPlugIn bufferingPlugIn,
				      boolean myExtraOutput, 
				      boolean myExtraExtraOutput) {
    super (bufferingPlugIn, myExtraOutput, myExtraExtraOutput);
  }

  /** 
   * Passes interesting test on to plugin
   */

  public boolean interestingTask(Task t) {
    return myPlugin.interestingTask (t);
  }

  /** 
   * Handling a task as a generic listener means buffering it.
   * When the task thresholds are reached, then UTILBufferingThread
   * will call processTasks on BufferingPlugIn.
   */
  public void handleTask (Task t) { 
    if (myExtraExtraOutput)
      System.out.println (classname + 
			  ".handleTask : " + this + " got task " + t + 
			  "\nfrom " + t.getSource ());
    addTask (t); 
  }

  public void handleRemovedTask (Task t) {
    if (myExtraExtraOutput)
      System.out.println (classname + 
			  ".handleRemovedTask : " + this + " got task " + t + 
			  "\nfrom " + t.getSource ());

	removeTask (t);
  }
  
  /** 
   * Asks listener to examine task
   *
   * @param t Task to check for consistency
   * @return true if task is OK
   */
  public boolean isTaskWellFormed(Task t) {
    return myPlugin.isTaskWellFormed(t);
  }

  /** 
   * Got an ill-formed task, now plugin should handle it.
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    myPlugin.handleIllFormedTask (t);
  }

  /** All listeners must be able to create a subscription */
  public IncrementalSubscription subscribeFromCallback(UnaryPredicate pred) {
    return myPlugin.subscribeFromCallback(pred);
  }

  /** 
   * All listeners must be able to create a subscription with a special container
   */
  public IncrementalSubscription subscribeFromCallback(UnaryPredicate pred,
						       Collection specialContainer) {
    return myPlugin.subscribeFromCallback(pred, specialContainer);
  }

  private static final String classname = 
    UTILListeningBufferingThread.class.getName ();
}
