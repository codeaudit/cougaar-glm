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

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Task;
import java.util.List;

/**
 * Selects which assets to use to schedule
 *
 */

public class GSTaskVerbMatch implements GSBoolean {

  private String verb;

  public GSTaskVerbMatch (String verb) {
    this.verb = verb;
  }

  public boolean matchesVerb (Task task) {
    return task.getVerb().equals(verb);
  }

  public boolean eval (List args) {
    if (args.size() != 1) {
      System.err.println("GSS Error: Wrong number of args passed to GSTaskVerbMatch\nExpected 1 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    if (!(obj1 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GSTaskVerbMatch"
			 + "\nExpected Task and got " + obj1.getClass()
			 + " so will ALWAYS return false");

      return false;
    }

    Task task = (Task)obj1;

    return matchesVerb (task);
  }
}
