/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.common;

import java.io.IOException;

/**
 * This is the interface that all objects passed between the event
 * generator and a cluster must implement. The methods insure that the
 * can be written to a LineWriter and recreated from a LineReader. In
 * addition, all classes implementing this interface must have a no
 * arg constructor.
 **/
public interface EGObject {
  static final Class[] egObjectClasses = {
    ExecutionTimeStatus.class,
    EGObjectArray.class,
    InventoryReport.class,
    InventoryReportSchedule.class,
    ReportSchedule.class,
    ExecutionWatcherParameters.class,
    InventoryReportParameters.class,
    FailureConsumptionRate.class,
    FailureConsumptionReport.class,
    TaskEventReport.class,
    ExecutionRate.class,
    SetExecutionTime.class,
    InventoryReportSchedule.Rescind.class,
    FailureConsumptionRate.Rescind.class,
    TaskEventReport.Rescind.class,
    ConstraintElement.class,
    TaskConstraintsRequest.class,
  };

  int getClassIndex();
  void write(LineWriter writer) throws IOException ;
  void read(LineReader reader) throws IOException ;
}
