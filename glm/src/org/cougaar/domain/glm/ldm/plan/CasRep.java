/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Date;
import org.cougaar.core.society.UniqueObject;

public interface CasRep
  extends UniqueObject
{

  // Types of status for a CasRep
  public static final String TICKET_COMPLETED = "Ticket Completed";
  public static final String INITIAL_TICKET = "Trouble Ticket";
  public static final String TROUBLESHOOTING= "Troubleshooting";

  public static final String PARTS_REQUIRED = "Parts Required, Not In Stock";
  public static final String PARTS_ORDERED = "Parts Ordered";
  public static final String AWAITING_PARTS = "Awaiting Parts Delivery";

  public static final String ASSISTANCE_REQUIRED = "Technical Assistance Requested";

  public static final String AWAITING_MAINTENANCE = "Awaiting Maintenance";
  public static final String REPAIR_UNDERWAY = "Repair Underway";
  public static final String REPAIR_COMPLETED = "Repair Completed";
	
	// Type of classifications for a given CasRep
  public static final String UNCLASSIFIED = "Unclassified";

  public static final String C2 = "C2";
  public static final String C3 = "C3";
  public static final String C4 = "C4";

  public String formatDate(java.util.Date param);
  public String formatShortDate(java.util.Date param);
  public String getDisplayDate();
  public long getTimeStamp();
  public String getTicketNumber();
  public String getOperator();
  public String getMachine();
  public String getSerialNumber();
  public String getSpace();
  public String getSymptoms();
  public String getRemarks();
  public Hashtable getMisc();
  public boolean isMissionCritical();
  public String getStatus();
  public String getBrokenPart();
  public String getBrokenPartDesc();
  public String getBrokenPartStatus();
  public boolean isCasRep();
  public boolean isNewCasRep();
  public String getVersion();
  public String getNextVersion();
  public String getFrom();
  public String getFromCluster();
  public String getTo();
  public String getInfo();
  public String getClassification();
  public String getCriticalityCode();
  public Object getBrokenMEI();
  public String getData();
  public String getCode();
  public String getCasRepNumber();
  public String getOnShipCasRepNumber();
  public String getNextOnShipCasRepNumber();
}
