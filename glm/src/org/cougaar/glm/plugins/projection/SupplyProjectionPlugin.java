/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
