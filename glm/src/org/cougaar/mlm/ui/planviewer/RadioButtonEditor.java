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
 
package org.cougaar.mlm.ui.planviewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

class RadioButtonEditor extends AbstractCellEditor {
        RadioButtonRenderer renderer = new RadioButtonRenderer();
        JRadioButton button = renderer.getRadioButton();
        public String shipName;

        public RadioButtonEditor() {}

        public Component getTableCellEditorComponent(
                                                                JTable table, Object value,
                                                                boolean isSelected,
                                                                int row, int column) {
                shipName = ((JRadioButton)value).getText();                                             
                System.out.println("Selected: " + shipName);
        return renderer.getTableCellRendererComponent(
                table, value, true, true, row, column);
        }
                public boolean stopCellEditing() {
                JRadioButton button = renderer.getRadioButton();
                JRadioButton b = new JRadioButton(button.getText(), button.isSelected()); 

                setCellEditorValue(b);
                return super.stopCellEditing();
        }

}
