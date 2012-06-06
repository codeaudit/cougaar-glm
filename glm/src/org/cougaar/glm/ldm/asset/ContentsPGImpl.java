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
/** Implementation of ContentsPG.
 *  @see ContentsPG
 *  @see NewContentsPG
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

public class ContentsPGImpl extends java.beans.SimpleBeanInfo
  implements NewContentsPG, Cloneable
{
  public ContentsPGImpl() {
  }

  // Slots

  private Collection theTypeIdentifications;
  public Collection getTypeIdentifications(){ return theTypeIdentifications; }
  public boolean inTypeIdentifications(String _element) {
    return (theTypeIdentifications==null)?false:(theTypeIdentifications.contains(_element));
  }
  public String[] getTypeIdentificationsAsArray() {
    if (theTypeIdentifications == null) return new String[0];
    int l = theTypeIdentifications.size();
    String[] v = new String[l];
    int i=0;
    for (Iterator n=theTypeIdentifications.iterator(); n.hasNext(); ) {
      v[i]=(String) n.next();
      i++;
    }
    return v;
  }
  public String getIndexedTypeIdentifications(int _index) {
    if (theTypeIdentifications == null) return null;
    for (Iterator _i = theTypeIdentifications.iterator(); _i.hasNext();) {
      String _e = (String) _i.next();
      if (_index == 0) return _e;
      _index--;
    }
    return null;
  }
  public void setTypeIdentifications(Collection type_identifications) {
    theTypeIdentifications=type_identifications;
  }
  public void clearTypeIdentifications() {
    theTypeIdentifications.clear();
  }
  public boolean removeFromTypeIdentifications(String _element) {
    return theTypeIdentifications.remove(_element);
  }
  public boolean addToTypeIdentifications(String _element) {
    return theTypeIdentifications.add(_element);
  }
  private Collection theNomenclatures;
  public Collection getNomenclatures(){ return theNomenclatures; }
  public boolean inNomenclatures(String _element) {
    return (theNomenclatures==null)?false:(theNomenclatures.contains(_element));
  }
  public String[] getNomenclaturesAsArray() {
    if (theNomenclatures == null) return new String[0];
    int l = theNomenclatures.size();
    String[] v = new String[l];
    int i=0;
    for (Iterator n=theNomenclatures.iterator(); n.hasNext(); ) {
      v[i]=(String) n.next();
      i++;
    }
    return v;
  }
  public String getIndexedNomenclatures(int _index) {
    if (theNomenclatures == null) return null;
    for (Iterator _i = theNomenclatures.iterator(); _i.hasNext();) {
      String _e = (String) _i.next();
      if (_index == 0) return _e;
      _index--;
    }
    return null;
  }
  public void setNomenclatures(Collection nomenclatures) {
    theNomenclatures=nomenclatures;
  }
  public void clearNomenclatures() {
    theNomenclatures.clear();
  }
  public boolean removeFromNomenclatures(String _element) {
    return theNomenclatures.remove(_element);
  }
  public boolean addToNomenclatures(String _element) {
    return theNomenclatures.add(_element);
  }
  private Collection theWeights;
  public Collection getWeights(){ return theWeights; }
  public boolean inWeights(Mass _element) {
    return (theWeights==null)?false:(theWeights.contains(_element));
  }
  public Mass[] getWeightsAsArray() {
    if (theWeights == null) return new Mass[0];
    int l = theWeights.size();
    Mass[] v = new Mass[l];
    int i=0;
    for (Iterator n=theWeights.iterator(); n.hasNext(); ) {
      v[i]=(Mass) n.next();
      i++;
    }
    return v;
  }
  public Mass getIndexedWeights(int _index) {
    if (theWeights == null) return null;
    for (Iterator _i = theWeights.iterator(); _i.hasNext();) {
      Mass _e = (Mass) _i.next();
      if (_index == 0) return _e;
      _index--;
    }
    return null;
  }
  public void setWeights(Collection weights) {
    theWeights=weights;
  }
  public void clearWeights() {
    theWeights.clear();
  }
  public boolean removeFromWeights(Mass _element) {
    return theWeights.remove(_element);
  }
  public boolean addToWeights(Mass _element) {
    return theWeights.add(_element);
  }
  private Collection theReceivers;
  public Collection getReceivers(){ return theReceivers; }
  public boolean inReceivers(String _element) {
    return (theReceivers==null)?false:(theReceivers.contains(_element));
  }
  public String[] getReceiversAsArray() {
    if (theReceivers == null) return new String[0];
    int l = theReceivers.size();
    String[] v = new String[l];
    int i=0;
    for (Iterator n=theReceivers.iterator(); n.hasNext(); ) {
      v[i]=(String) n.next();
      i++;
    }
    return v;
  }
  public String getIndexedReceivers(int _index) {
    if (theReceivers == null) return null;
    for (Iterator _i = theReceivers.iterator(); _i.hasNext();) {
      String _e = (String) _i.next();
      if (_index == 0) return _e;
      _index--;
    }
    return null;
  }
  public void setReceivers(Collection receivers) {
    theReceivers=receivers;
  }
  public void clearReceivers() {
    theReceivers.clear();
  }
  public boolean removeFromReceivers(String _element) {
    return theReceivers.remove(_element);
  }
  public boolean addToReceivers(String _element) {
    return theReceivers.add(_element);
  }


  public ContentsPGImpl(ContentsPG original) {
    theTypeIdentifications = original.getTypeIdentifications();
    theNomenclatures = original.getNomenclatures();
    theWeights = original.getWeights();
    theReceivers = original.getReceivers();
  }

  public boolean equals(Object other) {

    if (!(other instanceof ContentsPG)) {
      return false;
    }

    ContentsPG otherContentsPG = (ContentsPG) other;

    if (getTypeIdentifications() == null) {
      if (otherContentsPG.getTypeIdentifications() != null) {
        return false;
      }
    } else if (!(getTypeIdentifications().equals(otherContentsPG.getTypeIdentifications()))) {
      return false;
    }

    if (getNomenclatures() == null) {
      if (otherContentsPG.getNomenclatures() != null) {
        return false;
      }
    } else if (!(getNomenclatures().equals(otherContentsPG.getNomenclatures()))) {
      return false;
    }

    if (getWeights() == null) {
      if (otherContentsPG.getWeights() != null) {
        return false;
      }
    } else if (!(getWeights().equals(otherContentsPG.getWeights()))) {
      return false;
    }

    if (getReceivers() == null) {
      if (otherContentsPG.getReceivers() != null) {
        return false;
      }
    } else if (!(getReceivers().equals(otherContentsPG.getReceivers()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends ContentsPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(ContentsPG original) {
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


  private transient ContentsPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new ContentsPGImpl(ContentsPGImpl.this);
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
      properties[0]= new IndexedPropertyDescriptor("type_identifications", ContentsPG.class, "getTypeIdentificationsAsArray", null, "getIndexedTypeIdentifications", null);
      properties[1]= new IndexedPropertyDescriptor("nomenclatures", ContentsPG.class, "getNomenclaturesAsArray", null, "getIndexedNomenclatures", null);
      properties[2]= new IndexedPropertyDescriptor("weights", ContentsPG.class, "getWeightsAsArray", null, "getIndexedWeights", null);
      properties[3]= new IndexedPropertyDescriptor("receivers", ContentsPG.class, "getReceiversAsArray", null, "getIndexedReceivers", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(ContentsPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements ContentsPG, Cloneable, LockedPG
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
         return ContentsPGImpl.this;
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
      return new ContentsPGImpl(ContentsPGImpl.this);
    }

    public boolean equals(Object object) { return ContentsPGImpl.this.equals(object); }
    public Collection getTypeIdentifications() { return ContentsPGImpl.this.getTypeIdentifications(); }
  public boolean inTypeIdentifications(String _element) {
    return ContentsPGImpl.this.inTypeIdentifications(_element);
  }
  public String[] getTypeIdentificationsAsArray() {
    return ContentsPGImpl.this.getTypeIdentificationsAsArray();
  }
  public String getIndexedTypeIdentifications(int _index) {
    return ContentsPGImpl.this.getIndexedTypeIdentifications(_index);
  }
    public Collection getNomenclatures() { return ContentsPGImpl.this.getNomenclatures(); }
  public boolean inNomenclatures(String _element) {
    return ContentsPGImpl.this.inNomenclatures(_element);
  }
  public String[] getNomenclaturesAsArray() {
    return ContentsPGImpl.this.getNomenclaturesAsArray();
  }
  public String getIndexedNomenclatures(int _index) {
    return ContentsPGImpl.this.getIndexedNomenclatures(_index);
  }
    public Collection getWeights() { return ContentsPGImpl.this.getWeights(); }
  public boolean inWeights(Mass _element) {
    return ContentsPGImpl.this.inWeights(_element);
  }
  public Mass[] getWeightsAsArray() {
    return ContentsPGImpl.this.getWeightsAsArray();
  }
  public Mass getIndexedWeights(int _index) {
    return ContentsPGImpl.this.getIndexedWeights(_index);
  }
    public Collection getReceivers() { return ContentsPGImpl.this.getReceivers(); }
  public boolean inReceivers(String _element) {
    return ContentsPGImpl.this.inReceivers(_element);
  }
  public String[] getReceiversAsArray() {
    return ContentsPGImpl.this.getReceiversAsArray();
  }
  public String getIndexedReceivers(int _index) {
    return ContentsPGImpl.this.getIndexedReceivers(_index);
  }
  public final boolean hasDataQuality() { return ContentsPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return ContentsPGImpl.this.getDataQuality(); }
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
      return ContentsPGImpl.class;
    }

  }

}
