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

import org.cougaar.glm.execution.common.*;
import org.cougaar.planning.ldm.measure.AbstractRate;

public class FailureConsumptionSegment
  extends Timed
  implements TimeConstants
{
  public String theSource;
  protected FailureConsumptionRateManager theFailureConsumptionRateManager;
  public FailureConsumptionRate theFailureConsumptionRate;
  public FailureConsumptionRate theOriginalFailureConsumptionRate;
  public FailureConsumptionPluginItem thePluginItem;
  private int theUnits = AbstractRate.PER_DAY;

  public FailureConsumptionSegment(String aSource,
                                   FailureConsumptionRate aFailureConsumptionRate,
                                   FailureConsumptionRateManager aFailureConsumptionRateManager)
  {
    theSource = aSource.intern();
    theFailureConsumptionRate = aFailureConsumptionRate;
    theFailureConsumptionRateManager = aFailureConsumptionRateManager;
    setPluginItem(theFailureConsumptionRateManager.getPluginItem(aFailureConsumptionRate, this, null));
    setEnabled(true);
  }

  public void setPluginItem(FailureConsumptionPluginItem newFailureConsumptionPluginItem) {
    thePluginItem = newFailureConsumptionPluginItem;
  }

  public static Object getKey(FailureConsumptionRate fcr) {
    return fcr.theTaskUID;
  }

  public Object getKey() {
    return getKey(theFailureConsumptionRate);
  }

  protected int compareToTieBreaker(Timed other) {
    if (other instanceof FailureConsumptionSegment) {
      FailureConsumptionSegment that = (FailureConsumptionSegment) other;
      long diff = this.theFailureConsumptionRate.theEndTime - that.theFailureConsumptionRate.theEndTime;
      if (diff < 0) return -1;
      if (diff > 0) return 1;
    }
    return super.compareToTieBreaker(other);
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
      AnnotatedDouble quantity = thePluginItem.getQuantity(schedulingTime);
      int q = (int) Math.floor(quantity.value);
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
          .enqueueFailureConsumptionReport(theSource, theReport, quantity.annotation);
      }
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
    long timeQuantum = thePluginItem.getTimeQuantum(schedulingTime);
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
