/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package  org.cougaar.lib.param;

/**
 * Class that encapsulates a int parameter.  The name and 
 * the type of the parameter are inhereted from the base class.
 * this class only adds the value of the parameter.
 *
 */
public class IntParam extends Param {
  
  /**
   * Constructor
   * @param v the int value of the parameter
   */
  public IntParam(String n, int v){
    super(n, "int");
    this.value = v;
  }
  
  /**
   * Get accessor for the int value of a parameter.
   * Override if needed.
   * @return the int value of the parameter.
   */
  int getIntValue() throws ParamException{ 
    return this.value;
  }  
  
  public String toString () { return "" + value; }

  private int value;
}
