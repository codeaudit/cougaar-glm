/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

package org.cougaar.glm.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.cougaar.core.agent.service.alarm.Alarm;

import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.SubscriptionWatcher;

import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.SchedulerService;

import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.servlet.BlackboardServletSupport;

import org.cougaar.glm.parser.GLMTaskParser;

import org.cougaar.lib.util.UTILAllocate;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;
import org.cougaar.planning.servlet.data.xml.*;

import org.cougaar.util.DynamicUnaryPredicate;
import org.cougaar.util.SyncTriggerModelImpl;
import org.cougaar.util.Trigger;
import org.cougaar.util.TriggerModel;
import org.cougaar.util.UnaryPredicate;

/**
 * <pre>
 * One created for every URL access.
 *
 * Uses a blackboard
 * service, a watcher, and a trigger to monitor published tasks to see
 * when they are complete.
 *
 * Note : may not return if it can't go through all it's states.
 * </pre>
 **/
public class CompletionWatcherWorker extends ServletWorker {
  private static final int BEFORE_FIRST  = 0;
  private static final int DURING_FIRST  = 1;
  private static final int END_OF_FIRST  = 2;
  private static final int AFTER_FIRST   = 3;
  private static final int DURING_SECOND = 4;
  private static final int AFTER_SECOND  = 5;

  private static final String USAGE_IMAGE = "WatcherServlet.jpg";

  /**
   * <pre>
   * Handles all HTTP and HTTPS service requests.
   *
   * Makes a watcher to watch
   * the blackboard, a trigger that will call blackboardChanged (), 
   * and a trigger model to connect them.
   *
   * When a change to a subscription happens, the method call cascade is :
   *  1) The watcher's signalNotify method is called, which
   *  2) Triggers the TriggerModel
   *  3) The TriggerModel queues the trigger with the SchedulerService
   *  4) The SchedulerService calls my Trigger's trigger method
   *  5) That trigger method calls blackboardChanged
   *
   * Does these in order :
   *  - Creates all the watcher, trigger model, trigger overhead
   *  - Creates subscriptions to tasks and plan elements
   *  - Calls getResult
   *  - Writes results to output stream with writeResponse ()
   *  - Unsubscribes
   *  - Has the watcher unregister interest
   *  - Tells the trigger model to stop.
   *
   * </pre>
   * @see #getResult
   * @see #blackboardChanged
   * @see org.cougaar.planning.servlet.ServletWorker#writeResponse
   * @see org.cougaar.core.blackboard.SubscriptionWatcher#signalNotify
   * @see org.cougaar.util.Trigger#trigger
   * @see org.cougaar.util.SyncTriggerModelImpl
   * @see org.cougaar.core.service.SchedulerService
   */
  public void execute(HttpServletRequest request,
                      HttpServletResponse response,
                      SimpleServletSupport support) throws IOException, ServletException {
    this.support = (CompletionWatcherSupport) support;

    Enumeration params = request.getParameterNames ();
    for (;params.hasMoreElements();) {
      String name  = (String) params.nextElement ();
      String value = request.getParameter (name);
      getSettings (name, value);
    }

    if (support.getLog().isDebugEnabled ())
      support.getLog().debug ("CompletionWatcherWorker.Invoked...");

    if (getImage) {
      getUsageImage (response);
      return;
    }

      // create a blackboard watcher
    watcher =
      new SubscriptionWatcher() {
          public void signalNotify(int event) {
            // gets called frequently as the blackboard objects change
            super.signalNotify(event);
            tm.trigger ();
          }
          public String toString() {
            return "ThinWatcher("+CompletionWatcherWorker.this.toString()+")";
          }
        };

    // create a callback for running this component
    Trigger myTrigger =
      new Trigger() {
          // no need to "sync" when using "SyncTriggerModel"
          public void trigger() {
            watcher.clearSignal();
            blackboardChanged();
          }
          public String toString() {
            return "Trigger("+CompletionWatcherWorker.this.toString()+")";
          }
        };

    this.tm = new SyncTriggerModelImpl(this.support.getSchedulerService(), myTrigger);
    this.support.getBlackboardService().registerInterest(watcher);
    this.support.getBlackboardService().openTransaction();
    taskSubscription = (IncrementalSubscription)
      this.support.getBlackboardService().subscribe(taskPredicate);
    planElementSubscription = (IncrementalSubscription)
      this.support.getBlackboardService().subscribe(planElementPredicate);
    this.support.getBlackboardService().closeTransaction();
    // activate the trigger model
    tm.initialize();
    tm.load();
    tm.start();

    // returns an "XMLable" result.
    XMLable result = getResult();

    if (result != null)
      writeResponse (result, response.getOutputStream(), request, support, format);

    BlackboardService bb = this.support.getBlackboardService();
    bb.openTransaction();

    bb.unsubscribe(taskSubscription);
    bb.unsubscribe(planElementSubscription);
    bb.closeTransaction();
    bb.unregisterInterest(watcher);

    tm.unload ();
    tm.stop();
  }

