/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Task;

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
   * If the listener is a descendant of UTILPlugInAdapter, 
   * should not have to write anything,
   * since it already has a method with this name.
   * 
   * @see org.cougaar.lib.filter.UTILPlugInAdapter#getClusterName
   */
  String getClusterName ();
  /**
   * What the plugin does when it finds an Alien task.
   */
  void    handleTask     (Task t);
}
