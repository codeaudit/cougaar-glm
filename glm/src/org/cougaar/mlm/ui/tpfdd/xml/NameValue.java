/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/NameValue.java,v 1.1 2001-12-27 22:44:35 bdepass Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.mlm.ui.tpfdd.xml;


import org.w3c.dom.Element;


// Reminder: NameValue is String name/String value; PropertyNameValue is String name/Object value
public class NameValue extends LogPlanObject
{
    private String name;
    private String value;

    public NameValue(Element xml)
    {
	super(null, xml);
    }

    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public String getValue()
    {
	return value;
    }

    public void setValue(String value)
    {
	this.value = value;
    }
}
