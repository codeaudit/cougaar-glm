/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.plan.Schedule;

import java.text.DateFormat;
import java.util.Date;

import org.cougaar.util.log.*;

/**
 * Parses the schedule tag, which defines the initial role schedule 
 * availability of an asset.
 */
public class ScheduleParser{
  private static Logger logger;
  public void setLogger (Logger log) { logger = log; }

  public Schedule getSchedule( LDMServesPlugin ldm, Node node){

    Schedule newSchedule = null;

    if(node.getNodeName().equals("schedule")){

      String start = node.getAttributes().getNamedItem("start").getNodeValue();
      String end = node.getAttributes().getNamedItem("end").getNodeValue();

      try {
	DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
	Date startDate = df.parse(start);
	Date endDate = df.parse(end);
	if (endDate.before (startDate))
	  logger.error ("Hey! in creating asset instance, start date " + startDate +
			      " is after " + endDate);
	newSchedule = ldm.getFactory().newSimpleSchedule(startDate,endDate);
      }
      catch (Exception e) {
	logger.error ("Got exception ", e);
      }
    }
    // then throw some sort of error if not object should not get here!
    return newSchedule;
  }   
}

