/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.plugin.sample;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.ExpanderHelper;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.UnaryPredicate;

public class DetermineRequirementsAbsorberPlugin extends SimplePlugin {

  /** Subscription to DetermineRequirement Tasks **/
  protected IncrementalSubscription drTasksSub;
  protected IncrementalSubscription maintainInventorySub;

  protected Map timers = new HashMap();

  private Alarm alarm;

  private static UnaryPredicate drTasks = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task t = (Task) o;
          return Constants.Verb.DetermineRequirements.equals(t.getVerb());
        }
        return false;
      }
    };

  private static UnaryPredicate maintainInventoryExpansions = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Expansion) {
          Expansion exp = (Expansion) o;
          Task t = exp.getTask();
          return Constants.Verb.MaintainInventory.equals(t.getVerb());
        }
        return false;
      }
    };

  /**
   * Subscribe.
   */
  protected void setupSubscriptions() {
    logger = LoggingServiceWithPrefix.add(logger, getMessageAddress() + ": ");

    if (logger.isDebugEnabled()) {
      logger.debug(getClass()+" setting up subscriptions");
    }
    drTasksSub = (IncrementalSubscription) subscribe(drTasks);
    maintainInventorySub = (IncrementalSubscription) subscribe(maintainInventoryExpansions);
  }        

  protected long checkDRTasks(Enumeration e, long now) {
    long wakeTime = Long.MAX_VALUE;
    while (e.hasMoreElements()) {
      Task drTask = (Task) e.nextElement();
      if (drTask.getPlanElement() == null) {
        Long timer = (Long) timers.get(drTask);
        long expiry = now + 120000;
        if (timer == null) {
          if (logger.isInfoEnabled()) logger.info("Setting timer for undisposed drTask=" + drTask);
          timers.put(drTask, new Long(expiry));
        } else {
          expiry = timer.longValue();
          if (expiry > now) {
            if (logger.isDebugEnabled()) logger.debug("Still waiting for drTask=" + drTask);
            wakeTime = Math.min(wakeTime, expiry);
          }
        }
      } else {
        if (timers.remove(drTask) != null) {
          if (logger.isInfoEnabled()) logger.info("Has been disposed drTask=" + drTask);
        }
      }
    }
    return wakeTime;
  }

  protected void expireDRTask(Task drTask) {
    if (drTask.getPlanElement() != null) {
      if (logger.isDebugEnabled()) logger.debug("Has been disposed drTask=" + drTask);
      return;
    }
    if (logger.isDebugEnabled()) logger.debug("Expired timer for undisposed drTask=" + drTask);
    AllocationResult ar =
      PluginHelper.createEstimatedAllocationResult(drTask, theLDMF, 1.0, true);
    Disposition disposition =
      theLDMF.createDisposition(drTask.getPlan(), drTask, ar);
    publishAdd(disposition);
    String ofType = ExpanderHelper.getOfType(drTask, Constants.Preposition.OFTYPE);
    logger.shout("Disposed DetermineRequirements OfType " + ofType);
  }

  private boolean isUnconfidentEmptyWorkflow(Expansion exp) {
    if (exp.getWorkflow().getTasks().hasMoreElements()) return false;
    AllocationResult ar = exp.getEstimatedResult();
    if (ar == null) return true;
    return !(ar.getConfidenceRating() > .89999);
  }

  protected long checkMaintainInventoryExpansions(Enumeration e, long now) {
    long wakeTime = Long.MAX_VALUE;
    while (e.hasMoreElements()) {
      Expansion exp = (Expansion) e.nextElement();
      if (isUnconfidentEmptyWorkflow(exp)) {
        Long timer = (Long) timers.get(exp);
        long expiry = now + 120000;
        if (timer == null) {
          if (logger.isInfoEnabled()) logger.info("Setting timer for empty MaintainInventory expansion=" + exp);
          timers.put(exp, new Long(expiry));
        } else {
          expiry = timer.longValue();
          if (expiry > now) {
            if (logger.isDebugEnabled()) logger.debug("Still waiting for expansion=" + exp);
            wakeTime = Math.min(wakeTime, expiry);
          }
        }
      } else {
        if (timers.remove(exp) != null) {
          if (logger.isInfoEnabled()) logger.info("Has subtasks or confident result, expansion=" + exp);
        }
      }
    }
    return wakeTime;
  }

  protected void expireMaintainInventoryExpansion(Expansion exp) {
    if (logger.isDebugEnabled()) logger.debug("Expired timer for empty MaintainInventory expansion=" + exp);
    if (isUnconfidentEmptyWorkflow(exp)) {
      AllocationResult ar =
        PluginHelper.createEstimatedAllocationResult(exp.getTask(), theLDMF, 1.0, true);
      exp.setEstimatedResult(ar);
      publishChange(exp);
      logger.shout("Changed Estimated Result of MaintainInventory expansion=" + exp);
    } else if (logger.isDebugEnabled()) {
      AllocationResult ar = exp.getEstimatedResult();
      if (ar == null) {
        logger.debug("Acceptable confidence with subtasks");
      } else {
        logger.debug("Acceptable confidence=" + ar.getConfidenceRating());
      }
    }
  }

  /**
   * Execute.
   */
  protected void execute() {
    long now = System.currentTimeMillis();
    long wakeTime = now + 60000L;
    if (maintainInventorySub.hasChanged()) {
      wakeTime =
        Math.min(wakeTime,
                 checkMaintainInventoryExpansions(maintainInventorySub.getAddedList(), now));
      wakeTime =
        Math.min(wakeTime,
                 checkMaintainInventoryExpansions(maintainInventorySub.getChangedList(), now));
    }
    if (drTasksSub.hasChanged()) {
      wakeTime =
        Math.min(wakeTime,
                 checkDRTasks(drTasksSub.getAddedList(), now));
    }
    if (alarm != null && alarm.hasExpired()) {
      alarm = null;
      for (Iterator i = timers.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry entry = (Map.Entry) i.next();
        Object key = entry.getKey();
        Long timer = (Long) entry.getValue();
        long expiry = timer.longValue();
        if (expiry <= now) {
          if (key instanceof Task) {
            expireDRTask((Task) key);
          } else if (key instanceof Expansion) {
            expireMaintainInventoryExpansion((Expansion) key);
          }
          i.remove();
        }
      }
    }
    if (alarm == null) {
      if (timers.size() > 0) {
        if (logger.isDebugEnabled()) logger.debug("Waiting for " + (wakeTime - now) + " millis");
        alarm = wakeAtRealTime(wakeTime);
      } else {
        if (logger.isInfoEnabled()) logger.info("No more timers -- alarm not set");
      }
    } else {
      if (logger.isDebugEnabled()) logger.debug("Timer still active");
    }
  }

  /** rely upon load-time introspection to set these services - don't worry about revokation. */
  public final void setLoggingService (LoggingService logger) { this.logger = logger; }

  /**
   * Everybody needs a logger
   **/
  protected LoggingService logger;
}
