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

import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 * Filters for MP tasks, where the tasks
 * meet the test of isInteresting.
 *
 */

public class UTILMPWorkflowCallback extends UTILWorkflowCallback {
  public UTILMPWorkflowCallback (UTILGenericListener listener, Logger logger) {
    super (listener, logger);
  }

  /**
   * Filters for mp tasks
   *
   * They are tested
   * against the plugin-specific interestingTask test.
   *
   * set logger.isDebugEnabled() to true if you want to see logger.info on every 
   * handled task.  That is, which tasks have been 
   * allocated and which failed to allocate.
   * (Previously handled tasks will not be given to the listener.)
   *
   * @return anonymous UnaryPredicate inner class
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof MPTask) {
	  MPTask subtask = (MPTask) o;

	  boolean hasBeenAllocated =
	    (subtask.getPlanElement () != null);

	  if (logger.isDebugEnabled()) 
	    debugInfo (subtask, hasBeenAllocated);

	  if (!hasBeenAllocated &&
	      ((UTILGenericListener) myListener).interestingTask (subtask))
	    return true;
	}
	return false;
      }
    };
  }
}
