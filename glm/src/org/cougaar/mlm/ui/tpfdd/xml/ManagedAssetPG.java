/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/ManagedAssetPG.java,v 1.1 2001-12-27 22:44:35 bdepass Exp $ */

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


public class ManagedAssetPG extends LogPlanObject
{
    private long number_of_personnel;

    public ManagedAssetPG(Element xml)
    {
	super(null, xml);
    }

    public long getNumber_of_personnel()
    {
	return number_of_personnel;
    }

    public void setNumber_of_personnel(long number_of_personnel)
    {
	this.number_of_personnel = number_of_personnel;
    }
}
