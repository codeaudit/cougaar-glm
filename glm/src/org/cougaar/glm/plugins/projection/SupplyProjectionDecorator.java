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

import org.cougaar.glm.ldm.asset.ClassVIIMajorEndItem;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.PluginDecorator;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.util.UnaryPredicate;

/** Associates proper SupplyProjector with a given SupplyProjectionPlugin. 
 * @see SupplyProjectionPlugin
 * @see GenerateSupplyDemandExpander
 **/
public class SupplyProjectionDecorator extends PluginDecorator {
    private static final String            CLASSNAME = "SupplyProjectionDecorator";
    // move to the plug in
    Service serv_;
    SupplyProjectionPlugin thisPlugin_;
    Organization cluster_;
    
    /**  @param plugin to be configured */
    public SupplyProjectionDecorator(SupplyProjectionPlugin plugin) {
	super(plugin);
	thisPlugin_ = plugin;
    }

    /** Predicate for MEIs - used to define consumers for Supply projection.
     *  Accepts aggregate assets of MEIs if the environmental variable 
     *  "glm_aggregate_mei" is set to "true". **/
    static class MEIPredicate implements UnaryPredicate
    {
	private boolean include_aggregates_ = false;

	public MEIPredicate() {
	    String val = System.getProperty("glm_aggregate_mei");
	    if (val != null) {
	        if (val.equalsIgnoreCase("true"))
		    include_aggregates_ = true;
	    }

	}

	public boolean execute(Object o) {
	    if (o instanceof ClassVIIMajorEndItem) {
		return true;
	    }
	    
	    if (include_aggregates_) {
		if (o instanceof AggregateAsset) {
		    if (((AggregateAsset)o).getAsset() instanceof ClassVIIMajorEndItem) {
			return true;
		    }
		}
	    }
	    return false;
	}
    }

    /** 
     *  Customizes the given SupplyProjectionPlugin with one or more BasicProcessor 
     *  depending on information from the organizational asset 
     *  this cluster represents.
     *  @param cluster the given plugin is loaded into
     */
    public void decoratePlugin(Organization cluster) {
	cluster_ = cluster;
 	addTaskProcessor( new GenerateSupplyDemandExpander(thisPlugin_, cluster_, thisPlugin_.getPartTypes()));
    }
}
