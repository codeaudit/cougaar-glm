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
/** Implementation of PhysicalPG.
 *  @see PhysicalPG
 *  @see NewPhysicalPG
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

public class PhysicalPGImpl extends java.beans.SimpleBeanInfo
  implements NewPhysicalPG, Cloneable
{
  public PhysicalPGImpl() {
  }

  // Slots

  private Distance theLength;
  public Distance getLength(){ return theLength; }
  public void setLength(Distance length) {
    theLength=length;
  }
  private Distance theWidth;
  public Distance getWidth(){ return theWidth; }
  public void setWidth(Distance width) {
    theWidth=width;
  }
  private Distance theHeight;
  public Distance getHeight(){ return theHeight; }
  public void setHeight(Distance height) {
    theHeight=height;
  }
  private Area theFootprintArea;
  public Area getFootprintArea(){ return theFootprintArea; }
  public void setFootprintArea(Area footprint_area) {
    theFootprintArea=footprint_area;
  }
  private Volume theVolume;
  public Volume getVolume(){ return theVolume; }
  public void setVolume(Volume volume) {
    theVolume=volume;
  }
  private Mass theMass;
  public Mass getMass(){ return theMass; }
  public void setMass(Mass mass) {
    theMass=mass;
  }


  public PhysicalPGImpl(PhysicalPG original) {
    theLength = original.getLength();
    theWidth = original.getWidth();
    theHeight = original.getHeight();
    theFootprintArea = original.getFootprintArea();
    theVolume = original.getVolume();
    theMass = original.getMass();
  }

  public boolean equals(Object other) {

    if (!(other instanceof PhysicalPG)) {
      return false;
    }

    PhysicalPG otherPhysicalPG = (PhysicalPG) other;

    if (getLength() == null) {
      if (otherPhysicalPG.getLength() != null) {
        return false;
      }
    } else if (!(getLength().equals(otherPhysicalPG.getLength()))) {
      return false;
    }

    if (getWidth() == null) {
      if (otherPhysicalPG.getWidth() != null) {
        return false;
      }
    } else if (!(getWidth().equals(otherPhysicalPG.getWidth()))) {
      return false;
    }

    if (getHeight() == null) {
      if (otherPhysicalPG.getHeight() != null) {
        return false;
      }
    } else if (!(getHeight().equals(otherPhysicalPG.getHeight()))) {
      return false;
    }

    if (getFootprintArea() == null) {
      if (otherPhysicalPG.getFootprintArea() != null) {
        return false;
      }
    } else if (!(getFootprintArea().equals(otherPhysicalPG.getFootprintArea()))) {
      return false;
    }

    if (getVolume() == null) {
      if (otherPhysicalPG.getVolume() != null) {
        return false;
      }
    } else if (!(getVolume().equals(otherPhysicalPG.getVolume()))) {
      return false;
    }

    if (getMass() == null) {
      if (otherPhysicalPG.getMass() != null) {
        return false;
      }
    } else if (!(getMass().equals(otherPhysicalPG.getMass()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends PhysicalPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(PhysicalPG original) {
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


  private transient PhysicalPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new PhysicalPGImpl(PhysicalPGImpl.this);
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
      properties[0]= new PropertyDescriptor("length", PhysicalPG.class, "getLength", null);
      properties[1]= new PropertyDescriptor("width", PhysicalPG.class, "getWidth", null);
      properties[2]= new PropertyDescriptor("height", PhysicalPG.class, "getHeight", null);
      properties[3]= new PropertyDescriptor("footprint_area", PhysicalPG.class, "getFootprintArea", null);
      properties[4]= new PropertyDescriptor("volume", PhysicalPG.class, "getVolume", null);
      properties[5]= new PropertyDescriptor("mass", PhysicalPG.class, "getMass", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(PhysicalPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements PhysicalPG, Cloneable, LockedPG
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
         return PhysicalPGImpl.this;
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
      return new PhysicalPGImpl(PhysicalPGImpl.this);
    }

    public boolean equals(Object object) { return PhysicalPGImpl.this.equals(object); }
    public Distance getLength() { return PhysicalPGImpl.this.getLength(); }
    public Distance getWidth() { return PhysicalPGImpl.this.getWidth(); }
    public Distance getHeight() { return PhysicalPGImpl.this.getHeight(); }
    public Area getFootprintArea() { return PhysicalPGImpl.this.getFootprintArea(); }
    public Volume getVolume() { return PhysicalPGImpl.this.getVolume(); }
    public Mass getMass() { return PhysicalPGImpl.this.getMass(); }
  public final boolean hasDataQuality() { return PhysicalPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return PhysicalPGImpl.this.getDataQuality(); }
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
      return PhysicalPGImpl.class;
    }

  }

}
