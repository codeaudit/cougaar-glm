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

package org.cougaar.glm.plugins;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RuleParameter;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.planning.ldm.policy.StringRuleParameter;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cougaar.glm.debug.GLMDebug;

/**
 * The Policy GUI Plugin based on
 * org.cougaar.mlm.plugin.sample.PolicyPlugin
 */

public class GLMPolicyPlugin extends SimplePlugin
{
  class GLMPolicyGUI extends JFrame implements PropertyChangeListener {
    protected JButton changeBtn = new JButton("Change Policy");
    private Hashtable sliders_;
    private Hashtable textFields_;
    private Policy policy;
    


    GLMPolicyGUI(String name) {
      super(name);
    }
    
    GLMPolicyGUI(Policy forPolicy) {
      super(forPolicy.getName()+" Plugin");
      policy = forPolicy;
      sliders_ = new Hashtable();
      textFields_ = new Hashtable();
      setLocation(0,310);
      RuleParameter rules[] = policy.getRuleParameters();
      getContentPane().setLayout(new GridLayout(rules.length+1,0));
      
      for (int ii = 0; ii < rules.length; ii++) {
	RuleParameter rule = rules[ii];
	if (rule instanceof IntegerRuleParameter) {
	  addIntegerRule((IntegerRuleParameter)rule);
	} else if (rule instanceof StringRuleParameter) {		
	  addStringRule((StringRuleParameter)rule);
	} else {
	  GLMDebug.ERROR("GLMPolicyPlugin","Unexpected Rule Parameter type "+rule);
	}
      }

      changeBtn.addActionListener(new MyButtonListener(sliders_, textFields_));
      getContentPane().add(changeBtn);
      pack();
      setVisible(true);
      policy.addPropertyChangeListener(this);
    }
    
    public void addStringRule(StringRuleParameter rule) {
      JPanel panel = new JPanel();
      
      String hashKey = rule.getName().trim();
      // Create the text field with label
      Label label = new Label(rule.getName()+" = "+rule.getValue());
      String value = (String)rule.getValue();
      JTextField textField = new JTextField(value);
      panel.add(textField);
      panel.add(label);
      
      getContentPane().add(panel);
      textFields_.put(hashKey, textField);
    }
    
    public void addIntegerRule(IntegerRuleParameter rule) {
      JPanel panel = new JPanel();
      
      String hashKey = rule.getName().trim();
      // Create the slider with label
      Label label = new Label(rule.getName()+" = "+rule.getValue());
      int value = ((Integer)rule.getValue()).intValue();
      // 	  JSlider slider = new JSlider(JSlider.HORIZONTAL, rule.my_min, rule.my_max, value);
      JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 50, value);
      slider.addChangeListener(new MySliderListener(slider, rule, label));
      panel.add(slider);
      panel.add(label);
      
      getContentPane().add(panel);
      sliders_.put(hashKey, slider);
    }

    /** An ActionListener that listens to the buttons. */
    
    class MySliderListener implements ChangeListener {
      JSlider slider_;        RuleParameter rule_;
      Label label_;
      public MySliderListener(JSlider slider, RuleParameter rule, Label l) {
	super();
	slider_ = slider;            rule_ = rule;
	label_ = l;
      }
      
      public void stateChanged(ChangeEvent ce) {
	openTheTransaction();
	try {
	  rule_.setValue(new Integer(slider_.getValue()));
	} catch (org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException pe) {
	  GLMDebug.ERROR("GLMPolicyPlugin","changePolicy error :"+pe);
	  return;
	}
	label_.setText(rule_.getName()+" = "+rule_.getValue());
	
	closeTheTransaction(false);
      }
    }
    
    /** An ActionListener that listens to the buttons. */
    class MyButtonListener implements ActionListener {
      Hashtable sliders_;
      Hashtable textFields_;
      
      public MyButtonListener(Hashtable sliders, Hashtable textFields) {
	super();
	sliders_ = sliders;
	textFields_ = textFields;
      }
      