  /** way to do img tags in a cougaar servlet */
  protected void getUsageImage (HttpServletResponse response) throws IOException, ServletException {
    InputStream fin;
    String fileName = USAGE_IMAGE;
    
    fin = getClass().getResource(fileName).openStream ();

    if (fin == null) {
      response.sendError(
			 HttpServletResponse.SC_NOT_FOUND, 
			 "Unable to open file \""+fileName+"\"");
      support.getLog().error (".getUsageImage - Tried to find image " + fileName + " but couldn't." + 
			      " Should be in this directory...");
    }

    String contentType = guessContentType(fileName, fin);
    if (contentType != null) {
      response.setContentType(contentType);
    }

    // maybe add client "last-modified" header?

    OutputStream out = response.getOutputStream();
    byte[] buf = new byte[1024];
    while (true) {
      int len = fin.read(buf);
      if (len < 0) {
	break;
      }
      out.write(buf, 0, len);
    }

    fin.close();
    out.flush();
    out.close();
  }

  private String guessContentType(String fileName, 
				  InputStream fin) throws IOException {
    // examine the first couple bytes of the stream:
    return java.net.URLConnection.guessContentTypeFromStream(fin);
    // or instead examine the filename extention (.gif, etc)
  }

  /**
   * Matches PlanElements for tasks that we have sent and which are
   * complete, i.e. have plan elements and 100% confident reported/estimated
   * allocation results. Both Failure and Success are accepted.
   **/
  private DynamicUnaryPredicate planElementPredicate = new DynamicUnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
        PlanElement pe = (PlanElement) o;
        Task task = pe.getTask();

	if (!isVerbIncluded (task.getVerb()))
	  return false;

	boolean hasReported  = (pe.getReportedResult  () != null);
	boolean hasEstimated = (pe.getEstimatedResult () != null);
	boolean highConfidence = false;

	if (hasReported)
	  highConfidence = 
	    (pe.getReportedResult ().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE);
	else if (hasEstimated)
	  highConfidence = 
	    (pe.getEstimatedResult ().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE);
	    
