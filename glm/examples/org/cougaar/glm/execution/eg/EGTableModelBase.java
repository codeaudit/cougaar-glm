/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.glm.execution.eg;

import javax.swing.table.AbstractTableModel;
import javax.swing.JLabel;

/**
 * Augment the standard TableModel interface to include column layout info.
 **/
public abstract class EGTableModelBase extends AbstractTableModel {
  public static int SEND_WIDTH;
  public static int DATE_WIDTH;
  public static int STEP_WIDTH;
  public static int ITEM_WIDTH;
  public static int CONSUMER_WIDTH;
  public static int QUANTITY_WIDTH;
  public static int CLUSTER_WIDTH;

  static {
    SEND_WIDTH = getCellWidth("Send");
    DATE_WIDTH = getCellWidth("12/12/2000 23:59 (MMM)");
    STEP_WIDTH = getCellWidth("MONTHLY");
    CONSUMER_WIDTH = getCellWidth("M1A1 120 MM Tank Combat Full TrackXXX");
    ITEM_WIDTH = getCellWidth("NSN/XXXXXXXXXXXXX");
    QUANTITY_WIDTH = getNumericCellWidth("10,000,000.000");
    CLUSTER_WIDTH = getCellWidth("XXXXXXXXXXXXXXX");
    setWidths();
  }

  public static int getCellWidth(String s) {
    JLabel label = new JLabel(s);
    label.setFont(ReportManagerGUI.getFirstFont());
    return label.getPreferredSize().width;
  }

  public static int getNumericCellWidth(String s) {
    JLabel label = new JLabel(s);
    label.setFont(ReportManagerGUI.getFirstNumberFont());
    return label.getPreferredSize().width;
  }

  public static void setWidths() {
  }

  /**
   * Default implementation of compareRowsByColumn compares class
   * names if the objects have different classes. Otherwise uses
   * compareTo if both are Comparable. Otherwise, compares the
   * toString values of the objects.
   **/
  public int compareRowsByColumn(int row1, int row2, int column) {
    Object o1 = getValueAt(row1, column);
    Object o2 = getValueAt(row2, column);
    if (o1 == null) {
      if (o2 == null) return 0;
      return -1;
    } else if (o2 == null) {
      return 1;
    }
    if (o1.getClass() != o2.getClass()) {
      return o1.getClass().getName().compareTo(o2.getClass().getName());
    }
    if (o1 instanceof Comparable) {
      return ((Comparable) o1).compareTo(o2);
    }
    return o1.toString().compareTo(o2.toString());
  }

  public String getToolTipText(int row, int col) {
    return null;
  }
  public boolean cellIsMarked(int row, int col) {
    return false;
  }
  public void cellSelectionChanged(int row, int col) {
  }

  public boolean cellIsFirst(int row, int col) {
    if (row == 0) return true;
    Object thisValue = getValueAt(row, col);
    if (thisValue == null) return true;
    Object prevValue = getValueAt(row-1, col);
    if (prevValue == null) return true;
    return !thisValue.equals(prevValue);
  }
}

