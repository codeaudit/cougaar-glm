/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/Schedule.java,v 1.2 2001-02-23 01:02:24 wseitz Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.xml;


import java.util.Date;

import org.w3c.dom.Element;


public class Schedule extends LogPlanObject
{
    private ScheduleElement[] scheduleElements;

    private String scheduleElementType;
    private String scheduleType;
    private long startDate, endDate;
    private long simpleScheduleEndDate, simpleScheduleStartDate;

    public Schedule(Element xml)
    {
        super(null, xml);
    }

    public ScheduleElement[] getScheduleElements()
    {
	return scheduleElements;
    }

    public void setScheduleElements(ScheduleElement[] scheduleElement)
    {
	this.scheduleElements = scheduleElements;
    }

    public ScheduleElement[] getUIScheduleElement()
    {
	return scheduleElements;
    }

    public void setUIScheduleElement(ScheduleElement[] ScheduleElements)
    {
	this.scheduleElements = ScheduleElements;
    }

    public String getScheduleElementType()
    {
	return scheduleElementType;
    }

    public void setScheduleElementType(String scheduleElementType)
    {
	this.scheduleElementType = scheduleElementType;
    }
    
    public String getScheduleType()
    {
	return scheduleType;
    }

    public void setScheduleType(String scheduleType)
    {
	this.scheduleType = scheduleType;
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

    public long getSimpleScheduleStartDate()
    {
	return simpleScheduleStartDate;
    }

    public void setSimpleScheduleStartDate(long simpleScheduleStartDate)
    {
	this.simpleScheduleStartDate = simpleScheduleStartDate;
    }

    public long getSimpleScheduleEndDate()
    {
	return simpleScheduleEndDate;
    }

    public void setSimpleScheduleEndDate(long simpleScheduleEndDate)
    {
	this.simpleScheduleEndDate = simpleScheduleEndDate;
    }
}
