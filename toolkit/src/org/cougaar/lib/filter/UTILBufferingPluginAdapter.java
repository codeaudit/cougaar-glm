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

import java.util.List;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.QuiescenceReportService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.util.UTILPreference;
import org.cougaar.lib.util.UTILVerify;
import org.cougaar.planning.ldm.plan.Task;

/**
 * <pre>
 * Example implementation of a plugin that buffers tasks 
 * until a certain threshold is reached.  processTasks is then called.
 * allocators, expanders, and aggregators should all be derived from 
 * this instead of PluginAdapter; we can then turn on buffering at a higher
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
 * </pre>
 */
public abstract class UTILBufferingPluginAdapter extends UTILPluginAdapter
  implements UTILBufferingPlugin {

  protected QuiescenceReportService qService;
  protected AgentIdentificationService agentIDService;

  /**
   * <pre>
   * Start up the task buffering thread.
   *
   * Note that localSetup is called AFTER setupFilters, so 
   * getBufferingThread will not return null;
   *
   * IT IS CRUCIAL that this gets called with super () if this
   * is overridden!  Otherwise, plugin will be effectively dead. 
   * </pre>
   */
  public void localSetup() {
    super.localSetup ();

    // This is in setupSubscriptions -- no need to force to run again there....
    //    wakeUp ();
    if (logger.isDebugEnabled())
      logger.debug("Skipping call to wakeUp");

    verify = new UTILVerify (logger);
    prefHelper = new UTILPreference (logger);
  }

  /** 
   * Implemented for UTILBufferingPlugin
   *
   * OVERRIDE to specialize tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  public boolean interestingTask(Task t) {
    return true;
  }

  /** 
   * <pre>
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
   * </pre>
   * @param t Task to check for consistency
   * @return true if task is OK
   */
  public boolean isTaskWellFormed(Task t) {
    return true;
  }

  protected void reportIllFormedTask (Task t) {
    if (!verify.isTaskTimingCorrect(t)) 
      info (getName () + 
	    ".reportIllFormedTask - task " + t + 
	    " has " + verify.reportTimingError (t));  
    else if (!verify.hasDirectObject (t)) {
      info (getName () + 
	    ".reportIllFormedTask - task " + t.getUID () + 
	    " is missing direct object.");
    }
    else if (!verify.hasStartPreference (t)) {
      info (getName () + 
	    ".reportIllFormedTask - task " + t.getUID () + 
	    " is start time preference.");
    }
    else if (!verify.hasEndPreference (t)) {
      info (getName () + 
	    ".reportIllFormedTask - task " + t.getUID () + 
	    " is end time preference.");
    }
    else {
      info (getName () + 
	    ".reportIllFormedTask - task " + t + 
	    " was ill formed.  (start " + prefHelper.getReadyAt(t) + 
	    " e " + prefHelper.getEarlyDate(t) + 
	    " b " + prefHelper.getBestDate(t) + 
	    " l " + prefHelper.getLateDate(t) + 
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

    if (isDebugEnabled())
      debug (getName () + " creating buffering thread " + bufferingThread);

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
   * across the UTILBufferingPlugin interface.
   *
   * This plugin is NOT a workflow listener.
   *
   * @return UTILListeningBufferingThread with this as the BufferingPlugin
   * @see UTILBufferingPlugin
   * @see UTILListeningBufferingThread
   */
  protected UTILBufferingThread createBufferingThread () {
    return new UTILListeningBufferingThread (this, logger);
  }

  /** accessor */
  protected UTILBufferingThread getBufferingThread () { return bufferingThread; }

  /**
   * Implemented for UTILBufferingPlugin
   *
   * Deal with the tasks that we have accumulated.
   *
   * ABSTRACT, so derived plugins must implement this.
   *
   * @param tasks List of tasks to handle
   */
  public abstract void processTasks (List tasks);

  public void resume  () {
    super.resume ();

    if (isDebugEnabled())
      debug("In resume about to call wakeUp");

    wakeUp ();
  } 

  public void wakeUp () {
    if (isInfoEnabled())
      info (getName () + " wakeUp called.");
    
    examineBufferAgainIn (10l);
  }

  public final void setQuiescenceReportService (QuiescenceReportService qService) {
    this.qService = qService;
  }

  /** Buffering runnable wants to restart later */
  public void examineBufferAgainIn (long delayTime) {
    if (agentIDService == null) {
      agentIDService = (AgentIdentificationService) getServiceBroker().getService (this,
										   AgentIdentificationService.class,
										   new ServiceRevokedListener() {
										     public void serviceRevoked(ServiceRevokedEvent re) {
										       debug ("Agent id service revoked.");
										     }
										   }
										   );
      qService.setAgentIdentificationService(agentIDService);
    }

    // tell the world that we are busy
    qService.clearQuiescentState();

    if (isInfoEnabled())
      logger.info (getName () + " asking to be restarted in " + delayTime, new Throwable());

    if (currentAlarm != null)
      currentAlarm.cancel ();

    long absTime = System.currentTimeMillis()+delayTime;
    alarmService.addRealTimeAlarm (currentAlarm = new PluginAlarm (absTime));
  }

  /** lifted from PluginAdapter -- can't reuse it from there, sigh... */
  public class PluginAlarm implements Alarm {
    private long expiresAt;
    private boolean expired = false;
    public PluginAlarm (long expirationTime) {
      expiresAt = expirationTime;
    }
    public long getExpirationTime() { return expiresAt; }
    public synchronized void expire() {
      if (!expired) {
        expired = true;
        {
          org.cougaar.core.service.BlackboardService bbs = getBlackboardService();
          if (bbs != null) bbs.signalClientActivity();
        }
      }
    }
    public boolean hasExpired() { return expired; }
    public synchronized boolean cancel() {
      boolean was = expired;
      expired=true;
      return was;
    }
    public String toString() {
      return "<PluginAlarm "+expiresAt+
        (expired?"(Expired) ":" ")+
        "for "+UTILBufferingPluginAdapter.this.toString()+">";
    }
  }

  /** 
   * Called every time one of the filterCallback subscriptions
   * change.
   *
   * What the plugin does in response to a changed subscription.
   *
   * Directs the filterCallback with the changed subscription
   * to react to the change in some way.
   */
  protected void execute() {
    if (isDebugEnabled ())
      debug (getName () + " : cycle called (a subscription changed)");

    super.execute ();

    if (currentAlarm != null && currentAlarm.hasExpired()) {
      if (isDebugEnabled())
	debug("An alarm expired");
      bufferingThread.run();

      if (!bufferingThread.anyTasksLeft()) {
	// tell the world that we have completed
	if (isInfoEnabled())
	  info (getName () + " now quiescent.");

	//  FIXME: Could there be multiple alarms outstanding?
	// Shouldnt I clear quiescence based on the alarm firing, not some task count?
	qService.setQuiescentState();
      }
    }
  }

  protected UTILBufferingThread  bufferingThread = null;

  protected Schedulable schedulable; 

  protected UTILVerify verify;
  protected UTILPreference prefHelper;
  protected ThreadService threadService;
  protected PluginAlarm currentAlarm;
}
