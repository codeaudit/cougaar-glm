/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

/** Serializable object sent from Transport and Supply PSP 
  (PSP_EquipmentTransport)to stoplight display.
  */

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;

public class UILateStatus implements Serializable {
  String unitName;
  String equipmentName;
  String equipmentNomenclature;
  UIStoplightMetrics status;

  public UILateStatus(String unitName, String equipmentName,
                      String equipmentNomenclature) {
    this.unitName = unitName;
    this.equipmentName = equipmentName;
    this.equipmentNomenclature = equipmentNomenclature;
    status = new UIStoplightMetrics();
  }

  public void add(double quantity, int daysLate) {
    status.add(quantity, daysLate);
  }

  public String getUnitName() {
    return unitName;
  }

  public String getEquipmentName() {
    return equipmentName;
  }

  public String getEquipmentNomenclature() {
    return equipmentNomenclature;
  }

  public UIStoplightMetrics getStoplightMetrics() {
    return status;
  }
}

