/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.planning.ldm.plan.AssetPreferenceImpl;
import org.cougaar.domain.planning.ldm.asset.Asset;

public class AssetPreferenceWithDeck extends AssetPreferenceImpl
{
    Asset theAsset;
    
    public AssetPreferenceWithDeck (Asset anAsset)
    {
        super();
        theAsset = anAsset;
    }
	
    public AssetPreferenceWithDeck()
    {
        super();
    }
	
    public Asset getAsset()
    {
        return theAsset;
    }
	
    public void setAsset(Asset anAsset)
    {
        theAsset = anAsset;
    }
}
