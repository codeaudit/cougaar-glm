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
/** Implementation of RailSelfPropulsionPG.
 *  @see RailSelfPropulsionPG
 *  @see NewRailSelfPropulsionPG
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

public class RailSelfPropulsionPGImpl extends java.beans.SimpleBeanInfo
  implements NewRailSelfPropulsionPG, Cloneable
{
  public RailSelfPropulsionPGImpl() {
  }

  // Slots

  private Volume theFuelConsumptionPerMile;
  public Volume getFuelConsumptionPerMile(){ return theFuelConsumptionPerMile; }
  public void setFuelConsumptionPerMile(Volume fuel_consumption_per_mile) {
    theFuelConsumptionPerMile=fuel_consumption_per_mile;
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


  public RailSelfPropulsionPGImpl(RailSelfPropulsionPG original) {
    theFuelConsumptionPerMile = original.getFuelConsumptionPerMile();
    theEngineType = original.getEngineType();
    theFuelType = original.getFuelType();
    theMaximumSpeed = original.getMaximumSpeed();
    theCruiseSpeed = original.getCruiseSpeed();
    theFullPayloadRange = original.getFullPayloadRange();
    theEmptyPayloadRange = original.getEmptyPayloadRange();
  }

  public boolean equals(Object other) {

    if (!(other instanceof RailSelfPropulsionPG)) {
      return false;
    }

    RailSelfPropulsionPG otherRailSelfPropulsionPG = (RailSelfPropulsionPG) other;

    if (getFuelConsumptionPerMile() == null) {
      if (otherRailSelfPropulsionPG.getFuelConsumptionPerMile() != null) {
        return false;
      }
    } else if (!(getFuelConsumptionPerMile().equals(otherRailSelfPropulsionPG.getFuelConsumptionPerMile()))) {
      return false;
    }

    if (getEngineType() == null) {
      if (otherRailSelfPropulsionPG.getEngineType() != null) {
        return false;
      }
    } else if (!(getEngineType().equals(otherRailSelfPropulsionPG.getEngineType()))) {
      return false;
    }

    if (getFuelType() == null) {
      if (otherRailSelfPropulsionPG.getFuelType() != null) {
        return false;
      }
    } else if (!(getFuelType().equals(otherRailSelfPropulsionPG.getFuelType()))) {
      return false;
    }

    if (getMaximumSpeed() == null) {
      if (otherRailSelfPropulsionPG.getMaximumSpeed() != null) {
        return false;
      }
    } else if (!(getMaximumSpeed().equals(otherRailSelfPropulsionPG.getMaximumSpeed()))) {
      return false;
    }

    if (getCruiseSpeed() == null) {
      if (otherRailSelfPropulsionPG.getCruiseSpeed() != null) {
        return false;
      }
    } else if (!(getCruiseSpeed().equals(otherRailSelfPropulsionPG.getCruiseSpeed()))) {
      return false;
    }

    if (getFullPayloadRange() == null) {
      if (otherRailSelfPropulsionPG.getFullPayloadRange() != null) {
        return false;
      }
    } else if (!(getFullPayloadRange().equals(otherRailSelfPropulsionPG.getFullPayloadRange()))) {
      return false;
    }

    if (getEmptyPayloadRange() == null) {
      if (otherRailSelfPropulsionPG.getEmptyPayloadRange() != null) {
        return false;
      }
    } else if (!(getEmptyPayloadRange().equals(otherRailSelfPropulsionPG.getEmptyPayloadRange()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends RailSelfPropulsionPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(RailSelfPropulsionPG original) {
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


  private transient RailSelfPropulsionPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new RailSelfPropulsionPGImpl(RailSelfPropulsionPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[7];
  static {
    try {
      properties[0]= new PropertyDescriptor("fuel_consumption_per_mile", RailSelfPropulsionPG.class, "getFuelConsumptionPerMile", null);
      properties[1]= new PropertyDescriptor("engine_type", RailSelfPropulsionPG.class, "getEngineType", null);
      properties[2]= new PropertyDescriptor("fuel_type", RailSelfPropulsionPG.class, "getFuelType", null);
      properties[3]= new PropertyDescriptor("maximum_speed", RailSelfPropulsionPG.class, "getMaximumSpeed", null);
      properties[4]= new PropertyDescriptor("cruise_speed", RailSelfPropulsionPG.class, "getCruiseSpeed", null);
      properties[5]= new PropertyDescriptor("full_payload_range", RailSelfPropulsionPG.class, "getFullPayloadRange", null);
      properties[6]= new PropertyDescriptor("empty_payload_range", RailSelfPropulsionPG.class, "getEmptyPayloadRange", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(RailSelfPropulsionPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements RailSelfPropulsionPG, Cloneable, LockedPG
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
         return RailSelfPropulsionPGImpl.this;
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
      return new RailSelfPropulsionPGImpl(RailSelfPropulsionPGImpl.this);
    }

    public boolean equals(Object object) { return RailSelfPropulsionPGImpl.this.equals(object); }
    public Volume getFuelConsumptionPerMile() { return RailSelfPropulsionPGImpl.this.getFuelConsumptionPerMile(); }
    public String getEngineType() { return RailSelfPropulsionPGImpl.this.getEngineType(); }
    public String getFuelType() { return RailSelfPropulsionPGImpl.this.getFuelType(); }
    public Speed getMaximumSpeed() { return RailSelfPropulsionPGImpl.this.getMaximumSpeed(); }
    public Speed getCruiseSpeed() { return RailSelfPropulsionPGImpl.this.getCruiseSpeed(); }
    public Distance getFullPayloadRange() { return RailSelfPropulsionPGImpl.this.getFullPayloadRange(); }
    public Distance getEmptyPayloadRange() { return RailSelfPropulsionPGImpl.this.getEmptyPayloadRange(); }
  public final boolean hasDataQuality() { return RailSelfPropulsionPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return RailSelfPropulsionPGImpl.this.getDataQuality(); }
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
      return RailSelfPropulsionPGImpl.class;
    }

  }

}
