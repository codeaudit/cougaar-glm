/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.util.Vector;

public class HashElem implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private String key;
  private Vector values;

  public HashElem() {}

  public String getKey() {return key;}
  public void setKey(String k) {
    key = k;
  }

  public Vector getValues() {return values;}
  public void setValues(Vector v) {
    values = v;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(key, "Key");
    pr.print(values, "Values");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

}
