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








