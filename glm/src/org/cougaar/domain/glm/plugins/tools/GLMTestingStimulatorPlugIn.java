/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plugins.tools;

import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;

import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.Filters;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.TimeSpan;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;

import org.cougaar.domain.glm.callback.GLMOrganizationCallback;
import org.cougaar.domain.glm.callback.GLMOrganizationListener;
import org.cougaar.domain.glm.parser.GLMTaskParser;
import org.cougaar.domain.glm.util.AssetUtil;

import org.cougaar.lib.filter.UTILPlugInAdapter;

import org.cougaar.lib.callback.UTILAllocationListener;
import org.cougaar.lib.callback.UTILAllocationCallback;

import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.lib.util.UTILPreference;

/**
 * <pre>
 * An extension of the GLMStimulator PlugIn that automatically
 * sends batches of a given task and keep track of the time it
 * takes to finish the batch.  
 * </pre>
 */
public class GLMTestingStimulatorPlugIn extends GLMStimulatorPlugIn
    implements UTILAllocationListener {

  /** Add the filter for allocations */
  public void setupFilters () {
    super.setupFilters ();

    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for Allocations...");
    addFilter (myAllocCallback    = createAllocCallback   ());
  }
    
  /***************************/
  /*** Allocation Listener ***/
  /***************************/
    
  protected UTILAllocationCallback myAllocCallback;
  protected UTILAllocationCallback createAllocCallback () { return new UTILAllocationCallback(this);  } 
  protected UTILAllocationCallback getAllocCallback    () { return myAllocCallback; }
    
  public boolean interestingNotification(Task t) { return true; }
  public boolean needToRescind (Allocation alloc) { return false; }
  public boolean handleRescindedAlloc (Allocation alloc) { return false; }
  public void handleRemovedAlloc (Allocation alloc) {}
  public void publishRemovalOfAllocation(Allocation alloc) {}

  /**
   * Everytime a successful allocation returns, we want to send a new batch of
   * the same tasks until the desired total has been sent.  
   *
   * Also keep track of the time spent on each batch.
   *
   * Cache the already handled allocations by their UID's
   */
  public void handleSuccessfulAlloc(Allocation alloc) {
    // Check for a completed batch
    if ((alloc.getReportedResult() != null) && 
	(alloc.getReportedResult().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE)) {
      if (!handledAllocations.contains(alloc.getUID())) {
	// Cache the allocation UID
	handledAllocations.add(alloc.getUID());

	// Print timing information for the completed batch
	String t = getElapsedTime(batchStart);
	System.out.println("\n*** Testing batch #" + batchesSent + " completed in " + t);

	// Cache the timing information
	batchTimes.add(t);

	// Send another batch if needed
	if (batchesSent < totalBatches) {
	  sendTasksAgain(myLabel, myXmlTaskFile);
	  batchesSent++;
	} else {
	  printTestingSummary();
	}
      }
    }
  }

  /**
   * Actually create the GUI window.  
   * Adds an additional field for the number of times the tasks should be sent.
   * 
   * @param infile - default input file. From param 
   *                 "ClusterInputFile" defined in <ClusterName>.env.xml.
   */
  protected void createGUI(String infile) {
    frame = new JFrame(getClusterName () + " - GLMStimulatorPlugIn");
    frame.getContentPane().setLayout(new BorderLayout());

    JPanel panel = new JPanel();
    JButton button = new JButton("Send Tasks");
    JButton button2 = new JButton("Rescind Tasks");
    JTextField text = new JTextField(infile);
    JTextField iterations = new JTextField("1");
    JLabel label = new JLabel("                                             ");

    GLMButtonListener myGLMListener = new GLMButtonListener(label, text, iterations);
    button.addActionListener(myGLMListener);
    button2.addActionListener(myGLMListener);

    panel.add(button);
    panel.add(button2);
	frame.getRootPane().setDefaultButton(button); // hitting return sends the tasks
    panel.add(text);
    panel.add(iterations);

    frame.getContentPane().add("Center", panel);
    frame.getContentPane().add("South", label);
    frame.pack();
    frame.setVisible(true);
  }

  
  /** 
   * <pre>
   * An ActionListener that listens to the GLM buttons. 
   *
   * If the Send Tasks button is clicked, sends the tasks from
   * the file in the text field of the dialog.
   * 
   * Otherwise, if the rescind button is clicked, rescinds the 
   * last task sent.
   * 
   * </pre>
   */
  class GLMButtonListener implements ActionListener {
    GLMButtonListener(JLabel label, JTextField text, JTextField iterations){
      this.label = label;
      this.text = text;
      this.iterations = iterations;
    }
    protected JLabel label;
    protected JTextField text;
    protected JTextField iterations;
    public void actionPerformed(ActionEvent e) {
      String lnfName = e.getActionCommand();

      if (lnfName.equals("Send Tasks")){	  
	// Get name of XML data file
	if (getFinder().locateFile (text.getText()) == null) {
	  label.setText("Couldn't find file. Check path, try again.");
	  return;
	}
	totalBatches = Integer.parseInt(iterations.getText());
	batchesSent = 1;

	testStart = new Date();

	sendTasks(label, text.getText());
	// Should disable the input button after the task has been sent
      } else {
	rescindTasks (label);
      }
    }
  }

  protected void sendTasks (JLabel label, String xmlTaskFile) {
    batchStart = new Date();

    myLabel = label;
    myXmlTaskFile = xmlTaskFile;

	Collection tasks = null;
	tasksSent.clear();
	if (!subordinatesHaveReported ()) {
	  label.setText("No subordinates to task yet.  Wait until they report.");
	} 
	else {
	  try {
	    // Have to do this all within transaction boundary
	    
	    // For some reason, every setXXX (e.g. setVerb) on a NewTask
	    // fires off a changeReport, and these must be inside of a transaction,
	    // apparently.  There is no way currently to temporarily turn off this feature.
	    // GWFV 10/11/2000

		openTransaction();
		// Get the tasks out of the XML file
		tasks = readXmlTasks(xmlTaskFile);

		// First find the organizations that we will allocate to.
		Collection supportedOrgs = getSupportedOrgs ();
		if(supportedOrgs.isEmpty ()){
		  label.setText("No task sent, since no subordinates have reported (yet).");
		} else {
		  allocateTasks (tasks, supportedOrgs);
		  label.setText("Sent 1 batch to " + 
						((Organization) supportedOrgs.iterator ().next()).getItemIdentificationPG().getItemIdentification() + 
						((supportedOrgs.size () > 1) ? ", etc. clusters" : " cluster"));
		}
	  } catch (Exception exc) {
		System.err.println("Could not send tasks.");
		System.err.println(exc.getMessage());
		exc.printStackTrace();
	  }
	  finally{
		closeTransaction(false);
	  }
	} 
  }


  protected void sendTasksAgain (JLabel label, String xmlTaskFile) {
    batchStart = new Date();

    Collection tasks = null;
    try {
      tasks = readXmlTasks(xmlTaskFile);
    
      Collection supportedOrgs = getSupportedOrgs ();
      if(supportedOrgs.isEmpty ()){
	label.setText("No task sent, since no subordinates have reported (yet).");
      } else {
	allocateTasks (tasks, supportedOrgs);
	label.setText("Sent " + batchesSent + " batches to " + 
		      ((Organization) supportedOrgs.iterator ().next()).getItemIdentificationPG().getItemIdentification() + 
		      ((supportedOrgs.size () > 1) ? ", etc. clusters" : " cluster"));
      }
    } catch (Exception exc) {
      System.err.println("Could not send tasks.");
      System.err.println(exc.getMessage());
      exc.printStackTrace();
    }
  }

  protected String getElapsedTime (Date start) {
    Date end = new Date ();
    long diff = end.getTime () - start.getTime ();
    long min  = diff/60000l;
    long sec  = (diff - (min*60000l))/1000l;

    return min + ":" + ((sec < 10) ? "0":"") + sec;
  }

  protected void printTestingSummary() {
    String totalTime = getElapsedTime (testStart);

    System.out.println("\n************************* Testing Summary *************************\n");
    System.out.println("         Batch Number             Batch Time ");
    for (int i=0;i<batchTimes.size();i++)
      System.out.println("            " + (i+1) + "                       " + batchTimes.elementAt(i)); 
    System.out.println("\n         Total Time: " + totalTime);
    System.out.println("\n*******************************************************************\n");

  }

  protected Date testStart = null;
  protected Date batchStart = null;
  protected Vector batchTimes = new Vector();
  protected Collection handledAllocations = new HashSet();

  protected JLabel myLabel = null;
  protected String myXmlTaskFile = "";
  protected int batchesSent = 0;
  protected int totalBatches = 0;
}









