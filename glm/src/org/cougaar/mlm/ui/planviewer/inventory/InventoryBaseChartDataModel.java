/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.mlm.ui.planviewer.inventory;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
import com.klg.jclass.chart.ChartDataManager;

import org.cougaar.mlm.ui.data.UISimpleInventory;

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

