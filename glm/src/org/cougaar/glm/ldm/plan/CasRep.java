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

package org.cougaar.glm.ldm.plan;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Date;
import org.cougaar.core.util.UniqueObject;

public interface CasRep
  extends UniqueObject
{

  // Types of status for a CasRep
  String TICKET_COMPLETED = "Ticket Completed";
  String INITIAL_TICKET = "Trouble Ticket";
  String TROUBLESHOOTING= "Troubleshooting";

  String PARTS_REQUIRED = "Parts Required, Not In Stock";
  String PARTS_ORDERED = "Parts Ordered";
  String AWAITING_PARTS = "Awaiting Parts Delivery";

  String ASSISTANCE_REQUIRED = "Technical Assistance Requested";

  String AWAITING_MAINTENANCE = "Awaiting Maintenance";
  String REPAIR_UNDERWAY = "Repair Underway";
  String REPAIR_COMPLETED = "Repair Completed";
	
	// Type of classifications for a given CasRep
  String UNCLASSIFIED = "Unclassified";

  String C2 = "C2";
  String C3 = "C3";
  String C4 = "C4";

  String formatDate(java.util.Date param);
  String formatShortDate(java.util.Date param);
  String getDisplayDate();
  long getTimeStamp();
  String getTicketNumber();
  String getOperator();
  String getMachine();
  String getSerialNumber();
  String getSpace();
  String getSymptoms();
  String getRemarks();
  Hashtable getMisc();
  boolean isMissionCritical();
  String getStatus();
  String getBrokenPart();
  String getBrokenPartDesc();
  String getBrokenPartStatus();
  boolean isCasRep();
  boolean isNewCasRep();
  String getVersion();
  String getNextVersion();
  String getFrom();
  String getFromCluster();
  String getTo();
  String getInfo();
  String getClassification();
  String getCriticalityCode();
  Object getBrokenMEI();
  String getData();
  String getCode();
  String getCasRepNumber();
  String getOnShipCasRepNumber();
  String getNextOnShipCasRepNumber();
}
