/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/Asset.java,v 1.1 2001-12-27 22:44:33 bdepass Exp $ */

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


public class Asset extends LogPlanObject
{
    private String[] allocation;
    private Schedule availableSchedule;
    private String   alternateItemIdentification;
    private String   itemIdentification;
    private String   itemIdentificationPropertyNomenclature;
    private String   typeIdentification;
    private String   typeIdentificationPropertyNomenclature;
    private String   alternateTypeIdentification;
    private long     quantity;
    private String   supplyClass;
    private String   subClass;
    private PropertyNameValue[] property;
    private Asset    asset; // physically contained subasset

    public Asset(String UUID, Element xml)
    {
	super(UUID, xml);
    }

    // Redefine equals to be uuid.equal()
    public boolean equals(Object o)
    {
	if ( o instanceof Asset ) {
	    return UUID.equals(((Asset)o).getUUID());
	}
	return false;
    }

    public String[] getAllocation()
    {
	return allocation;
    }

    public void setAllocation(String[] allocation)
    {
	this.allocation = allocation;
    }

    public Schedule getAvailableSchedule()
    {
	return availableSchedule;
    }

    public void setAvailableSchedule(Schedule availableSchedule)
    {
	this.availableSchedule = availableSchedule;
    }

    public String getTypeIdentification()
    {
	return typeIdentification;
    }

    public void setTypeIdentification(String typeIdentification)
    {
	this.typeIdentification = typeIdentification;
    }

    public String getTypeIdentificationPropertyNomenclature()
    {
	return typeIdentificationPropertyNomenclature;
    }

    public void setTypeIdentificationPropertyNomenclature(String typeIdentificationPropertyNomenclature)
    {
	this.typeIdentificationPropertyNomenclature = typeIdentificationPropertyNomenclature;
    }

    public String getAlternateTypeIdentification()
    {
	return alternateTypeIdentification;
    }

    public void setAlternateTypeIdentification(String alternateTypeIdentification)
    {
	this.alternateTypeIdentification = alternateTypeIdentification;
    }
	    
    public String getSupplyClass()
    {
	return supplyClass;
    }

    public void setSupplyClass(String supplyClass)
    {
	this.supplyClass = supplyClass;
    }

    public String getSubClass()
    {
	return subClass;
    }

    public void setSubClass(String subClass)
    {
	this.subClass = subClass;
    }

    public String getItemIdentification()
    {
	return itemIdentification;
    }

    public void setItemIdentification(String itemIdentification)
    {
	this.itemIdentification = itemIdentification;
    }

    public String getItemIdentificationPropertyNomenclature() {
	return itemIdentificationPropertyNomenclature;
    }

    public void setItemIdentificationPropertyNomenclature(String itemIdentificationPropertyNomenclature) {
	this.itemIdentificationPropertyNomenclature = itemIdentificationPropertyNomenclature;
    }

    public String getAlternateItemIdentification() {
	return alternateItemIdentification;
    }

    public void setAlternateItemIdentification(String alternateItemIdentification)
    {
	this.alternateItemIdentification = alternateItemIdentification;
    }
    
    public Asset getAsset()
    {
	return asset;
    }

    public void setAsset(Asset asset)
    {
	this.asset = asset;
    }

    public long getQuantity()
    {
	return quantity;
    }

    public void setQuantity(long quantity)
    {
	this.quantity = quantity;
    }

    public void setProperty(PropertyNameValue[] property)
    {
	this.property = property;
    }

    public PropertyNameValue[] getProperty()
    {
	return property;
    }
}
