/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/TransportationNode.java,v 1.1 2001-02-22 22:42:40 wseitz Exp $ */

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


public class TransportationNode extends LogPlanObject
{
    private FacilityPG facilityPG;
    private TypeIdentificationPG typeIdentificationPG;
    private ItemIdentificationPG itemIdentificationPG;
    private PositionPG positionPG;
    private RoleSchedule roleSchedule;
    private UID UID;

    public TransportationNode(Element xml)
    {
	super(null, xml);
    }

    public FacilityPG getFacilityPG()
    {
	return facilityPG;
    }

    public void setFacilityPG(FacilityPG facilityPG)
    {
	this.facilityPG = facilityPG;
    }

    public TypeIdentificationPG getTypeIdentificationPG()
    {
	return typeIdentificationPG;
    }

    public void setTypeIdentificationPG(TypeIdentificationPG typeIdentificationPG)
    {
	this.typeIdentificationPG = typeIdentificationPG;
    }
    
    public ItemIdentificationPG getItemIdentificationPG()
    {
	return itemIdentificationPG;
    }

    public void setItemIdentificationPG(ItemIdentificationPG itemIdentificationPG)
    {
	this.itemIdentificationPG = itemIdentificationPG;
    }

    public PositionPG getPositionPG()
    {
	return positionPG;
    }

    public void setPositionPG(PositionPG positionPG)
    {
	this.positionPG = positionPG;
    }

    public RoleSchedule getRoleSchedule()
    {
	return roleSchedule;
    }

    public void setRoleSchedule(RoleSchedule roleSchedule)
    {
	this.roleSchedule = roleSchedule;
    }

    public UID getUID()
    {
	return UID;
    }

    public void setUID(UID UID)
    {
	this.UID = UID;
    }

    // XXX Links is empty object that comes back; ignore it
    public Object getLinks()
    {
	return null;
    }

    public void setLinks(Object links)
    {
    }
}
