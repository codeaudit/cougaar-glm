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
 
package org.cougaar.mlm.ui.planviewer.inventory;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.mlm.ui.planviewer.XMLClientConfiguration;
import org.cougaar.glm.execution.eg.ClusterInfo;

public class InventorySelector implements ActionListener {

    //Should match what's in the PSP_ALPInventory.java
    final public static String ASSET = "ASSET";
    final public static String ASSET_AND_CLASSTYPE =ASSET + ":" + "CLASS_TYPE:";
    final public static String GET_ALL_CLASS_TYPES = "All";
    final public static String[] ASSET_CLASS_TYPES = {GET_ALL_CLASS_TYPES,"Ammunition","BulkPOL","ClassISubsistence","ClassVIIIMedical","PackagedPOL","Consumable"};

  JPanel queryPanel;

  Vector clusterNames;

  JComboBox clusterNameBox;
  JComboBox assetNameBox;
  JComboBox classTypeBox;
  Hashtable clusterURLs;

  String clusterName; // set from code base if called as applet
  String classType;

  String SET_ASSET = "Set Asset";
  String SET_CLUSTER = "Set Cluster";
  String SUBMIT = "Submit";
  // insets are top, left, bottom, right
  Insets labelInternalPadding = new Insets(5, 10, 5, 5);
  Insets internalPadding = new Insets(5, 0, 5, 0);
  int gridy = 0;
  boolean assetNameBoxInitted = false;
  Vector assetNames;
  String hostAndPort; // defaults to http://localhost:5555/
  static final String PSP_id = "inventory"; //"GLMINVENTORY.PSP";
  boolean isApplet;
  Container container;
  boolean doDisplayTable = false;
  Color bgColor = Color.white;
    

  /** Called by an application.
   * Queries the user for the cluster to contact.
   */
  public InventorySelector(String hostname, String LPSPort) {
    isApplet = false;
    hostAndPort = "http://" + hostname + ":" + LPSPort + "/";
    JFrame frame = createFrame();
    container = frame.getContentPane();
    // fills frame
    doLayout(true);
    frame.pack();
//     frame.setSize((int)frame.getSize().getWidth() * 2,
//                   (int)frame.getSize().getHeight());
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // set location to middle of the screen
    frame.setLocation(screenSize.width/2 - (int)frame.getSize().getWidth()/2,
                      screenSize.height/2 - (int)frame.getSize().getHeight()/2);
    frame.setVisible(true);
  }

  /** Called by an application to display the data from a file.
   *    The file contains a UISimpleInventory object.
   */
  public InventorySelector(String filename) {
    UISimpleInventory inventory = null;
    try {
      FileInputStream f = new FileInputStream(filename);
      if (f == null) {
        displayErrorString("Could not read file: " + filename);
        return;
      }
      ObjectInputStream p = new ObjectInputStream(f);
      inventory = (UISimpleInventory)p.readObject();
    } catch (Exception e) {
      System.out.println("Object read exception: " + e);
      displayErrorString("Could not read file: " + filename);
      return;
    }
    new QueryHelper(new InventoryQuery(inventory));
  }


