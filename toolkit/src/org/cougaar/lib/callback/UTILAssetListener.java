/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import java.util.Enumeration;
import org.cougaar.domain.planning.ldm.asset.Asset;

/**
 * Asset listener -- can be used with physicalAsset or asset callback.
 */

public interface UTILAssetListener extends UTILFilterCallbackListener {

  /** 
   * Defines assets you find interesting. 
   * @param a Asser to check for interest
   * @return boolean true if asset is interesting
   */
  boolean interestingAsset (Asset a);

  /**
   * Place to handle updated assets.
   * @param newAssets new assets found in the container
   */
  void handleNewAssets     (Enumeration e);

  /**
   * Place to handle changed assets.
   * @param newAssets changed assets found in the container
   */
  void handleChangedAssets (Enumeration e);
}
        
        
                
                        
                
        
        
