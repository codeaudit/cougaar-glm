package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

public class EGFailureConsumptionRateHandler {
  private FailureConsumptionRateManager theScheduleManager;

  public EGFailureConsumptionRateHandler(FailureConsumptionRateManager aScheduleManager) {
    theScheduleManager = aScheduleManager;
  }

  public void execute(String source, FailureConsumptionRate fcs) {
    execute(source, new FailureConsumptionRate[] {fcs});
  }

  public void execute(String source, FailureConsumptionRate[] fcss) {
    theScheduleManager.receiveFailureConsumptionRates(source, fcss);
  }

  public void execute(String source, FailureConsumptionRate.Rescind fcsr) {
    execute(source, new FailureConsumptionRate.Rescind[] {fcsr});
  }

  public void execute(String source, FailureConsumptionRate.Rescind[] fcssr) {
    theScheduleManager.receiveFailureConsumptionRates(source, fcssr);
  }
}

  
