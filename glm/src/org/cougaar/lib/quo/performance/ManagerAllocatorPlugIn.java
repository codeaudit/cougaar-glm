/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */
package org.cougaar.lib.quo.performance;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.asset.Asset;

import java.util.Enumeration;

/**
 * This COUGAAR PlugIn allocates tasks of verb "CODE"
 * to Organizations that have the "SoftwareDevelopment" role.
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: ManagerAllocatorPlugIn.java,v 1.2 2001-08-16 14:33:06 psharma Exp $
 **/
public class ManagerAllocatorPlugIn extends SimplePlugIn {
  
  private IncrementalSubscription tasks;         // "CODE" tasks
  private IncrementalSubscription programmers;   // SoftwareDevelopment orgs
  private IncrementalSubscription allocations;   // My allocations


  /**
   * subscribe to tasks and programming organizations
   */
  protected void setupSubscriptions() {
    tasks = (IncrementalSubscription)subscribe(new myTaskPredicate());
    programmers = (IncrementalSubscription)subscribe(new myProgrammersPredicate());
    allocations = (IncrementalSubscription)subscribe(new myAllocationPredicate());
  }


  /**
   * Top level plugin execute loop.  Allocate CODE tasks to organizations
   */
  protected void execute () {
      allocateUnallocatedTasks();
  }

  /**
   * Allocate the task to the asset
   */
  private void allocateTo(Asset asset, Task task) {
    AllocationResult estAR = null;
    Allocation allocation =
      theLDMF.createAllocation(task.getPlan(), task, asset,estAR, Role.ASSIGNED);
    publishAdd(allocation);
  }

    protected void  allocateUnallocatedTasks(){
  	//   process unallocated tasks
	Enumeration task_enum = tasks.elements();
	while (task_enum.hasMoreElements()) {
	    Task t = (Task)task_enum.nextElement();
	    if (t.getPlanElement() != null)
		continue;  //already allocated
	    Asset organization = (Asset)programmers.first();
	    //Asset organization = (Asset)programmers;
	    if (organization != null)   //if no organization yet, give up for now
		allocateTo(organization, t);
	}
  }
}





