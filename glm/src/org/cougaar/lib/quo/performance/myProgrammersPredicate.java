/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */
package org.cougaar.lib.quo.performance;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.OrganizationPG;
import org.cougaar.domain.planning.ldm.plan.Role;


/**
 * A predicate that matches all organizations that can
 * fulfill the SoftwareDevelopment role
 */
class myProgrammersPredicate implements UnaryPredicate
{
    public boolean execute(Object o) {
	boolean ret = false;
	if (o instanceof Organization) {
	    Organization org = (Organization)o;
	    OrganizationPG orgPG = org.getOrganizationPG();
	    ret = orgPG.inRoles(Role.getRole("SoftwareDevelopment"));
	}
	return ret;
    }
}

