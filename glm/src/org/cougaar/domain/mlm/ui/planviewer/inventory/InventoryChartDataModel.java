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
 

package org.cougaar.domain.mlm.ui.planviewer.inventory;

import java.util.*;

import org.cougaar.domain.planning.ldm.plan.ScheduleType;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
import com.klg.jclass.chart.ChartDataManager;

import org.cougaar.domain.mlm.ui.data.*;

/**
 * Receives inventory objects from the clusters and implements
 * interfaces for JClass displays.
 */


public class InventoryChartDataModel extends InventoryBaseChartDataModel{

    final static String UNCONFIRMED_DUE_IN_LABEL="Qty May Receive";
    final static String ACTUAL_DUE_IN_LABEL="Qty Received";
    final static String ACTUAL_DUE_OUT_SHIPPED_LABEL="Qty Shipped";
    final static String ACTUAL_DUE_OUT_CONSUMED_LABEL="Qty Consumed";
    final static String ACTUAL_PROJECTED_DUE_OUT_SHIPPED_LABEL=
	"Projected Qty Shipped";
    final static String ACTUAL_PROJECTED_DUE_OUT_CONSUMED_LABEL=
	"Projected Qty Consumed";
    final static String REQUESTED_DUE_IN_LABEL="Restock Qty";
    final static String REQUESTED_DUE_OUT_LABEL="Requisition Qty";
    final static String REQUESTED_PROJECTED_DUE_OUT_LABEL="Projected Req Qty";

    final static String ACTUAL_PROJECTED_DUE_IN_LABEL="Projected Usage Qty";
    final static String REQUESTED_PROJECTED_DUE_IN_LABEL="Projected Req Usage Qty";


  boolean isInventory; // true if isInventory, false if capacity
  String assetName;
  Vector schedules;
  boolean provider;
  // chart values and labels
  double xvalues[][];
  double yvalues[][];
  String[] seriesLabels;
  String[] scheduleNames;
  boolean valuesSet = false;
  int nSeries;
  final static long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  String legendTitle = "";
  static final int LABOR_HOURS_PER_DAY = 8;
  static final int LABOR_HOURS_ADDL = 2;
  double ammoFactor = 1.0;
  Vector referenceOnHandSchedule;

  protected InventoryChartDataModel inactiveMe=null;

  long baseCDayTime;
  boolean useCDay;

  double zeroYVals[];
  HashSet offSeries=new HashSet(5);

  /** Argument is the vector of UISimpleNamedSchedules from which
    to provide data.
    */

  public InventoryChartDataModel(boolean inventory, 
				 String assetName,
				 String unitType,
				 Vector schedules,
				 Vector referenceOnHandSchedule,
				 String legendTitle,
				 boolean provider,
				 long   cDayTime,
				 boolean doUseCDay) {
    this.isInventory = inventory;
    this.assetName = assetName;
    this.schedules = schedules;
    this.referenceOnHandSchedule = referenceOnHandSchedule;
    this.legendTitle = legendTitle;
    this.provider = provider;
    this.baseCDayTime = cDayTime;
    this.useCDay = doUseCDay;
    nSeries = schedules.size();

//     AssetWeightTable weightTable = new AssetWeightTable();
//     if (unitType.equals("STons"))
//       ammoFactor = 2000/weightTable.get(assetName);
//     else
//       ammoFactor = 1.0;
    //    System.out.println("Ammo factor for: " + assetName + " is: " + ammoFactor);
  }

  /** For debugging, print all schedules.
   */

