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
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.plan.Task;
import java.util.List;

/**
 * Selects which assets to use to schedule
 *
 */

public class GSAssetClassMatch implements GSBoolean {

  Class theClass;

  public GSAssetClassMatch (String className) {
    try {
      theClass = Class.forName (className);
    } catch (Exception e) {
      System.out.println ("Bad class name for asset class match");
    }
  }

  public boolean matchesClass (Asset asset) {
    Asset a = asset;
    if (a instanceof AggregateAsset)
      a = ((AggregateAsset) a).getAsset();
    return theClass.isInstance (a);
  }

  public boolean eval (List args) {
    if (args.size() != 1) {
      System.err.println("GSS Error: Wrong number of args passed to GSAssetClassMatch\nExpected 1 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    if (!(obj1 instanceof Asset)) {
      System.err.println("GSS Error: Wrong type of args passed to GSAssetClassMatch"
			 + "\nExpected Asset and got " + obj1.getClass()
			 + " so will ALWAYS return false");

      return false;
    }

    Asset resource = (Asset)obj1;

    return matchesClass (resource);
  }

}
