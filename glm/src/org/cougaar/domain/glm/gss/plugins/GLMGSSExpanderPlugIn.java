/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/plugins/Attic/GLMGSSExpanderPlugIn.java,v 1.3 2001-08-22 20:27:21 mthome Exp $ */
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

package org.cougaar.domain.glm.gss.plugins;

import org.cougaar.domain.glm.gss.GLMSpecsHandler;

import org.cougaar.lib.gss.GSSpecsHandler;
import org.cougaar.lib.gss.plugins.UTILGSSExpanderPlugIn;

/**
 * The GLMGSSExpanderPlugInAdapter is a handy base class for TOPS 
 * Expanders.  getSubtasks should be redefined in subclasses.
 *
 * Has hooks for handling various replanning conditions.
 * 
 * Subclasses must define
 * <UL>
 * <LI>getSubtasks()
 * </UL>
 * @see org.cougaar.lib.plugin.plugins.UTILSimpleExpanderPlugIn
 */
public abstract class GLMGSSExpanderPlugIn extends UTILGSSExpanderPlugIn {
  public GSSpecsHandler getSpecsHandler() { return new GLMSpecsHandler(); }
}
