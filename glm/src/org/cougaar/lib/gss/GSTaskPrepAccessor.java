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

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;

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
