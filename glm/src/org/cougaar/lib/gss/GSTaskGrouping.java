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
