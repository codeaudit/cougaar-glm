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
  public abstract AnnotatedDouble getQuantity(long executionTime);

  /**
   * The interval that shoule elapse before invoking the plugin again
   **/
  public abstract long getTimeQuantum(long executionTime);
}
