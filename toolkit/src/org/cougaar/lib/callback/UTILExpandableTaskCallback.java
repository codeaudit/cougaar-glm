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

import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;

/**
 * For use with (threaded?) expanders.
 * 
 * Filters for tasks without workflows or plan elements.
 */

public class UTILExpandableTaskCallback extends UTILFilterCallbackAdapter {
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

    while (newtasks.hasMoreElements()) {
      Task t = (Task) newtasks.nextElement();
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
        
        
                
                        
                
        
        
