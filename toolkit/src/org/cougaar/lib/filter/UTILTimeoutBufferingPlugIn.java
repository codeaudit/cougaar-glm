/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

/**
 * The interface between the plugin and the TimeoutBufferingThread.
 *
 */

public interface UTILTimeoutBufferingPlugIn extends UTILBufferingPlugIn {
  /** are there any tasks left at the plugin */
  boolean anyTasksLeft ();

  /** tell plugin to process those tasks */
  void processLeftoverTasks ();
}
