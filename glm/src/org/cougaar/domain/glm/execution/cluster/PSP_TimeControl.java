/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.cluster;

import org.cougaar.core.cluster.ExecutionTimer;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.RuntimePSPException;
import java.io.IOException;
import org.cougaar.domain.glm.execution.common.*;

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
