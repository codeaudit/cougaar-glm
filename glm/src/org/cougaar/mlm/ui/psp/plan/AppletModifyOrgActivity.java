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
 
package org.cougaar.mlm.ui.psp.plan;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.cougaar.util.ShortDateFormat;
import org.cougaar.core.util.XMLObjectFactory;

import org.w3c.dom.Element;

import org.cougaar.mlm.ui.psp.society.SocietyUI;

/**
 * Modify Organization Activities GUI (via PSP_ModifyOrgActivity).
 * <p>
 * Allows user to look up the self org's org activity for a given
 * activity type on multiple clusters.  Several fields in the org
 * activity can then be changed:<br>
 * <ul>
 *   <li>OpTempo</li>
 *   <li>start date</li>
 *   <li>stop date</li>
 * </ul>
 * <p>
 * A group of clusters is selected from a user-modifiable XML
 * file called <code>AppletMOA_Groups.xml</code>.  Here's an example:
 * <pre>
 * &lt;object type="java.util.Vector"&gt;
 *   &lt;element type="ui.querychannel.HashElem"&gt;
 *     &lt;Key&gt;MiniTestConfig Clusters&lt;Key&gt;
 *     &lt;Values&gt;
 *       &lt;element type="java.lang.String"&gt;3ID&lt;element&gt;
 *       &lt;element type="java.lang.String"&gt;1BDE&lt;element&gt;
 *       &lt;element type="java.lang.String"&gt;3-69-ARBN&lt;element&gt;
 *       &lt;element type="java.lang.String"&gt;MCCGlobalMode&lt;element&gt;
 *     &lt;Values&gt;
 *   &lt;element&gt;
 *   &lt;element type="ui.querychannel.HashElem"&gt;
 *     &lt;Key&gt;Just 3ID&lt;Key&gt;
 *     &lt;Values&gt;
 *       &lt;element type="java.lang.String"&gt;3ID&lt;element&gt;
 *     &lt;Values&gt;
 *   &lt;element&gt;
 * &lt;object&gt;
 * </pre>
 */

public class AppletModifyOrgActivity extends java.applet.Applet {

  public void init() {
    String xmlFilename = "AppletMOA_Groups.xml";
    loadClusterIDsToGroupMap(xmlFilename);
    createGUI();
  }
        
  /** url/group/clusters info **/
  private JTextField urlText;
  private JComboBox groupSelector;
  private JList shownClusters;
  private DefaultListModel shownModel;

  /** org activity info **/
  private JLabel orgIdLabel;
  private JTextField typeText;
  private JTextField opTempoText;
  private JLabel cDayLabel;
  private JTextField startText;
  private JTextField endText;

  /** status label **/
  private JTextField statusLabel;

  /** Control buttons */
  private JButton getOrgActButton;
  private JButton modifyOrgActButton;

  /** Date formatter for "month/day/year" **/
  private ShortDateFormat dateFormatter = new ShortDateFormat();

  /**
   * This loads a HashMap from XML.
   * <p>
   * Okay existing conversion:
   * <ol>
   *   <li>make public class implements SelfPrinter {String Key,Vector Val}</li>
   *   <li>write XML for vector of these, build HashMap from that</li>
   * </ol>
   * Potentially better conversion:
   * <ol>
   *   <li>make SelfPrinter subclass of HashMap</li>
   * </ol>
   * Best idea
   * <ol>
   *   <li> make AsciiPrinter directly support java.util.Map</li>
   * </ol>
   * Just some thoughts....
   */
  protected HashMap loadHashMapFromXML(String xmlFilename) {
    Object obj = null;
    try {
      Element root = XMLObjectFactory.readXMLRoot(xmlFilename);
      if (root == null) {
        return null;
      } else {
        obj = XMLObjectFactory.parseObject(root);
        if (!(obj instanceof Vector))
          return null;
      }
    } catch (RuntimeException eBadXML) {
      return null;
    }

    Vector v = (Vector)obj;
    HashMap hMap = new HashMap(v.size());
    Enumeration en = v.elements();
    while (en.hasMoreElements()) {
      HashElem helem = (HashElem)en.nextElement();
      hMap.put(helem.getKey(), helem.getValues());
    }
    return hMap;
  }