  private void doLayout(boolean queryForClusters) {
      // this is the base panel with list of clusters 
      // and inventory assets to choose to display
    queryPanel = new JPanel();
    queryPanel.setLayout(new GridBagLayout());
    queryPanel.setBackground(bgColor);
    // Borders
    Border b = BorderFactory.createCompoundBorder(
                      BorderFactory.createRaisedBevelBorder(),
                      BorderFactory.createLoweredBevelBorder());
    Border margin = new EmptyBorder(10,10,10,10);
    queryPanel.setBorder(new CompoundBorder(margin, b));

    // create cluster list
    if (queryForClusters)
	addClusterList();

    // create cluster list
    if (queryForClusters)
	addClassTypeList();


    // create inventory list
    assetNameBox = new JComboBox();
    add("Display schedules for asset:", assetNameBox, SET_ASSET);

    // checkbox for table
    JCheckBox checkBox = new JCheckBox("Display table");
    checkBox.setBackground(bgColor);
    checkBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int event = e.getStateChange();
        if (event == ItemEvent.DESELECTED)
          doDisplayTable = false;
        else if (event == ItemEvent.SELECTED)
          doDisplayTable = true;
      }
    });

    addComponent(queryPanel, checkBox, 1, gridy++, 1, 1,
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, labelInternalPadding, 0, 0);

    // submit button
    layoutSubmitButton();
    container.setLayout(new BorderLayout());
    container.setBackground(bgColor);
    container.add("Center", queryPanel);

    updateInventoryBox();

  }

    /** Called by applet. If the applet is invoked with a URL
     * that includes the cluster name, then just use that cluster;
     * otherwise get the list of clusters and allow the user to select.
     */
  public InventorySelector(URL codeBase, Container container) {
    isApplet = true;
    this.container = container;
    hostAndPort = "http://" + codeBase.getHost() + ":" + 
      String.valueOf(codeBase.getPort()) + "/";

    // extract cluster name from code base
    String s = codeBase.toString();
    int startIndex = s.indexOf('$');
    if (startIndex == -1) {
      doLayout(true); // layout query panel with cluster list box
    } else {
      int endIndex = s.indexOf('/', startIndex);
      if (endIndex == -1) {
        displayErrorString("Enter URL of the form: http://hostname:5555/$clustername/alpine/demo/Inventory.html");
        return;
      }
      clusterName = s.substring(startIndex+1, endIndex);
      // set up clusterURLs hashtable for compatability with application
      clusterURLs = new Hashtable();
      clusterURLs.put(clusterName, hostAndPort + "$" + clusterName + "/");
      // fetch asset list for this cluster and display it
      assetNameBoxInitted = true;
      classType = GET_ALL_CLASS_TYPES;
      assetNames = getAssets(clusterName,classType);
      if (assetNames != null) {
        for (int i = 0; i < assetNames.size(); i++)
          assetNameBox.addItem(assetNames.elementAt(i));
      } else {
        displayErrorString("No inventory assets in this cluster");
        return;
      }
      doLayout(false); // layout query panel without cluster list box
    }
  }

  public void add(String label, JComboBox component, String actionCommand) {
    int gridx = 0;
    JLabel lbl = new JLabel(label);
    addComponent(queryPanel, lbl, gridx++, gridy, 1, 1,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 0, 0, labelInternalPadding, 0, 0);
    addComponent(queryPanel, component, gridx++, gridy++, 1, 1,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 0, 0, internalPadding, 0, 0);
    component.addActionListener(this);
    component.setActionCommand(actionCommand);
  }

  private JFrame createFrame() {
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    };
    JFrame frame = new JFrame("Inventory/Capacity");
    frame.addWindowListener(windowListener);
    return frame;
  }

  private void layoutSubmitButton() {
    JButton submitButton = new JButton("Submit");
    submitButton.addActionListener(this);
    submitButton.setActionCommand(SUBMIT);
    addComponent(queryPanel, submitButton, 1, gridy, 1, 1,
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, labelInternalPadding, 0, 0);
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

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (clusterNameBox != null)
      clusterName = (String)clusterNameBox.getSelectedItem();
    if (classTypeBox != null)
      classType = (String)classTypeBox.getSelectedItem();

    String assetName = (String)assetNameBox.getSelectedItem();

    // fetch assets for new cluster
    if ((command.equals(SET_CLUSTER)) || !assetNameBoxInitted) {
	SwingUtilities.invokeLater(
	    new Thread(clusterName + "updateInventoryBox") {
                public synchronized void run() {
                    updateInventoryBox();
		}});
        return;
    }

    if (command.equals(SUBMIT)) {
      if (assetName == null)
        return;

      // capture the clock time here in variable
      // setup logfile
      long submit_time = java.lang.System.currentTimeMillis();
      
      FileWriter logFile = getDefaultLogFile();
      
      new QueryHelper(new InventoryQuery(assetName),
                      hostAndPort + "$" + clusterName + "/",
                      isApplet, container, doDisplayTable,
		      submit_time, 
		      logFile);
    }
  }
  
  public static FileWriter getDefaultLogFile() {
    String logFileName = System.getProperty("org.cougaar.log.loginventorytimes");
    FileWriter logFile = null;
    
    if((logFileName != null) &&
       (!(logFileName.trim().equals("")))) {
      try {
	logFile = new FileWriter(logFileName,true);
	logFile.write("   Timestamp,    Display time (milliseconds)\n");
      }
      catch(IOException except) {
	System.err.println("Couldn't open file " + logFileName + " to log display times");
	System.err.println("Error was: " + except);
	logFile = null;
      }
    }
    
    return logFile;
  }
  
    public void updateInventoryBox() {
	if (assetNameBox.getItemCount() != 0)
	    assetNameBox.removeAllItems();
	assetNameBoxInitted = true;
	assetNames = getAssets(clusterName,classType);
	if (assetNames != null)
	    for (int i = 0; i < assetNames.size(); i++)
		assetNameBox.addItem(assetNames.elementAt(i));
					       
	return;
    }

  /* Send request to the cluster to get the list of assets
     with scheduled content property groups.
     */

  private Vector getAssets(String clusterName, String aClassType) {
    String clusterURL = (String)clusterURLs.get(clusterName);
    //When querying for all just ASSET
    String queryStr=ASSET;

    if(!(aClassType.equals(GET_ALL_CLASS_TYPES))) {
	queryStr = (ASSET_AND_CLASSTYPE + aClassType);
    }

    //System.out.println("Submitting: " + queryStr + " to: " + clusterURL +
    //                   " for: " + PSP_id);
    InputStream is = null;
    try {
      ConnectionHelper connection = 
        new ConnectionHelper(clusterURL, "inventory");
                             //XMLClientConfiguration.PSP_package, PSP_id);

      connection.sendData(queryStr);

      is = connection.getInputStream();
    } catch (Exception e) {
      displayErrorString(e.toString());
      return null;
    }
    Vector assetNames = null;
    try {
      ObjectInputStream p = new ObjectInputStream(is);
      assetNames = (Vector)p.readObject();
    } catch (Exception e) {
      displayErrorString("Object read exception: " + "_2_" + e.toString());
    }
    Collections.sort(assetNames);
    return assetNames;
  }

  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

  private Vector getClusterNames() {
      System.out.println("Querying for cluster list");
      try {
	  ConnectionHelper connection = new ConnectionHelper(hostAndPort);
	  clusterURLs = connection.getClusterIdsAndURLs();
	  if (clusterURLs == null) {
	      System.out.println("No clusters");
	      System.exit(0);
	  }
      } catch (Exception e) {
	  System.out.println(e);
	  System.exit(0);
      }
    Enumeration names = clusterURLs.keys();
    clusterNameBox = new JComboBox();
    Vector vNames = new Vector();
    while (names.hasMoreElements())
	vNames.addElement(names.nextElement());
    Collections.sort(vNames);
    
    return vNames;

  }

  private void addClassTypeList() {
      classTypeBox = new JComboBox();
      for(int i=0; i < ASSET_CLASS_TYPES.length; i++) {
	  classTypeBox.addItem(ASSET_CLASS_TYPES[i]);
      }
      classTypeBox.setSelectedItem(GET_ALL_CLASS_TYPES);
      classType = GET_ALL_CLASS_TYPES;
      add("Display Assets of type:", classTypeBox, SET_CLUSTER);
  }


