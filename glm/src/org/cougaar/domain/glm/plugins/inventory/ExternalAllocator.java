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

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.util.MutableTimeSpan;
import org.cougaar.util.NewTimeSpan;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.asset.AssignedPG;
import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.asset.Inventory;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.glm.plugins.AssetUtils;

public class ExternalAllocator extends InventoryProcessor {

    protected IncrementalSubscription        providerOrgs_ = null;
    /** Organizations with the right type of role */
    public Vector providers_ = new Vector();
    protected Role providerRole_;
    /** list of nsn's with no resupply point */
    protected Vector notified_ = new Vector();

    public ExternalAllocator(InventoryPlugIn plugin, Organization org, String type, org.cougaar.domain.planning.ldm.plan.Role role) {
	super(plugin, org, type);
	providerRole_ = role;
 	providerOrgs_ = subscribe(new ItemProviderPredicate(org, providerRole_));
    }
	
    
    /** Test if we have an Organization that is a provider_. */ 
    public static class ItemProviderPredicate implements UnaryPredicate {
	Role provider_;
 	Organization org_;

	public ItemProviderPredicate(Organization org, Role prov) {
	    org_ = org;
	    provider_ = prov;
	}

	public boolean execute(Object o) {
	    if (o instanceof Organization) {
		Organization org = (Organization) o;
 		return AssetUtils.isOrgSupporting(org_, org, provider_);
	    }
	    return false;
	}
    }

    /** This method is called everytime a subscription has changed. */
    public void update() {
	super.update(); // set up dates
	if (inventoryPlugIn_.getDetermineRequirementsTask() != null) {
	    notified_.clear();
	    rememberProviders();
	    // Supply tasks that are not handled in this cluster are allocated to
	    // an external cluster
	    allocateSupplyTasks(supplyTasks_.getAddedList());
	    // Allocate projection tasks for this cluster to an external cluster
	    allocateSupplyTasks(projectionTasks_.getAddedList());
	    // Allocate refill tasks for this cluster to external cluster
	    allocateRefillTasks(refillTasks_.getAddedList());
	    // Allocate inventory projections
	    allocateRefillTasks(myProjectionTasks_.getAddedList());
	}
    }

    /** reset providers table */
    public void rememberProviders() {
	Enumeration orgs = providerOrgs_.elements();
	providers_.clear();
	while (orgs.hasMoreElements()) {
	    Organization org = (Organization) orgs.nextElement();
	    // guess can only put one type of thing one place...
	    // can't send spark plug requests to one place and 
	    //  tires to another.
	    providers_.addElement(org);
	}
    }

    public void allocateSupplyTasks(Enumeration tasks) {
	Task supplyTask;
	Inventory inv=null;
	Asset proto;
	int allocatedTasks = 0;
	while (tasks.hasMoreElements()) {
	    supplyTask = (Task)tasks.nextElement();
	    proto = (Asset)supplyTask.getDirectObject();
	    // If inventory exists for this item then ignore task
	    // Let the Supply Expander handle it.
	    inv = inventoryPlugIn_.findOrMakeInventory(supplyType_, proto);
	    if (inv == null) {
		allocateTask(supplyTask);
// 		printDebug("allocateSupplyTasks(), allocated "+TaskUtils.taskDesc(supplyTask)+
// 			   " tasks to external sources.");
		allocatedTasks++;
	    }
	}
//  	printDebug("allocateSupplyTasks(), allocated "+allocatedTasks+
//  		   " tasks to external sources.");
    }

    public void allocateRefillTasks(Enumeration tasks) {
	Task refillTask;
	while (tasks.hasMoreElements()) {
	    refillTask = (Task)tasks.nextElement();
	    allocateTask(refillTask);
	}
    }

    /** Figure out which organization supplying item is best for us. */
    public Organization findBestSource(Task task) {
	// PAS FILL THIS IN
	Enumeration enum = providers_.elements();
	Enumeration support_orgs = AssetUtils.getSupportingOrgs(myOrganization_, providerRole_, 
								TaskUtils.getStartTime(task), TaskUtils.getEndTime(task));
	if (support_orgs.hasMoreElements()) {
	    // For now, returning the first supporting org during the time span
	    return (Organization)support_orgs.nextElement();
	}
	else {
	    printError("No "+providerRole_+", during "+TimeUtils.dateString(TaskUtils.getStartTime(task))+
		       TimeUtils.dateString(TaskUtils.getEndTime(task)));
	}
	return null;
    }

    protected void allocateTask(Task task) {
	String itemId = task.getDirectObject().getTypeIdentificationPG().getTypeIdentification();
	Organization provider = findBestSource(task);
	if (provider == null) {
	    // prevents same error message from being printed many times.
	    if (!notified_.contains(itemId)) {
		printDebug("allocateTask() <"+supplyType_+
			   "> allocateRefillTask no best source for "+itemId);
		notified_.addElement(itemId);
	    }
	} else {
// 	    printDebug("allocateRefillTask "+task+" to "+provider);
	    AllocationResult ar = createEstimatedAllocationResult(task);
	    publishAllocation(task, provider, providerRole_, ar);
	}
    }

}
