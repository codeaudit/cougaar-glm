/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.lib.callback;

import java.util.Enumeration;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
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
        
        
                
                        
                
        
        
