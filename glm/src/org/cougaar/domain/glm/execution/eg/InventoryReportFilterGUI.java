package org.cougaar.domain.glm.execution.eg;

import java.util.List;

public class InventoryReportFilterGUI extends FilterGUI {
  public InventoryReportFilterGUI(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public void addAspects(List aspects) {
    aspects.add(new FilterAspect.Time("Receive time",
                                      TimedInventoryReport.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((TimedInventoryReport) from).theInventoryReport.theReceivedDate,
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Time("Report time",
                                      TimedInventoryReport.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((TimedInventoryReport) from).theInventoryReport.theReportDate,
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Str_ng("Item",
                                        TimedInventoryReport.class,
                                        FilterAspect.stringComparisons,
                                        new String[] {"", "Inventory:DODIC/", "Inventory:NSN/"}) {
      public Object getItem(Object from) {
        return ((TimedInventoryReport) from).getItem().toUpperCase();
      }
    });
    aspects.add(new FilterAspect.Do_ble("Quantity",
                                        TimedInventoryReport.class,
                                        FilterAspect.numericComparisons,
                                        null) {
      public Object getItem(Object from) {
        return new Double(((TimedInventoryReport) from)
                          .theInventoryReport
                          .theQuantity);
      }
    });
  }
}
