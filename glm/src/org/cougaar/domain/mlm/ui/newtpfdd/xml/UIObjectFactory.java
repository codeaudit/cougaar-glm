/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/UIObjectFactory.java,v 1.3 2001-02-23 17:28:45 wseitz Exp $ */

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

import org.cougaar.domain.mlm.ui.newtpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Node;

import org.cougaar.domain.mlm.ui.newtpfdd.producer.PlanElementProvider;


// This has gotten too big.
public class UIObjectFactory
{
    static Parseable create(Element element, PlanElementProvider provider)
    {
        Parseable it = null;

	String className = PathString.basename(element.getAttribute("class"), '.');
	String UID = element.getAttribute("UID");
	if ( className.equals("UIAggregation") )
	    it = new Aggregation(UID, element);
	else if ( className.equals("UIAllocation") )
	    it = new Allocation(UID, element);
	else if ( className.equals("UIAsset") )
	    it = new Asset(UID, element);
	else if ( className.equals("UIAssetTransfer") )
	    it = new AssetTransfer(UID, element);
	else if ( className.equals("UIExpansion") )
	    it = new Expansion(UID, element);
	else if ( className.equals("UIMPTask") || className.equals("UITask") )
	    it = new Task(UID, element);
	else if ( className.equals("UIPolicy") )
	    it = new Policy(UID, element);
	else if ( className.equals("UIWorkflow") )
	    it = new Workflow(UID, element);
	// Non-first class LogPlan Objects; UID doesn't exist
	else if ( className.equals("UIAllocationResultImpl") )
	    it = new Result(element);
	else if ( className.equals("AssignedPGImpl$_Locked")
		  || className.equals("AssignedPGImpl") )
	    it = new AssignedPG(element);
	else if ( className.equals("AssignmentPGImpl$_Locked")
		  || className.equals("AssignmentPGImpl") )
	    it = new AssignmentPG(element);
	else if ( className.equals("ClusterPGImpl$_Locked")
		  || className.equals("ClusterPGImpl") )
	    it = new ClusterPG(element);
	else if ( className.equals("FacilityPGImpl$_Locked")
		  || className.equals("FacilityPGImpl") )
	    it = new FacilityPG(element);
	else if ( className.equals("GeolocLocationImpl") )
	    it = new GeolocLocation(element);
	else if ( className.equals("ItemIdentificationPGImpl$_Locked")
		  || className.equals("ItemIdentificationPGImpl") )
	    it = new ItemIdentificationPG(element);
	else if ( className.equals("Latitude") )
	    it = new Latitude(element);
	else if ( className.equals("UILocationImpl") ) {
	    // special case: locations get repeated many times; avoid for efficiency
	    it = new Location(element);
	    String code = ((Location)it).getGeolocCode();
	    Location oldIt = PlanElementProvider.getLocation(code);
	    if ( oldIt == null )
		PlanElementProvider.setLocation(code, (Location)it);
	    else
		it = oldIt;
	}
	else if ( className.equals("Longitude") )
	    it = new Longitude(element);
	else if ( className.equals("ManagedAssetPGImpl$_Locked")
		  || className.equals("ManagedAssetPGImpl") )
	    it = new ManagedAssetPG(element);
	else if ( className.equals("MilitaryOrgPGImpl$_Locked")
		  || className.equals("MilitaryOrgPGImpl") )
	    it = new MilitaryOrgPG(element);
	else if ( className.equals("UIPolicyParameterImpl") )
	    it = new NameValue(element);
	else if ( className.equals("OrganizationPGImpl$_Locked")
		  || className.equals("OrganizationPGImpl") )
	    it = new OrganizationPG(element);
	else if ( className.equals("PositionPGImpl$_Locked")
		  || className.equals("PositionPGImpl") )
	    it = new PositionPG(element);
	else if ( className.equals("UIPropertyNameValue") )
	    it = new PropertyNameValue(element);
	else if ( className.equals("Role") )
	    it = new Role(element);
	else if ( className.equals("RoleScheduleImpl") )
	    it = new RoleSchedule(element);
	else if ( className.equals("TOPSCONUSGroundModePredictorPlugIn$TOPSCONUSGroundModePredictor")
		  || className.equals("TOPSTheaterMCCPredictorPlugIn$TOPSTheaterMCCPredictor") )
	    it = null;
	else if ( className.equals("UIScheduleImpl")
		  || className.equals("ScheduleImpl") )
	    it = new Schedule(element);
	else if ( className.equals("UIScheduleElementImpl")
		  || className.equals("ScheduleElementImpl") )
	    it = new ScheduleElement(element);
	else if ( className.equals("UITaskItinerary") )
	    it = new TaskItinerary(element);
	else if ( className.equals("UITaskItineraryElement") )
	    it = new TaskItineraryElement(element);
	else if ( className.equals("Node") )
	    it = new Node(provider.getNodeMap(), UID, element);
	else if ( className.equals("UITAssetInfo") )
	    it = new TAssetInfo(element);
	else if ( className.equals("TransportationNode") )
	    it = new TransportationNode(element);
	else if ( className.equals("TypeIdentificationPGImpl$_Locked")
		  || className.equals("TypeIdentificationPGImpl") )
	    it = new TypeIdentificationPG(element);
	else if ( className.equals("UID") )
	    it = new UID(element);
	else if ( className.equals("ClusterList") )
	    it = new ClusterList(element);
	else {
	    OutputHandler.out("UIOF:create: Unknown compound type: '" + className + "'");
	    return null;
	}
	// Debug.out("UIOF: Returning: " + it);
	return it;
    }
}
