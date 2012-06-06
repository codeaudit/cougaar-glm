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
/** Implementation of PackagePG.
 *  @see PackagePG
 *  @see NewPackagePG
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

public class PackagePGImpl extends java.beans.SimpleBeanInfo
  implements NewPackagePG, Cloneable
{
  public PackagePGImpl() {
  }

  // Slots

  private long theCountPerPack;
  public long getCountPerPack(){ return theCountPerPack; }
  public void setCountPerPack(long count_per_pack) {
    theCountPerPack=count_per_pack;
  }
  private String theUnitOfIssue;
  public String getUnitOfIssue(){ return theUnitOfIssue; }
  public void setUnitOfIssue(String unit_of_issue) {
    theUnitOfIssue=unit_of_issue;
  }
  private Distance thePackLength;
  public Distance getPackLength(){ return thePackLength; }
  public void setPackLength(Distance pack_length) {
    thePackLength=pack_length;
  }
  private Distance thePackWidth;
  public Distance getPackWidth(){ return thePackWidth; }
  public void setPackWidth(Distance pack_width) {
    thePackWidth=pack_width;
  }
  private Distance thePackHeight;
  public Distance getPackHeight(){ return thePackHeight; }
  public void setPackHeight(Distance pack_height) {
    thePackHeight=pack_height;
  }
  private Area thePackFootprintArea;
  public Area getPackFootprintArea(){ return thePackFootprintArea; }
  public void setPackFootprintArea(Area pack_footprint_area) {
    thePackFootprintArea=pack_footprint_area;
  }
  private Volume thePackVolume;
  public Volume getPackVolume(){ return thePackVolume; }
  public void setPackVolume(Volume pack_volume) {
    thePackVolume=pack_volume;
  }
  private Mass thePackMass;
  public Mass getPackMass(){ return thePackMass; }
  public void setPackMass(Mass pack_mass) {
    thePackMass=pack_mass;
  }


  public PackagePGImpl(PackagePG original) {
    theCountPerPack = original.getCountPerPack();
    theUnitOfIssue = original.getUnitOfIssue();
    thePackLength = original.getPackLength();
    thePackWidth = original.getPackWidth();
    thePackHeight = original.getPackHeight();
    thePackFootprintArea = original.getPackFootprintArea();
    thePackVolume = original.getPackVolume();
    thePackMass = original.getPackMass();
  }

  public boolean equals(Object other) {

    if (!(other instanceof PackagePG)) {
      return false;
    }

    PackagePG otherPackagePG = (PackagePG) other;

    if (!(getCountPerPack() == otherPackagePG.getCountPerPack())) {
      return false;
    }

    if (getUnitOfIssue() == null) {
      if (otherPackagePG.getUnitOfIssue() != null) {
        return false;
      }
    } else if (!(getUnitOfIssue().equals(otherPackagePG.getUnitOfIssue()))) {
      return false;
    }

    if (getPackLength() == null) {
      if (otherPackagePG.getPackLength() != null) {
        return false;
      }
    } else if (!(getPackLength().equals(otherPackagePG.getPackLength()))) {
      return false;
    }

    if (getPackWidth() == null) {
      if (otherPackagePG.getPackWidth() != null) {
        return false;
      }
    } else if (!(getPackWidth().equals(otherPackagePG.getPackWidth()))) {
      return false;
    }

    if (getPackHeight() == null) {
      if (otherPackagePG.getPackHeight() != null) {
        return false;
      }
    } else if (!(getPackHeight().equals(otherPackagePG.getPackHeight()))) {
      return false;
    }

    if (getPackFootprintArea() == null) {
      if (otherPackagePG.getPackFootprintArea() != null) {
        return false;
      }
    } else if (!(getPackFootprintArea().equals(otherPackagePG.getPackFootprintArea()))) {
      return false;
    }

    if (getPackVolume() == null) {
      if (otherPackagePG.getPackVolume() != null) {
        return false;
      }
    } else if (!(getPackVolume().equals(otherPackagePG.getPackVolume()))) {
      return false;
    }

    if (getPackMass() == null) {
      if (otherPackagePG.getPackMass() != null) {
        return false;
      }
    } else if (!(getPackMass().equals(otherPackagePG.getPackMass()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends PackagePGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(PackagePG original) {
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


  private transient PackagePG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new PackagePGImpl(PackagePGImpl.this);
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
      properties[0]= new PropertyDescriptor("count_per_pack", PackagePG.class, "getCountPerPack", null);
      properties[1]= new PropertyDescriptor("unit_of_issue", PackagePG.class, "getUnitOfIssue", null);
      properties[2]= new PropertyDescriptor("pack_length", PackagePG.class, "getPackLength", null);
      properties[3]= new PropertyDescriptor("pack_width", PackagePG.class, "getPackWidth", null);
      properties[4]= new PropertyDescriptor("pack_height", PackagePG.class, "getPackHeight", null);
      properties[5]= new PropertyDescriptor("pack_footprint_area", PackagePG.class, "getPackFootprintArea", null);
      properties[6]= new PropertyDescriptor("pack_volume", PackagePG.class, "getPackVolume", null);
      properties[7]= new PropertyDescriptor("pack_mass", PackagePG.class, "getPackMass", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(PackagePG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements PackagePG, Cloneable, LockedPG
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
         return PackagePGImpl.this;
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
      return new PackagePGImpl(PackagePGImpl.this);
    }

    public boolean equals(Object object) { return PackagePGImpl.this.equals(object); }
    public long getCountPerPack() { return PackagePGImpl.this.getCountPerPack(); }
    public String getUnitOfIssue() { return PackagePGImpl.this.getUnitOfIssue(); }
    public Distance getPackLength() { return PackagePGImpl.this.getPackLength(); }
    public Distance getPackWidth() { return PackagePGImpl.this.getPackWidth(); }
    public Distance getPackHeight() { return PackagePGImpl.this.getPackHeight(); }
    public Area getPackFootprintArea() { return PackagePGImpl.this.getPackFootprintArea(); }
    public Volume getPackVolume() { return PackagePGImpl.this.getPackVolume(); }
    public Mass getPackMass() { return PackagePGImpl.this.getPackMass(); }
  public final boolean hasDataQuality() { return PackagePGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return PackagePGImpl.this.getDataQuality(); }
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
      return PackagePGImpl.class;
    }

  }

}
