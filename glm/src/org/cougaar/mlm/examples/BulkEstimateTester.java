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

package org.cougaar.mlm.examples;

import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.BulkEstimate;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

/**
 * The bulkestimate tester
 *
 *
 */

public class BulkEstimateTester extends SimplePlugin
{
  /** frame for 1-button UI **/
  static JFrame frame;

  Label BELabel;

  protected JButton beButton;

  private IncrementalSubscription bulkests;
  
  // Have to provide these on this plugin class, else the inner class
  // below will not be able to find them
  public void openTheTransaction() {
    openTransaction();
  }

  public void closeTheTransaction(boolean b) {
    closeTransaction(b);
  }


  /** An ActionListener that listens to the GLS buttons. */
  class BEButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String lnfName = e.getActionCommand();
      try {
        openTheTransaction();
        System.out.println("Creating BulkEstimates...");
        createBulkEstimates();
        closeTheTransaction(false);
        BELabel.setText("sent bulk estimates");
      } catch (Exception exc) {
        JButton button = (JButton)e.getSource();
        button.setEnabled(false);
        System.err.println("Could not execute BE button: " + lnfName);
      }
    }
  }
 

  private void createGUI() {
    frame = new JFrame("BulkEstimatePlugin");
    frame.setLocation(0, 80);
    frame.getContentPane().setLayout(new FlowLayout());
    JPanel panel = new JPanel();
    // Create the button
    beButton = new JButton("Create BulkEstimate Objects");
    beButton.setEnabled(true); 
    // Create a label for feedback on if the root task was sent
    BELabel = new Label("                                                              ");
    // Register a listener for the check box
    BEButtonListener myBEListener = new BEButtonListener();
    beButton.addActionListener(myBEListener);
    panel.add(beButton);
    panel.add(BELabel);
    frame.getContentPane().add("Center", panel);
    frame.pack();
    frame.setVisible(true);
  }

  
  protected void setupSubscriptions() {
    bulkests = (IncrementalSubscription) subscribe(bepred());
    createGUI();
  }

  public synchronized void execute() {
    if (bulkests.hasChanged()) {
      Enumeration bechanges = bulkests.getChangedList();
      while (bechanges.hasMoreElements()) {
        BulkEstimate completebe = (BulkEstimate) bechanges.nextElement();
        printBulkEstimate(completebe);
      }
    }
  }
  
  private void createBulkEstimates() {
    PlanningFactory factory = getFactory();
    NewTask ntask = factory.newTask();
    // fill in some of the task since we are only using it for testing
    // if it were a real task we would need to fill in more.
    ntask.setVerb(Constants.Verb.Transport);
    ntask.setPlan(factory.getRealityPlan());
    
    // create some AspectValues
    Calendar now = Calendar.getInstance();
    AspectValue time1 = TimeAspectValue.create(AspectType.START_TIME, now.getTime());
    now.add(Calendar.DATE, 5);
    AspectValue time2 = TimeAspectValue.create(AspectType.START_TIME, now.getTime());
    now.add(Calendar.DATE, 5);
    AspectValue time3 = TimeAspectValue.create(AspectType.START_TIME, now.getTime());
    now.add(Calendar.DATE, 5);
    AspectValue time4 = TimeAspectValue.create(AspectType.END_TIME, now.getTime());
    now.add(Calendar.DATE, 5);
    AspectValue time5 = TimeAspectValue.create(AspectType.END_TIME, now.getTime());
    now.add(Calendar.DATE, 5);
    AspectValue time6 = TimeAspectValue.create(AspectType.END_TIME, now.getTime());
    
    
    ScoringFunction sf1 = ScoringFunction.createStrictlyAtValue(time1);
    Preference pref1 = factory.newPreference(AspectType.START_TIME, sf1);
    ScoringFunction sf2 = ScoringFunction.createStrictlyAtValue(time4);
    Preference pref2 = factory.newPreference(AspectType.END_TIME, sf2);
    Preference[] prefset1 = {pref1, pref2};
    
    ScoringFunction sf3 = ScoringFunction.createStrictlyAtValue(time2);
    Preference pref3 = factory.newPreference(AspectType.START_TIME, sf3);
    ScoringFunction sf4 = ScoringFunction.createStrictlyAtValue(time5);
    Preference pref4 = factory.newPreference(AspectType.END_TIME, sf4);
    Preference[] prefset2 = {pref3, pref4};
    
    ScoringFunction sf5 = ScoringFunction.createStrictlyAtValue(time3);
    Preference pref5 = factory.newPreference(AspectType.START_TIME, sf5);
    ScoringFunction sf6 = ScoringFunction.createStrictlyAtValue(time6);
    Preference pref6 = factory.newPreference(AspectType.END_TIME, sf6);
    Preference[] prefset3 = {pref5, pref6};
    
    List prefsets = new ArrayList();
    prefsets.add(prefset1);
    prefsets.add(prefset2);
    prefsets.add(prefset3);
    
    // create the bulkestimate object by passing in the tasks, a collection
    // of preference sets and the confidence rating you want the allocation
    // result to reach.
    BulkEstimate newbe = factory.newBulkEstimate(ntask, prefsets, 1.0);
    
    // publish the BulkEstimate object
    publishAdd(newbe);
    
  }
  
  // print utility to see what we get back.
  private void printBulkEstimate(BulkEstimate compbe) {
    AllocationResult[] results = compbe.getAllocationResults();
    for (int x = 0; x < results.length; x++) {
      AllocationResult anar = results[x];
      System.out.println("\n\n AllocationResult: ");
      AspectValue[] myresults = anar.getAspectValueResults();
      for (int i = 0; i < myresults.length; i++) {
        AspectValue anav = myresults[i];
        if ( (anav.getAspectType() == 0) || (anav.getAspectType() == 1) ) {
          Date ndate = new Date( (long)anav.getValue() );
          System.out.println("AspectValue type: " + anav.getAspectType() +
                        "   AspectValue value: " + ndate.toString() );
        } else {    
          System.out.println("AspectValue type: " + anav.getAspectType() +
                        "   AspectValue value: " + anav.getValue() );
        }
      }
    }
  }

  
  
  // Predicate for getting all BulkEstimate objects that are complete.
  // Since we know we are the only ones using this, its ours. If this was
  // in a realy plugin in a realy society we would probably want to
  // create a tighter predicate. 
  private static UnaryPredicate bepred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof BulkEstimate) {
	  return ((BulkEstimate)o).isComplete();
        }
	return false;
      }
    };
  }
}
