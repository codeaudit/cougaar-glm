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
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.plan.Task;
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
