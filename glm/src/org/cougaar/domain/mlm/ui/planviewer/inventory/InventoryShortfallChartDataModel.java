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

import java.util.*;

import org.cougaar.domain.planning.ldm.plan.ScheduleType;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;

import com.klg.jclass.chart.ChartDataEvent;

import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.domain.mlm.ui.data.UISimpleSchedule;

/**
 * Receives inventory objects from the clusters and implements
 * interfaces for JClass displays.
 */

public class InventoryShortfallChartDataModel extends
    InventoryBaseChartDataModel{
  // chart values and labels
  double xvalues[][];
  double yvalues[][];
  String[] seriesLabels;
  boolean valuesSet = false;
  int nSeries;
  InventoryChartDataModel actual;
  InventoryChartDataModel requested;
  InventoryChartDataModel unconfirmed = null;
    int maxSeries = 4;

  public static final String DUE_OUT_SHORTFALL_LABEL = "Demand Shortfall";
  public static final String CONFIRMED_DUE_IN_SHORTFALL_LABEL = "Failed Resupply Shortfall";
  public static final String UNCONFIRMED_DUE_IN_SHORTFALL_LABEL = "Resupply Shortfall Qty";
  public static final String PROJECTED_DUE_OUT_SHORTFALL_LABEL = "Projected Demand Shortfall"; 
  public static final String PROJECTED_DUE_IN_SHORTFALL_LABEL = "Projected Resupply Shortfall"; 

  public InventoryShortfallChartDataModel(InventoryChartDataModel actual,
                                          InventoryChartDataModel requested) {
    this.actual = actual;
    this.requested = requested;
    if (actual.getNumSeries() == 0 && requested.getNumSeries() == 0) {
	maxSeries = 0;
      return;
    }
  }

  public InventoryShortfallChartDataModel(InventoryChartDataModel actual,
                                          InventoryChartDataModel requested,
                                          InventoryChartDataModel unconfirmed ) {
      this(actual,requested);
      this.unconfirmed = unconfirmed;
  }

  /** Set the values for the inventory chart using the inventory and
    schedule types specified in the constructor.
    */

  private void setValues() {
    if (valuesSet)
      return;
    valuesSet = true;

    // there's no actual or requested data
    if (maxSeries == 0) 
      return;

    // compare actual due-in to requested due-in and
    // actual due-out to requested due-out

    int actualDueInScheduleSeries = -1;
    int unconfirmedDueInScheduleSeries = -1;
    int actualDueOutScheduleSeries = -1;
    int requestedDueInScheduleSeries = -1;
    int requestedDueOutScheduleSeries = -1;
    int projectedDueOutScheduleSeries = -1;
    int projectedDueInScheduleSeries = -1;
    int requestedProjectedDueOutScheduleSeries = -1;
    int requestedProjectedDueInScheduleSeries = -1;

    String[] actualLabels = 
      ((LabelledChartDataModel)actual).getSeriesLabels();
    String[] requestedLabels = 
      ((LabelledChartDataModel)requested).getSeriesLabels();

    for (int i = 0; i < actualLabels.length; i++) {

      if (actualLabels[i].equals(InventoryChartDataModel.ACTUAL_DUE_IN_LABEL)) 
        actualDueInScheduleSeries = i;
      else if (actualLabels[i].equals(InventoryChartDataModel.ACTUAL_DUE_OUT_SHIPPED_LABEL) ||
               actualLabels[i].equals(InventoryChartDataModel.ACTUAL_DUE_OUT_CONSUMED_LABEL))
        actualDueOutScheduleSeries = i;
      else if (actualLabels[i].equals(InventoryChartDataModel.ACTUAL_PROJECTED_DUE_OUT_SHIPPED_LABEL) ||
               actualLabels[i].equals(InventoryChartDataModel.ACTUAL_PROJECTED_DUE_OUT_CONSUMED_LABEL))
	  projectedDueOutScheduleSeries = i;
      else if (actualLabels[i].equals(InventoryChartDataModel.ACTUAL_PROJECTED_DUE_IN_LABEL))
	  projectedDueInScheduleSeries = i;
      else
	    System.out.println("InventoryShortfallChartDataModel ERROR actualLabel not recognized <"+
			       actualLabels[i]+">");
    }
    for (int i = 0; i < requestedLabels.length; i++) {
      if (requestedLabels[i].equals(InventoryChartDataModel.REQUESTED_DUE_IN_LABEL))
	requestedDueInScheduleSeries = i;
      else if (requestedLabels[i].equals(InventoryChartDataModel.REQUESTED_DUE_OUT_LABEL))
        requestedDueOutScheduleSeries = i;
      else if (requestedLabels[i].equals(InventoryChartDataModel.REQUESTED_PROJECTED_DUE_OUT_LABEL))
        requestedProjectedDueOutScheduleSeries = i;
      else if (requestedLabels[i].equals(InventoryChartDataModel.REQUESTED_PROJECTED_DUE_IN_LABEL))
        requestedProjectedDueInScheduleSeries = i;
      else
	  System.out.println("InventoryShortfallChartDataModel ERROR requestedLabel not recognized <"+
			     requestedLabels[i]+">");
    }

    
    if (unconfirmed != null) {
	String[] unconfirmedLabels = 
	    ((LabelledChartDataModel)unconfirmed).getSeriesLabels();
	
	for (int i = 0; i < unconfirmedLabels.length; i++) {
	    if (unconfirmedLabels[i].equals(InventoryChartDataModel.UNCONFIRMED_DUE_IN_LABEL)) {
		unconfirmedDueInScheduleSeries = i;
		nSeries = 3;
	    } else 
		System.out.println("InventoryShortfallChartDataModel ERROR unconfirmedlLabel not recognized <"+
				   unconfirmedLabels[i]+">");
	}
    }


    xvalues = new double[maxSeries][];
    yvalues = new double[maxSeries][];
    seriesLabels = new String[maxSeries];

    nSeries = 0;



    double[][] restock_values = calculateShortfallValues(actualDueInScheduleSeries, 
					requestedDueInScheduleSeries);
    if ((restock_values[0] != null) && (restock_values[0].length > 0)) {
	if (unconfirmedDueInScheduleSeries == -1) {
	    // no unconfirmed - all shortfall due to known failure
	    seriesLabels[nSeries] = CONFIRMED_DUE_IN_SHORTFALL_LABEL;
	} else {
	    // some shortfall due to no allocation result
	    seriesLabels[nSeries] = UNCONFIRMED_DUE_IN_SHORTFALL_LABEL;
	}
	xvalues[nSeries] = restock_values[0];
	yvalues[nSeries] = restock_values[1];
	nSeries++;
    }

    double[][] failed_restock_values = calculateFailedRestockValues(unconfirmedDueInScheduleSeries,
				  actualDueInScheduleSeries,
				  requestedDueInScheduleSeries);
    if ((failed_restock_values[0] != null) && (failed_restock_values[0].length > 0)) {
	seriesLabels[nSeries] = CONFIRMED_DUE_IN_SHORTFALL_LABEL;
	xvalues[nSeries] = failed_restock_values[0];
	yvalues[nSeries] = failed_restock_values[1];
	nSeries++;
    }

    double[][] req_values = calculateShortfallValues(actualDueOutScheduleSeries, 
						    requestedDueOutScheduleSeries);
    if ((req_values[0] != null) && (req_values[0].length > 0)) {
	seriesLabels[nSeries] = DUE_OUT_SHORTFALL_LABEL;
	xvalues[nSeries] = req_values[0];
	yvalues[nSeries] = req_values[1];
	nSeries++;
    }

    double[][] proj_req_out_values = calculateShortfallValues(projectedDueOutScheduleSeries, 
							  requestedProjectedDueOutScheduleSeries);
    if ((proj_req_out_values[0] != null) && (proj_req_out_values[0].length > 0)) {
	seriesLabels[nSeries] = PROJECTED_DUE_OUT_SHORTFALL_LABEL;
	xvalues[nSeries] = proj_req_out_values[0];
	yvalues[nSeries] = proj_req_out_values[1];
	nSeries++;
    }

    double[][] proj_req_in_values = calculateShortfallValues(projectedDueInScheduleSeries, 
							     requestedProjectedDueInScheduleSeries);
    if ((proj_req_in_values[0] != null) && (proj_req_in_values[0].length > 0)) {
	seriesLabels[nSeries] = PROJECTED_DUE_IN_SHORTFALL_LABEL;
	xvalues[nSeries] = proj_req_in_values[0];
	yvalues[nSeries] = proj_req_in_values[1];
	nSeries++;
    }


  }

  /** both the actual and requested series exist, shortfall=requests-actuals
    the actual doesn't exist, shortfall = requests
    the requests don't exist, shortfall = 0
    */
  private double[][] calculateShortfallValues(int actualSeries, int requestedSeries) {
      double[][] xyvalues = new double[2][];
      boolean isShortfall = false;

      if (actualSeries == -1) {
	  if (requestedSeries == -1) return xyvalues;
	  // no actuals so set shortfall to a sum of the requests over time
	  System.out.println("InventoryShortfallChartDataModel has no actual data");
	  isShortfall = true;
	  xyvalues[0] = requested.getXSeries(requestedSeries);
	  int length = xyvalues[0].length;
	  xyvalues[1] = new double[length];
	  double[] requests = requested.getYSeries(requestedSeries);
	  xyvalues[1][0] = requests[0];
	  for (int j = 1; j < requests.length; j++)
	      xyvalues[1][j] = requests[j] + xyvalues[1][j-1];
      } else if (requestedSeries == -1) {
	  // set shortfall to 0 because there are no requests
	  xyvalues[0] = actual.getXSeries(actualSeries);
	  int length = xyvalues[0].length;
	  xyvalues[1] = new double[length];
	  double[] actuals = actual.getYSeries(actualSeries);
	  for (int j = 0; j < actuals.length; j++)
	      xyvalues[1][j] = 0;
      } else {
	  double[] actuals = actual.getYSeries(actualSeries);
	  double[] requests = requested.getYSeries(requestedSeries);
	  xyvalues[0] = actual.getXSeries(actualSeries);
	  xyvalues[1] = new double[actuals.length];
	  xyvalues[1][0] = requests[0] - actuals[0];
	  for (int j = 1; j < actuals.length; j++)
	      xyvalues[1][j] = 
		  requests[j] - actuals[j] + xyvalues[1][j-1];
	  // now fix the values, so none are negative
	  for (int j = 0; j < actuals.length; j++) {
	      if (xyvalues[1][j] < 0)
		  xyvalues[1][j] = 0;
	      else if (xyvalues[1][j] > 0) {
// 		  System.out.println("InventoryShortfallChartDataModel "+
// 				     " is shortfall at "+xyvalues[0][j]+", "+xyvalues[1][j]);
		  isShortfall = true;
	      }
	  }
      }
      if (isShortfall) return xyvalues;
      return new double[2][];
  }

  /** both the actual and requested series exist, shortfall=requests-actuals-unconfirmeds
    the actual doesn't exist, shortfall = requests - unconfirmeds
    */
  private double[][] calculateFailedRestockValues(int unconfirmedSeries, int actualSeries, int requestedSeries) {
      double[][] xyvalues = new double[2][];
      boolean isShortfall = false;
      if (unconfirmedSeries == -1) return xyvalues;
      if (unconfirmed == null) return xyvalues;

      if (requestedSeries == -1) return xyvalues;

      double[] requests = requested.getYSeries(requestedSeries);
      double[] unconfirmeds = unconfirmed.getYSeries(unconfirmedSeries);
      xyvalues[0] = unconfirmed.getXSeries(unconfirmedSeries);
      xyvalues[1] = new double[unconfirmeds.length];
      
      if (actualSeries == -1) {
	  xyvalues[1][0] = requests[0] - unconfirmeds[0];
	  for (int j = 1; j < unconfirmeds.length; j++)
	      xyvalues[1][j] = 
		  requests[j] - unconfirmeds[j] + xyvalues[1][j-1];
      } else {
	  double[] actuals = actual.getYSeries(actualSeries);
	  xyvalues[1][0] = requests[0] - actuals[0] - unconfirmeds[0];
	  for (int j = 1; j < actuals.length; j++)
	      xyvalues[1][j] = 
		  requests[j] - actuals[j] - unconfirmeds[j] + xyvalues[1][j-1];
      }
      // now fix the values, so none are negative
      for (int j = 0; j < unconfirmeds.length; j++) {
	  if (xyvalues[1][j] < 0)
	      xyvalues[1][j] = 0;
	  else if (xyvalues[1][j] > 0) {
// 	      System.out.println("InventoryShortfallChartDataModel "+
// 				 " is failed restock at "+xyvalues[0][j]+", "+xyvalues[1][j]);
	      isShortfall = true;
	  }
      }
      if (isShortfall) return xyvalues;
      return new double[2][];
  }

  /**
   * Retrieves the specified x-value series
   * Start and end dates of the schedule for each asset
   * @param index data series index
   * @return array of double values representing x-value data
   */
  public double[] getXSeries(int index) {
    setValues();
      //    return actual.getXSeries(index);
    return xvalues[index];
  }

  /**
   * Retrieves the specified y-value series
   * The nth asset
   * @param index data series index
   * @return array of double values representing y-value data
   */
  public double[] getYSeries(int index) {
    setValues();
    return yvalues[index];
  }

  /**
   * Retrieves the number of data series.
   */
  public int getNumSeries() {
    setValues();
    return nSeries;
  }

  /** Legend title.
   */

  public String getDataSourceName() {
    setValues();
    return InventoryChart.SHORTFALL_LEGEND;
  }

  public String[] getPointLabels() {
    setValues();
    return null;
  }

  public String[] getSeriesLabels() {
    setValues();
    return seriesLabels;
  }

  public void setDisplayCDays(boolean useCDays) {
      /***
      actual.setDisplayCDays(useCDays);
      requested.setDisplayCDays(useCDays);
      if(unconfirmed != null) {
	  unconfirmed.setDisplayCDays(useCDays);
      }
      */
      valuesSet=false;
      setValues();
      fireChartDataEvent(ChartDataEvent.RELOAD,
			 0,0);
  }

    public void resetInventory(UISimpleInventory inventory) {
	valuesSet=false;
	setValues();
	fireChartDataEvent(ChartDataEvent.RELOAD,
			   0,0);
    }

}

