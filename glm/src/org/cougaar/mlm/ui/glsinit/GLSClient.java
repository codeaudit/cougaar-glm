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

package org.cougaar.mlm.ui.glsinit;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.LayoutManager;

import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.cougaar.mlm.ui.SwingWorker;

public class GLSClient {


  private static String EXIT_ON_CLOSE_PROP =
    "org.cougaar.mlm.plugin.organization.GLSClient.exitOnClose";
  private static boolean exitOnClose = System.getProperty(EXIT_ON_CLOSE_PROP, "true").equals("true");

  /** frame for  UI **/
  private JFrame frame;

  /** connection info **/
  private JTextField hostField = new JTextField("localhost", 10);
  private JTextField portField = new JTextField("8800", 10); 
  private JTextField agentField = new JTextField("NCA", 10);

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

  /** A panel to hold the GUI **/
  private JPanel outerPanel = new JPanel((LayoutManager) null);

  /** number of GLS tasks published **/
  private int numGLS = 0;

  private ButtonListener buttonListener = new ButtonListener();

  public void createGUI() {
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

    createConnectionPanel();
    createOplanPanel();
    createInitPanel();

    frame = new JFrame("GLS");
    if (exitOnClose) frame.setDefaultCloseOperation(3);
    frame.getRootPane().setDefaultButton(connectButton); // hitting return sends GLS
    frame.setLocation(0,0);
    frame.setContentPane(outerPanel);
    frame.pack();
    frame.setVisible(true);
  }
  
