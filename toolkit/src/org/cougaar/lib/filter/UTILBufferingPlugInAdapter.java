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
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;

import org.cougaar.lib.util.UTILPreference;
import org.cougaar.lib.util.UTILVerify;

import java.util.List;

/**
 * Example implementation of a plugin that buffers tasks 
 * until a certain threshold is reached.  processTasks is then called.
 * allocators, expanders, and aggregators should all be derived from 
 * this instead of PlugInAdapter; we can then turn on buffering at a higher
 * level by changing the default for buffering from 1. (which is = to no 
 * buffering at all)
 *
 * Abstract because these are undefined:
 *
 * createThreadCallback -- half of the determination of the flavor of a plugin
 *  (Allocator, Expander, Aggregator).  The other half is declaring them listeners
 *  of the right type.
 * processTasks -- different for each plugin flavor
 *
 */
public abstract class UTILBufferingPlugInAdapter extends UTILPlugInAdapter
  implements UTILBufferingPlugIn {

  /**
   * Start up the task buffering thread.
   *
   * Note that localSetup is called AFTER setupFilters, so 
   * getBufferingThread will not return null;
   *
   * IT IS CRUCIAL that this gets called with super () if this
   * is overridden!  Otherwise, plugin will be effectively dead. 
   */
  public void localSetup() {
    super.localSetup ();

    myThread = new Thread(getBufferingThread(), getName ());
    myThread.start ();
  }

  /** 
   * Implemented for UTILBufferingPlugIn
   *
   * OVERRIDE to specialize tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  public boolean interestingTask(Task t) {
    return true; //(t.getVerb().equals(Constants.Verb.TRANSPORT));
  }

  /** 
   * Examines task to see if task looks like what the plugin 
   * expects it to look like.
   *
   * This is plugin-dependent.  For example, the
   * planning factor for unloading a ship is 2 days, but
   * if the task's time window is 1 day, the task is
   * not "well formed."  Duration is a common test, but others
   * could also be a good idea...
   *
   * This is an explicit contract with the plugin
   * that feeds this plugins tasks, governing which tasks
   * are possible for this plugin to handle.
   *
   * @param t Task to check for consistency
   * @return true if task is OK
   */
  public boolean isTaskWellFormed(Task t) {
    return true;
  }

  protected void reportIllFormedTask (Task t) {
    if (!UTILVerify.isTaskTimingCorrect(t)) 
      System.out.println (getName () + 
			  ".reportIllFormedTask - task " + t + 
			  " has " + UTILVerify.reportTimingError (t));  
    else if (!UTILVerify.hasDirectObject (t)) {
      System.out.println (getName () + 
			  ".reportIllFormedTask - task " + t.getUID () + 
			  " is missing direct object.");
    }
    else if (!UTILVerify.hasStartPreference (t)) {
      System.out.println (getName () + 
			  ".reportIllFormedTask - task " + t.getUID () + 
			  " is start time preference.");
    }
    else if (!UTILVerify.hasEndPreference (t)) {
      System.out.println (getName () + 
			  ".reportIllFormedTask - task " + t.getUID () + 
			  " is end time preference.");
    }
    else {
      System.out.println (getName () + 
			  ".reportIllFormedTask - task " + t + 
			  " was ill formed.  (start " + UTILPreference.getReadyAt(t) + 
			  " e " + UTILPreference.getEarlyDate(t) + 
			  " b " + UTILPreference.getBestDate(t) + 
			  " l " + UTILPreference.getLateDate(t) + 
			  ")");
    }

  }

  /**
   * Note that setupFilters is called BEFORE localSetup, so 
   * getBufferingThread will not return null;
   */
  public void setupFilters () {
    super.setupFilters ();

    bufferingThread = createBufferingThread ();

    if (myExtraOutput)
      System.out.println (getName () + 
			  " creating buffering thread " + bufferingThread);

    UTILFilterCallback threadCallback = 
      createThreadCallback ((UTILGenericListener) bufferingThread);

    addFilter (threadCallback);
  }

  /**
   * Provide the callback that is paired with the buffering thread, which is a
   * listener.  The buffering thread is the listener to the callback
   *
   * @return a FilterCallback with the buffering thread as its listener
   */
  protected abstract UTILFilterCallback createThreadCallback (UTILGenericListener listener);

  /**
   * The listening buffering thread communicates with the plugin
   * across the UTILBufferingPlugIn interface.
   *
   * This plugin is NOT a workflow listener.
   *
   * @return UTILListeningBufferingThread with this as the BufferingPlugIn
   * @see UTILBufferingPlugIn
   * @see UTILListeningBufferingThread
   */
  protected UTILBufferingThread createBufferingThread () {
    return new 
      UTILListeningBufferingThread (this, myExtraOutput, myExtraExtraOutput);
  }

  /** accessor */
  protected UTILBufferingThread getBufferingThread () { return bufferingThread; }

  /** 
   * Implemented for UTILBufferingPlugIn
   *
   * needed to lock the subscriber in the nested class   
   * public version of a protected method
   */
  public void startTransaction() { blackboard.openTransaction(); }

  /** 
   * Implemented for UTILBufferingPlugIn
   *
   * needed to unlock the subscriber in the nested class   
   * public version of a protected method
   */
  public void endTransaction()   { blackboard.closeTransaction(false);  }

  /**
   * Implemented for UTILBufferingPlugIn
   *
   * Deal with the tasks that we have accumulated.
   *
   * ABSTRACT, so derived plugins must implement this.
   *
   * @param List of tasks to handle
   */
  public abstract void processTasks (List tasks);

  protected UTILBufferingThread  bufferingThread = null;

  /** a reference to personal Thread to run the BufferingThread in **/
  private Thread myThread = null;
}
