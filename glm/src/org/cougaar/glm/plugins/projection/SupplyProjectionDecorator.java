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
