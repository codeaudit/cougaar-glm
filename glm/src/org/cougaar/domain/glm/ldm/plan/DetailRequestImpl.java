/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.society.UID;

public class DetailRequestImpl
  implements DetailRequest,  java.io.Serializable
{

  UID _requestedObject;
  ClusterIdentifier _sourceCid;
  ClusterIdentifier _requestingCid;

  public DetailRequestImpl(UID requestedObject,
			   ClusterIdentifier sourceCid,
			   ClusterIdentifier requestingCid) {
    _requestedObject = requestedObject;
    _sourceCid = sourceCid;
    _requestingCid = requestingCid;
  }

  public UID getDetailUID() {
    return _requestedObject;
  }

  /**
   * The cluster where the object lives, not the source of the request
   */

  public ClusterIdentifier getSourceCluster() {
    return  _sourceCid;
  }

  /**
   * The cluster requesting the object. The cluster the reply message will be sent to
   */
  public ClusterIdentifier getRequestingCluster() {
    return  _requestingCid;
  }
}

