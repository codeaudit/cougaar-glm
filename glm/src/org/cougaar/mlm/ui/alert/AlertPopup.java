/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 
package org.cougaar.mlm.ui.alert;

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

