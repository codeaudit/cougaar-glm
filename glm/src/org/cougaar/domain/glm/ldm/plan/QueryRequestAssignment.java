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
import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.planning.ldm.plan.DirectiveImpl;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class QueryRequestAssignment extends DirectiveImpl
{
  private QueryRequest _queryRequest;

  public QueryRequestAssignment(QueryRequest request) {

    _queryRequest = request;
    // we are sending this from the requesting cluster
    super.setSource(request.getRequestingCluster());

    // The source of the information, not the source of the request
    super.setDestination(request.getSourceCluster());
  }

  public QueryRequest getQueryRequest() {
    return _queryRequest;
  }
  
  public void setQueryRequest(QueryRequest request) {
    _queryRequest = request;
  }

  public String toString() {
    String descr = "(Null AssignedQueryRequest)";
    if( _queryRequest != null ) descr = _queryRequest.toString();

    return "<QueryRequestAssignment "+descr+", " + ">" + super.toString();
  }
}
