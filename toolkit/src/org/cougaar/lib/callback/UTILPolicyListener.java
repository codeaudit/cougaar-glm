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

package org.cougaar.lib.callback;

import java.util.Enumeration;

import org.cougaar.planning.ldm.policy.Policy;

/**
 * Listener for use with Workflow and ExpandableTask Callbacks.
 * Doesn't require steps of SingleTaskWorkflowListener.
 */

public interface UTILPolicyListener extends UTILFilterCallbackListener {
  /** 
   * Defines policies you find interesting. 
   * @param p Policy to check for interest
   * @return true if task is interesting
   */
  boolean interestingPolicy(Policy p);

  /** 
   * Got an interesting policy, now handle it in some way
   * @param np new Policies
   */
  void handleNewPolicies(Enumeration np);

  /**
   * An interesting policy has changed.  Do something.
   */
  void handleChangedPolicies(Enumeration cp);
}
