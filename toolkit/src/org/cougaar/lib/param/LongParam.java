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
 * Class that encapsulates a long parameter.  The name and 
 * the type of the parameter are inhereted from the base class.
 * this class only adds the value of the parameter.
 *
 */
public class LongParam extends Param {
  
  /**
   * Constructor
   * @param v the long value of the parameter
   */
  public LongParam(String n, long v){
    super(n, "long");
    this.value = v;
  }
  
  /**
   * Get accessor for the long value of a parameter.
   * Override if needed.
   * @return the long value of the parameter.
   */
  long getLongValue() throws ParamException{ 
    return this.value;
  }  
  
  public String toString () { return "" + value; }

  private long value;
}
