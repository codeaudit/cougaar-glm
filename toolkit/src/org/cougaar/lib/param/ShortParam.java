/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package  org.cougaar.lib.param;

/**
 * Class that encapsulates a short parameter.  The name and 
 * the type of the parameter are inhereted from the base class.
 * this class only adds the value of the parameter.
 *
 */
public class ShortParam extends Param {
  
  /**
   * Constructor
   * @param v the short value of the parameter
   */
  public ShortParam(String n, short v){
    super(n, "short");
    this.value = v;
  }
  
  /**
   * Get accessor for the short value of a parameter.
   * Override if needed.
   * @return the short value of the parameter.
   */
  short getShortValue() throws ParamException{ 
    return this.value;
  }  
  
  public String toString () { return "" + value; }

  private short value;
}
