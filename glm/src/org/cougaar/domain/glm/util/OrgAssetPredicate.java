/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.util;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.glm.asset.Organization;

public class OrgAssetPredicate  implements UnaryPredicate, NewOrgAssetPredicate {
    public boolean execute(Object o) {
	if ( o instanceof Organization ) 
	    return true;
	return false;
    }
}
