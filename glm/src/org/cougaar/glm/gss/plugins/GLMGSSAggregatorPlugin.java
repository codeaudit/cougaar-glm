// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/gss/plugins/Attic/GLMGSSAggregatorPlugin.java,v 1.1 2002-02-12 17:47:56 jwinston Exp $
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

package org.cougaar.glm.gss.plugins;

import org.cougaar.glm.gss.GLMSpecsHandler;

import org.cougaar.lib.gss.GSSpecsHandler;
import org.cougaar.lib.gss.plugins.UTILGSSAggregatorPlugin;

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
public abstract class GLMGSSAggregatorPlugin extends UTILGSSAggregatorPlugin {
  public GSSpecsHandler getSpecsHandler() { return new GLMSpecsHandler(); }
}
