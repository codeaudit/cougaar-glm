/*
 * <copyright>
 *  Copyright 1997-2001 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.construction;

import org.cougaar.domain.glm.plugins.GLMDecorationPlugIn;

import java.util.Vector;

/*
 * Construction (ClassIV) Projection Plugin 
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: ConstructionProjectionPlugIn.java,v 1.2 2001-04-10 17:39:09 bdepass Exp $
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