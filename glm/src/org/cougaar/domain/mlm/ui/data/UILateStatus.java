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

