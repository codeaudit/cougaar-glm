/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.alert;

import java.awt.Container;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Set;

import javax.swing.*;

import org.cougaar.util.OptionPane;
import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.PSPConnectionInfo;
import org.cougaar.domain.mlm.ui.planviewer.ScrollingTextLine;

/**
 * Subscribes to alerts from the specified cluster
 */

public class AlertHandler implements ActionListener {
  static final private String SET_CLUSTER_LABEL = "Cluster:";
  static final private String SET_CLUSTER_ACTION = "setCluster";

  static final private String SUBMIT_REQUEST_LABEL ="Submit Request";
  static final private String SUBMIT_REQUEST_ACTION ="submit";

  static final private int WIDTH = 400; // width and height of frame
  static final private int HEIGHT = 150;

  private ScrollingTextLine myScrollingTextLine;
  private Insets myNoInternalPadding;
  private Insets myInternalPadding;
  private Insets myLabelInternalPadding;
  
  // JComboBox for cluster names
  private JComboBox myClusterNameBox;

  // cluster names and URLs from a plan server at a cluster
  private Set myClusterNames = null;
  private Hashtable myClusterURLs = null;

  private String myBrowserPath;

  /**
   * Constructor - 
   * User will be asked to select a cluster.
   */
  public AlertHandler(String browserPath) {
    myBrowserPath = browserPath;
    getClusterInfo();
    createFrame();
  }

  /**
   * createFrame - create JFrame when running as an application
   */
  private void createFrame() {
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    };
    JFrame frame = new JFrame("ALP Alerts");
    frame.addWindowListener(windowListener);

    doLayout(frame.getContentPane());
    frame.pack();

    Dimension frameSize = frame.getPreferredSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - frameSize.width/2,
                      screenSize.height/2 - frameSize.height/2);

    frame.setVisible(true);
  }

  /**
   * doLayout - layout display
   *
   * @param container Container for display
   */
  private void doLayout(Container container) {
    container.setLayout(new BorderLayout());

    JPanel criteriaPanel = new JPanel();
    criteriaPanel.setLayout(new GridBagLayout());

    myInternalPadding = new Insets(2, 10, 2, 10);
    myNoInternalPadding = new Insets(0, 0, 0, 0);
    myLabelInternalPadding = new Insets(0, 100, 0, 0);
   
    int gridx = 0;
    int gridy = 0;

    JPanel clusterPanel = new JPanel();
    clusterPanel.setLayout(new GridBagLayout());

    JLabel label = new JLabel(SET_CLUSTER_LABEL);
    addComponent(clusterPanel, label, gridx++, gridy, 1, 1, 
                 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                 0, 0, myInternalPadding, 1, 0);
    
    myClusterNameBox = makeClusterComboBox();
    addComponent(clusterPanel, myClusterNameBox, gridx++, gridy++, 1, 1, 
                 GridBagConstraints.WEST, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 1, 0);

    gridx = 0;
    gridy = 0;
    addComponent(criteriaPanel, clusterPanel, gridx++, gridy++,
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, 
                 GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);
    gridx = 0;

    // button to submit new request
    gridx = 0;
    JButton button = new JButton(SUBMIT_REQUEST_LABEL);
    button.setActionCommand(SUBMIT_REQUEST_ACTION);
    button.addActionListener(this);
    addComponent(criteriaPanel, button, gridx, gridy++, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);

    // display the expression that the user is composing
    gridx = 0;
    myScrollingTextLine = new ScrollingTextLine(30);
    addComponent(criteriaPanel, myScrollingTextLine, gridx, gridy++, 
                 GridBagConstraints.REMAINDER, 1, 
                 GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 0, 0, myInternalPadding, 0, 0);

    //JPanel topPanel = new JPanel();
    //topPanel.setLayout(new GridBagLayout());
    //addComponent(topPanel, criteriaPanel, 0, 0, 1, 1, 
    //           GridBagConstraints.WEST, GridBagConstraints.NONE,
    //           0, 0, myInternalPadding, 1, 0);

    container.add(BorderLayout.CENTER, criteriaPanel);
  }

  /**
   * makeComboBox - make a JComboBox with cluster names
   */
  private JComboBox makeClusterComboBox() {
    JComboBox box = new JComboBox(new Vector(myClusterNames));
    box.setEditable(true); // allow user to add their own choices
    return box;
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
      String clusterSelected = (String)myClusterNameBox.getSelectedItem();

      submit(clusterSelected);
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
  private void getClusterInfo() {
    myClusterURLs = ConnectionHelper.getClusterInfo(null);
    if (myClusterURLs == null) {
      displayError("Unable to obtain cluster identifier to URL mappings.");
      System.exit(0);
    }
    myClusterNames = myClusterURLs.keySet();
    if (myClusterNames.size() == 0) {
      displayError("No clusters found.");
      System.exit(0);
    }
  }

  /**
   * submit - Connect to the URL which is a concatenation
   * of the ALP clusters host name (localhost), the port (5555), and the Alert
   * PSP.  Starts AlertThread to handle the returned alerts.
   *
   * @param clusterName String specifying the selected cluster
   */
  private void submit(String clusterName) {
    // Do I need to save so I can kill the previous AlertThread when cluster is 
    // changed?

    String url = (String)myClusterURLs.get(clusterName);
    AlertThread alertThread = new MyAlertThread(clusterName, url);

    alertThread.setPriority(Thread.MAX_PRIORITY);
    alertThread.start();

    displayExpression("Subscribed to Alerts from " + clusterName);
  }

  public static final String BROWSER_TAG = "-browser";

  public static void main(String[] args) {
    if ((args.length < 2) || 
        (!args[0].equals(BROWSER_TAG))) {
      System.out.println("Usage: AlertHandler " + 
                         BROWSER_TAG + " <browser path>");
      System.exit(-1);
    }

    ThemeFactory.establishMetalTheme();

    new AlertHandler(args[1]);
  }

  private void displayError(String reply) {
      OptionPane.showOptionDialog(null, reply, reply,
                                  OptionPane.DEFAULT_OPTION,
                                  OptionPane.ERROR_MESSAGE,
                                  null, null, null);
  }

  // Does real work of processing the alerts
  private class MyAlertThread extends AlertThread {
    public MyAlertThread(String name, String url) {
      super(name, url);
    }
    public void displayError(String error) {
      AlertHandler.this.displayError(error);
    }

    public void handleItem(String uid, String title, String severity,
                           String filename, String choices) {
      String procName = myBrowserPath + " " + myClusterURL + filename;

      System.out.println("exec: " + procName);
      try {
        Runtime.getRuntime().exec(procName);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        displayError("Unable to start browser - " + myBrowserPath);
      }
            
      // BOZO - burnt offerings.
      // Under some set of circumstances, we're not getting a new
      // browser for each exec command.  Sleep seems to help.
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }

}




