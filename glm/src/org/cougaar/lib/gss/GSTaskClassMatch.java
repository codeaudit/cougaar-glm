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
import java.util.List;

/**
 * Selects which assets to use to schedule
 *
 */

public class GSTaskClassMatch implements GSBoolean, GSParent {

  String preposition;
  GSAssetClassMatch assetClassMatch;

  public GSTaskClassMatch (String preposition) {
    this.preposition = preposition;
  }

  public void addChild (Object obj) {
    assetClassMatch = (GSAssetClassMatch) obj;
  }

  public boolean matchesClass (Task task) {
    return assetClassMatch.matchesClass
      ((Asset) GSTaskAccessorImpl.getTaskObject (task, preposition));
  }

  
  public boolean eval (List args) {
    if (args.size() != 1) {
      System.err.println("GSS Error: Wrong number of args passed to GSTaskClassMatch\nExpected 1 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    if (!(obj1 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GSTaskClassMatch"
			 + "\nExpected Task and got " + obj1.getClass()
			 + " so will ALWAYS return false");

      return false;
    }

    Task task = (Task)obj1;

    return matchesClass (task);
  }

}
