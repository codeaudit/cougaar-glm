/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/UID.java,v 1.3 2001-10-17 19:07:21 gvidaver Exp $ */

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


public class UID extends LogPlanObject
{
    private String UID;

    public UID(Element xml)
    {
	super(null, xml);
    }

    public String getUID()
    {
	return UID;
    }

    public void setUID(String UID)
    {
	this.UID = UID;
    }
}
