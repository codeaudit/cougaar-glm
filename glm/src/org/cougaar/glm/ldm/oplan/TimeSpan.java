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
package org.cougaar.glm.ldm.oplan;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * TimeSpan.
 * <p>
 * Interface change 10/25/99 to deprecate "delta" usage and replace
 * with Date usage.  <b>All deprecated methods and references to cDate
 * will be removed! 01/12/2000 /b>
 * <p>
 */
public class TimeSpan
  implements Serializable, Cloneable, org.cougaar.util.TimeSpan
{

  private Date startDate;
  private Date endDate;
        
  public TimeSpan() {}

  public TimeSpan(Date startDate, Date endDate) {
    this.startDate = internDate(startDate);
    this.endDate = internDate(endDate);
  }

  public TimeSpan(long startTime, long endTime) {
    this(new Date(startTime), new Date(endTime));
  }

  public boolean equals(Object o) {
    if (o instanceof TimeSpan) {
      TimeSpan ots = (TimeSpan) o;
      return getStartDate().equals(ots.getStartDate()) &&
        getEndDate().equals(ots.getEndDate());
    } 
    return false;
  }

  /**
   * startDelta and endDelta are the DAY offsets from baseDate
   * for the startDate and endDate, respectively.
   */
  public TimeSpan(Date baseDate, int startDelta, int endDelta) {
    Calendar formatter = Calendar.getInstance();
    formatter.setTime(baseDate);
    formatter.add(formatter.DATE, startDelta);
    this.startDate = internDate(formatter.getTime());

    formatter.setTime(baseDate);
    formatter.add(formatter.DATE, endDelta);
    this.endDate = internDate(formatter.getTime());

    this.cDate = internDate(baseDate); // to be removed
  }

  public void setStartDate(Date startDate) {
    this.startDate = internDate(startDate);
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /*
   * @deprecated - use setEndDate
   */
  public void setThruDate(Date thruDate) {
    printWarning("setThruDate");
    setEndDate(thruDate);
  }

  public Date getStartDate() {
    return startDate;
  }
     
  public Date getEndDate() {
    return new Date(getEndTime());
  }

  public long getStartTime() {
    return startDate.getTime();
  }
  public long getEndTime() {
    return endDate.getTime();
  }

  public Object clone() {
    TimeSpan ts = new TimeSpan(startDate, endDate);
    ts.cDate = cDate; // to be removed
    return ts;
  }

  /**
   * THE FOLLOWING METHODS HAVE ALL BEEN DEPRECATED!  THEY WILL BE REMOVED!
   */
  
  /** cDate to be removed **/
  private Date cDate;
 
  /**
    * @see #TimeSpan(Date,Date)
    * @deprecated cDate not needed
    */
   public TimeSpan(Date cDate, Date startDate, Date endDate) {
     printWarning("TimeSpan");
     this.cDate = internDate(cDate);
     this.startDate = internDate(startDate);
     this.endDate = internDate(endDate);
   }
 
   /**
    * @see #setThruDate
    * @deprecated Using setStartDate removes the need for cDate
    */
   public void setStartDelta(int startDelta) {
     printWarning("setStartDelta");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(cDate);
     formatter.add(formatter.DATE, startDelta);
     this.startDate = internDate(formatter.getTime());
   }
 
   /**
    * @see #setEndDate
    * @deprecated Using setEndDate removes the need for cDate
    */
   public void setEndDelta(int endDelta) {
     printWarning("setEndDelta");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(cDate);
     formatter.add(formatter.DATE, endDelta);
     this.endDate = internDate(formatter.getTime());
   }

   /**
    * @see #setThruDate
    * @deprecated Using setEndDate removes the need for cDate
    */
   public void setThruDelta(int thruDelta) {
     setEndDelta(thruDelta);
   }
 
   /**
    * TimeSpan "delta" to be removed.  This utility not needed.
    * @deprecated getStartDate removes the need for Oplan
    */
   public static Date getStartTime(int delta, Oplan oplan) {
     printWarning("getStartTime");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(oplan.getCday());
     formatter.add(formatter.DATE, delta);
     return (formatter.getTime());
   }
 
   /**
    * TimeSpan "delta" to be removed.  This utility not needed.
    * @deprecated getStartDate removes the need for cDay
    */
   public static Date getStartTime(int delta, Date cday) {
     printWarning("getStartTime");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(cday);
     formatter.add(formatter.DATE, delta);
     return (formatter.getTime());
   }
 
   /**
    * TimeSpan "delta" to be removed.  This utility not needed.
    * @deprecated getEndDate removes the need for Oplan
    */
   public static Date getEndTime(int delta, Oplan oplan) {
     printWarning("getEndTime");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(oplan.getCday());
     formatter.add(formatter.DATE, delta);
     return (formatter.getTime());
   }
 
   /**
    * TimeSpan "delta" to be removed.  This utility not needed.
    * @deprecated getEndDate removes the need for cDay
    */
  public static Date getEndTime(int delta, Date cday) {
     printWarning("getEndTime");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(cday);
     formatter.add(formatter.DATE, delta);
     return (formatter.getTime());
  }

  private static void printWarning(String sMethodName) {
    /* Silenced for now
    Exception e = new java.lang.IllegalArgumentException("CALLING DEPRECATED METHOD org.cougaar.glm.ldm.oplan.TimeSpan."+sMethodName+"()!");
    e.printStackTrace();
    */
  }

  private static final java.util.HashMap dateCache = new java.util.HashMap(89);
  private static final Date internDate(Date d) {
    synchronized (dateCache) {
      Date id = (Date) dateCache.get(d);
      if (id != null) return id;
      dateCache.put(d, d);
      return d;
    }
  }

  public String toString() {
    return formatDate(startDate) + " - " + formatDate(endDate);
  }
  private static SimpleDateFormat dateFormat =
    new SimpleDateFormat("MM/dd/yy HH:mm");

  private static String formatDate(long time) {
    return formatDate(new Date(time));
  }
  private static String formatDate(Date date) {
    return dateFormat.format(date);
  }
}
