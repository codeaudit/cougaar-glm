/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/PropertyNameValue.java,v 1.3 2001-10-17 19:07:19 gvidaver Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.xml;


import org.w3c.dom.Element;


public class PropertyNameValue extends LogPlanObject
{
    private String name;
    private Object value;

    public PropertyNameValue(Element xml)
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

    public Object getValue()
    {
	return value;
    }

    public void setValue(Object value)
    {
	this.value = value;
    }
}
