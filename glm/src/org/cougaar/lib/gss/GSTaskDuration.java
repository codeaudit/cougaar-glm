/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Schedule;
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
