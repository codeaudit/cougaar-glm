/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/TaskItineraryElement.java,v 1.2 2001-02-23 01:02:24 wseitz Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.xml;



import java.lang.reflect.Method;

import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.newtpfdd.util.MismatchException;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElement;


public class TaskItineraryElement extends UITaskItineraryElement implements Parseable
{
    private ParseableImpl parseable;

    public TaskItineraryElement(Element xml)
    {
	super();
	parseable = new ParseableImpl(this, xml);
    }

    public String toXMLDocument()
    {
	return parseable.toXMLDocument();
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
	return parseable.toString();
    }
}
