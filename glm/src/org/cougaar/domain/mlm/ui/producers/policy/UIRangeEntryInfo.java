/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers.policy;

import java.util.ArrayList;

import org.cougaar.domain.planning.ldm.policy.RangeRuleParameterEntry;
import org.cougaar.domain.planning.ldm.policy.RuleParameter;

import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

public class UIRangeEntryInfo implements SelfPrinter, java.io.Serializable {

  private Object myValue = null;
  private int myMin = Integer.MIN_VALUE;
  private int myMax = Integer.MAX_VALUE;

  /*
  public UIRangeEntryInfo(UIPolicyParameterInfo entry) {
    myValue = entry.getValue();
    myMin = entry.getRangeMin();
    myMax = entry.getRangeMax();
  }
  */
  public UIRangeEntryInfo(Object value, int min, int max) {
    myValue = value;
    myMin = min;
    myMax = max;
  }

  public UIRangeEntryInfo() {
  }


  /**
   * @return Object - get the value
   */
  public Object getValue() {
    return myValue;
  }

  /**
   * @param Object  - set the value
   */
  public void setValue(Object value) {
    myValue = value;
  }

  /**
   * @return int - returns the max
   */
  public int getMax() {
    return myMax;
  }

  /**
   * @param int - sets the max
   */
  public void setMax(int max) {
    myMax = max;
  }

  /**
   * @return int - returns the min
   */
  public int getMin() {
    return myMin;
  }

  /**
   * @param int - sets the min
   */
  public void setMin(int min) {
    myMin = min;
  }

  public void printContent(AsciiPrinter pr) {
    pr.print(myValue, "Value");
    pr.print(myMin, "Min");
    pr.print(myMax, "Max");
  }
}










