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
package org.cougaar.glm.execution.eg;

import org.cougaar.glm.execution.common.*;
import java.io.IOException;

public class TimedFailureConsumptionReport extends Timed {
  public String theSource;
  protected FailureConsumptionReportManager theFailureConsumptionReportManager;
  public FailureConsumptionReport theFailureConsumptionReport;
  public FailureConsumptionReport theOriginalFailureConsumptionReport;

  public TimedFailureConsumptionReport(String aSource,
                                       FailureConsumptionReport aFailureConsumptionReport,
                                       FailureConsumptionReportManager aFailureConsumptionReportManager,
                                       Object annotation)
  {
    theSource = aSource.intern();
    theFailureConsumptionReportManager = aFailureConsumptionReportManager;
    theFailureConsumptionReport = aFailureConsumptionReport;
    setAnnotation(annotation);
    setEnabled(false);
  }

  public Object getKey() {
    return null;
  }

  public String getCluster() {
    return theSource;
  }

  /**
   **/
  public boolean expired(long time) {
    try {
      theFailureConsumptionReportManager
        .sendFailureConsumptionReport(theSource, theFailureConsumptionReport);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return true;		// Expired
  }

  public long getTime() {
    return theFailureConsumptionReport.theReceivedDate;
  }

  public String getItem() {
    return theFailureConsumptionReport.theItemIdentification;
  }

  public String toString() {
    return super.toString() + " for " + getItem();
  }
}
