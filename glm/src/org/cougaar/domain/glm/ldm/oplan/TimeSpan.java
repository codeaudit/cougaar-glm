/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.ldm.oplan;

import java.util.Calendar;
import java.util.Date;
import java.text.*;
import org.cougaar.core.util.XMLizable;
import org.cougaar.core.util.XMLize;
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
  private Date endDate;
        
  public TimeSpan() {}

  public TimeSpan(Date startDate, Date endDate) {
    this.startDate = internDate(startDate);
    this.endDate = internDate(endDate);
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
     
  /*
   * @deprecated - use getEndDate
   */   
  public Date getThruDate() {
     printWarning("getThruDate");
    return getEndDate();
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

   /**
    * TimeSpan "delta" to be removed.  This utility not needed.
    * @deprecated getEndDate removes the need for Oplan
    */
   public static Date getThruTime(int delta, Oplan oplan) {
     printWarning("getThruTime");
     return getEndTime(delta, oplan);
   }
 
   /**
    * TimeSpan "delta" to be removed.  This utility not needed.
    * @deprecated getEndDate removes the need for cDay
    */
  public static Date getThruTime(int delta, Date cday) {
     printWarning("getThruTime");
     return getEndTime(delta, cday);
  }

  private static void printWarning(String sMethodName) {
    System.err.println(
	"CALLING DEPRECATED METHOD org.cougaar.domain.glm.ldm.oplan.TimeSpan."+sMethodName+"()!");
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









