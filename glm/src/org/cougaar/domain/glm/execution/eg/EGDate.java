/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.eg;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Extend java.util.Date to modify the toString method. Used as table
 * cell values.
 **/
public class EGDate extends Date {
  private static final SimpleDateFormat format =
    new SimpleDateFormat("MM/dd/yyyy HH:mm");

  private static Date fdate = new Date();
  static {
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  public EGDate(long millis) {
    super(millis);
  }

  public EGDate(String s) {
    super(s);
  }

  public static String format(Date date) {
    return format.format(date);
  }

  public static String format(long time) {
    synchronized (fdate) {
      fdate.setTime(time);
      return format(fdate);
    }
  }

  public String toString() {
    return format(this);
  }
}

			   
