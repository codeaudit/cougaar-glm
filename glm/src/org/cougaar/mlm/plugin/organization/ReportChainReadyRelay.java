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

/**
 * Relay for subordinates to report to their superior
 * that they & their subordinates are up, with report
 * chain intact. Used by the ReportChainDetectorPlugin
 **/
public class ReportChainReadyRelay
  implements Relay.Source, Relay.Target, UniqueObject
{
  private UID uid = null;

  private MessageAddress source = null;
  private MessageAddress target = null;

  private Object content = null;
  private Object response = null;

  private transient Set targets = null;

  public ReportChainReadyRelay( UID uid,
		      MessageAddress source,
		      MessageAddress target,
		      Object content ) {
    this.uid = uid;
    this.source = source;
    this.target = target;
    this.content = content;
    this.response = new Integer( 1 );
  }

  private static final class OPRFactory
    implements TargetFactory, Serializable 
  {
    public static final OPRFactory _instance =
      new OPRFactory();

    private OPRFactory() {}

    public Relay.Target create( UID uid,
				MessageAddress source,
				Object content,
				Token token) {
      return new ReportChainReadyRelay( uid, source, null, content );
    }

    private Object readResolve() {
      return _instance;
    }
  }

  public TargetFactory getTargetFactory() {
    return OPRFactory._instance;
  }

  public UID getUID() {
    return uid;
  }
    
  public void setUID( UID uid ) {
    throw new RuntimeException("Bozo API Exception!  Don't set UID.");
  }
	
  public int updateResponse( MessageAddress t, Object response ) {
    return Relay.NO_CHANGE;
  }

  public MessageAddress getSource() {
    return source;
  }

  public Object getResponse() {
    return response;
  }

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

  public int updateContent( Object content, Token token ) {
    return Relay.NO_CHANGE;
  }

  public boolean equals( Object o ) {
    ReportChainReadyRelay opr = (ReportChainReadyRelay) o;
    return opr.getUID().equals( uid );
  }	

  public int hashCode() {
    return uid.hashCode();
  }

  public String toString() {
    return "(" + uid + ", " + content + ", " + response + ")";
  }
}
