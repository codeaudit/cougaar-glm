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
