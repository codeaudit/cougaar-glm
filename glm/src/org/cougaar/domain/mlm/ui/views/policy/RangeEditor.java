/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views.policy;


import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.cougaar.domain.mlm.ui.producers.policy.*;

/**
 * RangeEditor - displays the default value and entries for a range parameter.
 */

public class RangeEditor extends EntryParameterEditor {
  private static final int VALUE_COL = 0;
  private static final int MIN_COL = 1;
  private static final int MAX_COL = 2;

  private static final String []COLUMN_HEADERS = {"Value", "Min", "Max"};

  public static void showEditor(JFrame frame, UIRangeParameterInfo paramInfo) {
    JDialog dialog = new RangeEditor(frame, paramInfo);

    // modal dialog so show blocks
    dialog.show();
    dialog.dispose();
  }

  /**
   * Constructer - takes a PolicyTableModel
   *
   * @param model PolicyTableModel with the actual data
   */
  private RangeEditor(JFrame frame, UIRangeParameterInfo rangeInfo){
    super(frame, "Edit Range Parameter", rangeInfo);
  }

  protected DefaultTableModel initTableModel(UIPolicyParameterInfo policyInfo) {
    UIRangeParameterInfo rangeInfo = (UIRangeParameterInfo)policyInfo;
    ArrayList rangeEntries = rangeInfo.getRangeEntries();
    Object [][]dataArray = new Object[rangeEntries.size()][3];
    int row = 0;

    for (Iterator iterator = rangeEntries.iterator();
         iterator.hasNext();
         row++) {
      UIRangeEntryInfo entry = (UIRangeEntryInfo)iterator.next();
      dataArray[row] = new Object[3];
      dataArray[row][VALUE_COL] = entry.getValue();
      dataArray[row][MIN_COL] = new Integer(entry.getMin());
      dataArray[row][MAX_COL] = new Integer(entry.getMax());
    }

    return new DefaultTableModel(dataArray, COLUMN_HEADERS) {
      public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
          case MIN_COL:
          case MAX_COL:
            return Integer.class;
          default:
            return super.getColumnClass(columnIndex);
        }
      }
    };
  }

  protected boolean updatePolicyInfo() {
    ArrayList rangeEntries = new ArrayList();
    for (int row = 0; row < myEntryTableModel.getRowCount(); row++) {
      String value = (String)myEntryTableModel.getValueAt(row, VALUE_COL);

      Object minValue = myEntryTableModel.getValueAt(row, MIN_COL);
      int min;
      if (minValue instanceof Integer) {
        min = ((Integer)minValue).intValue();
      } else {
        try {
          min = Integer.parseInt(minValue.toString());
        } catch (NumberFormatException nfe) {
          JOptionPane.showMessageDialog(null,
                                        "Error parsing minimum - " + minValue,
                                        "Input Error",
                                        JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }

      Object maxValue = myEntryTableModel.getValueAt(row, MAX_COL);
      int max;
      if (maxValue instanceof Integer) {
        max = ((Integer)maxValue).intValue();
      } else {
        try {
          max = Integer.parseInt(maxValue.toString());
        } catch (NumberFormatException nfe) {
          JOptionPane.showMessageDialog(null,
                                        "Error parsing maximum - " + maxValue,
                                        "Input Error",
                                        JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
      
      if (min > max) {
        JOptionPane.showMessageDialog(null,
                                      "Invalid range. Minimum: " + 
                                      min + " must be less than maximum: " + max,
                                      "InputError",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
      }

      rangeEntries.add(new UIRangeEntryInfo(value, min, max));
    }

    myPolicyInfo.setValue(myDefaultField.getText());
    ((UIRangeParameterInfo)myPolicyInfo).setRangeEntries(rangeEntries);
    return true;
  }

}
    








