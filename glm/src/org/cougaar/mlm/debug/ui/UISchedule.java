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


package org.cougaar.mlm.debug.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.glm.ldm.asset.PhysicalAsset;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** Creates the values for a bar graph for displaying assets vs. time.
 */

public class UISchedule implements UIBarGraphSource, UISubscriber {
  private UIPlugIn uiPlugIn;
  private static long MSECS_PER_HOUR = 3600000;
  private static long MSECS_PER_DAY = MSECS_PER_HOUR * 24;
  private Vector listeners;
  private UITimeLine uiTimeLine;
  private Date startDate;
  private int numberOfXIntervals;
  private String xLegend;
  private String xLabels[];
  private int numberOfYIntervals = 0;
  private String yLegend;
  private String yLabels[];
  private String legend[];
  private int intervalUnits; // Calendar.YEAR, DAY_OF_YEAR, HOUR_OF_DAY
  // for each bar, the y-value for the xth interval,
  // i.e. the quantity of an asset scheduled for each time interval
  private int values[][] = null;
  private Hashtable assetScheduleDictionary; // for multiple assets
  private String assetName = null;   // for single assets
  boolean singleAsset;        // flag used throughout to distinguish plotting single vs. multiple assets
  private Vector planElements = new Vector(10);

  /** Create the information for a bar graph of scheduled assets,
    which are assets allocated by the specified cluster for the
    specified plan.
    The display consists of a set of vertical bars for each asset;
    the y-axis represents quantity of the asset, the x-axis represents time.
    @param uiPlugIn this user interface plugin
    @param planName the name of the plan for which to display scheduled assets
    @param clusterId the cluster for which to display scheduled assets
    @param assetName name of asset, null to graph all assets
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UISchedule(UIPlugIn uiPlugIn, String planName, 
		    ClusterIdentifier clusterId, 
		    String assetName, Object listener) throws UINoPlanException {
    if (assetName != null) {
      singleAsset = true;
      this.assetName = assetName;
    } else {
      singleAsset = false;
    }
    listeners = new Vector(1);
    listeners.addElement(listener);
    this.uiPlugIn = uiPlugIn;
  }

  /** Can't start subscription in constructor, because you could
    get a subscriptionChanged before the UIBarGraphDisplay is ready.
    */

  public void startSubscription() {
    uiPlugIn.subscribe(this, planElementPredicate());
  }

