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
package org.cougaar.mlm.plugin.sample;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.StateModelException;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.plan.Alert;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.plugin.PluginAdapter;
import org.cougaar.util.UnaryPredicate;

/**
 * The GSLRescindPlugin will rescind the ?? task
 *
 * @author       ALPINE <alpine-software@bbn.com>
 *
 */

public class AlertReaderPlugin extends SimplePlugin
{
  /** frame for UI **/
  static JFrame frame;

  /** to display alerts **/
  protected JList listbox;
  protected DefaultListModel listmodel;

  /** Subscription to hold collection of input alerts **/
  private IncrementalSubscription myalert;

  /** subscribes for all alerts **/
  private static UnaryPredicate alertPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Alert);
      }
    };
  }

  /** UI that shows alert **/
  private void createGUI() {
    frame = new JFrame("AlertReaderPlugin");
    frame.getContentPane().setLayout(new FlowLayout());
    JPanel panel = new JPanel();
    listmodel = new DefaultListModel();
    listbox = new JList(listmodel);
    JScrollPane scrollPane = new JScrollPane(listbox);
    panel.add(scrollPane);
    frame.getContentPane().add("Center", panel);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Overrides the setupSubscriptions() in the SimplePlugin.
   */
  protected void setupSubscriptions() {
    myalert = (IncrementalSubscription)subscribe(alertPred());
    createGUI();
  }

  /* CCV2 execute method */
  /* This will be called every time a alert matches the above predicate */
  public synchronized void execute() {
    Enumeration e = myalert.getAddedList();
    while (e.hasMoreElements()) {
      Alert al = (Alert)e.nextElement();
      listmodel.addElement("Received Alert " +al.getAlertText());
    }
    e = myalert.getChangedList();
    while (e.hasMoreElements()) {
      Alert al = (Alert)e.nextElement();
      listmodel.addElement("Received Alert " +al.getAlertText());
    }

  }
}
