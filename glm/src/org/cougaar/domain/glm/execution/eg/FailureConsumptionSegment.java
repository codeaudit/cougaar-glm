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

import org.cougaar.domain.glm.execution.common.*;
import org.cougaar.domain.planning.ldm.measure.AbstractRate;

public class FailureConsumptionSegment
  extends Timed
  implements TimeConstants
{
  public String theSource;
  protected FailureConsumptionRateManager theFailureConsumptionRateManager;
  public FailureConsumptionRate theFailureConsumptionRate;
  public FailureConsumptionRate theOriginalFailureConsumptionRate;
  public FailureConsumptionPlugInItem thePlugInItem;
  private int theUnits = AbstractRate.PER_DAY;

  public FailureConsumptionSegment(String aSource,
                                   FailureConsumptionRate aFailureConsumptionRate,
                                   FailureConsumptionRateManager aFailureConsumptionRateManager)
  {
    theSource = aSource.intern();
    theFailureConsumptionRate = aFailureConsumptionRate;
    theFailureConsumptionRateManager = aFailureConsumptionRateManager;
    setPlugInItem(theFailureConsumptionRateManager.getPlugInItem(aFailureConsumptionRate, this, null));
    setEnabled(true);
  }

  public void setPlugInItem(FailureConsumptionPlugInItem newFailureConsumptionPlugInItem) {
    thePlugInItem = newFailureConsumptionPlugInItem;
  }

  public static Object getKey(FailureConsumptionRate fcr) {
    return fcr.theTaskUID;
  }

  public Object getKey() {
    return getKey(theFailureConsumptionRate);
  }

  public String getCluster() {
    return theSource;
  }

  /**
   **/
  public boolean expired(long time) {
    long schedulingTime =
      time + theFailureConsumptionRateManager
      .theEventGenerator
      .getSchedulingLookAhead();
    try {
      int q = (int) Math.floor(thePlugInItem.getQuantity(schedulingTime));
      if (q > 0) {
//          System.out.println(System.identityHashCode(this));
//          System.out.println("Consumed "
//                             + q
//                             + " "
//                             + theFailureConsumptionRate.theItemIdentification
//                             + " by "
//                             + theFailureConsumptionRate.theConsumer);
        FailureConsumptionReport theReport =
          new FailureConsumptionReport(theFailureConsumptionRate.theTaskUID,
                                       theFailureConsumptionRate.theItemIdentification,
                                       schedulingTime, schedulingTime, (double) q,
                                       theFailureConsumptionRate.theConsumer);
        theFailureConsumptionRateManager
          .enqueueFailureConsumptionReport(theSource, theReport);
      }
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
    long timeQuantum = thePlugInItem.getTimeQuantum(schedulingTime);
    timeQuantum = Math.max(timeQuantum, ONE_MINUTE);
    timeQuantum = Math.min(timeQuantum, ONE_DAY);
    theFailureConsumptionRate.theStartTime = time + timeQuantum;
    return (theFailureConsumptionRate.theStartTime >= theFailureConsumptionRate.theEndTime);
  }

  public long getTime() {
    return theFailureConsumptionRate.theStartTime;
  }

  public String getItem() {
    return theFailureConsumptionRate.theItemIdentification;
  }

  public int getUnits() {
    return theUnits;
  }

  public String toString() {
    return super.toString() + " for " + getItem();
  }
}
