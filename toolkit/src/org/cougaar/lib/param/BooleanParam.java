/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.param;

/**
 * Class that encapsulates a boolean parameter.  The name and 
 * the type of the parameter are inhereted from the base class.
 * this class only adds the value of the parameter.
 *
 */
public class BooleanParam extends Param {
  
  /**
   * Constructor
   * @param v the boolean value of the parameter
   */
  public BooleanParam(String n, boolean v){
    super(n, "boolean");
    this.value = v;
  }
  
  /**
   * Get accessor for the boolean value of a parameter.
   * Override if needed.
   * @return the boolean value of the parameter.
   */
  boolean getBooleanValue() throws ParamException{ 
    return this.value;
  }  
  
  public String toString () { return "" + value; }

  private boolean value;
}
