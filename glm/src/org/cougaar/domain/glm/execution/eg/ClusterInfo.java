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

/**
 * Keep track in bits of information about an individual cluster.
 **/
public class ClusterInfo {
  public String theClusterName;
  public String theClusterURL;
  public ExecutionTimeStatus theExecutionTimeStatus;

  public ClusterInfo(String aClusterName, String aClusterURL) {
    theClusterName = aClusterName;
    theClusterURL = aClusterURL;
  }
}
