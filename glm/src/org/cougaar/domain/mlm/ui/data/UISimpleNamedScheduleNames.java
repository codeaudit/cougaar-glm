/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
