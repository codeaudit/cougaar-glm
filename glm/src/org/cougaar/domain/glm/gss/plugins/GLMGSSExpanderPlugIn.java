/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/plugins/Attic/GLMGSSExpanderPlugIn.java,v 1.1 2000-12-15 20:18:00 mthome Exp $ */
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
