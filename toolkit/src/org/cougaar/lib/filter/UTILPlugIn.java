/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.param.ParamTable;

/**
 * Plugins listen for organization changes.
 *
 * Plugins have parameters.
 * Plugins set up filterCallbacks.
 *
 * Plugins dispatch filter changes
 * to the appropriate filter. This is the heart of the plugin.
 */

public interface UTILPlugIn {
  /**
   * The idea is to add subscriptions, and when they change, to
   * invoke the subscription's callback.
   */
  void setupFilters ();

  /**
   * Place to put any local plugin startup initiallization.
   * This is a good place to read local data from files.
   */
  void localSetup ();

  /** Add a filter callback */
  void addFilter  (UTILFilterCallback callbackObj);

  /** Allows child classes to read additional data from environment files. */
  void getEnvData ();

  /** params for this plugin */
  ParamTable getMyParams ();

  /**
   * turns on debugging info if a failed pe is encountered
   *
   */
  void showDebugIfFailure ();
}
