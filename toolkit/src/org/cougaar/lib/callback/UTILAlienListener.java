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

/**
 * Listener for use with the record/playback system.
 *
 * Listens for tasks that came from outside (alien!) clusters.
 * I.e. tasks expanded by the cluster will *not* be heard.
 * 
 * All the plugin should have to implement is interestingTask and handleTask.
 */

public interface UTILAlienListener extends UTILFilterCallbackListener {
  /** 
   * Defines tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingTask(Task t);

  /**
   * If the listener is a descendant of UTILPluginAdapter, 
   * should not have to write anything,
   * since it already has a method with this name.
   * 
   * @see org.cougaar.lib.filter.UTILPluginAdapter#getClusterName
   */
  String getClusterName ();
  /**
   * What the plugin does when it finds an Alien task.
   */
  void    handleTask     (Task t);
}
