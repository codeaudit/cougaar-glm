/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/callback/Attic/GLMOrganizationCallback.java,v 1.3 2001-04-05 19:27:29 mthome Exp $ */
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

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.Organization;

import java.util.Enumeration;

import org.cougaar.lib.callback.UTILFilterCallbackAdapter;

/**
 *  Filters for organizations, telling listeners of new and changed ones.
 */

public class GLMOrganizationCallback extends UTILFilterCallbackAdapter {
  public GLMOrganizationCallback (GLMOrganizationListener listener) {
    super (listener);
  }

  /**
   * Filter for organizations
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Organization);
      }
    };
  }

  /**
   * Tell listener when an organization changes, or is added 
   * to the set of organizations known to this cluster.
   */
  public void reactToChangedFilter () {
    Enumeration changedList = mySub.getChangedList();
    Enumeration addedList   = mySub.getAddedList();

    if (changedList.hasMoreElements ())
      ((GLMOrganizationListener) myListener).handleChangedOrganization (changedList);

    if (addedList.hasMoreElements ())
      ((GLMOrganizationListener) myListener).handleNewOrganization (addedList);
  }
}
        
        
                
                        
                
        
        
