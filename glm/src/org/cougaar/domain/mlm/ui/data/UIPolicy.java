/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

public interface UIPolicy extends UIUniqueObject {
   
  /**
   * @return String - the name of the policy
   */
  String getPolicyName();

  /**
   * @return UIPolicyParameter - names and values of policy rule parameters
   */

  UIPolicyParameter[] getPolicyParameters();
}

