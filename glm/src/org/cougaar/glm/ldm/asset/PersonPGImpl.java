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
/** Implementation of PersonPG.
 *  @see PersonPG
 *  @see NewPersonPG
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

public class PersonPGImpl extends java.beans.SimpleBeanInfo
  implements NewPersonPG, Cloneable
{
  public PersonPGImpl() {
  }

  // Slots

  private String theIDCode;
  public String getIDCode(){ return theIDCode; }
  public void setIDCode(String ID_code) {
    theIDCode=ID_code;
  }
  private String theNationality;
  public String getNationality(){ return theNationality; }
  public void setNationality(String nationality) {
    theNationality=nationality;
  }
  private Collection theSkills;
  public Collection getSkills(){ return theSkills; }
  public boolean inSkills(Skill _element) {
    return (theSkills==null)?false:(theSkills.contains(_element));
  }
  public Skill[] getSkillsAsArray() {
    if (theSkills == null) return new Skill[0];
    int l = theSkills.size();
    Skill[] v = new Skill[l];
    int i=0;
    for (Iterator n=theSkills.iterator(); n.hasNext(); ) {
      v[i]=(Skill) n.next();
      i++;
    }
    return v;
  }
  public Skill getIndexedSkills(int _index) {
    if (theSkills == null) return null;
    for (Iterator _i = theSkills.iterator(); _i.hasNext();) {
      Skill _e = (Skill) _i.next();
      if (_index == 0) return _e;
      _index--;
    }
    return null;
  }
  public void setSkills(Collection skills) {
    theSkills=skills;
  }
  public void clearSkills() {
    theSkills.clear();
  }
  public boolean removeFromSkills(Skill _element) {
    return theSkills.remove(_element);
  }
  public boolean addToSkills(Skill _element) {
    return theSkills.add(_element);
  }


  public PersonPGImpl(PersonPG original) {
    theIDCode = original.getIDCode();
    theNationality = original.getNationality();
    theSkills = original.getSkills();
  }

  public boolean equals(Object other) {

    if (!(other instanceof PersonPG)) {
      return false;
    }

    PersonPG otherPersonPG = (PersonPG) other;

    if (getIDCode() == null) {
      if (otherPersonPG.getIDCode() != null) {
        return false;
      }
    } else if (!(getIDCode().equals(otherPersonPG.getIDCode()))) {
      return false;
    }

    if (getNationality() == null) {
      if (otherPersonPG.getNationality() != null) {
        return false;
      }
    } else if (!(getNationality().equals(otherPersonPG.getNationality()))) {
      return false;
    }

    if (getSkills() == null) {
      if (otherPersonPG.getSkills() != null) {
        return false;
      }
    } else if (!(getSkills().equals(otherPersonPG.getSkills()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends PersonPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(PersonPG original) {
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


  private transient PersonPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new PersonPGImpl(PersonPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[3];
  static {
    try {
      properties[0]= new PropertyDescriptor("ID_code", PersonPG.class, "getIDCode", null);
      properties[1]= new PropertyDescriptor("nationality", PersonPG.class, "getNationality", null);
      properties[2]= new IndexedPropertyDescriptor("skills", PersonPG.class, "getSkillsAsArray", null, "getIndexedSkills", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(PersonPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements PersonPG, Cloneable, LockedPG
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
         return PersonPGImpl.this;
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
      return new PersonPGImpl(PersonPGImpl.this);
    }

    public boolean equals(Object object) { return PersonPGImpl.this.equals(object); }
    public String getIDCode() { return PersonPGImpl.this.getIDCode(); }
    public String getNationality() { return PersonPGImpl.this.getNationality(); }
    public Collection getSkills() { return PersonPGImpl.this.getSkills(); }
  public boolean inSkills(Skill _element) {
    return PersonPGImpl.this.inSkills(_element);
  }
  public Skill[] getSkillsAsArray() {
    return PersonPGImpl.this.getSkillsAsArray();
  }
  public Skill getIndexedSkills(int _index) {
    return PersonPGImpl.this.getIndexedSkills(_index);
  }
  public final boolean hasDataQuality() { return PersonPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return PersonPGImpl.this.getDataQuality(); }
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
      return PersonPGImpl.class;
    }

  }

}
