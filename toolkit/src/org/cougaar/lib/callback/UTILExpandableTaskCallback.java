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

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
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
