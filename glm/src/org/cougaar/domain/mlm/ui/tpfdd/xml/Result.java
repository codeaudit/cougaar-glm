/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/Result.java,v 1.3 2001-10-17 19:07:19 gvidaver Exp $ */

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

import java.util.Date;


public class Result extends LogPlanObject
{
    private boolean success;
    private double  confidenceRating;
    private double  POD;
    private double  interval;
    private double  quantity;
    private double  totalShipments;
    private boolean phased;
    private long    startTime, endTime;
    private long    PODDate;

    public Result(Element xml)
    {
	super(null, xml);
    }

    public boolean isSuccess()
    {
	return success;
    }

    public void setSuccess(boolean success)
    {
	this.success = success;
    }

    public double getConfidenceRating()
    {
	return confidenceRating;
    }

    public void setConfidenceRating(double confidenceRating)
    {
	this.confidenceRating = confidenceRating;
    }

    public double getPOD()
    {
	return POD;
    }

    public void setPOD(double POD)
    {
	this.POD = POD;
    }

    public double getInterval()
    {
	return interval;
    }

    public void setInterval(double interval)
    {
	this.interval = interval;
    }

    public double getQuantity()
    {
	return quantity;
    }

    public void setQuantity(double quantity)
    {
	this.quantity = quantity;
    }

    public double getTotalShipments()
    {
	return totalShipments;
    }

    public void setTotalShipments(double totalShipments)
    {
	this.totalShipments = totalShipments;
    }

    public boolean getPhased()
    {
	return phased;
    }

    public void setPhased(boolean phased)
    {
	this.phased = phased;
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

    public long getPODDate()
    {
	return PODDate;
    }

    public void setPODDate(long PODDate)
    {
	this.PODDate = PODDate;
    }
}
