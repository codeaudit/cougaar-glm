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

import org.cougaar.lib.param.ParamMap;

import org.cougaar.lib.util.UTILPluginException;
import org.cougaar.util.log.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
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
 * @see UTILListeningBufferingThread
 */

public class UTILBufferingThread implements Runnable {
  /** 
   * Works with a buffering plugin
   */
  public UTILBufferingThread (UTILBufferingPlugin bufferingPlugin, Logger logger) {
    myPlugin = bufferingPlugin;

    this.logger=logger;
    getEnvData ();

    bufferedTasks    = new ArrayList ();
    notYetDispatched = new ArrayList ();
  }

  /**
   * Uses the plugin's params to set its parameters.
   */
  protected void getEnvData(){
    ParamMap myParams = myPlugin.getMyParams ();

    try {
      if (myParams.hasParam("MinSize"))
	MINSIZE = myParams.getLongParam("MinSize");
      else
	MINSIZE = 1;
      if (myParams.hasParam("MaxSize"))
	MAXSIZE = myParams.getLongParam("MaxSize");
      else
	MAXSIZE = 1;
      if (myParams.hasParam("MaxTime"))
	MAXTIME = myParams.getLongParam("MaxTime") * 1000;
      else
	MAXTIME = 1000;
    } catch (Exception e) { logger.error ("error",e); }

    if (logger.isInfoEnabled()) {
      logger.info (this + " - Buffering thread params : MaxSize " + MAXSIZE +
		   " MinSize " + MINSIZE +
		   " MaxTime " + MAXTIME/1000 +
		   " seconds");
      if (MINSIZE > 1)
	logger.info (this + " - WARNING - MINSIZE > 1 (" + MINSIZE + 
		     ") - some tasks may not be handled!");
    }
  }

  /**
   * Grabs all tasks that have arrived via addTask, and caches them in a local
   * buffer.  It checks if there are any tasks left to be processed.  If so, 
   * waits (not sleeps) a finite amount of time before checking again.  Can be
   * awoken by new tasks arriving.
   *
   * If there are no tasks, waits indefinitely until notified (typically) by 
   * UTILWorkflowCallback/UTILExpandableTaskCallback.reactToChangeFilter ().
   *
   * After waiting, calls checkBuffer.
   * 
   * @see org.cougaar.lib.callback.UTILWorkflowCallback#reactToChangedFilter
   * @see org.cougaar.lib.callback.UTILExpandableTaskCallback#reactToChangedFilter
   * @see #checkBuffer
   */

  public void run() {
    try {
      while (true) {
	synchronized (this) {

          // the buffering plugin is about to be moved/suspended stop!
          if (pleaseStopFlag) {
            if (!stoppedFlag) {
              stoppedFlag = true;
              notifyAll();
            }
            break;
          }

	  // grab all the tasks that have come in recently

	  notYetDispatched.addAll (bufferedTasks);
	  bufferedTasks.clear ();

	  int tasksLeft = notYetDispatched.size (); 
	  if (anyTasksLeft ()) {
	    long waitTime = getWaitTime ();
	    if (waitTime > 0) {
		
	      if (logger.isDebugEnabled()) 
		logger.debug ("" + this + " listener " + myPlugin.getName () + 
			      " super has " + tasksLeft + " tasks left, waiting " + waitTime);
	      wait (waitTime);
	    }
	    else if (logger.isDebugEnabled()) 
	      logger.debug ("" + this + " listener " + myPlugin.getName () + " not waiting");
	  }
	  else {
	    if (logger.isDebugEnabled()) 
	      logger.debug ("" + this + " listener " + myPlugin.getName () +
			    " waiting with nothing to do.  Has " + tasksLeft + ".");
	    wait ();
	  }

          // ignore the "pleaseStopFlag" until after we've 
          // emptied our buffer
	}

	checkBuffer (notYetDispatched);
      }
    } 
    catch (Exception e) {
      e.printStackTrace();
      throw new UTILPluginException(e.getMessage());
    }
  }

  /** are there any tasks left to process */
  protected boolean anyTasksLeft () {
    return (!notYetDispatched.isEmpty ());
  }

  /** time since the first un-dispatched task */
  protected long getWaitTime () {
    return MAXTIME - staticTime();
  }

  /**
   * Checks two dispatch conditions on the buffered tasks.
   * Calls dispatchTasks or alternateDispatch as appropriate.
   *
   * notYetDispatched should be empty by the time
   * alternateDispatch is called, since dispatchConditionMet should 
   * always true first.
   * 
   * @see #dispatchConditionMet
   * @see #dispatchTasks
   * @see #alternateDispatchConditionMet
   * @see #alternateDispatch
   * @param buffered tasks - tasks to check to see if we're ready
   *        also the tasks sent to dispatchTasks
   */
  protected void checkBuffer (List notYetDispatched) { 
    if (dispatchConditionMet (notYetDispatched)) {
      if (logger.isDebugEnabled()) 
	logger.debug ("" + this + 
		      " - thread has " + notYetDispatched.size() + 
		      " tasks buffered.");
      if (logger.isDebugEnabled()) 
	logger.debug ("" + this + 
		      "- thread - " + staticTime()/1000 + 
		      " seconds spent waiting.");
      dispatchTasks(notYetDispatched);

      // having handled the tasks, we forget about them.
      notYetDispatched.clear ();
    }

    if (alternateDispatchConditionMet ())
      alternateDispatch ();
  }

