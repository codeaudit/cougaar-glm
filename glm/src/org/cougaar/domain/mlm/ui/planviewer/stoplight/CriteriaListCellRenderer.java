/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

import java.awt.*;

import javax.swing.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.data.UIUnitStatus;

public class CriteriaListCellRenderer extends DefaultListCellRenderer {
  static final String GRAY_LEGEND = "Not applicable";

  /** DefaultListCellRenderer extends JLabel so this simply
    sets the text, icon, foreground, background and alignment for JLabel.
    */

  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    String s = "";
    CriteriaParameters cp = (CriteriaParameters)value;
    String color = cp.getColor();
    if (color.equals(UIUnitStatus.GRAY))
      s = GRAY_LEGEND;
    else
      s = cp.getPerCentOperation() + " " +
        cp.getPerCent() + "% " +
        cp.getDaysLateOperation() + " " +
        cp.getDaysLate() + " days late ";
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    setText(s);
    setIcon(new ColoredSquare(color));
    setHorizontalAlignment(SwingConstants.LEFT);
    return this;
  }
}

    





