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


package org.cougaar.mlm.debug.ui;

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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.Capability;
import org.cougaar.planning.ldm.plan.ClusterObjectFactory;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.plugin.legacy.PluginDelegate;

/** Supports task creation by users.
 */

public class UserInput implements ActionListener, Runnable {
  static String CREATE_TASK = "Create Task";
  static String SET_DESTINATION_CLUSTER = "Set Destination Cluster: ";
  static String SET_VERB = "Verb: ";
  static String SET_DIRECT_OBJECT_CLUSTER_ASSET = "Direct Object Cluster Asset: ";
  static String SET_DIRECT_OBJECT_PHYSICAL_ASSET = "Direct Object Physical Asset: ";
  static String SET_DIRECT_OBJECT_ASSET_QUANTITY = "Direct Object Asset Quantity: ";
  static String SET_DIRECT_OBJECT_CAPABILITY = "Direct Object Capabilities: ";
  static String SET_PHRASE_PREPOSITION = "Phrase Preposition: ";
  static String SET_PHRASE_CLUSTER_ASSET = "Phrase Cluster Asset: ";
  static String SET_PHRASE_PHYSICAL_ASSET = "Phrase Physical Asset: ";
  static String SET_PHRASE_ASSET_QUANTITY = "Phrase Asset Quantity: ";
  static String SET_PHRASE_CAPABILITY = "Phrase Capabilities: ";
  static String SET_START_DATE = "Set Start Date: ";
  static String SET_END_DATE = "Set End Date: ";
  static String SET_BEST_DATE = "Set Best Date: ";
  String sourceCluster = "Commander"; // temporary, should be this cluster
  // set from user input
  String destinationCluster = "";
  String verb = "";
  String objectClusterAsset = "";
  String objectPhysicalAsset = "";
  String objectCapability = "";
  Vector objectCapabilities = new Vector(4);
  String phrasePreposition = "";
  String phraseClusterAsset = "";
  String phrasePhysicalAsset = "";
  String phraseCapability = "";
  Vector phraseCapabilities = new Vector(4);
  String defaultStartDate = "September 01, 1998 8:00 AM";
  String defaultEndDate = "December 31, 1998 8:00 AM";
  String defaultBestDate = "September 01, 1998 8:00 AM";
  String startDate = defaultStartDate;
  String endDate = defaultEndDate;
  String bestDate = defaultBestDate;
  int objectAssetQuantity = 0;
  int phraseAssetQuantity = 0;
  int WIDTH = 600; // width and height of frame
  int HEIGHT = 500;
  JComboBox box;
  JTextField startDateField;
  JTextField endDateField;
  JTextField bestDateField;
  ScrollingTextLine scrollingTextLine;
  int gridx = 0; // for grid bag layout
  int gridy = 0;
  Insets noInternalPadding;
  Insets internalPadding;
  Insets labelInternalPadding;
  JPanel panel;
  UIPlugin uiPlugin;
  UIDisplay uiDisplay;
  ClusterObjectFactory cof;
  Plan realityPlan;
  String planName;
  PluginDelegate delegate;


  /** Initialize values used by user input thread.
    @param uiPlugin this plugin
    @param uiDisplay the main display object
   */
  public UserInput(UIPlugin uiPlugin, UIDisplay uiDisplay, PluginDelegate delegate) {
    this.uiPlugin = uiPlugin;
    this.uiDisplay = uiDisplay;
    this.delegate = delegate;
    cof = delegate.getFactory();
    realityPlan = cof.getRealityPlan();
    planName = realityPlan.getPlanName();
  }

  /** Create the user input frame which allows the user to input
    tasks, specifying each field in the task.
   */

