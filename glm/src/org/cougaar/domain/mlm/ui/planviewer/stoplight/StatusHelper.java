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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.cougaar.domain.mlm.ui.data.*;

public class StatusHelper {
  Society society = null;
  EquipmentInfo equipmentInfo = null;
  SocietyStatus societyStatus = null;

  public StatusHelper(Hashtable clusterData, DefaultListModel criteriaModel) {
    UISupplyStatus status = getStatusFromMetrics(clusterData, criteriaModel);
    propagateStatus(status);
  }

  /** Status is computed by "aggregating" the status from the lowest units
    and equipment up the hierarchy.
    To handle the "headquarters" units, if a unit is not a battalion,
    then append "-HQ" to the unit name,
    before looking it up in the list of units.
    */

  private void propagateStatus(UISupplyStatus status) {
    society = new Society();
    equipmentInfo = new EquipmentInfo();
    societyStatus = new SocietyStatus();
    Vector statuses = status.getUnitStatuses();
    for (int i = 0; i < statuses.size(); i++) {
      UIUnitStatus unitStatus = (UIUnitStatus)statuses.elementAt(i);
      String unitName = unitStatus.getUnitName();
      String equipmentId = unitStatus.getEquipmentName();
      String equipmentNomenclature = unitStatus.getEquipmentNomenclature();
      String uestatus = unitStatus.getStatus();
      unitName = unitName.trim();
      if (unitName.startsWith("UIC/"))
        unitName = unitName.substring(4);
      if (society.isDefaultBattalion(unitName))
        society.addUnit(unitName, 0);
      else {
        unitName = unitName + "-HQ";
        if (society.isDefaultBattalion(unitName))
          society.addUnit(unitName, 0);
        else {
          System.out.println("Unit is not in default society: " +
                             unitName);
          continue;
        }
      }
      String equipmentName = equipmentInfo.itemIdToNomenclature(equipmentId);
      if (equipmentName == null)
        equipmentName = equipmentId; // if no mapping, just use id
      if (equipmentInfo.isDefaultItem(equipmentName))
        equipmentInfo.addEquipment(equipmentName, equipmentNomenclature, 0);
      else {
          System.out.println("Equipment is not in default society: " +
                           equipmentName);
        continue;
      }
      societyStatus.setStatus(unitName, equipmentName, uestatus);
    }
    
    // for debugging
    //    System.out.println("Society");
    //    society.printSociety();
    //    System.out.println("Equipment");
    //    equipmentInfo.printEquipmentInfo();
    //    System.out.println("======================");

    // first, set the status of all NSNs for all units
    // up the equipment heirarchy
    Vector bottomEquipment = equipmentInfo.getEquipmentAtLevel(0);
    for (int j = 0; j < bottomEquipment.size(); j++) {
      Equipment equipment = (Equipment)bottomEquipment.elementAt(j);
      String equipmentName = equipment.getName();
      setEquipmentStatus(equipmentName);
    }
    // next, determine status of each class of equipment
    // throughout the equipment hierarchy and
    // set the status of it for all units
    for (int i = 0; i < 10; i++) {
      Vector unitsAtLevel = society.getUnitsAtLevel(i);
      if (unitsAtLevel == null)
        break;
      for (int k = 0; k < unitsAtLevel.size(); k++) {
        Unit unit = (Unit)unitsAtLevel.elementAt(k);
        for (int j = 1; j < 5; j++) {
          Vector equipmentAtLevel = equipmentInfo.getEquipmentAtLevel(j);
          if (equipmentAtLevel == null)
            break;
          for (int m = 0; m < equipmentAtLevel.size(); m++) {
            Equipment equipment = (Equipment)equipmentAtLevel.elementAt(m);
            setStatusOfEquipmentClass(unit, equipment);
          }
        }
      }
    }
    //    societyStatus.printSocietyStatus();
  }

  /** Sets status of units vs. equipment class by inspecting status of 
    the equipment class subordinates.
   */

