/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/MilitaryOrgPG.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

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


public class MilitaryOrgPG extends LogPlanObject
{
    private String echelon;
    private Location home_location;
    private boolean reserve;
    private String SRC;
    private String UIC;
    private String UTC;

    public MilitaryOrgPG(Element xml)
    {
	super(null, xml);
    }

    public String getEchelon()
    {
	return echelon;
    }

    public void setEchelon(String echelon)
    {
	this.echelon = echelon;
    }
    
    public boolean isReserve()
    {
	return reserve;
    }

    public void setReserve(boolean reserve)
    {
	this.reserve = reserve;
    }
    
    public String getSRC()
    {
	return SRC;
    }

    public void setSRC(String SRC)
    {
	this.SRC = SRC;
    }
    
    public String getUIC()
    {
	return UIC;
    }

    public void setUIC(String UIC)
    {
	this.UIC = UIC;
    }
    
    public String getUTC()
    {
	return UTC;
    }

    public void setUTC(String UTC)
    {
	this.UTC = UTC;
    }
}
