/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.eg;

public interface EGTableModel extends javax.swing.table.TableModel {
  int getPreferredColumnWidth(int col);
  int getMinColumnWidth(int col);
  int getMaxColumnWidth(int col);
  String getToolTipText(int row, int col);
  boolean cellHasBeenEdited(int row, int col);
  boolean cellIsMarked(int row, int col);
  boolean cellIsFirst(int row, int col);
  void cellSelectionChanged(int row, int col);
  int compareRowsByColumn(int row1, int row2, int column);
  void fireTableDataChanged();
}
