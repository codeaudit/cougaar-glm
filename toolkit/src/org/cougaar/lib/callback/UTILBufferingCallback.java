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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.cougaar.lib.filter.UTILListeningBufferingThread;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.log.Logger;

/**
 * For use with (threaded?) expanders.
 * 
 * Filters for tasks without workflows or plan elements.
 */

public class UTILBufferingCallback extends UTILFilterCallbackAdapter {
  public UTILBufferingCallback (UTILGenericListener listener, Logger logger) {
    super (listener, logger);

    listenerIsBuffering = myListener instanceof UTILListeningBufferingThread;
  }
  /**
   * Called when new, changed, or removed tasks fall into or out of container.
   *
   * Tells listener about new tasks that are fit to be handled.  Checks each
   * to see if well formed.  Only tells listener of well formed tasks.
   *
   * Has debug logger.info to tell how many new tasks have arrived.  Also to tell
   * how many expandable tasks have been removed from container.
   *
   * @see #isWellFormed
   */

  public void reactToChangedFilter () {
    int i = 0;

    Collection removedCollection = mySub.getRemovedCollection ();

    Collection tasksForListener = new HashSet();

    for (Iterator iter = mySub.getAddedCollection ().iterator(); iter.hasNext();) {
      Task t = (Task) iter.next();
      if (removedCollection.contains (t))
	continue;
	  
      if (isWellFormed (t)) {

	if (!listenerIsBuffering)
	  ((UTILGenericListener) myListener).handleTask (t);
	else
	  tasksForListener.add (t);

	if (logger.isDebugEnabled()) 
	  logger.debug ("UTILBufferingCallback : Notifying " + myListener + 
			" about " + t.getUID());
	i++;
      }
    }

    for (Iterator iter = mySub.getChangedCollection().iterator(); iter.hasNext();) {
      Task changedT = (Task)iter.next();
      if (removedCollection.contains (changedT))
	continue;
      if (isWellFormed (changedT)) {

	if (!listenerIsBuffering)
	  ((UTILGenericListener) myListener).handleTask (changedT);
	else
	  tasksForListener.add (changedT);

	if (logger.isDebugEnabled()) 
	  logger.debug ("UTILBufferingCallback : Notifying " + myListener + 
			" about changed task " + changedT.getUID());
	i++;
      }
    }

    if (listenerIsBuffering)
      ((UTILListeningBufferingThread) myListener).handleAll (tasksForListener);

    if (!mySub.getAddedCollection().isEmpty() || 
	!mySub.getChangedCollection().isEmpty()) {
      // synchronize ????????
      if (logger.isInfoEnabled())
	logger.info ("UTILBufferingCallback : Notifying " + myListener + 
		     " about " + i + 
		     " tasks");
      if (logger.isInfoEnabled())
	logger.info("mySub had added or changed tasks - regardless of whether they are wellformed, we are waking up the listener");
      ((UTILGenericListener) myListener).wakeUp();
    }

    if (!mySub.getRemovedCollection().isEmpty ()) {
      for (Iterator iter = mySub.getRemovedCollection().iterator(); iter.hasNext();) {
	Task t = (Task) iter.next();
	if (logger.isDebugEnabled())
	  logger.debug ("UTILBufferingCallback : Telling listener that task " + t.getUID() + 
			" was removed.");
	((UTILGenericListener) myListener).handleRemovedTask(t);
      }
    }
  }

  /** 
   * Place where you can react to rehydration event.  
   *
   * If the blackboard says "didRehydrate" then each callback 
   * that is a RehydrateReactor will be called here.  
   * This can happen on an agent move too.
   *
   * NOTE : The plugin will only look at *unplanned* tasks on rehydration.
   *        I don't think this will cause problems.
   *        Fix for bug #3356: http://bugs.cougaar.org/show_bug.cgi?id=3356
   *
   * @see org.cougaar.lib.filter.UTILPluginAdapter#execute
   */
  public void reactToRehydrate () {
    Collection contents = mySub.getCollection ();
	
    if (logger.isInfoEnabled())
      logger.info ("UTILBufferingCallback.reactToRehydrate - Notifying " + myListener + 
		   " about " + contents.size () + " previously buffered tasks.");

    // Only want to call wakeUp if some tasks still match
    boolean workToBeDone = false;
    for (Iterator iter = contents.iterator (); iter.hasNext ();) {
      Task t = (Task) iter.next();
      
      if (isWellFormed (t)) {
	if (t.getPlanElement() == null) { // fix for bug #3356 - only look at unplanned tasks on rehydration
	  workToBeDone = true; // Will need to have plugin wake up later
	  ((UTILGenericListener) myListener).handleTask (t);
	  if (logger.isDebugEnabled())
	    logger.debug ("UTILBufferingCallback.reactToRehydrate - Notifying " + myListener + 
			  " about " + t.getUID());
	}
      }
    }

    if (workToBeDone) {
      if (logger.isDebugEnabled())
	logger.debug("UTILBufCal.react: asking listener to wake up?");
      ((UTILGenericListener) myListener).wakeUp();
    }
  }  


  protected boolean isWellFormed (Task task) { return true; }

  boolean listenerIsBuffering = myListener instanceof UTILListeningBufferingThread;
}
