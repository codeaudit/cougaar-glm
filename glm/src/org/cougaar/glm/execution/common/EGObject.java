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
