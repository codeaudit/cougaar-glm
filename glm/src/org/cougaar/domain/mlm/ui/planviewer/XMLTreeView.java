/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

public class XMLTreeView extends Frame implements ActionListener {
  public static String SAVE_COMMAND = "Save";
  byte[] buffer;
  DOMTree dTree;

  public XMLTreeView(String title, byte[] buffer) {
    super(title);
    this.buffer = buffer;

    //    addWindowListener(new java.awt.event.WindowAdapter() {
    //      public void windowClosing(java.awt.event.WindowEvent e) {
    //  System.exit(0);
    //      }
    //    });

    // parse input stream into tree
    //TreeNode root = null;

    dTree = new DOMTree();

    dTree.setRowHeight(18);

    Document root = null;
    try {
      DOMParser p = new DOMParser();
      p.parse(new InputSource(new ByteArrayInputStream(buffer)));
      root = p.getDocument();
    } catch (Exception e) {
      e.printStackTrace();
    }

    dTree.setDocument(root);

    //    getContentPane().add("Center", new JScrollPane(new JTree(root)));
    add("Center", new JScrollPane(dTree));
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

    if(dTree != null) {
      expandTree();
    }
  }

  private void expandTree() {
    int rows = 0;
    for(int levels=0; levels <=4; levels++) {
      rows = dTree.getRowCount();
      for(int i=0; i < rows; i++) {
	dTree.expandRow(i);
      }
    }
  }

  public void dispose() {
    removeAll();
    buffer = null;
    super.dispose();
    System.gc();
  }


  // parse input buffer and write to file

  private void writeDocumentToFile(String pathname) {
    try {
      DOMParser parser = new DOMParser();
      parser.parse(new InputSource(new ByteArrayInputStream(buffer)));
      Document doc = parser.getDocument();
      FileOutputStream fos = new FileOutputStream(pathname);
      PrintWriter pw = new PrintWriter(fos);
      XMLSerializer serializer = new XMLSerializer(pw, new OutputFormat());
      serializer.serialize(doc);
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
