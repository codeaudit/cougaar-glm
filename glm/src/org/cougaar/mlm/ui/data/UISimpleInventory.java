/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.ui.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

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
