/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/FacilityPG.java,v 1.1 2001-12-27 22:44:34 bdepass Exp $ */

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


public class FacilityPG extends LogPlanObject
{
    public FacilityPG(Element xml)
    {
	super(null, xml);
    }

    // Yes, this comes back from the current XML servers as an empty object.
    // Here for algorithmic consistency, and if any fields ever do appear.
}
