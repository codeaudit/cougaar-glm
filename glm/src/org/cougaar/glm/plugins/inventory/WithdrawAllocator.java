/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.inventory;

import java.util.Enumeration;
import java.util.Hashtable;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.debug.GLMDebug;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Ammunition;
import org.cougaar.glm.ldm.asset.BulkPOL;
import org.cougaar.glm.ldm.asset.Consumable;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.PackagedPOL;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.util.AllocationResultHelper;

public class WithdrawAllocator extends InventoryProcessor {

    protected IncrementalSubscription        withdrawTasks_;
    Hashtable bins_ = new Hashtable();
    Role role_;

    public WithdrawAllocator(InventoryPlugin plugin, Organization org, String type, Role role)
    {
	super(plugin, org, type);
	role_ = role;
	initialize();
    }

    /**
     *  Set up subscriptions, 
     *  get the this plugin's organization UIC, and 
     *  initialize the OPLAN object.
     */
    private void initialize()
    {
	// Subscribe to SupplyInventory Task
        withdrawTasks_ = subscribe(inventoryPlugin_.getDueOutPredicate(supplyType_));
    }
 
    public void update() {
	super.update(); // set up dates
	if (inventoryPlugin_.getDetermineRequirementsTask() != null) {
	    allocateWithdrawTasks(withdrawTasks_.getAddedList());
	    allocateWithdrawTasks(withdrawTasks_.getChangedList());
	}
    }

    protected void allocateWithdrawTasks(Enumeration tasks) {
	Task wdrawTask;
	Inventory inventory;
	Asset proto;
	AllocationResult ar;
	int num_tasks=0;
	while (tasks.hasMoreElements()) {
	    wdrawTask = (Task)tasks.nextElement();
	    proto = (Asset)wdrawTask.getDirectObject();
	    inventory = inventoryPlugin_.findOrMakeInventory(supplyType_, proto);
	    // A withdraw task is not created unless findOrMakeInventory returns an
	    // inventory object, so this should never happen.
	    if (inventory == null)  {
		String typeID = proto.getTypeIdentificationPG().getTypeIdentification();
		GLMDebug.ERROR("WithdrawAllocator", clusterId_, "Inventory NOT found for "+typeID);
		continue;
	    }
            boolean isNew = wdrawTask.getPlanElement() == null;
            if (isPrintConcise()) {
                printConcise("allocateWithdrawTasks() "
                             + (isNew ? "new " : "change ")
                             + wdrawTask.getUID()
                             + " "
                             + TaskUtils.getDailyQuantity(wdrawTask)
                             + (TaskUtils.isProjection(wdrawTask)
                                ? (" from "
                                   + TimeUtils.dateString(TaskUtils.getStartTime(wdrawTask))
                                   + " to "
                                   + TimeUtils.dateString(TaskUtils.getEndTime(wdrawTask)))
                                : (" on "
                                   + TimeUtils.dateString(TaskUtils.getEndTime(wdrawTask)))));
            }
            if (isNew) {
                ar = new AllocationResultHelper(wdrawTask, null).getAllocationResult(1.0);
                if (publishAllocation(wdrawTask, inventory, role_, ar)) {
// 		    GLMDebug.DEBUG("WithdrawAllocator", clusterId_, 
// 				      "Allocating "+TaskUtils.taskDesc(wdrawTask)+" to inventory, with PlanElement:"+
// 				      wdrawTask.getPlanElement().getUID()+ ", DESCRIPTION: "+inventoryDesc(inventory));
		    num_tasks++;
		}
	    }
            delegate_.publishChange(inventory);
	}
	if (num_tasks > 0) {
	    printDebug("allocateWithdrawTasks()  allocated "+num_tasks+
		       " tasks to Inventory objects");
	}

    }

    public Role roleForTask(Task t) {
	Asset  item = t.getDirectObject();
	if (item instanceof Consumable) {
	    return Constants.Role.SPAREPARTSPROVIDER;
	} else if (item instanceof BulkPOL) {
	    return Constants.Role.FUELSUPPLYPROVIDER;	
	} else if (item instanceof PackagedPOL) {
	    return Constants.Role.PACKAGEDPOLSUPPLYPROVIDER;
	} else if (item instanceof Ammunition) {
	    return Constants.Role.AMMUNITIONPROVIDER;
	}
	return null;
    }

}
