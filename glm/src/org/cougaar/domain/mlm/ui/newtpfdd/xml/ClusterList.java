/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/ClusterList.java,v 1.1 2001-02-22 22:42:36 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.xml;


import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;

import org.cougaar.domain.mlm.ui.tpfdd.producer.UIVector;


public class ClusterList extends LogPlanObject
{
    private String[] names;

    public ClusterList(Element xml)
    {
	super(null, xml);
    }

    // expects a comma separated list of cluster names
    public ClusterList(String clusterList)
    {
	super(null, null);
	if ( clusterList == null ) {
	    names = new String[15];
	    names[0] = "GlobalSea";
	    names[1] = "GlobalAir";
	    names[2] = "FortStewartCommercial"; 
	    names[3] = "FortStewartITO";
	    names[4] = "FortBenningCommercial";
	    names[5] = "FortBenningITO";
	    names[6] = "VirtualC141Wing";
	    names[7] = "SunnyPointPort";
	    names[9] = "SavannahPort";
	    names[9] = "JubailPort";
	    names[10] = "DammamPort";
	    names[11] = "6thAMSMcGuire";
	    names[12] = "VirtualAir";
            names[13] = "TheaterFort";
            names[14] = "OrganicAir";
	    return;
	}

	UIVector clusterNames = new UIVector();
	clusterList += ',';
	while ( true ) {
	    if ( clusterList.indexOf(',') == 0 ) {
		OutputHandler.out("ClusterList:ClusterList Error: invalid cluster list: found bad comma: -->" + clusterList
				  + ". Fix and restart.");
		return;
	    }
	    clusterNames.add(clusterList.substring(0, clusterList.indexOf(',')));
	    if ( clusterList.indexOf(',') == clusterList.length() - 1 )
		break;
	    clusterList = clusterList.substring(clusterList.indexOf(',') + 1, clusterList.length());
	}
	names = clusterNames.toStrings();
    }

    public String[] getNames()
    {
	return names;
    }

    public void setNames(String[] names)
    {
	this.names = names;
    }
}
