/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views.policy;

import org.cougaar.domain.mlm.ui.producers.policy.UIPolicyInfo;

/**
 * Interface for handling user changes in policy parameters,
 */
public interface PolicyClient {
  /** 
   * firePolicyChange - notification that policy has changed
   *
   * @param policyInfo UIPolicyInfo whose parameters have changed
   */
  public void firePolicyChange(UIPolicyInfo p);
}






