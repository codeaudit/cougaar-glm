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

    static class WithdrawTaskPredicate implements UnaryPredicate
    {
      String supplyType_;
      public WithdrawTaskPredicate(String type) {
	supplyType_ = type;
      }
	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task)o;
		if (task.getVerb().equals(Constants.Verb.WITHDRAW) ||
		    task.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {		 
		  if (TaskUtils.isDirectObjectOfType(task, supplyType_) ||
		      TaskUtils.isTaskPrepOfType(task, supplyType_)) {
		    // 		    if (TaskUtils.getQuantity(task) > 0.0){
		    return true;
		    // 		    }
		  }
		}
	    }
	    return false;
	}
    };

    /**
     *  Set up subscriptions, 
     *  get the this plugin's organization UIC, and 
     *  initialize the OPLAN object.
     */
    private void initialize()
    {
	// Subscribe to SupplyInventory Task
        withdrawTasks_ = subscribe(new WithdrawTaskPredicate(supplyType_));
    }
 
    public void update() {
	super.update(); // set up dates
	if (inventoryPlugIn_.getDetermineRequirementsTask() != null) {
	    allocateWithdrawTasks(withdrawTasks_.getAddedList());
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
	    ar =  createEstimatedAllocationResult(wdrawTask);
	    if (ar != null) {
		if(publishAllocation(wdrawTask, inventory, role_, ar)) {
// 		    GLMDebug.DEBUG("WithdrawAllocator", clusterId_, 
// 				      "Allocating "+TaskUtils.taskDesc(wdrawTask)+" to inventory, with PlanElement:"+
// 				      wdrawTask.getPlanElement().getUID()+ ", DESCRIPTION: "+inventoryDesc(inventory));
		    num_tasks++;
		}
	    } else {
		printError("createEstimatedAllocResults failed to return ar task:"+wdrawTask+" inventory:"+inventory);
		if (publishAllocation(wdrawTask, inventory, role_)){
		    printDebug("Allocating "+TaskUtils.taskDesc(wdrawTask)+
			       " to inventory, with PlanElement:"+
			       wdrawTask.getPlanElement().getUID());
		    num_tasks++;
		}
	    }
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
	} else if ((item instanceof BulkPOL) || (item instanceof PackagedPOL)) {
	    return Constants.Role.FUELSUPPLYPROVIDER;
	} else if (item instanceof Ammunition) {
	    return Constants.Role.AMMUNITIONPROVIDER;
	}
	return null;
    }

}
