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
 
package org.cougaar.mlm.ui.planviewer.stoplight;

import java.awt.*;

import javax.swing.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.mlm.ui.data.UIUnitStatus;

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

    





