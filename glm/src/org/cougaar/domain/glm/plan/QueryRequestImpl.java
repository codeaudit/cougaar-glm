/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.UnaryPredicate;

public class QueryRequestImpl
  implements QueryRequest,  java.io.Serializable
{

  UnaryPredicate _requestPredicate;
  ClusterIdentifier _sourceCid;
  ClusterIdentifier _requestingCid;

  public QueryRequestImpl(UnaryPredicate requestPredicate,
			   ClusterIdentifier sourceCid,
			   ClusterIdentifier requestingCid) {
    _requestPredicate = requestPredicate;
    _sourceCid = sourceCid;
    _requestingCid = requestingCid;
  }

  public UnaryPredicate getQueryPredicate() {
    return _requestPredicate;
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

