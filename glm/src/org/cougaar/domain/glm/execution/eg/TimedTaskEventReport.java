package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.core.society.UID;
import java.io.IOException;
import org.cougaar.domain.glm.plan.AlpineAspectType;
import org.cougaar.domain.glm.execution.common.*;

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
