/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.inventory;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.core.domain.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;

import org.cougaar.util.UnaryPredicate;

import java.util.*;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.debug.*;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.glm.plugins.AssetUtils;

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
