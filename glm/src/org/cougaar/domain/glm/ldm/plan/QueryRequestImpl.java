/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.UnaryPredicate;

public class QueryRequestImpl
  implements QueryRequest,  java.io.Serializable
{

  UnaryPredicate _requestPredicate;
  UnaryPredicate _localPredicate;
  ClusterIdentifier _sourceCid;
  ClusterIdentifier _requestingCid;

  public QueryRequestImpl(UnaryPredicate requestPredicate,
                          ClusterIdentifier sourceCid,
                          ClusterIdentifier requestingCid) {
    _requestPredicate = requestPredicate;
    _sourceCid = sourceCid;
    _requestingCid = requestingCid;
    _localPredicate = null;
  }

  public QueryRequestImpl(UnaryPredicate requestPredicate,
                          UnaryPredicate localPredicate,
                          ClusterIdentifier sourceCid,
                          ClusterIdentifier requestingCid) {
    _requestPredicate = requestPredicate;
    _localPredicate = localPredicate;
    _sourceCid = sourceCid;
    _requestingCid = requestingCid;
  }

  public UnaryPredicate getQueryPredicate() {
    return _requestPredicate;
  }

  public UnaryPredicate getLocalQueryPredicate() {
    return _localPredicate;
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




