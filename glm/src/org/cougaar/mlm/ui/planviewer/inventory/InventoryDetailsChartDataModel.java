/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import java.util.*;

import org.cougaar.planning.ldm.plan.ScheduleType;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
import com.klg.jclass.chart.ChartDataManager;

import org.cougaar.mlm.ui.data.*;

/**
 * Receives inventory objects from the clusters and implements
 * interfaces for JClass displays.
 */


public class InventoryDetailsChartDataModel extends InventoryChartDataModel{

    //    final public static int NUM_MILLISECS_GRANULARITY=1000;   // One second buckets
    final public static int NUM_MILLISECS_GRANULARITY=60 * 1000;   // One minute buckets

public InventoryDetailsChartDataModel(boolean inventory, 
				      String assetName,
				      String unitType,
				      Vector schedules,
				      Vector referenceOnHandSchedule,
				      String legendTitle,
				      boolean provider,
				      long   cDayTime,
				      boolean doUseCDay) {
    super(inventory,assetName,unitType,schedules,referenceOnHandSchedule,legendTitle,provider,cDayTime,doUseCDay);
  }


  /** Set inventory values; sets value for each day from start day to end day.
   */

  private void setDetailedOnHandValues(Vector onHandDetailedSchedule) {
    long minTime = -1;
    long maxTime = 0;

    // time base is set in InventoryChartBaseCalendar()
    // so all days have to be relative to that
    long baseTime = (useCDay) ? baseCDayTime : InventoryChartBaseCalendar.getBaseTime();
    seriesLabels = new String[nSeries];
    scheduleNames = new String[nSeries];
    seriesLabels[0] = getSeriesLabel(UISimpleNamedSchedule.ON_HAND);
    scheduleNames[0] = UISimpleNamedSchedule.ON_HAND;

    /**
     * do we need this part? MWD 

    for (int i = 0; i < onHandDetailedSchedule.size(); i++) {
      UIQuantityScheduleElement s = (UIQuantityScheduleElement)onHandDetailedSchedule.elementAt(i);
      long startTime = s.getStartTime();
      long endTime = s.getEndTime();
      long startSec = (long)((startTime - baseTime) / NUM_MILLISECS_GRANULARITY);
      long endSec = (long)((endTime - baseTime) / NUM_MILLISECS_GRANULARITY);
      if (minTime == -1)
	minTime = startSec;
      else if (startSec < minTime)
	minTime = startSec;
      maxTime = Math.max(endSec, maxTime);
    }
    **/

    int nValues = onHandDetailedSchedule.size();
    xvalues = new double[nSeries][];
    yvalues = new double[nSeries][];

    xvalues[0] = new double[onHandDetailedSchedule.size()];
    yvalues[0] = new double[onHandDetailedSchedule.size()];

    /** This isn't needed either
    for(int i=0; i< xvalues[0].length; i++) {
	xvalues[0][i] = (double)0D;
	yvalues[0][i] = (double)0D;
    }
    **/

    initZeroYVal(yvalues[0].length);

    for (int i = 0; i < onHandDetailedSchedule.size(); i++) {
      UIQuantityScheduleElement s = (UIQuantityScheduleElement)onHandDetailedSchedule.elementAt(i);
      long startTime = s.getStartTime();
      long endTime = s.getEndTime();
      startTime = (long)((startTime - baseTime) / NUM_MILLISECS_GRANULARITY);
      endTime = (long)((endTime - baseTime) / NUM_MILLISECS_GRANULARITY);
      double quantity = s.getQuantity() * ammoFactor;
      xvalues[0][i] = startTime;
      yvalues[0][i] = quantity;
    }

    System.out.println("Setting detailed inventory values");
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
    long minTime = -1;
    long maxTime = 0;

    // set on-hand (inventory) schedule
    for (int i = 0; i  < schedules.size(); i++) {
      UISimpleNamedSchedule namedSchedule =
	(UISimpleNamedSchedule)schedules.elementAt(i);
      if (namedSchedule.getName().equals(UISimpleNamedSchedule.ON_HAND_DETAILED)) {
	setDetailedOnHandValues(namedSchedule.getSchedule());
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
	long startSec = (long)((startTime - baseTime) / NUM_MILLISECS_GRANULARITY);
	long endSec = (long)((endTime - baseTime) / NUM_MILLISECS_GRANULARITY);
	if (minTime == -1)
	    minTime = startSec;
	else if (startSec < minTime)
	    minTime = startSec;
	maxTime = Math.max(endSec, maxTime);
      }
    }
 
    xvalues = new double[nSeries][];
    yvalues = new double[nSeries][];
    
    for (int i = 0; i < nSeries; i++) {
      UISimpleNamedSchedule namedSchedule = 
	(UISimpleNamedSchedule)schedules.elementAt(i);
      seriesLabels[i] = getSeriesLabel(namedSchedule.getName());
      scheduleNames[i] = namedSchedule.getName();

      //Vector schedule = computeDailyEndTimeBucketSchedule(namedSchedule.getSchedule());
      Vector schedule = namedSchedule.getSchedule();

      xvalues[i] = new double[schedule.size()];
      yvalues[i] = new double[schedule.size()];

      // use END DAY!!
     for (int j = 0; j < schedule.size(); j++) {
       UIQuantityScheduleElement s = (UIQuantityScheduleElement)schedule.elementAt(j);
       long endTime = s.getEndTime();
       long endSec = (long)((endTime - baseTime) / NUM_MILLISECS_GRANULARITY);
       if((referenceOnHandSchedule != null) &&
	  (endSec < minTime || endSec > maxTime)) {
	   System.out.println("WARNING: ignoring due-in or due-out schedule time that is before or after on-hand schedule times, Time: " + new Date(endTime * 1000));
       }
       else {
	   xvalues[i][j] = endSec;
	   yvalues[i][j] = s.getQuantity() * ammoFactor;
	 /***
	 System.out.println("InventoryChartDataModel::setInventoryValues: J is " + j +
			    " Qty is " + s.getQuantity() + " ammoFactor is " + ammoFactor +
			    "result is " + yvalues[i][j]);
	 */		    			    
       }
     }
    }
  }


    //Sums all the schedule elements that fall into one bucket
    //for the hard coded NUM_MILLISECS_GRANULARITY
    public Vector computeGranularEndTimeBucketSchedule(Vector schedule) {
	return computeGranularEndTimeBucketSchedule(schedule,NUM_MILLISECS_GRANULARITY);
    }


  public void resetInventory(UISimpleInventory inventory) {
      Vector scheduleNames = InventoryChart.getScheduleTypesForLegend(legendTitle,true);
      
      schedules = 
	  InventoryChart.extractSchedulesFromInventory(scheduleNames,
						       inventory);

      referenceOnHandSchedule=
	  InventoryChart.extractVectorFromInventory(UISimpleNamedSchedule.ON_HAND_DETAILED,
						    inventory);

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
	return null;
    }

    public void setAssociatedInactiveModel(InventoryChartDataModel assocModel){
	throw new RuntimeException("Cant call - this is detailed - no projections");
    }


    public Vector computeDailyEndTimeBucketSchedule(Vector schedule) {
	throw new RuntimeException("Whos calling this stupid method");
    }
}

