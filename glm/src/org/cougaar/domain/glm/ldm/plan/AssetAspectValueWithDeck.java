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

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.AssetAspectValue;

public class AssetAspectValueWithDeck extends 
    AssetAspectValue
{
    private Asset theAsset;
    
    public AssetAspectValueWithDeck(Asset anAsset, int type, double value)
    {
        super(anAsset, type, value);
        this.theAsset = anAsset;
    }
    
    public Asset getAsset()
    {
        return theAsset;
    }
}
