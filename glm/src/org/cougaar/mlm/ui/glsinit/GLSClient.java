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

package org.cougaar.mlm.ui.glsinit;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import org.cougaar.mlm.ui.SwingWorker;

/**
 * UI for invoking the GLSInitServlet to publish the OPLAN and send GLS Root.
 * This may be run stand-alone, or from the CSMART console.
 * @property org.cougaar.ui.userAuthClass used to specify the class to provide 
 *             User Authentication support. Defaults to 
 *             <code>org.cougaar.core.security.userauth.UserAuthenticatorImpl</code>, 
 *             not delivered with Cougaar.
 **/
public class GLSClient extends JPanel {
  String agentURL = null; // of the form http://host:port/$agent
  GLSClient glsClient; // for reference by inner classes

  /** optional connection fields **/
  private JTextField protocolField = new JTextField(10);
  private JTextField hostField = new JTextField(10);
  private JTextField portField = new JTextField(10);
  private JTextField agentField = new JTextField(10);

  /** for feedback to user on whether root GLS was successful **/
  private JLabel initLabel = new JLabel("Next Stage Description");
  private JLabel oplanPubLabel = new JLabel("No Oplan Info retrieved");

  /** A button to push to kick things off **/
  private JButton initButton = new JButton("Send Next Stage");
  private JButton oplanButton = new JButton("Get Oplan Info");
//   private JButton rescindButton = new JButton("Rescind GLS root");
  private JButton connectButton = new JButton("Connect");

  /** A label for displaying the given Oplan **/
  private JLabel oplanLabel = new JLabel();
  
  private String myOplanId;
  //private String myStageName;

  /** A label for displaying the current Stage **/
  private JLabel currStageLabel = new JLabel("Current Stage Description");
  private String currentStage;

  /** A text field for setting the CDay **/
  private JTextField cDayField = new JTextField(10); 
  
//    /** number of GLS tasks published **/
//    private int numGLS = 0;

  private ButtonListener buttonListener = new ButtonListener();

