/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views.policy;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.cougaar.domain.mlm.ui.producers.policy.*;

/**
 * KeyEditor - displays the default value and entries for a key parameter.
 */

public class KeyEditor extends EntryParameterEditor {
  private static final int VALUE_COL = 0;
  private static final int KEY_COL = 1;

  private static final String []COLUMN_HEADERS = {"Value", "Key"};

  public static void showEditor(JFrame frame, UIKeyParameterInfo paramInfo) {
    JDialog dialog = new KeyEditor(frame, paramInfo);

    // modal dialog so show blocks
    dialog.show();
    dialog.dispose();
  }

  /**
   * Constructer - takes a PolicyTableModel
   *
   * @param model PolicyTableModel with the actual data
   */
  private KeyEditor(JFrame frame, UIKeyParameterInfo keyInfo){
    super(frame, "Edit Key Parameter", keyInfo);
  }
  
  protected DefaultTableModel initTableModel(UIPolicyParameterInfo policyInfo) {
    UIKeyParameterInfo keyInfo = (UIKeyParameterInfo)policyInfo;
    ArrayList keyEntries = keyInfo.getKeyEntries();
    Object [][]dataArray = new Object[keyEntries.size()][3];
    int row = 0;
    for (Iterator iterator = keyEntries.iterator();
         iterator.hasNext();
         row++) {
      UIKeyEntryInfo entry = (UIKeyEntryInfo)iterator.next();
      dataArray[row] = new Object[2];
      dataArray[row][VALUE_COL] = entry.getValue();
      dataArray[row][KEY_COL] = entry.getKey();
    }

    return new DefaultTableModel(dataArray, COLUMN_HEADERS);
  }
    
  protected boolean updatePolicyInfo() {
    ArrayList keyEntries = new ArrayList();
    for (int row = 0; row < myEntryTableModel.getRowCount(); row++) {
      String value = (String)myEntryTableModel.getValueAt(row, VALUE_COL);
      String key = (String)myEntryTableModel.getValueAt(row, KEY_COL);
      keyEntries.add(new UIKeyEntryInfo(value, key));
    }

    myPolicyInfo.setValue(myDefaultField.getText());
    ((UIKeyParameterInfo)myPolicyInfo).setKeyEntries(keyEntries);
    return true;
  }
}








