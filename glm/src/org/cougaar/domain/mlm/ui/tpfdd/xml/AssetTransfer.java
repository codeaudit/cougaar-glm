/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/AssetTransfer.java,v 1.3 2001-10-17 19:07:15 gvidaver Exp $ */

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


public class AssetTransfer extends PlanElement
{
    private String UIAsset;
    private String assignee;
    private String assignor;
    private ScheduleElement UIScheduleElement;

    public AssetTransfer(String UUID, Element xml)
    {
	super(UUID, xml);
    }

    public boolean equals(Object o)
    {
	if ( o instanceof AssetTransfer )
	    return UUID.equals(((AssetTransfer)o).getUUID());
	return false;
    }

    public String getUIAsset()
    {
	return UIAsset;
    }

    public void setUIAsset(String UIAsset)
    {
	this.UIAsset = UIAsset;
    }

    public String getAssignee()
    {
	return assignee;
    }

    public void setAssignee(String assignee)
    {
	this.assignee = assignee;
    }

    public String getAssignor()
    {
	return assignee;
    }

    public void setAssignor(String assignee)
    {
	this.assignee = assignee;
    }

    public ScheduleElement getUIScheduleElement()
    {
	return UIScheduleElement;
    }

    public void setUIScheduleElement(ScheduleElement UIScheduleElement)
    {
	this.UIScheduleElement = UIScheduleElement;
    }
}
