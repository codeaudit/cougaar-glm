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
 
package org.cougaar.mlm.ui.views.policy;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.*;

import org.cougaar.mlm.ui.producers.policy.UIPolicyInfo;
import org.cougaar.mlm.ui.producers.policy.UIPolicyParameterInfo;

public class PolicyDisplay extends JPanel implements ActionListener {
  private JLabel myClusterLabel;
  private PolicyTable myPolicyTable;
  private PolicyTableModel myPolicyTableModel;
  private PolicyClient myClient = null;
  private JScrollPane myScrollPane;
  private JComboBox myPolicyNameBox;
  private Hashtable myPolicies = new Hashtable();
  private String myClusterName = null;

  private static final String NULL_STRING = null;

  public PolicyDisplay() {
    this(NULL_STRING);
  }

  public PolicyDisplay(String cluster) {
    super();
    myClusterName = cluster;
    initDisplay();
  }

  public void setCluster(String name) {
    myClusterName = name;
    myClusterLabel.setText("Cluster: " + myClusterName);
  }
  
  public void setClient(PolicyClient client) {
    myClient = client;
  }
  
  public void updatePolicies(UIPolicyInfo []policies) {
    reset();
    for (int i = 0; i < policies.length; i++) {
      addPolicy((UIPolicyInfo)policies[i]);
    }
  }

  protected void addPolicy(UIPolicyInfo p) {
    String name = p.getName();
    myPolicyNameBox.addItem(name);
    myPolicies.put(name,p);
    // If first one reported, set table to show it
    if (myPolicyTableModel.getPolicy() == null) {
      myPolicyTableModel.setPolicy(p);
      myPolicyTableModel.setPolicyClient(myClient);
      myPolicyTable.revalidate();
      myPolicyTable.repaint();
    }
  }
  
  // Reset to null cluster
  public void reset() {
    setCluster(null);
    myPolicies = new Hashtable();
    myPolicyTableModel.setPolicy(null);
    myPolicyTable.revalidate();
    myPolicyTable.repaint();
    if (myPolicyNameBox.getItemCount() > 0) {
      myPolicyNameBox.removeAllItems();
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("Policy")) {
      String policyName = (String)myPolicyNameBox.getSelectedItem();
      if (policyName!=null) {
        UIPolicyInfo p = (UIPolicyInfo)myPolicies.get(policyName);
        if (p!=null) {
          myPolicyTableModel.setPolicy(p);
          myPolicyTable.revalidate();
          myPolicyTable.repaint();
        }
      }
    } else if (command.equals("Change")) {
      if (myClient!=null) {
        DefaultCellEditor cellEditor = 
          (DefaultCellEditor)myPolicyTable.getCellEditor();

        // Don't commit if unable to save current edit
        if ((cellEditor == null) ||
            (cellEditor.stopCellEditing())) {
            myClient.firePolicyChange(myPolicyTableModel.getPolicy());
        }
      }
    }
  }

  private void initDisplay() {
    JPanel namePanel = new JPanel();
    myClusterLabel = new JLabel("Cluster: " + myClusterName);
    namePanel.add(myClusterLabel);

    JPanel boxPanel = new JPanel();
    boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.X_AXIS));
    JLabel lab = new JLabel("Policies: ");
    boxPanel.add(lab);
    myPolicyNameBox = new JComboBox();
    myPolicyNameBox.setEditable(false);
    myPolicyNameBox.setActionCommand("Policy");
    myPolicyNameBox.addActionListener(this);
    boxPanel.add(myPolicyNameBox);

    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    add(namePanel);
    add(boxPanel);

    myPolicyTableModel = new PolicyTableModel();
    myPolicyTable = new PolicyTable(myPolicyTableModel);
    myPolicyTable.setPreferredScrollableViewportSize(new Dimension(250,250));

    myScrollPane = new JScrollPane(myPolicyTable);
    add(myScrollPane);
    
    JPanel changePanel = new JPanel();
    JButton changeButton = new JButton("Commit");
    changeButton.setActionCommand("Change");
    changeButton.addActionListener(this);
    changePanel.add(changeButton);
    add(changePanel);
  }

  /*
  public static void testPopulate(PolicyDisplay pd) {
    System.out.println("pop");
    if (pd!=null) {
      UIPolicyParameterInfo pp1 = new UIPolicyParameterInfo("Days", "3");
      UIPolicyParameterInfo pp2 = new UIPolicyParameterInfo("Mode", "Air");
      UIPolicyParameterInfo pp3 = new UIPolicyParameterInfo("Threshold", "5");
      UIPolicyParameterInfo pp4 = new UIPolicyParameterInfo("Ideal", "2.5");
      UIPolicyParameterInfo pp5 = new UIPolicyParameterInfo("State", "off");
      Policy p = new Policy("ShipPolicy","1");
      p.add(pp1);
      p.add(pp2);
      Policy p2 = new Policy("DummyPolicy","2");
      p2.add(pp3);
      p2.add(pp4);
      p2.add(pp5);
      pd.addPolicy(p);
      pd.addPolicy(p2);     
    }
  }
  */
  public static void main(String argv[]) {
    JFrame frame = new JFrame("Policy Test");
    PolicyDisplay pd = new PolicyDisplay();
    frame.getContentPane().add(pd);
    frame.setSize(250,200);
    frame.show();       
  }
}    

