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
/** Implementation of SeaConditionPG.
 *  @see SeaConditionPG
 *  @see NewSeaConditionPG
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

public class SeaConditionPGImpl extends java.beans.SimpleBeanInfo
  implements NewSeaConditionPG, Cloneable
{
  public SeaConditionPGImpl() {
  }

  // Slots

  private double theSeaState;
  public double getSeaState(){ return theSeaState; }
  public void setSeaState(double sea_state) {
    theSeaState=sea_state;
  }
  private Temperature theSurfaceTemperature;
  public Temperature getSurfaceTemperature(){ return theSurfaceTemperature; }
  public void setSurfaceTemperature(Temperature surface_temperature) {
    theSurfaceTemperature=surface_temperature;
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


  public SeaConditionPGImpl(SeaConditionPG original) {
    theSeaState = original.getSeaState();
    theSurfaceTemperature = original.getSurfaceTemperature();
    theWindSpeed = original.getWindSpeed();
    theTemperature = original.getTemperature();
    thePrecipitationRate = original.getPrecipitationRate();
    theWindHeading = original.getWindHeading();
    theVisibility = original.getVisibility();
    theBarometricPressure = original.getBarometricPressure();
  }

  public boolean equals(Object other) {

    if (!(other instanceof SeaConditionPG)) {
      return false;
    }

    SeaConditionPG otherSeaConditionPG = (SeaConditionPG) other;

    if (!(getSeaState() == otherSeaConditionPG.getSeaState())) {
      return false;
    }

    if (getSurfaceTemperature() == null) {
      if (otherSeaConditionPG.getSurfaceTemperature() != null) {
        return false;
      }
    } else if (!(getSurfaceTemperature().equals(otherSeaConditionPG.getSurfaceTemperature()))) {
      return false;
    }

    if (getWindSpeed() == null) {
      if (otherSeaConditionPG.getWindSpeed() != null) {
        return false;
      }
    } else if (!(getWindSpeed().equals(otherSeaConditionPG.getWindSpeed()))) {
      return false;
    }

    if (getTemperature() == null) {
      if (otherSeaConditionPG.getTemperature() != null) {
        return false;
      }
    } else if (!(getTemperature().equals(otherSeaConditionPG.getTemperature()))) {
      return false;
    }

    if (!(getPrecipitationRate() == otherSeaConditionPG.getPrecipitationRate())) {
      return false;
    }

    if (getWindHeading() == null) {
      if (otherSeaConditionPG.getWindHeading() != null) {
        return false;
      }
    } else if (!(getWindHeading().equals(otherSeaConditionPG.getWindHeading()))) {
      return false;
    }

    if (getVisibility() == null) {
      if (otherSeaConditionPG.getVisibility() != null) {
        return false;
      }
    } else if (!(getVisibility().equals(otherSeaConditionPG.getVisibility()))) {
      return false;
    }

    if (!(getBarometricPressure() == otherSeaConditionPG.getBarometricPressure())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends SeaConditionPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(SeaConditionPG original) {
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


  private transient SeaConditionPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new SeaConditionPGImpl(SeaConditionPGImpl.this);
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
      properties[0]= new PropertyDescriptor("sea_state", SeaConditionPG.class, "getSeaState", null);
      properties[1]= new PropertyDescriptor("surface_temperature", SeaConditionPG.class, "getSurfaceTemperature", null);
      properties[2]= new PropertyDescriptor("wind_speed", SeaConditionPG.class, "getWindSpeed", null);
      properties[3]= new PropertyDescriptor("temperature", SeaConditionPG.class, "getTemperature", null);
      properties[4]= new PropertyDescriptor("precipitation_rate", SeaConditionPG.class, "getPrecipitationRate", null);
      properties[5]= new PropertyDescriptor("wind_heading", SeaConditionPG.class, "getWindHeading", null);
      properties[6]= new PropertyDescriptor("visibility", SeaConditionPG.class, "getVisibility", null);
      properties[7]= new PropertyDescriptor("barometric_pressure", SeaConditionPG.class, "getBarometricPressure", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(SeaConditionPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements SeaConditionPG, Cloneable, LockedPG
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
         return SeaConditionPGImpl.this;
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
      return new SeaConditionPGImpl(SeaConditionPGImpl.this);
    }

    public boolean equals(Object object) { return SeaConditionPGImpl.this.equals(object); }
    public double getSeaState() { return SeaConditionPGImpl.this.getSeaState(); }
    public Temperature getSurfaceTemperature() { return SeaConditionPGImpl.this.getSurfaceTemperature(); }
    public Speed getWindSpeed() { return SeaConditionPGImpl.this.getWindSpeed(); }
    public Temperature getTemperature() { return SeaConditionPGImpl.this.getTemperature(); }
    public double getPrecipitationRate() { return SeaConditionPGImpl.this.getPrecipitationRate(); }
    public Heading getWindHeading() { return SeaConditionPGImpl.this.getWindHeading(); }
    public Distance getVisibility() { return SeaConditionPGImpl.this.getVisibility(); }
    public double getBarometricPressure() { return SeaConditionPGImpl.this.getBarometricPressure(); }
  public final boolean hasDataQuality() { return SeaConditionPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return SeaConditionPGImpl.this.getDataQuality(); }
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
      return SeaConditionPGImpl.class;
    }

  }

}