  protected void printSchedules() {
    System.out.println("RESULTS=========================================");
    System.out.println("Asset name: " + assetName);
    for (int i = 0; i < schedules.size(); i++) {
      UISimpleNamedSchedule namedSchedule = 
	(UISimpleNamedSchedule)schedules.elementAt(i);
      System.out.println(namedSchedule.getName());
      Vector schedule = namedSchedule.getSchedule();
      if (schedule == null)
	return;
      for (int j = 0; j < schedule.size(); j++) {
	UIQuantityScheduleElement s = (UIQuantityScheduleElement)schedule.elementAt(j);
	System.out.println("Quantity: " + s.getQuantity());
	System.out.println("Start time: " + s.getStartTime());
	System.out.println("End time: " + s.getEndTime());
      }
    }
    System.out.println("================================================");
  }

  /** Set inventory values; sets value for each day from start day to end day.
   */

  private void setOnHandValues(Vector onHandSchedule) {
    int minDay = -1;
    int maxDay = 0;

    // time base is set in InventoryChartBaseCalendar()
    // so all days have to be relative to that
    long baseTime = (useCDay) ? baseCDayTime : InventoryChartBaseCalendar.getBaseTime();
    seriesLabels = new String[nSeries];
    scheduleNames = new String[nSeries];
    seriesLabels[0] = getSeriesLabel(UISimpleNamedSchedule.ON_HAND);
    scheduleNames[0] = UISimpleNamedSchedule.ON_HAND;
    for (int i = 0; i < onHandSchedule.size(); i++) {
      UIQuantityScheduleElement s = (UIQuantityScheduleElement)onHandSchedule.elementAt(i);
      long startTime = s.getStartTime();
      long endTime = s.getEndTime();
      int startDay = (int)((startTime - baseTime) / MILLIS_IN_DAY);
      int endDay = (int)((endTime - baseTime) / MILLIS_IN_DAY);
      if (minDay == -1)
	minDay = startDay;
      else if (startDay < minDay)
	minDay = startDay;
      maxDay = Math.max(endDay, maxDay);
    }
    int nValues = maxDay - minDay + 1;
    xvalues = new double[nSeries][nValues];
    yvalues = new double[nSeries][nValues];
    initZeroYVal(nValues);

    for (int i = 0; i < nSeries; i++) {
      for (int j = 0; j < nValues; j++) {
	xvalues[i][j] = minDay + j;
	yvalues[i][j] = 0;
      }
    }
    for (int i = 0; i < onHandSchedule.size(); i++) {
      UIQuantityScheduleElement s = (UIQuantityScheduleElement)onHandSchedule.elementAt(i);
      long startTime = s.getStartTime();
      long endTime = s.getEndTime();
      int startDay = (int)((startTime - baseTime) / MILLIS_IN_DAY);
      int endDay = (int)((endTime - baseTime) / MILLIS_IN_DAY);
      double quantity = s.getQuantity() * ammoFactor;
      for (int j = startDay; j <= endDay; j++)
	yvalues[0][j-minDay] = quantity;
    }
  }

  /** Set inventory values (on-hand, due-in, due-out, 
    requested due-in, requested due-out).  We use start and end times for
    on-hand data, but only start times for the others.
    This is somewhat complicated by the fact that the graphs only appear
    correctly if the same minimum day (and number of values?) are used
    in all the series in the simultaneously displayed graphs.  Thus,
    the referenceOnHandSchedule is used to determine the min time and number
    of values for all the inventory values (including requested and
    actual due-ins and due-outs).
    */

