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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.plugins.MaintainedItem;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logging;
import org.cougaar.util.log.Logger;

/** Allocate SUPPLY tasks to local inventory (if there is any) or to 
 *  the closest supplier.
 */
public class GeneralInventoryManager extends InventoryManager {
  private static Logger logger = Logging.getLogger(GeneralInventoryManager.class);
  private IncrementalSubscription refillAllocs_ = null;

  /** Constructor */
  public GeneralInventoryManager(InventoryPlugin plugin, Organization org, String type)
  {
    super(plugin, org, type);
    if (logger.isDebugEnabled()) {
      logger.debug("Constructor type:" + type);
    }
    initialize();

  }

  // ********************************************************
  //                                                        *
  // Point Of Entry to GeneralInventoryManager              *
  //                                                        *
  // ********************************************************

  // Uses update() from superclass (Template Method pattern) (learned at JavaOne)
  // It should not be overridden.
  // This method is called everytime a subscription has changed.



  // ********************************************************
  //                                                        *
  // Need Update / Reset Section                            *
  //                                                        *
  // ********************************************************

  // RJB changed
  protected Set needUpdate(Set invSet) {
    if (invSet == null) invSet = new HashSet();
    // Determine if this inventory processor needs to run
    // return a set of changed inventories to re-process
    boolean refill_changed = checkRefills(refillAllocs_, invSet);
    boolean inventory_changed = checkInventories(modifiedInventorySubscription_.getChangedList(), invSet);
    boolean inventory_policy_changed = checkInventoryPolicies(inventoryPolicySubscription_, 
							      modifiedInventorySubscription_.elements(), invSet);
            
    // Allocations of tasks with quantity > 0 to Inventory objects
    // inventoryAllocSubscription_ only used to determine when to run processor.
    // Inventory objects held in the plugin.
    boolean allocatedInventories = checkInventoryAllocations(inventoryAllocSubscription_, invSet);

    if (refill_changed  || allocatedInventories || inventory_changed || inventory_policy_changed) {
      String prefix = "<" + supplyType_ + "> UPDATING INVENTORIES: ";
      if (refill_changed) {
        if (logger.isDebugEnabled()) {
          logger.debug(prefix + "refill changed.");
        }
      }
      if (allocatedInventories) {
        if (logger.isDebugEnabled()) {
          logger.debug(prefix + "allocations added/removed.");
        }

      }
      if (inventory_changed) {
        if (logger.isDebugEnabled()) {
          logger.debug(prefix + "inventory changed.");
        }
      }
      if (inventory_policy_changed) {
        if (logger.isDebugEnabled()) {
          logger.debug(prefix + "inventory policy changed.");
        }
      }
    }
    return invSet;
  }

  private boolean checkRefills(IncrementalSubscription refillAllocs, Set invSet) {
    Enumeration refills = refillAllocs.getChangedList();
    boolean changed = false;
    while (refills.hasMoreElements()) {
      Allocation alloc = (Allocation) refills.nextElement();
      Set changes = refillAllocs.getChangeReports(alloc);
      if (TaskUtils.checkChangeReports(changes, PlanElement.EstimatedResultChangeReport.class)) {
	Task refill = alloc.getTask();
	MaintainedItem inventoryID = 
	  (MaintainedItem)refill.getPrepositionalPhrase(Constants.Preposition.MAINTAINING).getIndirectObject();
	Inventory inv = inventoryPlugin_.findOrMakeInventory(supplyType_, inventoryID.getTypeIdentification());
	if (inv != null) {
	  invSet.add(inv);
	  changed = true;
	}
      }
    }
    return changed;
  }

  private boolean checkInventories(Enumeration changedInventories, Set invSet) {
    boolean changed = changedInventories.hasMoreElements();
    while (changedInventories.hasMoreElements()) {
      invSet.add((Inventory) changedInventories.nextElement());
    }
    return changed;
  }

  private boolean checkInventoryPolicies(IncrementalSubscription policySubscription, Enumeration inventories, Set invSet) {
    boolean changed = updateInventoryPolicy(policySubscription.getAddedList()) ||
      updateInventoryPolicy(policySubscription.getChangedList());
    if (changed) {
      while (inventories.hasMoreElements()) {
	invSet.add((Inventory) inventories.nextElement());
      }
    }
    return changed;
  }

  private boolean checkInventoryAllocations(IncrementalSubscription invAllocSubscription, Set invSet) {
    boolean changed = false;
    if (invAllocSubscription.hasChanged()) {
      Enumeration allocs = invAllocSubscription.getAddedList();
      while (allocs.hasMoreElements()) {
	Allocation alloc = (Allocation) allocs.nextElement();
	if (!inventoryPlugin_.hasSeenAllConsumers()) {
	  inventoryPlugin_.recordCustomerForTask(alloc.getTask());
	}
	invSet.add(alloc.getAsset());
	changed = true;
      }
      allocs = invAllocSubscription.getRemovedList();
      while (allocs.hasMoreElements()) {
	invSet.add(((Allocation) allocs.nextElement()).getAsset());
	changed = true;
      }
    }
    return changed;
  }

  // ********************************************************
  //                                                        *
  // Utilities Section                                      *
  //                                                        *
  // ********************************************************

  // Public

  private Enumeration getAllScheduleElements(Inventory inventory) {
    ScheduledContentPG scp = inventory.getScheduledContentPG();
    return scp.getSchedule().getAllScheduleElements();
  }
        
  // this should be over-ridden for specialized inventory managers
  protected double getMinRefillQuantity(Inventory inventory) {
    return inventory.getVolumetricStockagePG().getMinReorderVolume().getGallons();
  }

  // ********************************************************
  //                                                        *
  // Initialization  Section                                *
  //                                                        *
  // ********************************************************

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

  /** Initialize this instance. */
  protected void initialize() {
    setupSubscriptions();
  }

  /** Initialize my subscriptions. */
  protected void setupSubscriptions() {
	
    refillAllocs_ = subscribe(new RefillAllocPredicate(supplyType_, myOrgName_));
	
    if (plugin_.didRehydrate()) {
      updateInventoryPolicy(Collections.enumeration(inventoryPolicySubscription_.getCollection()));
    }

  }
}
