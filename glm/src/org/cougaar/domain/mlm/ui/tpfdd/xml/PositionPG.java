/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/PositionPG.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

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


public class PositionPG extends LogPlanObject
{
    private GeolocLocation position;
    
    public PositionPG(Element xml)
    {
	super(null, xml);
    }

    public GeolocLocation getPosition()
    {
	return position;
    }

    public void setPosition(GeolocLocation position)
    {
	this.position = position;
    }
}
