/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.construction;

import java.util.Vector;

import org.cougaar.glm.plugins.GLMDecorationPlugin;

/*
 * Construction (ClassIV) Projection Plugin 
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/

public class ConstructionProjectionPlugin extends GLMDecorationPlugin {
    private static String                  theater_ = "SWA";

	public void setupSubscriptions() {
		super.setupSubscriptions();
		//System.out.println ("setup");
	} // setupSubscriptions

    /** Decorate plugin using ConstructionProjectionDecorator 
     * @see ConstructionProjectionDecorator */
    protected void decoratePlugin() {
    	ConstructionProjectionDecorator decorator = new ConstructionProjectionDecorator(this);

	    decorator.decoratePlugin(myOrganization_);
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
