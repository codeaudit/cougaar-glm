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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.data.UIUnitStatus;

class ColoredSquare implements Icon {
  Color color;
  String colorName;

  public ColoredSquare(String s) {
    if (s.equals(UIUnitStatus.YELLOW))
      color = ThemeFactory.getALPYellow();
    else if (s.equals(UIUnitStatus.RED))
      color = ThemeFactory.getALPRed();
    else {
      color = Color.gray;
      colorName = UIUnitStatus.GRAY;
    }
    colorName = s;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color oldColor = g.getColor();
    g.setColor(color);
    g.fill3DRect(x,y,getIconWidth(), getIconHeight(), true);
    g.setColor(oldColor);
  }
  
  public int getIconWidth() { return 12; }

  public int getIconHeight() { return 12; }

  public String getColor() {
    return colorName;
  }
}
