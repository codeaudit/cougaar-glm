/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

/** Serializable object sent from PSP to stoplight display.
  */

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;

public class UIUnitStatus implements Serializable {
  String unitName;
  String equipmentName;
  String equipmentNomenclature;
  String status;
  public static final String RED = "Red";
  public static final String YELLOW = "Yellow";
  public static final String GREEN = "Green";
  public static final String GRAY = "Gray";

  public UIUnitStatus(String unitName, String equipmentName, 
                      String equipmentNomenclature, String status) {
    this.unitName = unitName;
    this.equipmentName = equipmentName;
    this.equipmentNomenclature = equipmentNomenclature;
    this.status = status;
  }

  public String getUnitName() {
    return unitName;
  }

  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  public String getEquipmentName() {
    return equipmentName;
  }

  public void setEquipmentName(String equipmentName) {
    this.equipmentName = equipmentName;
  }

  public String getEquipmentNomenclature() {
    return equipmentNomenclature;
  }

  public void setEquipmentNomenclature(String equipmentNomenclature) {
    this.equipmentNomenclature = equipmentNomenclature;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  
  
}
