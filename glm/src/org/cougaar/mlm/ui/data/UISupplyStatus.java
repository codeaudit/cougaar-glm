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

/** Serializable object sent from Transportation Equipment PSP to
  equipment stoplight display.
  */

package org.cougaar.mlm.ui.data;

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
