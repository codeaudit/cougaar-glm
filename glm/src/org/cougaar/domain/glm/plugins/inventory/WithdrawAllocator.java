/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBNT Solutions LLC,
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 2000 by
 *             BBNT Solutions LLC,
 *             all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.inventory;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.util.AllocationResultHelper;
import org.cougaar.domain.planning.ldm.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.util.UnaryPredicate;

import java.util.*;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.glm.plugins.AssetUtils;

public class WithdrawAllocator extends InventoryProcessor {

    protected IncrementalSubscription        withdrawTasks_;
    Hashtable bins_ = new Hashtable();
    Role role_;

    public WithdrawAllocator(InventoryPlugIn plugin, Organization org, String type, Role role)
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
        withdrawTasks_ = subscribe(inventoryPlugIn_.getDueOutPredicate(supplyType_));
    }
 
    public void update() {
	super.update(); // set up dates
	if (inventoryPlugIn_.getDetermineRequirementsTask() != null) {
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
	    inventory = inventoryPlugIn_.findOrMakeInventory(supplyType_, proto);
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
