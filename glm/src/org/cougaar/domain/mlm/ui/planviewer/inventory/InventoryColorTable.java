/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
    colorTable.put(UISimpleNamedSchedule.ON_HAND, 
		   new Color(255, 255, 204)); // light yellow
    //    colorTable.put(UISimpleNamedSchedule.DUE_IN, 
    //             new Color(102, 51, 255));  // blue
    //    colorTable.put(UISimpleNamedSchedule.DUE_OUT, 
    //             new Color(0, 204, 0));     // green
    //    colorTable.put(UISimpleNamedSchedule.REQUESTED_DUE_IN, 
    //             new Color(153, 153, 255)); // light blue
    //    colorTable.put(UISimpleNamedSchedule.REQUESTED_DUE_OUT, 
    //             new Color(153, 255, 153)); // light green
    colorTable.put(UISimpleNamedSchedule.DUE_IN, 
                   new Color(0, 0, 150));  // blue
    colorTable.put(UISimpleNamedSchedule.DUE_OUT, 
                   new Color(0, 100, 0));     // green
    colorTable.put(UISimpleNamedSchedule.PROJECTED_DUE_OUT, 
                   new Color(255,180,0));     // orange
    colorTable.put(UISimpleNamedSchedule.REQUESTED_DUE_IN, 
		   new Color(160, 160, 255)); // light blue
    colorTable.put(UISimpleNamedSchedule.REQUESTED_DUE_OUT, 
		   new Color(120, 225, 120)); // light green
    colorTable.put(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_OUT, 
                   new Color(255, 210, 120));     // light orange
    
    //    colorTable.put(UISimpleNamedSchedule.TOTAL, 
    //             new Color(0, 153, 0));     // forest green
    colorTable.put(UISimpleNamedSchedule.ALLOCATED, 
                   new Color(127, 127, 255)); // blue
    colorTable.put(UISimpleNamedSchedule.TOTAL_LABOR_8, 
                   new Color(0, 153, 0));     // darkest green
    colorTable.put(UISimpleNamedSchedule.TOTAL_LABOR_10, 
                   new Color(0, 200, 0));     // medium green
    colorTable.put(UISimpleNamedSchedule.TOTAL_LABOR_12, 
                   new Color(0, 225, 0));     // lightest green
  }

  public Color get(String s) {
    if (colorTable.get(s) == null)
      return Color.white;
    return (Color)colorTable.get(s);
  }

}

