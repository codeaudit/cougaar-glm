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
/** Implementation of FuelSupplyPG.
 *  @see FuelSupplyPG
 *  @see NewFuelSupplyPG
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

public class FuelSupplyPGImpl extends java.beans.SimpleBeanInfo
  implements NewFuelSupplyPG, Cloneable
{
  public FuelSupplyPGImpl() {
  }

  // Slots

  private String theFuelTypes;
  public String getFuelTypes(){ return theFuelTypes; }
  public void setFuelTypes(String fuel_types) {
    theFuelTypes=fuel_types;
  }
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


  public FuelSupplyPGImpl(FuelSupplyPG original) {
    theFuelTypes = original.getFuelTypes();
    theStoreVolumePerDay = original.getStoreVolumePerDay();
    theDistributeVolumePerDay = original.getDistributeVolumePerDay();
    theIssueVolumePerDay = original.getIssueVolumePerDay();
    thePumpVolumePerMinute = original.getPumpVolumePerMinute();
  }

  public boolean equals(Object other) {

    if (!(other instanceof FuelSupplyPG)) {
      return false;
    }

    FuelSupplyPG otherFuelSupplyPG = (FuelSupplyPG) other;

    if (getFuelTypes() == null) {
      if (otherFuelSupplyPG.getFuelTypes() != null) {
        return false;
      }
    } else if (!(getFuelTypes().equals(otherFuelSupplyPG.getFuelTypes()))) {
      return false;
    }

    if (getStoreVolumePerDay() == null) {
      if (otherFuelSupplyPG.getStoreVolumePerDay() != null) {
        return false;
      }
    } else if (!(getStoreVolumePerDay().equals(otherFuelSupplyPG.getStoreVolumePerDay()))) {
      return false;
    }

    if (getDistributeVolumePerDay() == null) {
      if (otherFuelSupplyPG.getDistributeVolumePerDay() != null) {
        return false;
      }
    } else if (!(getDistributeVolumePerDay().equals(otherFuelSupplyPG.getDistributeVolumePerDay()))) {
      return false;
    }

    if (getIssueVolumePerDay() == null) {
      if (otherFuelSupplyPG.getIssueVolumePerDay() != null) {
        return false;
      }
    } else if (!(getIssueVolumePerDay().equals(otherFuelSupplyPG.getIssueVolumePerDay()))) {
      return false;
    }

    if (getPumpVolumePerMinute() == null) {
      if (otherFuelSupplyPG.getPumpVolumePerMinute() != null) {
        return false;
      }
    } else if (!(getPumpVolumePerMinute().equals(otherFuelSupplyPG.getPumpVolumePerMinute()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends FuelSupplyPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(FuelSupplyPG original) {
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


  private transient FuelSupplyPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new FuelSupplyPGImpl(FuelSupplyPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[5];
  static {
    try {
      properties[0]= new PropertyDescriptor("fuel_types", FuelSupplyPG.class, "getFuelTypes", null);
      properties[1]= new PropertyDescriptor("store_volume_per_day", FuelSupplyPG.class, "getStoreVolumePerDay", null);
      properties[2]= new PropertyDescriptor("distribute_volume_per_day", FuelSupplyPG.class, "getDistributeVolumePerDay", null);
      properties[3]= new PropertyDescriptor("issue_volume_per_day", FuelSupplyPG.class, "getIssueVolumePerDay", null);
      properties[4]= new PropertyDescriptor("pump_volume_per_minute", FuelSupplyPG.class, "getPumpVolumePerMinute", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(FuelSupplyPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements FuelSupplyPG, Cloneable, LockedPG
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
         return FuelSupplyPGImpl.this;
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
      return new FuelSupplyPGImpl(FuelSupplyPGImpl.this);
    }

    public boolean equals(Object object) { return FuelSupplyPGImpl.this.equals(object); }
    public String getFuelTypes() { return FuelSupplyPGImpl.this.getFuelTypes(); }
    public Volume getStoreVolumePerDay() { return FuelSupplyPGImpl.this.getStoreVolumePerDay(); }
    public Volume getDistributeVolumePerDay() { return FuelSupplyPGImpl.this.getDistributeVolumePerDay(); }
    public Volume getIssueVolumePerDay() { return FuelSupplyPGImpl.this.getIssueVolumePerDay(); }
    public Volume getPumpVolumePerMinute() { return FuelSupplyPGImpl.this.getPumpVolumePerMinute(); }
  public final boolean hasDataQuality() { return FuelSupplyPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return FuelSupplyPGImpl.this.getDataQuality(); }
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
      return FuelSupplyPGImpl.class;
    }

  }

}
