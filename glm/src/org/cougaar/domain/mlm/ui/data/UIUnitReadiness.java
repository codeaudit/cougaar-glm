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
import java.util.Date;

public class UIUnitReadiness implements Serializable {
  String unitName;
  String equipmentName;
  boolean success;
  double preferredQuantity;
  double actualQuantity;
  Date preferredStartDate;
  Date actualStartDate;
  Date preferredEndDate;
  Date actualEndDate;

  public UIUnitReadiness(String unitName, String equipmentName, 
                         boolean success,
                         double preferredQuantity, double actualQuantity,
                         Date preferredStartDate, Date actualStartDate,
                         Date preferredEndDate, Date actualEndDate) {
    this.unitName = unitName;
    this.equipmentName = equipmentName;
    this.success = success;
    this.preferredQuantity = preferredQuantity;
    this.actualQuantity = actualQuantity;
    this.preferredStartDate = preferredStartDate;
    this.actualStartDate = actualStartDate;
    this.preferredEndDate = preferredEndDate;
    this.actualEndDate = actualEndDate;
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

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public double getPreferredQuantity() {
    return preferredQuantity;
  }
  
  public void setPreferredQuantity(double preferredQuantity) {
    this.preferredQuantity = preferredQuantity;
  }

  public double getActualQuantity() {
    return actualQuantity;
  }
  
  public void setActualQuantity(double actualQuantity) {
    this.actualQuantity = actualQuantity;
  }

  public Date getPreferredStartDate() {
    return preferredStartDate;
  }

  public void setPreferredStartDate(Date preferredStartDate) {
    this.preferredStartDate = preferredStartDate;
  }

  public Date getActualStartDate() {
    return actualStartDate;
  }

  public void setActualStartDate(Date actualStartDate) {
    this.actualStartDate = actualStartDate;
  }

  public Date getPreferredEndDate() {
    return preferredEndDate;
  }

  public void setPreferredEndDate(Date preferredEndDate) {
    this.preferredEndDate = preferredEndDate;
  }

  public Date getActualEndDate() {
    return actualEndDate;
  }

  public void setActualEndDate(Date actualEndDate) {
    this.actualEndDate = actualEndDate;
  }

}

