/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/PlanElement.java,v 1.2 2001-02-23 01:02:23 wseitz Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.xml;


import org.w3c.dom.Element;


public class PlanElement extends LogPlanObject
{
    protected String planName;
    protected String task;
    protected Result estimatedResult, reportedResult;

    public PlanElement(String UUID, Element xml)
    {
	super(UUID, xml);
    }

    public boolean equals(Object o)
    {
	if ( o instanceof Expansion )
	    return UUID.equals(((Expansion)o).getUUID());
	return false;
    }

    public String getPlanName()
    {
	return planName;
    }

    public void setPlanName(String planName)
    {
	this.planName = planName;
    }

    public String getTask()
    {
	return task;
    }

    public void setTask(String task)
    {
	this.task = task;
    }

    public Result getEstimatedResult()
    {
	return estimatedResult;
    }

    public void setEstimatedResult(Result estimatedResult)
    {
	this.estimatedResult = estimatedResult;
    }

    public Result getReportedResult()
    {
	return reportedResult;
    }

    public void setReportedResult(Result reportedResult)
    {
	this.reportedResult = reportedResult;
    }
}
