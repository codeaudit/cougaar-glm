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
import java.util.Iterator;

import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

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
   * org.cougaar.domain.planning.ldm.policy.RuleParameter
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
   * @returns Object parameter value (String). Note : could be null.
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










