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
package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import org.cougaar.util.OptionPane;
import org.cougaar.util.Random;

public class InventoryPilferPlugIn implements InventoryPlugIn, TimeConstants {

  private String pilferItem = "DODIC/A576";
  private double pilferRate;
  private long minElapsed;
  private long lastTime = -1L;
  private static final Random random = new Random();
  private static final long MIN_INTERVAL = ONE_HOUR;
  private static final long MAX_INTERVAL = ONE_DAY;
  private static final double MIN_LAMBDA = 0.1;

  public InventoryPilferPlugIn() {
    setPilferRate(1.0);         // per day
  }

  private void setPilferRate(double rate) {
    pilferRate = rate;          // per day
    minElapsed = (long) Math.min(MAX_INTERVAL, Math.max(MIN_LAMBDA * ONE_DAY / pilferRate, MIN_INTERVAL));
  }

  /**
   * @return the name of this plugin
   **/
  public String getPlugInName() {
    return "Pilfer";
  }

  public String getDescription() {
    return "Pilfer plugin steals a certain item";
  }

  public void setParameter(String parameter) {
  }

  public boolean isConfigurable() {
    return true;
  }

  private static void addItem(JPanel message, int x, int y, JComponent c) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.anchor = gbc.WEST;
    message.add(c, gbc);
  }

  public void configure(java.awt.Component c) {
    JPanel message = new JPanel(new GridBagLayout());
    JTextField tf1 = new JTextField(40);
    tf1.setText(pilferItem);
    JTextField tf2 = new JTextField(40);
    tf2.setText("" + pilferRate);
    addItem(message, 0, 0, new JLabel("Item Identification: "));
    addItem(message, 1, 0, tf1);
    addItem(message, 0, 1, new JLabel("Pilferage Rate (per day): "));
    addItem(message, 1, 1, tf2);
    int result = OptionPane.showOptionDialog(c,
                                             message,
                                             "Configure " + getPlugInName(),
                                             OptionPane.OK_CANCEL_OPTION,
                                             OptionPane.QUESTION_MESSAGE,
                                             null, null, null);
    if (result == OptionPane.OK_OPTION) {
      try {
        setPilferRate(Double.parseDouble(tf2.getText()));
        pilferItem = tf1.getText();
      } catch (Exception e) {     // Ignore errors.
        e.printStackTrace();
      }
    }
  }

  public void save(java.util.Properties props, String prefix) {
    props.setProperty(prefix + "item", pilferItem);
    props.setProperty(prefix + "rate", Double.toString(pilferRate));
  }

  public void restore(java.util.Properties props, String prefix) {
    pilferItem = props.getProperty(prefix + "item", pilferItem);
    try {
      pilferRate = Double.parseDouble(props.getProperty(prefix + "rate"));
    } catch (Exception e) {
      // State probably not saved.
    }
  }

 public void setEventGenerator(EventGenerator eg) {
 }

  /**
   * Apply this plugin to an InventoryReport,
   * @return true if this plugin was applicable to the report.
   **/
  public boolean apply(TimedInventoryReport tir, long theExecutionTime) {
    if (lastTime == -1L) {
      lastTime = theExecutionTime;
    } else {
      
      double elapsed = (theExecutionTime - lastTime);
      if (elapsed > ONE_HOUR) {
        double pilferage = (double) random.nextPoisson(pilferRate * elapsed / ONE_DAY);
        InventoryReport theInventoryReport = tir.getModifiableInventoryReport();
        pilferage = Math.min(pilferage, theInventoryReport.theQuantity);
        theInventoryReport.theQuantity -= pilferage;
        lastTime = theExecutionTime;
      }
    }
    return true;
  }
}
