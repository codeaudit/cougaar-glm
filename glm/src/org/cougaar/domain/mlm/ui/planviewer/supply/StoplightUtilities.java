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
 
package org.cougaar.domain.mlm.ui.planviewer.supply;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.cougaar.domain.mlm.ui.data.UIUnitStatus;

/**
 * Create stoplight table with drill down.
 */

public class StoplightUtilities {
  Society society;
  EquipmentInfo equipmentInfo;
  SocietyStatus societyStatus;
  DefaultTableModel dataModel;
  static final String DRILLDOWNEQUIPMENT = "DDE";
  static final String DRILLDOWNUNIT = "DDU";
  static final String DRILLDOWNBOTH = "DDB";
  static final String DRILLDOWNNONE = "DDN";
  static final String DRILLUPEQUIPMENT = "DUE";
  static final String DRILLUPUNIT = "DUU";
  static final String DRILLUPBOTH = "DUB";
  static final String DRILLUPNONE = "DUN";
  static final String UPDATE = "Update";

  public StoplightUtilities(Society society, EquipmentInfo equipmentInfo,
                            SocietyStatus societyStatus) {
    this.society = society;
    this.equipmentInfo = equipmentInfo;
    this.societyStatus = societyStatus;
  }

  public DefaultTableModel initDataModel() {
    // for debugging
    //    society.printSociety();
    //    equipmentInfo.printEquipmentInfo();
    //    societyStatus.printSocietyStatus();

    // Create a model of the data.
    dataModel = new NonEditableTableModel();

    Unit top = society.getUnit("Society");
    Vector corpsNames = top.getSubordinateNames();
    Vector divisionNames = new Vector();
    for (int i = 0; i < corpsNames.size(); i++) {
      Unit corpsUnit = society.getUnit((String)corpsNames.elementAt(i));
      Vector tmp = corpsUnit.getSubordinateNames();
      for (int j = 0; j < tmp.size(); j++)
        divisionNames.addElement(tmp.elementAt(j));
    }

    Equipment equipment =
      equipmentInfo.getEquipment(equipmentInfo.TOPEQUIPMENT);
    Vector equipmentClassNames = equipment.getSubordinateNames();
    String[] classNames = new String[equipmentClassNames.size()];
    for (int i = 0; i < equipmentClassNames.size(); i++)
      classNames[i] = (String)equipmentClassNames.elementAt(i);
    int rowLength = divisionNames.size() + 1;
    Vector columnIds = new Vector(rowLength);
    columnIds.addElement(equipmentInfo.TOPEQUIPMENT);
    for (int i = 0; i < divisionNames.size(); i++)
      columnIds.addElement(divisionNames.elementAt(i));
    dataModel.setColumnIdentifiers(columnIds);
    for (int i = 0; i < classNames.length; i++) {
      Vector row = new Vector(rowLength);
      row.addElement(classNames[i]);
      for (int j = 0; j < rowLength-1; j++)
        row.addElement(
             societyStatus.getStatus((String)columnIds.elementAt(j+1),
                                     classNames[i]));
      dataModel.addRow(row);
    }
    return dataModel;
  }

  /** For debugging, populate the default society and equipment lists
    with status. */

  public void setDefaultStatus() {
    Enumeration enumUnits = society.getAllUnits();
    while (enumUnits.hasMoreElements()) {
      Unit unit = (Unit)enumUnits.nextElement();
      String unitName = unit.getName();
      Enumeration enumEquipment = equipmentInfo.getAllEquipment();
      while (enumEquipment.hasMoreElements()) {
        Equipment equipment = (Equipment)enumEquipment.nextElement();
        String equipmentName = equipment.getName();
        societyStatus.setStatus(unitName, equipmentName, UIUnitStatus.GREEN);
      }
    }
  }

  /** Called to set column names after drilling down on units.
   */

  private Object[] setSubordinateColumnNames(int selectedColumn) {
    String unitName = dataModel.getColumnName(selectedColumn);
    Unit unit = society.getUnit(unitName);
    Vector subordinateNames = unit.getSubordinateNames();
    int nColumns = subordinateNames.size()+1;
    String columnNames[] = new String[nColumns];
    columnNames[0] = dataModel.getColumnName(0); // preserve equipment level name
    for (int i = 0; i < subordinateNames.size(); i++)
      columnNames[i+1] = (String)subordinateNames.elementAt(i);
    return (Object[])columnNames;
  }


