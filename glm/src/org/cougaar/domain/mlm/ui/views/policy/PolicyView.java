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
 
package org.cougaar.domain.mlm.ui.views.policy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;

import org.cougaar.domain.mlm.ui.planviewer.ScrollingTextLine;
import org.cougaar.domain.mlm.ui.producers.ClusterCache;
import org.cougaar.domain.mlm.ui.producers.PolicyProducer;
import org.cougaar.domain.mlm.ui.producers.policy.UIPolicyInfo;
import org.cougaar.domain.mlm.ui.views.MLMConsumer;

/**
 * The <code>PolicyView</code> class demonstrates a typical MLMConsumer editor.
 * It has a dropdown list for selecting the target cluster, selecting the
 * desired policy within that cluster, and a simple 2-column table of the
 * policy parameters for that policy.
 * After editing the values, the changes are transmitted to the cluster
 * via the Commit button.
 *
 * @author  Nick Pioch, BBN Technologies
 * @version 1.0
 */

public class PolicyView extends JPanel 
  implements MLMConsumer, PolicyClient, ActionListener {
    
  private PolicyDisplay myPolicyDisplay=null;
  private String myTargetCluster = null;
  private JComboBox myClusterBox;
  private ScrollingTextLine myScrollingTextLine = null;
  private DisablingGlassPane myNoInputGlassPane = null;
  
  // No arg constructor to bring up initially empty view
  public PolicyView() {
    init();
  }

  public PolicyView(String clusterName) {
    myTargetCluster = clusterName;
    init();
  }
  
  /**
   * Do the one-time initializations, including component creation.  
   */
  private void init() {
    // COUGAAR-specific gui init
    // make a panel with refresh button and cluster pull-down list
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();


    JLabel label = new JLabel("Clusters:");
    panel1.add(label);

    myClusterBox = new JComboBox();
    myClusterBox.setEditable(false);
    Vector clusterNames = ClusterCache.getClusterNames();

    if (clusterNames != null) {
      for (int i=0; i<clusterNames.size(); i++) {
        myClusterBox.addItem((String)clusterNames.get(i));
      }
      myClusterBox.setActionCommand("ClusterSelected");
      myClusterBox.addActionListener(this);
    } else {
      System.err.println("Unable to find any clusters");
      Runtime.getRuntime().exit(0);
    }

    panel1.add(myClusterBox);

    add(panel1);

    JButton refreshButton = new JButton("Refresh");
    refreshButton.setActionCommand("Refresh");
    refreshButton.addActionListener(this);
    panel2.add(refreshButton);

    add(panel2);

    JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
    add(sep);
    
    myPolicyDisplay = new PolicyDisplay();
    add(myPolicyDisplay);
    myPolicyDisplay.setClient(this);
    
    myScrollingTextLine = new ScrollingTextLine(30);
    add(myScrollingTextLine);
    
    myNoInputGlassPane = new DisablingGlassPane(this);


  }
  
  /** 
   * This method is called via the PolicyClient interface to notify
   * that the user pressed Commit within the PolicyDisplay, so the
   * the updated Policy argument can be transmitted to the cluster PSP.
   */
  public void firePolicyChange(UIPolicyInfo p) {
    // disable input
    getRootPane().getGlassPane().setVisible(true);

    updateStatusLine("Policy " + p.getName() + " being sent to ALP.");

    // Lookup producer and publish the change
    PolicyProducer prod;
    prod = (PolicyProducer)ClusterCache.getClusterProducer(myTargetCluster,
                                                           "PolicyProducer");
    if (prod!=null) {
      prod.publish(p);
    }
    updateStatusLine("Policy " + p.getName() + " published.");
    
    // Publish always followed by a data update so leave the glass pane 
    // visible until we receive the data update.
  }
  
  /** 
   * Update all policies - 
   */
  public void fireDataUpdate(Object []updateData, Object producer) {
    UIPolicyInfo []policies = new UIPolicyInfo[updateData.length];
    System.arraycopy(updateData, 0, policies, 0, updateData.length);
    myPolicyDisplay.updatePolicies(policies);

    myPolicyDisplay.setCluster(myTargetCluster);
    
    updateStatusLine("Refreshed policies from " + myTargetCluster);

    getRootPane().getGlassPane().setVisible(false);
  }
  
  /** 
   * Report error to user
   */
  public void fireErrorReport(String errorText, Object producer) {
    //This is the only response we're going to get so disable the 
    // glass pane
    getRootPane().getGlassPane().setVisible(false);

    JOptionPane.showMessageDialog(null,
                                  errorText,
                                  "Error",
                                  JOptionPane.ERROR_MESSAGE);
  }

  public void refresh() {
    // Turned off on fireDataUpdate or fireErrorReport
    getRootPane().getGlassPane().setVisible(true);

    updateStatusLine("Refreshing policies for " + myTargetCluster);
    
    // Check if producer exists for this cluster
    PolicyProducer prod;
    prod = (PolicyProducer)ClusterCache.getClusterProducer(myTargetCluster,
                                                           "PolicyProducer");
    // If so, tell it to refresh its data
    if (prod!=null) {
      prod.refresh();
    } else {
      // If doesnt exist, start one up
      prod = new PolicyProducer(myTargetCluster);
      prod.addConsumer(this);
      prod.start();
    }
  }
  
  /**
   * Handle events from refresh button or cluster combo box
   */
  public void actionPerformed(ActionEvent e) {
    String clusterName = (String)myClusterBox.getSelectedItem();
    // Reset PolicyDisplay - in effect, throw away old cluster's dataitems
    myPolicyDisplay.reset();
    //if (clusterName==null || clusterName=="None") {
    if (clusterName == null) {
      myTargetCluster = null;
    } else {
      myTargetCluster = clusterName;
      refresh();
    }
    // show new cluster name in display
    myPolicyDisplay.setCluster(myTargetCluster);
  }

  public void addNotify() {
    super.addNotify();
    getRootPane().setGlassPane(myNoInputGlassPane);  

    // Select initial cluster - must be deferred until display
    // has been initialized
    if (myTargetCluster != null) {
      myClusterBox.setSelectedItem(myTargetCluster);
    } else {
      myClusterBox.setSelectedIndex(0);
    }
  }

  private void updateStatusLine(String status) {
    myScrollingTextLine.setText(status);
  }
  
}

