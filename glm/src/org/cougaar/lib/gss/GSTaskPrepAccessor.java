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

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;

import java.util.List;

/**
 * Accesses the specified preposition of a given task.
 * If preposition exists, eval returns true.
 *
 */

public class GSTaskPrepAccessor implements GSBoolean {
  /**
   * Constructor specifying which asset (using preposition) and
   * property value for that asset
   */
  public GSTaskPrepAccessor (String preposition) {
    this.preposition = preposition;
    if (preposition == null)
      System.out.println (this.getClass () + "No preposition for task accessor");
  }

  public boolean eval (List args) {
    Task task = (Task) args.get (0);
    boolean result = (task.getPrepositionalPhrase (preposition) != null);
    /*
    System.out.println ("taskPrep " + task.getUID () + 
			"->" + task.getPrepositionalPhrase (preposition) + 
			"-" + preposition);
    */
    return result;
  }

  private String preposition;
}
