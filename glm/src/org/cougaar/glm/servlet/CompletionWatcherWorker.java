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
 * If either ... true, uses a blackboard
 * service, a watcher, and a trigger to monitor published tasks to see
 * when they are complete.
 * </pre>
 **/
public class CompletionWatcherWorker extends ServletWorker {
  private static final int BEFORE_FIRST  = 0;
  private static final int DURING_FIRST  = 1;
  private static final int END_OF_FIRST  = 2;
  private static final int AFTER_FIRST   = 3;
  private static final int DURING_SECOND = 4;
  private static final int AFTER_SECOND  = 5;

  /**
   * <pre>
   * Here is our inner class that will handle all HTTP and
   * HTTPS service requests.
   *
   * If we should wait until the batch is complete, we make a watcher to watch
   * the blackboard, a trigger that will call blackboardChanged (), and a trigger model
   * to connect them.
   *
   * </pre>
   * @see #blackboardChanged
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
            blackboardChanged(false);
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

  /**
   * Matches PlanElements for tasks that we have sent and which are
   * complete, i.e. have plan elements and 100% confident reported
   * allocation results. Both Failure and Success are accepted.
   **/
  private DynamicUnaryPredicate planElementPredicate = new DynamicUnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
        PlanElement pe = (PlanElement) o;
        Task task = pe.getTask();
        if (incompleteTasks.contains(task)) {
	    boolean hasReported = (pe.getReportedResult () != null);
	    boolean hasEstimated = (pe.getEstimatedResult () != null);
	    boolean highConfidence = false;

	    if (!hasReported && !hasEstimated)
	      return false;
	    
	    if (hasEstimated) {
	      highConfidence = (pe.getEstimatedResult ().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE);
	      if (!highConfidence)
		System.out.println ("CompletionWatcherWorker - Interested in task with low confidence estimated. " + task.getUID());
	    }
	    else if (hasReported) {
	      highConfidence = (pe.getReportedResult ().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE);
	      if (!highConfidence)
		System.out.println ("CompletionWatcherWorker - Interested in task with low confidence reported. " + task.getUID());
	    }
	    
	    return (!highConfidence);
	}
      }
      return false;
    }
  };

  /**
   * Subscription to PlanElements disposing of our tasks
   **/
  private IncrementalSubscription planElementSubscription;

  /**
   * Looking for NOT complete tasks -- when this number is zero, we're at a quiet point
   * 
   **/
  private DynamicUnaryPredicate taskPredicate = new DynamicUnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task task = (Task) o;

	return !(task.getVerb().equals(Constants.Verb.ReportForService));
      } 
      return false;
    }
  };

  // unused
  protected String getPrefix () { return "CompletionWatcher at "; }

  /**
   * <pre>
   * Use a query parameter to set a field
   *
   * Sets the recognized parameters : inputFile, debug, totalBatches, tasksPerBatch, interval, and wait
   * </pre>
   */
  public void getSettings(String name, String value) {
    super.getSettings (name, value);

    if (support.getLog().isDebugEnabled ())
      support.getLog().debug ("CompletionWatcherWorker.getSettings - name " + name + " value " + value);

    /*
    if (eq (name, CompletionWatcherServlet.INPUT_FILE))
      inputFile = value;
    else if (eq (name, "debug"))
      debug = eq (value, "true");
    else if (eq (name, CompletionWatcherServlet.NUM_BATCHES))
      totalBatches = Integer.parseInt(value);
    else if (eq (name, CompletionWatcherServlet.TASKS_PER_BATCH))
      tasksPerBatch = Integer.parseInt(value);
    else if (eq (name, CompletionWatcherServlet.INTERVAL)) {
      try {
        interval = Long.parseLong(value);
        if (interval < MIN_INTERVAL)
          interval = MIN_INTERVAL;
      } catch (Exception e) { interval = 1000l; }
    } else if (eq (name, CompletionWatcherServlet.WAIT_BEFORE)) {
      waitBefore = eq (value, "true");
    } else if (eq (name, CompletionWatcherServlet.WAIT_AFTER)) {
      waitAfter = eq (value, "true");
    } else if (eq (name, CompletionWatcherServlet.RESCIND_AFTER_COMPLETE)) {
      rescindAfterComplete = eq (value, "true");
    } else if (eq (name, CompletionWatcherServlet.USE_CONFIDENCE)) {
      useConfidence = eq (value, "true");
    }
    */
  }

  /**
   * Main work done here. <p>
   *
   * Sends the first batch of tasks, and keeps sending until totalBatches have been
   * sent.  If should wait for completion, waits until notified by blackboardChanged (). <p>
   *
   * Will wait <b>interval</b> milliseconds between batches if there are
   * more than one batches to send.
   *
   * @see #getSettings
   * @see #blackboardChanged
   * @return an "XMLable" result.
   */
  protected XMLable getResult() {
    while (!isDone()) {
      if (support.getLog().isDebugEnabled ())
	support.getLog().debug ("CompletionWatcherWorker.getResult - checking quiet...");

      blackboardChanged (true);

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
   * Called when one of the tasks that was added to the blackboard has
   * it's plan element change.
   * 
   **/
  protected void blackboardChanged(boolean calledInitially) {
    try {
      //      Thread.dumpStack();
      support.getBlackboardService().openTransaction();

      boolean haveChanged = support.getBlackboardService().haveCollectionsChanged();

      boolean planElementChanged = planElementSubscription.hasChanged();
      boolean taskChanged        = taskSubscription.hasChanged();

      if (taskChanged)
	incompleteTasks.addAll(taskSubscription.getAddedCollection ());

      if (planElementChanged) {
        Collection changedItems = planElementSubscription.getChangedCollection();
	for (Iterator iter = changedItems.iterator (); iter.hasNext();) {
	  PlanElement pe = (PlanElement) iter.next();
	  incompleteTasks.remove (pe.getTask());
	}
      }

      if (calledInitially ||
	  planElementChanged || 
	  (timerStarted() && timerExpired ())) {
	support.getLog ().debug ("blackboardChanged called - " + 
				 ((planElementChanged) ? " PE sub changed " : 
				  ((timerExpired() ? " timer expired" : " huh?"))));
	advanceState ();
      }
      
      /*
      else if (!taskSubscription.getAddedCollection().isEmpty() ||
	       !taskSubscription.getRemovedCollection().isEmpty() ||
	       !taskSubscription.getChangedCollection().isEmpty())
	support.getLog ().debug ("blackboardChanged - " + 
				 " added "   + taskSubscription.getAddedCollection().size() +
				 " removed " + taskSubscription.getRemovedCollection().size() +
				 " changed " + taskSubscription.getChangedCollection().size());
      */

    } catch (Exception exc) {}
    finally{
      support.getBlackboardService().closeTransaction();
    }
  }

  protected void advanceState () {
    boolean allComplete = incompleteTasks.isEmpty ();
    int before = state;
    changeState (timerExpired(), allComplete); 
    if (state != before)
      support.getLog ().debug ("advanceState - state before " + before + 
			       " timerExpired " + timerExpired () + 
			       " all tasks complete " + allComplete + 
			       " new state " + state + 
			       " incompleteTasks " + incompleteTasks.size());
    else {
      support.getLog ().debug ("advanceState - incompleteTasks is " + incompleteTasks.size() + 
			       " added "   + taskSubscription.getAddedCollection().size() +
			       " removed " + taskSubscription.getRemovedCollection().size() +
			       " changed " + taskSubscription.getChangedCollection().size());
    }

    if (isDone ()) {
      synchronized (doneSignal) {
	doneSignal.notify ();
      }
    }
  } 

  /*
  else if (!changed && !timerExpired())
  support.getLog ().debug ("advanceState - doing nothing, task sub hasn't changed && timer not expired.");
  else 
    support.getLog ().debug ("advanceState - doing nothing, since task sub hasn't changed.");
}
  */

  protected boolean isDone () { return (state == AFTER_SECOND); }

  protected void changeState (boolean waitedLongEnough, boolean allComplete) { 
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
      }
      else if (!allComplete) {
	state--;
	cancelTimer ();
      }
      break;
    case END_OF_FIRST : 
      if (!allComplete) {
	start = System.currentTimeMillis ();
	support.getLog().debug ("start is " + new Date(start));
	state++;
      }
      break;
    case AFTER_FIRST:
      if (allComplete) {
	state++;
	end = System.currentTimeMillis ();
	support.getLog().debug ("possible end is " + new Date(end));
	startSecondWait ();
      }
      break;
    case DURING_SECOND:
      if (waitedLongEnough) 
	state++;
      else if (!allComplete) {
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
   * Schedule a update wakeup after some interval of time
   * @param delay how long to delay before the timer expires.
   **/
  protected void startTimer(final long delay) {
    if (timer != null) return;  // update already scheduled
    if (support.getLog().isDebugEnabled()) 
      support.getLog().debug("Starting timer with delay " + delay);

    timer = new Alarm() {
      long expirationTime = System.currentTimeMillis() + delay;
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

  protected boolean timerStarted () { return (timer != null); }

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
  private IncrementalSubscription taskSubscription;

  /** for waiting for a subscription on the blackboard */
  protected SubscriptionWatcher watcher;
  /** for waiting for a subscription on the blackboard */
  protected TriggerModel tm;
  /** for waiting for a subscription on the blackboard */
  private SchedulerService scheduler;

  /** returned response */
  protected GLMStimulatorResponseData responseData=new GLMStimulatorResponseData();

  /** dump debug output if true */
  protected boolean debug = false;

  protected Object doneSignal = new Object ();
  protected int state = BEFORE_FIRST;
  protected long start, end;

  protected long firstInterval = 5000l, secondInterval = 10000l;
  protected Set incompleteTasks = new HashSet();

  protected CompletionWatcherSupport support;
}
