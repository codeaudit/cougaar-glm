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
package org.cougaar.mlm.plugin.organization;

import java.io.Serializable;

import java.util.Set;
import java.util.Collections;

import org.cougaar.core.relay.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.*;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * Relay for subordinates to report to their operational 
 * superior.  The subordinate needs to obtain the ItemId
 * and TypeId of their OpCon (operational superior) in
 * order to generate the ReportForDuty task.
 * Used by the OrgDataPlugin
 **/
public class OpConInfoRelay
  implements Relay.Source, Relay.Target, UniqueObject {

  private static Logger myLogger = Logging.getLogger(OpConInfoRelay.class);

  private MessageAddress source = null;
  private MessageAddress target = null;
  private UID uid = null;

  private Object content = null;
  private Object response = null;

  private transient Set targets = null;

  public OpConInfoRelay(UID uid,
		      MessageAddress source,
		      MessageAddress target,
		      Object content,
                      Object response) {
    this.uid = uid;
    this.source = source;
    this.target = target;
    this.content = content;
    this.response = response;
  }

  // UniqueObject interface

  public void setUID(UID uid) {
    throw new RuntimeException("Attempt to change UID");
  }

  public UID getUID() {
    return uid;
  }


  /**
   * Address of the agent that was contacted.
   * @return The address of the agent.
   */
  public MessageAddress getTarget() {
    return target;
  }

  // Source interface

  public Set getTargets() {
    if (targets == null) {
      targets = ((target != null) ?
		 Collections.singleton( target ) :
		 Collections.EMPTY_SET);
    }
	
    return targets;
  }

  public Object getContent() {
    return content;
  }

  private static final class SimpleRelayFactory
    implements TargetFactory, Serializable {
    
    public static final SimpleRelayFactory _instance =
      new SimpleRelayFactory();

    private SimpleRelayFactory() {}

    public Relay.Target create( UID uid,
				MessageAddress source,
				Object content,
				Token token) {
      return new OpConInfoRelay( uid, source, null, content, null );
    }
    
    private Object readResolve() {
      return _instance;
    }
  }

  public TargetFactory getTargetFactory() {
    return SimpleRelayFactory._instance;
  }

  public int updateResponse( MessageAddress t, Object response ) {
    // assert response != null
    if (!(response.equals(this.response))) {
      this.response = response;
      return Relay.RESPONSE_CHANGE;
    }
    return Relay.NO_CHANGE;
  }

  // Target interface

  public MessageAddress getSource() {
    return source;
  }

  public Object getResponse() {
    return response;
  }

  public int updateContent(Object content, Token token) {
    // assert content != null
    if (!(content.equals(this.content))) {
      this.content = content;
      return CONTENT_CHANGE;
    }
    return NO_CHANGE;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OpConInfoRelay)) {
      return false;
    } else {
      UID u = ((OpConInfoRelay) o).getUID();
      return uid.equals(u);
    }
  }

  public int hashCode() {
    return uid.hashCode();
  }

  public String toString() {
    return "(" + uid + ", " + content + ", " + response + ")";
  }

}