  protected void setInventoryValues() {
    int minDay = -1;
    int maxDay = 0;

    // set on-hand (inventory) schedule
    for (int i = 0; i  < schedules.size(); i++) {
      UISimpleNamedSchedule namedSchedule =
	(UISimpleNamedSchedule)schedules.elementAt(i);
      if (namedSchedule.getName().equals(UISimpleNamedSchedule.ON_HAND)) {
	setOnHandValues(namedSchedule.getSchedule());
	return;
      }
    }

    // time base is set in InventoryChartBaseCalendar(below)
    // so all days have to be relative to that
    long baseTime = (useCDay) ? baseCDayTime : InventoryChartBaseCalendar.getBaseTime();
    //System.out.println("InventoryChartDataModel::UseCDay:" + useCDay + " Base Date is: " + new Date(baseTime)); 
    seriesLabels = new String[nSeries];
    scheduleNames = new String[nSeries];

    if (referenceOnHandSchedule != null) {
      for (int i = 0; i < referenceOnHandSchedule.size(); i++) {
	UIQuantityScheduleElement s = (UIQuantityScheduleElement)referenceOnHandSchedule.elementAt(i);
	long startTime = s.getStartTime();
	long endTime = s.getEndTime();
	int startDay = (int)((startTime - baseTime) / MILLIS_IN_DAY);
	int endDay = (int)((endTime - baseTime) / MILLIS_IN_DAY);
	if (minDay == -1)
	  minDay = startDay;
	else if (startDay < minDay)
	  minDay = startDay;
	maxDay = Math.max(endDay, maxDay);
      }
      int nValues = maxDay - minDay + 1;
      xvalues = new double[nSeries][nValues];
      yvalues = new double[nSeries][nValues];
      for (int i = 0; i < nSeries; i++) {
	for (int j = 0; j < nValues; j++) {
	  xvalues[i][j] = minDay + j;
	  yvalues[i][j] = 0;
	}
      }
    } else {
      xvalues = new double[nSeries][];
      yvalues = new double[nSeries][];
    }

    for (int i = 0; i < nSeries; i++) {
      UISimpleNamedSchedule namedSchedule = 
	(UISimpleNamedSchedule)schedules.elementAt(i);
      seriesLabels[i] = getSeriesLabel(namedSchedule.getName());
      scheduleNames[i] = namedSchedule.getName();
      Vector schedule = computeDailyEndTimeBucketSchedule(namedSchedule.getSchedule());
      if (referenceOnHandSchedule == null) {
	xvalues[i] = new double[schedule.size()];
	yvalues[i] = new double[schedule.size()];
      }

      initZeroYVal(yvalues[0].length);

      // use END DAY!!
     for (int j = 0; j < schedule.size(); j++) {
       UIQuantityScheduleElement s = (UIQuantityScheduleElement)schedule.elementAt(j);
       long endTime = s.getEndTime();
       int endDay = (int)((endTime - baseTime) / MILLIS_IN_DAY);
       if (referenceOnHandSchedule == null) {
	 xvalues[i][j] = endDay;
	 yvalues[i][j] = s.getQuantity() * ammoFactor;
	 /***
	 System.out.println("InventoryChartDataModel::setInventoryValues: J is " + j +
			    " Qty is " + s.getQuantity() + " ammoFactor is " + ammoFactor +
			    "result is " + yvalues[i][j]);
	 */		    
			    
			  
       } else {
	 if (endDay < minDay || endDay > maxDay)
	   System.out.println("WARNING: ignoring due-in or due-out schedule time that is before or after on-hand schedule times, day: " + endDay);
	 else
	   yvalues[i][endDay - minDay] = s.getQuantity() * ammoFactor;
       }
     }
//       for (int j = 0; j < schedule.size(); j++) {
// 	UIQuantityScheduleElement s = (UIQuantityScheduleElement)schedule.elementAt(j);
// 	long startTime = s.getStartTime();
// 	int startDay = (int)((startTime - baseTime) / MILLIS_IN_DAY);
// 	if (referenceOnHandSchedule == null) {
// 	  xvalues[i][j] = startDay;
// 	  yvalues[i][j] = s.getQuantity() * ammoFactor;
// 	} else {
// 	  if (startDay < minDay || startDay > maxDay)
// 	    System.out.println("WARNING: ignoring due-in or due-out schedule time that is before or after on-hand schedule times, day: " + startDay);
// 	  else
// 	    yvalues[i][startDay - minDay] = s.getQuantity() * ammoFactor;
// 	}
//       }
    }
    //for debugging
    //    for (int k = 0; k < nSeries; k++) {
    //      double[] tmpX = getXSeries(k);
    //      double[] tmpY = getYSeries(k);
    //      for (int j = 0; j < tmpX.length; j++)
    //	System.out.println("Series " + k + " value: " +
    //			   tmpX[j] + " " + tmpY[j]);
    //    }
    // end for debugging
  }

