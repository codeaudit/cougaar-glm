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