	return highConfidence;
      }
      return false;
    }
  };

  /**
   * Looking for non-ReportForService tasks
   **/
  private DynamicUnaryPredicate taskPredicate = new DynamicUnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task task = (Task) o;
	return isVerbIncluded (task.getVerb());
      } 
      return false;
    }
  };

  protected boolean isVerbIncluded (Verb verb) {
    if (verb.equals(Constants.Verb.ReportForService))
      return false;
    else if (verbsToInclude.isEmpty())
      return true;
    else
      return verbsToInclude.contains (verb);
  }

  // unused
  protected String getPrefix () { return "CompletionWatcher at "; }

  /**
   * <pre>
   * Use a query parameter to set a field
   *
   * Sets the recognized parameters : firstInterval, secondInterval
   * </pre>
   */
  public void getSettings(String name, String value) {
    super.getSettings (name, value);

    if (support.getLog().isDebugEnabled ())
      support.getLog().debug ("CompletionWatcherWorker.getSettings - name " + name + " value " + value);

    if (eq (name, CompletionWatcherServlet.FIRST_INTERVAL))
      firstInterval = Integer.parseInt(value);
    else if (eq (name, CompletionWatcherServlet.SECOND_INTERVAL))
      secondInterval = Integer.parseInt(value);
    else if (eq (name, CompletionWatcherServlet.VERBS_TO_INCLUDE))
      verbsToInclude = parseVerbs(value);
    else if (eq (name, "getImage"))
      getImage = true;
  }

  protected Collection parseVerbs (String verbs) {
    StringTokenizer tokenizer = new StringTokenizer(verbs, ",");
    Set verbSet = new HashSet ();

    while (tokenizer.hasMoreTokens())
      verbSet.add (Verb.getVerb(tokenizer.nextToken().trim()));

    return verbSet;
  }

  /**
   * Main work done here. <p>
   *
   * Calls blackboard changed initially, then waits until the state machine has reached "done." <p>
   * Then records run elapsed time.
   *
   * @see #getSettings
   * @see #blackboardChanged
   * @return an "XMLable" result.
   */
  protected XMLable getResult() {
    while (!isDone()) {
      if (support.getLog().isDebugEnabled ())
	support.getLog().debug ("CompletionWatcherWorker.getResult - checking quiet...");

      blackboardChanged ();

      synchronized (doneSignal) {
	if (!isDone())
	  try { doneSignal.wait (); } catch (Exception e) {}
      }
    }

    long elapsed = end-start;
    String readable = getElapsedTime(elapsed);

    responseData.addTaskAndTime("interval", readable, elapsed);

    return responseData;
  }

  /**
   * <pre>
   * Called when either 
   *
   * 1) the task subscription changes
   * 2) the plan element subscription changes
   * 3) a timer expires
   * 
   * Does book-keeping to keep track of incomplete tasks. 
   * (If a plan element has completed, it's task is removed from the
   * set of incomplete tasks.)
   *
   * Given whether all tasks are complete, if any arrived in the last transaction,
   * or if a timer expired, calls advance state.
   *
   * </pre>
   **/
  protected void blackboardChanged() {
    try {
      support.getBlackboardService().openTransaction();

      boolean haveChanged = support.getBlackboardService().haveCollectionsChanged();

      boolean planElementChanged = planElementSubscription.hasChanged();
      boolean taskChanged        = taskSubscription.hasChanged();
      Collection addedCollection = taskSubscription.getAddedCollection ();
      boolean hadNewTasks        = !addedCollection.isEmpty();
      
      if (hadNewTasks && support.getLog().isDebugEnabled())
	support.getLog ().debug ("blackboardChanged called - had " + 
				 addedCollection.size() + " NEW tasks.");

      if (taskChanged)
	incompleteTasks.addAll(addedCollection);

      int numBefore = incompleteTasks.size ();
      int numPEAdded   = planElementSubscription.getAddedCollection ().size();

      if (planElementChanged) {
	Collection addedItems = planElementSubscription.getAddedCollection();
	numPEAdded = addedItems.size ();

	for (Iterator iter = addedItems.iterator (); iter.hasNext();) {
	  PlanElement pe = (PlanElement) iter.next();
	  support.getLog ().debug ("blackboardChanged called - complete PE " + pe.getUID() + " added.");
	  incompleteTasks.remove (pe.getTask());
	}
      }

      if (support.getLog().isDebugEnabled()) {
	int numPEChanged = planElementSubscription.getChangedCollection ().size();
	int numPERemoved = planElementSubscription.getRemovedCollection ().size();
	support.getLog ().debug ("blackboardChanged called - incomplete tasks before " + numBefore + 
				 " after " + incompleteTasks.size());
	support.getLog ().debug ("blackboardChanged called - complete PE added " + numPEAdded + 
				 " changed " + numPEChanged + 
				 " removed " + numPERemoved + 
				 " total "   + planElementSubscription.getCollection().size());
      }
      
      boolean allComplete = incompleteTasks.isEmpty ();
      if (allComplete ||
	  planElementChanged || 
	  taskChanged ||
	  timerExpired ())
	advanceState (allComplete, hadNewTasks, timerExpired ());
    } catch (Exception exc) {}
    finally{
      support.getBlackboardService().closeTransaction();
    }
  }

  /** 
   * Calls changeState -- then tests if isDone and if so signals doneSignal in getResult ().
   * @see #getResult
   */
  protected void advanceState (boolean allComplete, boolean hadNewTasks, boolean expired) {
    int before = state;
    changeState (expired, allComplete, hadNewTasks); 

    if (support.getLog ().isInfoEnabled() ||
	support.getLog ().isDebugEnabled()) {
      if (state != before) {
	support.getLog ().info ("advanceState - state before " + before + " after " + state);
	support.getLog ().debug ("advanceState - timerExpired " + expired + 
				 " all tasks complete " + allComplete + 
				 " had new tasks " + hadNewTasks + 
				 " incompleteTasks " + incompleteTasks.size());
      }
      else {
	support.getLog ().debug ("advanceState - incompleteTasks is " + incompleteTasks.size() + 
				 " added "   + taskSubscription.getAddedCollection().size() +
				 " removed " + taskSubscription.getRemovedCollection().size() +
				 " changed " + taskSubscription.getChangedCollection().size());
	support.getLog ().debug ("advanceState - state (" + state + ")" +
				 " timerExpired " + expired + 
				 " all tasks complete " + allComplete + 
				 " had new tasks " + hadNewTasks);
      }
    }

    if (isDone ()) {
      synchronized (doneSignal) {
	doneSignal.notify ();

	if (support.getLog ().isInfoEnabled())
	  support.getLog ().info ("advanceState - run time was " + (end-start) + " millis.");
      }
    }
  } 

  /** we're done if the state machine is in the final state */
  protected boolean isDone () { return (state == AFTER_SECOND); }

  /**
   * <pre>
   * States are :
   *  BEFORE_FIRST - some tasks are incomplete
   *   Transition : when all are complete, start first timer, go to next state
   *  DURING_FIRST - all tasks are complete
   *   Transition : waited first interval, go to next state
   *   Transition : saw incomplete task before time elapsed, go to previous state
   *  END_OF_FIRST - the required quiet interval has elapsed
   *   Transition : saw first incomplete task, mark time, go to next state
   *  AFTER_FIRST  - there are some incomplete tasks
   *   Transition : all tasks complete, mark time, start second timer, go to next state
   *  DURING_SECOND - all tasks are complete again, we're possibly done
   *   Transition : waited second interval, go to next state
   *   Transition : saw incomplete task before time elapsed, forget end time, go to previous state
   *  AFTER_SECOND - we waited long enough to make sure we're done
   * 
   * </pre>
   * @param waitedLongEnough -- if the timer expired, signaling the required wait was completed
   * @param allComplete - are all tasks complete
   * @param hadNewTasks - even if all tasks were complete, did any new tasks appear?
   */
  protected void changeState (boolean waitedLongEnough, boolean allComplete, boolean hadNewTasks) { 
    switch (state) {
    case BEFORE_FIRST:
      if (allComplete) {
	state++; 
	startFirstWait ();
      }
      break;
    case DURING_FIRST:
      if (waitedLongEnough) {
	state++;
	cancelTimer ();
      }
      else if (!allComplete) {
	state--;
	cancelTimer ();
      }
      break;
    case END_OF_FIRST : 
      if (!allComplete || hadNewTasks) {
	start = System.currentTimeMillis ();
	support.getLog().info ("start is " + new Date(start));
	state++;
      }
      break;
    case AFTER_FIRST:
      if (allComplete) {
	state++;
	end = System.currentTimeMillis ();
	support.getLog().info ("possible end is " + new Date(end));
	startSecondWait ();
      }
      break;
    case DURING_SECOND:
      if (waitedLongEnough) 
	state++;
      else if (!allComplete || hadNewTasks) {
	state--;
	cancelTimer ();
      }
      break;
    case AFTER_SECOND :
      break;
    };
  }

  protected void startFirstWait  () { 
    if (support.getLog().isDebugEnabled()) 
      support.getLog().debug("Starting first timer");

    startTimer (firstInterval ); 
  }

  protected void startSecondWait () { 
    if (support.getLog().isDebugEnabled()) 
      support.getLog().debug("Starting second timer");

    startTimer (secondInterval); 
  }

  /**
   * Schedule a update wakeup after some interval of time. <p>
   * Uses an alarm.
   * @param delay how long to delay before the timer expires.
   * @see org.cougaar.core.agent.service.alarm.Alarm
   * @see org.cougaar.core.service.AlarmService#addRealTimeAlarm
   **/
  protected void startTimer(final long delay) {
    if (timer != null) return;  // update already scheduled
    if (support.getLog().isDebugEnabled()) 
      support.getLog().debug("Starting timer with delay " + delay);

    timer = new Alarm() {
      long expirationTime = System.currentTimeMillis() + delay*1000l;
      boolean expired = false;
      public long getExpirationTime() {return expirationTime;}
      public synchronized void expire() {
        if (!expired) {
          expired = true;
          support.getBlackboardService().signalClientActivity();
        }
      }
      public boolean hasExpired() { return expired; }
      public synchronized boolean cancel() {
        boolean was = expired;
        expired=true;
        return was;
      }
    };
    support.getAlarmService().addRealTimeAlarm(timer);
  }

  /**
   * Cancel the timer.
   **/
  protected void cancelTimer() {
    if (timer == null) return;
    if (support.getLog().isDebugEnabled()) 
      support.getLog().debug("Cancelling timer");
    timer.cancel();
    timer = null;
  }

  /**
   * Test if the timer has expired.
   * @return false if the timer is not running or has not yet expired
   * else return true.
   **/
  protected boolean timerExpired() {
    return timer != null && timer.hasExpired();
  }

  private Alarm timer;

  /** encodes a time interval in a min:sec:millis format */
  protected String getElapsedTime(long diff) {
    long min  = diff/60000l;
    long sec  = (diff - (min*60000l))/1000l;
    long millis = diff - (min*60000l) - (sec*1000l);
    return min + ":" + ((sec < 10) ? "0":"") + sec + ":" +
      ((millis < 10) ? "00": ((millis < 100) ? "0":"")) + millis;
  }

  // rely upon load-time introspection to set these services -
  //   don't worry about revokation.
  public final void setSchedulerService(SchedulerService ss) {
    scheduler = ss;
  }

  /**
   * Subscription to PlanElements disposing of our tasks
   **/
  private IncrementalSubscription planElementSubscription;

  /**
   * Subscription to PlanElements disposing of our tasks
   **/
  private IncrementalSubscription taskSubscription;

  /** for waiting for a subscription on the blackboard */
  protected SubscriptionWatcher watcher;
  /** for waiting for a subscription on the blackboard */
  protected TriggerModel tm;
  /** for waiting for a subscription on the blackboard */
  private SchedulerService scheduler;

  /** returned response */
  protected GLMStimulatorResponseData responseData=new GLMStimulatorResponseData();

  protected Object doneSignal = new Object ();
  protected int state = BEFORE_FIRST;
  protected long start, end;

  protected long firstInterval = 10l, secondInterval = 10l; // seconds
  protected boolean getImage = false;
  protected Set incompleteTasks = new HashSet();
  protected Collection verbsToInclude = new HashSet();

  protected CompletionWatcherSupport support;
}
