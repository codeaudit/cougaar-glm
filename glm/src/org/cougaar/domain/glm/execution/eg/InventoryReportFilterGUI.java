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
