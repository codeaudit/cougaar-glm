/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.report;

import javax.swing.JApplet;

import org.cougaar.util.ThemeFactory;

/**
 * ReportApplet  - invokes Applet form of ReportDisplay
 */

public class ReportApplet extends JApplet {

  public void init() {
    ThemeFactory.establishMetalTheme();
    
    ReportDisplay panel = 
      new ReportDisplay(getCodeBase().toString());
    getContentPane().add(panel);
  }
    
}










