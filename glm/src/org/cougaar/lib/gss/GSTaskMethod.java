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
