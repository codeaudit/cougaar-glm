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

package org.cougaar.mlm.examples;

import org.cougaar.glm.ldm.Constants;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.trigger.*;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.legacy.PluginAdapter;
import org.cougaar.util.UnaryPredicate;

/**
 * The Trigger tester plugin.  A GUI based plugin which creates test planelements
 * that triggers are placed on.  Both timebasedmonitors and predicatebasedmonitors
 * can be created.  Currently configured with 2 triggers with timebasedmonitors that
 * watch for stale allocations, and rescind them and 1 predicatebasedmonitor that looks
 * for certain planelements and marks them as stale.
 *
 * @author       ALPINE <alpine-software@bbn.com>
 *
 */

public class TriggerTesterPlugin extends SimplePlugin
{
  private IncrementalSubscription selfAssets;
  private IncrementalSubscription myplanelements;
  private IncrementalSubscription mype2;
  private Trigger mytrigger2, mytrigger;

  /** frame for 1-button UI **/
  static JFrame frame;

  Label TRLabel;

  protected JButton trButton;

  
  // Have to provide these on this plugin class, else the inner class
  // below will not be able to find them
  public void openTheTransaction() {
    openTransaction();
  }

  public void closeTheTransaction(boolean b) {
    closeTransaction(b);
  }


