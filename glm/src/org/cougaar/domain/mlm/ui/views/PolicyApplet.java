/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views;

import java.applet.Applet;
import java.net.URL;

import javax.swing.JApplet;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.producers.ClusterCache;
import org.cougaar.domain.mlm.ui.views.policy.PolicyView;

/**
 * This runs the policy editor as an applet, within the browser's frame
 */

public class PolicyApplet extends JApplet {
  
  public void init() {

    ThemeFactory.establishMetalTheme();

    // Load the cluster urls
    ClusterCache.initCache(getCodeBase());

    getContentPane().add(new PolicyView());
  }  
  
  
}
