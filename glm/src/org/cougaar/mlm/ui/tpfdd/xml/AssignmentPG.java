/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/AssignmentPG.java,v 1.1 2001-12-27 22:44:33 bdepass Exp $ */

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


public class AssignmentPG extends LogPlanObject
{
    private TransportationNode home_station;

    public AssignmentPG(Element xml)
    {
	super(null, xml);
    }

    public TransportationNode getHome_station()
    {
	return home_station;
    }

    public void setHome_station(TransportationNode home_station)
    {
	this.home_station = home_station;
    }
}
