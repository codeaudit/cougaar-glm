package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Connects to and listens to the output of the PSP_EventWatcher.
 * Dispatches the received objects to the appropriate handler.
 **/
public class ExecutionListener extends Listener {
  public static final String PSP_id = "EXECUTION_WATCHER.PSP";

  public ExecutionListener(ClusterInfo clusterInfo, Object[] handlers)
    throws IOException
  {
    super("ExecutionListener/" + clusterInfo.theClusterName,
          clusterInfo, PSP_id, makeQuery(), handlers);
  }

  private static EGObject makeQuery() {
    return new ExecutionWatcherParameters(30000L);
  }
}
