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

