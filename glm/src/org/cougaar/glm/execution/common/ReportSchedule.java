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
package org.cougaar.glm.execution.common;

import java.io.IOException;
import java.util.GregorianCalendar;

import org.cougaar.glm.ldm.asset.ReportSchedulePG;

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
