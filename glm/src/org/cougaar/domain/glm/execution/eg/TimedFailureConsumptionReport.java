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
