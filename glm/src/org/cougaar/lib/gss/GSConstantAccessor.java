/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
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
