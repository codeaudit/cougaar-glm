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

import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.util.UnaryPredicate;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import org.cougaar.util.log.Logger;
import org.cougaar.lib.filter.UTILListeningBufferingThread;

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

  /** place where you can react to rehydration event */
  public void reactToRehydrate () {
    Collection contents = mySub.getCollection ();
	
    if (logger.isInfoEnabled())
      logger.info ("UTILBufferingCallback.reactToRehydrate - Notifying " + myListener + 
		   " about " + contents.size () + " previously buffered tasks.");

    for (Iterator iter = contents.iterator (); iter.hasNext ();) {
      Task t = (Task) iter.next();
	  
      if (isWellFormed (t)) {
	((UTILGenericListener) myListener).handleTask (t);
	if (logger.isDebugEnabled())
	  logger.debug ("UTILBufferingCallback.reactToRehydrate - Notifying " + myListener + 
			" about " + t.getUID());
      }
    }
    ((UTILGenericListener) myListener).wakeUp();
  }  


  protected boolean isWellFormed (Task task) { return true; }

  boolean listenerIsBuffering = myListener instanceof UTILListeningBufferingThread;
}
