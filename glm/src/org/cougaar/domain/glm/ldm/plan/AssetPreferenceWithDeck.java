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
