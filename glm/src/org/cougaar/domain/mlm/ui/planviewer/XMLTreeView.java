/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.ibm.xml.parser.Parser;
import com.ibm.xml.parser.Stderr;
import com.ibm.xml.parser.TXDocument;

public class XMLTreeView extends Frame implements ActionListener {
  public static String SAVE_COMMAND = "Save";
  byte[] buffer;
  JTree jtree;

  public XMLTreeView(String title, byte[] buffer) {
    super(title);
    this.buffer = buffer;

    //    addWindowListener(new java.awt.event.WindowAdapter() {
    //      public void windowClosing(java.awt.event.WindowEvent e) {
    //  System.exit(0);
    //      }
    //    });

    // parse input stream into tree
    TreeNode root = null;
    try {
      Parser p = new Parser("Log Plan");
      p.setElementFactory(new XMLTreeFactory());
      root = (TreeNode)p.readStream(new ByteArrayInputStream(buffer));
    } catch (Exception e) {
      e.printStackTrace();
    }

    //    getContentPane().add("Center", new JScrollPane(new JTree(root)));
    jtree = new JTree(root);
    add("Center", new JScrollPane(jtree));
    // add button to allow saving to file
    JButton saveButton = new JButton(SAVE_COMMAND);
    saveButton.setToolTipText("Save xml to a file.");
    saveButton.addActionListener(this);
    saveButton.setActionCommand(SAVE_COMMAND);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.add(saveButton);
    //    getContentPane().add("North", buttonPanel);
    add("North", buttonPanel);
    setSize(320, 480);
  }

  public void dispose() {
    removeAll();
    buffer = null;
    jtree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
    jtree.setSelectionModel(new DefaultTreeSelectionModel());
    super.dispose();
    System.gc();
  }


  // parse input buffer and write to file

  private void writeDocumentToFile(String pathname) {
    Parser parser = new Parser("LogPlan");
    TXDocument doc = parser.readStream(new ByteArrayInputStream(buffer));
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(pathname);
      PrintWriter pw = new PrintWriter(fos);
      doc.printWithFormat(pw);
    } catch (Exception e) {
      System.out.println("Could not write to file:" + pathname + " exception: " + e);
    }
  }

  // action listener -- just handles "save to file" button

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals(SAVE_COMMAND)) {
      FileDialog fileDialog = new FileDialog(this, "Save", FileDialog.SAVE);
      fileDialog.show();
      String dirname = fileDialog.getDirectory();
      String filename = fileDialog.getFile();
      if ((dirname != null) && (filename != null))
        writeDocumentToFile(dirname + filename);
    }
  }

}
