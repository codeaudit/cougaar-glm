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
/** Implementation of LandConditionPG.
 *  @see LandConditionPG
 *  @see NewLandConditionPG
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

public class LandConditionPGImpl extends java.beans.SimpleBeanInfo
  implements NewLandConditionPG, Cloneable
{
  public LandConditionPGImpl() {
  }

  // Slots

  private double theSoilMoisture;
  public double getSoilMoisture(){ return theSoilMoisture; }
  public void setSoilMoisture(double soil_moisture) {
    theSoilMoisture=soil_moisture;
  }
  private Distance theSnowDepth;
  public Distance getSnowDepth(){ return theSnowDepth; }
  public void setSnowDepth(Distance snow_depth) {
    theSnowDepth=snow_depth;
  }
  private Speed theWindSpeed;
  public Speed getWindSpeed(){ return theWindSpeed; }
  public void setWindSpeed(Speed wind_speed) {
    theWindSpeed=wind_speed;
  }
  private Temperature theTemperature;
  public Temperature getTemperature(){ return theTemperature; }
  public void setTemperature(Temperature temperature) {
    theTemperature=temperature;
  }
  private double thePrecipitationRate;
  public double getPrecipitationRate(){ return thePrecipitationRate; }
  public void setPrecipitationRate(double precipitation_rate) {
    thePrecipitationRate=precipitation_rate;
  }
  private Heading theWindHeading;
  public Heading getWindHeading(){ return theWindHeading; }
  public void setWindHeading(Heading wind_heading) {
    theWindHeading=wind_heading;
  }
  private Distance theVisibility;
  public Distance getVisibility(){ return theVisibility; }
  public void setVisibility(Distance visibility) {
    theVisibility=visibility;
  }
  private double theBarometricPressure;
  public double getBarometricPressure(){ return theBarometricPressure; }
  public void setBarometricPressure(double barometric_pressure) {
    theBarometricPressure=barometric_pressure;
  }


  public LandConditionPGImpl(LandConditionPG original) {
    theSoilMoisture = original.getSoilMoisture();
    theSnowDepth = original.getSnowDepth();
    theWindSpeed = original.getWindSpeed();
    theTemperature = original.getTemperature();
    thePrecipitationRate = original.getPrecipitationRate();
    theWindHeading = original.getWindHeading();
    theVisibility = original.getVisibility();
    theBarometricPressure = original.getBarometricPressure();
  }

  public boolean equals(Object other) {

    if (!(other instanceof LandConditionPG)) {
      return false;
    }

    LandConditionPG otherLandConditionPG = (LandConditionPG) other;

    if (!(getSoilMoisture() == otherLandConditionPG.getSoilMoisture())) {
      return false;
    }

    if (getSnowDepth() == null) {
      if (otherLandConditionPG.getSnowDepth() != null) {
        return false;
      }
    } else if (!(getSnowDepth().equals(otherLandConditionPG.getSnowDepth()))) {
      return false;
    }

    if (getWindSpeed() == null) {
      if (otherLandConditionPG.getWindSpeed() != null) {
        return false;
      }
    } else if (!(getWindSpeed().equals(otherLandConditionPG.getWindSpeed()))) {
      return false;
    }

    if (getTemperature() == null) {
      if (otherLandConditionPG.getTemperature() != null) {
        return false;
      }
    } else if (!(getTemperature().equals(otherLandConditionPG.getTemperature()))) {
      return false;
    }

    if (!(getPrecipitationRate() == otherLandConditionPG.getPrecipitationRate())) {
      return false;
    }

    if (getWindHeading() == null) {
      if (otherLandConditionPG.getWindHeading() != null) {
        return false;
      }
    } else if (!(getWindHeading().equals(otherLandConditionPG.getWindHeading()))) {
      return false;
    }

    if (getVisibility() == null) {
      if (otherLandConditionPG.getVisibility() != null) {
        return false;
      }
    } else if (!(getVisibility().equals(otherLandConditionPG.getVisibility()))) {
      return false;
    }

    if (!(getBarometricPressure() == otherLandConditionPG.getBarometricPressure())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends LandConditionPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(LandConditionPG original) {
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


  private transient LandConditionPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new LandConditionPGImpl(LandConditionPGImpl.this);
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
      properties[0]= new PropertyDescriptor("soil_moisture", LandConditionPG.class, "getSoilMoisture", null);
      properties[1]= new PropertyDescriptor("snow_depth", LandConditionPG.class, "getSnowDepth", null);
      properties[2]= new PropertyDescriptor("wind_speed", LandConditionPG.class, "getWindSpeed", null);
      properties[3]= new PropertyDescriptor("temperature", LandConditionPG.class, "getTemperature", null);
      properties[4]= new PropertyDescriptor("precipitation_rate", LandConditionPG.class, "getPrecipitationRate", null);
      properties[5]= new PropertyDescriptor("wind_heading", LandConditionPG.class, "getWindHeading", null);
      properties[6]= new PropertyDescriptor("visibility", LandConditionPG.class, "getVisibility", null);
      properties[7]= new PropertyDescriptor("barometric_pressure", LandConditionPG.class, "getBarometricPressure", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(LandConditionPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements LandConditionPG, Cloneable, LockedPG
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
         return LandConditionPGImpl.this;
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
      return new LandConditionPGImpl(LandConditionPGImpl.this);
    }

    public boolean equals(Object object) { return LandConditionPGImpl.this.equals(object); }
    public double getSoilMoisture() { return LandConditionPGImpl.this.getSoilMoisture(); }
    public Distance getSnowDepth() { return LandConditionPGImpl.this.getSnowDepth(); }
    public Speed getWindSpeed() { return LandConditionPGImpl.this.getWindSpeed(); }
    public Temperature getTemperature() { return LandConditionPGImpl.this.getTemperature(); }
    public double getPrecipitationRate() { return LandConditionPGImpl.this.getPrecipitationRate(); }
    public Heading getWindHeading() { return LandConditionPGImpl.this.getWindHeading(); }
    public Distance getVisibility() { return LandConditionPGImpl.this.getVisibility(); }
    public double getBarometricPressure() { return LandConditionPGImpl.this.getBarometricPressure(); }
  public final boolean hasDataQuality() { return LandConditionPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return LandConditionPGImpl.this.getDataQuality(); }
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
      return LandConditionPGImpl.class;
    }

  }

}
