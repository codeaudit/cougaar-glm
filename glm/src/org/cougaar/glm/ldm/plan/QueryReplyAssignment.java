/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.glm.ldm.plan;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.Directive;
import org.cougaar.core.blackboard.DirectiveImpl;
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
  
  private UnaryPredicate _localPredicate;

  public QueryReplyAssignment(Collection reply, 
			      UnaryPredicate queryPredicate,
			      MessageAddress source,
			      MessageAddress dest) {

    _queryReply = reply;
    _predicate = queryPredicate;
    super.setSource(source);
    super.setDestination(dest);
    _localPredicate = null;
  }

  public QueryReplyAssignment(Collection reply, 
			      UnaryPredicate queryPredicate,
			      UnaryPredicate localPredicate,
			      MessageAddress source,
			      MessageAddress dest) {

    _queryReply = reply;
    _predicate = queryPredicate;
    _localPredicate = localPredicate;
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

  /**
   * @return the UnaryPredicate from the request. Used when queryResponse is null
   */
  public UnaryPredicate getLocalPredicate() {
    return _localPredicate;
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
