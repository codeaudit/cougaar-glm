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
import java.util.List;

/**
 * Determines whether two tasks pass a test for being grouped
 *
 */

public class GSTaskMatch implements GSBoolean, GSParent {

  public boolean eval (List args) {
    if (args.size() != 2) {
      System.err.println("GSS Error: Wrong number of args passed to GSTaskMatch\nExpected 2 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    Object obj2 = args.get(1);
    if (!(obj1 instanceof Task) || !(obj2 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GSTaskMatch"
			 + "\nExpected Tasks and got " + obj1.getClass()
			  + ", " + obj2.getClass() + 
			 " so will ALWAYS return false");
      return false;
    }
    Task task1 = (Task)args.get(0);
    Task task2 = (Task)args.get(1);
    
    return taskAccessor.value (task1).equals (taskAccessor.value (task2));
  }

  public void addChild (Object obj) {
    taskAccessor = (GSTaskAccessor) obj;
  }

  private GSTaskAccessor taskAccessor;

}
