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

package org.cougaar.glm.plugins.tools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cougaar.glm.callback.GLMOrganizationCallback;
import org.cougaar.glm.callback.GLMOrganizationListener;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.parser.GLMTaskParser;
import org.cougaar.glm.util.AssetUtil;
import org.cougaar.lib.filter.UTILPluginAdapter;
import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.TimeSpan;

/**
 * <pre>
 * Parses an XML file defining tasks to send to other clusters.
 * Creates tasks defined in the XML file and sends them 
 * to any subordinate, supporting, or provider clusters.
 *
 * Pops up a dialog box which provides a way to specify the 
 * XML file.  This defaults to whatever is in the ClusterInputFile 
 * parameter.
 *
 * In addition to sending tasks, they can also be rescinded.  
 * Tasks that have been sent are rescinded one at a time, with
 * each button press.
 * 
 * Implements the org listener interface so it can get all
 * reported orgs.
 *
 * (This code evolved from a version in the COUGAAR tree.)
 * </pre>
 */
public class GLMStimulatorPlugin extends UTILPluginAdapter
  implements GLMOrganizationListener {

  /** Add the filter for all organizations... */
  public void setupFilters () {
    super.setupFilters ();
    addFilter (myOrgCallback = createOrganizationCallback ());
    glmAssetUtil = new AssetUtil (logger);
  }

  /** Filter out/listen for all organizations... */
  protected GLMOrganizationCallback getOrganizationCallback    () { return myOrgCallback; }

  /** Create the filter for all organizations... */
  protected GLMOrganizationCallback createOrganizationCallback () { 
    return new GLMOrganizationCallback(this, logger); 
  }

  /** get Organizations */
  public Collection getOrgs () {
    return myOrgCallback.getSubscription ().getCollection ();
  }

  /** 
   * <pre>
   * Are there any subordinates yet?  
   *
   * This could be false if this plugin's cluster is declared 
   * early in the node .ini file.
   *
   * @return true if any subordinates have reported.
   * </pre>
   */
  protected boolean subordinatesHaveReported () {
    return (!myOrgCallback.getSubscription().isEmpty ());
  }

  /**
   * needed to implement the GLMOrganizationListener interface.
   * Does nothing.
   *
   * @see org.cougaar.glm.callback.GLMOrganizationListener
   */
  public void handleChangedOrganization (Enumeration e) {}

  /**
   * needed to implement the GLMOrganizationListener interface.
   * Does nothing.
   *
   * @see org.cougaar.glm.callback.GLMOrganizationListener
   */
  public void handleNewOrganization (Enumeration e) {
    if (!reportedSeenOrgs && myGLMListener != null) {
      myGLMListener.indicateReady();
      reportedSeenOrgs = true;
    }
  }

  boolean reportedSeenOrgs = false;
  
  /**
   * <pre>
   * Helper function to publish a plan element.
   *
   * Expands the direct object and publishes any assets it finds there.
   *
   * Publishes the tasks before the allocations, since they
   * would be automatically removed from the log plan otherwise.
   *
   * Also can provide debug output about whether the task holds a passenger.
   *
   * </pre>
   * @param pe the plan element to publish
   */
  protected void publishMyPlanElement(PlanElement pe) {
    if (isInfoEnabled())
      info (getName () + ".publishMyPlanElement : Publishing " + pe + 
	    " of task " + pe.getTask ());

    // Order matters here!
    publishAdd(pe.getTask());
    Vector createdObjects;
    if (!glmAssetUtil.isPassenger(pe.getTask().getDirectObject ())) {
      if (isInfoEnabled()) 
	info ("not a passenger");
      createdObjects = glmAssetUtil.ExpandAsset (getLDMService().getLDM().getFactory (), pe.getTask ().getDirectObject ());
    }
    else {
      if (isInfoEnabled()) 
	info ("is a passenger");
      createdObjects = new Vector ();
      createdObjects.add (pe.getTask ().getDirectObject ());
    }

    for (Iterator i = createdObjects.iterator (); i.hasNext ();)
      publishAdd (i.next ());
    publishAdd(pe);
  }

  /**
   * Actually create the GUI window.  Two buttons, a text input box,
   * and a status line.
   * 
   * @param infile - default input file. From param 
   *                 "ClusterInputFile" defined in <ClusterName>.env.xml.
   */
  protected void createGUI(String infile) {
    frame = new JFrame(getClusterName () + " - GLMStimulatorPlugin");
    frame.getContentPane().setLayout(new BorderLayout());

    JPanel panel = new JPanel();
    JButton button = new JButton("Send Tasks");
    JButton button2 = new JButton("Rescind Tasks");
    JTextField text = new JTextField(infile);
    JLabel label = new JLabel("No subordinates to task yet.  Wait until they report.");
    myGLMListener = new GLMButtonListener(label, text);
    button.addActionListener(myGLMListener);
    button2.addActionListener(myGLMListener);

    panel.add(button);
    panel.add(button2);
    frame.getRootPane().setDefaultButton(button); // hitting return sends the tasks
    panel.add(text);

    frame.getContentPane().add("Center", panel);
    frame.getContentPane().add("South", label);
    frame.pack();
    frame.setVisible(true);
  }

  /** needed so internal class can find the xml task file */
  public ConfigFinder getFinder () {
    return getConfigFinder();  
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
    GLMButtonListener(JLabel label, JTextField text){
      this.label = label;
      this.text = text;
    }
    protected JLabel label;
    protected JTextField text;
    public void actionPerformed(ActionEvent e) {
      String lnfName = e.getActionCommand();

      if (lnfName.equals("Send Tasks")){	  
	// Get name of XML data file
	if (getFinder().locateFile (text.getText()) == null) {
	  label.setText("Couldn't find file. Check path, try again.");
	  return;
	}
		
	sendTasks (label, text.getText());
      } else {
	rescindTasks (label);
      }
    }
    public void indicateReady () {
      label.setText("Ready to send tasks.");
    }
  }

  /**
   * <pre>
   * Parses the xml file <code>xmlTaskFile</code> and creates a collection of
   * the tasks that will be allocated to organizations.  Then finds all
   * subordinate, supporting, and provider organizations and allocates a copy
   * of each task to each asset.
   *
   * Sets the label on the dialog to provide feedback about the success
   * or failure of this process.
   * 
   * Note that since this is kicked off from the dialog, we must open a
   * transaction to do the publishing.
   *
   * </pre>
   * @param label - a label from the dialog used to provide feedback
   * @param xmlTaskFile - the file that is used to create the tasks
   */
  protected void sendTasks (JLabel label, String xmlTaskFile) {
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

	blackboard.openTransaction();
	// Get the tasks out of the XML file
	tasks = readXmlTasks(xmlTaskFile);
	// First find the organizations that we will allocate to.
	Collection supportedOrgs = getSupportedOrgs ();
	if(supportedOrgs.isEmpty ()){
	  label.setText("No task sent, since no subordinates have reported (yet).");
	} else {
	  allocateTasks (tasks, supportedOrgs);
	  label.setText("Sent task(s) to " + 
			((Organization) supportedOrgs.iterator ().next()).getItemIdentificationPG().getItemIdentification() + 
			((supportedOrgs.size () > 1) ? ", etc. clusters" : " cluster"));
	}
      } catch (Exception exc) {
	System.err.println("Could not send tasks.");
	System.err.println(exc.getMessage());
	exc.printStackTrace();
      }
      finally{
	blackboard.closeTransactionDontReset();
      }
    } 
  }

  public static final Role SUPPORTING = Role.getRole("Supporting");

  /** 
   * Find the organizations that we will allocate to.
   * @return Collection of supporting, subordinate, and provider 
   *         organizations
   */

  protected Collection getSupportedOrgs () {
    Collection orgs = getOrgs();
    Collection returnedOrgs = new HashSet ();
	
    for (Iterator iter = orgs.iterator (); iter.hasNext ();) {
      Organization org = (Organization)iter.next();
      //Look in self orgs relationship schedule
      if (org.isSelf()){
	RelationshipSchedule schedule = org.getRelationshipSchedule();

	Collection subordinates = org.getSubordinates(TimeSpan.MIN_VALUE,
						      TimeSpan.MAX_VALUE);
	if (!subordinates.isEmpty()){
	  for (Iterator it = subordinates.iterator(); it.hasNext();){
	    returnedOrgs.add(schedule.getOther((Relationship)it.next()));
	  }
	}
	//		returnedOrgs.addAll (subordinates);

	Collection supporting = 
	  schedule.getMatchingRelationships(SUPPORTING);
	if (!supporting.isEmpty()){
	  for (Iterator it = supporting.iterator(); it.hasNext();){
	    returnedOrgs.add(schedule.getOther((Relationship)it.next()));
	  }
	}
	//		returnedOrgs.addAll (schedule.getOther(supporting);

	//Assume providers use provider suffix
	Collection providers = 
	  schedule.getMatchingRelationships(Constants.RelationshipType.PROVIDER_SUFFIX,
					    TimeSpan.MIN_VALUE,
					    TimeSpan.MAX_VALUE);

	if (!providers.isEmpty()){
	  for (Iterator it = providers.iterator(); it.hasNext();){
	    returnedOrgs.add(schedule.getOther((Relationship)it.next()));
	  }
	}
	//	    returnedOrgs.addAll (providers);
      }
    }
    return returnedOrgs;
  }

  /**
   * Allocate the tasks to each supportedOrg.
   *
   * @param tasks to send 
   * @param supportedOrgs - the orgs to send them to
   */
  protected void allocateTasks (Collection tasks, Collection supportedOrgs) {
    boolean sentOriginals = false;
	
    for (Iterator iter = supportedOrgs.iterator(); iter.hasNext ();) {
      Organization supportedOrg = (Organization) iter.next();
      for (Iterator iter2 = tasks.iterator (); iter2.hasNext ();){
	Task task = (Task)iter2.next();
	Task actualTask = (sentOriginals) ? cloneTask(task) : task;
	allocateToOrg (actualTask, supportedOrg);
      }
      sentOriginals = true;
    }
  }
  
  /**
   * Clone the input task
   *
   * @param toClone task to copy
   * @return the copy
   */
  protected Task cloneTask(Task toClone) {
    NewTask task = ldmf.newTask();

    task.setDirectObject(toClone.getDirectObject());
    task.setPrepositionalPhrases(toClone.getPrepositionalPhrases());
    task.setVerb(toClone.getVerb ());
    task.setPlan(toClone.getPlan ());
    task.setPreferences(toClone.getPreferences ());
    task.setPriority(toClone.getPriority ());
    task.setSource(toClone.getSource());
    return task;
  }

  /**
   * Create and publish the allocation.
   *
   * @param task to allocate
   * @param supportedOrg org to allocate to
   */
  protected void allocateToOrg (Task task, Organization supportedOrg) {
    Allocation alloc = (Allocation) 
      allocHelper.makeAllocation (this,
				   ldmf,
				   ldmf.getRealityPlan(), 
				   task,
				   supportedOrg,
				   prefHelper.getReadyAt(task),
				   prefHelper.getBestDate(task),
				   allocHelper.HIGHEST_CONFIDENCE,
				   Constants.Role.TRANSPORTER);
    if (isInfoEnabled())
      info(getName () +" allocating to " + supportedOrg);

    publishMyPlanElement(alloc);
    tasksSent.add (task);
  }
  
  /**
   * When the rescind task button is pressed, rescind the task.
   *
   * @param label provides way to give feedback
   */
  protected void rescindTasks (JLabel label) {
    if (tasksSent.size() == 0){
      label.setText("No tasks to Rescind.");
    } else {
      try {
	blackboard.openTransaction();
	Iterator iter = tasksSent.iterator ();
	Object removed = iter.next ();
	iter.remove ();
		
	if (isInfoEnabled())
	  info ("GLMStimulatorPlugin - Removing " + removed);
	publishRemove(removed);
	tasksSent.remove(removed);
	label.setText("Rescinded last task. " + tasksSent.size () + " left.");
      }catch (Exception exc) {
	logger.error ("exception ", exc);
      }
      finally{
	blackboard.closeTransactionDontReset();
      }
    }
  }
  
  /**
   * Reads the ClusterInputFile parameter, brings up the GUI.
   */
  public void localSetup () {
    String infile = null;
    try{infile=getMyParams ().getStringParam("ClusterInputFile");}
    catch(Exception e){infile = "                          ";}
    createGUI(infile);
  }

  /**
   * Parse the xml file and return the COUGAAR tasks.
   *
   * @param  xmlTaskFile file defining tasks to stimulate cluster with
   * @return Collection of tasks defined in xml file
   */
  protected Collection readXmlTasks(String xmlTaskFile) {
    Collection tasks = null;
    try {
      GLMTaskParser tp = new GLMTaskParser(xmlTaskFile, ldmf, 
					   getAgentIdentifier(),
					   getConfigFinder(),
					   getLDMService().getLDM(),
					   logger);
      tasks = UTILAllocate.enumToList (tp.getTasks());
    } 
    catch( Exception ex ) {
      logger.error ("Got error reading xml file "  + xmlTaskFile, ex);
    }
    return tasks;
  }

  /** frame for 2-button UI */
  JFrame frame;

  /** Collection of tasks that have been sent.  Needed for later rescinds */
  protected Collection tasksSent = new HashSet();

  /** The callback mediating the org subscription */
  protected GLMOrganizationCallback myOrgCallback;
  GLMButtonListener myGLMListener;

  AssetUtil glmAssetUtil;
}






