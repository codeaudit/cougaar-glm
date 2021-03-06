
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

package org.cougaar.mlm.plugin.generic;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;

/**
 * Allocate to sink until user presses button (like UniversalAllocator),
 * then remove the allocation and actually allocate to an organization
 * (like GenericTablePlugin).  Need a GUI to control the button and
 * special checks for subscription added/all.
 * <p>
 * @see GenericTablePlugin for XML input usage
 * @see org.cougaar.mlm.plugin.sample.UniversalAllocatorPlugin for sink info
 */
public class GenericBufferedTablePlugin extends GenericTablePlugin {

  // Create a single dummy asset to which to allocate all appropriate tasks
  private Asset sink_asset = null;

  protected void setupSubscriptions() {
    super.setupSubscriptions();
    sink_asset = theLDMF.createPrototype("AbstractAsset", "GenericSink");
    publishAdd(sink_asset);
    createGUI();
  }

  protected void initializeSubscriptions() {
    // want subscriptions running
    makeSubscriptions();
  }

  public synchronized void execute() {
    // allocate to sink until button pressed
    int sinkAllocCounter = 0;
    for (int i = 0; i < tasksSub.length; i++) {
      if (!(tasksSub[i].hasChanged())) 
        continue;
      Enumeration eTasks = tasksSub[i].getAddedList();
      if (!(eTasks.hasMoreElements()))
        continue;
      CommandInfo c = allCommands[i];
      if (c.type_id == CommandInfo.TYPE_ALLOCATE) {
        // allocate to sink for now
        do {
          Task theTask = (Task) eTasks.nextElement();
          if (getTaskPlanElement(theTask, allocationsSub[i]) == null) {
            AllocationResult allocation_result = 
              computeAllocationResult(theTask);
            Allocation allocation = 
              theLDMF.createAllocation(
                theLDMF.getRealityPlan(),
                theTask,
                sink_asset,
                allocation_result,
                Role.BOGUS);
            publishAdd(allocation);
            sinkAllocCounter++;
          }
        } while (eTasks.hasMoreElements());
      } else if (c.type_id == CommandInfo.TYPE_EXPAND) {
        // expand as usual
        TaskInfo[] toTasks = ((ExpandCommandInfo)c).expandTasks;
        do {
          Task theTask = (Task) eTasks.nextElement();
	  // Need a sub to expansions to do this....
	  //          if (getTaskPlanElement(theTask, allocationsSub[i]) == null) {
          if (theTask.getPlanElement() == null) {
            for (int j = 0; j < toTasks.length; j++) {
              doExpansion(toTasks[j], theTask);
            }
          }
        } while (eTasks.hasMoreElements());
      } else {
        // no other commands!
      }
    }

    for (int i = 0; i < allocationsSub.length; i++) {
      if (allocationsSub[i].hasChanged()) {
        updateAllocationResult(allocationsSub[i]);
      }
    }

    // update GUI
    incrementSinkAllocCounter(sinkAllocCounter);
  }

  public synchronized void buttonExecute() {
    // unallocate tasks allocated to sink and reallocate
    int orgAllocCounter = 0;
    for (int i = 0; i < tasksSub.length; i++) {
      Enumeration eTasks = tasksSub[i].elements();
      if (!(eTasks.hasMoreElements()))
        continue;
      CommandInfo c = allCommands[i];
      if (c.type_id == CommandInfo.TYPE_ALLOCATE) {
        // allocate
        Organization capableOrg = null;
        do {
          Task theTask = (Task) eTasks.nextElement();
          PlanElement pe = getTaskPlanElement(theTask, allocationsSub[i]);
          if ((pe instanceof Allocation) &&
              (((Allocation)pe).getAsset() == sink_asset)) {
            // find capable org
            if (capableOrg == null) {
              capableOrg = 
                findCapableOrganization((AllocateCommandInfo)c);
              if (capableOrg == null) {
                // no capable org found!
                break;
              }
            }
            // unallocate from sink
            publishRemove(pe);
            // allocate to org
            doAllocation(capableOrg, theTask);
            orgAllocCounter++;
          }
        } while (eTasks.hasMoreElements());
      } else if (c.type_id == CommandInfo.TYPE_EXPAND) {
        // expand as usual
        TaskInfo[] toTasks = ((ExpandCommandInfo)c).expandTasks;
        do {
          Task theTask = (Task) eTasks.nextElement();
          if (theTask.getPlanElement() == null) {
	    //          if (getTaskPlanElement(theTask, allocationsSub[i]) == null) {
            for (int j = 0; j < toTasks.length; j++) {
              doExpansion(toTasks[j], theTask);
            }
          }
        } while (eTasks.hasMoreElements());
      } else {
        // no other commands!
      }
    }

    for (int i = 0; i < allocationsSub.length; i++) {
      updateAllocationResult(allocationsSub[i]);
    }

    // update GUI
    decrementSinkAllocCounter(orgAllocCounter);
  }

