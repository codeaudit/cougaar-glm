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
/** Implementation of AirLinkPG.
 *  @see AirLinkPG
 *  @see NewAirLinkPG
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

public class AirLinkPGImpl extends java.beans.SimpleBeanInfo
  implements NewAirLinkPG, Cloneable
{
  public AirLinkPGImpl() {
  }

  // Slots

  private Speed thePrevailingWindSpeed;
  public Speed getPrevailingWindSpeed(){ return thePrevailingWindSpeed; }
  public void setPrevailingWindSpeed(Speed prevailing_wind_speed) {
    thePrevailingWindSpeed=prevailing_wind_speed;
  }
  private Heading thePrevailingWindDirection;
  public Heading getPrevailingWindDirection(){ return thePrevailingWindDirection; }
  public void setPrevailingWindDirection(Heading prevailing_wind_direction) {
    thePrevailingWindDirection=prevailing_wind_direction;
  }
  private String theLinkName;
  public String getLinkName(){ return theLinkName; }
  public void setLinkName(String link_name) {
    theLinkName=link_name;
  }
  private Distance theLinkLength;
  public Distance getLinkLength(){ return theLinkLength; }
  public void setLinkLength(Distance link_length) {
    theLinkLength=link_length;
  }


  public AirLinkPGImpl(AirLinkPG original) {
    thePrevailingWindSpeed = original.getPrevailingWindSpeed();
    thePrevailingWindDirection = original.getPrevailingWindDirection();
    theLinkName = original.getLinkName();
    theLinkLength = original.getLinkLength();
  }

  public boolean equals(Object other) {

    if (!(other instanceof AirLinkPG)) {
      return false;
    }

    AirLinkPG otherAirLinkPG = (AirLinkPG) other;

    if (getPrevailingWindSpeed() == null) {
      if (otherAirLinkPG.getPrevailingWindSpeed() != null) {
        return false;
      }
    } else if (!(getPrevailingWindSpeed().equals(otherAirLinkPG.getPrevailingWindSpeed()))) {
      return false;
    }

    if (getPrevailingWindDirection() == null) {
      if (otherAirLinkPG.getPrevailingWindDirection() != null) {
        return false;
      }
    } else if (!(getPrevailingWindDirection().equals(otherAirLinkPG.getPrevailingWindDirection()))) {
      return false;
    }

    if (getLinkName() == null) {
      if (otherAirLinkPG.getLinkName() != null) {
        return false;
      }
    } else if (!(getLinkName().equals(otherAirLinkPG.getLinkName()))) {
      return false;
    }

    if (getLinkLength() == null) {
      if (otherAirLinkPG.getLinkLength() != null) {
        return false;
      }
    } else if (!(getLinkLength().equals(otherAirLinkPG.getLinkLength()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends AirLinkPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(AirLinkPG original) {
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


  private transient AirLinkPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new AirLinkPGImpl(AirLinkPGImpl.this);
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
      properties[0]= new PropertyDescriptor("prevailing_wind_speed", AirLinkPG.class, "getPrevailingWindSpeed", null);
      properties[1]= new PropertyDescriptor("prevailing_wind_direction", AirLinkPG.class, "getPrevailingWindDirection", null);
      properties[2]= new PropertyDescriptor("link_name", AirLinkPG.class, "getLinkName", null);
      properties[3]= new PropertyDescriptor("link_length", AirLinkPG.class, "getLinkLength", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(AirLinkPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements AirLinkPG, Cloneable, LockedPG
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
         return AirLinkPGImpl.this;
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
      return new AirLinkPGImpl(AirLinkPGImpl.this);
    }

    public boolean equals(Object object) { return AirLinkPGImpl.this.equals(object); }
    public Speed getPrevailingWindSpeed() { return AirLinkPGImpl.this.getPrevailingWindSpeed(); }
    public Heading getPrevailingWindDirection() { return AirLinkPGImpl.this.getPrevailingWindDirection(); }
    public String getLinkName() { return AirLinkPGImpl.this.getLinkName(); }
    public Distance getLinkLength() { return AirLinkPGImpl.this.getLinkLength(); }
  public final boolean hasDataQuality() { return AirLinkPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return AirLinkPGImpl.this.getDataQuality(); }
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
      return AirLinkPGImpl.class;
    }

  }

}
