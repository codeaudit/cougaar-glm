/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.lib.filter;

import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.param.ParamMap;

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
  ParamMap getMyParams ();

  /**
   * turns on debugging info if a failed pe is encountered
   *
   */
  void showDebugIfFailure ();
}
