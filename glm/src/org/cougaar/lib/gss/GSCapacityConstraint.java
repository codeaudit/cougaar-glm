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

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.asset.Asset;

/**
 * Determines whether a resource can perform a task
 */

public class GSCapacityConstraint extends GSDoubleAccessor {
  public static boolean debug = false;
  public static void setDebug (boolean d) { debug = d; }

  public boolean withinCapacity (Asset asset, double soFar) {
    Object value = resourceAccessor.value (asset);
    if (value == null)
      resourceAccessor.reportError (asset);
    return soFar <= convert (value);
  }

  public double incrementCapacity (Task task, double soFar) {
    Object value = taskAccessor.value (task);
    if (value == null)
      System.out.println ("Null returned from task accessor for task " + task);

    return convert (value) + soFar;
  }

  private double convert (Object value) {
    if (value instanceof Double)
      return ((Double) value).doubleValue();
    if (value instanceof Integer)
      return ((Integer) value).doubleValue();
    if (value instanceof Long)
      return ((Long) value).doubleValue();
	
    System.out.println
      ("Attempting to convert non-numeric object " + value + " to double");
    return 0.0;
  }
}
