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
import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.planning.ldm.plan.DirectiveImpl;
import org.cougaar.util.UnaryPredicate;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * The message sent back from a cluster containing the result of 
 * a QueryRequest.
 */

public class QueryReplyAssignment extends DirectiveImpl
{
  private Collection  _queryReply;

  /** predicate from request. Used in for reporting in case the object is missing */
  private UnaryPredicate _predicate;

  public QueryReplyAssignment(Collection reply, 
			      UnaryPredicate queryPredicate,
			      ClusterIdentifier source,
			      ClusterIdentifier dest) {

    _queryReply = reply;
    _predicate = queryPredicate;
    super.setSource(source);
    super.setDestination(dest);
  }

  /**
   * @return the found object
   */
  public Collection getQueryResponse() {
    return _queryReply;
  }
  
  /**
   * @return the UnaryPredicate from the request. Used when queryResponse is null
   */
  public UnaryPredicate getRequestPredicate() {
    return _predicate;
  }

  // Shouldn't really be used, but here for completeness.
  // The other "set"  methods are inherited
  public void setQueryReply(Collection reply) {
    _queryReply = reply;
  }

  public String toString() {
    String descr = "(Null AssignedQueryReply)";
    if( _queryReply != null ) descr = _queryReply.toString();

    return "<QueryReplyAssignment "+descr+", " + ">" + super.toString();
  }
}
