/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.plan.Schedule;

import java.text.DateFormat;
import java.util.Date;


/**
 * Parses the schedule tag, which defines the initial role schedule 
 * availability of an asset.
 */
public class ScheduleParser{

  public static Schedule getSchedule( LDMServesPlugIn ldm, Node node){

    Schedule newSchedule = null;

    if(node.getNodeName().equals("schedule")){

      String start = node.getAttributes().getNamedItem("start").getNodeValue();
      String end = node.getAttributes().getNamedItem("end").getNodeValue();

      try {
	DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
	Date startDate = df.parse(start);
	Date endDate = df.parse(end);
	if (endDate.before (startDate))
	  System.out.println ("Hey! in creating asset instance, start date " + startDate +
			      " is after " + endDate);
	newSchedule = ldm.getFactory().newSimpleSchedule(startDate,endDate);
      }
      catch (Exception e) {
	e.printStackTrace();
      }
    }
    // then throw some sort of error if not object should not get here!
    return newSchedule;
  }   

}

