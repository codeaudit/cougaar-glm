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

package org.cougaar.lib.param;

/**
 * Base class that encapsulates a simple parameter.  A parameter is 
 * is a [name, type, value] triplet.  This class encapsulates both 
 * the name and the type information. The value of the
 * parameter is not specified since values may be of different types.
 * Extend this class and redefine the appropiate getXXXValue() routine
 * to add the value of the parameter.
 *
 */
public class Param {

  /**
   * Constructor
   * @param n name of the parameter
   * @param t type of the parameter
   */
  Param (String n, String t) {
    this.name = n;
    this.type = t;
  }

  /**
   * Get accessor for the name of a parameter
   * @return the name of the parameter.
   */
  public String getName() { 
    return name; 
  }
  
  /**
   * Get accessor the the type of a parameter
   * @return the type of the parameter
   */
  String getType() { 
    return type; 
  }
  
  /**
   * Get accessor for the boolean value of a parameter.
   * Override if needed.
   * @return the boolean value of the parameter.
   */
  boolean getBooleanValue() throws ParamException{ 
    throw new ParamException("parameter " + name + " is not of type boolean");
  }
  
  /**
   * Get accessor for the short value of a parameter.
   * Override if needed.
   * @return the short value of the parameter.
   */
  short getShortValue() throws ParamException{ 
    throw new ParamException("parameter " + name + "  is not of type short");
  }
  
  /**
   * Get accessor for the int value of a parameter.
   * Override if needed.
   * @return the int value of the parameter.
   */
  int getIntValue() throws ParamException{ 
    throw new ParamException("parameter " + name + "  is not of type int");
  }
  
  /**
   * Get accessor for the long value of a parameter.
   * Override if needed.
   * @return the long value of the parameter.
   */
  long getLongValue() throws ParamException{ 
    throw new ParamException("parameter " + name + "  is not of type long");
  }
  
  /**
   * Get accessor for the float value of a parameter.
   * Override if needed.
   * @return the float value of the parameter.
   */
  float getFloatValue() throws ParamException{ 
    throw new ParamException("parameter " + name + "  is not of type float");
  }
  
  /**
   * Get accessor for the double value of a parameter.
   * Override if needed.
   * @return the double value of the parameter.
   */
  double getDoubleValue() throws ParamException{ 
    throw new ParamException("parameter " + name + "  is not of type double");
  }
  
  /**
   * Get accessor for the String value of a parameter.
   * Override if needed.
   *
   * public because LdmXMLPlugin just wants to read from params from
   * ini file.
   * @return the String value of the parameter.
   */
  public String getStringValue() throws ParamException{ 
    throw new ParamException("parameter " + name + "  is not of type String");
  }
  
  private String name;
  private String type;
}

