/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.ldm.plan;

import java.util.Collection;

import org.cougaar.core.blackboard.DirectiveImpl;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.util.UnaryPredicate;

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
