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
 * Class that encapsulates a String parameter.  The name and 
 * the type of the parameter are inhereted from the base class.
 * this class only adds the value of the parameter.
 *
 */
public class StringParam extends Param {
  
  /**
   * Constructor
   * @param v the String value of the parameter
   */
  public StringParam(String n, String v){
    super(n, "String");
    this.value = v;
  }
  
  /**
   * Get accessor for the String value of a parameter.
   * Override if needed.
   * @return the String value of the parameter.
   */
  public String getStringValue() throws ParamException{ 
    return this.value;
  }  
  
  public String toString () { return "" + value; }
  private String value;
}
