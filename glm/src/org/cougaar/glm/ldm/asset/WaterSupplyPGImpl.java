/*
 * <copyright>
 *  
 *  Copyright 1997-2012 Raytheon BBN Technologies
 *  under partial sponsorship of the Defense Advanced Research Projects
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

/* @generated Wed Jun 06 08:28:58 EDT 2012 from alpprops.def - DO NOT HAND EDIT */
/** Implementation of WaterSupplyPG.
 *  @see WaterSupplyPG
 *  @see NewWaterSupplyPG
 **/

package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;

import  org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.policy.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.execution.common.InventoryReport;


import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class WaterSupplyPGImpl extends java.beans.SimpleBeanInfo
  implements NewWaterSupplyPG, Cloneable
{
  public WaterSupplyPGImpl() {
  }

  // Slots

  private Volume theStoreVolumePerDay;
  public Volume getStoreVolumePerDay(){ return theStoreVolumePerDay; }
  public void setStoreVolumePerDay(Volume store_volume_per_day) {
    theStoreVolumePerDay=store_volume_per_day;
  }
  private Volume theDistributeVolumePerDay;
  public Volume getDistributeVolumePerDay(){ return theDistributeVolumePerDay; }
  public void setDistributeVolumePerDay(Volume distribute_volume_per_day) {
    theDistributeVolumePerDay=distribute_volume_per_day;
  }
  private Volume theIssueVolumePerDay;
  public Volume getIssueVolumePerDay(){ return theIssueVolumePerDay; }
  public void setIssueVolumePerDay(Volume issue_volume_per_day) {
    theIssueVolumePerDay=issue_volume_per_day;
  }
  private Volume thePumpVolumePerMinute;
  public Volume getPumpVolumePerMinute(){ return thePumpVolumePerMinute; }
  public void setPumpVolumePerMinute(Volume pump_volume_per_minute) {
    thePumpVolumePerMinute=pump_volume_per_minute;
  }


  public WaterSupplyPGImpl(WaterSupplyPG original) {
    theStoreVolumePerDay = original.getStoreVolumePerDay();
    theDistributeVolumePerDay = original.getDistributeVolumePerDay();
    theIssueVolumePerDay = original.getIssueVolumePerDay();
    thePumpVolumePerMinute = original.getPumpVolumePerMinute();
  }

  public boolean equals(Object other) {

    if (!(other instanceof WaterSupplyPG)) {
      return false;
    }

    WaterSupplyPG otherWaterSupplyPG = (WaterSupplyPG) other;

    if (getStoreVolumePerDay() == null) {
      if (otherWaterSupplyPG.getStoreVolumePerDay() != null) {
        return false;
      }
    } else if (!(getStoreVolumePerDay().equals(otherWaterSupplyPG.getStoreVolumePerDay()))) {
      return false;
    }

    if (getDistributeVolumePerDay() == null) {
      if (otherWaterSupplyPG.getDistributeVolumePerDay() != null) {
        return false;
      }
    } else if (!(getDistributeVolumePerDay().equals(otherWaterSupplyPG.getDistributeVolumePerDay()))) {
      return false;
    }

    if (getIssueVolumePerDay() == null) {
      if (otherWaterSupplyPG.getIssueVolumePerDay() != null) {
        return false;
      }
    } else if (!(getIssueVolumePerDay().equals(otherWaterSupplyPG.getIssueVolumePerDay()))) {
      return false;
    }

    if (getPumpVolumePerMinute() == null) {
      if (otherWaterSupplyPG.getPumpVolumePerMinute() != null) {
        return false;
      }
    } else if (!(getPumpVolumePerMinute().equals(otherWaterSupplyPG.getPumpVolumePerMinute()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends WaterSupplyPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(WaterSupplyPG original) {
    super(original);
   }
   public Object clone() { return new DQ(this); }
   private transient org.cougaar.planning.ldm.dq.DataQuality _dq = null;
   public boolean hasDataQuality() { return (_dq!=null); }
   public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return _dq; }
   public void setDataQuality(org.cougaar.planning.ldm.dq.DataQuality dq) { _dq=dq; }
   private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.persist.PersistenceOutputStream) out.writeObject(_dq);
   }
   private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.persist.PersistenceInputStream) _dq=(org.cougaar.planning.ldm.dq.DataQuality)in.readObject();
   }
    
    private final static PropertyDescriptor properties[]=new PropertyDescriptor[1];
    static {
      try {
        properties[0]= new PropertyDescriptor("dataQuality", DQ.class, "getDataQuality", null);
      } catch (Exception e) { e.printStackTrace(); }
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor[] pds = super.properties;
      PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+properties.length];
      System.arraycopy(pds, 0, ps, 0, pds.length);
      System.arraycopy(properties, 0, ps, pds.length, properties.length);
      return ps;
    }
  }


  private transient WaterSupplyPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new WaterSupplyPGImpl(WaterSupplyPGImpl.this);
  }

  public PropertyGroup copy() {
    try {
      return (PropertyGroup) clone();
    } catch (CloneNotSupportedException cnse) { return null;}
  }

  public Class getPrimaryClass() {
    return primaryClass;
  }
  public String getAssetGetMethod() {
    return assetGetter;
  }
  public String getAssetSetMethod() {
    return assetSetter;
  }

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[4];
  static {
    try {
      properties[0]= new PropertyDescriptor("store_volume_per_day", WaterSupplyPG.class, "getStoreVolumePerDay", null);
      properties[1]= new PropertyDescriptor("distribute_volume_per_day", WaterSupplyPG.class, "getDistributeVolumePerDay", null);
      properties[2]= new PropertyDescriptor("issue_volume_per_day", WaterSupplyPG.class, "getIssueVolumePerDay", null);
      properties[3]= new PropertyDescriptor("pump_volume_per_minute", WaterSupplyPG.class, "getPumpVolumePerMinute", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(WaterSupplyPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements WaterSupplyPG, Cloneable, LockedPG
  {
    private transient Object theKey = null;
    _Locked(Object key) { 
      if (this.theKey == null) this.theKey = key;
    }  

    public _Locked() {}

    public PropertyGroup lock() { return this; }
    public PropertyGroup lock(Object o) { return this; }

    public NewPropertyGroup unlock(Object key) throws IllegalAccessException {
       if( theKey.equals(key) ) {
         return WaterSupplyPGImpl.this;
       } else {
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
       }
    }

    public PropertyGroup copy() {
      try {
        return (PropertyGroup) clone();
      } catch (CloneNotSupportedException cnse) { return null;}
    }


    public Object clone() throws CloneNotSupportedException {
      return new WaterSupplyPGImpl(WaterSupplyPGImpl.this);
    }

    public boolean equals(Object object) { return WaterSupplyPGImpl.this.equals(object); }
    public Volume getStoreVolumePerDay() { return WaterSupplyPGImpl.this.getStoreVolumePerDay(); }
    public Volume getDistributeVolumePerDay() { return WaterSupplyPGImpl.this.getDistributeVolumePerDay(); }
    public Volume getIssueVolumePerDay() { return WaterSupplyPGImpl.this.getIssueVolumePerDay(); }
    public Volume getPumpVolumePerMinute() { return WaterSupplyPGImpl.this.getPumpVolumePerMinute(); }
  public final boolean hasDataQuality() { return WaterSupplyPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return WaterSupplyPGImpl.this.getDataQuality(); }
    public Class getPrimaryClass() {
      return primaryClass;
    }
    public String getAssetGetMethod() {
      return assetGetter;
    }
    public String getAssetSetMethod() {
      return assetSetter;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
      return properties;
    }

    public Class getIntrospectionClass() {
      return WaterSupplyPGImpl.class;
    }

  }

}