  /**
   * A HashMap of "Group" to a Vector of "ClusterID"s
   */
  protected HashMap groupToClusters = null;

  protected void loadClusterIDsToGroupMap(String xmlFilename) {
    groupToClusters = loadHashMapFromXML(xmlFilename);
    if (groupToClusters == null) {
      System.err.println("Unable to load XML data file: "+xmlFilename);
      groupToClusters = new HashMap();
    }
  }

  protected Vector getClusterIDsFromGroup(String group) {
    return (Vector)groupToClusters.get(group);
  }

  protected void clearUIModifyOrgActivityState() {
    orgIdLabel.setText("");
    //typeText.setText("");
    opTempoText.setText("");
    cDayLabel.setText("");
    startText.setText("");
    endText.setText("");
    setStatus(false, "");
  }

  protected void drawUIModifyOrgActivityState(
      UIModifyOrgActivityState moa) {
    orgIdLabel.setText(moa.getOrgIdLabel());
    //typeText.setText(moa.getTypeText());
    opTempoText.setText(moa.getOpTempoText());
    cDayLabel.setText(moa.getCDayLabel());
    startText.setText(moa.getStartText());
    endText.setText(moa.getEndText());
    setStatus(moa.getStatusError(), moa.getStatusLabel());
  }

  protected void grabUIModifyOrgActivityState(
      UIModifyOrgActivityState moa) {
    moa.setOrgIdLabel(orgIdLabel.getText());
    moa.setTypeText(typeText.getText());
    moa.setOpTempoText(opTempoText.getText());
    moa.setCDayLabel(cDayLabel.getText());
    moa.setStartText(startText.getText());
    moa.setEndText(endText.getText());
    moa.setStatusError(
      (statusLabel.getForeground() == Color.red));
    moa.setStatusLabel(statusLabel.getText());
  }
     
  protected void setStatus(String s) {
    setStatus(false, s);
  }

  protected void setStatus(boolean success, String s) {
    statusLabel.setForeground(
      (success ? Color.darkGray : Color.red));
    statusLabel.setText(s);
  }

  Vector shownMOAs;

  protected void drawMOA(String id) {
    if (id == null) {
      clearUIModifyOrgActivityState();
      setStatus("OrgID not set?");
    } else if (shownMOAs == null) {
      clearUIModifyOrgActivityState();
      setStatus("No OrgActivities found");
    } else {
      for (int i=0; i < shownMOAs.size(); i++) {
        UIModifyOrgActivityState moa = 
          (UIModifyOrgActivityState)shownMOAs.elementAt(i);
        if (id.equals(moa.getOrgIdLabel())) {
          shownClusters.setSelectedIndex(i);
          shownClusters.ensureIndexIsVisible(i);
          drawUIModifyOrgActivityState(moa);
          return;
        }
      }
      shownClusters.setSelectedIndex(-1);
      clearUIModifyOrgActivityState();
      setStatus("Unable to read OrgAct for "+id);
    }
  }

  protected void setMOAs(Vector moas) {
    shownMOAs = moas;
    if (moas != null) {
      if (moas.size() <= 0) {
        clearUIModifyOrgActivityState();
        setStatus("No ModifyOrgActs found");
      } else {
        drawMOA(((UIModifyOrgActivityState)shownMOAs.elementAt(0)).getOrgIdLabel());
      }
    }
  }

