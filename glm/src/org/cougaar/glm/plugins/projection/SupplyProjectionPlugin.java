/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 1999-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.projection;

import java.util.Vector;

import org.cougaar.glm.plugins.GLMDecorationPlugin;

/**
 * The plugin is decorated with the SupplyProjector processor 
 * with the proper arguments at run time
 * by the SupplyProjectionDecorator.
 * @see SupplyProjectionDecorator
 * @see GenerateSupplyDemandExpander
 */
public class SupplyProjectionPlugin extends GLMDecorationPlugin {
    private static String                  theater_ = "SWA";

    /** Decorate plugin using SupplyProjectionDecorator 
     * @see SupplyProjectionDecorator */
    protected void decoratePlugin() {
	SupplyProjectionDecorator decorator = new SupplyProjectionDecorator(this);
	decorator.decoratePlugin(myOrganization_);
    }

    /** Return theater (location) of cluster.
     *  For now it defaults to "SWA".
     **/
    public String getTheater() {
	return theater_;
    }

    /** Empty method - avoids default behavior of loading plugin parameters as processors.
     *  For this plugIn, parameters refer to the types of parts handled by the supply projector. 
     **/
    protected void readParameters() {
    }
    
    /** Returns an Vector of Strings with the 
     *  class name of the types of parts to be handled. **/
    public Vector getPartTypes() {
	return getParameters();
    }
}
