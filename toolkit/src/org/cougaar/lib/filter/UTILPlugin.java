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

public interface UTILPlugin {
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
   * turns on infoging debug if a failed pe is encountered
   *
   */
  // void showDebugIfFailure ();
}
