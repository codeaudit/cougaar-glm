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

import org.cougaar.domain.planning.ldm.policy.KeyRuleParameterEntry;

import org.cougaar.util.AsciiPrinter;
import org.cougaar.util.SelfPrinter;

public class UIKeyEntryInfo implements SelfPrinter, java.io.Serializable {

  private String myValue = null;
  private String myKey = null;
  
  public UIKeyEntryInfo(KeyRuleParameterEntry entry) {
    myValue = entry.getValue();
    myKey = entry.getKey();
  }

  public UIKeyEntryInfo(String value, String key) {
    myValue = value;
    myKey = key;
  }

  public UIKeyEntryInfo() {
  }


  /**
   * @return String - get the value
   */
  public String getValue() {
    return myValue;
  }

  /**
   * @param value - set the value
   */
  public void setValue(String value) {
    myValue = value;
  }

  /**
   * @return String - get the key
   */
  public String getKey() {
    return myKey;
  }

  /**
   * @param key - set the key
   */
  public void setKey(String key) {
    myKey = key;
  }

  public void printContent(AsciiPrinter pr) {
    pr.print(myValue, "Value");
    pr.print(myKey, "Key");
  }
}










