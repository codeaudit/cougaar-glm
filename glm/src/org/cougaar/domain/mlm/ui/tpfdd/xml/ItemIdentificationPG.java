/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/ItemIdentificationPG.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

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


public class ItemIdentificationPG extends LogPlanObject
{
    private String item_identification;
    private String alternate_item_identification;
    private String nomenclature;

    public ItemIdentificationPG(Element xml)
    {
	super(null, xml);
    }

    public String getItem_identification()
    {
	return item_identification;
    }

    public void setItem_identification(String item_identification)
    {
	this.item_identification = item_identification;
    }
    

    public String getAlternate_item_identification()
    {
	return alternate_item_identification;
    }

    public void setAlternate_item_identification(String alternate_item_identification)
    {
	this.alternate_item_identification = alternate_item_identification;
    }

    public String getNomenclature()
    {
	return nomenclature;
    }

    public void setNomenclature(String nomenclature)
    {
	this.nomenclature = nomenclature;
    }
}
