/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/component/Attic/RowLabel.java,v 1.1 2001-02-22 22:42:22 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Harry Tsai
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.component;

import java.awt.Color;


public class RowLabel {
  private String label = "";
  private Color background = null;
  private Color foreground = null;

  public RowLabel(String l) {
    label = l;
  }

  public String getLabel() { return label; }
  public void setLabel(String l) { label = l; }
  public Color getForeground() { return foreground; }
  public Color getBackground() { return background; }
  public void setForeground(Color c) { foreground = c; }
  public void setBackground(Color c) { background = c; }
}
