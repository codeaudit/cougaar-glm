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

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
 
import java.util.Enumeration;

/**
 * For use with the record/playback system.
 *
 * Filters for tasks that came from outside (alien!) clusters.
 * I.e. tasks expanded by the cluster will *not* show up here.
 */

public class UTILAlienTaskCallback extends UTILFilterCallbackAdapter {
  public UTILAlienTaskCallback (UTILAlienListener listener) {
    super (listener);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if ( o instanceof Task ) {
	  Task aTask = (Task) o;
	  UTILAlienListener alienListener = (UTILAlienListener) myListener;
	  boolean fromOtherCluster = 
	    !(aTask.getSource().equals(new ClusterIdentifier(alienListener.getClusterName ())));
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

    if (xxdebug) {
      if (mySub.getChangedList().hasMoreElements ())
	System.out.println ("UTILAlienTaskCallback : " + 
			    "Alien tasks were changed.");
      if (mySub.getRemovedList().hasMoreElements ()) {
	Enumeration removedtasks = mySub.getRemovedList();
	while (removedtasks.hasMoreElements()) {
	  Task t = (Task) removedtasks.nextElement();
	  System.out.println ("UTILAlienTaskCallback : Alien task " + 
			      t.getUID () + 
			      " was removed from container.");
	}
      }
    }
  }
}
        
        
                
                        
                
        
        
