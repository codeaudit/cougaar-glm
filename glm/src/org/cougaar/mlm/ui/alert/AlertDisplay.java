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
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;

/**
 * Abstract base class for alert displays. Provides makeSourcePanel(), 
 * and makeTextPanel(). Also provides access to common class level variables
 * Extenders responsible for laying out display and implmenting actions 
 * associated with buttons.
 */

abstract public class AlertDisplay extends JPanel {
  protected static final int DEFAULT_TEXT_WIDTH = 30;

  //Shouldn't there be a common place to put this?
  protected static final String CLUSTER_NAME_DELIM = "/";

  // cluster names and URLs from a plan server at a cluster
  private boolean myIsApplet;
  private String myClusterURL = null;
  private String myUID;
  private String myText;

  /**
   * Constructor - 
   *
   * @param clusterURL String - for Applet would be getCodeBase()
   * @param uid String - alert's unique identifier.
   * @param text String - explanation for the alert
   */
  public AlertDisplay(String clusterURL, String uid, String text) {
    myIsApplet = true;
    myClusterURL = clusterURL;
    myUID = uid;
    myText = text;
  }

  /**
   * makeSourcePanel - make a JPanel with the source cluster information
   * @return JPanel
   */
  protected JPanel makeSourcePanel(String uid) {
    JLabel label = new JLabel("Alert from " + getClusterNameFromUID(uid));
    label.setFont(new Font("Dialog", Font.BOLD, 15));

    JPanel sourcePanel = new JPanel();
    sourcePanel.add(label);

    return sourcePanel;
  }

  private static Map monospacedAttributeMap = new HashMap();
  static {
    monospacedAttributeMap.put(TextAttribute.FAMILY, "monospaced");
    monospacedAttributeMap.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
  }

  /**
   * makeTextPanel - make a JPanel with alert text
   *
   * @return JPanel
   */
  protected JPanel makeTextPanel(String text) {
    JTextArea textArea = new JTextArea(text);
    
    textArea.setColumns(DEFAULT_TEXT_WIDTH);
    textArea.setFont(UIManager.getFont("TextArea.font").deriveFont(monospacedAttributeMap));
    System.out.println(textArea.getFont());
//      textArea.setForeground(UIManager.getColor("Label.foreground"));
//      textArea.setBackground(UIManager.getColor("Label.background"));
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane textScrollPane = new JScrollPane(textArea);

    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BorderLayout());
    textPanel.add(BorderLayout.CENTER, textScrollPane);

    return textPanel;
  } 

  
  /**
   * getClusterURL - return the cluster URL
   * 
   * @return String - cluster URL
   */
  protected String getClusterURL() {
    return myClusterURL;
  }

  /**
   * getUID - return the uid for the associated alert
   * 
   * @return String - alert's uid
   */
  protected String getUID() {
    return myUID;
  }

  /**
   * getText - return the text associated with the alert
   * 
   * @return String - alert text
   */
  protected String getText() {
    return myText;
  }

  /**
   * getClusterNameFromUID - parse cluster name from the UID
   * BOZO - this should be a utility associated with the cluster
   * 
   * @param uuid String alert's unique id
   * @return String - cluster name
   */
  protected static String getClusterNameFromUID(String uuid) {
    int i = uuid.indexOf(CLUSTER_NAME_DELIM);
    String name;
    
    if (i < 1) {
      System.out.println("AlertDisplay.getClusterNameFromUID() - unable to parse uuid " + uuid);
      name = "";
    } else {
      name = uuid.substring(0,i);
    }

    return name;
  }
}










