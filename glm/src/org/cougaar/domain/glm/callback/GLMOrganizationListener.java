/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/callback/Attic/GLMOrganizationListener.java,v 1.2 2001-04-05 19:27:29 mthome Exp $ */
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

import java.util.Enumeration;

import org.cougaar.lib.callback.UTILFilterCallbackListener;

/**
 * defines interface for org listener.
 * currently only concerned about new and changed organizations
 */

public interface GLMOrganizationListener extends UTILFilterCallbackListener {
  /**
   * Place to handle new organizations.
   * @param Enumeration e -- new organizations found in the container
   */
  void handleNewOrganization     (Enumeration e);

  /**
   * Place to handle changed organizations.
   * @param Enumeration e -- changed organizations found in the container
   */
  void handleChangedOrganization (Enumeration e);
}
        
        
                
                        
                
        
        
