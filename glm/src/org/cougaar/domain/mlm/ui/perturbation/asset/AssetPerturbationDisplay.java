/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.perturbation.asset;

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.cougaar.util.ThemeFactory;
import org.cougaar.util.OptionPane;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.ScrollingTextLine;

/**
 * Displays AssetPerturbations from specified cluster.
 * Allows user to specify a screen date - only get InfoMessages since xxx
 */

public class AssetPerturbationDisplay extends JPanel {
  
  static final private String SET_CLUSTER_LABEL = "Cluster:";
  static final private String SET_CLUSTER_ACTION = "setCluster";

  static final private String START_DATE_LABEL = "From: ";
  static final private String END_DATE_LABEL = "To: ";

  static final private String SUBMIT_REQUEST_LABEL ="Get Assets";
  static final private String SUBMIT_REQUEST_ACTION ="getAssets";

  static final private String MODIFY_AVAILABILITY_LABEL ="Make Unavailable";
  static final private String MODIFY_AVAILABILITY_ACTION ="modifyAvailability";

  static final private SimpleDateFormat myDateFormatter = 
    new SimpleDateFormat("MM/dd/yyyy HH:mm");

  static final private DateFormat myStandardFormatter = 
    DateFormat.getDateInstance();

  static final private String UNDEFINED = "Undefined";

  static final private String DEFAULT_HOST = "localhost";
  static final private int DEFAULT_PORT = 5555;

  Map myDestroyed = new HashMap();

  private ScrollingTextLine myScrollingTextLine;
  private Insets myNoInternalPadding;
  private Insets myInternalPadding;
  private Insets myLabelInternalPadding;
  

  // JComboBox for cluster names
    //  private JComboBox myClusterNameBox;
  private JComboBox myClusterNameBox;

  // JTextField for screen date
  // Should be converted to real date entry 
  private JTextField myStartDateEntry;
  private JTextField myEndDateEntry;
  private Calendar myCalendar = Calendar.getInstance();
  
  private JButton mySubmitButton;
  private JButton myModifyAvailabilityButton;

  private JList myAssetList;

  // cluster names and URLs from a plan server at a cluster
  private Vector myClusterNames = null;
  private Hashtable myClusterURLs = null;
  private String myClusterURL = null;

  // Called by AssetPerturbationApplet as
  // AssetPerturbationDisplay(getCodeBase())
  /**
   * Constructor - Used when AssetPerturbationDisplay embedded in an Applet
   *
   * @param clusterURL String - for Applet would be getCodeBase()
   */
  public AssetPerturbationDisplay(String codeBase) {
    getClusterInfo(getConnectionInfo(codeBase));
    initializeLayout();
  }

  /**
   * Constructor - Used when AssetPerturbationDisplay embedded in an application
   * User will be asked to select a cluster.
   */
  public AssetPerturbationDisplay() {
    getClusterInfo("");
    createFrame();
  }

  /**
   * createFrame - create JFrame when running as an application
   */
  private void createFrame() {
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    };
    JFrame frame = new JFrame("COUGAAR Asset Perturbation");
    frame.addWindowListener(windowListener);

    initializeLayout();
    frame.getContentPane().add(this);
    frame.pack();

