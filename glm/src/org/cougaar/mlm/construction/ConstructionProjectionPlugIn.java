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

package org.cougaar.mlm.construction;

import org.cougaar.glm.plugins.GLMDecorationPlugIn;

import java.util.Vector;

/*
 * Construction (ClassIV) Projection Plugin 
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/

public class ConstructionProjectionPlugIn extends GLMDecorationPlugIn {
    private static String                  theater_ = "SWA";

	public void setupSubscriptions() {
		super.setupSubscriptions();
		//System.out.println ("setup");
	} // setupSubscriptions

    /** Decorate plugin using ConstructionProjectionDecorator 
     * @see ConstructionProjectionDecorator */
    protected void decoratePlugIn() {
    	ConstructionProjectionDecorator decorator = new ConstructionProjectionDecorator(this);

	    decorator.decoratePlugIn(myOrganization_);
    }

    /** Return theater (location) of cluster.
     *  For now it defaults to "SWA".
     **/
    public String getTheater() {
	return theater_;
    }


    /** Returns a Vector of Strings with the 
     *  class name of the types of parts to be handled. 
     *  Beth, I don't think you'll need this since you
     *  you are only handling a single type but I'm not sure
     **/
    public Vector getPartTypes() {
	return (Vector)myParams_.get(SUPPLYTYPES);
    }
}
