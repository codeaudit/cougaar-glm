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
/** Implementation of SupplyPG.
 *  @see SupplyPG
 *  @see NewSupplyPG
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

public class SupplyPGImpl extends java.beans.SimpleBeanInfo
  implements NewSupplyPG, Cloneable
{
  public SupplyPGImpl() {
  }

  // Slots

  private Mass theClass1HandleMassPerDay;
  public Mass getClass1HandleMassPerDay(){ return theClass1HandleMassPerDay; }
  public void setClass1HandleMassPerDay(Mass class_1_handle_mass_per_day) {
    theClass1HandleMassPerDay=class_1_handle_mass_per_day;
  }
  private Mass theClass2HandleMassPerDay;
  public Mass getClass2HandleMassPerDay(){ return theClass2HandleMassPerDay; }
  public void setClass2HandleMassPerDay(Mass class_2_handle_mass_per_day) {
    theClass2HandleMassPerDay=class_2_handle_mass_per_day;
  }
  private Mass theClass3pHandleMassPerDay;
  public Mass getClass3pHandleMassPerDay(){ return theClass3pHandleMassPerDay; }
  public void setClass3pHandleMassPerDay(Mass class_3p_handle_mass_per_day) {
    theClass3pHandleMassPerDay=class_3p_handle_mass_per_day;
  }
  private Mass theClass4bHandleMassPerDay;
  public Mass getClass4bHandleMassPerDay(){ return theClass4bHandleMassPerDay; }
  public void setClass4bHandleMassPerDay(Mass class_4b_handle_mass_per_day) {
    theClass4bHandleMassPerDay=class_4b_handle_mass_per_day;
  }
  private Mass theClass4HandleMassPerDay;
  public Mass getClass4HandleMassPerDay(){ return theClass4HandleMassPerDay; }
  public void setClass4HandleMassPerDay(Mass class_4_handle_mass_per_day) {
    theClass4HandleMassPerDay=class_4_handle_mass_per_day;
  }
  private Mass theClass5TransloadMassPerDay;
  public Mass getClass5TransloadMassPerDay(){ return theClass5TransloadMassPerDay; }
  public void setClass5TransloadMassPerDay(Mass class_5_transload_mass_per_day) {
    theClass5TransloadMassPerDay=class_5_transload_mass_per_day;
  }
  private Mass theClass7HandleMassPerDay;
  public Mass getClass7HandleMassPerDay(){ return theClass7HandleMassPerDay; }
  public void setClass7HandleMassPerDay(Mass class_7_handle_mass_per_day) {
    theClass7HandleMassPerDay=class_7_handle_mass_per_day;
  }
  private Mass theClass9HandleMassPerDay;
  public Mass getClass9HandleMassPerDay(){ return theClass9HandleMassPerDay; }
  public void setClass9HandleMassPerDay(Mass class_9_handle_mass_per_day) {
    theClass9HandleMassPerDay=class_9_handle_mass_per_day;
  }


  public SupplyPGImpl(SupplyPG original) {
    theClass1HandleMassPerDay = original.getClass1HandleMassPerDay();
    theClass2HandleMassPerDay = original.getClass2HandleMassPerDay();
    theClass3pHandleMassPerDay = original.getClass3pHandleMassPerDay();
    theClass4bHandleMassPerDay = original.getClass4bHandleMassPerDay();
    theClass4HandleMassPerDay = original.getClass4HandleMassPerDay();
    theClass5TransloadMassPerDay = original.getClass5TransloadMassPerDay();
    theClass7HandleMassPerDay = original.getClass7HandleMassPerDay();
    theClass9HandleMassPerDay = original.getClass9HandleMassPerDay();
  }

  public boolean equals(Object other) {

    if (!(other instanceof SupplyPG)) {
      return false;
    }

    SupplyPG otherSupplyPG = (SupplyPG) other;

    if (getClass1HandleMassPerDay() == null) {
      if (otherSupplyPG.getClass1HandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass1HandleMassPerDay().equals(otherSupplyPG.getClass1HandleMassPerDay()))) {
      return false;
    }

    if (getClass2HandleMassPerDay() == null) {
      if (otherSupplyPG.getClass2HandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass2HandleMassPerDay().equals(otherSupplyPG.getClass2HandleMassPerDay()))) {
      return false;
    }

    if (getClass3pHandleMassPerDay() == null) {
      if (otherSupplyPG.getClass3pHandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass3pHandleMassPerDay().equals(otherSupplyPG.getClass3pHandleMassPerDay()))) {
      return false;
    }

    if (getClass4bHandleMassPerDay() == null) {
      if (otherSupplyPG.getClass4bHandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass4bHandleMassPerDay().equals(otherSupplyPG.getClass4bHandleMassPerDay()))) {
      return false;
    }

    if (getClass4HandleMassPerDay() == null) {
      if (otherSupplyPG.getClass4HandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass4HandleMassPerDay().equals(otherSupplyPG.getClass4HandleMassPerDay()))) {
      return false;
    }

    if (getClass5TransloadMassPerDay() == null) {
      if (otherSupplyPG.getClass5TransloadMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass5TransloadMassPerDay().equals(otherSupplyPG.getClass5TransloadMassPerDay()))) {
      return false;
    }

    if (getClass7HandleMassPerDay() == null) {
      if (otherSupplyPG.getClass7HandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass7HandleMassPerDay().equals(otherSupplyPG.getClass7HandleMassPerDay()))) {
      return false;
    }

    if (getClass9HandleMassPerDay() == null) {
      if (otherSupplyPG.getClass9HandleMassPerDay() != null) {
        return false;
      }
    } else if (!(getClass9HandleMassPerDay().equals(otherSupplyPG.getClass9HandleMassPerDay()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends SupplyPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(SupplyPG original) {
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


  private transient SupplyPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new SupplyPGImpl(SupplyPGImpl.this);
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
      properties[0]= new PropertyDescriptor("class_1_handle_mass_per_day", SupplyPG.class, "getClass1HandleMassPerDay", null);
      properties[1]= new PropertyDescriptor("class_2_handle_mass_per_day", SupplyPG.class, "getClass2HandleMassPerDay", null);
      properties[2]= new PropertyDescriptor("class_3p_handle_mass_per_day", SupplyPG.class, "getClass3pHandleMassPerDay", null);
      properties[3]= new PropertyDescriptor("class_4b_handle_mass_per_day", SupplyPG.class, "getClass4bHandleMassPerDay", null);
      properties[4]= new PropertyDescriptor("class_4_handle_mass_per_day", SupplyPG.class, "getClass4HandleMassPerDay", null);
      properties[5]= new PropertyDescriptor("class_5_transload_mass_per_day", SupplyPG.class, "getClass5TransloadMassPerDay", null);
      properties[6]= new PropertyDescriptor("class_7_handle_mass_per_day", SupplyPG.class, "getClass7HandleMassPerDay", null);
      properties[7]= new PropertyDescriptor("class_9_handle_mass_per_day", SupplyPG.class, "getClass9HandleMassPerDay", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(SupplyPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements SupplyPG, Cloneable, LockedPG
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
         return SupplyPGImpl.this;
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
      return new SupplyPGImpl(SupplyPGImpl.this);
    }

    public boolean equals(Object object) { return SupplyPGImpl.this.equals(object); }
    public Mass getClass1HandleMassPerDay() { return SupplyPGImpl.this.getClass1HandleMassPerDay(); }
    public Mass getClass2HandleMassPerDay() { return SupplyPGImpl.this.getClass2HandleMassPerDay(); }
    public Mass getClass3pHandleMassPerDay() { return SupplyPGImpl.this.getClass3pHandleMassPerDay(); }
    public Mass getClass4bHandleMassPerDay() { return SupplyPGImpl.this.getClass4bHandleMassPerDay(); }
    public Mass getClass4HandleMassPerDay() { return SupplyPGImpl.this.getClass4HandleMassPerDay(); }
    public Mass getClass5TransloadMassPerDay() { return SupplyPGImpl.this.getClass5TransloadMassPerDay(); }
    public Mass getClass7HandleMassPerDay() { return SupplyPGImpl.this.getClass7HandleMassPerDay(); }
    public Mass getClass9HandleMassPerDay() { return SupplyPGImpl.this.getClass9HandleMassPerDay(); }
  public final boolean hasDataQuality() { return SupplyPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return SupplyPGImpl.this.getDataQuality(); }
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
      return SupplyPGImpl.class;
    }

  }

}
