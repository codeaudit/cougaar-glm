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
 
package org.cougaar.domain.mlm.ui.views.policy;

import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.cougaar.domain.mlm.ui.producers.policy.*;

/**
 * EntryParameterEditor - displays the default value and entries for a key parameter.
 */

abstract public class EntryParameterEditor extends JDialog {
  protected DefaultTableModel myEntryTableModel;
  protected JTextField myDefaultField;
  protected JTable myEntryTable;
  protected UIPolicyParameterInfo myPolicyInfo;
  protected JButton myCommitButton;
  protected JButton myCancelButton;

  protected boolean myEditable = true;

  /**
   * Constructer - takes a PolicyTableModel
   *
   * @param model PolicyTableModel with the actual data
   */
  protected EntryParameterEditor(JFrame frame, String title,
                                 UIPolicyParameterInfo policyInfo){
    super(frame, title, true);

    myPolicyInfo = policyInfo;
   
    initDisplay();
    initDefaultEditors();

    pack();
  }

  protected boolean getEditable() {
    return myEditable;
  }

  protected void setEditable(boolean editable) {
    myEditable = editable;

    if (myCommitButton != null) {
      myCommitButton.setEnabled(editable);
    }

    if (myDefaultField != null) {
      myDefaultField.setEditable(editable);
    }
  }

  /**
   * initDefaultEditors - set up default editors for parameters. 
   * Install editors for Integer and Double fields. Use JTable default for 
   * all else. Other default editors should be added here as needed.
   */
  private void initDefaultEditors() {
    final IntegerField integerField = new IntegerField(0, 5);
    integerField.setHorizontalAlignment(IntegerField.RIGHT);
    
    DefaultCellEditor integerEditor = 
      new EntryCellEditor(integerField) {
        //Override DefaultCellEditor's getCellEditorValue method
        //to return an Integer, not a String:
        public Object getCellEditorValue() {
          return new Integer(integerField.getValue());
        }
      };
    myEntryTable.setDefaultEditor(Integer.class, integerEditor);

    final DoubleField doubleField = new DoubleField(0, 10, 
                                                    new DecimalFormat());
    doubleField.setHorizontalAlignment(DoubleField.RIGHT);
    
    DefaultCellEditor decimalEditor = 
      new EntryCellEditor(doubleField) {
        //Override DefaultCellEditor's getCellEditorValue method
        //to return an Double, not a String:
        public Object getCellEditorValue() {
          return new Double(doubleField.getValue());
        }
      };
    myEntryTable.setDefaultEditor(Double.class, decimalEditor);

  }

  private class EntryCellEditor extends DefaultCellEditor {
    
    public EntryCellEditor(JTextField textField) {
      super(textField);
    }

    public boolean stopCellEditing() {
      boolean success;

      try {
        success = super.stopCellEditing();
      } catch (UIParameterInfoIllegalValueException piive) {
        success = false;
        
        //Make sure that current editor is removed
        myEntryTable.removeEditor();

        JOptionPane.showMessageDialog(null,
                                      piive.getMessage(),
             
                         "Input Error",
                                      JOptionPane.ERROR_MESSAGE);
      }

      return success;
    }
  } // end EntryCellEditor
  
  private void initDisplay() {
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    JPanel defaultPanel = new JPanel();
    defaultPanel.add(new JLabel("Default Value: "));
    
    myDefaultField = new JTextField((String)myPolicyInfo.getValue(), 15);
    myDefaultField.setEditable(getEditable());
    defaultPanel.add(myDefaultField);
    getContentPane().add(defaultPanel);

    JPanel tablePanel = new JPanel();

    myEntryTableModel = initTableModel(myPolicyInfo);
    myEntryTable = new JTable(myEntryTableModel);
      
    // Adjust row height to account for font size.
    Font tableFont = UIManager.getFont("Table.font");
    FontMetrics fontMetrics = myEntryTable.getFontMetrics(tableFont);
    int rowHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
    
    // fudge factor
    rowHeight += (.33) * rowHeight;
    
    myEntryTable.setRowHeight(Math.max(myEntryTable.getRowHeight(), 
                                       rowHeight));
    //myEntryTable.setPreferredScrollableViewportSize(new Dimension(250, 250));
    getContentPane().add(new JScrollPane(myEntryTable));

    JPanel buttonPanel = new JPanel();
    myCommitButton = new JButton("Commit");
    myCommitButton.setActionCommand("Commit");
    myCommitButton.addActionListener(new CommitListener());
    myCommitButton.setEnabled(getEditable());
    buttonPanel.add(myCommitButton);

    myCancelButton = new JButton("Cancel");
    myCancelButton.addActionListener(new CancelListener());
    buttonPanel.add(myCancelButton);
    getContentPane().add(buttonPanel);
  }

  abstract protected DefaultTableModel initTableModel(UIPolicyParameterInfo policyInfo);
  
  private class CommitListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      DefaultCellEditor cellEditor = 
        (DefaultCellEditor)myEntryTable.getCellEditor(); 
      
      // Submit all table changes - saves user from having to type carriage 
      // return.
      if (!(cellEditor == null) &&
          !cellEditor.stopCellEditing()){ 
        return;
      } else {
        //Editor can't be reused after call to stopCellEditing. 
        myEntryTable.removeEditor();
      }

      if (updatePolicyInfo()) {
        hide();
      }
    }
  }

  public void show() {
    super.show();
  }

  public void hide() {
    super.hide();
  }

  abstract protected boolean updatePolicyInfo();

  private class CancelListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      dispose();
    }
  }
}
    








