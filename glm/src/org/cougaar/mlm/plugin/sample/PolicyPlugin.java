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

package org.cougaar.mlm.plugin.sample;

import org.cougaar.glm.ldm.*;import org.cougaar.glm.ldm.*;import org.cougaar.glm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.policy.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.StateModelException;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.policy.*;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.legacy.PluginAdapter;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.mlm.plugin.UICoordinator;

/**
 * The PolicyPlugin
 */

public class PolicyPlugin extends SimplePlugin
{
  class PolicyGUI extends JFrame implements PropertyChangeListener {
    protected JLabel label;

    protected JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 3) {
      public Dimension getPreferredSize() {
        return new Dimension(100, super.getPreferredSize().height);
      }
      public Dimension getMinimumSize() {
        return new Dimension(100, super.getMinimumSize().height);
      }
    };

    protected ButtonGroup modeButtonGroup;
    JRadioButton groundButton = null;
    JRadioButton seaButton = null;
    JRadioButton airButton = null;

    private ShipPolicy policy;

    PolicyGUI(ShipPolicy forPolicy) {
      super("PolicyPlugin");
      policy = forPolicy;
      forPolicy.addPropertyChangeListener(this);
      getContentPane().setLayout(new FlowLayout());
      JPanel panel = new JPanel(null);

      int days = policy.getShipDays();
      label = new JLabel("ShipDays = " + days);
      MySliderListener mySliderListener = new MySliderListener();
      slider.setValue(days);
      slider.addChangeListener(mySliderListener);
      UICoordinator.layoutButtonAndLabel(panel, slider, label);

      JPanel modePanel = new JPanel();
      ModeButtonListener mbl = new ModeButtonListener();
      groundButton = new JRadioButton("Ground");
      seaButton = new JRadioButton("Sea");
      airButton = new JRadioButton("Air");
      groundButton.addActionListener(mbl);
      seaButton.addActionListener(mbl);
      airButton.addActionListener(mbl);
      modeButtonGroup = new ButtonGroup();
      modeButtonGroup.add(groundButton);
      modeButtonGroup.add(seaButton);
      modeButtonGroup.add(airButton);
      modePanel.add(groundButton);
      modePanel.add(seaButton);
      modePanel.add(airButton);
      if (getPolicyMode().equals("Ground")) {
	groundButton.setSelected(true);
      } else if (getPolicyMode().equals("Air")) {
	airButton.setSelected(true);
      } else if (getPolicyMode().equals("Sea")) {
	seaButton.setSelected(true);
      }

      UICoordinator.layoutSecondRow(panel, modePanel);
      setContentPane(panel);
      pack();
      UICoordinator.setBounds(this);
      setVisible(true);
    }
  

    /** An ActionListener that listens to the buttons. */
    class MySliderListener implements ChangeListener {
      public void stateChanged(ChangeEvent ce) {
	openTheTransaction();
	changePolicy(slider.getValue());
	closeTheTransaction(false);
      }
    }

    class ModeButtonListener implements ActionListener {
      public void actionPerformed(ActionEvent ae) {
	openTheTransaction();
	setPolicyMode(((JRadioButton)(ae.getSource())).getText());
	closeTheTransaction(false);
      }
    }

    public void changePolicy(int value) {
      policy.setShipDays(value);
      label.setText("ShipDays = " + value);

      publishTheChange(policy);
    }

    public String getPolicyMode() {
      return policy.getShipMode();
    }

    public void setPolicyMode(String newMode) {
      policy.setShipMode(newMode);
      publishTheChange(policy);
    }

    public void propertyChange(PropertyChangeEvent evt) {
      String propertyName = evt.getPropertyName();
      Object property = evt.getNewValue();
      try {
	if (propertyName == "RuleParameters") {
	  Hashtable params = (Hashtable)property;
	  IntegerRuleParameter dayParam = (IntegerRuleParameter) params.get(ShipPolicy.ShipDays);
	  Integer days = (Integer)dayParam.getValue();

	  slider.setValue(days.intValue());
	  EnumerationRuleParameter modeParam = (EnumerationRuleParameter) params.get(ShipPolicy.ShipMode);
	  String mode = (String) modeParam.getValue();
	  if (!groundButton.isSelected() && mode.equals(ShipPolicy.Ground))
	    groundButton.setSelected(true);
	  else if (!airButton.isSelected() && mode.equals(ShipPolicy.Air))
	    airButton.setSelected(true);
	  else if (!seaButton.isSelected() && mode.equals(ShipPolicy.Sea))
	    seaButton.setSelected(true);
	} else {
	  System.err.println("Unexpected Property Name " + propertyName);
	}
      }catch (Exception e) {
	System.err.println("Problem resetting ShipPolicy GUI with new values");
	e.printStackTrace();
      }
    }
  }

  // Have to provide these on this plugin class, else the inner class
  // below will not be able to find them
  public void openTheTransaction() {
    openTransaction();
  }

  public void closeTheTransaction(boolean b) {
    closeTransaction(b);
  }
  
  public void publishTheChange(Object o) {
    publishChange(o);
  }

  private static UnaryPredicate policyPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof ShipPolicy);
      }
    };
  }

  private IncrementalSubscription myPolicies;

  /**
   * Overrides the setupSubscriptions() in the SimplePlugin.
   */
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    myPolicies = (IncrementalSubscription) subscribe(policyPredicate());
//      addPolicies(myPolicies.elements());
  }

  private Hashtable policyGUIs = new Hashtable();

  /* CCV2 execute method */
  /* This will be called every time a new object matches the above predicate */
  public synchronized void execute() {
    if (myPolicies.hasChanged()) {
      addPolicies(myPolicies.getAddedList());
      removePolicies(myPolicies.getRemovedList());
    }
  }

  private void addPolicies(Enumeration e) {
    while (e.hasMoreElements()) {
      ShipPolicy policy = (ShipPolicy) e.nextElement();
      policyGUIs.put(policy, new PolicyGUI(policy));
    }
  }

  private void removePolicies(Enumeration e) {
    while (e.hasMoreElements()) {
      ShipPolicy policy = (ShipPolicy) e.nextElement();
      PolicyGUI gui = (PolicyGUI) policyGUIs.get(policy);
      if (gui != null) {
	gui.dispose();
	policyGUIs.remove(policy);
      }
    }
  }
}
