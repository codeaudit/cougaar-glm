/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/ScheduleElement.java,v 1.1 2001-02-22 22:42:39 wseitz Exp $ */

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


public class ScheduleElement extends LogPlanObject
{
    private String scheduleElementType;
    private long startTime, endTime, startDate, endDate;
    private String role;
    private Location startLocation, endLocation, location;
    private double startQuantity, endQuantity, quantity;
    private double rate;

    public ScheduleElement(Element xml)
    {
        super(null, xml);
    }

    public void setScheduleElementType(String scheduleElementType)
    {
	this.scheduleElementType = scheduleElementType;
    }

    public String getScheduleElementType()
    {
	return scheduleElementType;
    }

    public long getStartTime()
    {
	return startTime;
    }

    public void setStartTime(long startTime)
    {
	this.startTime = startTime;
    }

    public long getEndTime()
    {
	return endTime;
    }

    public void setEndTime(long endTime)
    {
	this.endTime = endTime;
    }

    public long getStartDate()
    {
	return startDate;
    }

    public void setStartDate(long startDate)
    {
	this.startDate = startDate;
    }

    public long getEndDate()
    {
	return endDate;
    }

    public void setEndDate(long endDate)
    {
	this.endDate = endDate;
    }

    public String getRole()
    {
	return role;
    }

    public void setRole(String role)
    {
	this.role = role;
    }

    public Location getStartLocation()
    {
	return startLocation;
    }

    public void setStartLocation(Location startLocation)
    {
	this.startLocation = startLocation;
    }

    public Location getEndLocation()
    {
	return endLocation;
    }

    public void setEndLocation(Location endLocation)
    {
	this.endLocation = endLocation;
    }

    public Location getLocation()
    {
	return location;
    }

    public void setLocation(Location location)
    {
	this.location = location;
    }

    public double getStartQuantity()
    {
	return startQuantity;
    }

    public void setStartQuantity(double startQuantity)
    {
	this.startQuantity = startQuantity;
    }

    public double getEndQuantity()
    {
	return endQuantity;
    }

    public void setEndQuantity(double endQuantity)
    {
	this.endQuantity = endQuantity;
    }

    public double getQuantity()
    {
	return quantity;
    }

    public void setQuantity(double quantity)
    {
	this.quantity = quantity;
    }

    public double getRate()
    {
	return rate;
    }

    public void setRate(double rate)
    {
	this.rate = rate;
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
