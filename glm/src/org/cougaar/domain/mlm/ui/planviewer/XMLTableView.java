/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