  /** servlets we send to; must match definitions in servlet classes **/
  private static final String GLS_INIT_SERVLET = "glsinit";
  private static final String GLS_REPLY_SERVLET = "glsreply";
  /** commands and parameters sent to those servlets **/
  private static final String CONNECT_COMMAND = "connect";
  private static final String INIT_COMMAND = "publishgls";
//   private static final String RESCIND_COMMAND = "rescindgls";
  private static final String OPLAN_COMMAND = "getopinfo";
  private static final String OPLAN_PARAM_NAME = "&oplanID=";
  private static final String CDATE_PARAM_NAME = "&c0_date=";

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
    cDayField.setEditable(false);
    init(true);
  }

  /**
   * A GUI panel that can be enclosed in a frame or internal frame.
   * GUI that contacts servlets in society to publish oplan
   * and send and rescind GLS tasks.
   * Specify an agentURL, which is not displayed, and is not editable.
   * @param agentURL agent url of the form protocol://host:port/$agent
   */
  public GLSClient(String agentURL) {
    super(null);
    this.agentURL = agentURL;
    init(false);
  }

  private void init(boolean displayConnectFields) {
    glsClient = this;

    // Could only do this secure init in certain circumstances?
    // move to getURLString()?
    doSecureUserAuthInit();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    if (displayConnectFields) 
      createConnectionPanel();
    else
      createSimpleConnectionPanel();
    createOplanPanel();
    createStageDescPanel();
    createInitPanel();
  }

  // Invoke the NAI security code, if available
  private void doSecureUserAuthInit() {
    String securityUIClass = System.getProperty("org.cougaar.ui.userAuthClass");
    
    if (securityUIClass == null) {
      securityUIClass = "org.cougaar.core.security.userauth.UserAuthenticatorImpl";
    }
    
    Class cls = null;
    try {
      cls = Class.forName(securityUIClass);
    } catch (ClassNotFoundException e) {
      System.err.println("Not using secure User Authentication: " + securityUIClass);
    } catch (ExceptionInInitializerError e) {
      System.err.println("Unable to use secure User Authentication: " + securityUIClass + ". ");
      e.printStackTrace();
    } catch (LinkageError e) {
      System.err.println("Not using secure User Authentication: " + securityUIClass);
    }
    
    if (cls != null) {
      try {
	cls.newInstance();
      } catch (Exception e) {
	System.err.println("Error using secure User Authentication (" + securityUIClass + ")");
	e.printStackTrace();
      }
    }
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
    //rescindButton.setEnabled(false);
    connectButton.setEnabled(false);
  }

  /** creates an x_axis oriented panel containing two components */
  private JPanel createXPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    panel.add(comp1);
    if (comp2 != null)
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
    oplanButton.setFocusPainted(false);
    oplanButton.addActionListener(buttonListener);
    oplanButton.setActionCommand(OPLAN_COMMAND);
    
    // turn this off until connection is made (even though it'll work)
    oplanButton.setEnabled(false); 

    JPanel opButtonPanel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    opButtonPanel.add(oplanPubLabel,
                new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(5, 5, 5, 5),
                                       0, 0));
    opButtonPanel.add(oplanButton,
                new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));

    JPanel infoComboPanel = new JPanel(new GridBagLayout());
    x = 0;
    y = 0;
    infoComboPanel.add(new JLabel("Oplan: "),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    infoComboPanel.add(oplanLabel,
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.CENTER,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
   
    x=0;
    infoComboPanel.add(new JLabel("Set C0 Day"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
 
    infoComboPanel.add(cDayField,
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.CENTER,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));

    JPanel oplanInfoPanel = createXPanel(opButtonPanel, infoComboPanel);
    oplanInfoPanel.setBorder(BorderFactory.createTitledBorder("Oplan"));
    add(oplanInfoPanel);
  }


  private void createStageDescPanel(){
    JPanel stagePanel = new JPanel(new FlowLayout());
    stagePanel.setBorder(BorderFactory.createTitledBorder("Current Stage"));
    stagePanel.add(currStageLabel);

    add(stagePanel);
  }



  private void createInitPanel() {
    initButton.setFocusPainted(false);
    initButton.setEnabled(false); // Leave this disabled until we have oplans
//     rescindButton.setFocusPainted(false);
//     rescindButton.setEnabled(false); // Leave this disabled until we have oplans
    initButton.addActionListener(buttonListener);
    initButton.setActionCommand(INIT_COMMAND);
//     rescindButton.addActionListener(buttonListener);
//     rescindButton.setActionCommand(RESCIND_COMMAND);
    
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

//     JPanel rescindPanel = new JPanel(new GridBagLayout());
//     x = 0;
//     y = 0;
//     rescindPanel.add(rescindButton,
//                    new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
//                                           GridBagConstraints.WEST,
//                                           GridBagConstraints.HORIZONTAL,
//                                           new Insets(0, 5, 5, 5),
//                                           0, 0));


//     JPanel glsPanel = createXPanel(glsButtons, rescindPanel);
     JPanel glsPanel = createXPanel(glsButtons, null);
    glsPanel.setBorder(BorderFactory.createTitledBorder("Next Stage"));
    add(glsPanel);
  }

  /**
   * Creates url of the form:
   * protocol://hostname:port/$agentname/servlet/?command=commandname&oplanID=oplanid&cDate=cdate
   */
  private String getURLString(String servlet, String command, String oplanId, String cDate) {
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
    if (cDate != null) {
      urlString.append(CDATE_PARAM_NAME);
      urlString.append(cDate);
    }
    return urlString.toString();
  }

  /**
   * Invoked when the user selects buttons:
   * Publish Oplan, Send GLS Root, Rescind GLS Root
   * Sends appropriate command to agent.
   */
  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      String command = ae.getActionCommand();
      String c0Date = null;
      if (command.equals(INIT_COMMAND)){
          initButton.setEnabled(false);
          initLabel.setText("");
          currStageLabel.setText(currentStage);
          c0Date = (String)cDayField.getText();
      }
//       if (command.equals(INIT_COMMAND) || command.equals(RESCIND_COMMAND)) {
//         c0Date = (String)cDayField.getText();
//       }
      String urlString = getURLString(GLS_INIT_SERVLET, command, myOplanId, c0Date);
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
   * Attempts to connect to the OSD.GOV(NCA) agent in the society running
   * in the current experiment.
   */
  private class ConnectionButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      String urlString = getURLString(GLS_REPLY_SERVLET, 
                                      CONNECT_COMMAND, null, null);
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
				     urlConnection.getURL().getProtocol() + "://" +
                                     urlConnection.getURL().getHost() + ":" +
                                     urlConnection.getURL().getPort());
        }

  	String inputLine;
  	while ((inputLine = in.readLine()) != null && !stopping) {
	  if (inputLine.indexOf("oplan") > 0) {//>=
            addOplanInfo(inputLine);
          }          
// 	  else if (inputLine.indexOf("GLS") > 0)//>=
//             updateGLSTasks(inputLine);
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
				      urlConnection.getURL().getProtocol() + "://" +
                                      urlConnection.getURL().getHost() + ":" +
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
				      urlConnection.getURL().getProtocol() + "://" +
                                      urlConnection.getURL().getHost() + ":" +
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
     * Add OPlan c0 date read from the agent to the display,
     * and update the GUI for the oplan name.  Also read 
     * next stage of oplan to send.
     */
    private void addOplanInfo(String s) {
      int nameIndex = s.indexOf("name=") + 5;
      int idIndex = s.indexOf("id=");
      String name = s.substring(nameIndex, idIndex).trim();
      oplanLabel.setText(name);

      myOplanId = s.substring(idIndex +3, idIndex + 8);

      int dateIndex = s.indexOf("c0_date=") + 8;
      int endOfDateInd = s.indexOf("nextStage") -1;
      String c0Date = s.substring(dateIndex,endOfDateInd);
      cDayField.setEditable(true);
      cDayField.setText(c0Date);
      
      oplanButton.setEnabled(false);
      oplanPubLabel.setText("Oplan Info Retrieved");
      
      int stageIndex = s.indexOf("nextStage=") + 10;
      int endOfStageIndex = s.indexOf("stageDesc") -1;
      String stageName = s.substring(stageIndex, endOfStageIndex);
      if (stageName.equals("All Stages Sent")) {
        initButton.setText(stageName);
        initLabel.setText("");
      }
      else {
        int stageDescIndex = s.indexOf("stageDesc=") + 10;
        String stageDesc = s.substring(stageDescIndex, s.length()-1);
        initButton.setText("Send " +stageName);
        initButton.setEnabled(true);
        String nameAndDesc = stageName +": "+stageDesc;
        initLabel.setText(nameAndDesc);
        currentStage = nameAndDesc;
      }
      getRootPane().setDefaultButton(initButton); 
    }

//     /**
//      * Update information about GLS tasks from information
//      * read from the agent and update the GUI.
//      */
//     private void updateGLSTasks(String s) {
//       int numGLS = Integer.parseInt(s.substring(s.indexOf("GLS")+4, 
//                                                 s.length()-1));
//       initLabel.setText(numGLS + " GLS Tasks published");
      //Disable rescind button temporarily until its working again.
//       if (numGLS > 0) 
//         rescindButton.setEnabled(true);
//       else
//         rescindButton.setEnabled(false);
//     }

  } // end LineReader class


  /**
   * Create a GUI panel; enclose it in a frame, and display the frame.
   * Creates the GUI with a default host, port, and agent
   * which the user can edit.
   */
  public static void main(String args[]) {
    GLSClient gui = new GLSClient("localhost", "8800", "OSD.GOV");
    JFrame frame = new JFrame("OPlan Control");
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