      public void actionPerformed(ActionEvent ae) {
	openTheTransaction();
	Enumeration keys = sliders_.keys();
	RuleParameter rule;
	JSlider slide;
	String hashKey;
	while (keys.hasMoreElements()) {
	  hashKey = (String) keys.nextElement();
	  rule    = policy.Lookup(hashKey);
	  slide   = (JSlider)sliders_.get(hashKey);
	  changePolicy(slide.getValue(), rule);
	}
	keys = textFields_.keys();
	JTextField textField;
	String value;
	while (keys.hasMoreElements()) {
	  hashKey = (String) keys.nextElement();
	  rule    = policy.Lookup(hashKey);
	  value     = (String) rule.getValue();
	  textField = (JTextField)textFields_.get(hashKey);
	  changePolicy(textField.getText(), rule);
	}
	closeTheTransaction(false);
      }
    }

    
    public void changePolicy(int value, RuleParameter rule) {
      try {
	rule.setValue(new Integer(value));
      } catch (org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException pe) {
	GLMDebug.ERROR("GLMPolicyPlugin","changePolicy error :"+pe);
	return;
      }
      publishTheChange(policy);
    }
    
    public void changePolicy(String value, RuleParameter rule) {
      try {
	rule.setValue(value);
      } catch (org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException pe) {
	GLMDebug.ERROR("GLMPolicyPlugin","changePolicy error :"+pe);
	return;
      }
      publishTheChange(policy);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
      String propertyName = evt.getPropertyName();
      Object property = evt.getNewValue();
      try {
	if (propertyName == "RuleParameters") {
	  Hashtable params = (Hashtable)property;
	  Enumeration keys = params.keys();
	  String hashKey, svalue;
	  IntegerRuleParameter irule;
	  RuleParameter rule;
	  StringRuleParameter srule;
	  Integer ivalue;
	  JSlider slider;
	  JTextField textField;
	  int intValue;

	  while (keys.hasMoreElements()) {
	    rule = (RuleParameter) params.get(keys.nextElement());
	    if (rule instanceof IntegerRuleParameter) {
	      irule    = (IntegerRuleParameter)rule;
	      ivalue    = (Integer)irule.getValue();
	      hashKey  = irule.getName();
	      slider   = (JSlider)sliders_.get(hashKey);
	      intValue = ivalue.intValue();

	      // don't cause unnecessary redraws
	      if (slider.getValue() != intValue)
		slider.setValue(intValue);
	      
	    } else if (rule instanceof StringRuleParameter) {
	      srule      = (StringRuleParameter)rule;
	      hashKey    = srule.getName().trim();
	      svalue     = (String)srule.getValue();
	      textField  = (JTextField)textFields_.get(hashKey);
	      
	      if (!(textField.getText().equals(svalue)))
		textField.setText(svalue);
	      
	    } else {
	      GLMDebug.ERROR("GLMPolicyPlugin","Unexpected Rule Parameter "+rule);
	    }
	  }
	} else {
	  GLMDebug.ERROR("GLMPolicyPlugin","Unexpected Property Name " + propertyName);
	}
      }catch (Exception e) {
	GLMDebug.ERROR("GLMPolicyPlugin"," Problem resetting Policy GUI with new values");
	e.printStackTrace();
      }
    }
  }
  
  private IncrementalSubscription myPolicies;
  private UnaryPredicate policyPredicate;
  private Hashtable policyGUIs = new Hashtable();
  
  public GLMPolicyPlugin( UnaryPredicate pol_pred) {
    policyPredicate = pol_pred;
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
  
  /**
   * Overrides the setupSubscriptions() in the SimplePlugin.
   */
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    myPolicies = (IncrementalSubscription) subscribe(policyPredicate);
  }
  
  public synchronized void execute() {
    for (Enumeration e = myPolicies.getAddedList(); e.hasMoreElements(); ) {
      Policy policy = (Policy) e.nextElement();
      policyGUIs.put(policy, new GLMPolicyGUI(policy));
    }
    for (Enumeration e = myPolicies.getRemovedList(); e.hasMoreElements(); ) {
      Policy policy = (Policy) e.nextElement();
      GLMPolicyGUI gui = (GLMPolicyGUI) policyGUIs.get(policy);
      if (gui != null) {
	gui.dispose();
	policyGUIs.remove(policy);
      }
    }
  }
}