  /** creates an x_axis oriented panel containing two components */
  private JPanel createXPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    panel.add(comp1);
    panel.add(comp2);
    return panel;
  }


  private JPanel createYPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    panel.add(comp1);
    panel.add(comp2);
    return panel;
  }

  private void createConnectionPanel() {
    JPanel connectPanel = new JPanel((LayoutManager) null);
    connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));
    connectPanel.setBorder(BorderFactory.createTitledBorder("Connection"));

    JLabel label = new JLabel("Host: ");
    connectPanel.add(createXPanel(label, hostField));

    label = new JLabel("Port: ");
    connectPanel.add(createXPanel(label, portField));

    label = new JLabel("Agent: ");
    connectPanel.add(createXPanel(label, agentField));

    connectButton.addActionListener(new ConnectionButtonListener());
    connectPanel.add(connectButton);

    outerPanel.add(connectPanel);
  }

  private void createInitPanel() {

    initButton.setEnabled(false); // Leave this disabled until we have oplans
    rescindButton.setEnabled(false); // Leave this disabled until we have oplans
    initButton.addActionListener(buttonListener);
    initButton.setActionCommand("publishgls");
    rescindButton.addActionListener(buttonListener);
    rescindButton.setActionCommand("rescindgls");

    JLabel oplanComboLabel = new JLabel("Select Oplan");
    JPanel buttonPanel = createYPanel(initLabel, initButton);
    buttonPanel.add(rescindButton);
    
    JPanel comboPanel = createYPanel(oplanComboLabel, oplanCombo);
    JPanel glsPanel = createXPanel(buttonPanel, comboPanel);
    glsPanel.setBorder(BorderFactory.createTitledBorder("GLS"));
    outerPanel.add(glsPanel);
  }


  private void createOplanPanel() {

    JPanel oplanPanel = new JPanel((LayoutManager) null);
    oplanPanel.setLayout(new BoxLayout(oplanPanel, BoxLayout.Y_AXIS));
    oplanPanel.setBorder(BorderFactory.createTitledBorder("Oplan"));
    oplanButton.addActionListener(buttonListener);
    oplanButton.setActionCommand("sendoplan");
    updateOplanButton.addActionListener(buttonListener);
    updateOplanButton.setActionCommand("updateoplan");
    
    // turn this off until connection is made (even though it'll work)
    oplanButton.setEnabled(false); 
    // turn this off until an oplan shows up
    updateOplanButton.setEnabled(false); 

    JPanel buttons = createYPanel(oplanButton, updateOplanButton);
    oplanPanel.add(createXPanel(buttons, oplanLabel));
    outerPanel.add(oplanPanel);
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

  private static final String http = "http://";

  private StringBuffer getURLString(String servlet) {
    StringBuffer urlString = new StringBuffer();
    urlString.append(http);
    urlString.append(hostField.getText());
    urlString.append(':');
    urlString.append(portField.getText());
    urlString.append("/$");
    urlString.append(agentField.getText());
    urlString.append('/');
    urlString.append(servlet);
    urlString.append("?command=");
    return urlString;
  }

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {

      StringBuffer urlString = getURLString("glsinit");
      String ac = ae.getActionCommand();
      urlString.append(ac);
      if (ac.equals("publishgls") || ac.equals("rescindgls")) {
	urlString.append("&oplanID=");
	urlString.append(((OplanInfo)oplanCombo.getSelectedItem()).getOplanId());
      }
      URL url;
      try {
	url = new URL(urlString.toString());
	//System.out.println(urlString );
		      
      } catch (java.net.MalformedURLException me) {
	System.err.println(me);
	JOptionPane.showMessageDialog(outerPanel, "Invalid Host, Port, or Agent\nPlease check fields and try again", "Bad URL",  JOptionPane.PLAIN_MESSAGE);
	return;
      }
      //System.out.println(url.getProtocol() + url.getHost() + url.getPath());
      
      URLConnection urlConnection;
      try {
	urlConnection = url.openConnection();
	//System.out.println("urlConnection open");

 	//System.out.println("length " + urlConnection.getContentLength());
	
  	BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
  	String inputLine;
  	while ((inputLine = in.readLine()) != null) {
  	  //System.out.println(inputLine);
	}
  	in.close();
 	//System.out.println("length " + urlConnection.getContentLength());

      } catch (java.io.IOException e) {
	JOptionPane.showMessageDialog(outerPanel, "Could not connect to Host, Port, or Agent\nThe Servlet may not be running there.\nPlease check fields and try again", "No Servlet",  JOptionPane.PLAIN_MESSAGE);
	e.printStackTrace();
      } catch (Exception e) {
	e.printStackTrace();
      }

      //System.out.println("Button listener done");
    }
  }

  private class ConnectionButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {

      StringBuffer urlString = getURLString("glsreply");
      LineReader lr;

      urlString.append("connect");
      URL url;
      try {
	url = new URL(urlString.toString());
	//System.out.println(urlString );
	lr  =  new LineReader(url.openConnection());		      
      } catch (java.net.MalformedURLException me) {
	JOptionPane.showMessageDialog(outerPanel, "Invalid Host, Port, or Agent\nPlease check fields and try again", "Bad URL",  JOptionPane.PLAIN_MESSAGE);
	//me.printStackTrace();
	return;
      } catch (java.io.IOException ioe) {
	JOptionPane.showMessageDialog(outerPanel, "Could not connect to Host, Port, or Agent\nThe Servlet may not be running there.\nPlease check fields and try again", "No Servlet",  JOptionPane.PLAIN_MESSAGE);
	ioe.printStackTrace();
	return;
      }
      //System.out.println(url.getProtocol() + url.getHost() + url.getPath());

      lr.start();
      //System.out.println("Connect Button listener done");
    }
  }

  private class LineReader extends SwingWorker {
    private URLConnection urlConnection;
    public LineReader(URLConnection url) {
      super();
      urlConnection = url;
    }
    
    public Object construct() {
      try {
 	//System.out.println("length " + urlConnection.getContentLength());
	connectButton.setEnabled(false);
	oplanButton.setEnabled(true);
	frame.getRootPane().setDefaultButton(oplanButton); 

  	BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
  	String inputLine;
  	while ((inputLine = in.readLine()) != null) {
  	  //System.out.println(inputLine);
	  if (inputLine.indexOf("oplan") > 0) {
	    int nameIndex = inputLine.indexOf("name=") + 5;
	    int idIndex = inputLine.indexOf("id=");
	    String name = inputLine.substring(nameIndex, idIndex).trim();
	    String id = inputLine.substring(idIndex +3, inputLine.length()-1);
	    if (!comboContains(id)) {
	      oplanCombo.addItem(new OplanInfo(name, id));
	    }
	    oplanButton.setEnabled(false);
	    oplanLabel.setText("Oplan Published");
	    updateOplanButton.setEnabled(true);
	    initButton.setEnabled(true);
	    frame.getRootPane().setDefaultButton(initButton); 
	  }
	  int position = inputLine.indexOf("GLS");
	  if ( position > 0) {
	    numGLS = Integer.parseInt(inputLine.substring(position+4, inputLine.length()-1));
	      initLabel.setText(numGLS + " GLS Tasks published");
	    if (numGLS > 0) {
	      rescindButton.setEnabled(true);
	    } else {
	      rescindButton.setEnabled(false);
	    }
	  }
	  //wait(5);
	}
  	in.close();
 	//System.out.println("length " + urlConnection.getContentLength());

      } catch (java.io.IOException e) {
	connectButton.setEnabled(true);
	JOptionPane.showMessageDialog(outerPanel, "Could not connect to Host, Port, or Agent\nThe Servlet may not be running there.\nPlease check fields and try again", "Bad URL",  JOptionPane.PLAIN_MESSAGE);
	//e.printStackTrace();
	oplanButton.setEnabled(false);
	frame.getRootPane().setDefaultButton(connectButton); 
	return null;
      } catch (Exception e) {
	e.printStackTrace();
	return null;
      }

      return null;
    }
  }

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

  public static void main(String args[]) {
    GLSClient gui = new GLSClient();
    gui.createGUI();
  }
}
