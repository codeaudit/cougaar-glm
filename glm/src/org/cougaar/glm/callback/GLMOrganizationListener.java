/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/callback/GLMOrganizationListener.java,v 1.3 2004-02-06 20:13:11 ahelsing Exp $ */
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

import java.util.Enumeration;

import org.cougaar.lib.callback.UTILFilterCallbackListener;

/**
 * defines interface for org listener.
 * currently only concerned about new and changed organizations
 */

public interface GLMOrganizationListener extends UTILFilterCallbackListener {
  /**
   * Place to handle new organizations.
   * @param  e -- new organizations found in the container
   */
  void handleNewOrganization     (Enumeration e);

  /**
   * Place to handle changed organizations.
   * @param  e -- changed organizations found in the container
   */
  void handleChangedOrganization (Enumeration e);
}
        
        
                
                        
                
        
        
