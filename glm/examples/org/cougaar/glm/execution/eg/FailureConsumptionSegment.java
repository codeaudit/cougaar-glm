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
package org.cougaar.glm.execution.eg;

import org.cougaar.glm.execution.common.*;
import org.cougaar.planning.ldm.measure.Duration;

public class FailureConsumptionSegment
  extends Timed
  implements TimeConstants
{
  public String theSource;
  protected FailureConsumptionRateManager theFailureConsumptionRateManager;
  public FailureConsumptionRate theFailureConsumptionRate;
  public FailureConsumptionRate theOriginalFailureConsumptionRate;
  public FailureConsumptionPluginItem thePluginItem;
  private int theUnits = Duration.DAYS;

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
