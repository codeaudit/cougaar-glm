/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.projection;

import org.cougaar.domain.glm.plugins.GLMDecorationPlugIn;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The plugin is decorated with the SupplyProjector processor 
 * with the proper arguments at run time
 * by the SupplyProjectionDecorator.
 * @see SupplyProjectionDecorator
 * @see SupplyProjector
 */
public class SupplyProjectionPlugIn extends GLMDecorationPlugIn {
    private static String                  theater_ = "SWA";

    /** Decorate plugin using SupplyProjectionDecorator 
     * @see SupplyProjectionDecorator */
    protected void decoratePlugIn() {
	SupplyProjectionDecorator decorator = new SupplyProjectionDecorator(this);
	decorator.decoratePlugIn(myOrganization_);
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
