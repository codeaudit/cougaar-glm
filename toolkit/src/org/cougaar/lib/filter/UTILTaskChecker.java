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

import org.cougaar.planning.ldm.plan.Task;

/**
 * Interface for listeners that check task consistency.
 * 
 */

public interface UTILTaskChecker {
  /** 
   * Examines task to see if task looks like what the plugin 
   * expects it to look like.
   *
   * This is plugin-dependent.  For example, the
   * planning factor for unloading a ship is 2 days, but
   * if the task's time window is 1 day, the task is
   * not "well formed."  Duration is a common test, but others
   * could also be a good idea...
   *
   * This is an explicit contract with the plugin
   * that feeds this plugins tasks, governing which tasks
   * are possible for this plugin to handle.
   *
   * @param t Task to check for consistency
   * @return true if task is OK
   */
  boolean isTaskWellFormed    (Task t);

  /** 
   * Got an ill-formed task, now handle it, probably by
   * publishing a failed plan element for the task.
   * @param t badly-formed task to handle
   */
  void    handleIllFormedTask (Task t);
}
