/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Displays forms that allow user to select cluster, plan objects,
 * and specific plan objects.  POSTs parameters to Plan Service Provider
 * on appropriate cluster, and displays results.
 */

public class XMLDisplay implements ActionListener {
  static int nTerms = 4; // number of terms in expression for selecting objects
  // labels on the display, also used as action commands
  static final String SET_CLUSTER="Select Cluster:";
  static final String SET_PLAN_OBJECT="Select Plan Object:";
  static final String SET_REQUESTED_FIELDS="Select Attributes:";
  static final String SUBMIT_REQUEST="Submit Request";
  static final String SET_EXPRESSION="Set Expression";
  static final String LIMIT_COMMAND = "LIMIT";
  // values displayed in comparator list
  String[] comparatorValues = { "=", "!=", "<", ">", "<=", ">=" };
  // indices in select plan object list
  static final int SELECTED_PLANELEMENTS = 0;
  static final int SELECTED_TASKS = 1;
  static final int SELECTED_ASSETS = 2;
  static final int SELECTED_WORKFLOWS = 3;
  static final int SELECTED_POLICIES = 4;
  static final int SELECTED_AGGREGATION = 5;
  static final int SELECTED_ALLOCATION = 6;
  static final int SELECTED_ASSETTRANSFER = 7;
  static final int SELECTED_EXPANSION= 8;
  static final int SELECTED_ALL = 9;
  ScrollingTextLine scrollingTextLine;
  int gridx = 0; // for grid bag layout
  int gridy = 0;
  Insets internalPadding;
  // JComboBoxes with names, values, comparators, and combiners
  Vector userInputs = new Vector(4*nTerms);
  // JComboBoxes with names (of plan objects)
  Vector userInputNameBoxes = new Vector(nTerms);
  // JComboBoxes with values (of plan objects)
  Vector userInputValueBoxes = new Vector(nTerms);
  // JComboBox for cluster names
  JComboBox clusterNameBox;
  // JComboBox for plan object names
  JComboBox planObjectClassBox;
  JComboBox requestedFieldsBox;
  String requestedFields = "";
  // cluster names and URLs from a plan server at a cluster
  Vector clusterNames = null;
  Hashtable clusterURLs = null;
  String clusterURL = null;
  boolean isApplet = false;
  // PSP to which to connect
  String PSP_package;
  String PSP_id;
  String title; // title on the user interface window
  String[][] fieldNames; // field names to display for log plan objects
  boolean initted = false;
  String clusterHost; // supplied by user, unless running applet
  String clusterPort; // supplied by user, unless running applet
  boolean limit = false; // set by user to limit number of objects returned

  /** Called by XMLApplet as
    XMLDisplay(getContentPane(), getCodeBase())
    */

  public XMLDisplay(Container container, String clusterURL) {
    isApplet = true;
    this.clusterURL = clusterURL;
    PSP_package = XMLClientConfiguration.PSP_package;
    PSP_id = XMLClientConfiguration.DebugPSP_id;
    doLayout(container);
  }

  /** Called by XMLClient to connect to Debug PSP.
   */

  public XMLDisplay() {
    this(XMLClientConfiguration.debuggerTitle,
         XMLClientConfiguration.PSP_package,
         XMLClientConfiguration.DebugPSP_id);
  }

  /** Called by clients to connect to specified PSP.
   */

  public XMLDisplay(String title, String PSP_package, String PSP_id) {
    this.title = title;
    this.PSP_package = PSP_package;
    this.PSP_id = PSP_id;
    getClusterHostFromUser(); 
    getClusterInfo();
    createFrame();
  }

