/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
