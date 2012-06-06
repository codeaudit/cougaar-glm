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
/** Implementation of AmmunitionPG.
 *  @see AmmunitionPG
 *  @see NewAmmunitionPG
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

public class AmmunitionPGImpl extends java.beans.SimpleBeanInfo
  implements NewAmmunitionPG, Cloneable
{
  public AmmunitionPGImpl() {
  }

  // Slots

  private String theDODIC;
  public String getDODIC(){ return theDODIC; }
  public void setDODIC(String DODIC) {
    theDODIC=DODIC;
  }
  private String theLotID;
  public String getLotID(){ return theLotID; }
  public void setLotID(String lotID) {
    theLotID=lotID;
  }
  private String theDODAAC;
  public String getDODAAC(){ return theDODAAC; }
  public void setDODAAC(String DODAAC) {
    theDODAAC=DODAAC;
  }
  private long theQuantityOhServ;
  public long getQuantityOhServ(){ return theQuantityOhServ; }
  public void setQuantityOhServ(long quantity_oh_serv) {
    theQuantityOhServ=quantity_oh_serv;
  }
  private long theQuantityOhUnserv;
  public long getQuantityOhUnserv(){ return theQuantityOhUnserv; }
  public void setQuantityOhUnserv(long quantity_oh_unserv) {
    theQuantityOhUnserv=quantity_oh_unserv;
  }


  public AmmunitionPGImpl(AmmunitionPG original) {
    theDODIC = original.getDODIC();
    theLotID = original.getLotID();
    theDODAAC = original.getDODAAC();
    theQuantityOhServ = original.getQuantityOhServ();
    theQuantityOhUnserv = original.getQuantityOhUnserv();
  }

  public boolean equals(Object other) {

    if (!(other instanceof AmmunitionPG)) {
      return false;
    }

    AmmunitionPG otherAmmunitionPG = (AmmunitionPG) other;

    if (getDODIC() == null) {
      if (otherAmmunitionPG.getDODIC() != null) {
        return false;
      }
    } else if (!(getDODIC().equals(otherAmmunitionPG.getDODIC()))) {
      return false;
    }

    if (getLotID() == null) {
      if (otherAmmunitionPG.getLotID() != null) {
        return false;
      }
    } else if (!(getLotID().equals(otherAmmunitionPG.getLotID()))) {
      return false;
    }

    if (getDODAAC() == null) {
      if (otherAmmunitionPG.getDODAAC() != null) {
        return false;
      }
    } else if (!(getDODAAC().equals(otherAmmunitionPG.getDODAAC()))) {
      return false;
    }

    if (!(getQuantityOhServ() == otherAmmunitionPG.getQuantityOhServ())) {
      return false;
    }

    if (!(getQuantityOhUnserv() == otherAmmunitionPG.getQuantityOhUnserv())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends AmmunitionPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(AmmunitionPG original) {
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


  private transient AmmunitionPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new AmmunitionPGImpl(AmmunitionPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[5];
  static {
    try {
      properties[0]= new PropertyDescriptor("DODIC", AmmunitionPG.class, "getDODIC", null);
      properties[1]= new PropertyDescriptor("lotID", AmmunitionPG.class, "getLotID", null);
      properties[2]= new PropertyDescriptor("DODAAC", AmmunitionPG.class, "getDODAAC", null);
      properties[3]= new PropertyDescriptor("quantity_oh_serv", AmmunitionPG.class, "getQuantityOhServ", null);
      properties[4]= new PropertyDescriptor("quantity_oh_unserv", AmmunitionPG.class, "getQuantityOhUnserv", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(AmmunitionPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements AmmunitionPG, Cloneable, LockedPG
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
         return AmmunitionPGImpl.this;
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
      return new AmmunitionPGImpl(AmmunitionPGImpl.this);
    }

    public boolean equals(Object object) { return AmmunitionPGImpl.this.equals(object); }
    public String getDODIC() { return AmmunitionPGImpl.this.getDODIC(); }
    public String getLotID() { return AmmunitionPGImpl.this.getLotID(); }
    public String getDODAAC() { return AmmunitionPGImpl.this.getDODAAC(); }
    public long getQuantityOhServ() { return AmmunitionPGImpl.this.getQuantityOhServ(); }
    public long getQuantityOhUnserv() { return AmmunitionPGImpl.this.getQuantityOhUnserv(); }
  public final boolean hasDataQuality() { return AmmunitionPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return AmmunitionPGImpl.this.getDataQuality(); }
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
      return AmmunitionPGImpl.class;
    }

  }

}
