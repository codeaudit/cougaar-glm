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
 
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import java.awt.Color;
import java.util.Hashtable;

import org.cougaar.domain.mlm.ui.data.UISimpleNamedSchedule;

public class InventoryColorTable {
  Hashtable colorTable;

  public InventoryColorTable() {
    colorTable = new Hashtable();
    twoPut(UISimpleNamedSchedule.ON_HAND, 
		   new Color(255, 255, 204)); // light yellow
    //    twoPut(UISimpleNamedSchedule.DUE_IN, 
    //             new Color(102, 51, 255));  // blue
    //    twoPut(UISimpleNamedSchedule.DUE_OUT, 
    //             new Color(0, 204, 0));     // green
    //    twoPut(UISimpleNamedSchedule.REQUESTED_DUE_IN, 
    //             new Color(153, 153, 255)); // light blue
    //    twoPut(UISimpleNamedSchedule.REQUESTED_DUE_OUT, 
    //             new Color(153, 255, 153)); // light green
    twoPut(UISimpleNamedSchedule.REQUESTED_DUE_IN, 
		   new Color(160, 160, 255)); // light blue
    twoPut(UISimpleNamedSchedule.DUE_IN, 
                   new Color(0, 0, 150));  // blue
    twoPut(InventoryShortfallChartDataModel.UNCONFIRMED_DUE_IN_SHORTFALL_LABEL,
	           new Color(100,100,225));
    twoPut(InventoryShortfallChartDataModel.CONFIRMED_DUE_IN_SHORTFALL_LABEL,
	           new Color(40,40,200));

    twoPut(UISimpleNamedSchedule.PROJECTED_DUE_IN, 
	   //                new Color(0, 100, 100));     //light blue - green 
	           new Color(150,0,150));
    twoPut(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_IN, 
	   //               new Color(100,220,220));     // light blue-green
	           new Color(240,100,240));
    twoPut(InventoryShortfallChartDataModel.PROJECTED_DUE_IN_SHORTFALL_LABEL,
	           new Color(160, 40, 160));

    twoPut(UISimpleNamedSchedule.DUE_OUT, 
                   new Color(0, 100, 0));     // green
    twoPut(UISimpleNamedSchedule.REQUESTED_DUE_OUT, 
		   new Color(120, 225, 120)); // light green
    twoPut(InventoryShortfallChartDataModel.DUE_OUT_SHORTFALL_LABEL,
	           new Color(20,140,20));

    twoPut(UISimpleNamedSchedule.PROJECTED_DUE_OUT, 
                   new Color(255,180,0));     // orange
    twoPut(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_OUT, 
	           new Color(255, 210, 120));     // light orange
    twoPut(InventoryShortfallChartDataModel.PROJECTED_DUE_OUT_SHORTFALL_LABEL,
	           new Color(255,150,50));

    //    twoPut(UISimpleNamedSchedule.TOTAL, 
    //             new Color(0, 153, 0));     // forest green
    twoPut(UISimpleNamedSchedule.ALLOCATED, 
           new Color(127, 127, 255)); // blue
    twoPut(UISimpleNamedSchedule.TOTAL_LABOR_8, 
           new Color(0, 153, 0));     // darkest green
    twoPut(UISimpleNamedSchedule.TOTAL_LABOR_10, 
           new Color(0, 200, 0));     // medium green
    twoPut(UISimpleNamedSchedule.TOTAL_LABOR_12, 
           new Color(0, 225, 0));     // lightest green
  }

  private void twoPut(String key, Color value) {
    colorTable.put(key, value);
    colorTable.put(key + UISimpleNamedSchedule.INACTIVE, value);
  }

  public Color get(String s) {
    if (colorTable.get(s) == null)
      return Color.white;
    return (Color) colorTable.get(s);
  }

}

