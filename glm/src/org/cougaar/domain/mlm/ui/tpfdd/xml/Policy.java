/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/Policy.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

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


public class Policy extends LogPlanObject
{
    private NameValue[] policyParameter;
    private String policyName;

    Policy(String UUID, Element xml)
    {
	super(UUID, xml);
    }

    public NameValue[] getPolicyParameter()
    {
	return policyParameter;
    }

    public void setPolicyParameter(NameValue[] policyParameter)
    {
	this.policyParameter = policyParameter;
    }

    public String getPolicyName()
    {
	return policyName;
    }

    public void setPolicyName(String policyName)
    {
	this.policyName = policyName;
    }
}
