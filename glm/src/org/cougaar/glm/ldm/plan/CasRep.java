/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.ldm.plan;

import java.util.Hashtable;

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
