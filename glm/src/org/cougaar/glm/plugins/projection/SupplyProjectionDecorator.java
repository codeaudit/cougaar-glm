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
package org.cougaar.glm.plugins.projection;

import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.glm.ldm.asset.ClassVIIMajorEndItem;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.PluginDecorator;

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
