/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

/**
 * Private state of the ReportChainDetectorPlugin
 **/
public class ReportChainState
    extends Object
    implements Serializable, UniqueObject
{
    private boolean chainReady = false;
    private UID uid = null;

    protected ReportChainState( UID uid ) {
	this.uid = uid;
    }

    protected void setChainReady( boolean chainReady ) {
	this.chainReady = chainReady;
    }

    public boolean isChainReady() {
	return chainReady;
    }

    public void setUID( UID uid ) {
	this.uid = uid;
    }

    public UID getUID() {
	return uid;
    }
}
