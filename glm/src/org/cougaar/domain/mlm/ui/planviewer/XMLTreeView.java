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
