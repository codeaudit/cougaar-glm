/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 
package org.cougaar.mlm.ui.producers.policy;

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
   * org.cougaar.planning.ldm.policy.RuleParameter
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
   * @return Object parameter value. Note : could be null.
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










