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
 
package org.cougaar.mlm.ui.planviewer.stoplight;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.mlm.ui.data.UIUnitStatus; // defines colors

public class SocietyStatus {
  Vector statuses;

  public SocietyStatus() {
    statuses = new Vector();
  }

  public String getStatus(String unitName, String assetName) {
    for (int i = 0; i < statuses.size(); i++) {
      UnitStatus unitStatus = (UnitStatus)statuses.elementAt(i);
      if (unitStatus.name.equals(unitName)) {
        Vector assetStatuses = unitStatus.assetStatus;
        for (int j = 0; j < assetStatuses.size(); j++) {
          AssetStatus assetStatus = (AssetStatus)assetStatuses.elementAt(j);
          if (assetStatus.name.equals(assetName))
            return assetStatus.status;
        }
      }
    }
    return UIUnitStatus.GRAY;
  }

  public void setStatus(String unitName, String assetName, 
                        String statusColor) {
    for (int i = 0; i < statuses.size(); i++) {
      UnitStatus unitStatus = (UnitStatus)statuses.elementAt(i);
      if (unitStatus.name.equals(unitName)) {
        Vector assetStatuses = unitStatus.assetStatus;
        for (int j = 0; j < assetStatuses.size(); j++) {
          AssetStatus assetStatus = (AssetStatus)assetStatuses.elementAt(j);
          if (assetStatus.name.equals(assetName)) {
            assetStatus.status = statusColor;
            return;
          }
        }
        unitStatus.addAssetStatus(assetName, statusColor);
        return;
      }
    }
    UnitStatus unitStatus = new UnitStatus(unitName);
    unitStatus.addAssetStatus(assetName, statusColor);
    statuses.addElement(unitStatus);
  }

  public void printSocietyStatus() {
    for (int i = 0; i < statuses.size(); i++) {
      UnitStatus unitStatus = (UnitStatus)(statuses.elementAt(i));
      Vector assetStatuses = unitStatus.assetStatus;
      for (int j = 0; j < assetStatuses.size(); j++) {
        AssetStatus assetStatus = (AssetStatus)(assetStatuses.elementAt(j));
        System.out.println("Unit: " + unitStatus.name +
                           " equipment: " + assetStatus.name + 
                           " status: " + assetStatus.status);
      }
    }
  }

  public static void main(String[] args) {
    SocietyStatus societyStatus = new SocietyStatus();
    Society society = new Society();
    society.createDefaultSociety();
    EquipmentInfo equipmentInfo = new EquipmentInfo();
    Enumeration enumUnits = society.getAllUnits();
    while (enumUnits.hasMoreElements()) {
      Unit unit = (Unit)enumUnits.nextElement();
      String unitName = unit.getName();
      Enumeration enumEquipment = equipmentInfo.getAllEquipment();
      while (enumEquipment.hasMoreElements()) {
        Equipment equipment = (Equipment)enumEquipment.nextElement();
        String equipmentName = equipment.getName();
        societyStatus.setStatus(unitName, equipmentName, "green");
      }
    }

    enumUnits = society.getAllUnits();
    while (enumUnits.hasMoreElements()) {
      Unit unit = (Unit)enumUnits.nextElement();
      String unitName = unit.getName();
      Enumeration enumEquipment = equipmentInfo.getAllEquipment();
      while (enumEquipment.hasMoreElements()) {
        Equipment equipment = (Equipment)enumEquipment.nextElement();
        String equipmentName = equipment.getName();
        System.out.println("Unit: " + unitName +
                           " equipment: " + equipmentName + 
                           " status: " + 
                           societyStatus.getStatus(unitName, equipmentName));
      }
    }

  }
}

class UnitStatus {
  String name;
  Vector assetStatus; 

  public UnitStatus(String name) {
    this.name = name;
    assetStatus = new Vector();
  }

  public void addAssetStatus(String assetName, String statusColor) {
    assetStatus.addElement(new AssetStatus(assetName, statusColor));
  }
}

class AssetStatus {
  String name;
  String status;

  public AssetStatus(String name, String status) {
    this.name = name;
    this.status = status;
  }
}
