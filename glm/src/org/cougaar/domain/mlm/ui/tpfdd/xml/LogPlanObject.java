/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/LogPlanObject.java,v 1.3 2001-10-17 19:07:17 gvidaver Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.xml;


import java.io.Serializable;

import java.lang.reflect.Method;

import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;

import org.cougaar.domain.mlm.ui.tpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;


public class LogPlanObject implements Parseable, UUID, Serializable
{
    private transient ParseableImpl parseable;
    protected String UUID;
    protected transient PlanElementProvider provider;

    // pseudo-constructor for transient part of remotely created object
    public void reconstituteSerialized()
    {
	parseable = new ParseableImpl(this, null);
    }

    public LogPlanObject(String UUID, Element xml)
    {
	this.UUID = UUID;
	parseable = new ParseableImpl(this, xml);
    }

    public LogPlanObject(PlanElementProvider provider, String UUID, Element xml)
    {
	this.provider = provider;
	this.UUID = UUID;
	parseable = new ParseableImpl(this, xml);
    }

    public String getUUID()
    {
	return UUID;
    }

    
    // Warning: should only be used to distinguish a clone from an original copies (see itineraryBrothers)
    // otherwise severe tire damage may result. plus a hashtable may not work.
    public void setUUID(String UUID)
    {
	this.UUID = UUID;
    }
    
    public String toXMLDocument()
    {
	return parseable.toXMLDocument();
    }

    public String toXML(boolean newLines)
    {
	return parseable.toXML(newLines);
    }

    public String toXML()
    {
	return parseable.toXML();
    }

    public String toURLQuery()
    {
	return parseable.toURLQuery();
    }

    public void copyFrom(Object source) throws MismatchException
    {
	parseable.copyFrom(source);
    }

    public Method getReader(String readerName)
    {
	return parseable.getReader(readerName);
    }

    public Method getWriter(String writerName)
    {
	return parseable.getWriter(writerName);
    }

    public String toString()
    {
	if ( parseable == null )
	    return super.toString();
	else
	    return parseable.toString();
    }
}
