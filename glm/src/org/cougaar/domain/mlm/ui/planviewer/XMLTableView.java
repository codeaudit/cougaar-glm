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
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.Dimension;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class XMLTableView extends JFrame {

  public XMLTableView(String title, String logPlanObjectClassName, byte[] buffer) {
    super(title);
    TableModel model = null;
    if (logPlanObjectClassName.equals("Task"))
      model = new XMLTaskTableModel(buffer);
    else if (logPlanObjectClassName.equals("Allocation"))
      model = new XMLAllocationTableModel(buffer);
    else
      return;
    if (model.getRowCount() == 0)
      return; // don't display empty tables for now

    // define table sorter
    TableSorter sorter = new TableSorter(model);
    JTable table = new JTable(sorter);
    sorter.addMouseListenerToHeaderInTable(table);

    // define cell renderer for date columns
    DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
      public void setValue(Object value) {
        setText(((Date)value).toString());
      }
    };
    table.setDefaultRenderer(Date.class, dateRenderer);

    table.setPreferredScrollableViewportSize(new Dimension(100, 50));
    getContentPane().add("Center", new JScrollPane(table));
    setSize(800, 200);
  }

}
