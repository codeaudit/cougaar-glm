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

package org.cougaar.glm.util;

import java.util.Date;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.lib.filter.UTILPlugin;
import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.log.Logger;

/** 
 * This class contains utility functions for allocations.
 */

public class GLMAllocate extends UTILAllocate {

  public GLMAllocate (Logger l) { super (l); }

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
  public PlanElement makeAllocation (UTILPlugin creator,
				     PlanningFactory ldmf,
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
  public PlanElement makeAllocation (UTILPlugin creator,
				     PlanningFactory ldmf,
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
  public PlanElement makeAllocation (UTILPlugin creator,
				     PlanningFactory ldmf,
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
  public PlanElement makeAllocation(UTILPlugin creator,
				    PlanningFactory ldmf,
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