  private static UnaryPredicate planElementPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof PlanElement );
      }
    };
  }

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    //    System.out.println("Container changed");
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements())
      planElements.addElement(added.nextElement());
    Enumeration removed = container.getRemovedList();
    while (removed.hasMoreElements())
      planElements.removeElement(removed.nextElement());
    recomputeAssets();
  }


  /** Get all the plan elements and then get all the assets and schedules.
    There is a hash table entry for each asset name which contains
    a vector of scheduleitems; each scheduleitem
    is a schedule from a single allocation and a quantity
    (which is 1 for single assets and asset.getCount()) for aggregate assets.
    This fills in the vectors of scheduleitems by recording the schedules
    in all the plan elements which are allocations
    and which allocate physical assets (not clusters).
    It also computes the earliest and latest dates in schedules;
    these are used to lay out the time line.
    */

  private void computeAssetSchedules() {
    int quantity;
    Date endDate = new Date(); // init value; it's set from first schedule
    startDate = endDate; // init for timeline
    boolean datesInitted = false; // set true when we see first real schedule
    assetScheduleDictionary = new Hashtable(10);
    int nAssets = 0; // number of different assets
    for (int j = 0; j < planElements.size(); j++) {
      PlanElement planElement = (PlanElement)planElements.elementAt(j);
      if (!(planElement instanceof Allocation))
	continue; // not an allocation
      Asset asset = (Asset)((Allocation)planElement).getAsset();
      if (!((asset instanceof PhysicalAsset) || (asset instanceof AggregateAsset)))
	continue; // not a physical or aggregate asset
      String thisAssetName = "";
      if (asset instanceof PhysicalAsset) 
	thisAssetName = UIAsset.getDescription(asset);
      else if (asset instanceof AggregateAsset) {
	AggregateAsset aa = (AggregateAsset) asset;
	if (aa.getAsset() == null)
	  continue; // ignore aggregate assets with null property
	thisAssetName = UIAsset.getDescription(aa.getAsset());

      }
		
      if ((thisAssetName == null) || (thisAssetName.equals("")))
	continue; // ignore assets with null or empty names
      if ((singleAsset) && (!(thisAssetName.equals(assetName))))
	continue; // not the asset we're interested in
    	ScheduleElement schedule = null;
    	AllocationResult ar = planElement.getReportedResult();
    	if (ar != null) {
    		double resultstart = ar.getValue(AspectType.START_TIME);
    		double resultend = ar.getValue(AspectType.END_TIME);
        schedule = new ScheduleElementImpl(new Date(((long)resultstart)), new Date(((long)resultend)) );
    	}
      if (schedule == null)
	continue; // ignore assets without schedules
      quantity = 1;
      if (asset instanceof AggregateAsset){
	quantity = (int)((AggregateAsset)asset).getQuantity();
      }	  		 
      ScheduleElementItem scheduleItem = new ScheduleElementItem(schedule, quantity);
      if (!datesInitted) {
	datesInitted = true;
	startDate = schedule.getStartDate();
	endDate = schedule.getEndDate();
      } else {
	if (schedule.getStartDate().before(startDate))
	  startDate = schedule.getStartDate();
	if (schedule.getEndDate().after(endDate))
	  endDate = schedule.getEndDate();
      }
      Vector assetSchedules =  (Vector)assetScheduleDictionary.get(thisAssetName);
      if (assetSchedules == null) {
	assetSchedules = new Vector(10);
	assetSchedules.addElement(scheduleItem);
	assetScheduleDictionary.put(thisAssetName, assetSchedules);
	nAssets++;
      } else {
	assetSchedules.addElement(scheduleItem);
      }
    }

    // determine if the timeline interval should be years or days or hours
    // and round start/end back/forward to year, day or hour boundaries
    GregorianCalendar startCalendar
      = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    startCalendar.setTime(startDate);
    GregorianCalendar endCalendar
      = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    endCalendar.setTime(endDate);
    int startYear = startCalendar.get(Calendar.YEAR);
    int endYear = endCalendar.get(Calendar.YEAR);
    if ((endYear - startYear) > 1) {
      intervalUnits = Calendar.YEAR;
      int year = startCalendar.get(Calendar.YEAR);
      startCalendar.clear();
      startCalendar.set(year, 0, 0);
      endCalendar.add(Calendar.YEAR, 1);
      year = endCalendar.get(Calendar.YEAR);
      endCalendar.clear();
      endCalendar.set(year, 0, 0);
    } else {
      long endTime = endCalendar.getTime().getTime();
      long startTime = startCalendar.getTime().getTime();
      int numberOfIntervals = (int)((endTime - startTime) / MSECS_PER_DAY) + 1;
      if (numberOfIntervals > 2) { // use days
	intervalUnits = Calendar.DAY_OF_YEAR;
	int year = startCalendar.get(Calendar.YEAR);
	int month = startCalendar.get(Calendar.MONTH);
	int day = startCalendar.get(Calendar.DAY_OF_MONTH);
	startCalendar.clear();
	startCalendar.set(year, month, day);
	endCalendar.add(Calendar.DAY_OF_YEAR, 1);
	year = endCalendar.get(Calendar.YEAR);
	month = endCalendar.get(Calendar.MONTH);
	day = endCalendar.get(Calendar.DAY_OF_MONTH);
	endCalendar.clear();
	endCalendar.set(year, month, day);
      }
      else { // use hours
	intervalUnits = Calendar.HOUR_OF_DAY;
	int year = startCalendar.get(Calendar.YEAR);
	int month = startCalendar.get(Calendar.MONTH);
	int day = startCalendar.get(Calendar.DAY_OF_MONTH);
	int hour = startCalendar.get(Calendar.HOUR_OF_DAY);
	startCalendar.clear();
	startCalendar.set(year, month, day, hour, 0);
	endCalendar.add(Calendar.HOUR_OF_DAY, 1);
	year = endCalendar.get(Calendar.YEAR);
	month = endCalendar.get(Calendar.MONTH);
	day = endCalendar.get(Calendar.DAY_OF_MONTH);
	hour = endCalendar.get(Calendar.HOUR_OF_DAY);
	endCalendar.clear();
	endCalendar.set(year, month, day, hour, 0);
      }
    }
    startDate = startCalendar.getTime();
    endDate = endCalendar.getTime();
    // create a time line for the x-axis
    uiTimeLine = new UITimeLine(startDate, endDate, intervalUnits);
    // set numberOfXIntervals, xLegend, xLabels from the timeline
    numberOfXIntervals = uiTimeLine.getNumberOfIntervals();
    xLegend = uiTimeLine.getIntervalUnitsName() + " starting at " + 
      getDateLabel(startDate);
    xLabels = uiTimeLine.getLabels();

    // init empty displays for single assets
    if ((singleAsset) && (nAssets == 0)) {
      legend = new String[1]; // legend is the name of this asset
      legend[0] = assetName;
      values = new int[0][];
    } else {
      // number of vertical bars at each interval is the number of assets
      values = new int[nAssets][];
      Enumeration assetNames = assetScheduleDictionary.keys();
      legend = new String[nAssets];
      int i = 0;
      while (assetNames.hasMoreElements()) {
	String assetName = (String)assetNames.nextElement();
	values[i] = getAssetSchedule(assetName);
	legend[i++] = assetName;
      }
    }

    // finally compute y labels which are just numbers of assets
    yLabels = new String[numberOfYIntervals];
    for (int i = 1; i <= numberOfYIntervals; i++)
      yLabels[i-1] = String.valueOf(i);
    // set the yLegend
    yLegend = "Assets";
  }

  /** Given the name of an asset, return the number of that
    asset used for each interval in the overall schedule.
    */
    
  private int[] getAssetSchedule(String assetName) {
    long intervalMSecs = 0;
    Vector assetSchedules = 
      (Vector)assetScheduleDictionary.get(assetName);
    int totals[] = new int[numberOfXIntervals];
    long intervalStart = startDate.getTime();
    if (intervalUnits == Calendar.HOUR_OF_DAY)
      intervalMSecs = 3600000;
    else if (intervalUnits == Calendar.DAY_OF_YEAR)
      intervalMSecs = 86400000;
    else if (intervalUnits == Calendar.YEAR)
      intervalMSecs = 86400000 * 365; // roughly
    long intervalEnd = intervalStart + intervalMSecs;
    for (int j = 0; j < numberOfXIntervals; j++) {
      int total = 0;
      for (int i = 0; i < assetSchedules.size(); i++) {
	ScheduleItem scheduleItem = (ScheduleItem)assetSchedules.elementAt(i);
	Schedule schedule = scheduleItem.schedule;
	long scheduleStart = schedule.getStartTime();
	long scheduleEnd = schedule.getEndTime();
	if (((intervalStart <= scheduleStart) &&
	     (intervalEnd > scheduleStart)) ||
	    ((intervalStart <= scheduleEnd) &&
	     (intervalEnd > scheduleEnd))) {
	  total = total + scheduleItem.quantity;
	}
      }
      totals[j] = total;
      numberOfYIntervals = Math.max(numberOfYIntervals, total);
      intervalStart = intervalStart + intervalMSecs;
      intervalEnd = intervalEnd + intervalMSecs;
    }
    return totals;
  }

  /** Return a string for the date and time in GMT.
   */

  private String getDateLabel(Date date) {
    DateFormat dateFormat = 
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(date);
  }

  /** Number of intervals (years, days, or hours) in the schedule.
    @return number of years, days or hours in the schedule
    */

  public int getNumberOfXIntervals() {
    return numberOfXIntervals;
  }

  /** (Years, days or hours) starting at (start date)
    @return "Years" "Days" or "Hours" "starting at" (start date)
   */

  public String getXLegend() {
    return xLegend;
  }

  /** Years, days or hours.
    @return numeric labels for years, days or hours
   */

  public String[] getXLabels() {
    return xLabels;
  }

  /** Maximum number of assets assigned in any one time interval.
    @return maximum number of assets assigned in a time interval
   */

  public int getNumberOfYIntervals() {
    return numberOfYIntervals;
  }

  /** "Assets"
    @return "Assets"
   */

  public String getYLegend() {
    return yLegend;
  }

  /** Quantities of assets.
    @return numeric labels for quantities of assets
   */
   
  public String[] getYLabels() {
    return yLabels;
  }

  /** Names of assets.
    @return names of assets
   */

  public String[] getLegend() {
    return legend;
  }

  /** The quantity of each asset scheduled for each time interval.
    @return for each asset, the quantity scheduled for each time interval
   */

  public int[][] getValues() {
    return values;
  }

  /** Whether or not to make the bars in the bar graph contiguous.
    @return for single assets, return true; for multiple assets, return false
    */

  public boolean getContiguous() {
    return singleAsset;
  }

  /** Listen for changes in the assets scheduled by the cluster.
    @param listener object to notify when scheduled assets change
   */

  public void registerListener(ActionListener listener) {
    listeners.addElement(listener);
  }

  /** Handle added, deleted or changed events on the plan elements
    and notify listeners of the change.  
    The UIBarGraphDisplay object listens for changes
    and invokes methods in the UIBarGraph object to get
    the updated values from this object and repaint the graph.
    @param e event (new, changed, or deleted object)
   */


  /** Notify listeners that scheduled assets have changed.
   */

  private void recomputeAssets() {
    computeAssetSchedules();
    for (int i = 0; i < listeners.size(); i++) {
      ActionListener listener = (ActionListener)listeners.elementAt(i);
      //System.out.println("Listener notified");
      listener.actionPerformed(new ActionEvent(this, 0, ""));
    }
  }


  /** Called to force an update of the asset schedules.
   */

  public void update() {
    recomputeAssets();
  }
}


class ScheduleItem {
  Schedule schedule;
  int quantity;

  public ScheduleItem(Schedule schedule, int quantity) {
    this.schedule = schedule;
    this.quantity = quantity;
  }
    
}

class ScheduleElementItem {
	ScheduleElement se;
	int quantity;
	
	public ScheduleElementItem(ScheduleElement scheduleelem, int quantity) {
		se = scheduleelem;
		this.quantity = quantity;
	}
}
