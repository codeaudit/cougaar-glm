/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

/** Serializable object sent from Readiness PSP to stoplight display.
  */

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;

public class UIReadiness implements Serializable {
  Vector societyReadiness; // vector of UIUnitReadiness

  public UIReadiness() {
    societyReadiness = new Vector();
  }

  public UIReadiness(Vector societyReadiness) {
    this.societyReadiness = societyReadiness;
  }

  public void addUnitReadiness(UIUnitReadiness readiness) {
    societyReadiness.addElement(readiness);
  }

  public Vector getSocietyReadiness() {
    return societyReadiness;
  }

}
