/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/callback/GLMPhysicalAssetCallback.java,v 1.1 2001-12-27 22:41:22 bdepass Exp $ */
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

package org.cougaar.glm.callback;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.glm.ldm.asset.PhysicalAsset;
import org.cougaar.lib.callback.UTILAssetCallback;
import org.cougaar.lib.callback.UTILFilterCallbackListener;

/**
 * Simple extension of Asset callback that filters for physical assets.
 */

public class GLMPhysicalAssetCallback extends UTILAssetCallback {
  public GLMPhysicalAssetCallback (UTILFilterCallbackListener listener) {
    super (listener);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof PhysicalAsset);
      }
    };
  }
}
