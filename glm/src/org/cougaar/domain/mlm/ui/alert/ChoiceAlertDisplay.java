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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import org.cougaar.util.ThemeFactory;
import org.cougaar.util.OptionPane;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

/**
 * Subscribes to alerts from the specified cluster
 */

public class ChoiceAlertDisplay extends AlertDisplay implements ActionListener {

  private JButton []myChoiceButtons;

  // cluster names and URLs from a plan server at a cluster
  private String []myChoices;

  // Called by XMLApplet as
  // ChoiceAlertDisplay(getContentPane(), getCodeBase())
  /**
   * Constructor - Used when ChoiceAlertDisplay embedded in an Applet
   *
   * @param clusterURL String - for Applet would be getCodeBase()
   */
  public ChoiceAlertDisplay(String clusterURL, String uid, String text, 
                            String []choices) {
    super(clusterURL, uid, text);
    myChoices = choices;
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
   * makeButtonPanel - make a panel with the alert choices as buttons
   */
  private JPanel makeButtonPanel() {
    myChoiceButtons = new JButton[myChoices.length];
    JPanel buttonPanel = new JPanel();


    for (int i = 0; i < myChoices.length; i++) {
      myChoiceButtons[i] = new JButton(myChoices[i]);
      myChoiceButtons[i].setActionCommand(Integer.toString(i));
      myChoiceButtons[i].addActionListener(this);
      buttonPanel.add(myChoiceButtons[i]);
    }

    return buttonPanel;
  }

  /**
   * actionPerformed - handle user actions.
   * Currently limited to submit request button
   *
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent event) {
    String choiceStr = event.getActionCommand();

    Object source = event.getSource();

    if (source instanceof JButton) {
      // Disable all buttons
      setChoicesEnabled(false);

      int choice = 0;
      try {
        choice = Integer.parseInt(choiceStr);
      } catch (Exception e) {
        // Malformed choice action. Strictly internal error
        System.out.println("ChoiceAlertDisplay.actionPerformed() - " +
                           "bad choice " + choiceStr + ". Must be integer.");
        System.exit(-1);
      }

      if (handleChoice(getUID(), choice)) {
          OptionPane.showOptionDialog(this, "Alert successfully updated.",
                                      "Status", 
                                      OptionPane.DEFAULT_OPTION,
                                      OptionPane.INFORMATION_MESSAGE,
                                      null, null, null); 
      } else {
          OptionPane.showOptionDialog(this, "Unable to update alert.", "Error",
                                      OptionPane.DEFAULT_OPTION,
                                      OptionPane.ERROR_MESSAGE,
                                      null, null, null);
        //Enable all buttons
        setChoicesEnabled(true);
      }
    } else {
      System.out.println("ChoiceAlertDisplay.actionPerformed() - " + 
                         "ignoring action from " + source);
    }
  }


  /**
   * handleChoice - Connect to the URL which is a concatenation
   * of the COUGAAR clusters host name (localhost), the port (5555), and the Alert
   * PSP.  Starts AlertThread to handle the returned alerts.
   *
   * @param clusterName String specifying the selected cluster
   */
  private boolean handleChoice(String uidText,  int choice) {
    System.out.println("ChoiceAlertDisplay:handleChoice - uid = " + uidText +
                       " choice = " + choice);
    boolean success = true;

    try {
      ConnectionHelper connection = 
        new ConnectionHelper(getClusterURL(),
                             ModifyAlertPSPConnectionInfo.current(),
                             true);
      
      String modifyRequest = PSP_ModifyAlert.generateChoicePostData(getUID(), 
                                                                    true,
                                                                    choice);
      System.out.println("ChoiceAlertDisplay:handleChoice - modify request " + 
                         modifyRequest);
      connection.sendData(modifyRequest);
      
      InputStream is = connection.getInputStream();
      
      String reply = new String(connection.getResponse());
      System.out.println("got response " + reply);
      
      int start = reply.indexOf(PSP_ModifyAlert.URL_LABEL);
      
      // Check is response includes a new url
      if (start > -1) {
        start += PSP_ModifyAlert.URL_LABEL.length();
        int end = reply.indexOf(PSP_ModifyAlert.DELIM, start);
        
        if (end < 0) {
          // BOZO - replace
          System.out.println("ChoiceAlertThread.handleChoice(): didn't find " + 
                             PSP_ModifyAlert.DELIM);
          success = false;
          return success;
        }
        
        String filename = reply.substring(start, end);
        
        Component appletParent = 
          SwingUtilities.getAncestorOfClass(JApplet.class, this);
        if (appletParent != null) {
          // In applet
          JApplet parent = (JApplet)appletParent;
          String urlStr = getClusterURL() + filename;
          System.out.println("showDocument: " + urlStr);
          
          try {
            parent.getAppletContext().showDocument(new URL(urlStr));
          } catch (MalformedURLException mue) {
            mue.printStackTrace();
            success = false;
          }
        } else {
          try {
            String cmdStr = 
              BROWSER_PATH + " " +  getClusterURL() + filename;
            System.out.println("exec: " + cmdStr);
            Runtime.getRuntime().exec(cmdStr);
          } catch (Exception ioe) {
            ioe.printStackTrace();
            success = false;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      success = false;
    }


    return success;
  }

  private void setChoicesEnabled(boolean enableFlag) {
    for (int i = 0; i < myChoices.length; i++) {
      myChoiceButtons[i].setEnabled(enableFlag);
    }
  }

  private static final String BROWSER_TAG = "-browser";
  private static String BROWSER_PATH = "";

  static void main(String[] args) {
    if ((args.length < 2) || 
        (!args[0].equals(BROWSER_TAG))) {
      System.out.println("Usage: ChoiceAlertDisplay " + 
                         BROWSER_TAG + " <browser path>");
      System.exit(-1);
    }

    ChoiceAlertDisplay.BROWSER_PATH = args[1];

    ThemeFactory.establishMetalTheme();
    
    Frame frame = new Frame();

    String []choices = {"View Info Messages"};
    ChoiceAlertDisplay display = 
      new ChoiceAlertDisplay("http://171.78.30.35:5555/$AlertCluster/alpine/demo/",
                             "AlertCluster/30",
                             "AlertText-1" +
                             "aaabbb\n" + 
                             "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                             "12345678990---\n" + 
                             "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm\n" +
                             "nnooppppp\n" +
                             "giraffe\n" + 
                             "elephant\n" +
                             "rhinoceros\n" + 
                             "gnu\n" + "gazelle\n" + "gorilla",
                             choices);
    
    frame.add(display);
    frame.pack();

    frame.show();
  }
}





