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

package org.cougaar.lib.gss;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Schedule;
import java.util.Date;
import org.cougaar.lib.util.UTILPreference;

import org.cougaar.lib.gss.GSTravelTime;

/**
 * Accesses the specified property in the specified asset for a given task
 *
 */

public class GSTaskDuration implements GSParent {

  private GSTaskAccessor taskAccessor = null;
  private GSTravelTime travelTime = null;
  private GSConstantAccessor fixedTaskDuration = null;

  public Object value (Task task, Asset asset) {
    if (fixedTaskDuration != null)
      return fixedTaskDuration.value (task /* ignored! */);
    if (taskAccessor != null) {
      Object v = taskAccessor.value (task);
      if (v instanceof Double)
        return new Long ((long) (1000.0 * ((Double) v).doubleValue()));
      else
        return new Long ((long) (1000 * ((Integer) v).intValue()));
    }
    if (travelTime != null)
      return new Long ((long) (1000.0 * travelTime.value (task, asset)));
    /*
    Date start = null;
    try {
      start = ((Schedule) task.getPrepositionalPhrase(Constants.Preposition.READYAT).
               getIndirectObject()).getStartDate();
    } catch (Exception e) {
      start = new Date();
      System.out.println ("Warning: no ready at preposition, so using now");
    }
    Date end = task.getPenaltyFunction().getDesiredScheduleBestDate();
    */
    Date start = UTILPreference.getReadyAt(task);
    Date end   = UTILPreference.getBestDate(task);

    return new Long (end.getTime() - start.getTime());
  }

  public void addChild (Object child) {
    if (child instanceof GSConstantAccessor)
      fixedTaskDuration = (GSConstantAccessor) child;
    else if (child instanceof GSTaskAccessor)
      taskAccessor = (GSTaskAccessor) child;
    else
      travelTime = (GSTravelTime) child;
  }

}
