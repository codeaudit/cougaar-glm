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
 
package org.cougaar.mlm.ui.planviewer.inventory;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.cougaar.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.data.UIQuantityScheduleElement;

/** 
 * .<pre>
 * Usage:
 *     JTable table = new JTable(new InventoryTableModel(UISimpleInventory)));
 *     JScrollPane scrollpane = new JScrollPane(table);
 * </pre>
 */

public class InventoryTableModel extends AbstractTableModel {
  UISimpleInventory inventory;
  int rowCount;
  Vector rows;
  DateFormat dateTimeFormater = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
  NumberFormat qtyFormater = NumberFormat.getInstance();

  public InventoryTableModel(UISimpleInventory inventory) {
    this.inventory = inventory;

    dateTimeFormater.setCalendar(new InventoryChartBaseCalendar());

    qtyFormater.setMaximumIntegerDigits(10);
    qtyFormater.setMinimumIntegerDigits(2);
    qtyFormater.setMinimumFractionDigits(2);
    qtyFormater.setMaximumFractionDigits(2);
    qtyFormater.setGroupingUsed(false);

    rows = new Vector();
    rows.addElement(new ScheduleTableEntry(inventory.getScheduleType(),
                                           inventory.getAssetName()));

    Vector v = inventory.getSchedules();
    for (int i = 0; i < v.size(); i++) {
      UISimpleNamedSchedule namedSchedule = 
        (UISimpleNamedSchedule)v.elementAt(i);
      rows.addElement(new ScheduleTableEntry(getName(namedSchedule.getName())));
      Vector s = namedSchedule.getSchedule();
      for (int j = 0; j < s.size(); j++) {
	UIQuantityScheduleElement schedule = (UIQuantityScheduleElement)s.elementAt(j);
	rows.addElement(new ScheduleTableEntry(schedule.getQuantity(),
					       schedule.getStartTime(),
					       schedule.getEndTime()));
      }
    }
    rowCount = rows.size();
  }

  private String getName(String scheduleName) {
    if (scheduleName.equals(UISimpleNamedSchedule.UNCONFIRMED_DUE_IN))
      return "Qty May Receive";
    if (scheduleName.equals(UISimpleNamedSchedule.DUE_IN))
      return "Qty Received";
    else if (scheduleName.equals(UISimpleNamedSchedule.DUE_OUT))
        return "Qty Shipped or Consumed";
    else if (scheduleName.equals(UISimpleNamedSchedule.REQUESTED_DUE_IN))
      return "Restock Qty";
    else if (scheduleName.equals(UISimpleNamedSchedule.REQUESTED_DUE_OUT))
      return "Requisition Qty";
    return scheduleName;
  }

    private String shortDate(long time) {
	if (time < 0) return "";
	String sdate = dateTimeFormater.format(new Date(time));
	// map '9/8/00 12:00 AM' to ' 9/8/00 12:00 AM'
	while(sdate.length()<17){
	    sdate = " "+sdate;
	}
	return sdate;
    }

  public int getColumnCount() { 
    return 3;
  }

  public int getRowCount() { 
    return rowCount;
  }
    
  public Object getValueAt(int row, int col) { 
    ScheduleTableEntry s = (ScheduleTableEntry)rows.elementAt(row);
    // first column is either name or quantity
    if (col == 0) {
      if (s.name != null)
        return s.name;
      else 
        return qtyFormater.format(s.quantity);
    } else if (col == 1) {
      // second column is either asset name or start time
      if (s.assetName != null)
        return s.assetName;
      else 
	return shortDate(s.startTime);
    } else if (col == 2) {
      // third column is end time
      return shortDate(s.endTime);
    } else
      return null;
  }

}

  class ScheduleTableEntry {
    String name;
    String assetName;
    double quantity;
    long startTime;
    long endTime;

    public ScheduleTableEntry(String name, String assetName) {
      this.name = name;
      this.assetName = assetName;
      quantity = 0;
      startTime = -1;
      endTime = -1;
    }

    public ScheduleTableEntry(String name) {
      this(name, null);
    }

    public ScheduleTableEntry(double quantity, long startTime, long endTime) {
      name = null;
      assetName = null;
      this.quantity = quantity;
      this.startTime = startTime;
      this.endTime = endTime;
    }
  }
