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
