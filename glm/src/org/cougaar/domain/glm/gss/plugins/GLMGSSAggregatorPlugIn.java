// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/plugins/Attic/GLMGSSAggregatorPlugIn.java,v 1.2 2001-04-05 19:27:35 mthome Exp $
/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.gss.plugins;

import org.cougaar.domain.glm.gss.GLMSpecsHandler;

import org.cougaar.lib.gss.GSSpecsHandler;
import org.cougaar.lib.gss.plugins.UTILGSSAggregatorPlugIn;

/**
 * Abstract because deriving classes need to define 
 * <UL>
 * <LI>NewMPTask getMPTask()
 * </UL>
 * <B>Note:</B> If you redefine needToRescind to return true (in a subclass), 
 * then you must redefine:
 * <UL>
 * <LI>public boolean handleRescindedAggregation(Aggregation)
 * <UL>
 * To properly handle the rescinded Aggregation, and then call super (that is, 
 * the function defined here).
 */
public abstract class GLMGSSAggregatorPlugIn extends UTILGSSAggregatorPlugIn {
  public GSSpecsHandler getSpecsHandler() { return new GLMSpecsHandler(); }
}
