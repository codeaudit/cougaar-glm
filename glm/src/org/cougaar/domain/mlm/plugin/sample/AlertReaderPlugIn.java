/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.sample;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Alert;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.util.UnaryPredicate;

/**
 * The GSLRescindPlugIn will rescind the ?? task
 *
 * @author       ALPINE <alpine-software@bbn.com>
 * @version      $Id: AlertReaderPlugIn.java,v 1.1 2000-12-15 20:17:45 mthome Exp $
 */

public class AlertReaderPlugIn extends SimplePlugIn
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
    frame = new JFrame("AlertReaderPlugIn");
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
   * Overrides the setupSubscriptions() in the SimplePlugIn.
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
