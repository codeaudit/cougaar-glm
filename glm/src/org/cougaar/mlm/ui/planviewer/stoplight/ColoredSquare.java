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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import org.cougaar.util.ThemeFactory;

import org.cougaar.mlm.ui.data.UIUnitStatus;

class ColoredSquare implements Icon {
  Color color;
  String colorName;

  public ColoredSquare(String s) {
    if (s.equals(UIUnitStatus.YELLOW))
      color = ThemeFactory.getCougaarYellow();
    else if (s.equals(UIUnitStatus.RED))
      color = ThemeFactory.getCougaarRed();
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