  public void run() {
    Vector choices;

    // create window
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { e.getWindow().dispose(); }
    };
    JFrame frame = new JFrame("COUGAAR User Input");
    frame.setForeground(Color.black);
    frame.setBackground(Color.lightGray);
    frame.addWindowListener(windowListener);
    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
		      screenSize.height/2 - HEIGHT/2);

    // physical assets
    Vector physicalAssets = new Vector(3);
    physicalAssets.addElement("Truck");
    physicalAssets.addElement("Solenoid");

    // capabilities
    Vector capabilities = new Vector(6);
    capabilities.addElement(Capability.SPAREPARTSPROJECTOR);
    capabilities.addElement(Capability.SPAREPARTSPROVIDER);
    capabilities.addElement(Capability.SUPERIOR);
    capabilities.addElement(Capability.SPAREPART);
    capabilities.addElement(Capability.MAJORENDITEM);
    capabilities.addElement(Capability.SUBORDINATE);

    // verb
    Vector verbs = new Vector(11);
    verbs.addElement(Constants.Verb.TRANSPORT);
    verbs.addElement(Constants.Verb.MAINTAIN);
    verbs.addElement(Constants.Verb.SUPPLY);
    verbs.addElement(Constants.Verb.ARM);
    verbs.addElement(Constants.Verb.FUEL);
    verbs.addElement(Constants.Verb.GETLOGSUPPORT);
    verbs.addElement(Constants.Verb.MANAGE);
    verbs.addElement(Constants.Verb.DETERMINEREQUIREMENTS);
    verbs.addElement(Constants.Verb.USERINPUT);
    verbs.addElement(Constants.Verb.SUPPORTREQUEST);
    verbs.addElement(Constants.Verb.REPORTFORDUTY);
    verbs.addElement(Constants.Verb.REPORTFORSERVICE);
    verbs.addElement(Constants.Verb.TRANSPORTATIONMISSION);

    // prepositional phrases: enumeration of phrase
    Vector prepositions = new Vector(5);
    prepositions.addElement(Constants.Preposition.WITH);
    prepositions.addElement(Constants.Preposition.TO);
    prepositions.addElement(Constants.Preposition.FROM);
    prepositions.addElement(Constants.Preposition.FOR);

    // do layout
    panel = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    panel.setLayout(gbl);
    internalPadding = new Insets(2, 10, 2, 10);
    noInternalPadding = new Insets(0, 0, 0, 0);
    labelInternalPadding = new Insets(0, 100, 0, 0);

    addClusterComboBox(SET_DESTINATION_CLUSTER);
    addComboBox(SET_VERB, verbs);
    addClusterComboBox(SET_DIRECT_OBJECT_CLUSTER_ASSET);
    addComboBox(SET_DIRECT_OBJECT_PHYSICAL_ASSET, physicalAssets);
    addComboBox(SET_DIRECT_OBJECT_CAPABILITY, capabilities);
    addTextField(SET_DIRECT_OBJECT_ASSET_QUANTITY, 6, "");
    addComboBox(SET_PHRASE_PREPOSITION, prepositions);
    addClusterComboBox(SET_PHRASE_CLUSTER_ASSET);
    addComboBox(SET_PHRASE_PHYSICAL_ASSET, physicalAssets);
    addComboBox(SET_PHRASE_CAPABILITY, capabilities);
    addTextField(SET_PHRASE_ASSET_QUANTITY, 6, "");
    startDateField = addTextField(SET_START_DATE, 20, defaultStartDate);
    endDateField = addTextField(SET_END_DATE, 20, defaultEndDate);
    bestDateField = addTextField(SET_BEST_DATE, 20, defaultBestDate);

    // display the task that the user is composing
    gridx = 0;
    scrollingTextLine = new ScrollingTextLine(50);
    addComponent(panel, scrollingTextLine, gridx, gridy++, 
		 GridBagConstraints.REMAINDER, 1, 
		 GridBagConstraints.CENTER, GridBagConstraints.NONE,
		 0, 0, internalPadding, 0, 0);

    // button to create new task
    gridx = 0;
    JButton button = new JButton(CREATE_TASK);
    button.setActionCommand(CREATE_TASK);
    button.addActionListener(this);
    addComponent(panel, button, gridx, gridy++, 
		 GridBagConstraints.REMAINDER, 1, 
		 GridBagConstraints.CENTER, GridBagConstraints.NONE,
		 0, 0, internalPadding, 0, 0);

    JScrollPane scroller = new JScrollPane();
    scroller.getViewport().add(panel);
    frame.getContentPane().add("Center", scroller);
    frame.setVisible(true);
  }

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

  private void addComboBox(String actionCommand, Vector choices) {
    gridx = 0;
    JLabel label = new JLabel(actionCommand);
    addComponent(panel, label, gridx++, gridy, 1, 1, 
		 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
		 0, 0, labelInternalPadding, 1, 0);
    JComboBox box = new JComboBox();
    for (int i = 0; i < choices.size(); i++)
      box.addItem(choices.elementAt(i));
    box.addItem("");
    box.setSelectedIndex(choices.size()); // unspecified to start
    box.setEditable(true); // allow user to add their own choices
    box.setActionCommand(actionCommand);
    box.addActionListener(this);
    addComponent(panel, box, gridx++, gridy++, 1, 1, 
		 GridBagConstraints.WEST, GridBagConstraints.NONE,
		 0, 0, internalPadding, 1, 0);
  }

  private void addClusterComboBox(String actionCommand) {
    gridx = 0;
    JLabel label = new JLabel(actionCommand);
    addComponent(panel, label, gridx++, gridy, 1, 1, 
		 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
		 0, 0, labelInternalPadding, 1, 0);
    JComboBox box = new JComboBox();
    Vector choices = uiPlugin.getClusterNames();
    for (int i = 0; i < choices.size(); i++)
      box.addItem(choices.elementAt(i));
    box.addItem("");
    box.setSelectedIndex(box.getItemCount()-1); // unspecified to start
    box.setActionCommand(actionCommand);
    box.addActionListener(this);
    addComponent(panel, box, gridx++, gridy++, 1, 1, 
		 GridBagConstraints.WEST, GridBagConstraints.NONE,
		 0, 0, internalPadding, 1, 0);
  }

  private JTextField addTextField(String title, int nColumns, String init) {
    gridx = 0;
    JLabel label = new JLabel(title);
    addComponent(panel, label, gridx++, gridy, 1, 1, 
		 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
		 0, 0, labelInternalPadding, 1, 0);
    JTextField textField = new JTextField(nColumns);
    textField.setActionCommand(title); // title and action command are the same
    textField.addActionListener(this);
    textField.setText(init);
    addComponent(panel, textField, gridx++, gridy++, 1, 1, 
		 GridBagConstraints.WEST, GridBagConstraints.NONE,
		 0, 0, internalPadding, 1, 0);
    return textField;
  }

  private void displayTask() {
    String objectQuantity = "";
    String phraseQuantity = "";
    if (objectAssetQuantity != 0)
      objectQuantity = String.valueOf(objectAssetQuantity);
    if (phraseAssetQuantity != 0)
      phraseQuantity = String.valueOf(phraseAssetQuantity);
    String s1 = "";
    for (int i = 0; i < objectCapabilities.size(); i++)
      s1 = s1 + objectCapabilities.elementAt(i);
    String s2 = "";
    for (int i = 0; i < phraseCapabilities.size(); i++)
      s2 = s2 + phraseCapabilities.elementAt(i);
    String s = planName + " " +
      sourceCluster + "->" + destinationCluster + " " +
      verb + " " +
      objectClusterAsset + " " + objectPhysicalAsset + " " + 
      s1 + " " + objectQuantity + " " +
      phrasePreposition + " " +
      phraseClusterAsset + " " + phrasePhysicalAsset + " " +
      s2 + " " + phraseQuantity + " " +
      startDate + " " + endDate + " " + bestDate;
    s.trim();
    scrollingTextLine.setText(s);
  }


  private void resetForm(JPanel panel) {
    int count = panel.getComponentCount();
    for (int i = 0; i < count; i++) {
      Component component = panel.getComponent(i);
      if (component instanceof JPanel)
	resetForm((JPanel)component);
      if (component instanceof JComboBox) 
	((JComboBox)component).setSelectedItem("");
      else if (component instanceof JTextField) {
	JTextField textField = (JTextField)component;
	if (textField.isEditable()) {
	  if (textField.equals(startDateField))
	    textField.setText(defaultStartDate);
	  else if (textField.equals(endDateField))
	    textField.setText(defaultEndDate);
	  else if (textField.equals(bestDateField))
	    textField.setText(defaultBestDate);
	  else
	    textField.setText("");
	}
	scrollingTextLine.setText(""); 
      }
    }
  }

  private void initDefaults() {
    destinationCluster = "";
    verb = "";
    objectClusterAsset = "";
    objectPhysicalAsset = "";
    objectCapability = "";
    objectCapabilities = new Vector(4);
    phrasePreposition = "";
    phraseClusterAsset = "";
    phrasePhysicalAsset = "";
    phraseCapability = "";
    phraseCapabilities = new Vector(4);
    objectAssetQuantity = 0;
    phraseAssetQuantity = 0;
    startDate = defaultStartDate;
    endDate = defaultEndDate;
    bestDate = defaultBestDate;
  }

  /** User made a selection.  We need to set the value of the appropriate
    task object and add the user's input to the task displayed.
    */

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    Object source = e.getSource();

    if (source instanceof JButton) {
      if (command.equals(CREATE_TASK)) {
	UserInputTask newTask = 
            new UserInputTask(uiPlugin, delegate, destinationCluster,
			      objectClusterAsset, objectPhysicalAsset, 
			      objectCapabilities, objectAssetQuantity, 
			      phrasePreposition,
			      phraseClusterAsset, phrasePhysicalAsset,
			      phraseCapabilities, phraseAssetQuantity,
			      verb, startDate, endDate, bestDate);
	newTask.addToLogPlan();
	initDefaults();
	resetForm(panel);
      }
    } else if (source instanceof JComboBox) {
      JComboBox box = (JComboBox)source;
      Object o = box.getSelectedItem();
      String s = "";
      if (o instanceof String)
	s = (String)o;
      // if user added new value, then add it to list of choices
      if (box.getSelectedIndex() == -1)
	box.addItem(s);
      if (command.equals(SET_DESTINATION_CLUSTER)) 
	destinationCluster = s;
      else if (command.equals(SET_DIRECT_OBJECT_CLUSTER_ASSET))
	objectClusterAsset = s;
      else if (command.equals(SET_PHRASE_CLUSTER_ASSET))
	phraseClusterAsset = s;
      else if (command.equals(SET_VERB))
	verb = s;
      else if (command.equals(SET_DIRECT_OBJECT_PHYSICAL_ASSET))
	objectPhysicalAsset = s;
      else if (command.equals(SET_DIRECT_OBJECT_CAPABILITY)) {
	if (!s.equals(""))
	  objectCapabilities.addElement(s);
      }
      else if (command.equals(SET_PHRASE_PREPOSITION))
	phrasePreposition = s;
      else if (command.equals(SET_PHRASE_PHYSICAL_ASSET))
	phrasePhysicalAsset = s;
      else if (command.equals(SET_PHRASE_CAPABILITY)) {
	if (!s.equals(""))
	  phraseCapabilities.addElement(s);
      }
    } else if (source instanceof JTextField) {
      JTextField textField = (JTextField)source;
      String s = textField.getText();
      if (command.equals(SET_DIRECT_OBJECT_ASSET_QUANTITY))
	objectAssetQuantity = Integer.parseInt(s);
      else if (command.equals(SET_PHRASE_ASSET_QUANTITY))
	phraseAssetQuantity = Integer.parseInt(s);
      else if (command.equals(SET_START_DATE))
	startDate = s;
      else if (command.equals(SET_END_DATE))
	endDate = s;
      else if (command.equals(SET_BEST_DATE))
	bestDate = s;
    }
    displayTask();
  }

}

