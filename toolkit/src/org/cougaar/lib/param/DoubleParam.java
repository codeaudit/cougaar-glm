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
 * Class that encapsulates a double parameter.  The name and 
 * the type of the parameter are inhereted from the base class.
 * this class only adds the value of the parameter.
 *
 */
public class DoubleParam extends Param {
  
  /**
   * Constructor
   * @param v the double value of the parameter
   */
  public DoubleParam(String n, double v){
    super(n, "double");
    this.value = v;
  }
  
  /**
   * Get accessor for the double value of a parameter.
   * Override if needed.
   * @return the double value of the parameter.
   */
  double getDoubleValue() throws ParamException{ 
    return this.value;
  }  
  
  public String toString () { return "" + value; }

  private double value;
}
