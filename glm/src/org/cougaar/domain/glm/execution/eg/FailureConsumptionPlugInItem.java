package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

/**
 **/
public abstract class FailureConsumptionPlugInItem {
  public FailureConsumptionRate theFailureConsumptionRate;

  protected FailureConsumptionPlugInItem(FailureConsumptionRate aFailureConsumptionRate)
  {
    theFailureConsumptionRate = aFailureConsumptionRate;
  }

  /**
   * Get the failed/consumed quantity for a particular execution time.
   * @param executionTime the time at which consumption might occur.
   * @return the failed or consumed quantity.
   **/
  public abstract int getQuantity(long executionTime);

  /**
   * The interval that shoule elapse before invoking the plugin again
   **/
  public abstract long getTimeQuantum(long executionTime);
}
