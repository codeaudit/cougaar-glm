/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

package org.cougaar.mlm.ui.glsinit;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.Border;
import javax.swing.*;

import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.cougaar.mlm.ui.SwingWorker;

public class GLSClient extends JPanel {
  String agentURL = null; // of the form http://host:port/$agent
  GLSClient glsClient; // for reference by inner classes

  /** optional connection fields **/
  private JTextField protocolField = new JTextField(10);
  private JTextField hostField = new JTextField(10);
  private JTextField portField = new JTextField(10);
  private JTextField agentField = new JTextField(10);

  /** for feedback to user on whether root GLS was successful **/
  private JLabel initLabel = new JLabel ("0 GLS Tasks published ");
  private JLabel oplanLabel = new JLabel("No Oplan published");

  /** A button to push to kick things off **/
  private JButton initButton = new JButton("Send GLS root");
  private JButton oplanButton = new JButton("Publish Oplan");
  private JButton updateOplanButton = new JButton("Update Oplan");
  private JButton rescindButton = new JButton("Rescind GLS root");
  private JButton connectButton = new JButton("Connect");

  /** A combo box for selecting the Oplan **/
  private JComboBox oplanCombo = new JComboBox();

  /** number of GLS tasks published **/
  private int numGLS = 0;

  private ButtonListener buttonListener = new ButtonListener();

  /** servlets we send to; must match definitions in servlet classes **/
  private static final String GLS_INIT_SERVLET = "glsinit";
  private static final String GLS_REPLY_SERVLET = "glsreply";
  /** commands and parameters sent to those servlets **/
  private static final String CONNECT_COMMAND = "connect";
  private static final String INIT_COMMAND = "publishgls";
  private static final String RESCIND_COMMAND = "rescindgls";
  private static final String OPLAN_COMMAND = "sendoplan";
  private static final String UPDATE_OPLAN_COMMAND = "updateoplan";
  private static final String OPLAN_PARAM_NAME = "&oplanID=";

  private boolean stopping = false;

  /**
   * A GUI panel that can be enclosed in a frame or internal frame.
   * GUI that contacts servlets in society to publish oplan
   * and send and rescind GLS tasks.
   * Specify default host, port and agent; 
   * these are displayed in text fields, so the user can edit them.
   * This constructs a url of the form: http://host:port/$agent
   * @param host host name
   * @param port port number as a string
   * @param agent agentname
   */

  public GLSClient(String host, String port, String agent) {
//      super(null);
//      hostField.setText(host);
//      portField.setText(port);
//      agentField.setText(agent);
//      init(true);
    this("http", host, port, agent);
  }

  /**
   * A GUI panel that can be enclosed in a frame or internal frame.
   * GUI that contacts servlets in society to publish oplan
   * and send and rescind GLS tasks.
   * Specify default host, port and agent; 
   * these are displayed in text fields, so the user can edit them.
   * This constructs a url of the form: protocol://host:port/$agent
   * @param protocol http or https
   * @param host host name
   * @param port port number as a string
   * @param agent agentname
   */
  public GLSClient(String protocol, String host, String port, String agent) {
    super(null);
    protocolField.setText(protocol);
    hostField.setText(host);
    portField.setText(port);
    agentField.setText(agent);
    init(true);
  }

  /**
   * A GUI panel that can be enclosed in a frame or internal frame.
   * GUI that contacts servlets in society to publish oplan
   * and send and rescind GLS tasks.
   * Specify an agentURL, which is not displayed, and is not editable.
   * @param agentURL agent url of the form http://host:port/$agent
   */

  public GLSClient(String agentURL) {
    super(null);
    this.agentURL = agentURL;
    init(false);
  }

  private void init(boolean displayConnectFields) {
    glsClient = this;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    if (displayConnectFields) 
      createConnectionPanel();
    else
      createSimpleConnectionPanel();
    createOplanPanel();
    createInitPanel();
  }


  /**
   * Set the initial default button to be the connect button.
   * Must be done after placing the GUI in a frame or internal frame.
   * Also enables the connect button.
   */

  public void setDefaultButton() {
    JRootPane rootPane = getRootPane();
    if (rootPane != null) {
      rootPane.setDefaultButton(connectButton);
      connectButton.setEnabled(true);
    }
  }

  /**
   * Stop reading from any open input streams.
   */

