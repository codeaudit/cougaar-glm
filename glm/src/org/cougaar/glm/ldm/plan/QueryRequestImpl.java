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

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.util.UnaryPredicate;

public class QueryRequestImpl
  implements QueryRequest,  java.io.Serializable
{

  UnaryPredicate _requestPredicate;
  UnaryPredicate _localPredicate;
  MessageAddress _sourceCid;
  MessageAddress _requestingCid;

  public QueryRequestImpl(UnaryPredicate requestPredicate,
                          MessageAddress sourceCid,
                          MessageAddress requestingCid) {
    _requestPredicate = requestPredicate;
    _sourceCid = sourceCid;
    _requestingCid = requestingCid;
    _localPredicate = null;
  }

  public QueryRequestImpl(UnaryPredicate requestPredicate,
                          UnaryPredicate localPredicate,
                          MessageAddress sourceCid,
                          MessageAddress requestingCid) {
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

  public MessageAddress getSourceCluster() {
    return  _sourceCid;
  }

  /**
   * The cluster requesting the object. The cluster the reply message will be sent to
   */
  public MessageAddress getRequestingCluster() {
    return  _requestingCid;
  }
}




