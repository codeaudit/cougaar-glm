/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.callback;

import java.util.Enumeration;
import org.cougaar.domain.glm.ldm.asset.TransportationNode;

import org.cougaar.lib.callback.UTILFilterCallbackListener;

/**
 * Location listener -- can be used with LocationCallback
 */

public interface GLMLocationListener extends UTILFilterCallbackListener {

  /** 
   * Defines assets you find interesting. 
   * @param a Asset to check for interest
   * @return boolean true if asset is interesting
   */
  boolean interestingLocation (TransportationNode location);

  /**
   * Place to handle updated assets.
   * @param newLocations new assets found in the container
   */
  void handleNewLocations     (Enumeration e);

  /**
   * Place to handle changed assets.
   * @param newLocations changed assets found in the container
   */
  void handleChangedLocations (Enumeration e);
}
        
        
                
                        
                
        
        
