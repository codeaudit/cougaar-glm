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

package org.cougaar.lib.filter;

import java.util.Collection;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 * <pre>
 * A buffering thread that is a generic listener.
 * 
 * Paired with an ExpandableTaskCallback, it can be used
 * in a buffering expander plugin.
 *
 * Paired with an WorkflowCallback, it can be used
 * in a buffering allocator plugin.
 * 
 * Assumes that the buffered objects are tasks.
 * </pre>
 */

public class UTILListeningBufferingThread 
  extends UTILBufferingThread 
  implements UTILGenericListener {

  /** 
   * Works with a buffering plugin
   */
  public UTILListeningBufferingThread (UTILBufferingPlugin bufferingPlugin, Logger logger) {
    super (bufferingPlugin, logger);
  }

  /** 
   * Passes interesting test on to plugin
   */

  public boolean interestingTask(Task t) {
    return myPlugin.interestingTask (t);
  }

  public void handleAll (Collection tasks) {
    addAll (tasks);
  }

  /** 
   * Handling a task as a generic listener means buffering it.
   * When the task thresholds are reached, then UTILBufferingThread
   * will call processTasks on BufferingPlugin.
   */
  public void handleTask (Task t) { 
    if (logger.isDebugEnabled())
      logger.debug (classname + 
	     ".handleTask : " + this + " got task " + t + 
	     "\nfrom " + t.getSource ());
    addTask (t); 
  }

  public void handleRemovedTask (Task t) {
    if (logger.isDebugEnabled())
      logger.debug (classname + 
	     ".handleRemovedTask : " + this + " got task " + t + 
	     "\nfrom " + t.getSource ());

    removeTask (t);
  }
  
  /** 
   * Asks listener to examine task
   *
   * @param t Task to check for consistency
   * @return true if task is OK
   */
  public boolean isTaskWellFormed(Task t) {
    return myPlugin.isTaskWellFormed(t);
  }

  /** 
   * Got an ill-formed task, now plugin should handle it.
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    myPlugin.handleIllFormedTask (t);
  }

  /** All listeners must be able to create a subscription */
  public IncrementalSubscription subscribeFromCallback(UnaryPredicate pred) {
    return myPlugin.subscribeFromCallback(pred);
  }

  /** 
   * All listeners must be able to create a subscription with a special container
   */
  public IncrementalSubscription subscribeFromCallback(UnaryPredicate pred,
						       Collection specialContainer) {
    return myPlugin.subscribeFromCallback(pred, specialContainer);
  }

  public void wakeUp () {
    if (logger.isDebugEnabled())
      logger.debug("UTILListeningThread about to do plugin.wakeUp");
    myPlugin.wakeUp ();
  }

  private static final String classname = 
    UTILListeningBufferingThread.class.getName ();
}
