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
 
package org.cougaar.domain.mlm.ui.producers.policy;

import org.cougaar.domain.planning.ldm.policy.KeyRuleParameterEntry;

import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

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










