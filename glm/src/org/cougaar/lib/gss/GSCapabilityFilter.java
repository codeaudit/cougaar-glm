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
import java.util.List;

/**
 * Determines whether a resource can perform a task.
 * Implements an arbitrary boolean expression of capability matches
 *
 */

public class GSCapabilityFilter implements GSParent, GSBoolean {

  public void addChild (Object obj) {
    if (match != null)
      System.out.println ("More than one expression in capability filter");
    match = (GSBoolean) obj;
  }

  public boolean eval (List args) {
    if (args.size() != 2) {
      System.err.println("GSS Error: Wrong number of args passed to GSCapabilityFilter\nExpected 2 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    Object obj2 = args.get(1);

    if (!(obj1 instanceof Asset) || !(obj2 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GSCapabilityFilter"
			 + "\nExpected Asset, Task and got " + obj1.getClass()
			  + ", " + obj2.getClass() + 
			 " so will ALWAYS return false");
      return false;
    }
    
    return match.eval(args);
  }

  GSBoolean match = null;
}
