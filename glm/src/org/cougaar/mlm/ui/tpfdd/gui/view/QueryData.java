/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/view/Attic/QueryData.java,v 1.1 2001-12-27 22:44:26 bdepass Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.mlm.ui.tpfdd.gui.view;


import org.cougaar.mlm.ui.tpfdd.util.Debug;

import org.cougaar.mlm.ui.tpfdd.xml.LogPlanObject;


public class QueryData extends LogPlanObject implements TaskFilter
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

    public boolean admits(TaskNode node)
    {
	// NODE TYPE FILTER
	boolean nodeTypeMatch = false;
	for ( int i = 0; i < nodeTypes.length; i++ )
	    if ( node.getSourceType() == nodeTypes[i] ) {
		nodeTypeMatch = true;
		break;
	    }
	if ( !nodeTypeMatch )
	    return false;

	// UNIT NAME FILTER
	boolean unitNameMatch = true;
	if ( unitNames != null ) {
	    unitNameMatch = false;
	    for ( int i = 0; i < unitNames.length; i++ )
		if ( node.getUnitName().equals(unitNames[i]) ) {
		    unitNameMatch = true;
		    break;
		}
	}
	if ( !unitNameMatch )
	    return false;



	// CARRIER NAME/TYPE FILTER
	boolean carrierNameMatch = true;
	boolean carrierTypeMatch = true;
	String name, type;
	if ( node.getSourceType() == TaskNode.ITINERARY_LEG ) {
	    name = node.getParentCarrierName();
	    type = node.getParentCarrierType();
	}
	else {
	    name = node.getCarrierName();
	    type = node.getCarrierType();
	}
	
	if ( carrierIncluded ) {
	    if ( carrierNames != null ) {
		carrierNameMatch = false;
		for ( int i = 0; i < carrierNames.length; i++ ) {
		  if ( name.equals(carrierNames[i]) ) {
		    carrierNameMatch = true;
		    break;
		  }
		}
	    }

	    if ( carrierTypes != null ) {
		carrierTypeMatch = false;
		for ( int i = 0; i < carrierTypes.length; i++ ){
		    if ( type.equals(carrierTypes[i]) ) {
		      carrierTypeMatch = true;
		      break;
		    }
		}
	    }
	}
	if ( !carrierNameMatch || !carrierTypeMatch )
	    return false;

	// CARGO NAME/TYPE FILTER
	boolean cargoNameMatch = true;
	boolean cargoTypeMatch = true;
	if ( cargoIncluded ) {
	    if ( cargoNames != null ) {
		cargoNameMatch = false;
		for ( int i = 0; i < cargoNames.length; i++ )
		    if ( node.getDirectObjectName().equals(cargoNames[i]) ) {
			cargoNameMatch = true;
			break;
		    }
	    }

	    if ( cargoTypes != null ) {
		cargoTypeMatch = false;
		for ( int i = 0; i < cargoTypes.length; i++ )
		    if ( node.getDirectObjectType().equals(cargoTypes[i]) ) {
			cargoTypeMatch = true;
			break;
		    }
	    }
	}
	if ( !cargoNameMatch || !cargoTypeMatch )
	    return false;



	// Carrier type node
	if (carrierIncluded) {
	  if (node.getTypeCargoCarrier() != TaskNode.CARRIER_TYPE) {
	    return false;
	  }
	}
	// Cargo type node
	if (cargoIncluded)
	  if (node.getTypeCargoCarrier() != TaskNode.CARGO_TYPE){
	    return false;
	  }



	return true;
    }
}
