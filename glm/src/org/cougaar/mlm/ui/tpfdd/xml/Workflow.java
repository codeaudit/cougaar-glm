/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/Workflow.java,v 1.1 2001-12-27 22:44:39 bdepass Exp $ */

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

import org.cougaar.mlm.ui.tpfdd.producer.AbstractProducer;


public class Workflow extends LogPlanObject
{
    private String parentTask;
    private String[] task;
    private boolean constraintViolated;
    private boolean propagatingToSubtasks;

    public Workflow(String UUID, Element xml)
    {
	super(UUID, xml);
    }

    // XXX this is because XML reports this extraneously
    public void setUUID(String UUID)
    {
	this.UUID = UUID;
    }

    public boolean equals(Object o)
    {
	if (o instanceof Workflow)
	    return this.UUID.equals(((Workflow)o).getUUID());
	return false;
    }

    public String getParentTask()
    {
	return parentTask;
    }

    public void setParentTask(String parent)
    {
	this.parentTask = parentTask;
    }

    public String[] getTask()
    {
	return task;
    }

    public void setTask(String[] task)
    {
	this.task = task;
    }

    public boolean getConstraintViolated()
    {
	return constraintViolated;
    }

    public void setConstraintViolated(boolean constraintViolated)
    {
	this.constraintViolated = constraintViolated;
    }

    public boolean getPropagatingToSubtasks()
    {
	return propagatingToSubtasks;
    }      

    public void setPropagatingToSubtasks(boolean propagatingToSubtasks)
    {
	this.propagatingToSubtasks = propagatingToSubtasks;
    }
}
