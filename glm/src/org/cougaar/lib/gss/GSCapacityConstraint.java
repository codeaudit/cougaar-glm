/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.asset.Asset;

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
