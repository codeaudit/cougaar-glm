/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

public class UIPropertyNameValue {
  String name;
  Object value;

  public UIPropertyNameValue(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public String getUIPropertyName() {
    return name;
  }

  public Object getUIPropertyValue() {
    return value;
  }

}
