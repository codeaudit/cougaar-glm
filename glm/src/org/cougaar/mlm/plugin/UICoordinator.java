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

package org.cougaar.mlm.plugin;

import org.cougaar.util.ConfigFinder;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UICoordinator {
  static HashMap locations = new HashMap();

  private String name;

  private Rectangle bounds;

  static {
    try {
      doFileLayout(System.getProperty("org.cougaar.core.plugin.UICoordinator.ui_config",
				      "ui_config.txt"));
    }
    catch (IOException e) {
      doDefaultLayout();
    }
  }

  /**
   * The layout found in a file.
   **/
  private static void doFileLayout(String filename) throws IOException {

    InputStream fs = ConfigFinder.getInstance().open(filename);
    BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
    try {
      StreamTokenizer tokens = new StreamTokenizer(reader);
      tokens.commentChar('#');
      tokens.whitespaceChars(',', ',');
      while (true) {
	int ttype = tokens.nextToken();
	if (ttype == tokens.TT_EOF) return;
	if (ttype != '"' && ttype != '\'' && ttype != tokens.TT_WORD) throw new IOException("Bad file format");
	String title = tokens.sval;
	if (tokens.nextToken() != tokens.TT_NUMBER) throw new IOException("Bad file format");
	int x = (int) tokens.nval;
	if (tokens.nextToken() != tokens.TT_NUMBER) throw new IOException("Bad file format");
	int y = (int) tokens.nval;
	if (tokens.nextToken() != tokens.TT_NUMBER) throw new IOException("Bad file format");
	int width = (int) tokens.nval;
	if (tokens.nextToken() != tokens.TT_NUMBER) throw new IOException("Bad file format");
	int height = (int) tokens.nval;
	add(title, x, y, width, height);
      }
    }
    finally {
      reader.close();
    }
  }

  /**
   * Use this layout if a config file cannot be found.
   **/
  private static void doDefaultLayout() {
    add("GLSInitPlugIn",    		   	      0,   0, 378,  78);
    add("GLSRescindPlugIn", 		   	      0,  80, 378,  78);
    add("OplanPlugIn",      		   	      0, 160, 378,  78);
    add("AllocationAssessorPlugIn for <3ID>", 	      0, 240, 378, 400);
    add("MCCTriggerCreatorPlugIn",                  380,   0, 378,  78);
    add("PolicyPlugIn",                             380,  80, 378, 108);
    add("AllocationAssessorPlugIn for <3-69-ARBN>", 380, 160, 378, 400);
  }

  private UICoordinator(String name, int x, int y, int width, int height) {
    this.name = name;
    this.bounds = new Rectangle(x, y, width, height);
  }

  public static UICoordinator add(String name, int x, int y, int width, int height) {
    UICoordinator uic = new UICoordinator(name, x, y, width, height);
    locations.put(name, uic);
    return uic;
  }

  public static void setBounds(JFrame frame) {
    UICoordinator uic = (UICoordinator) locations.get(frame.getTitle());
    if (uic == null) {
      return;
    }
    frame.setBounds(uic.bounds);
  }

  private static GridBagConstraints buttonConstraints = new GridBagConstraints();
  private static GridBagConstraints labelConstraints = new GridBagConstraints();
  private static GridBagConstraints row2Constraints = new GridBagConstraints();
  static {
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = GridBagConstraints.RELATIVE;
    buttonConstraints.fill = GridBagConstraints.NONE;
    buttonConstraints.weightx = 0.0;
    buttonConstraints.anchor = GridBagConstraints.CENTER;
    buttonConstraints.insets = new Insets(2, 10, 2, 10);
    
    labelConstraints.gridx = 1;
    labelConstraints.gridy = buttonConstraints.gridy;
    labelConstraints.fill = GridBagConstraints.HORIZONTAL;
    labelConstraints.weightx = 1.0;
    labelConstraints.anchor = GridBagConstraints.CENTER;
    labelConstraints.insets = new Insets(2, 10, 2, 10);

    row2Constraints.gridx = 0;
    row2Constraints.gridy = GridBagConstraints.RELATIVE;
    row2Constraints.gridwidth = 2;
    row2Constraints.fill = GridBagConstraints.HORIZONTAL;
    row2Constraints.weightx = 1.0;
    row2Constraints.anchor = GridBagConstraints.CENTER;
    row2Constraints.insets = new Insets(2, 10, 2, 10);
  }

  public static void layoutButton(JPanel panel, Component button) {
    panel.setLayout(new GridBagLayout());
    panel.add(button, buttonConstraints);
  }

  public static void layoutButtonAndLabel(JPanel panel, Component button, JComponent label) {
    if (panel.getLayout() == null)
      panel.setLayout(new GridBagLayout());
    panel.add(button, buttonConstraints);
    if (label instanceof JLabel) {
      ((JLabel) label).setHorizontalAlignment(JLabel.RIGHT);
    } else {
      label.setAlignmentX(1f);
    }
    panel.add(label, labelConstraints);
  }

  public static void layoutSecondRow(JPanel panel, Component c) {
    panel.add(c, row2Constraints);
    panel.revalidate();
  }
}

    
