/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.planning.ldm.plan.DirectiveImpl;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.society.UID;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * The message sent back from a cluster containing the result of 
 * a DetailRequest.
 */

public class DetailReplyAssignment extends DirectiveImpl
{
  private UniqueObject  _detailReply;

  /** _uid of requested object. Used in for reporting in case the object is missing */
  private UID _uid;

  public DetailReplyAssignment(UniqueObject replyObj, 
			       UID uid,
			       ClusterIdentifier source,
			       ClusterIdentifier dest) {

    _detailReply = replyObj;
    _uid = uid;
    super.setSource(source);
    super.setDestination(dest);
  }

  /**
   * @return the found object
   */
  public UniqueObject getDetailObject() {
    return _detailReply;
  }
  
  /**
   * @return the UID from the request. Used when detailObject is null
   */
  public UID getRequestUID() {
    return _uid;
  }

  // Shouldn't really be used, but here for completeness.
  // The other "set"  methods are inherited
  public void setDetailReply(UniqueObject reply) {
    _detailReply = reply;
  }

  public String toString() {
    String descr = "(Null AssignedDetailReply)";
    if( _detailReply != null ) descr = _detailReply.toString();

    return "<DetailReplyAssignment "+descr+", " + ">" + super.toString();
  }
}
