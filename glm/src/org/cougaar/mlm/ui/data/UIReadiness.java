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

/** Serializable object sent from Readiness PSP to stoplight display.
  */

package org.cougaar.mlm.ui.data;

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
