/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.asset.Asset;

import java.util.Enumeration;

/**
 * Used to filter for new or changed generic assets.
 * 
 * Try not to use this.  There are more specific filters -
 * for organizations and physical assets...
 */

public class UTILAssetCallback extends UTILFilterCallbackAdapter {
  public UTILAssetCallback (UTILFilterCallbackListener listener) {
    super (listener);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Asset && ((UTILAssetListener)myListener).interestingAsset((Asset) o));
      }
    };
  }

  public void reactToChangedFilter () {
    Enumeration changedList = mySub.getChangedList();
    Enumeration addedList   = mySub.getAddedList();

    if (changedList.hasMoreElements ())
      ((UTILAssetListener) myListener).handleChangedAssets (changedList);

    if (addedList.hasMoreElements ())
      ((UTILAssetListener) myListener).handleNewAssets (addedList);
  }
}
        
        
                
                        
                
        
        
