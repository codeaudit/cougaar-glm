/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/GeolocLocation.java,v 1.1 2001-12-27 22:44:34 bdepass Exp $ */

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


public class GeolocLocation extends LogPlanObject
{
    private String name;
    private String geolocCode;
    private Latitude latitude;
    private Longitude longitude;
    
    public GeolocLocation(Element xml)
    {
	super(null, xml);
    }

    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public String getGeolocCode()
    {
	return geolocCode;
    }

    public void setGeolocCode(String geolocCode)
    {
	this.geolocCode = geolocCode;
    }

    public Latitude getLatitude()
    {
	return latitude;
    }

    public void setLatitude(Latitude latitude)
    {
	this.latitude = latitude;
    }

    public Longitude getLongitude()
    {
	return longitude;
    }

    public void setLongitude(Longitude longitude)
    {
	this.longitude = longitude;
    }

    // XXX the following four methods shouldn't be here; XML returned shouldn't
    // be returning internals!
    public boolean getDeepValid()
    {
	return true;
    }

    public void setDeepValid(boolean deepValid)
    {
    }

    public boolean getValid()
    {
	return true;
    }

    public void setValid(boolean deepValid)
    {
    }
}
