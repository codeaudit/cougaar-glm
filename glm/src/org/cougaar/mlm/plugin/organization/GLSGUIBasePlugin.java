/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.util.UID;
import org.cougaar.util.UnaryPredicate;

import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Collection;
import javax.swing.*;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.mlm.plugin.RandomButtonPusher;
import org.cougaar.mlm.plugin.UICoordinator;

/**
 * The GLSGUIBasePlugin supports both init and rescind plugins by
 * providing the basic oplan tracking and management. For each Oplan,
 * the combobox of the gui is augmented with the additional oplan.
 * When the selected oplan is changed, methods are invoked in the
 * subclasses to allow labels to be updated.
 *
 **/
public abstract class GLSGUIBasePlugin extends SimplePlugin {
  private static String EXIT_ON_CLOSE_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIBasePlugin.exitOnClose";
  private static boolean exitOnClose = System.getProperty(EXIT_ON_CLOSE_PROP, "false").equals("true");

  /** frame for 1-button UI **/
  private JFrame frame;

  /** for knowing when we get our self org asset **/
  protected Organization selfOrgAsset = null;

  /** for feedback to user on whether root GLS was successful **/
  private JLabel glsLabel = new JLabel("                ");

  /** A button to push to kick things off **/
  protected JButton glsButton = new JButton("Send GLS root");

  /** A combo box for selecting the Oplan **/
  protected JComboBox oplanCombo = new JComboBox();

  /** A panel to hold the GUI **/
  JPanel panel = new JPanel((LayoutManager) null);

  private IncrementalSubscription myorgassets;
  private IncrementalSubscription oplanSubscription;
  private IncrementalSubscription taskSubscription;
  protected boolean canSend=true;

  private static final String forRoot = "ForRoot".intern();

