/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/Task.java,v 1.3 2001-10-17 19:07:20 gvidaver Exp $ */

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


public class Task extends LogPlanObject
{
    private String   source;
    private String   destination;
    private String   verb;
    private String   directObject;
    private String   forWhom;
    private String   forClusterId;
    private Location fromLocation;
    private Location toLocation;
    private Schedule itinerary;
    private String   parentTask;
    private String[] parentTasks;
    private String   workflow;
    private String   planElement;
    private String   planName;
    private String   ofRequirementsType;
    private long     preferredStartTime;
    private long     preferredEndTime;
    private long     preferredPODDate;
    private double   preferredInterval;
    private double   preferredPOD;
    private double   preferredQuantity;
    private double   preferredTotalShipments;
    private byte     priority;
    
    public Task(String UUID, Element xml)
    {
	super(UUID, xml);
    }

     // Redefine equals to be uuid.equal()
    public boolean equals(Object o)
    {
	if ( o instanceof Task )
	    return UUID.equals(((Task)o).getUUID());
	return false;
    }

    public String getSource()
    {
	return source;
    }

    public void setSource(String source)
    {
	this.source = source;
    }

    public String getDestination()
    {
	return destination;
    }

    public void setDestination(String destination)
    {
	this.destination = destination;
    }

    public String getVerb()
    {
	return verb;
    }

    public void setVerb(String verb)
    {
	this.verb = verb;
    }

    public String getDirectObject()
    {
	return directObject;
    }

    public void setDirectObject(String directObject)
    {
	this.directObject = directObject;
    }

    public String getForClusterId()
    {
	return forClusterId;
    }

    public void setForClusterId(String forClusterId)
    {
	this.forClusterId = forClusterId;
    }

    public String getForWhom()
    {
	return forWhom;
    }

    public void setForWhom(String forWhom)
    {
	this.forWhom = forWhom;
    }

    public Location getFromLocation()
    {
	return fromLocation;
    }

    public void setFromLocation(Location fromLocation)
    {
	this.fromLocation = fromLocation;
    }

    public Location getToLocation()
    {
	return toLocation;
    }

    public void setToLocation(Location toLocation)
    {
	this.toLocation = toLocation;
    }

    public Schedule getItinerary()
    {
	return itinerary;
    }

    public void setItinerary(Schedule itinerary)
    {
	this.itinerary = itinerary;
    }

    public String getParentTask()
    {
	return parentTask;
    }

    public void setParentTask(String parentTask)
    {
	this.parentTask = parentTask;
    }

    public String[] getParentTasks()
    {
	return parentTasks;
    }

    public void setParentTasks(String[] parentTask)
    {
	this.parentTasks = parentTasks;
    }

    public String getWorkflow()
    {
	return workflow;
    }

    public void setWorkflow(String workflow)
    {
	this.workflow = workflow;
    }

    public String getPlanElement()
    {
	return planElement;
    }

    public void setPlanElement(String planElement)
    {
	this.planElement = planElement;
    }

    public String getPlanName()
    {
	return planName;
    }

    public void setPlanName(String planName)
    {
	this.planName = planName;
    }

    public String getOfRequirementsType()
    {
	return ofRequirementsType;
    }

    public void setOfRequirementsType(String ofRequirementsType)
    {
	this.ofRequirementsType = ofRequirementsType;
    }

    public long getPreferredStartTime()
    {
	return preferredStartTime;
    }

    public void setPreferredStartTime(long preferredStartTime)
    {
	this.preferredStartTime = preferredStartTime;
    }

    public long getPreferredEndTime()
    {
	return preferredEndTime;
    }

    public void setPreferredEndTime(long preferredEndTime)
    {
	this.preferredEndTime = preferredEndTime;
    }
    
    public long getPreferredPODDate()
    {
	return preferredPODDate;
    }

    public void setPreferredPODDate(long preferredPODDate)
    {
	this.preferredPODDate = preferredPODDate;
    }
    
    public double getPreferredInterval()
    {
	return preferredInterval;
    }

    public void setPreferredInterval(double preferredInterval)
    {
	this.preferredInterval = preferredInterval;
    }

    public double getPreferredPOD()
    {
	return preferredPOD;
    }

    public void setPreferredPOD(double preferredPOD)
    {
	this.preferredPOD = preferredPOD;
    }

    public double getPreferredQuantity()
    {
	return preferredQuantity;
    }

    public void setPreferredQuantity(double preferredQuantity)
    {
	this.preferredQuantity = preferredQuantity;
    }

    public double getPreferredTotalShipments()
    {
	return preferredTotalShipments;
    }

    public void setPreferredTotalShipments(double preferredTotalShipments)
    {
	this.preferredTotalShipments = preferredTotalShipments;
    }

    public byte getPriority()
    {
	return priority;
    }

    public void setPriority(byte priority)
    {
	this.priority = priority;
    }
}
