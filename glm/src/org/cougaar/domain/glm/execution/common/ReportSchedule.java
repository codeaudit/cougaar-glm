/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.common;

import java.util.GregorianCalendar;
import org.cougaar.domain.glm.ldm.asset.ReportSchedulePG;
import java.io.IOException;

public class ReportSchedule extends EGObjectBase implements EGObject {

  /**
   * Schedule times are expressed as a Calendar indicating some point in the cycle of the schedule plus one of the constants from the Calendar class indicating which field of the calendar varies. So a weekly schedule for reports at 10:03 AM on Tuesdays would have:
   * calendar.setTime(new Date(currentExecutionTimeInMillis))
   * calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
   * calendar.set(Calendar.HOUR), 10);
   * calendar.set(Calendar.MINUTE), 3);
   * calendar.set(Calendar.SECOND), 0);
   * calendar.set(Calendar.MILLISECOND), 0);
   * vary = Calendar.WEEK
   * offset is expressed in minutes after the beginning of the period
   * assuming 31 day months. 
   *
   * HOURLY -- minute of the hour
   * DAILY -- minute of the day
   * WEEKLY -- minute of the week
   * MONTHLY -- minute of the month
   * 
   **/
  public GregorianCalendar theBase; // The time of the first report
  public int theStep;              // The kind of step (e.g. Calendar.DAY)
  public int theJitter;

  public ReportSchedule(ReportSchedulePG rspg) {
    theBase = rspg.getBase();
    theStep = rspg.getStep();
    theJitter = rspg.getJitter();
  }

  public ReportSchedule() {
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeObject(theBase);
    writer.writeInt(theStep);
    writer.writeInt(theJitter);
  }

  public void read(LineReader reader) throws IOException {
    theBase = (GregorianCalendar) reader.readObject();
    theStep = reader.readInt();
    theJitter = reader.readInt();
  }
}
