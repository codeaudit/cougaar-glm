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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */
package org.cougaar.lib.quo.performance;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.OrganizationPG;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.UnaryPredicate;


/**
 * This COUGAAR Plugin allocates tasks of verb "CODE"
 * to Organizations that have the "SoftwareDevelopment" role.
 * @author ALPINE (alpine-software@bbn.com)
 *
 **/
public class ManagerAllocatorPlugin extends  CommonUtilPlugin {
  
    private IncrementalSubscription tasks;         // "CODE" tasks
    private IncrementalSubscription programmers;   // SoftwareDevelopment orgs
    private IncrementalSubscription allocations;   // My allocations
    protected  String VERB ;//= getParameterValue(getParameters(), "VERB");
    protected String DEPARTMENT="SoftwareDevelopment"; //defaults

    // protected Verb verb = Verb.get(VERB);
     /**
     * parsing the plugIn arguments and setting the values for CPUCONSUME and MESSAGESIZE
     */
      protected void parseParameter(){
 	Vector p = getParameters();
 	VERB=getParameterValue(p, "VERB");
	DEPARTMENT=getParameterValue(p, "DEPARTMENT");
  	//System.out.println("ManagerAllocator ===" + DEPARTMENT);
      }

    public UnaryPredicate myAllocationPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Allocation) {
		    Task task = ((Allocation)o).getTask();
		    
		return (task != null) && (task.getVerb().equals(Verb.get(VERB)));
		}
		return false;
	    }
	};


    /**
     * Predicate that matches VERB" 
     */
    private UnaryPredicate myTaskPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Task)
		    {
			Task task = (Task)o;
			return task.getVerb().equals(Verb.get(VERB));
		    }
		return false;
	    }
	};

    protected UnaryPredicate  myProgrammersPredicate = new  UnaryPredicate(){
	    public boolean execute(Object o) {
		boolean ret = false;
		if (o instanceof Organization) {
		    Organization org = (Organization)o;
		    OrganizationPG orgPG = org.getOrganizationPG();
		    //ret = orgPG.inRoles(Role.getRole("SoftwareDevelopment"));
		    ret = orgPG.inRoles(Role.getRole(DEPARTMENT));
		}
		return ret;
	    }
	};

    /**
     * subscribe to tasks and programming organizations
     */
    protected void setupSubscriptions() {
	parseParameter();
	tasks = (IncrementalSubscription)subscribe( myTaskPredicate);
	programmers = (IncrementalSubscription)subscribe(myProgrammersPredicate);
	allocations = (IncrementalSubscription)subscribe(myAllocationPredicate);
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
  	Enumeration task_enum = tasks.elements();	//   process unallocated tasks
	while (task_enum.hasMoreElements()) {
	    Task t = (Task)task_enum.nextElement();
	    if (t.getPlanElement() != null)
		continue;  //already allocated
	    Asset organization = (Asset)programmers.first();
	    if (organization != null)   //if no organization yet, give up for now
		allocateTo(organization, t);
	}
    }
}