  /** Called to set column names after drilling up on units.
   */

  private Object[] setSuperiorColumnNames(int selectedColumn) {
    String unitName = dataModel.getColumnName(selectedColumn);
    Unit unit = society.getUnit(unitName);
    String superiorName = unit.getSuperiorName(); // my superior
    Unit superiorUnit = society.getUnit(superiorName);
    Vector superiorUnits = society.getUnitsAtLevel(superiorUnit.getLevel());
    Vector superiorNames = new Vector();
    for (int i = 0; i < superiorUnits.size(); i++)
      superiorNames.addElement(((Unit)superiorUnits.elementAt(i)).getName());
    int nColumns = superiorNames.size()+1;
    Object columnNames[] = new Object[nColumns];
    columnNames[0] = dataModel.getColumnName(0); // preserve equipment level name
    for (int i = 1; i < nColumns; i++)
      columnNames[i] = superiorNames.elementAt(i-1);
    return columnNames;
  }

  /** Called to set row names after drilling down on equipment.
   */

  private Object[][] setSubordinateRowNames(int selectedRow) {
    String equipmentName = (String)dataModel.getValueAt(selectedRow, 0);
    Equipment equipment = equipmentInfo.getEquipment(equipmentName);
    Vector subordinateNames = equipment.getSubordinateNames();
    int nRows = subordinateNames.size();
    int nColumns = dataModel.getColumnCount();
    Object[][] newData = new Object[nRows][nColumns];
    for (int i = 0; i < nRows; i++) {
      newData[i][0] = subordinateNames.elementAt(i);
      for (int j = 1; j < nColumns; j++)
        newData[i][j] = 
          societyStatus.getStatus(dataModel.getColumnName(j),
                                  (String)newData[i][0]);
    }
    return newData;
  }

  /** Called to set row names after drilling up or down on equipment.
   */

  private Object[][] setSubordinateRowNames(Object[] columnNames,
                                            int selectedRow) {
    String equipmentName = (String)dataModel.getValueAt(selectedRow, 0);
    Equipment equipment = equipmentInfo.getEquipment(equipmentName);
    Vector subordinateNames = equipment.getSubordinateNames();
    int nRows = subordinateNames.size();
    int nColumns = columnNames.length;
    Object[][] newData = new Object[nRows][nColumns];
    for (int i = 0; i < nRows; i++) {
      newData[i][0] = subordinateNames.elementAt(i);
      for (int j = 1; j < nColumns; j++)
        newData[i][j] = societyStatus.getStatus((String)columnNames[j], 
                                                (String)newData[i][0]);
    }
    return newData;
  }

  /** Called to set row names after drilling up on equipment.
    Get the current column 0 name, get its superior, and
    get that superior's subordinates.  Those are the new row names.
   */

  private Object[][] setSuperiorRowNames() {
    String equipmentName = dataModel.getColumnName(0);
    Equipment equipment = equipmentInfo.getEquipment(equipmentName);
    String superiorName = equipment.getSuperiorName();
    Equipment superiorEquipment = equipmentInfo.getEquipment(superiorName);
    Vector newRowNames = superiorEquipment.getSubordinateNames();
    int nRows = newRowNames.size();
    int nColumns = dataModel.getColumnCount();
    Object[][] newData = new Object[nRows][nColumns];
    for (int i = 0; i < nRows; i++) {
      newData[i][0] = newRowNames.elementAt(i);
      for (int j = 1; j < nColumns; j++)
        newData[i][j] = 
          societyStatus.getStatus(dataModel.getColumnName(j), 
                                  (String)newData[i][0]);
    }
    return newData;
  }

  /** Used when drilling up on both units and equipment.  Sets new rows
    and then sets new data based on a new set of columns.
    */

