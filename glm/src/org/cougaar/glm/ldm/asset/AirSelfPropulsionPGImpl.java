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
/** Implementation of AirSelfPropulsionPG.
 *  @see AirSelfPropulsionPG
 *  @see NewAirSelfPropulsionPG
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

public class AirSelfPropulsionPGImpl extends java.beans.SimpleBeanInfo
  implements NewAirSelfPropulsionPG, Cloneable
{
  public AirSelfPropulsionPGImpl() {
  }

  // Slots

  private Distance theMaximumAltitude;
  public Distance getMaximumAltitude(){ return theMaximumAltitude; }
  public void setMaximumAltitude(Distance maximum_altitude) {
    theMaximumAltitude=maximum_altitude;
  }
  private Distance theCruiseAltitude;
  public Distance getCruiseAltitude(){ return theCruiseAltitude; }
  public void setCruiseAltitude(Distance cruise_altitude) {
    theCruiseAltitude=cruise_altitude;
  }
  private FlowRate theFuelConsumptionRate;
  public FlowRate getFuelConsumptionRate(){ return theFuelConsumptionRate; }
  public void setFuelConsumptionRate(FlowRate fuel_consumption_rate) {
    theFuelConsumptionRate=fuel_consumption_rate;
  }
  private String theEngineType;
  public String getEngineType(){ return theEngineType; }
  public void setEngineType(String engine_type) {
    theEngineType=engine_type;
  }
  private String theFuelType;
  public String getFuelType(){ return theFuelType; }
  public void setFuelType(String fuel_type) {
    theFuelType=fuel_type;
  }
  private Speed theMaximumSpeed;
  public Speed getMaximumSpeed(){ return theMaximumSpeed; }
  public void setMaximumSpeed(Speed maximum_speed) {
    theMaximumSpeed=maximum_speed;
  }
  private Speed theCruiseSpeed;
  public Speed getCruiseSpeed(){ return theCruiseSpeed; }
  public void setCruiseSpeed(Speed cruise_speed) {
    theCruiseSpeed=cruise_speed;
  }
  private Distance theFullPayloadRange;
  public Distance getFullPayloadRange(){ return theFullPayloadRange; }
  public void setFullPayloadRange(Distance full_payload_range) {
    theFullPayloadRange=full_payload_range;
  }
  private Distance theEmptyPayloadRange;
  public Distance getEmptyPayloadRange(){ return theEmptyPayloadRange; }
  public void setEmptyPayloadRange(Distance empty_payload_range) {
    theEmptyPayloadRange=empty_payload_range;
  }


  public AirSelfPropulsionPGImpl(AirSelfPropulsionPG original) {
    theMaximumAltitude = original.getMaximumAltitude();
    theCruiseAltitude = original.getCruiseAltitude();
    theFuelConsumptionRate = original.getFuelConsumptionRate();
    theEngineType = original.getEngineType();
    theFuelType = original.getFuelType();
    theMaximumSpeed = original.getMaximumSpeed();
    theCruiseSpeed = original.getCruiseSpeed();
    theFullPayloadRange = original.getFullPayloadRange();
    theEmptyPayloadRange = original.getEmptyPayloadRange();
  }

  public boolean equals(Object other) {

    if (!(other instanceof AirSelfPropulsionPG)) {
      return false;
    }

    AirSelfPropulsionPG otherAirSelfPropulsionPG = (AirSelfPropulsionPG) other;

    if (getMaximumAltitude() == null) {
      if (otherAirSelfPropulsionPG.getMaximumAltitude() != null) {
        return false;
      }
    } else if (!(getMaximumAltitude().equals(otherAirSelfPropulsionPG.getMaximumAltitude()))) {
      return false;
    }

    if (getCruiseAltitude() == null) {
      if (otherAirSelfPropulsionPG.getCruiseAltitude() != null) {
        return false;
      }
    } else if (!(getCruiseAltitude().equals(otherAirSelfPropulsionPG.getCruiseAltitude()))) {
      return false;
    }

    if (getFuelConsumptionRate() == null) {
      if (otherAirSelfPropulsionPG.getFuelConsumptionRate() != null) {
        return false;
      }
    } else if (!(getFuelConsumptionRate().equals(otherAirSelfPropulsionPG.getFuelConsumptionRate()))) {
      return false;
    }

    if (getEngineType() == null) {
      if (otherAirSelfPropulsionPG.getEngineType() != null) {
        return false;
      }
    } else if (!(getEngineType().equals(otherAirSelfPropulsionPG.getEngineType()))) {
      return false;
    }

    if (getFuelType() == null) {
      if (otherAirSelfPropulsionPG.getFuelType() != null) {
        return false;
      }
    } else if (!(getFuelType().equals(otherAirSelfPropulsionPG.getFuelType()))) {
      return false;
    }

    if (getMaximumSpeed() == null) {
      if (otherAirSelfPropulsionPG.getMaximumSpeed() != null) {
        return false;
      }
    } else if (!(getMaximumSpeed().equals(otherAirSelfPropulsionPG.getMaximumSpeed()))) {
      return false;
    }

    if (getCruiseSpeed() == null) {
      if (otherAirSelfPropulsionPG.getCruiseSpeed() != null) {
        return false;
      }
    } else if (!(getCruiseSpeed().equals(otherAirSelfPropulsionPG.getCruiseSpeed()))) {
      return false;
    }

    if (getFullPayloadRange() == null) {
      if (otherAirSelfPropulsionPG.getFullPayloadRange() != null) {
        return false;
      }
    } else if (!(getFullPayloadRange().equals(otherAirSelfPropulsionPG.getFullPayloadRange()))) {
      return false;
    }

    if (getEmptyPayloadRange() == null) {
      if (otherAirSelfPropulsionPG.getEmptyPayloadRange() != null) {
        return false;
      }
    } else if (!(getEmptyPayloadRange().equals(otherAirSelfPropulsionPG.getEmptyPayloadRange()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends AirSelfPropulsionPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(AirSelfPropulsionPG original) {
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


  private transient AirSelfPropulsionPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new AirSelfPropulsionPGImpl(AirSelfPropulsionPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[9];
  static {
    try {
      properties[0]= new PropertyDescriptor("maximum_altitude", AirSelfPropulsionPG.class, "getMaximumAltitude", null);
      properties[1]= new PropertyDescriptor("cruise_altitude", AirSelfPropulsionPG.class, "getCruiseAltitude", null);
      properties[2]= new PropertyDescriptor("fuel_consumption_rate", AirSelfPropulsionPG.class, "getFuelConsumptionRate", null);
      properties[3]= new PropertyDescriptor("engine_type", AirSelfPropulsionPG.class, "getEngineType", null);
      properties[4]= new PropertyDescriptor("fuel_type", AirSelfPropulsionPG.class, "getFuelType", null);
      properties[5]= new PropertyDescriptor("maximum_speed", AirSelfPropulsionPG.class, "getMaximumSpeed", null);
      properties[6]= new PropertyDescriptor("cruise_speed", AirSelfPropulsionPG.class, "getCruiseSpeed", null);
      properties[7]= new PropertyDescriptor("full_payload_range", AirSelfPropulsionPG.class, "getFullPayloadRange", null);
      properties[8]= new PropertyDescriptor("empty_payload_range", AirSelfPropulsionPG.class, "getEmptyPayloadRange", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(AirSelfPropulsionPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements AirSelfPropulsionPG, Cloneable, LockedPG
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
         return AirSelfPropulsionPGImpl.this;
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
      return new AirSelfPropulsionPGImpl(AirSelfPropulsionPGImpl.this);
    }

    public boolean equals(Object object) { return AirSelfPropulsionPGImpl.this.equals(object); }
    public Distance getMaximumAltitude() { return AirSelfPropulsionPGImpl.this.getMaximumAltitude(); }
    public Distance getCruiseAltitude() { return AirSelfPropulsionPGImpl.this.getCruiseAltitude(); }
    public FlowRate getFuelConsumptionRate() { return AirSelfPropulsionPGImpl.this.getFuelConsumptionRate(); }
    public String getEngineType() { return AirSelfPropulsionPGImpl.this.getEngineType(); }
    public String getFuelType() { return AirSelfPropulsionPGImpl.this.getFuelType(); }
    public Speed getMaximumSpeed() { return AirSelfPropulsionPGImpl.this.getMaximumSpeed(); }
    public Speed getCruiseSpeed() { return AirSelfPropulsionPGImpl.this.getCruiseSpeed(); }
    public Distance getFullPayloadRange() { return AirSelfPropulsionPGImpl.this.getFullPayloadRange(); }
    public Distance getEmptyPayloadRange() { return AirSelfPropulsionPGImpl.this.getEmptyPayloadRange(); }
  public final boolean hasDataQuality() { return AirSelfPropulsionPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return AirSelfPropulsionPGImpl.this.getDataQuality(); }
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
      return AirSelfPropulsionPGImpl.class;
    }

  }

}
