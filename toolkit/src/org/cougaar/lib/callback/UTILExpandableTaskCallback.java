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
import java.util.Iterator;

/**
 * For use with (threaded?) expanders.
 * 
 * Filters for tasks without workflows or plan elements.
 */

public class UTILExpandableTaskCallback extends UTILFilterCallbackAdapter implements UTILRehydrateReactor {
  public UTILExpandableTaskCallback (UTILGenericListener listener) {
    super (listener);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if ( o instanceof Task ) {
	  if (xxdebug) {
	    System.out.println("T:"+o);
	    System.out.println("W:"+((Task)o).getWorkflow());
	    System.out.println("PE:"+((Task)o).getPlanElement());
	  }
	  return ( (((Task)o).getWorkflow() == null )  &&
		   (((Task)o).getPlanElement() == null ) &&
		   ((UTILGenericListener) myListener).interestingTask ((Task) o)); 
	}
	return false;
      }
    };
  }

  /**
   * Called when new, changed, or removed tasks fall into or out of container.
   *
   * Tells listener about new tasks that are fit to be expanded.  Checks each
   * to see if well formed.  Only tells listener of well formed tasks.
   *
   * BOZO 01/11/00 GWFV :
   *
   * For now, does NOT tell of changed tasks. This may be a problem in some
   * situations.  Should tackle them as they arise.
   *
   *
   * Has debug info to tell how many new tasks have arrived.  Also to tell
   * how many expandable tasks have been removed from container.
   *
   * @see #isWellFormed
   */

  public void reactToChangedFilter () {
    Enumeration newtasks = mySub.getAddedList();
    boolean anythingChanged = newtasks.hasMoreElements();
    int i = 0;

    Collection removedCollection = mySub.getRemovedCollection ();

    while (newtasks.hasMoreElements()) {
      Task t = (Task) newtasks.nextElement();
	  if (removedCollection.contains (t))
		continue;
	  
      if (isWellFormed (t)) {
	((UTILGenericListener) myListener).handleTask (t);
	if (xxdebug) 
	  System.out.println ("UTILExpandableTaskCallback : Notifying " + myListener + 
			      " about " + t.getUID());
	i++;
      }
    }


    Enumeration changedTasks = mySub.getChangedList();
    if (!anythingChanged)
      anythingChanged = changedTasks.hasMoreElements();

    while (changedTasks.hasMoreElements()) {
      Task changedT = (Task)changedTasks.nextElement();
	  if (removedCollection.contains (changedT))
		continue;
      if (isWellFormed (changedT)) {
	((UTILGenericListener) myListener).handleTask(changedT);
	System.out.println ("UTILExpandableTaskCallback : Notifying " + myListener + 
			    " about changed task " + changedT.getUID());
	i++;
      }
    }


    if (anythingChanged)
      synchronized (myListener) {
	if (xxdebug)
	  System.out.println ("UTILExpandableTaskCallback : Notifying " + myListener + 
			      " about " + i + 
			      " tasks");
	myListener.notify ();
      }

	if (mySub.getRemovedList().hasMoreElements ()) {
	  Enumeration removedtasks = mySub.getRemovedList();
	  while (removedtasks.hasMoreElements()) {
		Task t = (Task) removedtasks.nextElement();
		if (xxdebug)
		  System.out.println ("UTILExpandableTaskCallback : Telling listener that task " + t.getUID() + 
							  " was removed.");
		((UTILGenericListener) myListener).handleRemovedTask(t);
	  }
	}
	
    if (xxdebug) {
      if (mySub.getRemovedList().hasMoreElements ()) {
	Enumeration removedtasks = mySub.getRemovedList();
	while (removedtasks.hasMoreElements()) {
	  Task t = (Task) removedtasks.nextElement();
	  System.out.println ("UTILExpandableTaskCallback : Expandable task " + t + 
			      " was removed from container.");
	}
      }
    }
  }

  /** place where you can react to rehydration event */
  public void reactToRehydrate () {
    Collection contents = mySub.getCollection ();
	
	if (xdebug || xxdebug || true) 
	  System.out.println ("UTILExpandableTaskCallback.reactToRehydrate - Notifying " + myListener + 
						  " about " + contents.size () + " previously buffered tasks.");

    for (Iterator iter = contents.iterator (); iter.hasNext ();) {
      Task t = (Task) iter.next();
	  
      if (isWellFormed (t)) {
		((UTILGenericListener) myListener).handleTask (t);
		if (xxdebug) 
		  System.out.println ("UTILExpandableTaskCallback.reactToRehydrate - Notifying " + myListener + 
							  " about " + t.getUID());
      }
    }
	synchronized (myListener) {  myListener.notify ();	}
  }  

  /**
   * NOTE : duplicate in UTILWorkflowCallback -- should make common base class later!
   *
   * Examines an incoming task to see if it is well formed.
   * Looks at timing information, and asks listener to examine
   * task as well.  If task is ill formed, asks listener to handle
   * it (probably publish as a failed plan element).
   */
  protected boolean isWellFormed (Task task) {
    UTILGenericListener genericListener = 
      (UTILGenericListener) myListener;

    if (genericListener.isTaskWellFormed(task)) 
      return true;
    else
      genericListener.handleIllFormedTask (task);

    return false;
  }
}
        
        
                
                        
                
        
        
