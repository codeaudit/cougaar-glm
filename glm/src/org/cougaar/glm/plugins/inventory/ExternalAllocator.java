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

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.plugins.AssetUtils;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.util.Enumeration;
import java.util.Vector;

public class ExternalAllocator extends InventoryProcessor {

    protected IncrementalSubscription refillAllocs_ = null;
    protected IncrementalSubscription providerOrgs_ = null;
    /** Organizations with the right type of role */
    public Vector providers_ = new Vector();
    protected Role providerRole_;
    /** list of nsn's with no resupply point */
    protected Vector notified_ = new Vector();
    private Logger logger = Logging.getLogger(ExternalAllocator.class);

    public ExternalAllocator(InventoryPlugin plugin, Organization org, String type, org.cougaar.planning.ldm.plan.Role role) {
	super(plugin, org, type);
	providerRole_ = role;
 	providerOrgs_ = subscribe(new ItemProviderPredicate(org, providerRole_));
	refillAllocs_ = subscribe(new RefillAllocPredicate(supplyType_, myOrgName_));
    }
	
    //Allocation of refill tasks
    static class RefillAllocPredicate implements UnaryPredicate
    {
	String type_;
	String orgName_;

	public RefillAllocPredicate(String type, String orgName) {
	    type_ = type;
	    orgName_ = orgName;
	}

	public boolean execute(Object o) {
	    if (o instanceof Allocation ) {
		Task task = ((Allocation)o).getTask();
                Verb verb = task.getVerb();
		if (verb.equals(Constants.Verb.SUPPLY)
                    || verb.equals(Constants.Verb.PROJECTSUPPLY)) {
		    if (TaskUtils.isDirectObjectOfType(task, type_)) {
			// need to check if externally allocated
			if(((Allocation)o).getAsset() instanceof Organization) {
                            if (TaskUtils.isMyRefillTask(task, orgName_)){
                                return true;
                            }
                        }
                    }
		}
	    }
	    return false;
	}
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
	if (inventoryPlugin_.getDetermineRequirementsTask() != null) {
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
            PluginHelper.updateAllocationResult(refillAllocs_);
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
	    inv = inventoryPlugin_.findOrMakeInventory(supplyType_, proto);
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
	Enumeration support_orgs;
        if (TaskUtils.isProjection(task)) {
            /* For a projection, should be time-phased as support
               changes over time. We ignore that, for now */
            support_orgs = AssetUtils.getSupportingOrgs(myOrganization_, providerRole_, 
                                                        TaskUtils.getStartTime(task), TaskUtils.getEndTime(task));
        } else {
            support_orgs = AssetUtils.getSupportingOrgs(myOrganization_, providerRole_, 
                                                        TaskUtils.getEndTime(task));
        }
	if (support_orgs.hasMoreElements()) {
	    // For now, returning the first supporting org during the time span
	    return (Organization)support_orgs.nextElement();
	}
	else {
	    if (TaskUtils.isProjection(task)) {
          if (logger.isErrorEnabled()) {
            logger.error("No " + providerRole_ + ", during " + TimeUtils.dateString(TaskUtils.getStartTime(task)) + TimeUtils.dateString(TaskUtils.getEndTime(task)));
          }
        } else {
          if (logger.isErrorEnabled()) {
            logger.error("No " + providerRole_ + ", during " + TimeUtils.dateString(TaskUtils.getEndTime(task)));
          }
        }
	}
	return null;
    }

    protected void allocateTask(Task task) {
	String itemId = task.getDirectObject().getTypeIdentificationPG().getTypeIdentification();
	Organization provider = findBestSource(task);
	if (provider == null) {
	    // prevents same error message from being printed many times.
	    if (!notified_.contains(itemId)) {
          if (logger.isDebugEnabled()) {
            logger.debug("allocateTask() <" + supplyType_ + "> allocateRefillTask no best source for " + itemId);
          }
          notified_.addElement(itemId);
        }
	} else {
// 	    printDebug("allocateRefillTask "+task+" to "+provider);
	    AllocationResult ar =  new AllocationResultHelper(task, null).getAllocationResult(1.0);
	    publishAllocation(task, provider, providerRole_, ar);
	}
    }
}
