/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/TAssetInfo.java,v 1.3 2001-10-17 19:07:20 gvidaver Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.xml;



import java.lang.reflect.Method;

import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.tpfdd.util.MismatchException;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITAssetInfo;


public class TAssetInfo extends UITAssetInfo implements Parseable
{
    private ParseableImpl parseable;

    public TAssetInfo(Element xml)
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