  /**
   * adds a task to the list that need to be handled
   *
   * If this is the first task we've seen since last
   * dispatch, remember the time so we can compare it later
   * against MAX_TIME.
   *
   * Synchronized here and in run.
   *
   * @param newObject is a new object to be added
   * @see #run
   */
  protected void addTask(Object newObject) {
    synchronized (this) {
      bufferedTasks.add (newObject);

      if (lastupdate == null)
	lastupdate = new Date();

      if (logger.isDebugEnabled()) 
	logger.debug ("" + this + 
		      " after add, thread now has " + bufferedTasks.size () + 
		      " elements queued.");
    }
  }

  protected void removeTask(Object removedObject) {
    synchronized (this) {
      bufferedTasks.remove (removedObject);
      if (logger.isDebugEnabled()) 
	logger.debug("" + this + 
		     " after remove, thread now has " + bufferedTasks.size () + 
		     " elements queued.");
    }
  }
  
  /**
   * Defines when to dispatch buffered tasks
   *
   * @param  the list of tasks to check for size
   * @return true if there are more than MAXSIZE # of tasks OR
   *         there are more than MINSIZE and the MAXTIME has been exceeded
   */
  protected boolean dispatchConditionMet (List tasks) {
    int num = tasks.size();
    boolean moreThanMaxSize = (num >= MAXSIZE);
    boolean moreThanMinSize = (num >= MINSIZE);
    boolean moreThanMaxTimeElapsed = (staticTime() >= MAXTIME);

    boolean result = (moreThanMaxSize || 
		      (moreThanMinSize && moreThanMaxTimeElapsed));

    return result;

  }

  /**
   * Way for subclasses to fire off actions that are independent of 
   * the first dispatchCondition. Mainly when using a second timer.
   * 
   * @see org.cougaar.lib.filter.UTILTimeoutBufferingThread#alternateDispatchConditionMet
   */
  protected boolean alternateDispatchConditionMet () {
    return false;
  }

  protected void alternateDispatch () {}

  /**
   * now it is time to deal with the tasks that we have 
   * accumulated.
   *
   * records last time tasks were dispatched
   *
   * wraps processBufferedTasks call in COUGAAR transaction
   *
   * calls UTILBufferingPlugin.processTasks
   * @see UTILBufferingPlugin#processTasks
   */
  protected void dispatchTasks(List tasks) {
    lastupdate = new Date ();

    if (logger.isDebugEnabled()) 
      logger.debug("" + this + " - dispatching " + 
		   tasks.size () + " tasks.");

    // Start a transaction
    myPlugin.startTransaction();

    try{
      processBufferedTasks (tasks);
    } catch(Exception e){
      if (logger.isErrorEnabled())
	logger.error("" + this + 
		     " - Exception raised in processTasks; dropping " +
		     tasks.size() + " tasks on the floor.", e);
    } finally {
      // end the transaction
      myPlugin.endTransaction();
    }
  }

  /**
   * Overriden in UTILTimeoutBufferingThread
   *
   * Gives no more than MAXSIZE tasks to plugin
   * @param bufferedTasks - currently buffered tasks to give to plugin
   */
  protected void processBufferedTasks (List bufferedTasks) {
    // call plugin's processTasks function
    while (!bufferedTasks.isEmpty ()) {
      List temp = new ArrayList ();
      for (int i = 0; i < MAXSIZE && !bufferedTasks.isEmpty(); i++)
	temp.add (bufferedTasks.remove(0));
      myPlugin.processTasks(temp);
    }
  }

  public void pleaseStop () {
    synchronized (this) {
      if (!stoppedFlag) {
        pleaseStopFlag = true;
        if (logger.isInfoEnabled()) {
          logger.info (this + " - stopping");
        }
        notifyAll();
        while (!stoppedFlag) {
          try {
            wait();
          } catch (InterruptedException ie) {
            if (logger.isErrorEnabled()) {
              logger.error (this + " - stop interrupted", ie);
            }
            break;
          }
        }
        if (logger.isInfoEnabled()) {
          logger.info (this + " - stopped");
        }
      }
    }
  }

  /**
   * local helper function
   */
  protected long staticTime() {
    if (lastupdate == null)
      return 0;

    Date now = new Date();
    return now.getTime() - lastupdate.getTime();
  }

  private List bufferedTasks;
  private List notYetDispatched;

  protected UTILBufferingPlugin myPlugin = null;
  protected long MAXSIZE;
  protected long MINSIZE;
  protected long MAXTIME; // milliseconds
  protected Date lastupdate = null;
  protected boolean pleaseStopFlag = false;
  protected boolean stoppedFlag = false;

  protected Logger logger;
}
