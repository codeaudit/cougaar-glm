/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.domain.glm.ldm.plan;


import org.cougaar.domain.glm.ldm.plan.CasRep;
import org.cougaar.domain.glm.ldm.plan.NewCasRep;

import org.cougaar.core.society.UID;

import org.cougaar.core.util.XMLizable;

import java.lang.String;
import java.util.Vector;
import java.util.Date;
import java.io.*;
import java.io.Serializable;
import java.util.Enumeration;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.core.util.XMLizable;
import org.cougaar.domain.planning.ldm.asset.*;
 
import org.cougaar.core.util.XMLize;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

public class CasRepChangeIndicatorImpl extends CasRepImpl
{
    public CasRepChangeIndicatorImpl()
	{
	}// CasRepChangeIndicatorImpl Constructor
}//CasRepChangeIndicator
