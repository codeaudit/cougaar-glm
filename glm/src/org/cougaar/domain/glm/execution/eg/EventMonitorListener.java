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
public class EventMonitorListener extends Listener {
  public static final String PSP_id = "EVENT_MONITOR.PSP";

  public EventMonitorListener(ClusterInfo clusterInfo,
                              Object[] handlers,
                              Report aReport)
    throws IOException
  {
    super("EventMonitor",
          clusterInfo,
          PSP_id,
          aReport,
          handlers);
  }
}
