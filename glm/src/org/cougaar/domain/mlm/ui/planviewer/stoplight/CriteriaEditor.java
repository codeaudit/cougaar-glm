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
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import javax.swing.*;

import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.mlm.ui.data.UIUnitStatus;

import org.w3c.dom.*;

public class CriteriaEditor implements ActionListener {
  public static final String APPLY = "Apply";
  public static final String CRITERIA_FILENAME = "stoplight.criteria.xml";

  JComboBox perCentOperationBox;
  JTextField perCentField;
  JComboBox daysLateOperationBox;
  JTextField daysLateField;
  JComboBox colorBox;
  JButton addButton;
  JButton deleteButton;
  JButton saveButton;
  JList criteriaList;

  String perCentOperation;
  int perCent;
  String daysLateOperation;
  int daysLate;
  String color;
  DefaultListModel criteriaModel;
  AsciiPrinter printer;
  boolean isApplet;

  public CriteriaEditor(boolean isApplet) {
    this.isApplet = isApplet;
    criteriaModel = new DefaultListModel();
    // initialize criteria from configuration file, if this isn't an applet
    if (!isApplet)
      initializeCriteria();
  }

  public JPanel createDisplay(ActionListener listener) {
    JPanel editPanel = new JPanel(new BorderLayout());

    JPanel expressionPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.ipadx = 0;
    gbc.ipady = 0;
    gbc.insets = new Insets(2, 5, 2, 5);
    gbc.weightx = 0;
    gbc.weighty = 0;

    colorBox = new JComboBox();
    colorBox.addItem(new ColoredSquare(UIUnitStatus.YELLOW));
    colorBox.addItem(new ColoredSquare(UIUnitStatus.RED));
    colorBox.setSelectedIndex(0);
    expressionPanel.add(colorBox, gbc);

    gbc.gridx++;
    perCentOperationBox = makeOperationBox();
    perCentOperationBox.setSelectedIndex(0);
    perCentOperationBox.addActionListener(this);
    expressionPanel.add(perCentOperationBox, gbc);

    gbc.gridx++;
    perCentField = new JTextField(3);
    perCentField.setHorizontalAlignment(JTextField.RIGHT);
    expressionPanel.add(perCentField, gbc);

    gbc.gridx++;
    expressionPanel.add(new JLabel("%"), gbc);

    gbc.gridx++;
    daysLateOperationBox = makeOperationBox();
    daysLateOperationBox.setSelectedIndex(0);
    expressionPanel.add(daysLateOperationBox, gbc);

    gbc.gridx++;
    daysLateField = new JTextField(3);
    daysLateField.setHorizontalAlignment(JTextField.RIGHT);
    expressionPanel.add(daysLateField, gbc);

    gbc.gridx++;
    expressionPanel.add(new JLabel("days late"), gbc);

    JPanel buttonPanel = new JPanel(new FlowLayout());
    addButton = new JButton("Add");
    addButton.addActionListener(this);
    buttonPanel.add(addButton);
    deleteButton = new JButton("Delete");
    deleteButton.addActionListener(this);
    buttonPanel.add(deleteButton);

    JPanel tmp = new JPanel(new BorderLayout());
    tmp.add(expressionPanel, "North");
    tmp.add(buttonPanel, "South");

    criteriaList = new JList(criteriaModel);
    criteriaList.setCellRenderer(new CriteriaListCellRenderer());
    criteriaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    criteriaList.setSelectedIndex(0);
    JScrollPane scroll = new JScrollPane(criteriaList);

    editPanel.add(tmp, "North");
    editPanel.add(scroll, "Center");

    JButton applyButton = new JButton("Apply to Stoplight Chart");
    if (listener != null) {
      applyButton.setActionCommand(APPLY);
      applyButton.addActionListener(listener);
    }

    JPanel bottom = new JPanel(new FlowLayout());
    bottom.add(applyButton);
    if (!isApplet) {
      saveButton = new JButton("Save Criteria");
      saveButton.addActionListener(this);
      bottom.add(saveButton);
    }
    editPanel.add(bottom, "South");
    return editPanel;
  }

  private void initializeCriteria() {
    Element root = null;
    try {
      Document doc = 
        ConfigFileFinder.parseXMLConfigFile("stoplight.criteria.xml");
      if (doc != null) {
        root = doc.getDocumentElement();
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    if (root != null) {
      Object obj = XMLObjectFactory.parseObject(null, root);
      if (obj instanceof Vector) {
        Vector v = (Vector)obj;
        for (int i = 0; i < v.size(); i++) {
          criteriaModel.addElement(v.elementAt(i));
        }
      }
    }
  }

  public DefaultListModel getModel() {
    return criteriaModel;
  }

  private JComboBox makeOperationBox() {
    String[] OperationValues = { "=", "!=", "<", ">", "<=", ">=" };
    JComboBox box = new JComboBox();
    for (int i = 0; i < OperationValues.length; i++)
      box.addItem(OperationValues[i]);
    box.setEditable(false);
    return box;
  }

  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, "Error", 
                                  JOptionPane.ERROR_MESSAGE);
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    Object source = e.getSource();
    if (source.equals(addButton)) {
      perCentOperation = (String)perCentOperationBox.getSelectedItem();
      try {
        perCent = Integer.parseInt(perCentField.getText());
      } catch (Exception exc) {
        displayErrorString("Enter number");
      }
      daysLateOperation = (String)daysLateOperationBox.getSelectedItem();
      try {
        daysLate = Integer.parseInt(daysLateField.getText());
      } catch (Exception exc) {
        displayErrorString("Enter number");
      }
      color = ((ColoredSquare)colorBox.getSelectedItem()).getColor();
      criteriaModel.addElement(new CriteriaParameters(perCentOperation,
                               perCent, daysLateOperation,
                               daysLate, color));
    } else if (source.equals(deleteButton)) {
      Object selectedObject = criteriaList.getSelectedValue();
      if (selectedObject != null)
        criteriaModel.removeElement(selectedObject);
    } else if (source.equals(saveButton)) {
      File criteriaFile = ConfigFileFinder.locateFile(CRITERIA_FILENAME);
      if (criteriaFile != null) {
        FileOutputStream f = null;
        try {
          f = new FileOutputStream(criteriaFile);
        } catch (Exception exc) {
          System.out.println("Cannot open file: " +
                             CRITERIA_FILENAME + " to save criteria: " + exc);
          displayErrorString("Cannot save criteria");
        }
        if (f != null) {
          printer = (AsciiPrinter)AbstractPrinter.createPrinter("xml", f);
          // get a vector from the criteria model so that it can be "printed"
          Vector tmp = new Vector(criteriaModel.size());
          for (int i = 0; i < criteriaModel.size(); i++)
            tmp.addElement(criteriaModel.getElementAt(i));
          printer.printObject(tmp);
        }
      } else {
        System.out.println("Cannot locate file: " +
                           CRITERIA_FILENAME + " to save criteria.");
        displayErrorString("Cannot save criteria");
      }
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    CriteriaEditor ce = new CriteriaEditor(false);
    f.getContentPane().add(ce.createDisplay(null));
    f.pack();
    f.setVisible(true);
  }
}



