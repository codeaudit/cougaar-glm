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
import java.util.List;

/**
 * Determines whether two tasks can be grouped.
 * Implements an arbitrary boolean expression of task matches
 *
 */

public class GSTaskGrouping implements GSParent, GSBoolean {

  public void addChild (Object obj) {
    if (match != null)
      System.out.println ("More than one expression in capability filter");
    match = (GSBoolean) obj;
  }

  public boolean eval (List args) {
    if (args.size() != 2) {
      System.err.println("GSS Error: Wrong number of args passed to GSTaskGrouping\nExpected 2 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    Object obj2 = args.get(1);
    if (!(obj1 instanceof Task) || !(obj2 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GSTaskGrouping"
			 + "\nExpected Asset, Task and got " + obj1.getClass()
			  + ", " + obj2.getClass() + 
			 " so will ALWAYS return false");
      return false;
    }
    
    return match.eval(args);
  }

  GSBoolean match = null;
}
