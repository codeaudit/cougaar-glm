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
 * <pre>
 * Interface for a parameter map.
 * Supports [name, org.cougaar.lib.param.ParamTable] pairs.  
 *
 * If a parameter does not exist, a ParamException will be thrown. 
 * This is not a RuntimeException,
 * so clients can use the try{}catch{} trick.  The catch block is a 
 * good place to put "default" values for parameters.
 * </pre>
 */
public interface ParamMap {
  /**
   * Add a parameter into the parameter table.
   * @param n name of the paramter
   * @param p the parameter to add
   */
  void addParam(String n, Param p);
  
  /**
   * Get the value of a boolean parameter.
   * @param name of the parameter to get
   * @return the boolean value of the parameter
   */
  boolean getBooleanParam(String name) throws ParamException;

  /**
   * Get the value of a double parameter.
   * @param name of the parameter to get
   * @return the double value of the parameter
   */
  double getDoubleParam(String name) throws ParamException;
  
  /**
   * Get the value of a float parameter.
   * @param name of the parameter to get
   * @return the float value of the parameter
   */
  float getFloatParam(String name) throws ParamException;
  
  /**
   * Get the value of a int parameter.
   * @param name of the parameter to get
   * @return the int value of the parameter
   */
  int getIntParam(String name) throws ParamException;
  
  /**
   * Get the value of a long parameter.
   * @param name of the parameter to get
   * @return the long value of the parameter
   */
  long getLongParam(String name) throws ParamException;

  /**
   * Get the value of a short parameter.
   * @param name of the parameter to get
   * @return the short value of the parameter
   */
  short getShortParam(String name) throws ParamException;

  /**
   * Get the value of a String parameter.
   * @param name of the parameter to get
   * @return the String value of the parameter
   */
  String getStringParam(String name) throws ParamException;

  boolean hasParam (String name);
}
