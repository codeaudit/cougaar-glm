/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers.policy;

import java.util.ArrayList;

import org.cougaar.domain.planning.ldm.policy.RangeRuleParameterEntry;
import org.cougaar.domain.planning.ldm.policy.RuleParameter;

import org.cougaar.util.AsciiPrinter;
import org.cougaar.util.SelfPrinter;

public class UIRangeEntryInfo implements SelfPrinter, java.io.Serializable {

  private String myValue = null;
  private int myMin = Integer.MIN_VALUE;
  private int myMax = Integer.MAX_VALUE;
  
  public UIRangeEntryInfo(RangeRuleParameterEntry entry) {
    myValue = entry.getValue();
    myMin = entry.getRangeMin();
    myMax = entry.getRangeMax();
  }

  public UIRangeEntryInfo(String value, int min, int max) {
    myValue = value;
    myMin = min;
    myMax = max;
  }

  public UIRangeEntryInfo() {
  }


  /**
   * @return String - get the value
   */
  public String getValue() {
    return myValue;
  }

  /**
   * @param String  - set the value
   */
  public void setValue(String value) {
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