    //Sums all the schedule elements on the same day into one bucket
    //for display of inventory charts that display data on the same day
    //MWD This is NOT currently called in computing capacity (or 
    // labor)  - should it?
    public Vector computeDailyEndTimeBucketSchedule(Vector schedule) {
	return computeGranularEndTimeBucketSchedule(schedule, MILLIS_IN_DAY);
    }

    //Sums all the schedule elements that fall into one bucket
    //for the given number of millisecond size buckets.
    public Vector computeGranularEndTimeBucketSchedule(Vector schedule, long numMillisecsGranularity) {
	Vector bucketVector = new Vector(schedule.size());
	long baseTime = (useCDay) ? baseCDayTime : InventoryChartBaseCalendar.getBaseTime();
	UIQuantityScheduleElement currElement=null;
	long currEndPoint=-1;

	for(int i=0; i<schedule.size(); i++) {
	    UIQuantityScheduleElement s = (UIQuantityScheduleElement)schedule.elementAt(i);
	    long endTime = s.getEndTime();
	    long endPoint = (int)((endTime - baseTime) / numMillisecsGranularity);
	    if(currElement == null) {
		currElement = (UIQuantityScheduleElement)s.clone();
	        currEndPoint = endPoint;
	    }
	    else {
		if(endPoint != currEndPoint) {
		    bucketVector.add(currElement);
		    currElement = (UIQuantityScheduleElement)s.clone();
		    currEndPoint=endPoint;
		}
		else {
		    //   System.out.println("InventoryChartDataModel::daily bucket Same Day(" + endPoint + ")!!");
		    currElement.setQuantity((currElement.getQuantity() + s.getQuantity()));
		}
	    }
	}

	bucketVector.add(currElement);
	
	// System.out.println("InventoryChartDataModel::daily bucket orig schedule:|" + schedule.size() + "| new one:|" + bucketVector.size());

	return bucketVector;
    }


  /** Set the values for the inventory chart using the inventory and
    schedule types specified in the constructor.
    */

  protected void setValues() {
    if (valuesSet)
      return;
    valuesSet = true;

    //printSchedules();

    if (isInventory)
      setInventoryValues();
    else
      setCapacityValues();
  }

