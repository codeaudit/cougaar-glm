package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

public interface FailureConsumptionPlugIn extends PlugIn {
  /**
   * Create a FailureConsumptionItem for this plugin to handle a
   * particular FailureConsumptionRate.
   * @param aRate the FailureConsumptionRate to watch
   * @param theExecutionTime the base time from which
   * failures/consumptions should be computed (current execution
   * time).
   * @param aFailureConsumptionPlugInItem a pre-existing plugin item
   * that may suffice. May be null if there is no pre-existing item.
   **/
  FailureConsumptionPlugInItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPlugInItem aFailureConsumptionPlugInItem);
}
