/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.debug.ui;

import javax.swing.border.BevelBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.core.plugin.PlugInDelegate;

/** The sample User Interface PlugIn display.
  This is created and run from the UIPlugIn.start method.  It's run
  in a separate thread so that the UIPlugIn.start method
  can return immediately while this new thread creates
  the user display and awaits user actions.

  The display contains buttons for displaying the log plan, 
  expandable tasks, allocatable workflows, assets and asset schedules.

  A new thread is created for each display; it:
  obtains the requested information;
  creates a window and displays the requested information;
  listens for changes in the cluster (for local cluster only);
  listens for user changes (currently only closing the window).

  This assumes that there is only one plan ("Reality").
  In the future, this should allow the user to:
  select a plan and see the clusters associated with that plan (at
  the local cluster)
  */

public class UIDisplay implements ActionListener, Runnable {
  // contains correspondence between display buttons and cluster boxes
  private Hashtable displayDictionary = new Hashtable();
  private UIPlugIn uiPlugIn;      // plugin we're a part of
  private PlugInDelegate delegate;
  private Plan plan = null;       // plan we're examining
  private ClusterIdentifier myClusterId; // local cluster id
  private String myClusterName;
  private static int WIDTH = 250; // width and height of initial frame
  private static int HEIGHT = 550;
  public static String PLAN_COMMAND = "PLAN";// log plan
  public static String PLAN_DETAILS_COMMAND = "PLAN_DETAILS";// log plan details
  public static String TASKS_COMMAND = "TASKS"; // tasks
  public static String WORKFLOWS_COMMAND = "WORKFLOWS"; // workflows
  public static String ASSETS_COMMAND = "ASSETS"; // assets assigned to cluster
  public static String CLUSTER_ASSETS_COMMAND = "CLUSTER_ASSETS"; 
  public static String ALL_ASSETS_COMMAND = "ALL_ASSETS"; 
  public static String ASSET_SCHEDULE_COMMAND = "ASSET_SCHEDULE"; // assets allocated by cluster
  public static String SINGLE_ASSET_SCHEDULE_COMMAND = "SINGLE_ASSET_SCHEDULE";
  public static String OUTPUT_FILE_COMMAND = "OUTPUT_FILE"; // for DLA work
  public static String PROVIDE_LOG_SUPPORT_COMMAND = "PROVIDE_LOG_SUPPORT";//do log support
  public static String ASSIGN_ASSETS_COMMAND = "ASSIGN_ASSETS";
  public static String CREATE_TASK_COMMAND = "CREATE_TASK"; // general purpose user input
  int gridx = 0; // for grid bag layout
  int gridy = 0;
  Insets noInternalPadding;
  Insets internalPadding;
  Insets labelInternalPadding;
  JPanel panel;

  /** Called from UIPlugIn; records the plugin this is part of for use by
    objects it creates.
    @param uiPlugIn this user interface plugin
    */

  public UIDisplay(UIPlugIn uiPlugIn, PlugInDelegate del) {
    this.uiPlugIn = uiPlugIn;
    delegate = del;
  }

  /** how many gui frames we've exposed (in this vm) **/
  private static int framecount = 0;
  private static Object framecountlock = new Object();
  private static int getFrameCount() {
    synchronized (framecountlock) {
      int i = framecount;
      framecount++;
      return i;
    }
  }

  /** Obtain the information requested and display it.
    Information is obtained in the run method, so that the main user
    interface thread is not hung waiting to obtain information from
    (potentially remote) clusters.
    If the user closes this window (the main display), then the plugin exits.
    In the future, this should query the user for the plan; for now,
    it simply gets the first plan (should be "Reality")
    and exits if it doesn't exist.
    Although the plan is passed to the createRow method and used
    to display potentially different lists of cluster ids, the actionperformed
    method simply gets the plan from the global plan variable; eventually
    it will get it from the user's input.
   */

  public void run() {
    myClusterId = delegate.getClusterIdentifier();
    myClusterName = myClusterId.getAddress();
    // get the plan
    plan = uiPlugIn.getPlan();

    // quit when user closes window

    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { 
      	System.out.println( "UIDisplay().run() calling System.exit through an anonymous WindowAdapter class." );
      	System.exit(0); }
    };

