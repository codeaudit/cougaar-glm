/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.mlm.ui.planviewer.inventory;

import org.cougaar.glm.execution.eg.Listener;
import org.cougaar.glm.execution.eg.ClusterInfo;
import org.cougaar.glm.execution.common.EGObject;
import org.cougaar.glm.execution.common.ExecutionWatcherParameters;

import java.io.IOException;

/**
 * Connects to and listens to the output of the PSP_EventWatcher.
 * Dispatches the received objects to the appropriate handler.
 **/
public class InventoryExecutionListener extends Listener {
  public static final String PSP_id = "EXECUTION_WATCHER.PSP";

  public InventoryExecutionListener(ClusterInfo clusterInfo, Object[] handlers)
    throws IOException
  {
    super("ExecutionListener/" + clusterInfo.theClusterName,
          clusterInfo, PSP_id, makeQuery(), handlers);
  }

  private static EGObject makeQuery() {
    return new ExecutionWatcherParameters(10000L,true);
  }
}
