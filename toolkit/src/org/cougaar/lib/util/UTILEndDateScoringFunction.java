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

package org.cougaar.lib.util;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ScoringFunction;

import java.util.Calendar;
import java.util.Date;
import org.cougaar.util.log.*;

/** 
 * Represents an <early, best, late> end date scoring function
 * 
 */

public class UTILEndDateScoringFunction extends ScoringFunction.VScoringFunction {
  private static boolean debug = false;
  private static Logger logger=LoggerFactory.getInstance().createLogger("UTILEndDateScoringFunction");

  public static void setDebug (boolean dbg) { debug = dbg; }
  
  public UTILEndDateScoringFunction(Date early, Date best, Date late,
				    double boundaryScore) {
    super (new AspectValue (AspectType.END_TIME, (double) early.getTime ()),
	   new AspectValue (AspectType.END_TIME, (double) best.getTime  ()),
	   new AspectValue (AspectType.END_TIME, (double) late.getTime  ()),
	   boundaryScore);
  }

  public Date getEarlyDate () {
    return new Date ((long) point1.getValue ());
  }
  public Date getBestDate () {
    return new Date ((long) best.getValue ());
  }
  public Date getLateDate () {
    return new Date ((long) point2.getValue ());
  }

  public Object clone() {
    return new UTILEndDateScoringFunction(new Date(point1.longValue()),
					  new Date(best.longValue()),
					  new Date(point2.longValue()),
					  ok);
  }
  
  public static void main (String [] args) {
    Calendar cal = Calendar.getInstance ();
    cal.set (1999, 6, 1, 11, 59, 59);
    Date beforeearly = cal.getTime ();
    cal.set (1999, 6, 1, 12, 0, 0);
    Date early = cal.getTime ();

    cal.set (1999, 6, 1, 13, 0, 0);
    Date best = cal.getTime ();
    cal.set (1999, 6, 1, 14, 0, 0);
    Date late = cal.getTime ();
    cal.set (1999, 6, 1, 14, 0, 1);
    Date afterlate = cal.getTime ();
    ScoringFunction sf = new UTILEndDateScoringFunction (early, 
							 best,
							 late, 0.9);
    setDebug (true);
    AspectValue av = new AspectValue (AspectType.END_TIME, 
				      (double) beforeearly.getTime ());
    logger.debug ("Score for before early " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) early.getTime ());
    logger.debug ("Score for early " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) best.getTime ());
    logger.debug ("Score for best " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) late.getTime ());
    logger.debug ("Score for late " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) afterlate.getTime ());
    logger.debug ("Score for after late " + sf.getScore (av));
  }
}
