/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * Create stoplight table with drill down.
 */

public class StoplightModel extends AbstractTableModel {
  Vector equipmentNames;
  Vector unitNames;
  String equipmentType; // used as label of column 0
  Society society;
  EquipmentInfo equipmentInfo;
  SocietyStatus societyStatus;
  static final String DRILLDOWNEQUIPMENT = "DDE";
  static final String DRILLDOWNUNIT = "DDU";
  static final String DRILLDOWNBOTH = "DDB";
  static final String DRILLDOWNNONE = "DDN";
  static final String DRILLUPEQUIPMENT = "DUE";
  static final String DRILLUPUNIT = "DUU";
  static final String DRILLUPBOTH = "DUB";
  static final String DRILLUPNONE = "DUN";
  // added to change Class IX to Class IX by FSC on drill down
  static final String CLASSIX = "Class IX";
  static final String CLASSIX_BY_FSC = "Class IX by FSC";

  public StoplightModel(Society society, EquipmentInfo equipmentInfo,
                        SocietyStatus societyStatus) {
    this.society = society;
    this.equipmentInfo = equipmentInfo;
    this.societyStatus = societyStatus;

    Unit top = society.getUnit(society.TOPUNIT);
    unitNames = top.getSubordinateNames();

    Equipment equipment =
      equipmentInfo.getEquipment(equipmentInfo.TOPEQUIPMENT);
    equipmentNames = equipment.getSubordinateNames();
    adjustEquipmentNames();
    equipmentType = equipment.getName();
  }

  /** Set this data model to use the subordinates of the specified
    unit name and equipment name.
    */

  public synchronized void setUnitAndEquipment(String unitName, String equipmentName) {
    unitNames = society.getUnit(unitName).getSubordinateNames();
    if (equipmentName.equals(CLASSIX_BY_FSC))
        equipmentNames = 
            equipmentInfo.getEquipment(CLASSIX).getSubordinateNames();
    else
        equipmentNames = 
            equipmentInfo.getEquipment(equipmentName).getSubordinateNames();
    adjustEquipmentNames();
    equipmentType = equipmentName;
  }

  public synchronized int getRowCount() {
    return equipmentNames.size();
  }

  public synchronized int getColumnCount() {
    return unitNames.size() + 1;
  }

  public synchronized Object getValueAt(int row, int column) {
      String displayName;
      String equipmentName;

      String tmp = (String)equipmentNames.elementAt(row);
      if (tmp.startsWith("NSN")) {
          int i = tmp.indexOf(" ");
          displayName = tmp.substring(i+1); // nomenclature
          equipmentName = tmp.substring(0, i); // NSN
      } else {
          displayName = tmp;
          equipmentName = tmp;
      }
      if (column == 0)
          return displayName;
      if (column >= (unitNames.size()+1))
          return null;
      if (row >= equipmentNames.size())
          return null;
      return societyStatus.getStatus((String)unitNames.elementAt(column-1), 
                                     equipmentName);
  }

  public synchronized String getColumnName(int column) {
    if (column == 0)
      return equipmentType;
    return (String)unitNames.elementAt(column-1);
  }

    /**
       If equipment names are NSNs, then try to append nomenclature.
    */

    private void adjustEquipmentNames() {
      int n = equipmentNames.size();
      if (n > 0) {
          if (((String)equipmentNames.elementAt(0)).startsWith("NSN")) {
              Vector tmp = new Vector(n);
              for (int i = 0; i < n; i++) {
                  String s = (String)equipmentNames.elementAt(i);
                  tmp.addElement(s + " " + 
                                 equipmentInfo.nameToNomenclature(s));
              }
              equipmentNames = tmp;
          }
      }
    }

  public synchronized void modify(String command, int row, int column) {
    if (command.equals(DRILLDOWNUNIT)) {
      String unitName = (String)unitNames.elementAt(column-1);
      unitNames = society.getUnit(unitName).getSubordinateNames();
    } else if (command.equals(DRILLUPUNIT)) {
      String unitName = (String)unitNames.elementAt(0);
      String superiorName = 
        society.getSuperiorName(society.getSuperiorName(unitName));
      unitNames = society.getUnit(superiorName).getSubordinateNames();
    } else if (command.equals(DRILLDOWNEQUIPMENT)) {
      equipmentType = (String)equipmentNames.elementAt(row);
      equipmentNames = 
        equipmentInfo.getEquipment(equipmentType).getSubordinateNames();
      adjustEquipmentNames();
      if (equipmentType.equals(CLASSIX))
        equipmentType = CLASSIX_BY_FSC;
    } else if (command.equals(DRILLUPEQUIPMENT)) {
      if (equipmentType.equals(CLASSIX_BY_FSC))
        equipmentType = CLASSIX;
      equipmentType = equipmentInfo.getSuperiorName(equipmentType);
      equipmentNames = 
        equipmentInfo.getEquipment(equipmentType).getSubordinateNames();
      if (equipmentType.equals(CLASSIX))
        equipmentType = CLASSIX_BY_FSC;
    } else
      System.out.println("Command not supported: " + command);
  }

  public boolean isCellEditable(int row, int column) {
    return false;
  }

}
