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

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.core.util.UID;
import java.io.IOException;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.execution.common.*;

public class TimedTaskEventReport extends Timed {
  public TaskEventReport theTaskEventReport;
  public TaskEventReport theOriginalTaskEventReport;
  public String theSource;
  public EventGroup theEventGroup = null;

  private TaskEventReportManager theTaskEventReportManager;

  public static Object getKey(String aSource,
                              TaskEventReport aTaskEventReport)
  {
    return aTaskEventReport.theTaskEventId;
  }

  public Object getKey() {
    return getKey(theSource, theTaskEventReport);
  }

  protected int compareToTieBreaker(Timed o) {
    if (o == this) return 0;
    TimedTaskEventReport that = (TimedTaskEventReport) o;
    int diff;
    if (this.theEventGroup != that.theEventGroup) {
      if (this.theEventGroup == null) return 1;
      if (that.theEventGroup == null) return -1;
      diff = this.theEventGroup.theGroupId.compareTo(that.theEventGroup.theGroupId);
      if (diff != 0) return diff;
    }
    diff = theTaskEventReport.theTaskEventId.compareTo(that.theTaskEventReport.theTaskEventId);
    if (diff != 0)  return diff;
    throw new IllegalArgumentException("Different tter having equal keys");
//      return super.compareToTieBreaker(o);
  }

  public static String checkAspectTypeString(String pattern) {
    for (int aspectType = 0; aspectType <= AlpineAspectType.LAST_ALPINE_ASPECT; aspectType++) {
      String aspectString = AlpineAspectType.aspectTypeToString(aspectType);
      if (pattern.equals(aspectString)) {
        return aspectString;
      }
    }
    try {
      return "" + Integer.parseInt(pattern);
    } catch (RuntimeException re) {
      return null;
    }
  }

  public TimedTaskEventReport(String aSource,
                              TaskEventReport aTaskEventReport,
                              TaskEventReportManager aTaskEventReportManager)
  {
    theTaskEventReportManager = aTaskEventReportManager;
    theSource = aSource.intern();
    theTaskEventReport = aTaskEventReport;
    setEnabled(false);
  }

  public String getAspectTypeString() {
    return AlpineAspectType.aspectTypeToString(theTaskEventReport.theTaskEventId.theAspectType);
  }

  /**
   **/
  public boolean expired(long time) {
    try {
      theTaskEventReportManager.sendTaskEventReport(theSource, theTaskEventReport);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return true;		// Expired
  }
    
  public long getTime() {
    return theTaskEventReport.theReceivedDate;
  }
    
  public String getCluster() {
    return theSource;
  }

  public TaskEventReport getModifiableTaskEventReport() {
    if (theOriginalTaskEventReport == null) {
      theOriginalTaskEventReport = new TaskEventReport(theTaskEventReport);
    }
    return theTaskEventReport;
  }
}