  private static UnaryPredicate orgAssetPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Organization);
    }
  };

  private static UnaryPredicate oplanPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };

  /**
   * This predicate selects for root tasks injected by the GLSGUIInitPlugin
   **/
  private UnaryPredicate taskPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof Task)) return false;
	Task task = (Task) o;
	if (!task.getSource().equals(getTheMessageAddress())) return false;
	if (!task.getDestination().equals(getTheMessageAddress())) return false;
	if (!task.getVerb().equals(Constants.Verb.GETLOGSUPPORT)) return false;
	return (task.getPrepositionalPhrase(forRoot) != null);
      }
    };
  }

  /**
   * Wrap an oplan to implement toString for the combobox
   **/
  protected static class OplanWrapper {
    public Oplan oplan;
    public ArrayList tasks = new ArrayList(1);
    public OplanWrapper(Oplan oplan) {
      this.oplan = oplan;
    }
    public String toString() {
      return oplan.getOperationName();
    }
    public void addTask(Task t) {
      tasks.add(t);
    }
    public void removeTask(Task t) {
      tasks.remove(t);
    }
  }

  private OplanWrapper findOplanWrapper(Task t) {
    ContextOfUIDs context = (ContextOfUIDs) t.getContext();
    UID oplanUID = context.get(0);
    OplanWrapper wrapper = null;
    for (int i = 0, n = oplanCombo.getItemCount(); i < n; i++) {
      wrapper = (OplanWrapper) oplanCombo.getItemAt(i);
      if (wrapper.oplan.getUID().equals(oplanUID)) return wrapper;
    }
    // There should be no way for a root task with no corresponding oplan to exist
    return null;
  }

  // Have to provide these on this plugin class, else the inner class
  // below will not be able to find them
  public void openTheTransaction() {
    openTransaction();
  }

  public void closeTheTransaction(boolean b) {
    closeTransaction(b);
  }

  public MessageAddress getTheMessageAddress() {
    return getMessageAddress();
  }

  private void createGUI() {
    frame = new JFrame(getGUITitle());
    if (exitOnClose) frame.setDefaultCloseOperation(3);
    frame.setLocation(0,0);
    // Create the button
    // Register a listener for the radio buttons
    glsButton.setText(getButtonText());
    glsButton.setEnabled(false); // Leave this disabled until we have oplans
    glsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OplanWrapper wrapper = (OplanWrapper) oplanCombo.getSelectedItem();
        if (wrapper != null) buttonPushed(wrapper);
      }
    });
    oplanCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkButtonEnable();
      }
    });
    UICoordinator.layoutButtonAndLabel(panel, glsButton, glsLabel);
	frame.getRootPane().setDefaultButton(glsButton); // hitting return sends GLS
    JLabel oplanComboLabel = new JLabel("Select Oplan");
    UICoordinator.layoutButtonAndLabel(panel, oplanCombo, oplanComboLabel);
    frame.setContentPane(panel);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }

  public void unload() {
    super.unload();
    frame.dispose();
  }
  /**
   * Overrides the setupSubscriptions() in the SimplifiedPlugin.
   * Should be further overridden in subclasses which must call
   * super.setupSubscriptions().
   **/
  protected final void setupSubscriptions() {
    // Create a simple UI button for kicking off root GLS for testing
    createGUI();

    myorgassets = (IncrementalSubscription) subscribe(orgAssetPred);
    oplanSubscription = (IncrementalSubscription) subscribe(oplanPred);
    taskSubscription = (IncrementalSubscription) subscribe(taskPred());
    createSubscriptions();      // Give the subclass a chance to create subscriptions

    if (!didRehydrate()) {
      createPrivateState();     // Create private state if any
    } else {
      //Initialize gui state based on existing info
      restorePrivateState();    // Restore private state from logplan

      handleOplan(oplanSubscription.elements());
      handleMyOrgAssets(myorgassets.elements());
      handleAddedTasks(taskSubscription.elements());
      checkButtonEnable();
    }
  }

  /* This will be called every time a new task matches the above predicate */

  public final void execute() {
    additionalExecute();
    handlePrivateState();

    if (myorgassets.hasChanged()) {
      handleMyOrgAssets(myorgassets.getAddedList());
    }

    if (oplanSubscription.hasChanged()) {
      handleOplan(oplanSubscription.getAddedList());
    }

    if (taskSubscription.hasChanged()) {
      handleAddedTasks(taskSubscription.getAddedList());
      handleRemovedTasks(taskSubscription.getRemovedList());
    }
    checkButtonEnable();
  }

  protected void handleAddedTasks(Enumeration tasks) {
    while (tasks.hasMoreElements()) {
      Task t = (Task) tasks.nextElement();
      OplanWrapper wrapper = findOplanWrapper(t);
      if (wrapper != null) wrapper.addTask(t);
    }
  }

  protected void handleRemovedTasks(Enumeration tasks) {
    while (tasks.hasMoreElements()) {
      Task t = (Task) tasks.nextElement();
      OplanWrapper wrapper = findOplanWrapper(t);
      if (wrapper != null) wrapper.removeTask(t);
    }
  }

  protected abstract void createSubscriptions();
  protected abstract void handlePrivateState();
  protected abstract void createPrivateState();
  protected abstract void restorePrivateState();
  protected abstract boolean isPrivateStateOk();
  protected abstract String getGUITitle();
  protected abstract String getButtonText();
  protected abstract String getGLSLabelText(int nTasks);
  protected abstract void buttonPushed(OplanWrapper wrapper);
  protected abstract void additionalExecute();

  private void handleMyOrgAssets(Enumeration e) {
    while (e.hasMoreElements()) {
      Organization org = (Organization)e.nextElement();

      // Pick up self org
      if (org.isSelf()) {
        selfOrgAsset = org;
        checkButtonEnable();
      }
    }
  }

  /** New oplan received, add to combobox. **/
  private void handleOplan(Enumeration e) {
    while (e.hasMoreElements()) {
      Oplan oplan = (Oplan) e.nextElement();
      checkButtonEnable();
      oplanCombo.addItem(new OplanWrapper(oplan));
    }
  }

  protected void checkButtonEnable() {
    boolean privateStateOK = isPrivateStateOk();
    glsButton.setEnabled(privateStateOK
                         && selfOrgAsset != null
                         && oplanCombo.getSelectedItem() != null);
    if (!privateStateOK) {
      glsLabel.setText("Waiting for private state");
    } else if (selfOrgAsset == null) {
      glsLabel.setText("Waiting for Self");
    } else {
      OplanWrapper wrapper = (OplanWrapper) oplanCombo.getSelectedItem();
      if (wrapper == null) {
        glsLabel.setText("Waiting for Oplan(s)");
      } else {
        glsLabel.setText(getGLSLabelText(wrapper.tasks.size()));
      }
    }
  }

  protected static DateFormat logTimeFormat =
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  protected static String formatDate(long when) {
    return logTimeFormat.format(new Date(when));
  }

}