  private void setCapacityValues() {
    System.out.println("InventoryChartDataModel setCapacityValues()");
    // remove labor schedule from schedules to plot and treat it specially
    Vector laborSchedule = null;
    for (int i = 0; i < schedules.size(); i++) {
      UISimpleNamedSchedule namedSchedule = 
	(UISimpleNamedSchedule)schedules.elementAt(i);
      if (namedSchedule.getName().equals(UISimpleNamedSchedule.TOTAL_LABOR)) {
	laborSchedule = namedSchedule.getSchedule();
	schedules.removeElementAt(i); 
	nSeries--;
	break;
      }
    }

    int nLaborSchedules = 0;

    // if there's a labor schedule, then plot it as 3 schedules
    // showing 8, 10 and 12 hour schedules
    if (laborSchedule != null) {
      nSeries = nSeries + 3;
      nLaborSchedules = 3;
    }

    seriesLabels = new String[nSeries];
    scheduleNames = new String[nSeries];
    xvalues = new double[nSeries][];
    yvalues = new double[nSeries][];
    long baseTime = (useCDay) ? baseCDayTime : InventoryChartBaseCalendar.getBaseTime();

    if (laborSchedule != null) {
      seriesLabels[0] = UISimpleNamedSchedule.TOTAL_LABOR_12;
      seriesLabels[1] = UISimpleNamedSchedule.TOTAL_LABOR_10;
      seriesLabels[2] = UISimpleNamedSchedule.TOTAL_LABOR_8;
      scheduleNames[0] = UISimpleNamedSchedule.TOTAL_LABOR_12;
      scheduleNames[1] = UISimpleNamedSchedule.TOTAL_LABOR_10;
      scheduleNames[2] = UISimpleNamedSchedule.TOTAL_LABOR_8;
      for (int i = 0; i < nLaborSchedules; i++) {
	xvalues[i] = new double[laborSchedule.size()*2];
	yvalues[i] = new double[laborSchedule.size()*2];
	for (int j = 0; j < laborSchedule.size(); j++) {
	  UIQuantityScheduleElement s = (UIQuantityScheduleElement)laborSchedule.elementAt(j);
	  long startTime = s.getStartTime();
	  long endTime = s.getEndTime();
	  int nDays = (int)((endTime - startTime) / MILLIS_IN_DAY);
	  int startDay = (int)((startTime - baseTime) / MILLIS_IN_DAY);
	  int endDay = startDay + nDays;
	  xvalues[i][j*2] = startDay;
	  yvalues[i][j*2] = s.getQuantity() * (LABOR_HOURS_PER_DAY +
	    (nLaborSchedules - i -1) * LABOR_HOURS_ADDL);
	  xvalues[i][j*2+1] = endDay;
	  yvalues[i][j*2+1] = s.getQuantity() * (LABOR_HOURS_PER_DAY +
	    (nLaborSchedules - i - 1) * LABOR_HOURS_ADDL);
	}
      }
    }

    int nSchedules = schedules.size();
    for (int i = 0; i < nSchedules; i++) {
      UISimpleNamedSchedule namedSchedule =
	(UISimpleNamedSchedule)schedules.elementAt(i);
      seriesLabels[i+nLaborSchedules] = 
	getSeriesLabel(namedSchedule.getName());
      scheduleNames[i+nLaborSchedules] = namedSchedule.getName();
      Vector schedule = namedSchedule.getSchedule();
      xvalues[i+nLaborSchedules] = new double[schedule.size()*2];
      yvalues[i+nLaborSchedules] = new double[schedule.size()*2];
      for (int j = 0; j < schedule.size(); j++) {
	UIQuantityScheduleElement s = (UIQuantityScheduleElement)schedule.elementAt(j);
	long startTime = s.getStartTime();
	long endTime = s.getEndTime();
	int nDays = (int)((endTime - startTime) / MILLIS_IN_DAY);
	int startDay = (int)((startTime - baseTime) / MILLIS_IN_DAY);
	int endDay = startDay + nDays;
	xvalues[i+nLaborSchedules][j*2] = startDay;
	yvalues[i+nLaborSchedules][j*2] = s.getQuantity() * ammoFactor;
	xvalues[i+nLaborSchedules][j*2+1] = endDay;
	yvalues[i+nLaborSchedules][j*2+1] = s.getQuantity() * ammoFactor;
      }
    }

    // for debugging
    //    for (int k = 0; k < nSeries; k++) {
    //      double[] tmpX = getXSeries(k);
    //      double[] tmpY = getYSeries(k);
    //      for (int j = 0; j < tmpX.length; j++)
    //	System.out.println("Series " + k + " value: " +
    //			   tmpX[j] + " " + tmpY[j]);
    //    }
    // end for debugging
  }


  /**
   * Retrieves the specified x-value series
   * Start and end times of the schedule for each asset
   * @param index data series index
   * @return array of double values representing x-value data
   */
  public double[] getXSeries(int index) {
    setValues();
    // for debugging
    //    for (int k = 0; k < xvalues[index].length; k++)
    //      System.out.println("Series " + index + " x-value: " +
    //			 xvalues[index][k]);
    // end for debugging
    if (xvalues == null) {
	System.out.println("InventoryChartDataModel ERROR getXSeries no xvalues?");
    }
    return xvalues[index];
  }

