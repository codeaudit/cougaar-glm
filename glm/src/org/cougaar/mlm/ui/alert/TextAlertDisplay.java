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
 
package org.cougaar.mlm.ui.alert;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import org.cougaar.util.OptionPane;
import javax.swing.JPanel;

import org.cougaar.util.ThemeFactory;

import org.cougaar.mlm.ui.planviewer.ConnectionHelper;

/**
 * Subscribes to alerts from the specified cluster
 */

public class TextAlertDisplay extends AlertDisplay implements ActionListener {
  static final private String ACKNOWLEDGE_LABEL = "Acknowledge";
  static final private String ACKNOWLEDGE_ACTION = "acknowledgeAlert";


  // Button for acknowledging the alert
  private JButton myAcknowledgeButton;


  /**
   * Constructor - Used when TextAlertDisplay embedded in an Applet
   *
   * @param clusterURL String - for Applet would be getCodeBase()
   * @param uid String - alert's uid.
   * @param text String - text associated with the alert
   */
  public TextAlertDisplay(String clusterURL, String uid, String text) {
    super(clusterURL, uid, text);
    initializeLayout();
  }

  /**
   * doLayout - layout display
   *
   */
  private void initializeLayout() {
    setLayout(new BorderLayout());

    JPanel sourcePanel = makeSourcePanel(getUID());
    add(BorderLayout.NORTH, sourcePanel);
    
    JPanel textPanel = makeTextPanel(getText());
    add(BorderLayout.CENTER, textPanel);

    JPanel buttonPanel = makeButtonPanel();
    add(BorderLayout.SOUTH, buttonPanel);
  }
    
  /**
   * makeButtonPanel - make a JPanel with the acknowledge button
   */
  private JPanel makeButtonPanel() {
    myAcknowledgeButton = new JButton(ACKNOWLEDGE_LABEL);
    myAcknowledgeButton.setActionCommand(ACKNOWLEDGE_ACTION);
    myAcknowledgeButton.addActionListener(this);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(myAcknowledgeButton);

    return buttonPanel;
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

    // user hit "Acknowledge" button 
    if (command.equals(ACKNOWLEDGE_ACTION)) {
      myAcknowledgeButton.setEnabled(false);

      if (acknowledge(getUID())) {
          OptionPane.showOptionDialog(this, "Alert successfully acknowledged",
                                      "Acknowledged", 
                                      OptionPane.DEFAULT_OPTION,
                                      OptionPane.INFORMATION_MESSAGE,
                                      null, null, null);
      } else {
          OptionPane.showOptionDialog(this, "Unable to acknowledge alert.",
                                      "Error",
                                      OptionPane.DEFAULT_OPTION,
                                      OptionPane.ERROR_MESSAGE,
                                      null, null, null);
          myAcknowledgeButton.setEnabled(true);
      }
    }
  }


  /**
   * acknowledge - Connect to the URL which is a concatenation
   * of the COUGAAR clusters host name (localhost), the port (5555), and the Alert
   * PSP.  Starts AlertThread to handle the returned alerts.
   *
   * @param clusterName String specifying the selected cluster
   */
  private boolean acknowledge(String uidText) {
    System.out.println("Acknowledging " + uidText);
    boolean status = true;
    boolean ack = true;


    try {
      ConnectionHelper connection = 
        new ConnectionHelper(getClusterURL(), 
                             ModifyAlertPSPConnectionInfo.current(),
                             true);
      
      String modifyRequest = 
        PSP_ModifyAlert.generateAckPostData(getUID(), true);
      
      System.out.println("Modify request " + modifyRequest);
      connection.sendData(modifyRequest);
      
      InputStream is = connection.getInputStream();
      
      String reply = new String(connection.getResponse());
      System.out.println("got response " + reply);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Unable to acknowledge alert");
      status = false;
    }

    return status;
  }

  private static final String BROWSER_TAG = "-browser";
  private static String BROWSER_PATH = "";

  static void main(String[] args) {
    if ((args.length < 2) || 
        (!args[0].equals(BROWSER_TAG))) {
      System.out.println("Usage: TextAlertDisplay " + 
                         BROWSER_TAG + " <browser path>");
      System.exit(-1);
    }

    TextAlertDisplay.BROWSER_PATH = args[1];

    ThemeFactory.establishMetalTheme();

    Frame frame = new Frame();

    TextAlertDisplay display = 
      new TextAlertDisplay("http://171.78.30.35:5555/$AlertCluster/alpine/demo/",
                           "AlertCluster/31",
                           "AlertText-1" + 
                           "aaabbb\n" + 
                           "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                           "12345678990---\n" + 
                           "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm\n" +
                           "nnooppppp\n" +
                           "giraffe\n" + 
                           "elephant\n" +
                           "rhinoceros\n" + 
                           "gnu\n" + "gazelle\n" + "gorilla");
    
    frame.add(display);
    frame.pack();

    frame.show();
  }
}











