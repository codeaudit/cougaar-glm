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
 * @version $Id: ManagerAllocatorPlugIn.java,v 1.3 2001-08-22 20:28:06 mthome Exp $
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





