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

import org.cougaar.core.blackboard.DirectiveImpl;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

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
			       MessageAddress source,
			       MessageAddress dest) {

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
