/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/view/Attic/QueryData.java,v 1.2 2001-02-23 01:02:17 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;


import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.newtpfdd.xml.LogPlanObject;


public class QueryData extends LogPlanObject
{
    // context of where the request came from so we know whom to answer;
    // currently applicable to server
    private Object closure;

    private boolean cargoIncluded;
    private boolean carrierIncluded;

    private String[] unitNames;
    private boolean ownWindow;

    private int[] nodeTypes;

    private String[] carrierNames;
    private String[] carrierTypes;

    private String[] cargoNames;
    private String[] cargoTypes;

    private String otherCommand;

    public QueryData()
    {
	super(null, null);
    }

    public void setClosure(Object closure)
    {
	this.closure = closure;
    }

    public Object getClosure()
    {
	return closure;
    }

    public boolean getCargoIncluded()
    {
	return cargoIncluded;
    }

    public void setCargoIncluded(boolean cargoIncluded)
    {
	this.cargoIncluded = cargoIncluded;
    }

    public boolean getCarrierIncluded()
    {
	return carrierIncluded;
    }

    public void setCarrierIncluded(boolean carrierIncluded)
    {
	this.carrierIncluded = carrierIncluded;
    }

    public String[] getUnitNames()
    {
	return unitNames;
    }

    public void setUnitNames(String[] unitNames)
    {
	this.unitNames = unitNames;
    }

    public boolean isOwnWindow()
    {
	return ownWindow;
    }

    public void setOwnWindow(boolean ownWindow)
    {
	this.ownWindow = ownWindow;
    }

    public int[] getNodeTypes()
    {
	return nodeTypes;
    }

    public void setNodeTypes(int[] nodeTypes)
    {
	this.nodeTypes = nodeTypes;
    }

    public String[] getCarrierNames()
    {
	return carrierNames;
    }

    public void setCarrierNames(String[] carrierNames)
    {
	this.carrierNames = carrierNames;
    }

    public String[] getCarrierTypes()
    {
	return carrierTypes;
    }

    public void setCarrierTypes(String[] carrierTypes)
    {
	this.carrierTypes = carrierTypes;
    }

    public String[] getCargoNames()
    {
	return cargoNames;
    }

    public void setCargoNames(String[] cargoNames)
    {
	this.cargoNames = cargoNames;
    }

    public String[] getCargoTypes()
    {
	return cargoTypes;
    }

    public void setCargoTypes(String[] cargoTypes)
    {
	this.cargoTypes = cargoTypes;
    }

    public void setOtherCommand(String otherCommand)
    {
	this.otherCommand = otherCommand;
    }

    public String getOtherCommand()
    {
	return otherCommand;
    }

}
