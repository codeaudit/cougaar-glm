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

import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.glm.debug.GLMDebug;
import org.cougaar.domain.glm.execution.common.InventoryReport;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Inventory;
import org.cougaar.domain.glm.ldm.asset.InventoryPG;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.util.UnaryPredicate;

public class DeletionProcessor extends InventoryProcessor {
    protected IncrementalSubscription tasks_;
    private Calendar tCalendar_ = Calendar.getInstance();

    /** Delete old reports when oldest becomes this old **/
    private static final long INVENTORY_REPORT_CUTOFF = 4 * TimeUtils.MSEC_PER_WEEK;

    /** When pruning old reports, remove all older than this age **/
    private static final long INVENTORY_REPORT_PRUNE = TimeUtils.MSEC_PER_WEEK;

    public DeletionProcessor(InventoryPlugIn plugin, Organization org, String type) {
        super(plugin, org, type);
	initialize();
    }

    static class InventoryTaskPredicate implements UnaryPredicate {
        String supplyType_;
        String myOrgName_;
        public InventoryTaskPredicate(String type, String orgName) {
            supplyType_ = type;
            myOrgName_ = orgName;
        }
	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task) o;
                Verb verb = task.getVerb();
		if (verb.equals(Constants.Verb.WITHDRAW) ||
		    verb.equals(Constants.Verb.PROJECTWITHDRAW)) {
                    return (TaskUtils.isDirectObjectOfType(task, supplyType_) ||
                            TaskUtils.isTaskPrepOfType(task, supplyType_));
                }
                if (verb.equals(Constants.Verb.SUPPLY)) {
                    if (TaskUtils.isDirectObjectOfType(task, supplyType_) ||
                        TaskUtils.isTaskPrepOfType(task, supplyType_)) {
                        return TaskUtils.isMyRefillTask(task, myOrgName_);
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
	// Subscribe to withdraw and refill tasks
        tasks_ = subscribe(new InventoryTaskPredicate(supplyType_, myOrgName_));
    }
 
    public void update() {
	super.update(); // set up dates
	if (inventoryPlugIn_.getDetermineRequirementsTask() != null) {
	    removeTasks(tasks_.getRemovedList());
	}
    }

    /**
     * Process all the removed tasks. For each removed task find the
     * relevant Inventory and assocate with that inventory the time of
     * the latest such task. After all tasks have been examined,
     * create an InventoryReport reflecting the inventory level at the
     * time of that task (pushed to the end of the day) and add that
     * inventory report to the InventoryBG.
     *
     * Note that we don't perform a publishChange on the inventory
     * except when old inventory reports are pruned because we are
     * dealing with ancient history and the inventory level going
     * forward from the time of the removed tasks should not have been
     * changed by our actions. We do publishChange when old reports
     * are pruned as a debugging aid. Otherwise, we might not see any
     * bad effects.
     *
     * Inventory reports are pruned whenever the oldest report is more
     * than one week old.
     **/
    private void removeTasks(Enumeration tasks) {
        Map tMap = new HashMap();
        while (tasks.hasMoreElements()) {
            Task task = (Task) tasks.nextElement();
            if (!task.isDeleted()) continue; // Rescind requires no special handling
            Asset proto = (Asset) task.getDirectObject();
	    Inventory inventory = inventoryPlugIn_.findOrMakeInventory(supplyType_, proto);
            if (inventory == null)  {
		String typeID = proto.getTypeIdentificationPG().getTypeIdentification();
		GLMDebug.ERROR("WithdrawAllocator", clusterId_, "Inventory NOT found for "+typeID);
		continue;
	    }
            System.out.println("Removing task from inventory: " + TaskUtils.taskDesc(task));
            long et = TaskUtils.getEndTime(task);
            et = TimeUtils.pushToEndOfDay(tCalendar_, et);
            Long latest = (Long) tMap.get(inventory);
            if (latest == null || latest.longValue() < et) {
                tMap.put(inventory, new Long(et));
            }
        }
        long inventoryReportCutoffTime = plugin_.currentTimeMillis() - INVENTORY_REPORT_CUTOFF;
        long inventoryReportPruneTime = plugin_.currentTimeMillis() - INVENTORY_REPORT_PRUNE;
        for (Iterator keys = tMap.keySet().iterator(); keys.hasNext(); ) {
            Inventory inventory = (Inventory) keys.next();
            InventoryPG invpg =
                (InventoryPG) inventory.searchForPropertyGroup(InventoryPG.class);
            long et = ((Long) tMap.get(inventory)).longValue();
            Schedule schedule =
                inventory.getScheduledContentPG().getSchedule();
            Iterator iter = schedule.getScheduleElementsWithTime(et).iterator();
            if (iter.hasNext()) { // There should be exactly one element
                QuantityScheduleElement qse = (QuantityScheduleElement) iter.next();
                double q = qse.getQuantity();
                System.out.println("Adding inventory report to "
                                   + inventory
                                   + " at "
                                   + new java.util.Date(et)
                                   + " level "
                                   + q);
                ItemIdentificationPG iipg = inventory.getItemIdentificationPG();
                String iid = iipg.getItemIdentification();
                invpg.addInventoryReport(new InventoryReport(iid, et, et, q));
            } else {
                System.err.println("No scheduled content");
            }
            InventoryReport oldestReport = invpg.getOldestInventoryReport();
            if (oldestReport != null && oldestReport.theReportDate < inventoryReportCutoffTime) {
                System.out.println("Pruning old inventoryReports: " + inventory);
                invpg.pruneOldInventoryReports(inventoryReportPruneTime);
                publishChangeAsset(inventory);
            }
        }
    }
}
