package org.cougaar.domain.glm.execution.eg;

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
