/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.lib.callback;

import java.util.Enumeration;

import org.cougaar.planning.ldm.asset.Asset;

/**
 * Asset listener -- can be used with physicalAsset or asset callback.
 */

public interface UTILAssetListener extends UTILFilterCallbackListener {

  /** 
   * Defines assets you find interesting. 
   * @param a Asset to check for interest
   * @return boolean true if asset is interesting
   */
  boolean interestingAsset (Asset a);

  /**
   * Place to handle updated assets.
   * @param e new assets found in the container
   */
  void handleNewAssets     (Enumeration e);

  /**
   * Place to handle changed assets.
   * @param e changed assets found in the container
   */
  void handleChangedAssets (Enumeration e);
}
        
        
                
                        
                
        
        