  public void stop() {
    stopping = true;
    disableButtons();
  }

  private void disableButtons() {
    initButton.setEnabled(false);
    oplanButton.setEnabled(false);
    updateOplanButton.setEnabled(false);
    rescindButton.setEnabled(false);
    connectButton.setEnabled(false);
  }

  /** creates an x_axis oriented panel containing two components */
  private JPanel createXPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    panel.add(comp1);
    panel.add(comp2);
    return panel;
  }

  /** creates an y_axis oriented panel containing two components */
  private JPanel createYPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    panel.add(comp1);
    panel.add(comp2);
    return panel;
  }

  private JPanel createConnectionPanelWorker() {
    JPanel connectPanel = new JPanel();
    connectPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
    connectButton.setFocusPainted(false);
    connectButton.addActionListener(new ConnectionButtonListener());
    add(connectPanel);
    return connectPanel;
  }

  /** creates a connection panel with just the connect button **/
  private void createSimpleConnectionPanel() {
    JPanel connectPanel = createConnectionPanelWorker();
    connectPanel.add(connectButton);
  }

  /** creates connection panel with fields for editing host, port and agent **/
  private void createConnectionPanel() {
    JPanel connectPanel = createConnectionPanelWorker();
    connectPanel.setLayout(new GridBagLayout());
    JPanel buttons = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    buttons.add(new JLabel("Protocol"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(5, 5, 5, 5),
                                       0, 0));
    buttons.add(protocolField,
                new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                       GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(5, 5, 5, 5),
                                       0, 0));
    x = 0;
    buttons.add(new JLabel("Host"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    buttons.add(hostField,
                new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                       GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    x = 0;
    buttons.add(new JLabel("Port"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    buttons.add(portField,
                new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                       GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    x = 0;
    buttons.add(new JLabel("Agent"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    buttons.add(agentField,
                new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                       GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    connectPanel.add(createYPanel(buttons, connectButton));
  }

  private void createOplanPanel() {
    JPanel oplanPanel = new JPanel();
    oplanPanel.setLayout(new BoxLayout(oplanPanel, BoxLayout.Y_AXIS));
    oplanPanel.setBorder(BorderFactory.createTitledBorder("Oplan"));
    oplanButton.setFocusPainted(false);
    oplanButton.addActionListener(buttonListener);
    oplanButton.setActionCommand(OPLAN_COMMAND);
    updateOplanButton.setFocusPainted(false);
    updateOplanButton.addActionListener(buttonListener);
    updateOplanButton.setActionCommand(UPDATE_OPLAN_COMMAND);
    
    // turn this off until connection is made (even though it'll work)
    oplanButton.setEnabled(false); 
    // turn this off until an oplan shows up
    updateOplanButton.setEnabled(false); 

    JPanel buttons = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    buttons.add(oplanButton,
                new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(5, 5, 5, 5),
                                       0, 0));
    buttons.add(updateOplanButton,
                new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    oplanPanel.add(createXPanel(buttons, oplanLabel));
    add(oplanPanel);
  }

  private void createInitPanel() {
    initButton.setFocusPainted(false);
    initButton.setEnabled(false); // Leave this disabled until we have oplans
    rescindButton.setFocusPainted(false);
    rescindButton.setEnabled(false); // Leave this disabled until we have oplans
    initButton.addActionListener(buttonListener);
    initButton.setActionCommand(INIT_COMMAND);
    rescindButton.addActionListener(buttonListener);
    rescindButton.setActionCommand(RESCIND_COMMAND);

    JLabel oplanComboLabel = new JLabel("Select Oplan");
    JPanel buttonPanel = createYPanel(initLabel, initButton);
    buttonPanel.add(rescindButton);
    
    JPanel glsButtons = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    glsButtons.add(initLabel,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    glsButtons.add(initButton,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 5, 5, 5),
                                          0, 0));
    glsButtons.add(rescindButton,
                   new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 5, 5, 5),
                                          0, 0));
    JPanel comboPanel = new JPanel(new GridBagLayout());
    x = 0;
    y = 0;
    comboPanel.add(oplanComboLabel,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.CENTER,
                                          GridBagConstraints.NONE,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    comboPanel.add(oplanCombo,
                   new GridBagConstraints(x, y, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.CENTER,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    JPanel glsPanel = createXPanel(glsButtons, comboPanel);
    glsPanel.setBorder(BorderFactory.createTitledBorder("GLS"));
    add(glsPanel);
  }

  private boolean comboContains(String id) {
    for (int i = 0, n = oplanCombo.getItemCount(); i < n; i++) {
      OplanInfo current = (OplanInfo) oplanCombo.getItemAt(i);
      if (current.getOplanId().equals(id)) {
	return true;
      }
    }
    return false;
  }

  /**
   * Creates url of the form:
   * http://hostname:port/$agentname/servlet/?command=commandname&oplanID=oplanid
   */

  private String getURLString(String servlet, String command, String oplanId) {
    StringBuffer urlString = new StringBuffer();
    if (agentURL != null)
      urlString.append(agentURL);
    else {
      urlString.append(protocolField.getText());
      urlString.append("://");
      urlString.append(hostField.getText());
      urlString.append(':');
      urlString.append(portField.getText());
      urlString.append("/$");
      urlString.append(agentField.getText());
    }
    urlString.append('/');
    urlString.append(servlet);
    urlString.append("?command=");
    urlString.append(command);
    if (oplanId != null) {
      urlString.append(OPLAN_PARAM_NAME);
      urlString.append(oplanId);
    }
    return urlString.toString();
  }

  /**
   * Invoked when the user selects buttons:
   * Publish Oplan, Update Oplan, Send GLS Root, Rescind GLS Root
   * Sends appropriate command to agent.
   */

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      String command = ae.getActionCommand();
      String oplanId = null;
      if (command.equals(INIT_COMMAND) || command.equals(RESCIND_COMMAND))
        oplanId = ((OplanInfo)oplanCombo.getSelectedItem()).getOplanId();
      String urlString = getURLString(GLS_INIT_SERVLET, command, oplanId);
      URL url;
      URLConnection urlConnection;
      try {
	url = new URL(urlString.toString());
        if (url == null)
          return;
        urlConnection = url.openConnection();
        if (urlConnection == null)
          return;
  	BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
  	String inputLine;
  	while ((inputLine = in.readLine()) != null && !stopping) {
          // read and discard any feedback
	}
  	in.close();
        if (stopping)
          return;
      } catch (java.net.MalformedURLException me) {
	JOptionPane.showMessageDialog(glsClient,
                                      "Invalid Host, Port, Agent or Servlet.",
                                      "Bad URL", 
                                      JOptionPane.ERROR_MESSAGE);
        me.printStackTrace();
        return;
      } catch (java.io.IOException ioe) {
        if (stopping)
          return;
	JOptionPane.showMessageDialog(glsClient,
                                      "Could not connect to Host, Port, or Agent.\nThe Servlet may not be running there.",
                                      "No Servlet", 
                                      JOptionPane.ERROR_MESSAGE);
	ioe.printStackTrace();
      }
    }
  } // end of ButtonListener class

  /**
   * Invoked when user selects "Connect" button.
   * Attempts to connect to the NCA agent in the society running
   * in the current experiment.
   */

  private class ConnectionButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      String urlString = getURLString(GLS_REPLY_SERVLET, 
                                      CONNECT_COMMAND, null);
      URLConnection urlConnection = null;
      try {
	URL url = new URL(urlString);
        urlConnection = url.openConnection();
      } catch (java.net.MalformedURLException me) {
	JOptionPane.showMessageDialog(glsClient,
                                      "Invalid Host, Port, Agent or Servlet.",
                                      "Bad URL", 
                                      JOptionPane.ERROR_MESSAGE);
        me.printStackTrace();
	return;
      } catch (java.io.IOException ioe) {
	JOptionPane.showMessageDialog(glsClient,
                                      "Could not connect to Host, Port, or Agent.\nThe Servlet may not be running there.",
                                      "No Servlet", 
                                      JOptionPane.ERROR_MESSAGE);
	ioe.printStackTrace();
	return;
      }
      // read from open connection
      if (urlConnection != null) 
        (new LineReader(urlConnection)).start();
    }
  } // end ConnectionButtonListener

  /**
   * Reads information from an open URL connection.
   * Note that this keeps the URL connection open and
   * reads whenever new information is available.
   */

  private class LineReader extends SwingWorker {
    private URLConnection urlConnection;
    public LineReader(URLConnection urlConnection) {
      super();
      this.urlConnection = urlConnection;
    }
    
    public Object construct() {
      connectButton.setEnabled(false);
      oplanButton.setEnabled(true);
      getRootPane().setDefaultButton(oplanButton); 
      try {
  	BufferedReader in = 
          new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        // check for errors and discard any associated text
        int responseCode = 
          ((HttpURLConnection) urlConnection).getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
          String inputLine;
          while ((inputLine = in.readLine()) != null && !stopping) {
          }
          in.close();
          if (stopping)
            return null;
          throw new RuntimeException("Unable to contact: " +
                                     urlConnection.getURL().getHost() + " " +
                                     urlConnection.getURL().getPort());
        }

  	String inputLine;
  	while ((inputLine = in.readLine()) != null && !stopping) {
	  if (inputLine.indexOf("oplan") > 0)
            addOplan(inputLine);
	  else if (inputLine.indexOf("GLS") > 0)
            updateGLSTasks(inputLine);
	}
  	in.close();
        if (stopping)
          return null;
      } catch (java.io.IOException e) {
        if (stopping)
          return null;
	connectButton.setEnabled(true);
	JOptionPane.showMessageDialog(glsClient,
                                      "Could not read from servlet at: " +
                                      urlConnection.getURL().getHost() + " " +
                                      urlConnection.getURL().getPort(),
                                      "Bad Connection",
                                      JOptionPane.ERROR_MESSAGE);
        oplanButton.setEnabled(false);
        getRootPane().setDefaultButton(connectButton);
	return null;
      } catch (RuntimeException re) {
        if (stopping)
          return null;
        connectButton.setEnabled(true);
        JOptionPane.showMessageDialog(glsClient,
                                     "Could not contact servlet at: " +
                                      urlConnection.getURL().getHost() + " " +
                                      urlConnection.getURL().getPort() +
                        " ; servlet may not be initialized; retry later.",
                                     "Initialization Not Complete",
                                     JOptionPane.ERROR_MESSAGE);
        oplanButton.setEnabled(false);
        getRootPane().setDefaultButton(connectButton);
        return null;
      } catch (Exception e) {
        if (stopping)
          return null;
	e.printStackTrace();
	return null;
      }

      return null;
    }

    /**
     * Add OPlan read from the agent to the list of available oplans,
     * and update the GUI.
     */

    private void addOplan(String s) {
      int nameIndex = s.indexOf("name=") + 5;
      int idIndex = s.indexOf("id=");
      String name = s.substring(nameIndex, idIndex).trim();
      String id = s.substring(idIndex +3, s.length()-1);
      if (!comboContains(id)) {
        oplanCombo.addItem(new OplanInfo(name, id));
      }
      oplanButton.setEnabled(false);
      oplanLabel.setText("Oplan Published");
      updateOplanButton.setEnabled(true);
      initButton.setEnabled(true);
      getRootPane().setDefaultButton(initButton); 
    }

    /**
     * Update information about GLS tasks from information
     * read from the agent and update the GUI.
     */

    private void updateGLSTasks(String s) {
      int numGLS = Integer.parseInt(s.substring(s.indexOf("GLS")+4, 
                                                s.length()-1));
      initLabel.setText(numGLS + " GLS Tasks published");
      if (numGLS > 0) 
        rescindButton.setEnabled(true);
      else
        rescindButton.setEnabled(false);
    }

  } // end LineReader class

  private class OplanInfo {
    private String oplanName;
    private String oplanID;

    public OplanInfo(String name, String id) {
      oplanName = name;
      oplanID = id;
    }
    public String toString() {
      return oplanName;
    }
    public String getOplanId() {
      return oplanID;
    }
  }

  /**
   * Create a GUI panel; enclose it in a frame, and display the frame.
   * Creates the GUI with a default host, port, and agent
   * which the user can edit.
   */

  public static void main(String args[]) {
    GLSClient gui = new GLSClient("localhost", "8800", "NCA");
    JFrame frame = new JFrame("GLS");
    frame.getContentPane().add(gui);
    gui.setDefaultButton();
    String EXIT_ON_CLOSE_PROP =
      "org.cougaar.mlm.plugin.organization.GLSClient.exitOnClose";
    boolean exitOnClose = 
      System.getProperty(EXIT_ON_CLOSE_PROP, "true").equals("true");
    if (exitOnClose) 
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(0,0);
    frame.setContentPane(gui);
    frame.pack();
    frame.setVisible(true);
  }

}
