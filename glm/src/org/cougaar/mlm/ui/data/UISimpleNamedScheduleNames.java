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

package org.cougaar.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;

public interface UISimpleNamedScheduleNames {
  String ON_HAND = "On Hand";
  String ON_HAND_DETAILED = "On Hand Detailed";
  String DUE_IN = "Due In";
  String DUE_OUT = "Due Out";
  String PROJECTED_DUE_OUT = "Projected Due Out";
  String REQUESTED_DUE_IN = "Requested Due In";
  String PROJECTED_DUE_IN = "Projected Due In";
  String REQUESTED_DUE_OUT = "Requested Due Out";
  String PROJECTED_REQUESTED_DUE_OUT = "Projected Requested Due Out";
  String PROJECTED_REQUESTED_DUE_IN = "Projected Requested Due In";
  String REQUESTED_DUE_OUT_SHORTFALL = "Requested Due Out Shortfall";
  String UNCONFIRMED_DUE_IN = "Unconfirmed Due In";
  String ALLOCATED = "Allocated";
  String AVAILABLE = "Available";
  String TOTAL_LABOR = "Labor";
  String TOTAL_LABOR_8 = "Labor 8 Hours/Day";
  String TOTAL_LABOR_10 = "Labor 10 Hours/Day";
  String TOTAL_LABOR_12 = "Labor 12 Hours/Day";

  String GOAL_LEVEL = "Goal Level";
  String REORDER_LEVEL = "Reorder Level";
  String AVERAGE_DEMAND_LEVEL = "Average Demand";
  String ON_HAND_MOCK_PERIOD = "On Hand w/Mock Period";    
  String PROJECTED_DUE_IN_MOCK_PERIOD = "Projected Due In w/Mock Period";
  String PROJECTED_DUE_OUT_MOCK_PERIOD = "Requested Due Out w/Mock Period";
  String PROJECTED_REQUESTED_DUE_OUT_MOCK_PERIOD = "Projected Requested Due Out w/Mock Period";
  String PROJECTED_REQUESTED_DUE_IN_MOCK_PERIOD = "Projected Requested Due In w/Mock Period";
   
  String INACTIVE = "_INACTIVE"; // Suffix for inactive schedules
}
