/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/util/GLMAllocate.java,v 1.1 2001-12-27 22:42:14 bdepass Exp $ */
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

package org.cougaar.glm.util;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.glm.ldm.Constants;

import org.cougaar.lib.filter.UTILPlugIn;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import org.cougaar.lib.util.UTILAllocate;

/** 
 * This class contains utility functions for allocations.
 */

public class GLMAllocate extends UTILAllocate {

  /**
   * Provides backword compatibility for next method.
   *
   * Deprecated :
   * 
   * Each plugin should think of the role the assigned asset is 
   * acting in and migrate to using the other version of makeAllocation.
   *
   * Assigns a default role of TRANSPORTER.
   *
   * @see org.cougaar.planning.ldm.plan.Role
   * @deprecated
   */
  public static PlanElement makeAllocation (UTILPlugIn creator,
					    RootFactory ldmf,
					    Plan plan,
					    Task t,
					    Asset asset,
					    Date start,
					    Date end,
					    double cost,
					    double confidence) {
    return makeAllocation (creator, ldmf, plan, t, asset, start, end, cost,
			   confidence, Constants.Role.TRANSPORTER);
  }


					   
  /**
   * Provides backword compatibility for next method.
   *
   * Deprecated :
   * 
   * Each plugin should think of the role the assigned asset is 
   * acting in and migrate to using the other version of makeAllocation.
   *
   * Assigns a default role of TRANSPORTER.
   *
   * @see org.cougaar.planning.ldm.plan.Role
   * @deprecated
   */
  public static PlanElement makeAllocation (UTILPlugIn creator,
					    RootFactory ldmf,
					    Plan plan,
					    Task t,
					    Asset asset,
					    Date start,
					    Date end,
					    double confidence) {
    return makeAllocation (creator,
			   ldmf, plan, t, asset, start, end, 
			   confidence, Constants.Role.TRANSPORTER);
  }
	   
  /**
   * Provides backword compatibility for next method.
   *
   * Deprecated :
   * 
   * Each plugin should think of the role the assigned asset is 
   * acting in and migrate to using the other version of makeAllocation.
   *
   * Assigns a default role of TRANSPORTER.
   *
   * @see org.cougaar.planning.ldm.plan.Role
   * @deprecated
   */
 public static PlanElement makeAllocation (UTILPlugIn creator,
					   RootFactory ldmf,
					    Plan plan,
					    Task t,
					    Asset asset,
					    AspectValue [] aspects,
					    double confidence) {
    return makeAllocation (creator,
			   ldmf, plan, t, asset, aspects, 
			   confidence, Constants.Role.TRANSPORTER);
  }
				  

  /**
   * Provides backword compatibility for next method.
   *
   * Deprecated :
   * 
   * Each plugin should think of the role the assigned asset is 
   * acting in and migrate to using the other version of makeAllocation.
   *
   * Assigns a default role of TRANSPORTER.
   *
   * @see org.cougaar.planning.ldm.plan.Role
   * @deprecated
   */
  public static PlanElement makeAllocation(UTILPlugIn creator,
					   RootFactory ldmf,
					   Plan plan,
					   Task t,
					   Asset asset,
					   int[] aspectarray,
					   double[] resultsarray,
					   double confidence) {
    return makeAllocation (creator,
			   ldmf, plan, t, asset, aspectarray, resultsarray,
			   confidence, Constants.Role.TRANSPORTER);
  }

}
