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

			   