  /**
   * Show the selected cluster
   */
  MouseListener MyClustersListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      Object obj = shownClusters.getSelectedValue();
      if (obj instanceof String)
        drawMOA((String)obj);
    }
  };

  /**
   * Fill the cluster's list with the selected group
   * @return read-only vector of selected clusters
   */
  protected Vector showSelectedGroup() {
    // get selected group name
    Object selectedGroup = groupSelector.getSelectedItem();
    if (!(selectedGroup instanceof String)) {
      setStatus("Not a group");
      return null;
    }
    // get clusters for the group
    Vector clusters = getClusterIDsFromGroup((String)selectedGroup);
    shownModel.removeAllElements();
    if ((clusters == null) || (clusters.size() <= 0)) {
      setStatus("No clusters in group");
      return null;
    }
    Enumeration en = clusters.elements();
    while (en.hasMoreElements()) {
      shownModel.addElement((String)en.nextElement());
    }
    return clusters;
  }

  /**
   * Refresh the list and group
   */
  class MyGroupListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      showSelectedGroup();
    }
  }

  /**
   * An ActionListener that listens to the buttons.
   */
  class DRTListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JButton button = (JButton)e.getSource();
      // get url string
      String url_base = urlText.getText();
      if ((url_base == null) ||
          ((url_base = url_base.trim()).length() <= 0)) {
        setStatus("Must specify URL location");
        return;
      }
      if (!(url_base.toLowerCase().startsWith("http://"))) {
        url_base = "http://"+url_base;
        urlText.setText(url_base);
      }
      // type must be set
      String type = typeText.getText();
      if ((type == null) ||
          ((type = type.trim()).length() <= 0)) {
        setStatus("Must specify Activity Type");
        return;
      }
      Vector clusters = showSelectedGroup();
      if (clusters == null)
        return;
      // take the mod org act info
      UIModifyOrgActivityState moa = new UIModifyOrgActivityState();
      String sPressButton;
      if (button == getOrgActButton)
        sPressButton = PSP_ModifyOrgActivity.GET_ORG_ACT_BUTTON;
      else
        sPressButton = PSP_ModifyOrgActivity.MODIFY_ORG_ACT_BUTTON;
      moa.setPressedButton(sPressButton);
      grabUIModifyOrgActivityState(moa);
      // fetch the new info
      setMOAs(
        SocietyUI.sendUIModifyOrgActivityState(
          url_base, clusters, moa));
    }
  }

  private void createGUI() {
    // Create outer panel
    urlText = new JTextField(20);
    Object[] groupsArray = groupToClusters.keySet().toArray();
    groupSelector = new JComboBox(groupsArray);
    groupSelector.addActionListener(new MyGroupListener());
    shownModel = new DefaultListModel();
    shownModel.setSize(5);
    shownClusters = new JList(shownModel);
    if (groupToClusters.size() > 0) {
      shownClusters.setSelectedIndex(0);
      showSelectedGroup();
    }
    shownClusters.addMouseListener(MyClustersListener);

    // Create buttons, labels, etc for ModOrgAct Panel
    orgIdLabel = new JLabel();
    orgIdLabel.setForeground(Color.black);
    typeText = new JTextField(11);
    opTempoText = new JTextField(11);
    cDayLabel = new JLabel();
    cDayLabel.setForeground(Color.black);
    startText = new JTextField(11);
    endText = new JTextField(11);
    getOrgActButton = new JButton("Get Org Activities");
    getOrgActButton.addActionListener(new DRTListener());
    modifyOrgActButton = new JButton("Modify Org Activities");
    modifyOrgActButton.addActionListener(new DRTListener());
    statusLabel = new JTextField(20);
    statusLabel.setEditable(false);
    setStatus(true, "");

    // do layout
    JFrame frame = new JFrame("ModifyOrgActivityGUIPlugIn");
    frame.setLocation(0,0);
    JPanel rootPanel = new JPanel((LayoutManager) null);
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5,5,5,5);
    gbc.fill = GridBagConstraints.BOTH;
    rootPanel.setLayout(gbl);

    // url
    JPanel urlPanel = new JPanel();
    urlPanel.setLayout(new GridLayout(2,1));
    JLabel urlLabel = new JLabel("URL:");
    urlLabel.setForeground(Color.blue);
    urlPanel.add(urlLabel);
    urlPanel.add(urlText);
    gbc.gridy = 1;
    gbl.setConstraints(urlPanel, gbc);
    rootPanel.add(urlPanel);

    // type
    JPanel typePanel = new JPanel();
    typePanel.setLayout(new GridLayout(2,1));
    JLabel typeLabel = new JLabel("Activity Type:");
    typeLabel.setForeground(Color.blue);
    typePanel.add(typeLabel);
    typePanel.add(typeText);
    gbc.gridy = 2;
    gbl.setConstraints(typePanel, gbc);
    rootPanel.add(typePanel);

    // group
    JPanel groupPanel = new JPanel();
    groupPanel.setLayout(new BorderLayout());
    JLabel groupLabel = new JLabel("Group:");
    groupLabel.setForeground(Color.blue);
    groupPanel.add(groupLabel, BorderLayout.NORTH);
    groupPanel.add(groupSelector, BorderLayout.CENTER);
    gbc.gridy = 3;
    gbl.setConstraints(groupPanel, gbc);
    rootPanel.add(groupPanel);

    // clusters
    JPanel clustersPanel = new JPanel();
    clustersPanel.setLayout(new BorderLayout());
    JLabel clustersLabel = new JLabel("Clusters:");
    clustersLabel.setForeground(Color.blue);
    clustersPanel.add(clustersLabel, BorderLayout.NORTH);
    JScrollPane scroller = new JScrollPane(shownClusters);
    clustersPanel.add(scroller, BorderLayout.CENTER);
    gbc.gridy = 4;
    gbl.setConstraints(clustersPanel, gbc);
    rootPanel.add(clustersPanel);

    // get
    gbc.gridy = 5;
    gbl.setConstraints(getOrgActButton, gbc);
    rootPanel.add(getOrgActButton);

    // set
    gbc.gridy = 6;
    gbl.setConstraints(modifyOrgActButton, gbc);
    rootPanel.add(modifyOrgActButton);

    // org activity info
    JPanel orgActPanel = new JPanel();
    orgActPanel.setLayout(new BorderLayout());
    JLabel orgActLabel = new JLabel("Org Activity:");
    orgActLabel.setForeground(Color.blue);
    orgActPanel.add(orgActLabel, BorderLayout.NORTH);
    JPanel orgActInfoPanel = new JPanel();
    orgActInfoPanel.setLayout(new BorderLayout());
    JPanel orgActInfoTextPanel = new JPanel();
    orgActInfoTextPanel.setLayout(new GridLayout(5,1));
    orgActInfoTextPanel.add(new JLabel("OrgId:"));
    orgActInfoTextPanel.add(new JLabel("OpTempo:"));
    orgActInfoTextPanel.add(new JLabel("cDay:"));
    orgActInfoTextPanel.add(new JLabel("Start Date:"));
    orgActInfoTextPanel.add(new JLabel("End Date:"));
    JPanel orgActInfoValuePanel = new JPanel();
    orgActInfoValuePanel.setLayout(new GridLayout(5,1));
    orgActInfoValuePanel.add(orgIdLabel);
    orgActInfoValuePanel.add(opTempoText);
    orgActInfoValuePanel.add(cDayLabel);
    orgActInfoValuePanel.add(startText);
    orgActInfoValuePanel.add(endText);
    orgActInfoPanel.add(orgActInfoTextPanel, BorderLayout.CENTER);
    orgActInfoPanel.add(orgActInfoValuePanel, BorderLayout.EAST);
    orgActPanel.add(orgActInfoPanel, BorderLayout.CENTER);
    gbc.gridy = 7;
    gbl.setConstraints(orgActPanel, gbc);
    rootPanel.add(orgActPanel);

    // status
    JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new GridLayout(2,1));
    JLabel statusLabelLabel = new JLabel("Status:");
    statusLabelLabel.setForeground(Color.blue);
    statusPanel.add(statusLabelLabel);
    statusPanel.add(statusLabel);
    gbc.gridy = 8;
    gbl.setConstraints(statusPanel, gbc);
    rootPanel.add(statusPanel);

    frame.setContentPane(rootPanel);
    frame.pack();
    frame.setVisible(true);

    setStatus(true, "Ready");
  }

  public static void main(String args[]) {
    (new AppletModifyOrgActivity()).init();
  }

}
