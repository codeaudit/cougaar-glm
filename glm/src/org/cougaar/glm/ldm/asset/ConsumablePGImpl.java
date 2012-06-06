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
/** Implementation of ConsumablePG.
 *  @see ConsumablePG
 *  @see NewConsumablePG
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

public class ConsumablePGImpl extends java.beans.SimpleBeanInfo
  implements NewConsumablePG, Cloneable
{
  public ConsumablePGImpl() {
  }

  // Slots

  private double theFailRate;
  public double getFailRate(){ return theFailRate; }
  public void setFailRate(double fail_rate) {
    theFailRate=fail_rate;
  }
  private String theAcquisitionAdviceCode;
  public String getAcquisitionAdviceCode(){ return theAcquisitionAdviceCode; }
  public void setAcquisitionAdviceCode(String acquisition_advice_code) {
    theAcquisitionAdviceCode=acquisition_advice_code;
  }
  private String theSupplyStatusCode;
  public String getSupplyStatusCode(){ return theSupplyStatusCode; }
  public void setSupplyStatusCode(String supply_status_code) {
    theSupplyStatusCode=supply_status_code;
  }
  private String theItemCategoryCode;
  public String getItemCategoryCode(){ return theItemCategoryCode; }
  public void setItemCategoryCode(String item_category_code) {
    theItemCategoryCode=item_category_code;
  }


  public ConsumablePGImpl(ConsumablePG original) {
    theFailRate = original.getFailRate();
    theAcquisitionAdviceCode = original.getAcquisitionAdviceCode();
    theSupplyStatusCode = original.getSupplyStatusCode();
    theItemCategoryCode = original.getItemCategoryCode();
  }

  public boolean equals(Object other) {

    if (!(other instanceof ConsumablePG)) {
      return false;
    }

    ConsumablePG otherConsumablePG = (ConsumablePG) other;

    if (!(getFailRate() == otherConsumablePG.getFailRate())) {
      return false;
    }

    if (getAcquisitionAdviceCode() == null) {
      if (otherConsumablePG.getAcquisitionAdviceCode() != null) {
        return false;
      }
    } else if (!(getAcquisitionAdviceCode().equals(otherConsumablePG.getAcquisitionAdviceCode()))) {
      return false;
    }

    if (getSupplyStatusCode() == null) {
      if (otherConsumablePG.getSupplyStatusCode() != null) {
        return false;
      }
    } else if (!(getSupplyStatusCode().equals(otherConsumablePG.getSupplyStatusCode()))) {
      return false;
    }

    if (getItemCategoryCode() == null) {
      if (otherConsumablePG.getItemCategoryCode() != null) {
        return false;
      }
    } else if (!(getItemCategoryCode().equals(otherConsumablePG.getItemCategoryCode()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends ConsumablePGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(ConsumablePG original) {
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


  private transient ConsumablePG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new ConsumablePGImpl(ConsumablePGImpl.this);
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
      properties[0]= new PropertyDescriptor("fail_rate", ConsumablePG.class, "getFailRate", null);
      properties[1]= new PropertyDescriptor("acquisition_advice_code", ConsumablePG.class, "getAcquisitionAdviceCode", null);
      properties[2]= new PropertyDescriptor("supply_status_code", ConsumablePG.class, "getSupplyStatusCode", null);
      properties[3]= new PropertyDescriptor("item_category_code", ConsumablePG.class, "getItemCategoryCode", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(ConsumablePG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements ConsumablePG, Cloneable, LockedPG
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
         return ConsumablePGImpl.this;
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
      return new ConsumablePGImpl(ConsumablePGImpl.this);
    }

    public boolean equals(Object object) { return ConsumablePGImpl.this.equals(object); }
    public double getFailRate() { return ConsumablePGImpl.this.getFailRate(); }
    public String getAcquisitionAdviceCode() { return ConsumablePGImpl.this.getAcquisitionAdviceCode(); }
    public String getSupplyStatusCode() { return ConsumablePGImpl.this.getSupplyStatusCode(); }
    public String getItemCategoryCode() { return ConsumablePGImpl.this.getItemCategoryCode(); }
  public final boolean hasDataQuality() { return ConsumablePGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return ConsumablePGImpl.this.getDataQuality(); }
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
      return ConsumablePGImpl.class;
    }

  }

}
