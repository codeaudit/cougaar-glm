/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.alert;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlertPopup extends JFrame {
  static int alertcount = 1;
  static int WIDTH = 550;
  static int HEIGHT = 130;
  
  public AlertPopup(String cluster, String text) {
    super("ALERT #" + alertcount);
    JPanel panel = new JPanel();
    panel.add(new JLabel(cluster));
    panel.add(new JLabel(text));
    getContentPane().add(panel);
    setSize(WIDTH,HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(screenSize.width/2 - WIDTH/2 + (alertcount%10)*20,
                screenSize.height/2 - HEIGHT/2 + (alertcount%10)*20);
    toFront();
    show();
    alertcount++;
  }

  public static void main(String[] args) {
    String clusterText = args[0];
    String alertText = args[1];

    AlertPopup ap = new AlertPopup(clusterText, alertText);
    
    // Need to exit on window destruction
  }
}

