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
/** Implementation of AirVehiclePG.
 *  @see AirVehiclePG
 *  @see NewAirVehiclePG
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

public class AirVehiclePGImpl extends java.beans.SimpleBeanInfo
  implements NewAirVehiclePG, Cloneable
{
  public AirVehiclePGImpl() {
  }

  // Slots

  private Distance theMinimumRunwayLength;
  public Distance getMinimumRunwayLength(){ return theMinimumRunwayLength; }
  public void setMinimumRunwayLength(Distance minimum_runway_length) {
    theMinimumRunwayLength=minimum_runway_length;
  }
  private boolean theMidAirRefuelable;
  public boolean getMidAirRefuelable(){ return theMidAirRefuelable; }
  public void setMidAirRefuelable(boolean mid_air_refuelable) {
    theMidAirRefuelable=mid_air_refuelable;
  }
  private Duration theQuickTurnaroundTime;
  public Duration getQuickTurnaroundTime(){ return theQuickTurnaroundTime; }
  public void setQuickTurnaroundTime(Duration quick_turnaround_time) {
    theQuickTurnaroundTime=quick_turnaround_time;
  }
  private Duration theMeanTimeToRepair;
  public Duration getMeanTimeToRepair(){ return theMeanTimeToRepair; }
  public void setMeanTimeToRepair(Duration mean_time_to_repair) {
    theMeanTimeToRepair=mean_time_to_repair;
  }
  private long theMeanMissionLegsBetweenRepairs;
  public long getMeanMissionLegsBetweenRepairs(){ return theMeanMissionLegsBetweenRepairs; }
  public void setMeanMissionLegsBetweenRepairs(long mean_mission_legs_between_repairs) {
    theMeanMissionLegsBetweenRepairs=mean_mission_legs_between_repairs;
  }
  private Duration theMeanFlightTimeBetweenRepairs;
  public Duration getMeanFlightTimeBetweenRepairs(){ return theMeanFlightTimeBetweenRepairs; }
  public void setMeanFlightTimeBetweenRepairs(Duration mean_flight_time_between_repairs) {
    theMeanFlightTimeBetweenRepairs=mean_flight_time_between_repairs;
  }
  private String theVehicleType;
  public String getVehicleType(){ return theVehicleType; }
  public void setVehicleType(String vehicle_type) {
    theVehicleType=vehicle_type;
  }
  private long theCrewRequirements;
  public long getCrewRequirements(){ return theCrewRequirements; }
  public void setCrewRequirements(long crew_requirements) {
    theCrewRequirements=crew_requirements;
  }


  public AirVehiclePGImpl(AirVehiclePG original) {
    theMinimumRunwayLength = original.getMinimumRunwayLength();
    theMidAirRefuelable = original.getMidAirRefuelable();
    theQuickTurnaroundTime = original.getQuickTurnaroundTime();
    theMeanTimeToRepair = original.getMeanTimeToRepair();
    theMeanMissionLegsBetweenRepairs = original.getMeanMissionLegsBetweenRepairs();
    theMeanFlightTimeBetweenRepairs = original.getMeanFlightTimeBetweenRepairs();
    theVehicleType = original.getVehicleType();
    theCrewRequirements = original.getCrewRequirements();
  }

  public boolean equals(Object other) {

    if (!(other instanceof AirVehiclePG)) {
      return false;
    }

    AirVehiclePG otherAirVehiclePG = (AirVehiclePG) other;

    if (getMinimumRunwayLength() == null) {
      if (otherAirVehiclePG.getMinimumRunwayLength() != null) {
        return false;
      }
    } else if (!(getMinimumRunwayLength().equals(otherAirVehiclePG.getMinimumRunwayLength()))) {
      return false;
    }

    if (!(getMidAirRefuelable() == otherAirVehiclePG.getMidAirRefuelable())) {
      return false;
    }

    if (getQuickTurnaroundTime() == null) {
      if (otherAirVehiclePG.getQuickTurnaroundTime() != null) {
        return false;
      }
    } else if (!(getQuickTurnaroundTime().equals(otherAirVehiclePG.getQuickTurnaroundTime()))) {
      return false;
    }

    if (getMeanTimeToRepair() == null) {
      if (otherAirVehiclePG.getMeanTimeToRepair() != null) {
        return false;
      }
    } else if (!(getMeanTimeToRepair().equals(otherAirVehiclePG.getMeanTimeToRepair()))) {
      return false;
    }

    if (!(getMeanMissionLegsBetweenRepairs() == otherAirVehiclePG.getMeanMissionLegsBetweenRepairs())) {
      return false;
    }

    if (getMeanFlightTimeBetweenRepairs() == null) {
      if (otherAirVehiclePG.getMeanFlightTimeBetweenRepairs() != null) {
        return false;
      }
    } else if (!(getMeanFlightTimeBetweenRepairs().equals(otherAirVehiclePG.getMeanFlightTimeBetweenRepairs()))) {
      return false;
    }

    if (getVehicleType() == null) {
      if (otherAirVehiclePG.getVehicleType() != null) {
        return false;
      }
    } else if (!(getVehicleType().equals(otherAirVehiclePG.getVehicleType()))) {
      return false;
    }

    if (!(getCrewRequirements() == otherAirVehiclePG.getCrewRequirements())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends AirVehiclePGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(AirVehiclePG original) {
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


  private transient AirVehiclePG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new AirVehiclePGImpl(AirVehiclePGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[8];
  static {
    try {
      properties[0]= new PropertyDescriptor("minimum_runway_length", AirVehiclePG.class, "getMinimumRunwayLength", null);
      properties[1]= new PropertyDescriptor("mid_air_refuelable", AirVehiclePG.class, "getMidAirRefuelable", null);
      properties[2]= new PropertyDescriptor("quick_turnaround_time", AirVehiclePG.class, "getQuickTurnaroundTime", null);
      properties[3]= new PropertyDescriptor("mean_time_to_repair", AirVehiclePG.class, "getMeanTimeToRepair", null);
      properties[4]= new PropertyDescriptor("mean_mission_legs_between_repairs", AirVehiclePG.class, "getMeanMissionLegsBetweenRepairs", null);
      properties[5]= new PropertyDescriptor("mean_flight_time_between_repairs", AirVehiclePG.class, "getMeanFlightTimeBetweenRepairs", null);
      properties[6]= new PropertyDescriptor("vehicle_type", AirVehiclePG.class, "getVehicleType", null);
      properties[7]= new PropertyDescriptor("crew_requirements", AirVehiclePG.class, "getCrewRequirements", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(AirVehiclePG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements AirVehiclePG, Cloneable, LockedPG
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
         return AirVehiclePGImpl.this;
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
      return new AirVehiclePGImpl(AirVehiclePGImpl.this);
    }

    public boolean equals(Object object) { return AirVehiclePGImpl.this.equals(object); }
    public Distance getMinimumRunwayLength() { return AirVehiclePGImpl.this.getMinimumRunwayLength(); }
    public boolean getMidAirRefuelable() { return AirVehiclePGImpl.this.getMidAirRefuelable(); }
    public Duration getQuickTurnaroundTime() { return AirVehiclePGImpl.this.getQuickTurnaroundTime(); }
    public Duration getMeanTimeToRepair() { return AirVehiclePGImpl.this.getMeanTimeToRepair(); }
    public long getMeanMissionLegsBetweenRepairs() { return AirVehiclePGImpl.this.getMeanMissionLegsBetweenRepairs(); }
    public Duration getMeanFlightTimeBetweenRepairs() { return AirVehiclePGImpl.this.getMeanFlightTimeBetweenRepairs(); }
    public String getVehicleType() { return AirVehiclePGImpl.this.getVehicleType(); }
    public long getCrewRequirements() { return AirVehiclePGImpl.this.getCrewRequirements(); }
  public final boolean hasDataQuality() { return AirVehiclePGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return AirVehiclePGImpl.this.getDataQuality(); }
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
      return AirVehiclePGImpl.class;
    }

  }

}
