/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import org.cougaar.domain.glm.execution.eg.Listener;
import org.cougaar.domain.glm.execution.eg.ClusterInfo;
import org.cougaar.domain.glm.execution.common.EGObject;
import org.cougaar.domain.glm.execution.common.ExecutionWatcherParameters;

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
