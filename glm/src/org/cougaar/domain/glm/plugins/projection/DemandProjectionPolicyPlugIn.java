/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plugins.projection;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.glm.plugins.GLMPolicyPlugIn;

/**
 * The DemandProjectionPolicy GUI PlugIn based on
 * org.cougaar.domain.mlm.plugin.sample.PolicyPlugIn
 */

public class DemandProjectionPolicyPlugIn extends GLMPolicyPlugIn					    
{
    static UnaryPredicate policyPredicate = new UnaryPredicate() {
	public boolean execute(Object o) {
	    if (o instanceof DemandProjectionPolicy) {
		return true;
	    }
	    return false;
	}
    };

    public DemandProjectionPolicyPlugIn() {
	super(policyPredicate);
    }
    
}
