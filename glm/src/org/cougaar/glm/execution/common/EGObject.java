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
package org.cougaar.glm.execution.common;

import java.io.IOException;

/**
 * This is the interface that all objects passed between the event
 * generator and a cluster must implement. The methods insure that the
 * can be written to a LineWriter and recreated from a LineReader. In
 * addition, all classes implementing this interface must have a no
 * arg constructor.
 **/
public interface EGObject {
  Class[] egObjectClasses = {
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