  /** An ActionListener that listens to the Trigger buttons. */
  class TRButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String lnfName = e.getActionCommand();
      try {
        openTheTransaction();
        System.out.println("Creating Triggers...");
        createTriggers();
        closeTheTransaction(false);
        TRLabel.setText("sent triggers");
      } catch (Exception exc) {
        JButton button = (JButton)e.getSource();
        button.setEnabled(false);
        System.err.println("Could not execute TR button: " + lnfName);
      }
    }
  }
 

  private void createGUI() {
    frame = new JFrame("TriggerPlugin");
    frame.setLocation(0, 80);
    frame.getContentPane().setLayout(new FlowLayout());
    JPanel panel = new JPanel();
    // Create the button
    trButton = new JButton("Create Trigger Objects");
    trButton.setEnabled(true); 
    // Create a label for feedback on if the root task was sent
    TRLabel = new Label("                                                              ");
    // Register a listener for the check box
    TRButtonListener myTRListener = new TRButtonListener();
    trButton.addActionListener(myTRListener);
    panel.add(trButton);
    panel.add(TRLabel);
    frame.getContentPane().add("Center", panel);
    frame.pack();
    frame.setVisible(true);
  }

  
  protected void setupSubscriptions() {
    selfAssets = (IncrementalSubscription) subscribe(selfPred());
    myplanelements = (IncrementalSubscription) subscribe(pepred());
    mype2 = (IncrementalSubscription) subscribe(pepred2());
    createGUI();
  }

  public synchronized void execute() {
    
    // used if we want to make the allocation stale to kick a trigger into action
    //if (myplanelements.hasChanged()) {
      //Enumeration added = myplanelements.getAddedList();
      //while (added.hasMoreElements()) {
        //Allocation alloc = (Allocation) added.nextElement();
        // make it stale for testing purposes
        //alloc.setStale(true);
        //publishChange(alloc);
      //}
    //}
    
    // rescind the trigger if it did its job and we don't expect anything else to use it.
    // Used mostly to test the TriggerManagerPlugin's management of triggers.
    if (myplanelements.hasChanged()) {
      Enumeration oneremoved = myplanelements.getRemovedList();
      if (oneremoved.hasMoreElements()) {
        publishRemove(mytrigger);
        System.err.println("Just rescinded mytrigger (1)");
      }
    }
    
    // rescind the trigger if it did its job and we don't expect anything else to use it
    // Used mostly to test the TriggerManagerPlugin's management of triggers.
    if (mype2.hasChanged()) {
      Enumeration removed = mype2.getRemovedList();
      if (removed.hasMoreElements()) {
        // we only expect one, so if we are here, rescind its trigger.
        publishRemove(mytrigger2);
        System.err.println("Just rescinded mytrigger2!");
      }
    }
        
  }
  
  private void createTriggers() {
    // create a planelement first
    PlanElement boguspe = createAPlanElement(Constants.Verb.TRANSPORT);
    // now create a trigger.
    // first make a montior
    //TriggerMonitor mymonitor = new TriggerPredicateBasedMonitor(pepred());
    Object[] monobjects = {boguspe};
    TriggerMonitor mytimemon = new TriggerTimeBasedMonitor(30000, monobjects, getDelegate());
    // create a stale tester.
    TriggerTester mytester = new TriggerStaleTester();
    // create a rescind action
    TriggerAction myaction = new TriggerRescindAction(boguspe);
    mytrigger = new Trigger(mytimemon, mytester, myaction);
    //mytrigger = new Trigger(mymonitor, mytester, myaction);
    publishAdd(mytrigger);
    
    // create a second one
    PlanElement secondpe = createAPlanElement(Constants.Verb.TRANSPORTATIONMISSION);
    Object[] monobjects2 = {secondpe};
    TriggerMonitor mytimemon2 = new TriggerTimeBasedMonitor(10000, monobjects2, getDelegate());
    // create a stale tester.
    TriggerTester mytester2 = new TriggerStaleTester();
    // create a rescind action
    TriggerAction myaction2 = new TriggerRescindAction(secondpe);
    mytrigger2 = new Trigger(mytimemon2, mytester2, myaction2);
    publishAdd(mytrigger2);  
    
    // create another trigger on one of the planelements.
    TriggerMonitor mymonitor3 = new TriggerPredicateBasedMonitor(pepred3());
    // create a stale tester.
    TriggerTester mytester3 = null;
    // create a make stale action
    TriggerAction myaction3 = new TriggerMakeStaleAction(((Allocation)boguspe));
    Trigger mytrigger3 = new Trigger(mymonitor3, mytester3, myaction3);
    publishAdd(mytrigger3);
        
  }
  
  
  public PlanElement createAPlanElement(String theverb) {
    PlanningFactory factory = getFactory();
    NewTask ntask = factory.newTask();
    // only fill in a few things on the task since it is only for testing.
    // if this was a real task we would need to fill in more.
    ntask.setVerb(new Verb(theverb));
    ntask.setPlan(factory.getRealityPlan());
        
    Asset theasset = null;
    Enumeration assete = selfAssets.elements();
    if (assete.hasMoreElements()) {
      theasset = (Asset) assete.nextElement();
    }
    
    if (theasset != null) {
      // create an allocationresult
      Calendar now = Calendar.getInstance();
      now.add(Calendar.DATE, 5);
      AspectValue time1 = TimeAspectValue.create(AspectType.START_TIME, now.getTime());
      now.add(Calendar.DATE, 5);
      AspectValue time2 = TimeAspectValue.create(AspectType.END_TIME, now.getTime());
      AspectValue[] avresults = {time1, time2};
      AllocationResult theresult = factory.newAVAllocationResult(1.0, true, avresults);
      // create an allocation of the new task to this cluster
      Allocation newalloc = factory.createAllocation(ntask.getPlan(), ntask, theasset, theresult, Constants.Role.TRANSPORTER);
      // make it stale for testing purposes
      //newalloc.setStale(true);
      publishAdd(newalloc);
      return newalloc;
    } else {
      System.err.println("Don't have an asset to allocate to!!!");
    }
    //if we make it to here, there was a problem
    return null;
  }
 
  // predicate for getting all PlanElement objects
  private static UnaryPredicate pepred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof PlanElement) {
	  Task t = ((PlanElement)o).getTask();
	  if (t.getVerb().equals(Constants.Verb.TRANSPORT)) {
	    return true;
	  }
	}
	return false;
      }
    };
  }
  
  // predicate for getting all PlanElement objects
  private static UnaryPredicate pepred3() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof PlanElement) {
	  Task t = ((PlanElement)o).getTask();
	  if ( (t.getVerb().equals(Constants.Verb.TRANSPORT)) && (! ((Allocation)o).isStale()) ) {
	    return true;
	  }
	}
	return false;
      }
    };
  }
  
  // predicate for getting second PlanElement objects
  private static UnaryPredicate pepred2() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof PlanElement) {
	  Task t = ((PlanElement)o).getTask();
	  if (t.getVerb().equals(Constants.Verb.TRANSPORTATIONMISSION)) {
	    return true;
	  }
	}
	return false;
      }
    };
  }
  
  // predicate for getting self asset
  private static UnaryPredicate selfPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Organization) {
          return ((Organization)o).isSelf();
	} 
	return false;
      }
    };
  }
  
}
