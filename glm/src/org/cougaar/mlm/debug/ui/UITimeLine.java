/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class UITimeLine {
  private static long MSECS_PER_HOUR = 3600000;
  private static long MSECS_PER_DAY = MSECS_PER_HOUR * 24;
  private static int MIN_INTERVALS = 2;
  private int numberOfIntervals;
  private int intervalUnits;
  private Date startDate;
  private GregorianCalendar startCalendar;

  /** Create a timeline with the minimum and maximum times.
    Optionally determine timeline divisions: years, days or hours.
    @param startDate the date corresponding to the first division on the timeline
    @param stopDate the date corresponding to the last division on the timeline
    */
  public UITimeLine(Date startDate, Date stopDate) {
    initTimeLine(startDate, stopDate, false, 0);
  }

  /** Create a timeline with the minimum and maximum times.
    Optionally determine timeline divisions: years, days or hours.
    @param startDate the date corresponding to the first division on the timeline
    @param stopDate the date corresponding to the last division on the timeline
    @param intervalUnits years, days, or hours, or unspecified
    */
  public UITimeLine(Date startDate, Date stopDate, int intervalUnits) {
    initTimeLine(startDate, stopDate, true, intervalUnits);
  }

  private void initTimeLine(Date startDate, Date stopDate, 
			    boolean haveTimeIntervals, int intervalUnits) {
    this.startDate = startDate;
    startCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    startCalendar.setTime(startDate);
    GregorianCalendar stopCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    stopCalendar.setTime(stopDate);
    // determines number of intervals and interval units
    if (haveTimeIntervals) {
      this.intervalUnits = intervalUnits;
      if (intervalUnits == Calendar.YEAR) {
	int startYear = startCalendar.get(Calendar.YEAR);
	int stopYear = stopCalendar.get(Calendar.YEAR);
	numberOfIntervals = stopYear - startYear + 1;
      } else {
	long stopTime = stopCalendar.getTime().getTime();
	long startTime = startCalendar.getTime().getTime();
	if (intervalUnits == Calendar.DAY_OF_YEAR) {
	  numberOfIntervals = 
	    (int)((stopTime - startTime) / MSECS_PER_DAY) + 1;
	} else if (intervalUnits == Calendar.HOUR_OF_DAY) { // use hours
	  numberOfIntervals = 
	    (int)((stopTime - startTime) / MSECS_PER_HOUR) + 1;
	} else {
	  System.out.println("Ignoring invalid time line interval.");
	  setIntervalUnits(startCalendar, stopCalendar);
	}
      }
    } else
      setIntervalUnits(startCalendar, stopCalendar);
  }

  private void setIntervalUnits(GregorianCalendar startCalendar,
				GregorianCalendar stopCalendar) {
    int startYear = startCalendar.get(Calendar.YEAR);
    int stopYear = stopCalendar.get(Calendar.YEAR);

    // if schedule is greater than 1 year long, use years as time intervals
    if ((stopYear - startYear) > 1) {
      numberOfIntervals = stopYear - startYear + 1;
      intervalUnits = Calendar.YEAR;
      return;
    }

    // if schedule is greater than minInterval days long, use days
    long stopTime = stopCalendar.getTime().getTime();
    long startTime = startCalendar.getTime().getTime();
    numberOfIntervals = (int)((stopTime - startTime) / MSECS_PER_DAY) + 1;
    if (numberOfIntervals > MIN_INTERVALS) {
      intervalUnits = Calendar.DAY_OF_YEAR;
      return;
    }

    // use hour intervals
    intervalUnits = Calendar.HOUR_OF_DAY;
    numberOfIntervals = (int)((stopTime - startTime) / MSECS_PER_HOUR) + 1;
  }

  /** Return array of strings for timeline labels.
    @return the numeric labels for the timeline
   */

  public String[] getLabels() {
    String labels[] = new String[numberOfIntervals];
    GregorianCalendar nextCalendar = (GregorianCalendar)startCalendar.clone();
    int labelUnits = intervalUnits;
    if (labelUnits == Calendar.DAY_OF_YEAR)
      labelUnits = Calendar.DAY_OF_MONTH; // more useful
    for (int i = 0; i < numberOfIntervals; i++) {
      labels[i] = String.valueOf(nextCalendar.get(labelUnits));
      nextCalendar.add(intervalUnits, 1);
    }
    return labels;
  }

  /** Get number of intervals in the timeline.
    @return number of intervals in the timeline
   */

  public int getNumberOfIntervals() {
    return numberOfIntervals;
  }

  /** Get timeline units.
    @return Calendar.HOUR_OF_DAY, Calendar.DAY_OF_YEAR, or Calendar.HOUR_OF_DAY
    */

  public int getIntervalUnits() {
    return intervalUnits;
  }

  /** Get timeline units name.
    @return "Hours" "Days" or "Year"
    */

  public String getIntervalUnitsName() {
    switch (intervalUnits) {
    case Calendar.HOUR_OF_DAY:
      return "Hours";
    case Calendar.DAY_OF_YEAR:
      return "Days";
    case Calendar.YEAR:
      return "Year";
    default:
      return "";
    }
  }

  /** For testing, create a time line.
   */

  public static void main(String args[]) {
    GregorianCalendar start = new GregorianCalendar(1999, 12, 31, 9, 0, 0);
    GregorianCalendar stop = new GregorianCalendar(2000, 1, 1, 20, 0, 0);
    Date startDate = start.getTime();
    Date stopDate = stop.getTime();
    long x = stopDate.getTime() - startDate.getTime();
    System.out.println("Difference is: " + x);
    UITimeLine timeLine = new UITimeLine(startDate, stopDate);
    System.out.println("Number of intervals: " + timeLine.getNumberOfIntervals());
  }
}
