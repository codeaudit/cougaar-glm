/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/AssignedPG.java,v 1.1 2000-12-15 20:17:46 mthome Exp $ */

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


public class AssignedPG extends LogPlanObject
{
    private String relationship;
    private Role[] roles;

    public AssignedPG(Element xml)
    {
	super(null, xml);
    }

    public String getRelationship()
    {
	return relationship;
    }

    public void setRelationship(String relationship)
    {
	this.relationship = relationship;
    }

    public Role[] getRoles()
    {
	return roles;
    }

    public void setRoles(Role[] roles)
    {
	this.roles = roles;
    }
}
