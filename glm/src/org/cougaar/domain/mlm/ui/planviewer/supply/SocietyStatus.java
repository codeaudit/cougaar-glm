/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.supply;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.domain.mlm.ui.data.UIUnitStatus; // defines colors

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
