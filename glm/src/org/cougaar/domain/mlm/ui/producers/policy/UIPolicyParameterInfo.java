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

import org.cougaar.util.UnaryPredicate;


public class UIPolicyParameterInfo implements SelfPrinter, java.io.Serializable {
  //PSP_PolicyEditor responsible for conversion from RuleParameter types to
  // UIPolicyParameterInfo types.
  public static final int STRING_TYPE = 0;
  public static final int INTEGER_TYPE = 1;
  public static final int DOUBLE_TYPE = 2;
  public static final int ENUMERATION_TYPE = 3;
  public static final int BOOLEAN_TYPE = 4;
  public static final int CLASS_TYPE = 5;
  public static final int RANGE_TYPE = 6;
  public static final int KEY_TYPE = 7;
  public static final int LONG_TYPE = 8;
  public static final int PREDICATE_TYPE = 9;
  
  // Remember to update if add/removing types
  private static final int MIN_TYPE = STRING_TYPE;
  private static final int MAX_TYPE = PREDICATE_TYPE; 
  

  private String myName = null;
  private int myType = -1;
  private Object myValue = null;
  private boolean myEditable = false;

  public static String convertToString(int type) {

    switch (type) {
    case INTEGER_TYPE:
      return "Integer";
      
    case LONG_TYPE:
      return "Long";
      
    case DOUBLE_TYPE:
      return "Double";

    case STRING_TYPE:
      return "String";
      
    case ENUMERATION_TYPE:
      return "Enumeration";
      
    case BOOLEAN_TYPE:
      return "Boolean";
      
    case CLASS_TYPE: 
      return "Class";

    case RANGE_TYPE: 
      return "Range";

    case KEY_TYPE: 
      return "Key";

    case PREDICATE_TYPE: 
      return "UnaryPredicate";
    
    default:
      return "Invalid type";
    }
  }

  public UIPolicyParameterInfo(String name, int type, Object value) {
    setName(name);
    setType(type);
    setValue(value);

    myEditable = defaultEditable();
  }

  public UIPolicyParameterInfo() {
  }


  /**
   * @return String - the name of the policy parameter
   */
  public String getName() {
    return myName;
  }

  /**
   * @return String - the name of the policy parameter
   */
  public void setName(String name) {
    myName = name;
  }

  /**
   * @return int - the type of the policy parameter. Defined in 
   * org.cougaar.domain.planning.ldm.policy.RuleParameter
   */
  public int getType() {
    return myType;
  }

  /**
   * @return int - the type of the policy parameter. Defined in 
   * org.cougaar.domain.planning.ldm.policy.RuleParameter
   */
  public void setType(int type) {
    if ((type >= MIN_TYPE) &&
        (type <= MAX_TYPE)) {
      myType = type;
    } else {
      System.err.println("UIPolicyParameterInfo.setType - invalid type " + 
                         type + " for " + getName());
    }
  }

  /**
   * @return Object - the value of the policy parameter
   */
  public Object getValue() {
    return myValue;
  }

  /**
   * @return Object - the value of the policy parameter
   */
  public void setValue(Object value) 
    throws UIParameterInfoIllegalValueException {
    boolean success = true;

    switch (getType()) {
    case STRING_TYPE:
      if (value instanceof String) {
        myValue = value;
      } else {
        myValue = value.toString();
      }

      break;

    case INTEGER_TYPE:
      if (value instanceof Integer) {
        myValue = value;
      } else {
        try {
          myValue = new Integer(value.toString());
        } catch (NumberFormatException nfe) {
          nfe.printStackTrace();
          success = false;
        }
      }
      
      break;

    case LONG_TYPE:
      if (value instanceof Long) {
        myValue = value;
      } else {
        try {
          myValue = new Long(value.toString());
        } catch (NumberFormatException nfe) {
          nfe.printStackTrace();
          success = false;
        }
      }
      
      break;

    case DOUBLE_TYPE:
      if (value instanceof Double) {
        myValue = value;
      } else {
        try {
          myValue = new Double(value.toString());
        } catch (NumberFormatException nfe) {
          System.err.println("UIPolicyParameterException.setType - " + value +
                             " not convertable to double.");
          nfe.printStackTrace();
          success = false;
        }
      }
      
      break;


    case BOOLEAN_TYPE:
      if (value instanceof Boolean) {
        myValue = value;
        break;
      } else if (value instanceof String) {
        String valueStr = (String)value;

        if (valueStr.equals(Boolean.TRUE.toString())) {
          myValue = Boolean.TRUE;
          break;
        } else if (valueStr.equals(Boolean.FALSE.toString())) {
          myValue = Boolean.FALSE;
          break;
        }
      }

      success = false;
      break;

    case CLASS_TYPE:
      if (value instanceof Class) {
        myValue = value;
      } else {
        try {
          myValue = Class.forName(value.toString());
        } catch (ClassNotFoundException cnfe) {
          //cnfe.printStackTrace();
          myValue = value.toString();
        }
      }

      break;

    case PREDICATE_TYPE:
      if (value instanceof UnaryPredicate) {
        myValue = value.getClass();
      } else {
        try {
          myValue = Class.forName(value.toString());
        } catch (ClassNotFoundException cnfe) {
          //cnfe.printStackTrace();
          myValue = value.toString();
        }
      }

      break;

    default:
      myValue = value;
      success = true;
    }
      
    if (!success) {
      UIParameterInfoIllegalValueException piive = 
        new UIParameterInfoIllegalValueException(value +
                                                 " not convertable to a " + 
                                                 convertToString(myType));
      throw(piive);
    }
  }


  public boolean getEditable() {
    return myEditable;
  }

  public void setEditable(boolean editable) {
    myEditable = editable;
  }

  public String validDataInfo() {
    String typeString;

    return convertToString(getType());
  }

  public void printContent(AsciiPrinter pr) {
    pr.print(myName, "Name");
    pr.print(myType, "Type");
    pr.print(myValue, "Value");
    pr.print(myEditable, "Editable");
  }

  protected boolean defaultEditable() {
    return ((myType != CLASS_TYPE) &&
            (myType != PREDICATE_TYPE));
  }
}

