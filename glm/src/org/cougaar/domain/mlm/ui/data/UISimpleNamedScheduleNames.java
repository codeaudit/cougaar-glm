/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;

public interface UISimpleNamedScheduleNames {
  public final static String ON_HAND = "On Hand";
  public final static String ON_HAND_DETAILED = "On Hand Detailed";
  public final static String DUE_IN = "Due In";
  public final static String DUE_OUT = "Due Out";
  public final static String PROJECTED_DUE_OUT = "Projected Due Out";
  public final static String REQUESTED_DUE_IN = "Requested Due In";
  public final static String PROJECTED_DUE_IN = "Projected Due In";
  public final static String REQUESTED_DUE_OUT = "Requested Due Out";
  public final static String PROJECTED_REQUESTED_DUE_OUT = "Projected Requested Due Out";
  public final static String PROJECTED_REQUESTED_DUE_IN = "Projected Requested Due In";
  public final static String REQUESTED_DUE_OUT_SHORTFALL = "Requested Due Out Shortfall";
  public final static String UNCONFIRMED_DUE_IN = "Unconfirmed Due In";
  public final static String ALLOCATED = "Allocated";
  public final static String AVAILABLE = "Available";
  public final static String TOTAL_LABOR = "Labor";
  public final static String TOTAL_LABOR_8 = "Labor 8 Hours/Day";
  public final static String TOTAL_LABOR_10 = "Labor 10 Hours/Day";
  public final static String TOTAL_LABOR_12 = "Labor 12 Hours/Day";

  public final static String GOAL_LEVEL = "Goal Level";
  public final static String REORDER_LEVEL = "Reorder Level";
  public final static String AVERAGE_DEMAND_LEVEL = "Average Demand";
  public final static String ON_HAND_MOCK_PERIOD = "On Hand w/Mock Period";    
  public final static String PROJECTED_DUE_IN_MOCK_PERIOD = "Projected Due In w/Mock Period";
  public final static String PROJECTED_DUE_OUT_MOCK_PERIOD = "Requested Due Out w/Mock Period";
  public final static String PROJECTED_REQUESTED_DUE_OUT_MOCK_PERIOD = "Projected Requested Due Out w/Mock Period";
  public final static String PROJECTED_REQUESTED_DUE_IN_MOCK_PERIOD = "Projected Requested Due In w/Mock Period";
   

  public final static String INACTIVE = "_INACTIVE"; // Suffix for inactive schedules
}
