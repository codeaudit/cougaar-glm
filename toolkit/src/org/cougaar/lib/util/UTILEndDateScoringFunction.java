/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.util;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;

import java.util.Calendar;
import java.util.Date;

/** 
 * Represents an <early, best, late> end date scoring function
 * 
 */

public class UTILEndDateScoringFunction extends ScoringFunction.VScoringFunction {
  private static boolean debug = false;
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
    System.out.println ("Score for before early " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) early.getTime ());
    System.out.println ("Score for early " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) best.getTime ());
    System.out.println ("Score for best " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) late.getTime ());
    System.out.println ("Score for late " + sf.getScore (av));

    av = new AspectValue (AspectType.END_TIME, (double) afterlate.getTime ());
    System.out.println ("Score for after late " + sf.getScore (av));
  }
}
