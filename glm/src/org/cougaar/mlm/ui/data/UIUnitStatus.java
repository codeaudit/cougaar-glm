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

/** Serializable object sent from PSP to stoplight display.
  */

package org.cougaar.mlm.ui.data;

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