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
import java.util.Arrays;

import org.cougaar.planning.ldm.policy.RuleParameter;
import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

public class UIEnumerationParameterInfo extends UIPolicyParameterInfo 
  implements SelfPrinter, java.io.Serializable {

  private ArrayList myEnumeration = null;
  
  public UIEnumerationParameterInfo(String name, int type, Object value, 
                                    ArrayList enumeration) {
    super(name, type, value);
    myEnumeration = enumeration;
  }

  public UIEnumerationParameterInfo() {
  }


  /**
   * @return String - the name of the policy parameter
   */
  public ArrayList getEnumeration() {
    return myEnumeration;
  }

  /**
   * @return String - the name of the policy parameter
   */
  public void setEnumeration(ArrayList enumeration) {
    myEnumeration = enumeration;
  }

  /**
   * @return int - the type of the policy parameter. Defined in 
   * org.cougaar.planning.ldm.policy.RuleParameter
   */
  public void setType(int type) {
    if (type == ENUMERATION_TYPE) {
      super.setType(type);
    } else {
      System.err.println("UIEnumerationParameterInfo.setType - invalid type " + 
                         type + " for " + getName());
    }
  }

  /**
   * @return Object - the value of the policy parameter
   */
  public void setValue(Object value)
    throws UIParameterInfoIllegalValueException {
    boolean success = false;

    switch (getType()) {
    case ENUMERATION_TYPE:
      if (myEnumeration == null) {
        success = true;
      } else {
        String stringValue;
        if (value instanceof String) {
          stringValue = (String)value;
        } else {
          stringValue = value.toString();
        }
        
        // BOZO - move to isValid
        for (int i = 0; i < myEnumeration.size(); i++) {
          if (myEnumeration.get(i).equals(stringValue)) {
            success = true;
            break;
          }
        }
        
        if (!success) {
          System.err.println("UIEnumerationParameterException.setValue - " + value +
                             " not within bounds.");
        }
      }

      break;

    default:
      System.err.println("UIEnumerationParameterException.setValue - " + 
                         " unrecognized parameter type " + getType());
      success = false;
    }
      
    if (success) {
      super.setValue(value);
    } else {
      String text = value + " not convertable to one of the following\n";
      for (int i = 0; i < myEnumeration.size(); i++) {
        text = text + "    " + myEnumeration.get(i).toString() + "\n";
      }

      UIParameterInfoIllegalValueException piive = 
        new UIParameterInfoIllegalValueException(text);
      throw(piive);
    }
    

  }

  public void printContent(AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(myEnumeration, "Enumeration");
  }

  public String validDataInfo() {
    String text = 
      "One of the following - ";
    
    for (int i = 0; i < myEnumeration.size(); i++) {
      if (i == 0) {
        text = text + myEnumeration.get(i).toString();
      } else {
        text = text + "," + myEnumeration.get(i).toString();
      }
    }

    return text;
  }
}









