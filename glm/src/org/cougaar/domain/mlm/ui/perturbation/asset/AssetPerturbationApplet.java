/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.perturbation.asset;

import javax.swing.JApplet;
import javax.swing.JRootPane;

import org.cougaar.util.ThemeFactory;

/**
 * AssetPerturbationApplet  - invokes Applet form of AssetPerturbationDisplay
 */

public class AssetPerturbationApplet extends JApplet {

  public void init() {
    ThemeFactory.establishMetalTheme();
    
    AssetPerturbationDisplay panel = 
      new AssetPerturbationDisplay(getCodeBase().toString());
    getContentPane().add(panel);
  }
    
}

