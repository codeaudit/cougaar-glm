/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.domain.planning.ldm.policy.Policy;

import java.util.Enumeration;

/**
 * Listener for use with Workflow and ExpandableTask Callbacks.
 * Doesn't require steps of SingleTaskWorkflowListener.
 */

public interface UTILPolicyListener extends UTILFilterCallbackListener {
  /** 
   * Defines policies you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingPolicy(Policy p);

  /** 
   * Got an interesting policy, now handle it in some way
   * @param t Task to handle
   */
  void handleNewPolicies(Enumeration np);

  /**
   * An interesting policy has changed.  Do something.
   */
  void handleChangedPolicies(Enumeration cp);
}