  private void setStatusOfEquipmentClass(Unit unit, Equipment equipment) {
    Vector subordinateEquipmentNames = equipment.getSubordinateNames();
    String defaultStatus = UIUnitStatus.GRAY;
    for (int j = 0; j < subordinateEquipmentNames.size(); j++) {
      String subordinateEquipmentName = 
        (String)subordinateEquipmentNames.elementAt(j);
      String status = 
        societyStatus.getStatus(unit.getName(), subordinateEquipmentName);
      if (status.equals(UIUnitStatus.RED)) {
        defaultStatus = UIUnitStatus.RED;
        break;
      }
      if (status.equals(UIUnitStatus.YELLOW) && 
          (defaultStatus.equals(UIUnitStatus.GREEN) || defaultStatus.equals(UIUnitStatus.GRAY)))
        defaultStatus = UIUnitStatus.YELLOW;
      if (status.equals(UIUnitStatus.GREEN) && defaultStatus.equals(UIUnitStatus.GRAY))
        defaultStatus = UIUnitStatus.GREEN;
    }
    societyStatus.setStatus(unit.getName(), equipment.getName(),
                            defaultStatus);
  }

  /** Set the status of a superior unit and an equipment item
    from the status for a subordinate unit and equipment item.
    For example, given the status of the 3-69-ARBN for NSN/5234,
    this determines the status of the 1BDE-3ID for NSN/5234, 
    and the status of the 3ID for NSN/5234.
    */

  private void setEquipmentStatus(String equipmentName) {
    Vector bottomUnits = society.getUnitsAtLevel(0);
    for (int i = 0; i < bottomUnits.size(); i++) {
      Unit unit = (Unit)bottomUnits.elementAt(i);
      String unitName = unit.getName();
      String superiorName = society.getSuperiorName(unitName);
      while (superiorName != null) {
        setStatusForSuperior(superiorName, equipmentName, 
                             societyStatus.getStatus(unitName, equipmentName));
        superiorName = society.getSuperiorName(superiorName);
      }
    }
  }

  /** Called by setEquipmentStatus to propagate status of
    lowest level of equipment from lowest units to top of society.
    As the status from different units for the same equipment is added, 
    red overrides yellow overrides green.
    */

  private void setStatusForSuperior(String unitName, String equipmentName, 
                                    String status) {
    String s = societyStatus.getStatus(unitName, equipmentName);
    if (s == null) {
      societyStatus.setStatus(unitName, equipmentName, status);
    } else if (s.equals(UIUnitStatus.GRAY)) {
      societyStatus.setStatus(unitName, equipmentName, status);
    } else if (status.equals(UIUnitStatus.YELLOW) && s.equals(UIUnitStatus.GREEN)) {
      societyStatus.setStatus(unitName, equipmentName, status);
    } else if (status.equals(UIUnitStatus.RED)) {
      societyStatus.setStatus(unitName, equipmentName, status);
    }
  }

  public Society getSociety() {
    return society;
  }

  public EquipmentInfo getEquipmentInfo() {
    return equipmentInfo;
  }

  public SocietyStatus getSocietyStatus() {
    return societyStatus;
  }

  /** Set unit and equipment names and set all statuses green.
   */

  private UISupplyStatus getStatus(Hashtable clusterData) {
    UISupplyStatus status = new UISupplyStatus();
    Enumeration keys = clusterData.keys();
    while (keys.hasMoreElements()) {
      UILateStatus uil = (UILateStatus)clusterData.get(keys.nextElement());
      String unitName = uil.getUnitName();
      String equipmentName = uil.getEquipmentName();
      String equipmentNomenclature = uil.getEquipmentNomenclature();
      status.addUnitStatus(new UIUnitStatus(unitName, equipmentName,
                                            equipmentNomenclature,
                                            UIUnitStatus.GREEN));
    }
    return status;
  }

  private UISupplyStatus getStatusFromMetrics(Hashtable clusterData,
                                              DefaultListModel criteria) {
    UISupplyStatus status = new UISupplyStatus();
    Enumeration keys = clusterData.keys();
    while (keys.hasMoreElements()) {
      UILateStatus uil = (UILateStatus)clusterData.get(keys.nextElement());
      String unitName = uil.getUnitName();
      String equipmentName = uil.getEquipmentName();
      String equipmentNomenclature = uil.getEquipmentNomenclature();
      status.addUnitStatus(new UIUnitStatus(unitName, equipmentName,
                                            equipmentNomenclature,
                                            UIUnitStatus.GREEN));
      UIStoplightMetrics metrics = uil.getStoplightMetrics();
      for (int i = 0; i < criteria.size(); i++) {
        CriteriaParameters cp = (CriteriaParameters)criteria.getElementAt(i);
        if (metrics.applyTest(cp.getPerCentOperation(), 
                              cp.getPerCent(),
                              cp.getDaysLateOperation(), 
                              cp.getDaysLate()))
          status.addUnitStatus(new UIUnitStatus(unitName, equipmentName, 
                                                equipmentNomenclature,
                                                cp.getColor()));
      }
    }
    return status;
  }
}


  
