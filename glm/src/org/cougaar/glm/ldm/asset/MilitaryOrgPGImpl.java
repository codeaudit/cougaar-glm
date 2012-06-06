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
/** Implementation of MilitaryOrgPG.
 *  @see MilitaryOrgPG
 *  @see NewMilitaryOrgPG
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

public class MilitaryOrgPGImpl extends java.beans.SimpleBeanInfo
  implements NewMilitaryOrgPG, Cloneable
{
  public MilitaryOrgPGImpl() {
  }

  // Slots

  private String theStandardName;
  public String getStandardName(){ return theStandardName; }
  public void setStandardName(String standard_name) {
    theStandardName=standard_name;
  }
  private String theUIC;
  public String getUIC(){ return theUIC; }
  public void setUIC(String UIC) {
    theUIC=UIC;
  }
  private String theUTC;
  public String getUTC(){ return theUTC; }
  public void setUTC(String UTC) {
    theUTC=UTC;
  }
  private String theSRC;
  public String getSRC(){ return theSRC; }
  public void setSRC(String SRC) {
    theSRC=SRC;
  }
  private String theEchelon;
  public String getEchelon(){ return theEchelon; }
  public void setEchelon(String echelon) {
    theEchelon=echelon;
  }
  private String theRequestedEchelonOfSupport;
  public String getRequestedEchelonOfSupport(){ return theRequestedEchelonOfSupport; }
  public void setRequestedEchelonOfSupport(String requested_echelon_of_support) {
    theRequestedEchelonOfSupport=requested_echelon_of_support;
  }
  private String theHierarchy2525;
  public String getHierarchy2525(){ return theHierarchy2525; }
  public void setHierarchy2525(String hierarchy_2525) {
    theHierarchy2525=hierarchy_2525;
  }
  private boolean theIsReserve;
  public boolean getIsReserve(){ return theIsReserve; }
  public void setIsReserve(boolean is_reserve) {
    theIsReserve=is_reserve;
  }
  private Location theHomeLocation;
  public Location getHomeLocation(){ return theHomeLocation; }
  public void setHomeLocation(Location home_location) {
    theHomeLocation=home_location;
  }


  public MilitaryOrgPGImpl(MilitaryOrgPG original) {
    theStandardName = original.getStandardName();
    theUIC = original.getUIC();
    theUTC = original.getUTC();
    theSRC = original.getSRC();
    theEchelon = original.getEchelon();
    theRequestedEchelonOfSupport = original.getRequestedEchelonOfSupport();
    theHierarchy2525 = original.getHierarchy2525();
    theIsReserve = original.getIsReserve();
    theHomeLocation = original.getHomeLocation();
  }

  public boolean equals(Object other) {

    if (!(other instanceof MilitaryOrgPG)) {
      return false;
    }

    MilitaryOrgPG otherMilitaryOrgPG = (MilitaryOrgPG) other;

    if (getStandardName() == null) {
      if (otherMilitaryOrgPG.getStandardName() != null) {
        return false;
      }
    } else if (!(getStandardName().equals(otherMilitaryOrgPG.getStandardName()))) {
      return false;
    }

    if (getUIC() == null) {
      if (otherMilitaryOrgPG.getUIC() != null) {
        return false;
      }
    } else if (!(getUIC().equals(otherMilitaryOrgPG.getUIC()))) {
      return false;
    }

    if (getUTC() == null) {
      if (otherMilitaryOrgPG.getUTC() != null) {
        return false;
      }
    } else if (!(getUTC().equals(otherMilitaryOrgPG.getUTC()))) {
      return false;
    }

    if (getSRC() == null) {
      if (otherMilitaryOrgPG.getSRC() != null) {
        return false;
      }
    } else if (!(getSRC().equals(otherMilitaryOrgPG.getSRC()))) {
      return false;
    }

    if (getEchelon() == null) {
      if (otherMilitaryOrgPG.getEchelon() != null) {
        return false;
      }
    } else if (!(getEchelon().equals(otherMilitaryOrgPG.getEchelon()))) {
      return false;
    }

    if (getRequestedEchelonOfSupport() == null) {
      if (otherMilitaryOrgPG.getRequestedEchelonOfSupport() != null) {
        return false;
      }
    } else if (!(getRequestedEchelonOfSupport().equals(otherMilitaryOrgPG.getRequestedEchelonOfSupport()))) {
      return false;
    }

    if (getHierarchy2525() == null) {
      if (otherMilitaryOrgPG.getHierarchy2525() != null) {
        return false;
      }
    } else if (!(getHierarchy2525().equals(otherMilitaryOrgPG.getHierarchy2525()))) {
      return false;
    }

    if (!(getIsReserve() == otherMilitaryOrgPG.getIsReserve())) {
      return false;
    }

    if (getHomeLocation() == null) {
      if (otherMilitaryOrgPG.getHomeLocation() != null) {
        return false;
      }
    } else if (!(getHomeLocation().equals(otherMilitaryOrgPG.getHomeLocation()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends MilitaryOrgPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(MilitaryOrgPG original) {
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


  private transient MilitaryOrgPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new MilitaryOrgPGImpl(MilitaryOrgPGImpl.this);
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
      properties[0]= new PropertyDescriptor("standard_name", MilitaryOrgPG.class, "getStandardName", null);
      properties[1]= new PropertyDescriptor("UIC", MilitaryOrgPG.class, "getUIC", null);
      properties[2]= new PropertyDescriptor("UTC", MilitaryOrgPG.class, "getUTC", null);
      properties[3]= new PropertyDescriptor("SRC", MilitaryOrgPG.class, "getSRC", null);
      properties[4]= new PropertyDescriptor("echelon", MilitaryOrgPG.class, "getEchelon", null);
      properties[5]= new PropertyDescriptor("requested_echelon_of_support", MilitaryOrgPG.class, "getRequestedEchelonOfSupport", null);
      properties[6]= new PropertyDescriptor("hierarchy_2525", MilitaryOrgPG.class, "getHierarchy2525", null);
      properties[7]= new PropertyDescriptor("is_reserve", MilitaryOrgPG.class, "getIsReserve", null);
      properties[8]= new PropertyDescriptor("home_location", MilitaryOrgPG.class, "getHomeLocation", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(MilitaryOrgPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements MilitaryOrgPG, Cloneable, LockedPG
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
         return MilitaryOrgPGImpl.this;
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
      return new MilitaryOrgPGImpl(MilitaryOrgPGImpl.this);
    }

    public boolean equals(Object object) { return MilitaryOrgPGImpl.this.equals(object); }
    public String getStandardName() { return MilitaryOrgPGImpl.this.getStandardName(); }
    public String getUIC() { return MilitaryOrgPGImpl.this.getUIC(); }
    public String getUTC() { return MilitaryOrgPGImpl.this.getUTC(); }
    public String getSRC() { return MilitaryOrgPGImpl.this.getSRC(); }
    public String getEchelon() { return MilitaryOrgPGImpl.this.getEchelon(); }
    public String getRequestedEchelonOfSupport() { return MilitaryOrgPGImpl.this.getRequestedEchelonOfSupport(); }
    public String getHierarchy2525() { return MilitaryOrgPGImpl.this.getHierarchy2525(); }
    public boolean getIsReserve() { return MilitaryOrgPGImpl.this.getIsReserve(); }
    public Location getHomeLocation() { return MilitaryOrgPGImpl.this.getHomeLocation(); }
  public final boolean hasDataQuality() { return MilitaryOrgPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return MilitaryOrgPGImpl.this.getDataQuality(); }
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
      return MilitaryOrgPGImpl.class;
    }

  }

}