    // create a frame to hold the gui controls
    JFrame frame = new JFrame("*" + myClusterName);
    frame.setForeground(Color.black);
    frame.setBackground(Color.lightGray);
    frame.addWindowListener(windowListener);
    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // center the frame on the display
    
    int fo = getFrameCount() * 20;
    int cx = (screenSize.width - WIDTH)/3+fo;
    int cy = (screenSize.height - HEIGHT)/3+fo;
    frame.setLocation(cx, cy);

    /*
    frame.setLocation(screenSize.width/2 - WIDTH/2,
		      screenSize.height/2 - HEIGHT/2);
    */

    // do layout
    panel = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    panel.setLayout(gbl);
    internalPadding = new Insets(10, 2, 10, 2); // top, left, bottom, right
    noInternalPadding = new Insets(0, 0, 0, 0);
    labelInternalPadding = new Insets(10, 2, 10, 2);
    gridx = 0;
    gridy = 0;
    createRow("Display LogPlan", PLAN_COMMAND, plan);
    createRow("Display LogPlanDetails", PLAN_DETAILS_COMMAND, plan);
    createRow("Display Expandable Tasks", TASKS_COMMAND, plan);
    createRow("Display Allocatable Workflows", WORKFLOWS_COMMAND, plan);
    createRow("Display Asset Quantity", ASSETS_COMMAND, plan);
    createRow("Display Organization Assets", CLUSTER_ASSETS_COMMAND, plan);
    createRow("Display All Assets", ALL_ASSETS_COMMAND, plan);
    createRow("Display All Asset Schedules", ASSET_SCHEDULE_COMMAND, plan);
    createRow("Display Single Asset Schedule", 
	      SINGLE_ASSET_SCHEDULE_COMMAND, plan);
    createRow("Display Output File", OUTPUT_FILE_COMMAND, plan);

    // add buttons for user input
    // assign assets
    //    if (org.cougaar.core.cluster.ClusterImpl.useTH) {
    //      gridx = 0;
    //      JButton button1 = new JButton("Assign Assets");
    //      button1.setActionCommand(ASSIGN_ASSETS_COMMAND);
    //      button1.addActionListener(this);
    //      button1.setEnabled(false);
    //      addComponent(panel, button1, gridx, gridy++, 
    //		   GridBagConstraints.REMAINDER, 1, 
    //		   GridBagConstraints.CENTER, GridBagConstraints.NONE,
    //		   0, 0, internalPadding, 0, 0);
      //    }
    // provide log support
      //    gridx = 0;
      //    JButton button2 = new JButton("Provide Log Support");
      //    button2.setActionCommand(PROVIDE_LOG_SUPPORT_COMMAND);
      //    button2.setEnabled(false);
      //    button2.addActionListener(this);
      //    addComponent(panel, button2, gridx, gridy++, 
      //		 GridBagConstraints.REMAINDER, 1, 
      //		 GridBagConstraints.CENTER, GridBagConstraints.NONE,
      //		 0, 0, internalPadding, 0, 0);

