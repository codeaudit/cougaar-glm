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

import java.util.List;

public class FailureConsumptionReportFilterGUI extends FilterGUI {
  public FailureConsumptionReportFilterGUI(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public void addAspects(List aspects) {
    aspects.add(new FilterAspect.Time("Report time",
                                      TimedFailureConsumptionReport.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((Timed) from).getTime(), theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Str_ng("Item",
                                        TimedFailureConsumptionReport.class,
                                        FilterAspect.stringComparisons,
                                        new String[] {"", "DODIC/", "NSN/"}) {
      public Object getItem(Object from) {
        return ((TimedFailureConsumptionReport) from).getItem().toUpperCase();
      }
    });
    aspects.add(new FilterAspect.Do_ble("Quantity",
                                        TimedFailureConsumptionReport.class,
                                        FilterAspect.numericComparisons,
                                        null) {
      public Object getItem(Object from) {
        return new Double(((TimedFailureConsumptionReport) from)
                          .theFailureConsumptionReport
                          .theQuantity);
      }
    });
  }
}
