package org.cougaar.domain.glm.execution.eg;

import java.util.List;

public class InventoryScheduleFilterGUI extends FilterGUI {
  public InventoryScheduleFilterGUI(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public void addAspects(List aspects) {
    aspects.add(new FilterAspect.Time("Scheduled time",
                                      SteppedInventoryReportSchedule.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((SteppedInventoryReportSchedule) from).getTime(),
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Str_ng("Item",
                                        SteppedInventoryReportSchedule.class,
                                        FilterAspect.stringComparisons,
                                        new String[] {"", "Inventory:DODIC/", "Inventory:NSN/"}) {
      public Object getItem(Object from) {
        return ((SteppedInventoryReportSchedule) from).getItem().toUpperCase();
      }
    });
    // Add periodicity filter
  }
}