    // create general task
    gridx = 0;
    JButton button3 = new JButton("Create Task");
    button3.setActionCommand(CREATE_TASK_COMMAND);
    button3.addActionListener(this);
    addComponent(panel, button3, gridx, gridy++, 
		 GridBagConstraints.REMAINDER, 1, 
		 GridBagConstraints.CENTER, GridBagConstraints.NONE,
		 0, 0, internalPadding, 0, 0);
    frame.getContentPane().add("Center", panel);
    frame.setVisible(true);
  }

  /** Create  a row of components in the main display.
    Each row contains a button for displaying info from the cluster.
    */

  private void createRow(String buttonLabel, String buttonCommand,
			 Plan plan) {
    gridx = 0;
    JButton button = new JButton(buttonLabel);
    button.setActionCommand(buttonCommand);
    button.addActionListener(this);
    addComponent(panel, button, gridx++, gridy++, 1, 1, 
		 GridBagConstraints.WEST, GridBagConstraints.NONE,
		 0, 0, internalPadding, 1, 0);
  }

  /** For adding components using grid bag layout.
   */

  private void addComponent(Container container, Component component,
			    int gridx, int gridy, 
			    int gridwidth, int gridheight,
			    int anchor, int fill,
			    int ipadx, int ipady, Insets insets,
			    double weightx, double weighty) {
    LayoutManager gbl = container.getLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gridx;
    gbc.gridy = gridy;
    gbc.gridwidth = gridwidth;
    gbc.gridheight = gridheight;
    gbc.fill = fill;
    gbc.anchor = anchor;
    gbc.ipadx = ipadx;
    gbc.ipady = ipady;
    gbc.insets = insets;
    gbc.weightx = weightx;
    gbc.weighty = weighty;
    ((GridBagLayout)gbl).setConstraints(component, gbc);
    container.add(component);
  }

  /** Handle user selection.
    Create a new thread which creates a new window, displays the
    information requested, and listens for changes (from local cluster only).
    Each display is handled by a separate thread, so while one
    display is waiting to collect information from the clusters,
    the user can request another set of information.
    This creates a tree display for log plans, tasks and workflows,
    and a bar graph display for assets and asset schedules.
    @param e the event generated by the user
    */

  public void actionPerformed(ActionEvent e) {
    Runnable display;
    String command = e.getActionCommand();
    // this handles a user input event
    //    if (command.equals(ASSIGN_ASSETS_COMMAND)) {
    //      ((JButton)(e.getSource())).setEnabled(false);
    //      UIInput uiInput = new UIInput(uiPlugIn, ASSIGN_ASSETS_COMMAND);
    //      Thread uiThread = new Thread(uiInput);
    //      uiThread.setName("uiThread:ASSIGN_ASSET_COMMAND:" + uiThread.getName());
    //      uiThread.start(); // go assign assets
    //    }
    //    else if (command.equals(PROVIDE_LOG_SUPPORT_COMMAND)) {
    //    else if (command.equals(PROVIDE_LOG_SUPPORT_COMMAND)) {
    //      ((JButton)(e.getSource())).setEnabled(false); // disable button
    //      UIInput uiInput = new UIInput(uiPlugIn, PROVIDE_LOG_SUPPORT_COMMAND); // create uiinput
    //      Thread uiThread = new Thread(uiInput);
    //      uiThread.setName("uiThread:PROVIDE_LOG_SUPPORT_COMMAND:" + uiThread.getName());
    //      uiThread.start(); // start uiinput
    if (command.equals(CREATE_TASK_COMMAND)) {
      UserInput userInput = new UserInput(uiPlugIn, this, delegate);
      Thread uiThread = new Thread(userInput);
      uiThread.setName("uiThread:CREATE_TASK_COMMAND:" + uiThread.getName());
      uiThread.start();
    } else {      
      String planName = plan.getPlanName();
      if (command.equals(PLAN_COMMAND) ||
	  command.equals(PLAN_DETAILS_COMMAND) ||
	  command.equals(TASKS_COMMAND) ||
	  command.equals(WORKFLOWS_COMMAND) ||
	  command.equals(CLUSTER_ASSETS_COMMAND) ||
	  command.equals(ALL_ASSETS_COMMAND)) {
	  display = new UITreeDisplay(uiPlugIn, planName, myClusterId, command);
	  Thread uiThread = new Thread(display);
	  uiThread.setName("uiThread:DISPLAY:" + uiThread.getName());
	  uiThread.start();
      } else if (command.equals(ASSETS_COMMAND) ||
		 command.equals(ASSET_SCHEDULE_COMMAND) ||
		 command.equals(SINGLE_ASSET_SCHEDULE_COMMAND)) {
	display = new UIBarGraphDisplay(uiPlugIn, planName, myClusterId, command);
	Thread uiThread = new Thread(display);
	uiThread.setName("uiThread:SCHEDULE:" + uiThread.getName());
	uiThread.start();
      } else if (command.equals(OUTPUT_FILE_COMMAND)) {
	display = new UIFile();
	Thread uiThread = new Thread(display);
	uiThread.setName("uiThread:OUTPUTFILE:" + uiThread.getName());
	uiThread.start();
      } else
	System.out.println("Received unknown action event: " + command);
    }
  }
}