  private Object[][] setSuperiorRowNames(Object[] columnNames) {
    String equipmentName = dataModel.getColumnName(0);
    Equipment equipment = equipmentInfo.getEquipment(equipmentName);
    String superiorName = equipment.getSuperiorName();
    Equipment superiorEquipment = equipmentInfo.getEquipment(superiorName);
    Vector newRowNames = superiorEquipment.getSubordinateNames();
    int nRows = newRowNames.size();
    int nColumns = columnNames.length;
    Object[][] newData = new Object[nRows][nColumns];
    for (int i = 0; i < nRows; i++) {
      newData[i][0] = newRowNames.elementAt(i);
      for (int j = 1; j < nColumns; j++)
        newData[i][j] = societyStatus.getStatus((String)columnNames[j], 
                                                (String)newData[i][0]);
    }
    return newData;
  }

  /** Called to set new data after drilling up or down on unit.
    Takes the new column names as an argument.
   */

  private Object[][] setNewData(Object[] columnNames) {
    Vector oldData = dataModel.getDataVector();
    //    int nRows = table.getRowCount();
    int nRows = dataModel.getRowCount();
    int nColumns = columnNames.length;
    Object[][] newData = new Object[nRows][nColumns];
    for (int i = 0; i < nRows; i++) {
      newData[i][0] = ((Vector)oldData.elementAt(i)).elementAt(0);
      for (int j = 1; j < nColumns; j++)
        newData[i][j] = societyStatus.getStatus((String)columnNames[j],
                                                (String)newData[i][0]);
    }
    return newData;
  }

  /** Handle drill down or drill up commands.
    Caller is responsible for limiting the drill down/drill up to
    appropriate bounds (i.e. don't drill down past lowest level
    or drill up past highest level).
   */

  public void drill(String command, int selectedRow, int selectedColumn) {
    System.out.println("Command: " + command +
                       " Row: " + selectedRow +
                       " Column: " + selectedColumn);

    if (command.equals(DRILLDOWNUNIT)) {
      Object[] columnNames = setSubordinateColumnNames(selectedColumn);
      Object[][] newData = setNewData(columnNames);
      dataModel.setDataVector(newData, (Object[])columnNames);
    } else if (command.equals(DRILLUPUNIT)) {
      Object[] columnNames = setSuperiorColumnNames(selectedColumn);
      Object[][] newData = setNewData(columnNames);
      dataModel.setDataVector(newData, (Object[])columnNames);
    } else if (command.equals(DRILLDOWNEQUIPMENT)) {
      String newEquipmentName = (String)dataModel.getValueAt(selectedRow, 0);
      Object[][] newData = setSubordinateRowNames(selectedRow);
      int nColumns = dataModel.getColumnCount();
      String[] columnNames = new String[nColumns];
      for (int i = 0; i < nColumns; i++)
        columnNames[i] = dataModel.getColumnName(i);
      columnNames[0] = newEquipmentName;
      dataModel.setDataVector(newData, (Object[])columnNames);
    } else if (command.equals(DRILLUPEQUIPMENT)) {
      Equipment equipment = 
        equipmentInfo.getEquipment(dataModel.getColumnName(0));
      Object[][] newData = setSuperiorRowNames();
      int nColumns = dataModel.getColumnCount();
      String[] columnNames = new String[nColumns];
      for (int i = 0; i < nColumns; i++)
        columnNames[i] = dataModel.getColumnName(i);
      columnNames[0] = equipment.getSuperiorName();
      dataModel.setDataVector(newData, (Object[])columnNames);
    } else if (command.equals(DRILLUPBOTH)) {
      Object[] columnNames = setSuperiorColumnNames(selectedColumn);
      Object[][] newData = setSuperiorRowNames(columnNames);
      dataModel.setDataVector(newData, (Object[])columnNames);
    } else if (command.equals(DRILLDOWNBOTH)) {
      Object[] columnNames = setSubordinateColumnNames(selectedColumn);
      Object[][] newData = setSubordinateRowNames(columnNames, selectedRow);
      dataModel.setDataVector(newData, (Object[])columnNames);
    }
  }
}

class NonEditableTableModel extends DefaultTableModel {

  public boolean isCellEditable(int row, int column) {
    return false;
  }
}





