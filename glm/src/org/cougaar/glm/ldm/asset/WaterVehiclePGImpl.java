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
/** Implementation of WaterVehiclePG.
 *  @see WaterVehiclePG
 *  @see NewWaterVehiclePG
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

public class WaterVehiclePGImpl extends java.beans.SimpleBeanInfo
  implements NewWaterVehiclePG, Cloneable
{
  public WaterVehiclePGImpl() {
  }

  // Slots

  private Distance theMinimumDraft;
  public Distance getMinimumDraft(){ return theMinimumDraft; }
  public void setMinimumDraft(Distance minimum_draft) {
    theMinimumDraft=minimum_draft;
  }
  private Distance theMaximumDraft;
  public Distance getMaximumDraft(){ return theMaximumDraft; }
  public void setMaximumDraft(Distance maximum_draft) {
    theMaximumDraft=maximum_draft;
  }
  private Distance theLength;
  public Distance getLength(){ return theLength; }
  public void setLength(Distance length) {
    theLength=length;
  }
  private Distance theBeam;
  public Distance getBeam(){ return theBeam; }
  public void setBeam(Distance beam) {
    theBeam=beam;
  }
  private String theBerthRequirements;
  public String getBerthRequirements(){ return theBerthRequirements; }
  public void setBerthRequirements(String berth_requirements) {
    theBerthRequirements=berth_requirements;
  }
  private Duration theMeanEngineTimeBetweenRepairs;
  public Duration getMeanEngineTimeBetweenRepairs(){ return theMeanEngineTimeBetweenRepairs; }
  public void setMeanEngineTimeBetweenRepairs(Duration mean_engine_time_between_repairs) {
    theMeanEngineTimeBetweenRepairs=mean_engine_time_between_repairs;
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


  public WaterVehiclePGImpl(WaterVehiclePG original) {
    theMinimumDraft = original.getMinimumDraft();
    theMaximumDraft = original.getMaximumDraft();
    theLength = original.getLength();
    theBeam = original.getBeam();
    theBerthRequirements = original.getBerthRequirements();
    theMeanEngineTimeBetweenRepairs = original.getMeanEngineTimeBetweenRepairs();
    theVehicleType = original.getVehicleType();
    theCrewRequirements = original.getCrewRequirements();
  }

  public boolean equals(Object other) {

    if (!(other instanceof WaterVehiclePG)) {
      return false;
    }

    WaterVehiclePG otherWaterVehiclePG = (WaterVehiclePG) other;

    if (getMinimumDraft() == null) {
      if (otherWaterVehiclePG.getMinimumDraft() != null) {
        return false;
      }
    } else if (!(getMinimumDraft().equals(otherWaterVehiclePG.getMinimumDraft()))) {
      return false;
    }

    if (getMaximumDraft() == null) {
      if (otherWaterVehiclePG.getMaximumDraft() != null) {
        return false;
      }
    } else if (!(getMaximumDraft().equals(otherWaterVehiclePG.getMaximumDraft()))) {
      return false;
    }

    if (getLength() == null) {
      if (otherWaterVehiclePG.getLength() != null) {
        return false;
      }
    } else if (!(getLength().equals(otherWaterVehiclePG.getLength()))) {
      return false;
    }

    if (getBeam() == null) {
      if (otherWaterVehiclePG.getBeam() != null) {
        return false;
      }
    } else if (!(getBeam().equals(otherWaterVehiclePG.getBeam()))) {
      return false;
    }

    if (getBerthRequirements() == null) {
      if (otherWaterVehiclePG.getBerthRequirements() != null) {
        return false;
      }
    } else if (!(getBerthRequirements().equals(otherWaterVehiclePG.getBerthRequirements()))) {
      return false;
    }

    if (getMeanEngineTimeBetweenRepairs() == null) {
      if (otherWaterVehiclePG.getMeanEngineTimeBetweenRepairs() != null) {
        return false;
      }
    } else if (!(getMeanEngineTimeBetweenRepairs().equals(otherWaterVehiclePG.getMeanEngineTimeBetweenRepairs()))) {
      return false;
    }

    if (getVehicleType() == null) {
      if (otherWaterVehiclePG.getVehicleType() != null) {
        return false;
      }
    } else if (!(getVehicleType().equals(otherWaterVehiclePG.getVehicleType()))) {
      return false;
    }

    if (!(getCrewRequirements() == otherWaterVehiclePG.getCrewRequirements())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends WaterVehiclePGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(WaterVehiclePG original) {
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


  private transient WaterVehiclePG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new WaterVehiclePGImpl(WaterVehiclePGImpl.this);
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
      properties[0]= new PropertyDescriptor("minimum_draft", WaterVehiclePG.class, "getMinimumDraft", null);
      properties[1]= new PropertyDescriptor("maximum_draft", WaterVehiclePG.class, "getMaximumDraft", null);
      properties[2]= new PropertyDescriptor("length", WaterVehiclePG.class, "getLength", null);
      properties[3]= new PropertyDescriptor("beam", WaterVehiclePG.class, "getBeam", null);
      properties[4]= new PropertyDescriptor("berth_requirements", WaterVehiclePG.class, "getBerthRequirements", null);
      properties[5]= new PropertyDescriptor("mean_engine_time_between_repairs", WaterVehiclePG.class, "getMeanEngineTimeBetweenRepairs", null);
      properties[6]= new PropertyDescriptor("vehicle_type", WaterVehiclePG.class, "getVehicleType", null);
      properties[7]= new PropertyDescriptor("crew_requirements", WaterVehiclePG.class, "getCrewRequirements", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(WaterVehiclePG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements WaterVehiclePG, Cloneable, LockedPG
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
         return WaterVehiclePGImpl.this;
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
      return new WaterVehiclePGImpl(WaterVehiclePGImpl.this);
    }

    public boolean equals(Object object) { return WaterVehiclePGImpl.this.equals(object); }
    public Distance getMinimumDraft() { return WaterVehiclePGImpl.this.getMinimumDraft(); }
    public Distance getMaximumDraft() { return WaterVehiclePGImpl.this.getMaximumDraft(); }
    public Distance getLength() { return WaterVehiclePGImpl.this.getLength(); }
    public Distance getBeam() { return WaterVehiclePGImpl.this.getBeam(); }
    public String getBerthRequirements() { return WaterVehiclePGImpl.this.getBerthRequirements(); }
    public Duration getMeanEngineTimeBetweenRepairs() { return WaterVehiclePGImpl.this.getMeanEngineTimeBetweenRepairs(); }
    public String getVehicleType() { return WaterVehiclePGImpl.this.getVehicleType(); }
    public long getCrewRequirements() { return WaterVehiclePGImpl.this.getCrewRequirements(); }
  public final boolean hasDataQuality() { return WaterVehiclePGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return WaterVehiclePGImpl.this.getDataQuality(); }
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
      return WaterVehiclePGImpl.class;
    }

  }

}
