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
package org.cougaar.glm.execution.cluster;

import org.cougaar.core.agent.service.alarm.ExecutionTimer;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.RuntimePSPException;
import java.io.IOException;
import org.cougaar.glm.execution.common.*;

/**
 * Receives clock control messages from the EventGenerator and sets
 * the execution clock accordingly. Acknowledgement is performed
 * implicitly by the ConnectionAcknowledgement.
 **/
public class PSP_TimeControl
  extends PSP_Base
  implements PlanServiceProvider
{
  public PSP_TimeControl() throws RuntimePSPException {
    super();
  }

  public PSP_TimeControl(String pkg, String id) throws RuntimePSPException {
    super(pkg, id);
  }

  public PlanServiceProvider pspClone() throws RuntimePSPException {
    return new PSP_TimeControl();
  }

  private static final long CHANGE_DELAY = 5000L;
  private static final long DEFAULT_TRANSITION_INTERVAL = 25000L;
  private static final long MIN_TRANSITION_INTERVAL = 10000L;

  protected Context createContext() {
    return new MyContext();
  }

  protected static class MyContext extends Context {
  protected void execute() throws IOException, InterruptedException {
    try {
      SetExecutionTime params = (SetExecutionTime) reader.readEGObject();
      ExecutionTimer.Change[] changes;
      long now = System.currentTimeMillis();
      long transitionInterval = DEFAULT_TRANSITION_INTERVAL;
      if (params.theTransitionInterval > 0L) {
        transitionInterval = Math.max(MIN_TRANSITION_INTERVAL, params.theTransitionInterval);
      }
      if (params.timeIsAbsolute) {
        // Absolute changes are problematic because smooth change must
        // be relative. So we get as close as we can with relative
        // changes and ignore the discrepancy. The discrepancy depends
        // on the current execution rate and the delay between reading
        // the current execution time and applying the changes. The
        // delay is ordinarily brief unless this thread gets
        // preempted.
        long theDelta = params.theTime - cluster.currentTimeMillis();
        double theTransitionRate =  theDelta * 1.0 / transitionInterval;
        changes = new ExecutionTimer.Change[] {
          new ExecutionTimer.Change(theTransitionRate, 0L, CHANGE_DELAY),
          new ExecutionTimer.Change(params.theExecutionRate, 0L, transitionInterval),
        };
      } else if (params.theTime > 0L) {
        long theDelta = params.theTime;
        double theTransitionRate =  theDelta * 1.0 / transitionInterval;
        changes = new ExecutionTimer.Change[] {
          new ExecutionTimer.Change(theTransitionRate, 0L, CHANGE_DELAY),
          new ExecutionTimer.Change(params.theExecutionRate, 0L, transitionInterval),
        };
      } else {
        changes = new ExecutionTimer.Change[] {
          new ExecutionTimer.Change(params.theExecutionRate, 0L, CHANGE_DELAY),
        };
      }
      cluster.advanceTime(changes);
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
  }
}
}
