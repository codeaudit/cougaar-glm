/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.lib.param.ParamTable;

import java.util.Date;
import java.util.List;

/**
 * Interacts with a BufferingPlugIn.
 *
 * Implements runnable -- must be started inside a java Thread.
 *
 * Main methods are addTask and dispatchTasks, which runs whenever
 * threshold conditions are met.
 *
 * Note that although the method is named add*Task*, it takes an Object, 
 * not a Task, so that if for some reason a client wanted to buffer 
 * up, say, assets, this class could be used without modification.
 *
 * This class doesn't do much by itself -- it needs to be a listener
 * to get tasks. 
 *
 * @see UTILListeningBufferingThread
 */

public class UTILTimeoutBufferingThread extends UTILListeningBufferingThread {
  /** 
   * Works with a buffering plugin
   */
  public UTILTimeoutBufferingThread (UTILTimeoutBufferingPlugIn bufferingPlugIn,
				     boolean myExtraOutput, boolean myExtraExtraOutput) {
      super (bufferingPlugIn, myExtraOutput, myExtraExtraOutput);
  }

  /** Read additional data from environment files. */
  public void getEnvData () {
    super.getEnvData ();

    try{
      DISPATCH_TIMEOUT = myPlugin.getMyParams ().getLongParam("DISPATCH_TIMEOUT")*1000;
    } catch (Exception e) {
      DISPATCH_TIMEOUT = 3000;
    }
  }

  /** 
   * It's important this return true if there are tasks left to be handled.
   * If it doesn't, these tasks will potentially remain unhandled forever.
   *
   * lastDisposition is null after alternateDispatch
   *
   * @return true if there any tasks left that have not been dispatched, or the plugin
   * has not yet handled 
   */
  protected boolean anyTasksLeft () {
      if (super.anyTasksLeft ())
	  return true;
      boolean retval = (lastDisposition != null) && 
	  ((UTILTimeoutBufferingPlugIn) myPlugin).anyTasksLeft ();

      if (myExtraExtraOutput && retval)
	  System.out.println (this + " - Has tasks left");

      return retval;
  }

  /**
   * @return the longer of the super's wait time and the timeout 
   *         wait time.
   */
  protected long getWaitTime () {
    long waitTime   = super.getWaitTime ();

    if (lastDisposition == null)
      return waitTime;

    long myWaitTime = DISPATCH_TIMEOUT - dispositionWaitTime();

    if (myExtraExtraOutput)
      System.out.println ("" + this + "super wait " + waitTime + 
			  " mission wait " + myWaitTime);

    return  ((waitTime > myWaitTime) ? waitTime : myWaitTime);
  }

  /** 
   * Way for to fire off actions independent of task buffering.
   *
   * Implements a timer, so even partially full assets will have plan 
   * elements made for them, eventually.
   *
   * Note : lastDisposition will be set after the first time
   *        dispatchTasks is called, but will be null after alternateDispatch
   *        
   * @return true if 
   *         1) we haven't met this condition before on the list of tasks AND
   *         2) we have waited long enough (> DISPATCH_TIMEOUT) AND
   *         3) there are tasks left over
   */
  protected boolean alternateDispatchConditionMet () {
    if (myExtraOutput)
      if (lastDisposition != null)
	System.out.println ("" + this + 
			    " - Disposition wait time " + dispositionWaitTime ()/1000 + 
			    " secs. Compare with TIMEOUT max time " + DISPATCH_TIMEOUT/1000 + 
			    " secs.");

    return ((lastDisposition != null) &&
	    (dispositionWaitTime() >= DISPATCH_TIMEOUT) &&
	    ((UTILTimeoutBufferingPlugIn) myPlugin).anyTasksLeft ());
  }

  /**
   * Tell plugin to process these tasks.
   *
   * Gives no more than MAXSIZE tasks to plugin
   *
   * reset last disposition to time to now
   *
   * @param bufferedTasks - currently buffered tasks to give to plugin
   */
  protected void processBufferedTasks (List bufferedTasks) {
    if (!bufferedTasks.isEmpty ())
      lastDisposition = new Date ();

    // call plugin's processTasks function
    super.processBufferedTasks (bufferedTasks);
  }

  /**
   * Deal with the tasks that the plugin has accumulated.
   *
   * Records that there all "leftover" tasks have been handled.
   *
   * wraps processLeftoverTasks call in ALP transaction
   *
   * calls UTILBufferingPlugIn.processTasks
   * @see UTILBufferingPlugIn#processTasks
   */
  protected void alternateDispatch () {
    myPlugin.startTransaction();
    try{
      ((UTILTimeoutBufferingPlugIn)myPlugin).processLeftoverTasks ();
      // we've taken care of all leftover tasks, 
      // so we don't need to keep checking
      lastDisposition = null;
    }
    catch(Exception e){
      e.printStackTrace();
    }
    finally{
      // end the transaction
      myPlugin.endTransaction();
    }
  }

  /**
   * local helper function
   */
  protected long dispositionWaitTime() {
    Date now = new Date();
    return now.getTime() - lastDisposition.getTime();
  }

  Date lastDisposition = null;
  protected long DISPATCH_TIMEOUT; // milliseconds
}