private void addClusterList() {

    clusterNames = getClusterNames();

    for (int i = 0; i < clusterNames.size(); i++)
        clusterNameBox.addItem(clusterNames.elementAt(i));
    if (!clusterNames.isEmpty()) 
	clusterName = (String)clusterNames.elementAt(0);
    add("Display schedules from:", clusterNameBox, SET_CLUSTER);
    // if applet, modify all cluster URLs to use the code base host and port
    // note that this doesn't sort
    loadClusterURLs();
}

    private void loadClusterURLs() {
    // if applet, modify all cluster URLs to use the code base host and port
    // note that this doesn't sort
    if (isApplet) {
      Hashtable tmpHT = new Hashtable();
      Enumeration names = clusterURLs.keys();
      while (names.hasMoreElements()) {
        String name = (String)names.nextElement();
        String clusterURL = (String)clusterURLs.get(name);
        int startIndex = clusterURL.indexOf('$');
        if (startIndex == -1)
          continue;
        int endIndex = clusterURL.indexOf('/', startIndex);
        if (endIndex == -1)
          continue;
        clusterURL = hostAndPort + clusterURL.substring(startIndex, endIndex+1);
        tmpHT.put(name, clusterURL);
      }
      clusterURLs = tmpHT; 
    }
    }


}
