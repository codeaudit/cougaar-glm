/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
  public void addParam(String n, Param p);
  
  /**
   * Get the value of a boolean parameter.
   * @param name of the parameter to get
   * @return the boolean value of the parameter
   */
  public boolean getBooleanParam(String name) throws ParamException;

  /**
   * Get the value of a double parameter.
   * @param name of the parameter to get
   * @return the double value of the parameter
   */
  public double getDoubleParam(String name) throws ParamException;
  
  /**
   * Get the value of a float parameter.
   * @param name of the parameter to get
   * @return the float value of the parameter
   */
  public float getFloatParam(String name) throws ParamException;
  
  /**
   * Get the value of a int parameter.
   * @param name of the parameter to get
   * @return the int value of the parameter
   */
  public int getIntParam(String name) throws ParamException;
  
  /**
   * Get the value of a long parameter.
   * @param name of the parameter to get
   * @return the long value of the parameter
   */
  public long getLongParam(String name) throws ParamException;

  /**
   * Get the value of a short parameter.
   * @param name of the parameter to get
   * @return the short value of the parameter
   */
  public short getShortParam(String name) throws ParamException;

  /**
   * Get the value of a String parameter.
   * @param name of the parameter to get
   * @return the String value of the parameter
   */
  public String getStringParam(String name) throws ParamException;
}
