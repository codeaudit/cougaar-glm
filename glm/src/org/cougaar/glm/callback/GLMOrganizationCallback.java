/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/callback/GLMOrganizationCallback.java,v 1.3 2003-01-23 19:53:17 mthome Exp $ */
/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.glm.ldm.asset.Organization;

import java.util.Enumeration;

import org.cougaar.lib.callback.UTILFilterCallbackAdapter;

import org.cougaar.util.log.Logger;

/**
 *  Filters for organizations, telling listeners of new and changed ones.
 */

public class GLMOrganizationCallback extends UTILFilterCallbackAdapter {
  public GLMOrganizationCallback (GLMOrganizationListener listener, Logger logger) {
    super (listener, logger);
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
        
        
                
                        
                
        
        
