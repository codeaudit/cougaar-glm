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

package org.cougaar.mlm.debug.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Create a JFrame to display information requested by the user.
  Used for all displays generated from the main display.
 */

public class UIFrame extends JFrame {
  private static int WIDTH = 500;// width and height of initial frame
  private static int HEIGHT = 300;
  public static String UPDATE_COMMAND = "Update";
  public static String SAVE_COMMAND = "Save";
  public static String EXPAND_COMMAND = "Expand";

  /** Create a frame with the specified title and containing the
    specified component, which is either a tree or a bar graph.
    Optionally creates buttons for updating display (with info from
    a remote cluster), saving the display (for tree displays only),
    fully expanding the display (for tree displays only).
    @param title the string to display at the top of the frame
    @param component the component to put in the frame
    @param actionListener object to notify when buttons are pressed
    @param provideUpdateFunction true to provide update button
    @param provideSaveFunction true to provide save button
    @param provideExpandFunction true to provide expand button
    */

  public UIFrame(String title, Component component, 
		 ActionListener actionListener, 
		 boolean provideUpdateFunction,
		 boolean provideSaveFunction,
		 boolean provideExpandFunction) {
    super(title);
    setForeground(Color.black);
    setBackground(Color.lightGray);

    // cleanup for this display only, for example
    // remove any listeners that we've set up!!!
    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { e.getWindow().dispose(); }
    };

    addWindowListener(windowListener);
    setSize(WIDTH, HEIGHT);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(component);
    getContentPane().add("Center", scrollPane);

    // add buttons if needed to provide save function and
    // update display
    if (provideSaveFunction || provideUpdateFunction || provideExpandFunction) {
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      if (provideSaveFunction) {
	JButton saveButton = new JButton(SAVE_COMMAND);
	saveButton.setToolTipText("Save displayed information to a file.");
	saveButton.addActionListener(actionListener);
	saveButton.setActionCommand(SAVE_COMMAND);
	buttonPanel.add(saveButton);
      }
      if (provideUpdateFunction) {
	buttonPanel.setBackground(Color.blue); // signifies remote cluster
	JButton updateButton = new JButton(UPDATE_COMMAND);
	updateButton.setToolTipText("Retrieve new information from remote cluster.");
	updateButton.addActionListener(actionListener);
	updateButton.setActionCommand(UPDATE_COMMAND);
	buttonPanel.add(updateButton);
      } 
      if (provideExpandFunction) {
	JButton expandButton = new JButton(EXPAND_COMMAND);
	getContentPane().add("North", buttonPanel);
	expandButton.setToolTipText("Expand information.");
	expandButton.addActionListener(actionListener);
	expandButton.setActionCommand(EXPAND_COMMAND);
	buttonPanel.add(expandButton);
      }
    }
    // create display at the upper left corner of the window
    setLocation(0, 0);
    // and display it
    setVisible(true);
  }

}