  protected void executeCommands() {
    // Have to do this transaction boundary
    openTransaction();
    buttonExecute();
    closeTransactionDontReset();
  }

  // Return an allocation result that gives back a successful/optimistic answer
  // consisting of the best value for every aspect
  private AllocationResult computeAllocationResult(Task task) 
  {
    int num_prefs = 0;
    Enumeration prefs = task.getPreferences();
    while (prefs.hasMoreElements()) {
      prefs.nextElement(); 
      num_prefs++; 
    }
    int []types = new int[num_prefs];
    double []results = new double[num_prefs];
    prefs = task.getPreferences();

    int index = 0;
    while (prefs.hasMoreElements()) {
      Preference pref = (Preference)prefs.nextElement();
      types[index] = pref.getAspectType();
      results[index] = pref.getScoringFunction().getBest().getValue();
      index++;
    }

    AllocationResult result = 
      theLDMF.newAllocationResult(
        1.0, // Rating,
        true, // Success,
        types,
        results);
    return result;
  }

  JButton executeButton;
  JLabel sinkAllocCounterLabel;
  int sinkAllocCounter = 0;

  /** called by synchronized method! **/
  protected void incrementSinkAllocCounter(int nMoreAllocs) {
    if (nMoreAllocs > 0) {
      sinkAllocCounter += nMoreAllocs;
      sinkAllocCounterLabel.setText(Integer.toString(sinkAllocCounter));
    }
  }

  /** called by synchronized method! **/
  protected void decrementSinkAllocCounter(int nFewerAllocs) {
    if (nFewerAllocs > 0) {
      sinkAllocCounter -= nFewerAllocs;
      sinkAllocCounterLabel.setText(Integer.toString(sinkAllocCounter));
    }
  }

  /**
   * An ActionListener that listens to the button.
   */
  class MyListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JButton button = (JButton)e.getSource();
      if (button == executeButton) {
        try {
          executeCommands();
        } catch (Exception exc) {
          System.err.println("Could not execute button: " +
             e.getActionCommand());
        }
      }
    }
  }

  protected String getClusterID() {
    String s;
    try {
      s = getAgentIdentifier().toString();
    } catch (Exception e) {
      s = "UNKNOWN";
    }
    int beginIdx = 0;
    if (s.startsWith("<")) {
      beginIdx++;
    }
    int endIdx = s.length();
    if (s.endsWith(">")) {
      endIdx--;
    }
    // substring notices when indexes are (0, len)
    return s.substring(beginIdx, endIdx);
  }

  protected void createGUI() {
    executeButton = new JButton("Execute");
    executeButton.addActionListener(new MyListener());
    sinkAllocCounterLabel = new JLabel("<        >", JLabel.RIGHT);
    sinkAllocCounterLabel.setText("0");

    String clusterID = getClusterID();

    JFrame frame =
      new JFrame("GenericBufferedTablePlugin "+clusterID);
    frame.setLocation(0, 0);
    JPanel rootPanel = new JPanel((LayoutManager) null);
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    //gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.BOTH;
    rootPanel.setLayout(gbl);

    JLabel titleLabel = 
      new JLabel("Buffer Allocator for "+clusterID);
    titleLabel.setForeground(Color.blue);
    gbc.gridy = 1;
    gbl.setConstraints(titleLabel, gbc);
    rootPanel.add(titleLabel);

    JPanel sinkPanel = new JPanel();
    sinkPanel.setLayout(new GridLayout(1,2));
    JLabel sinkLabel = 
      new JLabel("Number of tasks allocated to sink: ");
    sinkLabel.setForeground(Color.blue);
    sinkPanel.add(sinkLabel);
    sinkPanel.add(sinkAllocCounterLabel);
    gbc.gridy = 2;
    gbl.setConstraints(sinkPanel, gbc);
    rootPanel.add(sinkPanel);

    JLabel instructionsLabel = 
      new JLabel("Press button to send buffered "+clusterID+" tasks");
    instructionsLabel.setForeground(Color.blue);
    gbc.gridy = 3;
    gbl.setConstraints(instructionsLabel, gbc);
    rootPanel.add(instructionsLabel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(executeButton);
    gbc.gridy = 4;
    gbl.setConstraints(buttonPanel, gbc);
    rootPanel.add(buttonPanel);

    frame.setContentPane(rootPanel);
    frame.pack();
    frame.setVisible(true);
  }
}
