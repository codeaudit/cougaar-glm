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
/** Implementation of CargoFacilityPG.
 *  @see CargoFacilityPG
 *  @see NewCargoFacilityPG
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

public class CargoFacilityPGImpl extends java.beans.SimpleBeanInfo
  implements NewCargoFacilityPG, Cloneable
{
  public CargoFacilityPGImpl() {
  }

  // Slots

  private Area theCoveredStagingArea;
  public Area getCoveredStagingArea(){ return theCoveredStagingArea; }
  public void setCoveredStagingArea(Area covered_staging_area) {
    theCoveredStagingArea=covered_staging_area;
  }
  private Area theOpenStagingArea;
  public Area getOpenStagingArea(){ return theOpenStagingArea; }
  public void setOpenStagingArea(Area open_staging_area) {
    theOpenStagingArea=open_staging_area;
  }
  private long theThroughputContainersPerDay;
  public long getThroughputContainersPerDay(){ return theThroughputContainersPerDay; }
  public void setThroughputContainersPerDay(long throughput_containers_per_day) {
    theThroughputContainersPerDay=throughput_containers_per_day;
  }
  private long theThroughputPalletsPerDay;
  public long getThroughputPalletsPerDay(){ return theThroughputPalletsPerDay; }
  public void setThroughputPalletsPerDay(long throughput_pallets_per_day) {
    theThroughputPalletsPerDay=throughput_pallets_per_day;
  }


  public CargoFacilityPGImpl(CargoFacilityPG original) {
    theCoveredStagingArea = original.getCoveredStagingArea();
    theOpenStagingArea = original.getOpenStagingArea();
    theThroughputContainersPerDay = original.getThroughputContainersPerDay();
    theThroughputPalletsPerDay = original.getThroughputPalletsPerDay();
  }

  public boolean equals(Object other) {

    if (!(other instanceof CargoFacilityPG)) {
      return false;
    }

    CargoFacilityPG otherCargoFacilityPG = (CargoFacilityPG) other;

    if (getCoveredStagingArea() == null) {
      if (otherCargoFacilityPG.getCoveredStagingArea() != null) {
        return false;
      }
    } else if (!(getCoveredStagingArea().equals(otherCargoFacilityPG.getCoveredStagingArea()))) {
      return false;
    }

    if (getOpenStagingArea() == null) {
      if (otherCargoFacilityPG.getOpenStagingArea() != null) {
        return false;
      }
    } else if (!(getOpenStagingArea().equals(otherCargoFacilityPG.getOpenStagingArea()))) {
      return false;
    }

    if (!(getThroughputContainersPerDay() == otherCargoFacilityPG.getThroughputContainersPerDay())) {
      return false;
    }

    if (!(getThroughputPalletsPerDay() == otherCargoFacilityPG.getThroughputPalletsPerDay())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends CargoFacilityPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(CargoFacilityPG original) {
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


  private transient CargoFacilityPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new CargoFacilityPGImpl(CargoFacilityPGImpl.this);
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
      properties[0]= new PropertyDescriptor("covered_staging_area", CargoFacilityPG.class, "getCoveredStagingArea", null);
      properties[1]= new PropertyDescriptor("open_staging_area", CargoFacilityPG.class, "getOpenStagingArea", null);
      properties[2]= new PropertyDescriptor("throughput_containers_per_day", CargoFacilityPG.class, "getThroughputContainersPerDay", null);
      properties[3]= new PropertyDescriptor("throughput_pallets_per_day", CargoFacilityPG.class, "getThroughputPalletsPerDay", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(CargoFacilityPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements CargoFacilityPG, Cloneable, LockedPG
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
         return CargoFacilityPGImpl.this;
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
      return new CargoFacilityPGImpl(CargoFacilityPGImpl.this);
    }

    public boolean equals(Object object) { return CargoFacilityPGImpl.this.equals(object); }
    public Area getCoveredStagingArea() { return CargoFacilityPGImpl.this.getCoveredStagingArea(); }
    public Area getOpenStagingArea() { return CargoFacilityPGImpl.this.getOpenStagingArea(); }
    public long getThroughputContainersPerDay() { return CargoFacilityPGImpl.this.getThroughputContainersPerDay(); }
    public long getThroughputPalletsPerDay() { return CargoFacilityPGImpl.this.getThroughputPalletsPerDay(); }
  public final boolean hasDataQuality() { return CargoFacilityPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return CargoFacilityPGImpl.this.getDataQuality(); }
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
      return CargoFacilityPGImpl.class;
    }

  }

}
