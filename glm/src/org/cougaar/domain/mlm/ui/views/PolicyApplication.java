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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.producers.ClusterCache;
import org.cougaar.domain.mlm.ui.views.policy.PolicyView;

/**
 * This runs the policy view as an application, not applet
 */

public class PolicyApplication {
  
  public static void main(String args[]) {
    ThemeFactory.establishMetalTheme();

    // Load the cluster urls
    ClusterCache.initCache(null);

    // Create the views
    JFrame policyFrame = new JFrame("Policy View");
    policyFrame.getContentPane().add(new PolicyView());
    policyFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    policyFrame.pack();
    policyFrame.setVisible(true);
  }  
  
  
}