  /**
   * Retrieves the specified y-value series
   * The nth asset
   * @param index data series index
   * @return array of double values representing y-value data
   */
  public synchronized double[] getYSeries(int index) {
    setValues();
    // for debugging
    //    for (int k = 0; k < yvalues[index].length; k++)
    //      System.out.println("Series " + index + " y-value: " +
    //			 yvalues[index][k]);
    // end for debugging
    if(offSeries.contains(new Integer(index))) {
	return zeroYVals;
    }
    else {
	return yvalues[index];
    }
  }

   
  public synchronized void setSeriesVisible(int index, boolean visible) {
      if(visible)
	  offSeries.remove(new Integer(index));
      else
	  offSeries.add(new Integer(index));

      fireChartDataEvent(ChartDataEvent.RELOAD_SERIES,
			 index,0);
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
    return legendTitle;
  }

  public String[] getPointLabels() {
    setValues();
    return null;
  }

  public String[] getSeriesLabels() {
    setValues();
    return seriesLabels;
  }

  /** Derive series label from schedule name.
   */

  protected String getSeriesLabel(String scheduleName) {
    if (scheduleName.equals(UISimpleNamedSchedule.UNCONFIRMED_DUE_IN))
      return UNCONFIRMED_DUE_IN_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.DUE_IN))
      return ACTUAL_DUE_IN_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.DUE_OUT)) {
      if (provider)
	return ACTUAL_DUE_OUT_SHIPPED_LABEL;
      else
	return ACTUAL_DUE_OUT_CONSUMED_LABEL;
    }
    else if (scheduleName.equals(UISimpleNamedSchedule.PROJECTED_DUE_OUT)) {
      if (provider)
	return ACTUAL_PROJECTED_DUE_OUT_SHIPPED_LABEL;
      else
	return ACTUAL_PROJECTED_DUE_OUT_CONSUMED_LABEL;
    }
    else if (scheduleName.equals(UISimpleNamedSchedule.REQUESTED_DUE_IN))
      return REQUESTED_DUE_IN_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.REQUESTED_DUE_OUT))
      return REQUESTED_DUE_OUT_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_OUT))
      return REQUESTED_PROJECTED_DUE_OUT_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.PROJECTED_DUE_IN))
      return ACTUAL_PROJECTED_DUE_IN_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_IN))
      return REQUESTED_PROJECTED_DUE_IN_LABEL;
    else if (scheduleName.equals(UISimpleNamedSchedule.ON_HAND_DETAILED))
      return getSeriesLabel(UISimpleNamedSchedule.ON_HAND);
   
    return scheduleName;
  }

  public String[] getScheduleNames() {
    setValues();
    return scheduleNames;
  }

  public void setDisplayCDays(boolean useCDays) {
      useCDay = useCDays;
      valuesSet=false;
      setValues();
      fireChartDataEvent(ChartDataEvent.RELOAD,
			 0,0);
  }

  public void resetInventory(UISimpleInventory inventory) {
      Vector scheduleNames = InventoryChart.getScheduleTypesForLegend(legendTitle,false);
      
      schedules = 
	  InventoryChart.extractSchedulesFromInventory(scheduleNames,
						       inventory);
      referenceOnHandSchedule=
	  InventoryChart.extractOnHandSchedule(inventory);

      valuesSet=false;
      setValues();
      fireChartDataEvent(ChartDataEvent.RELOAD,
			 0,0);
  }

  protected void initZeroYVal(int len) {
      zeroYVals = new double[len];
      for(int i=0; i<len; i++) {
	  zeroYVals[i] = (double)0D;
      }
  }

    public InventoryChartDataModel getAssociatedInactiveModel() {
	return inactiveMe;
    }

    public void setAssociatedInactiveModel(InventoryChartDataModel assocModel){
	inactiveMe = assocModel;
    }
}

