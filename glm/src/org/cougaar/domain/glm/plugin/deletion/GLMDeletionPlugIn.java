/*
 * <copyright>
 *  Copyright 1997-2001 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plugin.deletion;

import org.cougaar.domain.planning.ldm.DeletionPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.glm.ldm.asset.Organization;

public class GLMDeletionPlugIn extends DeletionPlugIn {
    protected boolean isAssetRemote(Asset asset) {
        return asset instanceof Organization;
    }
}

