/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
import org.cougaar.util.log.Logger;

/**
 * For use with (threaded?) expanders.
 * 
 * Filters for tasks without workflows or plan elements.
 */

public class UTILExpandableTaskCallback extends UTILBufferingCallback implements UTILRehydrateReactor {
  public UTILExpandableTaskCallback (UTILGenericListener listener, Logger logger) {
    super (listener, logger);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
	public boolean execute(Object o) {
	  if ( o instanceof Task ) {
	    if (logger.isDebugEnabled()) {
	      logger.debug("T:"+o);
	      logger.debug("W:"+((Task)o).getWorkflow());
	      logger.debug("PE:"+((Task)o).getPlanElement());
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
   * NOTE : duplicate in UTILWorkflowCallback -- should make common base class later!
   *
   * Examines an incoming task to see if it is well formed.
   * Looks at timing logger.information, and asks listener to examine
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
