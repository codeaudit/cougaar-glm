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
import org.cougaar.util.Random;

public class FailureConsumptionDefaultPlugin implements FailureConsumptionPlugin, TimeConstants {
  public static final long MIN_INTERVAL = ONE_HOUR;
  private static Random random = new Random();

  private class Item extends FailureConsumptionPluginItem {
    long previousTime = 0L;

    public Item(FailureConsumptionRate aRate, long theExecutionTime) {
      super(aRate);
      previousTime = Math.max(theExecutionTime, aRate.theStartTime);
    }

    public AnnotatedDouble getQuantity(long executionTime) {
      if (executionTime - previousTime < MIN_INTERVAL) return new AnnotatedDouble(0.0);
      double qPerMilli = theFailureConsumptionRate.theRateValue / ONE_DAY;
      if (qPerMilli <= 0.0) return new AnnotatedDouble(0.0);
      long elapsed = executionTime - previousTime;
      previousTime = executionTime;
      return new AnnotatedDouble(random.nextPoisson(elapsed * qPerMilli));
    }

    /**
     * Get a good time quantum for this failure/consumption rate.
     * Tries to not let qPerMilli drop below 0.001 but at least once
     * per day and no more often than once per hour.
     * @param executionTime the current execution time (not used)
     **/
    public long getTimeQuantum(long executionTime) {
      double qPerMilli = theFailureConsumptionRate.theRateValue / ONE_DAY;
      return (long) Math.min(ONE_DAY, Math.max(.001 / qPerMilli, MIN_INTERVAL));
    }
  }

  /**
   * @return the name of this plugin
   **/
  public String getPluginName() {
    return "Default";
  }

  /**
   * A brief description of this plugin.
   **/
  public String getDescription() {
    return "Default plugin fails/consumes integer amounts at the exact rate";
  }

  public void setParameter(String parameter) {
    // No parameter needed
  }

  /**
   * Thus plugin is not configurable.
   **/
  public boolean isConfigurable() {
    return false;
  }

  /**
   * This plugin is not configurable, so this method does nothing.
   **/
  public void configure(java.awt.Component c) {}

  public void save(java.util.Properties props, String prefix) {
  }

  public void restore(java.util.Properties props, String prefix) {
  }

 public void setEventGenerator(EventGenerator eg) {
 }

  /**
   * Create a FailureConsumptionItem for this plugin to handle a
   * particular FailureConsumptionRate.
   **/
  public FailureConsumptionPluginItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPluginItem aFailureConsumptionPluginItem)
  {
    if (aFailureConsumptionPluginItem instanceof Item
        && aFailureConsumptionPluginItem.theFailureConsumptionRate == aRate) {
      return aFailureConsumptionPluginItem;
    }
    return new Item(aRate, theExecutionTime);
  }
}
