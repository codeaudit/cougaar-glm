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

import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.domain.glm.asset.ClassVIIMajorEndItem;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.plan.Service;
import org.cougaar.domain.glm.plugins.PlugInDecorator;

/** Associates proper SupplyProjector with a given SupplyProjectionPlugIn. 
 * @see SupplyProjectionPlugIn
 * @see SupplyProjector **/
public class SupplyProjectionDecorator extends PlugInDecorator {
    private static final String            CLASSNAME = "SupplyProjectionDecorator";
    // move to the plug in
    Service serv_;
    SupplyProjectionPlugIn thisPlugIn_;
    Organization cluster_;
    
    /**  @param plugin to be configured */
    public SupplyProjectionDecorator(SupplyProjectionPlugIn plugin) {
	super(plugin);
	thisPlugIn_ = plugin;
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
    };

    /** 
     *  Customizes the given SupplyProjectionPlugin with one or more BasicProcessor 
     *  depending on information from the organizational asset 
     *  this cluster represents.
     *  @param cluster the given plugin is loaded into
     */
    public void decoratePlugIn(Organization cluster) {
	cluster_ = cluster;
 	addTaskProcessor( new GenerateSupplyDemandExpander(thisPlugIn_, cluster_, thisPlugIn_.getPartTypes()));
    }
}
