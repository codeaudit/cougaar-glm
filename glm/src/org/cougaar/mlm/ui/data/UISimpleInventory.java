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

package org.cougaar.mlm.ui.data;

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
  long alpNowTime;

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
	//System.out.println("Schedule " + name + " has " + schedule.size() + " elements");
      schedules.addElement(new UISimpleNamedSchedule(name, schedule));
    } else {
        System.out.println("Schedule " + name + " is null");
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

  public void setAlpNow(long theAlpNowTime) {
      alpNowTime = theAlpNowTime;
  }

  public void setAlpNow(Date AlpNow) {
      setAlpNow(AlpNow.getTime());
  }

  public long getAlpNowTime() {
      return alpNowTime;
  }

  public String toString() {
    return
      "UISimpleInventory {"+
      "\n  assetName: "+assetName+
      "\n  scheduleType: "+scheduleType+
      "\n  unitType: "+unitType+
      "\n  schedules: "+schedules+
      "\n  provider: "+provider+
      "\n  baseCDayTime: "+baseCDayTime+
      "\n}";
  }
}
