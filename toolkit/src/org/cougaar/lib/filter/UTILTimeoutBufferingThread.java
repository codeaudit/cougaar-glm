/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import java.util.Date;
import java.util.List;

import org.cougaar.util.log.Logger;

/**
 * <pre>
 * Interacts with a BufferingPlugin.
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
 * </pre>
 * @see UTILListeningBufferingThread
 */

public class UTILTimeoutBufferingThread extends UTILListeningBufferingThread {
  /** 
   * Works with a buffering plugin
   */
  public UTILTimeoutBufferingThread (UTILTimeoutBufferingPlugin bufferingPlugin,Logger logger) {
    super (bufferingPlugin, logger);
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
	  ((UTILTimeoutBufferingPlugin) myPlugin).anyTasksLeft ();

      if (logger.isDebugEnabled() && retval)
	  logger.debug (this + " - Has tasks left");

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

    if (logger.isDebugEnabled())
      logger.debug ("" + this + "super wait " + waitTime + 
	    " mission wait " + myWaitTime);

    return  ((waitTime > myWaitTime) ? waitTime : myWaitTime);
  }

  /** 
   * <pre>
   * Way for to fire off actions independent of task buffering.
   *
   * Implements a timer, so even partially full assets will have plan 
   * elements made for them, eventually.
   *
   * Note : lastDisposition will be set after the first time
   *        dispatchTasks is called, but will be null after alternateDispatch
   *        
   * </pre>
   * @return true if 
   *         1) we haven't met this condition before on the list of tasks AND
   *         2) we have waited long enough (> DISPATCH_TIMEOUT) AND
   *         3) there are tasks left over
   */
  protected boolean alternateDispatchConditionMet () {
    if (logger.isInfoEnabled())
      if (lastDisposition != null)
	logger.info ("" + this + 
		     " - Disposition wait time " + dispositionWaitTime ()/1000 + 
		     " secs. Compare with TIMEOUT max time " + DISPATCH_TIMEOUT/1000 + 
		     " secs.");

    return ((lastDisposition != null) &&
	    (dispositionWaitTime() >= DISPATCH_TIMEOUT) &&
	    ((UTILTimeoutBufferingPlugin) myPlugin).anyTasksLeft ());
  }

  /**
   * <pre>
   * Tell plugin to process these tasks.
   *
   * Gives no more than MAXSIZE tasks to plugin
   *
   * reset last disposition to time to now
   *
   * </pre>
   * @param bufferedTasks - currently buffered tasks to give to plugin
   */
  protected void processBufferedTasks (List bufferedTasks) {
    if (!bufferedTasks.isEmpty ())
      lastDisposition = new Date ();

    // call plugin's processTasks function
    super.processBufferedTasks (bufferedTasks);
  }

  /**
   * <pre>
   * Deal with the tasks that the plugin has accumulated.
   *
   * Records that there all "leftover" tasks have been handled.
   *
   * calls UTILBufferingPlugin.processTasks
   * </pre>
   * @see UTILBufferingPlugin#processTasks
   */
  protected void alternateDispatch () {
    try{
      ((UTILTimeoutBufferingPlugin)myPlugin).processLeftoverTasks ();
      // we've taken care of all leftover tasks, 
      // so we don't need to keep checking
      lastDisposition = null;
    }
    catch(Exception e){
      e.printStackTrace();
    }
    finally{
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
