/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

import org.cougaar.domain.planning.ldm.plan.ScheduleType;

/** 
 * Planning Schedule types
 */
public interface PlanScheduleType extends ScheduleType {
  static final String TOTAL_CAPACITY = "Total_Capacity";
  static final String ALLOCATED_CAPACITY = "Allocated_Capacity";
  static final String AVAILABLE_CAPACITY = "Available_Capacity";

  static final String TOTAL_INVENTORY = "Total_Inventory";
  static final String ACTUAL_CAPACITY = "Actual_Capacity";

  static final String LABOR = "Labor";
}

