/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.ui.planviewer.inventory;

/**
 * Handles day rollever events by the InventoryExecutionTimeStatusHandler.
 *
 **/
public interface InventoryDayRolloverListener {
 
    //public static final String PSP_id = "EXECUTION_WATCHER.PSP";

    public void dayRolloverUpdate(long date, double rate);
}
