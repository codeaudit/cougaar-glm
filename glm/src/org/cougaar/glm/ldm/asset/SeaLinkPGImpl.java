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
/** Implementation of SeaLinkPG.
 *  @see SeaLinkPG
 *  @see NewSeaLinkPG
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

public class SeaLinkPGImpl extends java.beans.SimpleBeanInfo
  implements NewSeaLinkPG, Cloneable
{
  public SeaLinkPGImpl() {
  }

  // Slots

  private Speed thePrevailingWindSpeed;
  public Speed getPrevailingWindSpeed(){ return thePrevailingWindSpeed; }
  public void setPrevailingWindSpeed(Speed prevailing_wind_speed) {
    thePrevailingWindSpeed=prevailing_wind_speed;
  }
  private double thePrevailingWindDirection;
  public double getPrevailingWindDirection(){ return thePrevailingWindDirection; }
  public void setPrevailingWindDirection(double prevailing_wind_direction) {
    thePrevailingWindDirection=prevailing_wind_direction;
  }
  private Speed thePrevailingCurrentSpeed;
  public Speed getPrevailingCurrentSpeed(){ return thePrevailingCurrentSpeed; }
  public void setPrevailingCurrentSpeed(Speed prevailing_current_speed) {
    thePrevailingCurrentSpeed=prevailing_current_speed;
  }
  private double thePrevailingCurrentDirection;
  public double getPrevailingCurrentDirection(){ return thePrevailingCurrentDirection; }
  public void setPrevailingCurrentDirection(double prevailing_current_direction) {
    thePrevailingCurrentDirection=prevailing_current_direction;
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


  public SeaLinkPGImpl(SeaLinkPG original) {
    thePrevailingWindSpeed = original.getPrevailingWindSpeed();
    thePrevailingWindDirection = original.getPrevailingWindDirection();
    thePrevailingCurrentSpeed = original.getPrevailingCurrentSpeed();
    thePrevailingCurrentDirection = original.getPrevailingCurrentDirection();
    theLinkName = original.getLinkName();
    theLinkLength = original.getLinkLength();
  }

  public boolean equals(Object other) {

    if (!(other instanceof SeaLinkPG)) {
      return false;
    }

    SeaLinkPG otherSeaLinkPG = (SeaLinkPG) other;

    if (getPrevailingWindSpeed() == null) {
      if (otherSeaLinkPG.getPrevailingWindSpeed() != null) {
        return false;
      }
    } else if (!(getPrevailingWindSpeed().equals(otherSeaLinkPG.getPrevailingWindSpeed()))) {
      return false;
    }

    if (!(getPrevailingWindDirection() == otherSeaLinkPG.getPrevailingWindDirection())) {
      return false;
    }

    if (getPrevailingCurrentSpeed() == null) {
      if (otherSeaLinkPG.getPrevailingCurrentSpeed() != null) {
        return false;
      }
    } else if (!(getPrevailingCurrentSpeed().equals(otherSeaLinkPG.getPrevailingCurrentSpeed()))) {
      return false;
    }

    if (!(getPrevailingCurrentDirection() == otherSeaLinkPG.getPrevailingCurrentDirection())) {
      return false;
    }

    if (getLinkName() == null) {
      if (otherSeaLinkPG.getLinkName() != null) {
        return false;
      }
    } else if (!(getLinkName().equals(otherSeaLinkPG.getLinkName()))) {
      return false;
    }

    if (getLinkLength() == null) {
      if (otherSeaLinkPG.getLinkLength() != null) {
        return false;
      }
    } else if (!(getLinkLength().equals(otherSeaLinkPG.getLinkLength()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends SeaLinkPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(SeaLinkPG original) {
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


  private transient SeaLinkPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new SeaLinkPGImpl(SeaLinkPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[6];
  static {
    try {
      properties[0]= new PropertyDescriptor("prevailing_wind_speed", SeaLinkPG.class, "getPrevailingWindSpeed", null);
      properties[1]= new PropertyDescriptor("prevailing_wind_direction", SeaLinkPG.class, "getPrevailingWindDirection", null);
      properties[2]= new PropertyDescriptor("prevailing_current_speed", SeaLinkPG.class, "getPrevailingCurrentSpeed", null);
      properties[3]= new PropertyDescriptor("prevailing_current_direction", SeaLinkPG.class, "getPrevailingCurrentDirection", null);
      properties[4]= new PropertyDescriptor("link_name", SeaLinkPG.class, "getLinkName", null);
      properties[5]= new PropertyDescriptor("link_length", SeaLinkPG.class, "getLinkLength", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(SeaLinkPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements SeaLinkPG, Cloneable, LockedPG
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
         return SeaLinkPGImpl.this;
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
      return new SeaLinkPGImpl(SeaLinkPGImpl.this);
    }

    public boolean equals(Object object) { return SeaLinkPGImpl.this.equals(object); }
    public Speed getPrevailingWindSpeed() { return SeaLinkPGImpl.this.getPrevailingWindSpeed(); }
    public double getPrevailingWindDirection() { return SeaLinkPGImpl.this.getPrevailingWindDirection(); }
    public Speed getPrevailingCurrentSpeed() { return SeaLinkPGImpl.this.getPrevailingCurrentSpeed(); }
    public double getPrevailingCurrentDirection() { return SeaLinkPGImpl.this.getPrevailingCurrentDirection(); }
    public String getLinkName() { return SeaLinkPGImpl.this.getLinkName(); }
    public Distance getLinkLength() { return SeaLinkPGImpl.this.getLinkLength(); }
  public final boolean hasDataQuality() { return SeaLinkPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return SeaLinkPGImpl.this.getDataQuality(); }
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
      return SeaLinkPGImpl.class;
    }

  }

}
