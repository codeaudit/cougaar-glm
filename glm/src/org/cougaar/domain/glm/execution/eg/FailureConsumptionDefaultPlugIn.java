package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;
import org.cougaar.domain.planning.ldm.measure.AbstractRate;
import org.cougaar.util.Random;

public class FailureConsumptionDefaultPlugIn implements FailureConsumptionPlugIn, TimeConstants {
  public static final long MIN_INTERVAL = ONE_HOUR;
  private static Random random = new Random();

  private class Item extends FailureConsumptionPlugInItem {
    long previousTime = 0L;

    public Item(FailureConsumptionRate aRate, long theExecutionTime) {
      super(aRate);
      previousTime = Math.max(theExecutionTime, aRate.theStartTime);
    }

    public int getQuantity(long executionTime) {
      if (executionTime - previousTime < MIN_INTERVAL) return 0;
      double qPerMilli = theFailureConsumptionRate.theRateValue / ONE_DAY;
      if (qPerMilli <= 0.0) return 0;
      long elapsed = executionTime - previousTime;
      previousTime = executionTime;
      return random.nextPoisson(elapsed * qPerMilli);
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
  public String getPlugInName() {
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
  public FailureConsumptionPlugInItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPlugInItem aFailureConsumptionPlugInItem)
  {
    if (aFailureConsumptionPlugInItem instanceof Item
        && aFailureConsumptionPlugInItem.theFailureConsumptionRate == aRate) {
      return aFailureConsumptionPlugInItem;
    }
    return new Item(aRate, theExecutionTime);
  }
}
