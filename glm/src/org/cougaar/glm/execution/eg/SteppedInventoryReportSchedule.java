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
package org.cougaar.glm.execution.eg;

import org.cougaar.glm.execution.common.*;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;

public class SteppedInventoryReportSchedule extends Timed implements TimeConstants {
  public String theSource;
  protected InventoryScheduleManager theInventoryScheduleManager;
  public InventoryReportSchedule theInventoryReportSchedule;
  private Object theKey = null;
  private int step;
  private long theTime = -1;  // -1 signifies needs to be computed

  public SteppedInventoryReportSchedule(String aSource,
                                        InventoryReportSchedule anInventoryReportSchedule,
                                        InventoryScheduleManager anInventoryScheduleManager)
  {
    theSource = aSource.intern();
    theInventoryScheduleManager = anInventoryScheduleManager;
    if (anInventoryReportSchedule == null) {
      throw new IllegalArgumentException("anInventoryReportSchedule is null");
    }
    theInventoryReportSchedule = anInventoryReportSchedule;
    setEnabled(true);
  }

  public static Object getKey(String theSource, InventoryReportSchedule irs) {
    return theSource + "/" + irs.theItemIdentification;
  }

  public Object getKey() {
    if (theKey == null) {
      theKey = getKey(theSource, theInventoryReportSchedule);
    }
    return theKey;
  }

  public String getCluster() {
    return theSource;
  }

    /** Crazy code asks for yesterday's inventory report today and stamps it with yesterday's date
     **/
  public boolean expired(long time) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(getTime()));
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.MILLISECOND, -1); // Last millisecond of previous day
    time = cal.getTime().getTime();
    try {
      theInventoryScheduleManager
        .requestInventoryReport(theSource,
                                theInventoryReportSchedule.theItemIdentification,
                                time);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    step(1);
    return false;
  }

  public void step(int amount) {
    step += amount;
    theTime = -1L;
  }

  public long getTime() {
    if (theTime < 0L) {
      GregorianCalendar cal = (GregorianCalendar) theInventoryReportSchedule.theBase.clone();
      cal.add(theInventoryReportSchedule.theStep, step);
      theTime = cal.getTime().getTime();
    }
    return theTime;
  }

  public String getItem() {
    return theInventoryReportSchedule.theItemIdentification;
  }
}
