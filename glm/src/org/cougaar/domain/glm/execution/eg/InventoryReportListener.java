/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
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
public class InventoryReportListener extends Listener {
  public static final String PSP_id = "INVENTORY_REPORT.PSP";

  public InventoryReportListener(ClusterInfo clusterInfo,
				 Object[] handlers,
				 String itemIdentification,
                                 long aReportDate)
    throws IOException
  {
    super("InventoryReport",
          clusterInfo,
          PSP_id,
          makeQuery(itemIdentification, aReportDate),
          handlers);
  }

  private static EGObject makeQuery(String itemIdentification, long aReportDate) {
    return new InventoryReportParameters(itemIdentification, aReportDate);
  }
}
