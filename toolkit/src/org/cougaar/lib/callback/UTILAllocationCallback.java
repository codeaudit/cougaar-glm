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
import java.util.HashMap;
import java.util.Map;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 * Filter for allocations.  Proscribes protocol for dealing with
 * changed allocations.
 */

public class UTILAllocationCallback extends UTILFilterCallbackAdapter {
  public UTILAllocationCallback (UTILAllocationListener listener, Logger logger) {
    super (listener, logger);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Allocation) {
	  Task t = ((Allocation)o).getTask();
	  return ((UTILAllocationListener) 
		  myListener).interestingNotification (t);
	}
	return false;
      }
    };
  }

  public void reactToChangedFilter () {
    Map  seenAllocsHash = new HashMap ();

    if (logger.isDebugEnabled())
      logger.debug ("UTILAllocationCallback " + this + 
		    " (listener = " + getClassName () + 
		    ") entered.");

    Enumeration changedallocs = mySub.getChangedList();

    int i = 0;
    while (changedallocs.hasMoreElements()) {
	i++;
      Allocation alloc = (Allocation) changedallocs.nextElement();
      if (seenAllocsHash.get (alloc) == null) {
	reactToChangedAlloc (alloc);
	seenAllocsHash.put (alloc, alloc);
      } else if (logger.isDebugEnabled()) 
	logger.debug ("UTILAllocationCallback : " + 
		      "Duplicate changed alloc for task " + 
		      alloc.getTask ().getUID () + " ignored.");
    }

    seenAllocsHash = new HashMap ();

    int j = 0;
    Enumeration removedallocs;
    if ((removedallocs = mySub.getRemovedList()).hasMoreElements ()) {
      while (removedallocs.hasMoreElements()) {
	j++;
	Allocation alloc = (Allocation) removedallocs.nextElement();
	if (seenAllocsHash.get (alloc) == null) {
	  ((UTILAllocationListener) myListener).handleRemovedAlloc(alloc);
	  seenAllocsHash.put (alloc, alloc);
	} else if (logger.isDebugEnabled()) 
	  logger.debug ("UTILAllocationCallback : " + 
			"Duplicate removed alloc for task " + 
			alloc.getTask ().getUID () + " ignored.");
      }
    }


    if (logger.isDebugEnabled()) {
      if (i > 0 || j > 0)
	logger.debug ("UTILAllocationCallback " + this + 
		      " (listener = " + getClassName () + 
		      " had " + i + 
		      " changed and " + j + 
		      " removed allocs");

      if (mySub.getAddedList().hasMoreElements ())
	logger.debug ("UTILAllocationCallback : " + 
		      "Allocations were added (ignored by callback).");

      logger.debug ("UTILAllocationCallback " + this + 
		    " (listener = " + getClassName () + 
		    ") exited.");
    }
  }

  protected String getClassName () {
    String classname = myListener.getClass().getName ();
    int index = classname.lastIndexOf (".");
    classname = classname.substring (index+1, classname.length ());
    return classname;
  }

  /**
   * Defines protocol for dealing with Allocations as they change over time.
   *
   * Allocator plugins have two shots at dealing with failed allocations:
   *  1) immediately, as they happen -- hook here is handleRescindedAlloc
   *     Here the plugin can realloc immediately upon failure. 
   *  2) the next cycle -- when the allocation is removed, the task for that
   *     allocation is now as it was initially, and so ready to allocate.
   *     Perhaps now the plugin can group it with other tasks and allocate
   *     differently.  
   *        NOTE : This option can result in infinite loops :
   *        if the plugin doesn't do anything different the second time, the 
   *        allocation will fail again, be rescinded, and we're back where we 
   *        started.
   *
   * @param alloc Allocation to examine
   */
  protected void reactToChangedAlloc (Allocation alloc) {
    UTILAllocationListener listener = (UTILAllocationListener) myListener;
    if (listener.needToRescind(alloc)){
      listener.publishRemovalOfAllocation(alloc);
      // need to call this instead of allocate(Task) because the cluster
      // needs to know that the current resource is rejected.
      listener.handleRescindedAlloc(alloc);
    }
    else {
      listener.updateAllocationResult(alloc);
      listener.handleSuccessfulAlloc(alloc);
    }
  }
}
        
        
                
                        
                
        
        
