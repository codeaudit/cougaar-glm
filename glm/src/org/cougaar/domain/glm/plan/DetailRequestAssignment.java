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
import org.cougaar.core.society.UID;
import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.planning.ldm.plan.DirectiveImpl;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class DetailRequestAssignment extends DirectiveImpl
{
  private DetailRequest _detailRequest;

  public DetailRequestAssignment(DetailRequest request) {

    _detailRequest = request;
    // we are sending this from the requesting cluster
    super.setSource(request.getRequestingCluster());

    // The source of the information, not the source of the request
    super.setDestination(request.getSourceCluster());
  }

  public DetailRequest getDetailRequest() {
    return _detailRequest;
  }
  
  public void setDetailRequest(DetailRequest request) {
    _detailRequest = request;
  }

  public String toString() {
    String descr = "(Null AssignedDetailRequest)";
    if( _detailRequest != null ) descr = _detailRequest.toString();

    return "<DetailRequestAssignment "+descr+", " + ">" + super.toString();
  }
}
