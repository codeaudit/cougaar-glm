/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.report;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.ScrollingTextLine;

/**
 * Displays Reports from specified cluster.
 * Allows user to specify a screen date - only get Reports since xxx
 */

public class ReportDisplay extends JPanel implements ActionListener {
  
  static final private String SET_CLUSTER_LABEL = "Cluster:";
  static final private String SET_CLUSTER_ACTION = "setCluster";

  static final private String SET_DATE_LABEL = "Messages Since: ";

  static final private String SUBMIT_REQUEST_LABEL ="Submit Request";
  static final private String SUBMIT_REQUEST_ACTION ="submit";

  static final private SimpleDateFormat myDateFormatter = 
    new SimpleDateFormat("MM/dd/yyyy HH:mm");

  static final private DateFormat myStandardFormatter = 
    DateFormat.getDateInstance();

  static final private String UNDEFINED = "Undefined";

  static final private String DEFAULT_HOST = "localhost";
  static final private int DEFAULT_PORT = 5555;

  private ScrollingTextLine myScrollingTextLine;
  private Insets myNoInternalPadding;
  private Insets myInternalPadding;
  private Insets myLabelInternalPadding;
  
  // JComboBox for cluster names
  private JComboBox myClusterNameBox;

  // JTextField for screen date
  // Should be converted to real date entry 
  private JTextField myDateEntry;
  private Calendar myCalendar = Calendar.getInstance();
  
  private JButton mySubmitButton;

  private ReportTable myReportTable;

  // cluster names and URLs from a plan server at a cluster
  private Vector myClusterNames = null;
  private Hashtable myClusterURLs = null;
  private String myClusterURL = null;

  // Called by ReportApplet as
  // ReportDisplay(getCodeBase())
  /**
   * Constructor - Used when ReportDisplay embedded in an Applet
   *
   * @param clusterURL String - for Applet would be getCodeBase()
   */
  public ReportDisplay(String codeBase) {
    getClusterInfo(getConnectionInfo(codeBase));
    initializeLayout();
  }

