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
import java.lang.reflect.Method;

/**
 * Invoke the specified method on the given task
 *
 */

public class GSTaskMethod implements GSTaskAccessor {

  private Method method;
  private GSSchedulingSpecs specs;

  public GSTaskMethod (String methodName, String className,
		       GSSchedulingSpecs specs) {
    this.specs = specs;
    try {
      method = Class.forName(className).getMethod
        (methodName, new Class[] {Task.class});
      
    } catch (Exception e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
  }

  public Object value (Task task) {
    try {
      return method.invoke (specs, new Object[] {task});
    } catch (Exception e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

}
