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

import org.cougaar.domain.planning.ldm.policy.RuleParameter;
import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

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
   * org.cougaar.domain.planning.ldm.policy.RuleParameter
   */
  public void setType(int type) {
    if ((type == INTEGER_TYPE) ||
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










