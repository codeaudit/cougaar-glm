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

package org.cougaar.mlm.plugin;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cougaar.util.ConfigFinder;

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
    add("GLSInitPlugin",    		   	      0,   0, 378,  78);
    add("GLSRescindPlugin", 		   	      0,  80, 378,  78);
    add("OplanPlugin",      		   	      0, 160, 378,  78);
    add("AllocationAssessorPlugin for <3ID>", 	      0, 240, 378, 400);
    add("MCCTriggerCreatorPlugin",                  380,   0, 378,  78);
    add("PolicyPlugin",                             380,  80, 378, 108);
    add("AllocationAssessorPlugin for <3-69-ARBN>", 380, 160, 378, 400);
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

    
