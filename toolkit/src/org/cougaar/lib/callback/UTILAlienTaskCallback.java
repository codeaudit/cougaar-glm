/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
 
import java.util.Enumeration;

import org.cougaar.util.log.Logger;

/**
 * For use with the record/playback system.
 *
 * Filters for tasks that came from outside (alien!) clusters.
 * I.e. tasks expanded by the cluster will *not* show up here.
 */

public class UTILAlienTaskCallback extends UTILFilterCallbackAdapter {
  public UTILAlienTaskCallback (UTILAlienListener listener, Logger logger) {
    super (listener, logger);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if ( o instanceof Task ) {
	  Task aTask = (Task) o;
	  UTILAlienListener alienListener = (UTILAlienListener) myListener;
	  boolean fromOtherCluster = 
	    !(aTask.getSource().equals(MessageAddress.getMessageAddress(alienListener.getClusterName ())));
	  return (fromOtherCluster &&
		  alienListener.interestingTask (aTask));
	}
	return false;
      }
    };
  }

  public void reactToChangedFilter () {
    Enumeration newtasks = mySub.getAddedList();
    UTILAlienListener alienListener = (UTILAlienListener) myListener;
    while (newtasks.hasMoreElements()) {
      Task t = (Task) newtasks.nextElement();
      alienListener.handleTask (t);
    }

    if (logger.isDebugEnabled()) {
      if (mySub.getChangedList().hasMoreElements ())
	logger.debug ("UTILAlienTaskCallback : Alien tasks were changed.");
      if (mySub.getRemovedList().hasMoreElements ()) {
	Enumeration removedtasks = mySub.getRemovedList();
	while (removedtasks.hasMoreElements()) {
	  Task t = (Task) removedtasks.nextElement();
	  logger.info ("UTILAlienTaskCallback : Alien task " + 
			      t.getUID () + 
			      " was removed from container.");
	}
      }
    }
  }
}
        
        
                
                        
                
        
        
