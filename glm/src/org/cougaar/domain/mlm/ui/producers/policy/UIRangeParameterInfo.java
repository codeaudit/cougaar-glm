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
import java.util.Iterator;

import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

public class UIRangeParameterInfo extends UIPolicyParameterInfo 
  implements SelfPrinter, java.io.Serializable {

  private ArrayList myRangeEntries = null;
  
  public UIRangeParameterInfo(String name, int type, Object value,
                              ArrayList rangeEntries) {
    super(name, type, value);
    setRangeEntries(rangeEntries);
  }

  public UIRangeParameterInfo() {
  }


  /**
   * @return ArrayList - All UIRangeEntryInfos
   */
  public ArrayList getRangeEntries() {
    return myRangeEntries;
  }

  /**
   * sets range entries
   * @param rangeEntries - ArrayList of UIRangeEntryInfos
   */
  public void setRangeEntries(ArrayList rangeEntries) {
    myRangeEntries = rangeEntries;
  }

  /**
   * @return int - the type of the policy parameter. Defined in 
   * org.cougaar.domain.planning.ldm.policy.RuleParameter
   */
  public void setType(int type) {
    if (type == RANGE_TYPE) {
      super.setType(type);
    } else {
      System.err.println("UIRangeParameterInfo.setType - invalid type " + 
                         type + " for " + getName());
    }
  }

  /**
   * Get parameter value keyed by int
   * If key fits into one of the defined ranges, return associated
   * value, otherwise return default value .
   * @returns Object parameter value. Note : could be null.
   */
  public Object getValue(int key)
  {
    Object value = getValue();
    for(int i = 0; i < myRangeEntries.size(); i++) {
      UIRangeEntryInfo range = (UIRangeEntryInfo)myRangeEntries.get(i);
      if ((range.getMin() <= key) &&
          (range.getMax() >= key)) {
        value = range.getValue();
        break;
      }
    }
    
    return value; 
  }

  /**
   * @return Object - the value of the policy parameter
   */
  public void setValue(Object value)
    throws UIParameterInfoIllegalValueException {
    super.setValue(value);
  }

  public void printContent(AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(myRangeEntries, "RangeEntries");
  }
}










