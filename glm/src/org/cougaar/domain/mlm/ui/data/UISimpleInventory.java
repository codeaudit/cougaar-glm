/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;
import java.util.Date;

public class UISimpleInventory implements Serializable {
  String assetName;
  String scheduleType;
  String unitType;
  Vector schedules = new Vector();
  boolean provider;
  long baseCDayTime;

  public void setAssetName(String assetName) {
    this.assetName = assetName;
  }

  public String getAssetName() {
    return assetName;
  }

  public void setScheduleType(String scheduleType) {
    this.scheduleType = scheduleType;
  }

  public String getScheduleType() {
    return scheduleType;
  }

  public void setUnitType(String unitType) {
    this.unitType = unitType;
  }

  public String getUnitType() {
    return unitType;
  }

  public void addNamedSchedule(String name, Vector schedule) {
    if (schedule != null) {
//        System.out.println("Schedule " + name + " has " + schedule.size() + " elements");
      schedules.addElement(new UISimpleNamedSchedule(name, schedule));
    } else {
//        System.out.println("Schedule " + name + " is null");
    }
  }

  public UISimpleNamedSchedule getNamedSchedule(String name) {
    for (int i = 0; i < schedules.size(); i++) {
      UISimpleNamedSchedule s = (UISimpleNamedSchedule)schedules.elementAt(i);
      if (s.getName().equals(name))
        return s;
    }
    return null;
  }

  public Vector getSchedules() {
    return schedules;
  }

  public void setProvider(boolean provider) {
    this.provider = provider;
  }

  public boolean isProvider() {
    return provider;
  }

  public void setBaseCDay(long theBaseCDayTime) {
      baseCDayTime = theBaseCDayTime;
  }

  public void setBaseCDay(Date baseCDay) {
      baseCDayTime = baseCDay.getTime();
  }

  public long getBaseCDayTime() {
      return baseCDayTime;
  }

}
