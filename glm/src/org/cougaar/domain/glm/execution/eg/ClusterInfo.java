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
