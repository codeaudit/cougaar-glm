/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

/** Serializable object sent from Transportation Equipment PSP to
  equipment stoplight display.
  */

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;

public class UISupplyStatus implements Serializable {
  Vector unitStatuses; // vector of UIUnitStatus

  public UISupplyStatus() {
    unitStatuses = new Vector();
  }

  public UISupplyStatus(Vector unitStatuses) {
    this.unitStatuses = unitStatuses;
  }

  /** If no status exists for this unit and equipment, then
    set the status for this unit and equipment, but if we already
    have a status for this unit and equipment then override it
    with the worst status.
    */

  public void addUnitStatus(UIUnitStatus newUnitStatus) {
    String existingStatus = getUnitStatus(newUnitStatus.getUnitName(),
                                          newUnitStatus.getEquipmentName());
    if (existingStatus == null) {
      unitStatuses.addElement(newUnitStatus);
      return;
    }
    String newStatus = newUnitStatus.getStatus();
    if (existingStatus.equals(UIUnitStatus.RED)) 
      return;
    if (newStatus.equals(UIUnitStatus.RED) || newStatus.equals(UIUnitStatus.YELLOW))
      setUnitStatus(newUnitStatus);
  }

  public Vector getUnitStatuses() {
    return unitStatuses;
  }

  public void setUnitStatus(UIUnitStatus fromUnitStatus) {
    String unitName = fromUnitStatus.getUnitName();
    String equipmentName = fromUnitStatus.getEquipmentName();
    for (int i = 0; i < unitStatuses.size(); i++) {
      UIUnitStatus status = (UIUnitStatus)unitStatuses.elementAt(i);
      if ((status.getUnitName().equals(unitName)) &
          (status.getEquipmentName().equals(equipmentName))) {
        ((UIUnitStatus)unitStatuses.elementAt(i)).setStatus(fromUnitStatus.getStatus());
        break;
      }
    }
  }

  public String getUnitStatus(String unitName, String equipmentName) {
    for (int i = 0; i < unitStatuses.size(); i++) {
      UIUnitStatus status = (UIUnitStatus)unitStatuses.elementAt(i);
      if ((status.getUnitName().equals(unitName)) &&
          (status.getEquipmentName().equals(equipmentName))) {
        return status.getStatus();
      }
    }
    return null;
  }

}
