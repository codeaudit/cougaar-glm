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

package org.cougaar.glm.plugins.inventory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.execution.common.InventoryReport;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.InventoryPG;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.UnaryPredicate;

public class DeletionProcessor extends InventoryProcessor {
    protected IncrementalSubscription tasks_;
    private Calendar tCalendar_ = Calendar.getInstance();

    /** Delete old reports when oldest becomes this old **/
    private static final long INVENTORY_REPORT_CUTOFF = 8 * TimeUtils.MSEC_PER_WEEK;

    /** When pruning old reports, remove all older than this age. O
        disables pruning. This should match the inventoryBG refill
        task anticipation interval (current 14 days) to provide detail
        during that interval. Mismatch is not harmful, but leads
        excess inventory reports or missing detail.
     **/
    private static final long INVENTORY_REPORT_PRUNE = 4 * TimeUtils.MSEC_PER_WEEK;

    private static final boolean DEBUG = true;

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
        removeTasks(tasks_.getRemovedList());
    }

    /**
     * Keep track of the range of times for which tasks have been
     * deleted from an inventory. Inventory reports are created to
     * prop up the inventory over that time range.
     **/
    private static class DeletionTimeRange {
        long earliestTime;
        long latestTime;
        DeletionTimeRange(long t) {
            earliestTime = latestTime = t;
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
     * Inventory reports may be pruned. When enabled and the oldest
     * report is older than INVENTORY_REPORT_PRUNE the inventories
     * older than INVENTORY_REPORT_CUTOFF are removed.
     **/
    private void removeTasks(Enumeration tasks) {
        Map tMap = new HashMap();
        while (tasks.hasMoreElements()) {
            Task task = (Task) tasks.nextElement();
            if (!task.isDeleted()) { // Rescind requires no special handling
                continue;
            }
            Asset proto = (Asset) task.getDirectObject();
	    Inventory inventory = inventoryPlugIn_.findOrMakeInventory(supplyType_, proto);
            if (inventory == null)  {
		String typeID = proto.getTypeIdentificationPG().getTypeIdentification();
		printError("Inventory NOT found for "+typeID);
		continue;
	    }
            if (DEBUG) printDebug(1000, "Removing task from inventory: " + TaskUtils.taskDesc(task));
            long et = TaskUtils.getEndTime(task);
            et = TimeUtils.pushToEndOfDay(tCalendar_, et);
            DeletionTimeRange dtr = (DeletionTimeRange) tMap.get(inventory);
            if (dtr == null) {
                dtr = new DeletionTimeRange(et);
                tMap.put(inventory, dtr);
            } else {
                dtr.earliestTime = Math.min(dtr.earliestTime, et);
                dtr.latestTime = Math.max(dtr.latestTime, et);
            }
        }
        long inventoryReportCutoffTime = plugin_.currentTimeMillis() - INVENTORY_REPORT_CUTOFF;
        long inventoryReportPruneTime = plugin_.currentTimeMillis() - INVENTORY_REPORT_PRUNE;
        for (Iterator keys = tMap.keySet().iterator(); keys.hasNext(); ) {
            boolean needPublishChange = false;
            Inventory inventory = (Inventory) keys.next();
            InventoryPG invpg =
                (InventoryPG) inventory.getInventoryPG();
            DeletionTimeRange dtr = (DeletionTimeRange) tMap.get(inventory);
            Schedule schedule =
                inventory.getScheduledContentPG().getSchedule();
            ItemIdentificationPG iipg = inventory.getItemIdentificationPG();
            String iid = iipg.getItemIdentification();
            long et;
            if (true) {
                et = dtr.earliestTime;
            } else {
                et = dtr.latestTime;
            }
            for (; et <= dtr.latestTime; et += TimeUtils.MSEC_PER_DAY) {
                Iterator iter = schedule.getScheduleElementsWithTime(et).iterator();
                if (iter.hasNext()) { // There should be exactly one element
                    QuantityScheduleElement qse = (QuantityScheduleElement) iter.next();
                    double q = qse.getQuantity();
                    if (DEBUG) printDebug(1000,
                               "Adding inventory report to "
                               + inventory
                               + " at "
                               + new java.util.Date(et)
                               + " level "
                               + q);
                    invpg.addInventoryReport(new InventoryReport(iid, et, et, q));
                    needPublishChange = true;
                } else {
                    printError("No scheduled content");
                }
            }
            if (INVENTORY_REPORT_PRUNE > 0L) {
                InventoryReport oldestReport = invpg.getOldestInventoryReport();
                if (oldestReport != null && oldestReport.theReportDate < inventoryReportCutoffTime) {
                    if (DEBUG) printDebug(1000, "Pruning old inventoryReports: " + inventory);
                    invpg.pruneOldInventoryReports(inventoryReportPruneTime);
                    needPublishChange = true;
                }
                if (needPublishChange) {
                    publishChangeAsset(inventory);
                }
            }
        }
    }
}
