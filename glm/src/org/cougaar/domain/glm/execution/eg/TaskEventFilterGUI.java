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
import org.cougaar.domain.glm.ldm.plan.AlpineAspectType;

public class TaskEventFilterGUI extends FilterGUI {
  public TaskEventFilterGUI(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public void addAspects(List aspects) {
    aspects.add(new FilterAspect.Time("Receive time",
                                      TimedTaskEventReport.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((TimedTaskEventReport) from)
                                           .theTaskEventReport.theReceivedDate,
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Time("Report time",
                                      TimedTaskEventReport.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((TimedTaskEventReport) from)
                                           .theTaskEventReport.theReportDate,
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Str_ng("Aspect Type",
                                        TimedTaskEventReport.class,
                                        FilterAspect.stringComparisons,
                                        AlpineAspectType.getAspectTypes()) {
      public Object getItem(Object from) {
        return ((TimedTaskEventReport) from).getAspectTypeString().toUpperCase();
      }
      public ParsedPattern parsePattern(String pattern) {
        pattern = TimedTaskEventReport.checkAspectTypeString(pattern.toUpperCase());
        if (pattern == null) return null;
        return super.parsePattern(pattern);
      }
    });
    aspects.add(new FilterAspect.Str_ng("Description",
                                        TimedTaskEventReport.class,
                                        FilterAspect.stringComparisons,
                                        null) {
      public Object getItem(Object from) {
        return (((TimedTaskEventReport) from)
                .theTaskEventReport
                .theShortDescription.toUpperCase());
      }
    });
  }
}
