/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/Expansion.java,v 1.1 2001-12-27 22:44:34 bdepass Exp $ */

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

import org.cougaar.mlm.ui.tpfdd.util.Debug;


public class Expansion extends PlanElement
{
    private String workflow;

    public Expansion(String UUID, Element xml)
    {
	super(UUID, xml);
    }

    public String getWorkflow()
    {
	return workflow;
    }

    public void setWorkflow(String workflow)
    {
	this.workflow = workflow;
    }
}
