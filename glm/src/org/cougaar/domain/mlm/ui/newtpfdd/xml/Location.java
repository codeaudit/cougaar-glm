/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/Location.java,v 1.2 2001-02-23 01:02:22 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.xml;


import org.w3c.dom.Element;


public class Location extends LogPlanObject
{
    private String locationType;
    private double latitude, longitude;
    private String name;
    private String geolocCode;
    private String installationTypeCode;
    private String countryStateCode;
    private String countryStateName;
    private String icaoCode;

    public Location(Element xml)
    {
	super(null, xml);
    }

    public void setLocationType(String locationType)
    {
	this.locationType = locationType;
    }

    public String getLocationType()
    {
	return locationType;
    }

    public double getLatitude()
    {
	return latitude;
    }

    public void setLatitude(double latitude)
    {
	this.latitude = latitude;
    }

    public double getLongitude()
    {
	return longitude;
    }

    public void setLongitude(double longitude)
    {
	this.longitude = longitude;
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

    public String getInstallationTypeCode()
    {
	return installationTypeCode;
    }

    public void setInstallationTypeCode(String installationTypeCode)
    {
	this.installationTypeCode = installationTypeCode;
    }

    public String getCountryStateCode()
    {
	return countryStateCode;
    }

    public void setCountryStateCode(String countryStateCode)
    {
	this.countryStateCode = countryStateCode;
    }

    public String getCountryStateName()
    {
	return countryStateName;
    }

    public void setCountryStateName(String countryStateName)
    {
	this.countryStateName = countryStateName;
    }

    public String getIcaoCode()
    {
	return icaoCode;
    }

    public void setIcaoCode(String icaoCode)
    {
	this.icaoCode = icaoCode;
    }
}