    Dimension frameSize = frame.getPreferredSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - frameSize.width/2,
                      screenSize.height/2 - frameSize.height/2);

    frame.setVisible(true);
  }

  /**
   * initializeLayout - layout display
   *
   */
  private void initializeLayout() {
    JPanel criteriaPanel = new JPanel();
    criteriaPanel.setLayout(new GridBagLayout());
    myInternalPadding = new Insets(2, 10, 2, 10);
    myNoInternalPadding = new Insets(0, 0, 0, 0);
    myLabelInternalPadding = new Insets(0, 100, 0, 0);

    int gridx = 0;
    int gridy = 0;

    JLabel clusterLabel = new JLabel(SET_CLUSTER_LABEL);
    addComponent(criteriaPanel, clusterLabel, gridx++, gridy, 1, 1, 
                 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                 0, 0, myLabelInternalPadding, 1, 0);
    
    myClusterNameBox = makeClusterComboBox();
    addComponent(criteriaPanel, myClusterNameBox, gridx++, gridy++, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 1, 0);

    gridx = 0;

    // button to submit new request
    gridx = 0;
    mySubmitButton = new JButton(SUBMIT_REQUEST_LABEL);
    mySubmitButton.setActionCommand(SUBMIT_REQUEST_ACTION);
    mySubmitButton.addActionListener(new SubmitListener());
    addComponent(criteriaPanel, mySubmitButton, gridx, gridy++, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);



    JPanel topPanel = new JPanel();
    topPanel.setLayout(new GridBagLayout());
    addComponent(topPanel, criteriaPanel, 0, 0, 1, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 1, 0);


    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, topPanel);

    myAssetList = new JList();
    myAssetList.setCellRenderer(new AssetRenderer());

    JScrollPane listScrollPane = 
      new JScrollPane(myAssetList);
    add(BorderLayout.CENTER, listScrollPane);

    // Make start/end date entry
    JPanel modifyAssetPanel = new JPanel();
    modifyAssetPanel.setLayout(new GridBagLayout());

    gridx = 0;
    JLabel label = new JLabel(START_DATE_LABEL);
    addComponent(modifyAssetPanel, label, gridx++, gridy, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                 0, 0, myLabelInternalPadding, 1, 0);
    
    myStartDateEntry  = makeDateEntry(getDefaultDate());
    addComponent(modifyAssetPanel, myStartDateEntry, gridx++, gridy++, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 1, 0);

    gridx = 0;
    label = new JLabel(END_DATE_LABEL);
    addComponent(modifyAssetPanel, label, gridx++, gridy, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                 0, 0, myLabelInternalPadding, 1, 0);
    
    myEndDateEntry  = makeDateEntry(getDefaultDate());
    addComponent(modifyAssetPanel, myEndDateEntry, gridx++, gridy++, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 1, 0);


    // button to submit new request
    gridx = 0;
    myModifyAvailabilityButton = new JButton(MODIFY_AVAILABILITY_LABEL);
    myModifyAvailabilityButton.setEnabled(false);
    myModifyAvailabilityButton.setActionCommand(MODIFY_AVAILABILITY_ACTION);
    myModifyAvailabilityButton.addActionListener(new ModifyListener());
    addComponent(modifyAssetPanel, myModifyAvailabilityButton, gridx, gridy++, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);



    // display the expression that the user is composing
    gridx = 0;
    myScrollingTextLine = new ScrollingTextLine(30);
    addComponent(modifyAssetPanel, myScrollingTextLine, gridx, gridy++, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);

    add(BorderLayout.SOUTH, modifyAssetPanel);
  }

  /**
   * makeComboBox - make a JComboBox with cluster names
   */
  private JComboBox makeClusterComboBox() {
    JComboBox box = new JComboBox();
    for (int i = 0; i < myClusterNames.size(); i++)
      box.addItem(myClusterNames.elementAt(i));
    box.setEditable(true); // allow user to add their own choices

    return box;
  }

  /**
   * makeDateEntry - make a text field for date entry
   * BOZO - should probably be a real date entry
   *
   * @param defaultDate Date used as initial value
   */
  static private JTextField makeDateEntry(Date defaultDate) {

    JTextField entry  = new JTextField(myDateFormatter.format(defaultDate));
    entry.setEditable(true); // allow user to edit the date

    return entry;
  }

  private static GridBagConstraints myConstraints;
  /**
   * addComponent - add a component to a container with GridBagLayout
   * See GridBag doc for info on the various parameters
   *
   * @param container Container
   * @param component Component
   * @param gridx int
   * @param gridy int
   * @param gridwidth int
   * @param anchor int
   * @param fill int
   * @param ipadx int
   * @param ipady int
   * @param insets Insets
   * @param weigthx double
   * @param weighty double
   */
  static private void addComponent(Container container, Component component,
                            int gridx, int gridy, 
                            int gridwidth, int gridheight,
                            int anchor, int fill,
                            int ipadx, int ipady, Insets insets,
                            double weightx, double weighty) {
    GridBagLayout gbl = (GridBagLayout)container.getLayout();

    if (myConstraints == null) {
      myConstraints = new GridBagConstraints();
    }

    myConstraints.gridx = gridx;
    myConstraints.gridy = gridy;
    myConstraints.gridwidth = gridwidth;
    myConstraints.gridheight = gridheight;
    myConstraints.fill = fill;
    myConstraints.anchor = anchor;
    myConstraints.ipadx = ipadx;
    myConstraints.ipady = ipady;
    myConstraints.insets = insets;
    myConstraints.weightx = weightx;
    myConstraints.weighty = weighty;
    ((GridBagLayout)gbl).setConstraints(component, myConstraints);
    container.add(component);
  }


  /**
   * displayExpression - update contents of the status line
   *
   * @param exp String to display
   */
  private void displayExpression(String exp) {
    myScrollingTextLine.setText(exp);
  }

  /**
   * getClusterInfo - get the location of the cluster
   * Only used when running in an application
   */
  private void getClusterInfo(String connectionInfo) {
    if (connectionInfo.equals("")) {
      String host = 
        (String) OptionPane.showInputDialog(null,
                                            "Enter location of a cluster.",
                                            "Cluster Location",
                                            OptionPane.INFORMATION_MESSAGE,
                                            null, null, DEFAULT_HOST);
      if (host == null) {
        System.exit(0);
      }
      
      host = host.trim();
      
      if (host.length() == 0) {
        host = DEFAULT_HOST;
      }
      connectionInfo = "http://" + host + ":" + DEFAULT_PORT + "/";
    }     
      
    try {
      ConnectionHelper connection = 
        new ConnectionHelper(connectionInfo);
      myClusterURLs = connection.getClusterIdsAndURLs();
      if (myClusterURLs == null) {
        displayError("Unable to obtain cluster identifier to URL mappings.");
        System.exit(0);
      }
    } catch (Exception e) {
      displayError(e.toString());
      System.exit(0);
    }

    Enumeration names = myClusterURLs.keys();
    myClusterNames = new Vector();
    while (names.hasMoreElements()){
      myClusterNames.addElement(names.nextElement());
    }

    if (myClusterNames.size() == 0) {
      displayError("Unable to obtain cluster identifier to URL mappings.");
      System.exit(0);
    }
  }

  /**
   * getAssets - Connect to the URL which is a concatenation
   * of the COUGAAR clusters host name (localhost), the port (5555), and the 
   * AssetPerturbation PSP.  Parses HTML returned into AssetInfo objects and updates 
   * myAssetList with the new AssetInfos.
   *
   * @param clusterName String specifying the selected cluster
   */
  private void getAssets(String clusterName) {
    displayExpression("Getting cargo vehicles  from " + clusterName);

    byte[] reply = null;

    // if not an applet, fetch cluster names and URLs from server, once
    // then look up the cluster URL for each submission
    
    String clusterURL = (String)myClusterURLs.get(clusterName);
    String s = "";

    try {
      ConnectionHelper connection = 
        new ConnectionHelper(clusterURL, 
                             AssetPerturbationPSPConnectionInfo.current(),
                             false);

      //System.out.println("Connected " + connection);
      String query = AssetPerturbationMessage.generateQueryRequest();

      //System.out.println("Query - " + query);
      connection.sendData(query);

      InputStream is = connection.getInputStream();
    
      ByteArrayOutputStream os = new ByteArrayOutputStream();


      byte byteBuffer[] = new byte[512];
      int len;

      Vector rows = new Vector();
      
      while((len = is.read(byteBuffer, 0, byteBuffer.length)) > -1 ) {
        //System.out.println("Reading data");
        os.write(byteBuffer, 0, len);

        // Ensure that we bridge buffers correctly.
        StringBuffer stringBuffer = new StringBuffer(s);
        stringBuffer.append(new String(byteBuffer, 0, len));
        s = new String(stringBuffer);
        
        //System.out.println("Response - " + s);

        int start = 0;
        int end = 0;
        ParsePosition parsePosition = new ParsePosition(0);
        while (start < s.length()) { 
          
          String name = 
            AssetPerturbationMessage.parseParameter(s,
                                                    AssetPerturbationMessage.NAME_LABEL, 
                                                    AssetPerturbationMessage.DELIM,
                                                    parsePosition);

          if (name.equals("")) {
            break;
          }
        
          String uidStr = 
            AssetPerturbationMessage.parseParameter(s,
                                     AssetPerturbationMessage.UID_LABEL, 
                                     AssetPerturbationMessage.DELIM,
                                     parsePosition);

          if (uidStr.equals("")) {
            break;
          }

          // Got all the data - add a row to the table.
          AssetInfo assetInfo = new AssetInfo(name, uidStr);
          //System.out.println("Name = " + name);
          rows.add(assetInfo);

          // Reset start of parsing for next asset
          start = parsePosition.getIndex();
        }

        s = s.substring(start);
      }
      myAssetList.setListData(rows);
      displayExpression(rows.size() + " cargo vehicles retrieved.");

      if (rows.size() > 0) {
        myAssetList.setSelectedIndex(0);
        myModifyAvailabilityButton.setEnabled(true);
      } else {
        myModifyAvailabilityButton.setEnabled(false);
      }

    } catch (Exception e) {
      System.err.println("AssetDisplay.execute - " + 
                         "exception reading AssetPerturbationMessage output.");
      e.printStackTrace();
      displayError("Exception " + e + " retrieving info messages.");
    }
  }


  /**
   * getAssets - Connect to the URL which is a concatenation
   * of the COUGAAR clusters host name (localhost), the port (5555), and the 
   * AssetPerturbation PSP.  Parses HTML returned into AssetInfo objects and updates 
   * myAssetList with the new AssetInfos.
   *
   * @param clusterName String specifying the selected cluster
   */
  private void modifyAvailability(String clusterName, 
                                  AssetInfo assetInfo,
                                  Date startDate,
                                  Date endDate) {
    displayExpression(assetInfo.getName() + " unavailable from " + 
                      myDateFormatter.format(startDate) + 
                      " to " + myDateFormatter.format(endDate));

    // if not an applet, fetch cluster names and URLs from server, once
    // then look up the cluster URL for each submission
    
    String clusterURL = (String)myClusterURLs.get(clusterName);
    String s = "";

    try {
      ConnectionHelper connection = 
        new ConnectionHelper(clusterURL, 
                             AssetPerturbationPSPConnectionInfo.current(),
                             false);

      String request = 
        AssetPerturbationMessage.generateMakeUnavailableRequest(assetInfo.getUID(),
                                                                startDate,
                                                                endDate);

      //System.out.println("Request - " + request);
      connection.sendData(request);

      String reply = new String(connection.getResponse());
      //System.out.println("got response " + reply);

      myDestroyed.put(assetInfo.getName(), assetInfo);
      //System.out.println("last is " + assetInfo.getName());
    } catch (Exception e) {
      System.err.println("AssetDisplay.execute - " + 
                         "exception reading AssetPerturbationMessage output.");
      e.printStackTrace();
      displayError("Exception " + e + " retrieving info messages.");
    }
  }

  private void displayError(String reply) {
      OptionPane.showOptionDialog(null, reply, reply, 
                                  OptionPane.DEFAULT_OPTION,
                                  OptionPane.ERROR_MESSAGE,
                                  null, null, null);
  }

  private static String getConnectionInfo(String codeBase) {
    //BOZO - major kludge - assuming that html loaded through
    //the same connection.
    String portStr = Integer.toString(DEFAULT_PORT);
    int start = codeBase.indexOf(portStr);
    int end = codeBase.indexOf("/", start);

    String connectionInfo = codeBase.substring(0, end + 1);

    //System.out.println("getConnectionInfo - codeBase " + codeBase + 
    //                   " connectionInfo " + connectionInfo);

    return connectionInfo;
  }

  private Date getDefaultDate() {
    myCalendar.setTime(new Date(System.currentTimeMillis()));
    int year = myCalendar.get(Calendar.YEAR);
    int month = myCalendar.get(Calendar.MONTH);
    int date = myCalendar.get(Calendar.DAY_OF_MONTH);
    myCalendar.clear();
    myCalendar.set(year, month, date);
    
    return myCalendar.getTime();
  }
    
  public static void main(String[] args) {
    ThemeFactory.establishMetalTheme();
    
    new AssetPerturbationDisplay();
  }

  private class SubmitListener implements ActionListener {

    /**
     * actionPerformed - handle submit button actions
     *
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
      mySubmitButton.setEnabled(false);
      
      String clusterSelected = "";
      clusterSelected = (String)myClusterNameBox.getSelectedItem();
      
      getAssets(clusterSelected);
      
      mySubmitButton.setEnabled(true);
      return;
    }
  } // end SubmitListener

  private class ModifyListener implements ActionListener {

    /**
     * actionPerformed - handle modify availability button
     *
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
      myModifyAvailabilityButton.setEnabled(false);
      
      String clusterSelected = "";
      clusterSelected = (String)myClusterNameBox.getSelectedItem();

      AssetInfo assetInfo = (AssetInfo)myAssetList.getSelectedValue();

      try {
        Date startDate = myDateFormatter.parse(myStartDateEntry.getText());
        Date endDate = myDateFormatter.parse(myEndDateEntry.getText());
        
        modifyAvailability(clusterSelected, assetInfo, startDate, endDate);
      } catch (ParseException pe) {
          OptionPane.showOptionDialog(null,
                                      "Unable to parse date entry.\n" + 
                                      "Should be in mm/dd/yyyy hh:mm format",
                                      "Error",
                                      OptionPane.DEFAULT_OPTION,
                                      OptionPane.ERROR_MESSAGE,
                                      null, null, null);
      }
      
      myModifyAvailabilityButton.setEnabled(true);
      return;
    }
  } // end ModifyListener

  protected class AssetRenderer extends DefaultListCellRenderer {
    
    public AssetRenderer() {
      super();
    }
    
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      
      AssetInfo assetInfo = (AssetInfo)value;
      String text = "";
      JLabel label = null;
      if (assetInfo == null) {
        text = "";
      } else {
        text= assetInfo.getName();
      }

      setText(text);
      if (isSelected) {
        if (myDestroyed.get(assetInfo.getName()) != null)
          this.setBackground(Color.red);
        else
          this.setBackground(list.getSelectionBackground());
        this.setForeground(list.getSelectionForeground());
      }
      else if (myDestroyed.get (assetInfo.getName ()) != null) {
        this.setBackground (Color.red);
      } else {
        this.setBackground(list.getBackground());
        this.setForeground(list.getForeground());
      }
      this.setEnabled(list.isEnabled());
      this.setFont(list.getFont());
      
      return this;
    }
  } // ends AssetRenderer   

  private class AssetInfo {
    private String myName;
    private String myUID;
    
    public AssetInfo(String name, String uidStr) {
      myName = name;
      myUID = uidStr;
    }

    public String getName() {
      return myName;
    }

    public String getUID() {
      return myUID;
    }
  } //ends AssetRenderer

  
}