  private void createFrame() {
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    };
    JFrame frame = new JFrame(title);
    frame.addWindowListener(windowListener);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // to debug graphics layout
    //    RepaintManager repaintManager = 
    //      RepaintManager.currentManager(frame.getRootPane());
    //    repaintManager.setDoubleBufferingEnabled(false); 
    doLayout(frame.getContentPane());
    frame.pack();
    // center the display on the screen
    frame.setLocation(screenSize.width/2 - frame.getWidth()/2,
                      screenSize.height/2 - frame.getHeight()/2);
    frame.setVisible(true);
  }

  /** Notes on layout:
    The SelectCluster, SelectPlanObject and Limit controls are placed
    in the topPanel with a grid bag layout that allows the combo boxes to
    expand horizontally.
    The expression boxes are placed in the expressionPanel with a grid bag
    layout that allows the name and value combo boxes to expand horizontally.
    The summary line and submit button are placed in the summaryPanel
    with a border layout that allows the text line to expand horizontally.
    The topPanel, expressionPanel, and summaryPanel are placed in north
    and south positions of successive border layouts so that they expand
    horizontally.
    Nothing expands vertically.
    Note that in grid bag layouts when fill is HORIZONTAL, anchor is not used;
    in these cases, we use the default anchor, CENTER.
    */

  private void doLayout(Container container) {
    JPanel topPanel = new JPanel(new GridBagLayout());
    internalPadding = new Insets(2, 10, 2, 10);

    if (!isApplet) {
      clusterNameBox = addComboBox(topPanel, SET_CLUSTER, clusterNames);
    }

    // add plan objects selection box
    String[] planObjects = XMLClientConfiguration.planObjectNames;
    if (PSP_id == XMLClientConfiguration.DebugPSP_id) { // debugger PSP
      fieldNames = XMLClientConfiguration.planObjectFieldNames;
    } else {
      fieldNames = XMLClientConfiguration.UIPlanObjectFieldNames;
    }
    planObjectClassBox = addComboBoxFromArray(topPanel, SET_PLAN_OBJECT, 
                                              planObjects);
    planObjectClassBox.setSelectedIndex(0);

    // in debugger, don't allow specification of fields to return
    // because this isn't supported within the debug psp
    if (!PSP_id.equals(XMLClientConfiguration.DebugPSP_id)) {
      String[] noFields = { "none" };
      requestedFieldsBox = addComboBoxFromArray(topPanel, SET_REQUESTED_FIELDS,
                                                noFields);
    }

    // add check box to limit number of objects retrieved
    gridx = 0;
    JCheckBox checkBox = new JCheckBox("Limit number of objects returned");
    checkBox.setSelected(false);
    limit = false;
    checkBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED)
          limit = true;
        else if (state == ItemEvent.DESELECTED)
          limit = false;
      }
    });
    addComponent(topPanel, checkBox, gridx++, gridy, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 0, 0, internalPadding, 1, 0);

    // add expression boxes
    JPanel expressionPanel = new JPanel(new GridBagLayout());
    gridx = 0;

    Vector comparators = new Vector(comparatorValues.length);
    for (int i = 0; i < comparatorValues.length; i++)
      comparators.addElement(comparatorValues[i]);

    Vector values = new Vector(1);
    values.addElement("");

    Vector combiners = new Vector(2);
    combiners.addElement("&");
    combiners.addElement("|");

    // add expression terms
    for (gridy = 0; gridy < nTerms; gridy++) {
      gridx = 0;
      userInputNameBoxes.addElement(addExpressionBoxFromArray(expressionPanel, fieldNames[0], true, true));
      addExpressionBox(expressionPanel, comparators, false, false);
      userInputValueBoxes.addElement(addExpressionBox(expressionPanel, values, true, true));
      if (gridy < nTerms)
        addExpressionBox(expressionPanel, combiners, false, false);
    }

    // display the expression that the user is composing
    JPanel summaryPanel = new JPanel(new BorderLayout());
    gridx = 0;
    gridy = 0;
    scrollingTextLine = new ScrollingTextLine(50);
    summaryPanel.add("North", scrollingTextLine);
    displayExpression();

    // button to submit new request
    gridx = 0;
    JButton button = new JButton(SUBMIT_REQUEST);
    button.setActionCommand(SUBMIT_REQUEST);
    button.addActionListener(this);
    // the default FlowLayout manager keeps the button at its preferred size
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(button);
    summaryPanel.add("South", buttonPanel);

    JScrollPane scroller = new JScrollPane();
    //    scroller.setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add("North", topPanel);
    panel.add("South", expressionPanel);
    JPanel totalPanel = new JPanel(new BorderLayout());
    totalPanel.add("North", panel);
    totalPanel.add("South", summaryPanel);
    JPanel finalPanel = new JPanel(new BorderLayout());
    finalPanel.add("North", totalPanel);
    // scroller.getViewport has a javax.swing.ViewportLayout layout manager
    scroller.getViewport().add(finalPanel);
    // container has a BorderLayout layout manager
    container.add("Center", scroller);
    
    initted = true;
  }

  private JComboBox addExpressionBoxHelper(JPanel panel, boolean editable, boolean expandable) {
    JComboBox box = new JComboBox();
    box.setEditable(editable);
    box.addActionListener(this);
    if (expandable)
      addComponent(panel, box, gridx++, gridy, 1, 1,
                   GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                   0, 0, internalPadding, .5, 0);
    else
      addComponent(panel, box, gridx++, gridy, 1, 1,
                   GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                   0, 0, internalPadding, 0, 0);
    userInputs.addElement(box);
    return box;
  }

  private JComboBox addExpressionBox(JPanel panel, Vector choices, 
                                     boolean editable, boolean expandable) {
    JComboBox box = addExpressionBoxHelper(panel, editable, expandable);
    for (int i = 0; i < choices.size(); i++)
      box.addItem(choices.elementAt(i));
    return box;
  }

  private JComboBox addExpressionBoxFromArray(JPanel panel, String[] choices, 
                                              boolean editable, boolean expandable) {
    JComboBox box = addExpressionBoxHelper(panel, editable, expandable);
    for (int i = 0; i < choices.length; i++)
      box.addItem(choices[i]);
    return box;
  }

  private JComboBox addComboBoxHelper(JPanel panel, String actionCommand) {
    gridx = 0;
    JLabel label = new JLabel(actionCommand);
    addComponent(panel, label, gridx++, gridy, 1, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 0, 0, internalPadding, 0, 0);
    JComboBox box = new JComboBox();
    box.setEditable(true); // allow user to add their own choices
    box.setActionCommand(actionCommand);
    box.addActionListener(this);
    addComponent(panel, box, gridx++, gridy++, 1, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 0, 0, internalPadding, 1, 0);
    return box;
  }

  private JComboBox addComboBox(JPanel panel, String actionCommand, 
                                Vector choices) {
    JComboBox box = addComboBoxHelper(panel, actionCommand);
    for (int i = 0; i < choices.size(); i++)
      box.addItem(choices.elementAt(i));
    return box;
  }

  private JComboBox addComboBoxFromArray(JPanel panel,
                                         String actionCommand,
                                         String[] choices) {
    JComboBox box = addComboBoxHelper(panel, actionCommand);
    for (int i = 0; i < choices.length; i++)
      box.addItem(choices[i]);
    return box;
  }

  private void addComponent(Container container, Component component,
                            int gridx, int gridy, 
                            int gridwidth, int gridheight,
                            int anchor, int fill,
                            int ipadx, int ipady, Insets insets,
                            double weightx, double weighty) {
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
    container.add(component, gbc);
  }

  /*
    Handle user actions.
   */

  public void actionPerformed(ActionEvent e) {
    if (!initted) // ignore all calls until display is initted
      return;
    String command = e.getActionCommand();
    Object source = e.getSource();

    // user selected a cluster, no action needed yet
    if (command.equals(SET_CLUSTER))
      return;

    // user hit "Submit" button to submit request to the cluster
    if (command.equals(SUBMIT_REQUEST)) {
      String clusterSelected = "";
      if (isApplet)
        clusterSelected = clusterURL;
      else
        clusterSelected = (String)clusterNameBox.getSelectedItem();
      String planObjectSelected = (String)planObjectClassBox.getSelectedItem();
      String s = "";
      if (limit)
        s = "LIMIT:" + scrollingTextLine.getText();
      else
        s = scrollingTextLine.getText();
      submit(clusterSelected, s, planObjectSelected);
      return;
    }

    // user set what plan object to retrieve
    if (command.equals(SET_PLAN_OBJECT)) {
      JComboBox box = (JComboBox)source;
      String[] names = fieldNames[box.getSelectedIndex()];
      String selection = (String)box.getSelectedItem();
      //      boolean editable = !selection.equals("all");
      boolean editable = true;
      for (int i = 0; i < userInputNameBoxes.size(); i++) {
        JComboBox nameBox = (JComboBox)userInputNameBoxes.elementAt(i);
        JComboBox valueBox = (JComboBox)userInputValueBoxes.elementAt(i);
        nameBox.setEnabled(editable);
        valueBox.setEnabled(editable);
        // ignore when items are removed
        nameBox.removeActionListener(this);
        nameBox.removeAllItems();
        valueBox.removeActionListener(this);
        valueBox.removeAllItems();
        valueBox.addItem("");
        valueBox.setSelectedIndex(0);
        for (int j = 0; j < names.length; j++)
          nameBox.addItem(names[j]);
        nameBox.setSelectedIndex(0);
        nameBox.addActionListener(this);
        valueBox.addActionListener(this);
      }
      displayExpression();
      return;
    }

    if (command.equals(SET_REQUESTED_FIELDS)) {
      String tmp = (String)requestedFieldsBox.getSelectedItem();
      if (tmp.length() != 0) {
        if (tmp.equalsIgnoreCase("none"))
          requestedFields = "";
        else if (requestedFields.length() == 0)
          requestedFields = tmp;
        else
          requestedFields = requestedFields + "," + tmp;
        if (requestedFieldsBox.getSelectedIndex() == -1)
          requestedFieldsBox.addItem(tmp);
      }
      displayExpression();
      return;
    }
      

    // user changed something in the search expression
    if (source instanceof JComboBox) {
      JComboBox box = (JComboBox)source;
      // if user added new value, then add it to list of choices
      if (box.getSelectedIndex() == -1)
        box.addItem(box.getSelectedItem());
      // update the expression displayed
      displayExpression();
    }

  }

  /*
    Compose the expression as the user creates it.
  */

  private String composeExpression() {
    JComboBox combinerBox = null;
    int i = 0;
    String planObjectSelected = (String)planObjectClassBox.getSelectedItem();
    String s = ""; // expression user is composing
    // prepended to property specification, either "object class." or empty
    String prefix = ""; 
    if (!planObjectSelected.equals("all")) {
      prefix = planObjectSelected + ".";
      s = planObjectSelected;
    }

    while (i < userInputs.size()) {
      JComboBox nameBox = (JComboBox)userInputs.elementAt(i++);
      JComboBox comparatorBox = (JComboBox)userInputs.elementAt(i++);
      JComboBox valueBox = (JComboBox)userInputs.elementAt(i++);
      if ((nameBox.getSelectedItem() == null) ||
          (valueBox.getSelectedItem() == null))
        break;
      String name = (String)nameBox.getSelectedItem();
      name = name.trim();
      if (name.length() == 0)
        break;
      String value = (String)valueBox.getSelectedItem();
      value = value.trim();
      if (value.length() == 0)
        break;
      if (combinerBox == null)
        s = prefix + name +
          (String)comparatorBox.getSelectedItem() +
          value;
      else
        s = s + " " + (String)combinerBox.getSelectedItem() + " " +
          prefix + name +
          (String)comparatorBox.getSelectedItem() +
          value;
      combinerBox = (JComboBox)userInputs.elementAt(i++);
    }

    if (requestedFields.length() != 0)
      s = s + ":" + requestedFields;

    return s;
  }

  private void displayExpression() {
    scrollingTextLine.setText(composeExpression());
    //    summary.setText(composeExpression());
  }

  /*
    Before sending the first request, ask the user for the location
    of a cluster.  Get the list of URLs and clusters from there.
  */
  
  private void getClusterHostFromUser() {
    String s = (String)JOptionPane.showInputDialog(null,
      "Enter location of a cluster Log Plan Server in the form host:port",
                   "Cluster Location",
                    JOptionPane.INFORMATION_MESSAGE,
                    null, null, "localhost:5555");
    if (s == null)
      System.exit(0); // if we don't know where the clusters are located, quit
    s = s.trim();
    if (s.length() == 0) {
      clusterHost = "localhost";
      clusterPort = "5555";
    } else {
      int i = s.indexOf(":");
      if (i == -1) {
        clusterHost = s;
        clusterPort = "5555";
      } else {
        clusterHost = s.substring(0, i);
        clusterPort = s.substring(i+1);
      }
    }
  }

  private void getClusterInfo() {
    try {
      ConnectionHelper connection = 
        new ConnectionHelper("http://" + clusterHost + ":" + clusterPort + "/");
      clusterURLs = connection.getClusterIdsAndURLs();
      if (clusterURLs == null) {
        displayErrorString("Unable to obtain cluster identifier to URL mappings.");
        //      System.exit(0);
      }
    } catch (Exception e) {
      displayErrorString(e.toString());
      clusterURLs = null;
      //      System.exit(0);
    }
    clusterNames = new Vector();
    if (clusterURLs != null) {
      Enumeration names = clusterURLs.keys();
      while (names.hasMoreElements())
        clusterNames.addElement(names.nextElement());
      Collections.sort(clusterNames);
      if (clusterNames.size() == 0) {
        displayErrorString("Unable to obtain cluster identifier to URL mappings.");
        //System.exit(0);
      }
    }
  }

  private void updateClusterNameBox() {
    clusterNameBox.removeAllItems();
    for (int i = 0; i < clusterNames.size(); i++)
      clusterNameBox.addItem(clusterNames.elementAt(i));
  }

  /*  Connect to the URL which is a concatenation
      of the COUGAAR clusters host name (localhost), the port (5555), and
      an "id" which allows the XML Plan Server
      to match up to our plan service provider (PSP)
      then, send the parameters which were collected from the user,
      and are passed through the XML Plan Server to be interpreted by the PSP.
      Finally, read the incoming XML to a buffer, so it can
      be parsed and displayed and optionally saved.
      The logPlanObjectClassName and requestToDisplay are solely
      for displaying in the results window.
  */

  private void submit(String clusterName, 
                      String requestToSend, 
                      String logPlanObjectClassName) {

    // if not an applet, look up the cluster URL for each submission
    if (!isApplet) {
      clusterURL = null;
      // if have cluster name/URL mappings, then look for this cluster
      if (clusterURLs != null)
        clusterURL = (String)clusterURLs.get(clusterName);
      // if cluster not found
      if (clusterURL == null) {
        // query for latest cluster name/URL mappings
        getClusterInfo();
        updateClusterNameBox();
        // if can't get cluster name/URL mappings, don't submit request
        if (clusterURLs == null)
          return;
        // get cluster name/URL mapping
        clusterURL = (String)clusterURLs.get(clusterName);
        // if this cluster is unknown, display error message
        if (clusterURL == null) {
          displayErrorString("Cannot locate cluster: " + clusterName);
          return;
        }
      }
    }

    System.out.println("Submitting: " + requestToSend + " to: " + clusterURL);
    byte[] reply = null;
    try {
      ConnectionHelper connection = 
        new ConnectionHelper(clusterURL, PSP_package, PSP_id);
      connection.sendData(requestToSend);
      reply = connection.getResponse();
    } catch (Exception e) {
      displayErrorString(e.toString());
      return;
    }

    // check if it's a XML document or an error message
    String xmlPrefix = "<?xml";
    int len = xmlPrefix.length();
    if (reply.length < len)
      displayErrorString(new String(reply));
    else {
      String replyString = new String(reply, 0, len);
      if (replyString.equals(xmlPrefix)) {
        String title = clusterName + ":" + 
                        logPlanObjectClassName + ":" +
                        requestToSend;
        final XMLTreeView treeView = new XMLTreeView(title, reply);
        WindowListener l = new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            treeView.dispose();
          }
          public void windowClosed(WindowEvent e) {
          }
        };
        treeView.addWindowListener(l);
        treeView.show();
        // temporary, until we implement table view for all objects
        // also, only works with real log plan objects, not UI Data objects
        if ((PSP_id == XMLClientConfiguration.DebugPSP_id) &&
            (logPlanObjectClassName.equals("Task") ||
             logPlanObjectClassName.equals("Allocation"))) {
          XMLTableView tableView = 
            new XMLTableView(title, logPlanObjectClassName, reply);
          tableView.setLocation(0,
                                (int)(treeView.getLocation().getY() +
                                      treeView.getSize().getHeight()));
          tableView.show();
        }
      } else
        displayErrorString(new String(reply));
    }
  }

  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

}
