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

package org.cougaar.lib.util;

import java.util.Date;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

/** 
 * Represents an <early, best, late> end date scoring function
 * 
 */

public class UTILEndDateScoringFunction extends ScoringFunction.VScoringFunction {
  /* only used for isolated main ()-style testing */
  private static Logger logger=LoggerFactory.getInstance().createLogger("UTILEndDateScoringFunction");

  public UTILEndDateScoringFunction(Date early, Date best, Date late,
				    double boundaryScore) {
    super (AspectValue.newAspectValue (AspectType.END_TIME, (double) early.getTime ()),
	   AspectValue.newAspectValue (AspectType.END_TIME, (double) best.getTime  ()),
	   AspectValue.newAspectValue (AspectType.END_TIME, (double) late.getTime  ()),
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
  
  /*
  public void main (String [] args) {
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
    AspectValue av = AspectValue.newAspectValue (AspectType.END_TIME, 
				      (double) beforeearly.getTime ());
    logger.debug ("Score for before early " + sf.getScore (av));

    av = AspectValue.newAspectValue (AspectType.END_TIME, (double) early.getTime ());
    logger.debug ("Score for early " + sf.getScore (av));

    av = AspectValue.newAspectValue (AspectType.END_TIME, (double) best.getTime ());
    logger.debug ("Score for best " + sf.getScore (av));

    av = AspectValue.newAspectValue (AspectType.END_TIME, (double) late.getTime ());
    logger.debug ("Score for late " + sf.getScore (av));

    av = AspectValue.newAspectValue (AspectType.END_TIME, (double) afterlate.getTime ());
    logger.debug ("Score for after late " + sf.getScore (av));
  }
  */
}
