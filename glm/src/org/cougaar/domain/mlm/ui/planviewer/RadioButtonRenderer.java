/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.*;
import java.lang.String;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

class RadioButtonRenderer extends DefaultTableCellRenderer
                                                  //implements TableCellRenderer
{
        private JRadioButton button = new JRadioButton();

        public RadioButtonRenderer() {
                setLayout(new BorderLayout());
                button.setOpaque(true);
                button.setForeground(Color.black);
                button.setBackground(Color.white);
                add(button, BorderLayout.CENTER);

        }
        public Component getTableCellRendererComponent(
                                                                JTable table, Object value,
                                                                boolean isSelected,
                                                                boolean hasFocus,
                                                                int row, int col) {
           JRadioButton b = ((JRadioButton)value);
       button.setText(b.getText());
           return this;
        }
        public JRadioButton getRadioButton() {
                return button;
        }
}
