/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.oplan;

import java.util.Calendar;
import java.util.Date;
import java.text.*;
import org.cougaar.util.XMLizable;
import org.cougaar.util.XMLize;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.Serializable;

/**
 * TimeSpan.
 * <p>
 * Interface change 10/25/99 to deprecate "delta" usage and replace
 * with Date usage.  <b>All deprecated methods and references to cDate
 * will be removed! 01/12/2000 /b>
 * <p>
 */
public class TimeSpan
  implements XMLizable, Serializable, Cloneable, org.cougaar.util.TimeSpan
{

  private Date startDate;
  private Date thruDate;
        
  public TimeSpan() {}

  public TimeSpan(Date startDate, Date thruDate) {
    this.startDate = internDate(startDate);
    this.thruDate = internDate(thruDate);
  }

  public boolean equals(Object o) {
    if (o instanceof TimeSpan) {
      TimeSpan ots = (TimeSpan) o;
      return getStartDate().equals(ots.getStartDate()) &&
        getThruDate().equals(ots.getThruDate());
    } 
    return false;
  }

  /**
   * startDelta and thruDelta are the DAY offsets from baseDate
   * for the startDate and thruDate, respectively.
   */
  public TimeSpan(Date baseDate, int startDelta, int thruDelta) {
    Calendar formatter = Calendar.getInstance();
    formatter.setTime(baseDate);
    formatter.add(formatter.DATE, startDelta);
    this.startDate = internDate(formatter.getTime());
    formatter.setTime(baseDate);
    formatter.add(formatter.DATE, thruDelta + 1); // MSB/JEB : 12-2-99 ThruDate means through the end of the day
    this.thruDate = internDate(formatter.getTime());
    this.cDate = internDate(baseDate); // to be removed
  }

  public void setStartDate(Date startDate) {
    this.startDate = internDate(startDate);
  }

  public void setThruDate(Date thruDate) {
    this.thruDate = internDate(thruDate);
  }

  public Date getStartDate() {
    return startDate;
  }
        
  public Date getThruDate() {
    return thruDate;
  }

  public long getStartTime() {
    return startDate.getTime();
  }
  public long getEndTime() {
    // Add a day of millis, since thruDate is defined to be 24 
    // hours earlier than EndTime.
    return thruDate.getTime()+(24*60*60*1000);
  }

  public Object clone() {
    TimeSpan ts = new TimeSpan(startDate, thruDate);
    ts.cDate = cDate; // to be removed
    return ts;
  }

  /**
   * XMLizable method for UI, other clients
   */ 
  public Element getXML(Document doc) {
    return XMLize.getPlanObjectXML(this,doc);
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
   public TimeSpan(Date cDate, Date startDate, Date thruDate) {
     printWarning("TimeSpan");
     this.cDate = internDate(cDate);
     this.startDate = internDate(startDate);
     this.thruDate = internDate(thruDate);
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
    * @see #setThruDate
    * @deprecated Using setThruDate removes the need for cDate
    */
   public void setThruDelta(int thruDelta) {
     printWarning("setThruDelta");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(cDate);
     formatter.add(formatter.DATE, thruDelta);
     this.thruDate = internDate(formatter.getTime());
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
    * @deprecated getThruDate removes the need for Oplan
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
    * @deprecated getThruDate removes the need for cDay
    */
  public static Date getEndTime(int delta, Date cday) {
     printWarning("getEndTime");
     Calendar formatter = Calendar.getInstance();
     formatter.setTime(cday);
     formatter.add(formatter.DATE, delta);
     return (formatter.getTime());
  }

  private static void printWarning(String sMethodName) {
    System.err.println(
	"CALLING DEPRECATED METHOD org.cougaar.domain.glm.oplan.TimeSpan."+sMethodName+"()!");
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

}
