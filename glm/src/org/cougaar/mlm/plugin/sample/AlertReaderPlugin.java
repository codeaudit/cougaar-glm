/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.mlm.plugin.sample;

import java.awt.FlowLayout;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.Alert;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

/**
 * The GSLRescindPlugin will rescind the ?? task
 *
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
