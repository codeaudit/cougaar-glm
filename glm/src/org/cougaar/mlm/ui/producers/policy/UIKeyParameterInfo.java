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
 
package org.cougaar.mlm.ui.producers.policy;

import java.util.ArrayList;
import java.util.Iterator;

import org.cougaar.mlm.ui.util.AsciiPrinter;
import org.cougaar.mlm.ui.util.SelfPrinter;

public class UIKeyParameterInfo extends UIPolicyParameterInfo 
  implements SelfPrinter, java.io.Serializable {

  private ArrayList myKeyEntries = null;
  
  public UIKeyParameterInfo(String name, int type, Object value,
                            ArrayList keyEntries) {
    super(name, type, value);
    myKeyEntries = keyEntries;
  }

  public UIKeyParameterInfo() {
  }


  /**
   * @return ArrayList - Set of key entries.
   * @see UIKeyEntryInfo
   */
  public ArrayList getKeyEntries() {
    return myKeyEntries;
  }

  /**
   * @param keyEntries - ArrayList of UIKeyEntryInfo
   */
  public void setKeyEntries(ArrayList keyEntries) {
    myKeyEntries = keyEntries;
  }

  /**
   * @return int - the type of the policy parameter. Defined in 
   * org.cougaar.planning.ldm.policy.RuleParameter
   */
  public void setType(int type) {
    if (type == KEY_TYPE) {
      super.setType(type);
    } else {
      System.err.println("UIKeyParameterInfo.setType - invalid type " + 
                         type + " for " + getName());
    }
  }

  /**
   * Get parameter value (String) keyed by int
   * If key fits into one of the defined Keys, return associated
   * value, otherwise return default value (String).
   * @return Object parameter value (String). Note : could be null.
   */
  public Object getValue(String key)
  {
    String value = (String)getValue();
    for(int i = 0; i < myKeyEntries.size(); i++) {
      UIKeyEntryInfo entry= (UIKeyEntryInfo)myKeyEntries.get(i);
      if (entry.getKey().equals(key)) {
        value = entry.getValue();
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

    if (value instanceof String) {
      super.setValue((String)value);
    } else {
      super.setValue(value.toString());
    }
  }

  public void printContent(AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(myKeyEntries, "KeyEntries");
  }
}










