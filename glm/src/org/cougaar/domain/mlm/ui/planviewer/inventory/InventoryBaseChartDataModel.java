/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
import com.klg.jclass.chart.ChartDataManager;

import org.cougaar.domain.mlm.ui.data.UISimpleInventory;

/**
 * Receives inventory objects from the clusters and implements
 * interfaces for JClass displays.
 */


public abstract class InventoryBaseChartDataModel extends ChartDataSupport
  implements ChartDataManageable,
             ChartDataModel,
	     LabelledChartDataModel {

  public abstract void setDisplayCDays(boolean useCDays);
  public abstract void resetInventory(UISimpleInventory inventory);

  public ChartDataManager getChartDataManager() { 
      return (ChartDataManager)this; 
  }
	
  public abstract int getNumSeries();
  public abstract double[] getXSeries(int index);
  public abstract double[] getYSeries(int index);

  public abstract String getDataSourceName();
  public abstract String[] getPointLabels();
  public abstract String[] getSeriesLabels();


}

