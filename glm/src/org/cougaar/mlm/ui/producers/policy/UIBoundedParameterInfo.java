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

import org.cougaar.planning.ldm.policy.RuleParameter;
import org.cougaar.mlm.ui.util.AsciiPrinter;
import org.cougaar.mlm.ui.util.SelfPrinter;

public class UIBoundedParameterInfo extends UIPolicyParameterInfo 
  implements SelfPrinter, java.io.Serializable {

  private Number myMin;
  private Number myMax;

  
  public UIBoundedParameterInfo(String name, int type, Object value, 
                               Number min, Number max) {
    super(name, type, value);
    myMin = min;
    myMax = max;
  }

  public UIBoundedParameterInfo() {
  }


  /**
   * @return String - the name of the policy parameter
   */
  public Number getMax() {
    return myMax;
  }

  /**
   * @return String - the name of the policy parameter
   */
  public void setMax(Number max) {
    myMax = max;
  }

  /**
   * @return String - the name of the policy parameter
   */
  public Number getMin() {
    return myMin;
  }

  /**
   * @return String - the name of the policy parameter
   */
  public void setMin(Number min) {
    myMin = min;
  }


  /**
   * @return int - the type of the policy parameter. Defined in 
   * org.cougaar.planning.ldm.policy.RuleParameter
   */
  public void setType(int type) {
    if ((type == INTEGER_TYPE) ||
        (type == LONG_TYPE) ||
        (type == DOUBLE_TYPE)) {
      super.setType(type);
    } else {
      System.err.println("UIBoundedParameterInfo.setType - invalid type " + 
                         type + " for " + getName());
    }
  }

  /**
   * @return Object - the value of the policy parameter
   */
  public void setValue(Object value) 
    throws UIParameterInfoIllegalValueException {
    boolean success = true;

    switch (getType()) {
    case INTEGER_TYPE:
      if ((myMin != null) &&
          (myMax != null)) {
        int intVal = 0;
        if (value instanceof Integer) {
          intVal = ((Integer)value).intValue();
        } else {
          try {
            intVal = new Integer(value.toString()).intValue();
          } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            success = false;
          }
        }
        
        if ((intVal < myMin.intValue()) ||
            (intVal > myMax.intValue())) {
          success = false;
        }
      }

      break;

    case LONG_TYPE:
      if ((myMin != null) &&
          (myMax != null)) {
        long longVal = 0;
        if (value instanceof Long) {
          longVal = ((Long)value).longValue();
        } else {
          try {
            longVal = new Long(value.toString()).longValue();
          } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            success = false;
          }
        }
        
        if ((longVal < myMin.longValue()) ||
            (longVal > myMax.longValue())) {
          success = false;
        }
      }

      break;

    case DOUBLE_TYPE:
      if ((myMin != null) &&
          (myMax != null)) {
        double doubleVal = 0;
        if (value instanceof Double) {
          doubleVal = ((Double)value).doubleValue();
        } else {
          try {
            doubleVal = new Double(value.toString()).doubleValue();
          } catch (NumberFormatException nfe) {
            success = false;
          }
        }
        
        if ((doubleVal < myMin.doubleValue()) ||
            (doubleVal > myMax.doubleValue())) {
          success = false;
        }
      } 
      break;

    default:
      success = false;
    }
      
    if (success) {
      super.setValue(value);
    } else {
      UIParameterInfoIllegalValueException piive = 
        new UIParameterInfoIllegalValueException(value +
                                                 " not convertable to a " + 
                                                 convertToString(getType()) + 
                                                 " between " + getMin() + 
                                                 " and " + getMax());
      throw(piive);
    }
  }

  public void printContent(AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(myMin, "Min");
    pr.print(myMax, "Max");
  }

  public String validDataInfo() {

    return super.validDataInfo() + " between " + 
      getMin() + " and " + getMax();
  }
}










