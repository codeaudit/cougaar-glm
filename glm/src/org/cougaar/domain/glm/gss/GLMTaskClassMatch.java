// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/Attic/GLMTaskClassMatch.java,v 1.3 2001-08-22 20:27:21 mthome Exp $
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

package org.cougaar.domain.glm.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Task;
import java.util.List;

import org.cougaar.lib.gss.*;

/**
 * Selects which assets to use to schedule
 *
 * Copyright (C) 1999 BBN Technologies
 */

public class GLMTaskClassMatch implements GSBoolean, GSParent {

  String preposition;
  GSAssetClassMatch assetClassMatch;

  public GLMTaskClassMatch (String preposition) {
    this.preposition = preposition;
  }

  public void addChild (Object obj) {
    assetClassMatch = (GSAssetClassMatch) obj;
  }

  public boolean matchesClass (Task task) {
    return assetClassMatch.matchesClass
      ((Asset) GLMTaskAccessorImpl.getTaskObject (task, preposition));
  }

  
  public boolean eval (List args) {
    if (args.size() != 1) {
      System.err.println("GSS Error: Wrong number of args passed to GLMTaskClassMatch\nExpected 1 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    if (!(obj1 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GLMTaskClassMatch"
			 + "\nExpected Task and got " + obj1.getClass()
			 + " so will ALWAYS return false");

      return false;
    }

    Task task = (Task)obj1;

    return matchesClass (task);
  }

}
