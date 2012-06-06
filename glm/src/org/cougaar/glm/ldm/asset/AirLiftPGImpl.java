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
/** Implementation of AirLiftPG.
 *  @see AirLiftPG
 *  @see NewAirLiftPG
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

public class AirLiftPGImpl extends java.beans.SimpleBeanInfo
  implements NewAirLiftPG, Cloneable
{
  public AirLiftPGImpl() {
  }

  // Slots

  private Volume theMaximumBulkVolume;
  public Volume getMaximumBulkVolume(){ return theMaximumBulkVolume; }
  public void setMaximumBulkVolume(Volume maximum_bulk_volume) {
    theMaximumBulkVolume=maximum_bulk_volume;
  }
  private Mass theMaximumBulkWeight;
  public Mass getMaximumBulkWeight(){ return theMaximumBulkWeight; }
  public void setMaximumBulkWeight(Mass maximum_bulk_weight) {
    theMaximumBulkWeight=maximum_bulk_weight;
  }
  private Volume theMaximumOversizeVolume;
  public Volume getMaximumOversizeVolume(){ return theMaximumOversizeVolume; }
  public void setMaximumOversizeVolume(Volume maximum_oversize_volume) {
    theMaximumOversizeVolume=maximum_oversize_volume;
  }
  private Mass theMaximumOversizeWeight;
  public Mass getMaximumOversizeWeight(){ return theMaximumOversizeWeight; }
  public void setMaximumOversizeWeight(Mass maximum_oversize_weight) {
    theMaximumOversizeWeight=maximum_oversize_weight;
  }
  private Volume theMaximumOutsizeVolume;
  public Volume getMaximumOutsizeVolume(){ return theMaximumOutsizeVolume; }
  public void setMaximumOutsizeVolume(Volume maximum_outsize_volume) {
    theMaximumOutsizeVolume=maximum_outsize_volume;
  }
  private Mass theMaximumOutsizeWeight;
  public Mass getMaximumOutsizeWeight(){ return theMaximumOutsizeWeight; }
  public void setMaximumOutsizeWeight(Mass maximum_outsize_weight) {
    theMaximumOutsizeWeight=maximum_outsize_weight;
  }
  private int theMaximumPallets;
  public int getMaximumPallets(){ return theMaximumPallets; }
  public void setMaximumPallets(int maximum_pallets) {
    theMaximumPallets=maximum_pallets;
  }
  private Distance theMaximumLength;
  public Distance getMaximumLength(){ return theMaximumLength; }
  public void setMaximumLength(Distance maximum_length) {
    theMaximumLength=maximum_length;
  }
  private Distance theMaximumWidth;
  public Distance getMaximumWidth(){ return theMaximumWidth; }
  public void setMaximumWidth(Distance maximum_width) {
    theMaximumWidth=maximum_width;
  }
  private Distance theMaximumHeight;
  public Distance getMaximumHeight(){ return theMaximumHeight; }
  public void setMaximumHeight(Distance maximum_height) {
    theMaximumHeight=maximum_height;
  }
  private String theCargoRestrictions;
  public String getCargoRestrictions(){ return theCargoRestrictions; }
  public void setCargoRestrictions(String cargo_restrictions) {
    theCargoRestrictions=cargo_restrictions;
  }
  private String thePermittedCargoCategoryCodes;
  public String getPermittedCargoCategoryCodes(){ return thePermittedCargoCategoryCodes; }
  public void setPermittedCargoCategoryCodes(String permitted_cargo_category_codes) {
    thePermittedCargoCategoryCodes=permitted_cargo_category_codes;
  }


  public AirLiftPGImpl(AirLiftPG original) {
    theMaximumBulkVolume = original.getMaximumBulkVolume();
    theMaximumBulkWeight = original.getMaximumBulkWeight();
    theMaximumOversizeVolume = original.getMaximumOversizeVolume();
    theMaximumOversizeWeight = original.getMaximumOversizeWeight();
    theMaximumOutsizeVolume = original.getMaximumOutsizeVolume();
    theMaximumOutsizeWeight = original.getMaximumOutsizeWeight();
    theMaximumPallets = original.getMaximumPallets();
    theMaximumLength = original.getMaximumLength();
    theMaximumWidth = original.getMaximumWidth();
    theMaximumHeight = original.getMaximumHeight();
    theCargoRestrictions = original.getCargoRestrictions();
    thePermittedCargoCategoryCodes = original.getPermittedCargoCategoryCodes();
  }

  public boolean equals(Object other) {

    if (!(other instanceof AirLiftPG)) {
      return false;
    }

    AirLiftPG otherAirLiftPG = (AirLiftPG) other;

    if (getMaximumBulkVolume() == null) {
      if (otherAirLiftPG.getMaximumBulkVolume() != null) {
        return false;
      }
    } else if (!(getMaximumBulkVolume().equals(otherAirLiftPG.getMaximumBulkVolume()))) {
      return false;
    }

    if (getMaximumBulkWeight() == null) {
      if (otherAirLiftPG.getMaximumBulkWeight() != null) {
        return false;
      }
    } else if (!(getMaximumBulkWeight().equals(otherAirLiftPG.getMaximumBulkWeight()))) {
      return false;
    }

    if (getMaximumOversizeVolume() == null) {
      if (otherAirLiftPG.getMaximumOversizeVolume() != null) {
        return false;
      }
    } else if (!(getMaximumOversizeVolume().equals(otherAirLiftPG.getMaximumOversizeVolume()))) {
      return false;
    }

    if (getMaximumOversizeWeight() == null) {
      if (otherAirLiftPG.getMaximumOversizeWeight() != null) {
        return false;
      }
    } else if (!(getMaximumOversizeWeight().equals(otherAirLiftPG.getMaximumOversizeWeight()))) {
      return false;
    }

    if (getMaximumOutsizeVolume() == null) {
      if (otherAirLiftPG.getMaximumOutsizeVolume() != null) {
        return false;
      }
    } else if (!(getMaximumOutsizeVolume().equals(otherAirLiftPG.getMaximumOutsizeVolume()))) {
      return false;
    }

    if (getMaximumOutsizeWeight() == null) {
      if (otherAirLiftPG.getMaximumOutsizeWeight() != null) {
        return false;
      }
    } else if (!(getMaximumOutsizeWeight().equals(otherAirLiftPG.getMaximumOutsizeWeight()))) {
      return false;
    }

    if (!(getMaximumPallets() == otherAirLiftPG.getMaximumPallets())) {
      return false;
    }

    if (getMaximumLength() == null) {
      if (otherAirLiftPG.getMaximumLength() != null) {
        return false;
      }
    } else if (!(getMaximumLength().equals(otherAirLiftPG.getMaximumLength()))) {
      return false;
    }

    if (getMaximumWidth() == null) {
      if (otherAirLiftPG.getMaximumWidth() != null) {
        return false;
      }
    } else if (!(getMaximumWidth().equals(otherAirLiftPG.getMaximumWidth()))) {
      return false;
    }

    if (getMaximumHeight() == null) {
      if (otherAirLiftPG.getMaximumHeight() != null) {
        return false;
      }
    } else if (!(getMaximumHeight().equals(otherAirLiftPG.getMaximumHeight()))) {
      return false;
    }

    if (getCargoRestrictions() == null) {
      if (otherAirLiftPG.getCargoRestrictions() != null) {
        return false;
      }
    } else if (!(getCargoRestrictions().equals(otherAirLiftPG.getCargoRestrictions()))) {
      return false;
    }

    if (getPermittedCargoCategoryCodes() == null) {
      if (otherAirLiftPG.getPermittedCargoCategoryCodes() != null) {
        return false;
      }
    } else if (!(getPermittedCargoCategoryCodes().equals(otherAirLiftPG.getPermittedCargoCategoryCodes()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends AirLiftPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(AirLiftPG original) {
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


  private transient AirLiftPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new AirLiftPGImpl(AirLiftPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[12];
  static {
    try {
      properties[0]= new PropertyDescriptor("maximum_bulk_volume", AirLiftPG.class, "getMaximumBulkVolume", null);
      properties[1]= new PropertyDescriptor("maximum_bulk_weight", AirLiftPG.class, "getMaximumBulkWeight", null);
      properties[2]= new PropertyDescriptor("maximum_oversize_volume", AirLiftPG.class, "getMaximumOversizeVolume", null);
      properties[3]= new PropertyDescriptor("maximum_oversize_weight", AirLiftPG.class, "getMaximumOversizeWeight", null);
      properties[4]= new PropertyDescriptor("maximum_outsize_volume", AirLiftPG.class, "getMaximumOutsizeVolume", null);
      properties[5]= new PropertyDescriptor("maximum_outsize_weight", AirLiftPG.class, "getMaximumOutsizeWeight", null);
      properties[6]= new PropertyDescriptor("maximum_pallets", AirLiftPG.class, "getMaximumPallets", null);
      properties[7]= new PropertyDescriptor("maximum_length", AirLiftPG.class, "getMaximumLength", null);
      properties[8]= new PropertyDescriptor("maximum_width", AirLiftPG.class, "getMaximumWidth", null);
      properties[9]= new PropertyDescriptor("maximum_height", AirLiftPG.class, "getMaximumHeight", null);
      properties[10]= new PropertyDescriptor("cargo_restrictions", AirLiftPG.class, "getCargoRestrictions", null);
      properties[11]= new PropertyDescriptor("permitted_cargo_category_codes", AirLiftPG.class, "getPermittedCargoCategoryCodes", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(AirLiftPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements AirLiftPG, Cloneable, LockedPG
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
         return AirLiftPGImpl.this;
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
      return new AirLiftPGImpl(AirLiftPGImpl.this);
    }

    public boolean equals(Object object) { return AirLiftPGImpl.this.equals(object); }
    public Volume getMaximumBulkVolume() { return AirLiftPGImpl.this.getMaximumBulkVolume(); }
    public Mass getMaximumBulkWeight() { return AirLiftPGImpl.this.getMaximumBulkWeight(); }
    public Volume getMaximumOversizeVolume() { return AirLiftPGImpl.this.getMaximumOversizeVolume(); }
    public Mass getMaximumOversizeWeight() { return AirLiftPGImpl.this.getMaximumOversizeWeight(); }
    public Volume getMaximumOutsizeVolume() { return AirLiftPGImpl.this.getMaximumOutsizeVolume(); }
    public Mass getMaximumOutsizeWeight() { return AirLiftPGImpl.this.getMaximumOutsizeWeight(); }
    public int getMaximumPallets() { return AirLiftPGImpl.this.getMaximumPallets(); }
    public Distance getMaximumLength() { return AirLiftPGImpl.this.getMaximumLength(); }
    public Distance getMaximumWidth() { return AirLiftPGImpl.this.getMaximumWidth(); }
    public Distance getMaximumHeight() { return AirLiftPGImpl.this.getMaximumHeight(); }
    public String getCargoRestrictions() { return AirLiftPGImpl.this.getCargoRestrictions(); }
    public String getPermittedCargoCategoryCodes() { return AirLiftPGImpl.this.getPermittedCargoCategoryCodes(); }
  public final boolean hasDataQuality() { return AirLiftPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return AirLiftPGImpl.this.getDataQuality(); }
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
      return AirLiftPGImpl.class;
    }

  }

}
