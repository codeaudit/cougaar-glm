/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/callback/Attic/GLMNotOrganizationCallback.java,v 1.3 2001-04-05 19:27:29 mthome Exp $ */
/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.callback;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.lib.callback.UTILAssetCallback;
import org.cougaar.lib.callback.UTILFilterCallbackListener;
/**
 * Simple extension of Asset callback that filters for 
 * everything but organizations.
 */

public class GLMNotOrganizationCallback extends UTILAssetCallback {
  public GLMNotOrganizationCallback (UTILFilterCallbackListener listener) {
    super (listener);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return ((!(o instanceof Organization)) && 
		(o instanceof Asset));
	// return(o instanceof Asset);
      }
    };
  }
}
