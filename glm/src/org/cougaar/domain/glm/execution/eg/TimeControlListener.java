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
public class TimeControlListener extends Listener {
  public static final String PSP_id = "TIME_CONTROL.PSP";

  public TimeControlListener(ClusterInfo clusterInfo,
                             SetExecutionTime aRate)
    throws IOException
  {
    super("TimeControl",
          clusterInfo,
          PSP_id,
          aRate,
          emptyHandlers);
  }
}
