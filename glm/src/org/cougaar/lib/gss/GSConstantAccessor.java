/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.asset.Asset;

/**
 * Replacess a task or resource accessor with a constant value
 *
 */

public class GSConstantAccessor implements GSAssetAccessor,
					   GSTaskAccessor {
  
  /** Constructor */
  public GSConstantAccessor (String value, String type) {
    if (type.equals ("string"))
      returnValue = value;
    else if (type.equals ("int"))
      returnValue = new Integer (value);
    else if (type.equals ("long"))
      returnValue = new Long (value);
    else if (type.equals ("double"))
      returnValue = new Double (value);
    else if (type.equals ("boolean"))
      returnValue = new Boolean (value);
  }

  /** Just returns the constant value */
  public Object value (Task task) {
    return returnValue;
  }

  /** Just returns the constant value */
  public Object value (Asset asset) {
    return returnValue;
  }

  /**
   * For GSAssetAccessorInterface- stub
   *  value returned null - give some indication why 
   */
  public void reportError (Asset asset) {
    // Should never return null here unless the constant was set up
    // incorrectly- doesn't depend on asset
    System.err.println("GSConstantAcccessor.reportError - Invalid constant " +
		       returnValue);
  }

  private Object returnValue;

}
