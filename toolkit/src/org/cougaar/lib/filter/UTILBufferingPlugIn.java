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

import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.lib.callback.UTILFilterCallbackListener;

import org.cougaar.lib.param.ParamMap;

import java.util.List;

/**
 * The interface between the plugin and the BufferingThread.
 *
 * Defines a one-way flow of communication between the BufferingThread
 * and the plugin.
 *
 * The buffering thread needs parameters from the plugin, a way to
 * ask it which tasks are interesting (since it's the listener, not the
 * plugin), it needs to be able to lock the container, and finally tell the
 * plugin when it has buffered up the required number of tasks.
 *
 * Since processTasks takes an Enumeration, a BufferingThread subclass could 
 * give the BufferingPlugIn a list of non-tasks (workflows? allocations?) 
 * that it wished to buffer.
 */

public interface UTILBufferingPlugIn extends UTILFilterCallbackListener, UTILTaskChecker {
  /** get readable, unique name of plugin */
  String getName ();

  /** get parameters for plugin */
  ParamMap getMyParams ();

  /** 
   * Defines tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingTask(Task t);

  /** needed to lock the subscriber in the nested class */
  void startTransaction();

  /** needed to unlock the subscriber in the nested class */
  void endTransaction();

  /**
   * Deal with the tasks that we have accumulated.
   * Derived plugins must implement this.
   * @param tasks the tasks to handle
   */
  void processTasks (List tasks);
}
