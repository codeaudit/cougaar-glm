package org.cougaar.domain.mlm.ui.planviewer.inventory;

/**
 * Handles day rollever events by the InventoryExecutionTimeStatusHandler.
 *
 **/
public interface InventoryDayRolloverListener {
 
    //public static final String PSP_id = "EXECUTION_WATCHER.PSP";

    public void dayRolloverUpdate(long date, double rate);
}
