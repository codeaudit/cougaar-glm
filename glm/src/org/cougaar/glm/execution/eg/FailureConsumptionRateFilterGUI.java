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

import java.util.List;

public class FailureConsumptionRateFilterGUI extends FilterGUI {
  public FailureConsumptionRateFilterGUI(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public void addAspects(List aspects) {
    aspects.add(new FilterAspect.Time("Start time",
                                      FailureConsumptionSegment.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((Timed) from).getTime(), theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Time("End time",
                                      FailureConsumptionSegment.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((FailureConsumptionSegment) from).theFailureConsumptionRate.theEndTime,
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Str_ng("Item",
                                        FailureConsumptionSegment.class,
                                        FilterAspect.stringComparisons,
                                        new String[] {"", "DODIC/", "NSN/"}) {
      public Object getItem(Object from) {
        return ((FailureConsumptionSegment) from).getItem().toUpperCase();
      }
    });
    aspects.add(new FilterAspect.Do_ble("Rate",
                                        FailureConsumptionSegment.class,
                                        FilterAspect.numericComparisons,
                                        null) {
      public Object getItem(Object from) {
        FailureConsumptionSegment fcs = (FailureConsumptionSegment) from;
        return new Double(fcs.theFailureConsumptionRate.theRateValue);
      }
    });
    String[] rateUnits = {
      "",
      "Gallons/Day",
      "Eaches/Day",
    };
    aspects.add(new FilterAspect.Str_ng("Units",
                                        FailureConsumptionSegment.class,
                                        FilterAspect.stringComparisons,
                                        rateUnits) {
      public Object getItem(Object from) {
        FailureConsumptionSegment fcs = (FailureConsumptionSegment) from;
        return fcs.theFailureConsumptionRate.theRateUnits;
      }
    });
  }
}
