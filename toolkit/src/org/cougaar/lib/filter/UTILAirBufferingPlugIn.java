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

import java.util.Enumeration;

import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.callback.UTILFilterCallbackListener;

import org.cougaar.lib.param.ParamTable;

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

public interface UTILAirBufferingPlugIn extends UTILBufferingPlugIn {
    boolean anyTasksLeft ();

    void processLeftoverTasks ();
}
