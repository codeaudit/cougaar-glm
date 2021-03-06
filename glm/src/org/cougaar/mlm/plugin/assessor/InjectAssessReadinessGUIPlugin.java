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

package org.cougaar.mlm.plugin.assessor;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.mlm.plugin.UICoordinator;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.service.LDMService;
import org.cougaar.util.UnaryPredicate;

/**
 * Publishes an AssessReadiness task to the logplan every time the button is pressed.
 *
 **/
public class InjectAssessReadinessGUIPlugin extends ComponentPlugin
{ 
  /** frame for UI **/
  private JFrame frame;

  /** for feedback to user on whether root GLS was successful **/
  JLabel label;

  protected JButton arButton;
  protected JButton rescindButton;

  protected PlanningFactory rootFactory;
  private long rollupSpan = 10;

  private IncrementalSubscription assessReadinessSubscription;
  private static UnaryPredicate assessReadinessPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task t = (Task)o;
	if (t.getVerb().equals(Constants.Verb.AssessReadiness)) {
	  if (!t.getPrepositionalPhrases().hasMoreElements()) {
	    if (t.getPlanElement() == null) {
	      return true;
	    }
	  }
	}
      }
      return false;
    }
  };


  private IncrementalSubscription oplanSubscription;
  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Oplan) {
	return true;
      }
      return false;
    }
  };


  // called by introspection
  public void setLDMService(LDMService service) {
    rootFactory = service.getFactory();
  }

  protected void setupSubscriptions() 
  {	
    assessReadinessSubscription = (IncrementalSubscription) blackboard.subscribe(assessReadinessPredicate);
    oplanSubscription = (IncrementalSubscription) blackboard.subscribe(oplanPredicate);

    // refill contributors Collection on rehydrate
    processAdds(assessReadinessSubscription.getCollection());

    createGUI();
  }	   		 
  
  /**
   * Executes Plugin functionality.
   */
  protected void execute(){

    if (assessReadinessSubscription.hasChanged()) {
      Collection adds = assessReadinessSubscription.getAddedCollection();
      if (adds != null) {
	processAdds(adds);
      }
      Collection changes = assessReadinessSubscription.getChangedCollection();
      if (changes != null) {
	processChanges(changes);
      }
      Collection deletes = assessReadinessSubscription.getRemovedCollection();
      if (deletes !=null) {
	processDeletes(deletes);
      }
    }
    
    if (oplanSubscription.hasChanged()) {
      if (oplanSubscription.size() > 0) 
	if (!arButton.isEnabled())
	  arButton.setEnabled(true);
      else
	arButton.setEnabled(false);
    }
  }

  private long getOplanStartTime() {
    // Yes, I know there can be more than one Oplan. I'll deal with it if I have time
    Oplan oplan = (Oplan) oplanSubscription.iterator().next();
    return oplan.getCday().getTime();
  }

  private long getOplanEndTime() {
    // Yes, I know there can be more than one Oplan. I'll deal with it if I have time
    Oplan oplan = (Oplan) oplanSubscription.iterator().next();
    return oplan.getEndDay().getTime();
  }
    
  private UID getOplanUID() {
    // Yes, I know there can be more than one Oplan. I'll deal with it if I have time
    Oplan oplan = (Oplan) oplanSubscription.iterator().next();
    return oplan.getUID();
  }

  private void createGUI() {
    frame = new JFrame("InjectAssessReadinessGUIPlugin for " + getAgentIdentifier());
    JPanel panel = new JPanel((LayoutManager) null);
    // Create the button
    arButton = new JButton("Publish AssessReadiness Task");
    arButton.setEnabled(false);
    label = new JLabel("No AssessReadiness task have been published.");
    // Register a listener for the check box
    ARButtonListener myARListener = new ARButtonListener();
    arButton.addActionListener(myARListener);
    UICoordinator.layoutButtonAndLabel(panel, arButton, label);

    rescindButton = new JButton("Rescind AssessReadiness Tasks");
    rescindButton.setEnabled(false);
    RescindButtonListener myRescindListener = new RescindButtonListener();
    rescindButton.addActionListener(myRescindListener);
    UICoordinator.layoutButton(panel, rescindButton);

    frame.setContentPane(panel);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }
 
  /** An ActionListener that listens to the GLS buttons. */
  class ARButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      rescindAssessReadiness();
      publishAssessReadiness();
    }
  }

  private void updateLabel() {
    int subSize = assessReadinessSubscription.size();
    label.setText(subSize +  " AssessReadiness tasks on blackboard");
    if (subSize > 0)
      rescindButton.setEnabled(true);
    else
      rescindButton.setEnabled(false);
  }

  private void publishAssessReadiness() {
    blackboard.openTransaction();
    NewTask task = rootFactory.newTask();
    task.setVerb(Constants.Verb.AssessReadiness);
    
    Vector prefs = new Vector(2);
    Preference p = rootFactory.newPreference(AspectType.START_TIME, 
					     ScoringFunction.createStrictlyAtValue(AspectValue.newAspectValue(AspectType.START_TIME, getOplanStartTime())));

    prefs.add(p);
    p = rootFactory.newPreference(AspectType.END_TIME, 
					     ScoringFunction.createStrictlyAtValue(AspectValue.newAspectValue(AspectType.END_TIME, getOplanEndTime())));

    prefs.add(p);
    p = rootFactory.newPreference(AspectType.INTERVAL,
				  ScoringFunction.createStrictlyAtValue(AspectValue.newAspectValue(AspectType.INTERVAL, rollupSpan)));
    prefs.add(p);

    task.setPreferences(prefs.elements());
    task.setContext(new ContextOfUIDs(getOplanUID()));
    blackboard.publishAdd(task);
    blackboard.closeTransactionDontReset();
    updateLabel();
  }


  /** An ActionListener that listens to the Rescind button. */
  class RescindButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      rescindAssessReadiness();
    }
  }

  private void rescindAssessReadiness() {
    blackboard.openTransaction();
    for (Iterator taskIt = assessReadinessSubscription.iterator(); taskIt.hasNext();) {
      Task task = (Task) taskIt.next();
      blackboard.publishRemove(task);
    }
    blackboard.closeTransactionDontReset();
    updateLabel();
  }

  private void processAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Task t = (Task) it.next();
      updateLabel();
    }
  }

  private void processChanges(Collection changes) {
  }

  private void processDeletes(Collection deletes) {
    for (Iterator it = deletes.iterator(); it.hasNext();) {
      Task t = (Task) it.next();
      updateLabel();
    }
  }

  // found by introspection
  public void setParameter(Object param) {
    System.out.println("InjectAssessReadiness.setParameter()");
    for (Iterator paramIt = ((Collection)param).iterator(); paramIt.hasNext();) {
      String sParam = (String)paramIt.next();
      int sep = sParam.indexOf('=');
      if (sep > 0) {
        String name=sParam.substring(0, sep).trim();
        String val=sParam.substring(sep+1).trim();
	if (name.equalsIgnoreCase("rollupspan"))
	  rollupSpan = Long.parseLong(val);
	else
	  System.out.println("InjectAssessReadinessGUIPlugin.parseParameters() unexpected parameter " + sParam);
      }
    }
  }
}











