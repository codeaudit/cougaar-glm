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

import org.cougaar.lib.util.UTILPlugInException;

import java.util.Date;
import java.util.ArrayList;
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

public class UTILBufferingThread implements Runnable {
  /** 
   * Works with a buffering plugin
   */
  public UTILBufferingThread (UTILBufferingPlugIn bufferingPlugIn,
			      boolean myExtraOutput, boolean myExtraExtraOutput) {
    myPlugin = bufferingPlugIn;

    this.myExtraOutput      = myExtraOutput;
    this.myExtraExtraOutput = myExtraExtraOutput;
    getEnvData ();

    bufferedTasks    = new ArrayList ();
    notYetDispatched = new ArrayList ();
  }

  /**
   * Uses the plugin's params to set its parameters.
   */
  protected void getEnvData(){
    ParamTable myParams = myPlugin.getMyParams ();

    try {
      MINSIZE = myParams.getLongParam("MinSize");
    } catch (Exception e) {
      MINSIZE = 1;
    }
    try {
      MAXSIZE = myParams.getLongParam("MaxSize");
    } catch (Exception e) {
      MAXSIZE = 1;
    }
    try {
      MAXTIME = myParams.getLongParam("MaxTime") * 1000;
    } catch (Exception e) {
      MAXTIME = 1000;
    }
    if (myExtraOutput)
      System.out.println (this + 
			  " - Buffering thread params : MaxSize " + MAXSIZE +
			  " MinSize " + MINSIZE +
			  " MaxTime " + MAXTIME/1000 +
			  " seconds");
    if (MINSIZE > 1)
      System.out.println (this + " - WARNING - MINSIZE > 1 (" + MINSIZE + 
			  ") - some tasks may not be handled!");
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
   * @see org.cougaar.lib.callback.UTILWorkflowCallback#reactToChangeFilter ()
   * @see org.cougaar.lib.callback.UTILExpandableTaskCallback#reactToChangeFilter ()
   * @see #checkBuffer
   */

  public void run() {
    try {
      while (true) {
	synchronized (this) {
	  // grab all the tasks that have come in recently

	  notYetDispatched.addAll (bufferedTasks);
	  bufferedTasks.clear ();

	  int tasksLeft = notYetDispatched.size (); 
	  if (anyTasksLeft ()) {
	    long waitTime = getWaitTime ();
	    if (waitTime > 0) {
		
	      if (myExtraExtraOutput) 
		System.out.println ("" + this + " listener " + myPlugin.getName () + 
				    " super has " + tasksLeft + " tasks left, waiting " + waitTime);
	      wait (waitTime);
	    }
	    else if (myExtraExtraOutput) 
	      System.out.println ("" + this + " listener " + myPlugin.getName () + " not waiting");
	  }
	  else {
	    if (myExtraExtraOutput) 
	      System.out.println ("" + this + " listener " + myPlugin.getName () +
				  " waiting with nothing to do.  Has " + tasksLeft + ".");
	    wait ();
	  }
	}

	checkBuffer (notYetDispatched);
      }
    } 
    catch (Exception e) {
      e.printStackTrace();
      throw new UTILPlugInException(e.getMessage());
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
   * @see dispatchConditionMet
   * @see dispatchTasks
   * @see alternateDispatchConditionMet
   * @see alternateDispatch
   * @param buffered tasks - tasks to check to see if we're ready
   *        also the tasks sent to dispatchTasks
   */
  protected void checkBuffer (List notYetDispatched) { 
    if (dispatchConditionMet (notYetDispatched)) {
      if (myExtraExtraOutput) 
	System.out.println ("" + this + 
			    " - thread has " + notYetDispatched.size() + 
			    " tasks buffered.");
      if (myExtraExtraOutput) 
	System.out.println ("" + this + 
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

      if (myExtraExtraOutput) 
	System.out.println("" + this + 
			   " thread now has " + bufferedTasks.size () + 
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
   * calls UTILBufferingPlugIn.processTasks
   * @see UTILBufferingPlugIn#processTasks
   */
  protected void dispatchTasks(List tasks) {
    lastupdate = new Date ();

    if (myExtraExtraOutput) 
      System.out.println("" + this + " - dispatching " + 
			 tasks.size () + " tasks.");

    // Start a transaction
    myPlugin.startTransaction();

    try{
      processBufferedTasks (tasks);
    } catch(Exception e){
      if (myExtraOutput)
	System.err.println("" + this + 
			   " - Exception raised in processTasks; dropping " +
			   tasks.size() + " tasks on the floor.");
      e.printStackTrace();
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

  protected UTILBufferingPlugIn myPlugin = null;
  protected long MAXSIZE;
  protected long MINSIZE;
  protected long MAXTIME; // milliseconds
  protected Date lastupdate = null;

  protected boolean myExtraOutput;
  protected boolean myExtraExtraOutput;
}
