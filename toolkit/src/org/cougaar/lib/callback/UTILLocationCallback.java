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

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.TransportationNode;

import java.util.Enumeration;

/**
 * Used to filter for new or changed generic assets.
 * 
 * Try not to use this.  There are more specific filters -
 * for organizations and physical assets...
 */

public class UTILLocationCallback extends UTILFilterCallbackAdapter {
  public UTILLocationCallback (UTILFilterCallbackListener listener) {
    super (listener);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof TransportationNode && 
		((UTILLocationListener)myListener).interestingLocation((TransportationNode) o));
      }
    };
  }

  public void reactToChangedFilter () {
    Enumeration changedList = mySub.getChangedList();
    Enumeration addedList   = mySub.getAddedList();

    if (changedList.hasMoreElements ())
      ((UTILLocationListener) myListener).handleChangedLocations (changedList);

    if (addedList.hasMoreElements ())
      ((UTILLocationListener) myListener).handleNewLocations (addedList);
  }
}
        
        
                
                        
                
        
        
