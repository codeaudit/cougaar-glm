/*
 * <copyright>
 *  Copyright 1997-2004 BBNT Solutions, LLC
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
