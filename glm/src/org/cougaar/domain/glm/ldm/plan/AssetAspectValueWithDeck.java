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
