/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/TypeIdentificationPG.java,v 1.1 2001-12-27 22:44:38 bdepass Exp $ */

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


public class TypeIdentificationPG extends LogPlanObject
{
    private String type_identification;
    private String alternate_type_identification;
    private String nomenclature;

    public TypeIdentificationPG(Element xml)
    {
	super(null, xml);
    }

    public String getType_identification()
    {
	return type_identification;
    }

    public void setType_identification(String type_identification)
    {
	this.type_identification = type_identification;
    }
    

    public String getAlternate_type_identification()
    {
	return alternate_type_identification;
    }

    public void setAlternate_type_identification(String alternate_type_identification)
    {
	this.alternate_type_identification = alternate_type_identification;
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