  /**
   * Constructor - Used when ReportDisplay embedded in an application
   * User will be asked to select a cluster.
   */
  public ReportDisplay() {
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
    JFrame frame = new JFrame("COUGAAR Reports");
    frame.setForeground(Color.black);
    frame.setBackground(Color.lightGray);
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
    GridBagLayout gbl = new GridBagLayout();
    criteriaPanel.setLayout(gbl);
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

    JLabel label = new JLabel(SET_DATE_LABEL);
    addComponent(criteriaPanel, label, gridx++, gridy, 1, 1, 
                 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                 0, 0, myLabelInternalPadding, 1, 0);
    
    myDateEntry  = makeDateEntry(getDefaultDate());
    addComponent(criteriaPanel, myDateEntry, gridx++, gridy++, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 1, 0);



    // button to submit new request
    gridx = 0;
    mySubmitButton = new JButton(SUBMIT_REQUEST_LABEL);
    mySubmitButton.setActionCommand(SUBMIT_REQUEST_ACTION);
    mySubmitButton.addActionListener(this);
    addComponent(criteriaPanel, mySubmitButton, gridx, gridy++, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);


    // display the expression that the user is composing
    gridx = 0;
    myScrollingTextLine = new ScrollingTextLine(50);
    addComponent(criteriaPanel, myScrollingTextLine, gridx, gridy++, 
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

    myReportTable = new ReportTable();

    JScrollPane tableScrollPane = 
      new JScrollPane(myReportTable);
    add(BorderLayout.CENTER, tableScrollPane);
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
   * actionPerformed - handle user actions.
   * Currently limited to submit request button
   *
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    Object source = e.getSource();

    // user hit "Submit" button to submit request to the cluster
    if (command.equals(SUBMIT_REQUEST_ACTION)) {
      mySubmitButton.setEnabled(false);

      String clusterSelected = "";
      clusterSelected = (String)myClusterNameBox.getSelectedItem();

      try {
        Date screenDate = myDateFormatter.parse(myDateEntry.getText());
        
        submit(clusterSelected, screenDate);
      } catch (ParseException pe) {
        JOptionPane.showMessageDialog(this, 
                                      "Unable to parse date entry.\n" + 
                                      "Should be in mm/dd/yyyy hh:mm format",
                                      "Error", JOptionPane.ERROR_MESSAGE);
      }

      mySubmitButton.setEnabled(true);
      return;
    }
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
        (String)JOptionPane.showInputDialog(null,
                                            "Enter location of a cluster.",
                                            "Cluster Location",
                                            JOptionPane.INFORMATION_MESSAGE,
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
   * submit - Connect to the URL which is a concatenation
   * of the COUGAAR clusters host name (localhost), the port (5555), and the 
   * Report PSP.  Screen date added as a queryParameter. Parses HTML 
   * returned and displays the reports (text and date).
   *
   * @param clusterName String specifying the selected cluster
   */
  private void submit(String clusterName, Date screenDate) {
    displayExpression("Getting Reports since " + screenDate + 
                        " from " + clusterName);

    byte[] reply = null;

    // if not an applet, fetch cluster names and URLs from server, once
    // then look up the cluster URL for each submission
    
    String clusterURL = (String)myClusterURLs.get(clusterName);
    String s = "";

    try {
      ConnectionHelper connection = 
        new ConnectionHelper(clusterURL, 
                             ReportPSPConnectionInfo.current(),
                             false);

      String query = PSP_Report.generateQuery(screenDate);

      System.out.println("Query - " + query);
      connection.sendData(query);

      InputStream is = connection.getInputStream();
    
      ByteArrayOutputStream os = new ByteArrayOutputStream();


      byte byteBuffer[] = new byte[512];
      int len;

      Vector rows = new Vector();
      int rowCount = 0;
      
      while((len = is.read(byteBuffer, 0, byteBuffer.length)) > -1 ) {
        System.out.println("Reading data");
        os.write(byteBuffer, 0, len);

        // Ensure that we bridge buffers correctly.
        StringBuffer stringBuffer = new StringBuffer(s);
        stringBuffer.append(new String(byteBuffer, 0, len));
        s = new String(stringBuffer);
    
        int start = 0;
        int end = 0;
        DateFormat pspDateFormatter = PSP_Report.getDateFormat();
        while ((start = s.indexOf(PSP_Report.DATE_LABEL)) >=0) {

          String dateStr = 
            PSP_Report.parseParameter(s,
                                      PSP_Report.DATE_LABEL, 
                                      PSP_Report.DELIM,
                                      true);

          if (dateStr.equals("")) {
            break;
          }
          

          Date date = null;
          try {
            date = pspDateFormatter.parse(dateStr);
          } catch (ParseException pe) {
            System.out.println("Unable to parse date: " + dateStr);
          }
      
          start += PSP_Report.DATE_LABEL.length() + dateStr.length() + 
            PSP_Report.DELIM.length();

          String text = 
            PSP_Report.parseParameter(s.substring(start),
                                      PSP_Report.TEXT_LABEL, 
                                      PSP_Report.DELIM,
                                      true);

          if (text.equals("")) {
            break;
          }

          start += PSP_Report.TEXT_LABEL.length() + text.length() + 
            PSP_Report.DELIM.length();
          s = s.substring(start);

          // Got all the data - add a row to the table.
          Vector row = new Vector(); 
          if (date == null) {
            row.add(UNDEFINED);
          } else {
            row.add(myDateFormatter.format(date));
          }
          row.add(text);

          rows.add(row);
          rowCount++;
        }
      }
      myReportTable.updateModel(rows);
      displayExpression(rowCount + " Reports retrieved.");
    } catch (Exception e) {
      System.out.println("ReportDisplay.execute - " + 
                         "exception reading PSP_Report output.");
      e.printStackTrace();
      displayError("Exception " + e + " retrieving reports.");
    }
  }


  private void displayError(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

  private static String getConnectionInfo(String codeBase) {
    //BOZO - major kludge - assuming that html loaded through
    //the same connection.
    String portStr = Integer.toString(DEFAULT_PORT);
    int start = codeBase.indexOf(portStr);
    int end = codeBase.indexOf("/", start);

    String connectionInfo = codeBase.substring(0, end + 1);

    System.out.println("getConnectionInfo - codeBase " + codeBase + 
                       " connectionInfo " + connectionInfo);

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

    new ReportDisplay();
  }

}





