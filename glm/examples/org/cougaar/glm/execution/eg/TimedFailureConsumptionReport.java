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
