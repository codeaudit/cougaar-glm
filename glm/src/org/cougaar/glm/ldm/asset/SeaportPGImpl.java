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
/** Implementation of SeaportPG.
 *  @see SeaportPG
 *  @see NewSeaportPG
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

public class SeaportPGImpl extends java.beans.SimpleBeanInfo
  implements NewSeaportPG, Cloneable
{
  public SeaportPGImpl() {
  }

  // Slots

  private long theNumberOfROROBerths;
  public long getNumberOfROROBerths(){ return theNumberOfROROBerths; }
  public void setNumberOfROROBerths(long number_of_RORO_berths) {
    theNumberOfROROBerths=number_of_RORO_berths;
  }
  private long theNumberOfContainerBerths;
  public long getNumberOfContainerBerths(){ return theNumberOfContainerBerths; }
  public void setNumberOfContainerBerths(long number_of_container_berths) {
    theNumberOfContainerBerths=number_of_container_berths;
  }
  private Distance thePierLength;
  public Distance getPierLength(){ return thePierLength; }
  public void setPierLength(Distance pier_length) {
    thePierLength=pier_length;
  }
  private Distance thePierWidth;
  public Distance getPierWidth(){ return thePierWidth; }
  public void setPierWidth(Distance pier_width) {
    thePierWidth=pier_width;
  }
  private Distance thePierDepth;
  public Distance getPierDepth(){ return thePierDepth; }
  public void setPierDepth(Distance pier_depth) {
    thePierDepth=pier_depth;
  }
  private Distance theSupportedTurningRadius;
  public Distance getSupportedTurningRadius(){ return theSupportedTurningRadius; }
  public void setSupportedTurningRadius(Distance supported_turning_radius) {
    theSupportedTurningRadius=supported_turning_radius;
  }
  private Distance theHighTideDraft;
  public Distance getHighTideDraft(){ return theHighTideDraft; }
  public void setHighTideDraft(Distance high_tide_draft) {
    theHighTideDraft=high_tide_draft;
  }
  private Distance theLowTideDraft;
  public Distance getLowTideDraft(){ return theLowTideDraft; }
  public void setLowTideDraft(Distance low_tide_draft) {
    theLowTideDraft=low_tide_draft;
  }
  private long theNumberOfContainerCranes;
  public long getNumberOfContainerCranes(){ return theNumberOfContainerCranes; }
  public void setNumberOfContainerCranes(long number_of_container_cranes) {
    theNumberOfContainerCranes=number_of_container_cranes;
  }


  public SeaportPGImpl(SeaportPG original) {
    theNumberOfROROBerths = original.getNumberOfROROBerths();
    theNumberOfContainerBerths = original.getNumberOfContainerBerths();
    thePierLength = original.getPierLength();
    thePierWidth = original.getPierWidth();
    thePierDepth = original.getPierDepth();
    theSupportedTurningRadius = original.getSupportedTurningRadius();
    theHighTideDraft = original.getHighTideDraft();
    theLowTideDraft = original.getLowTideDraft();
    theNumberOfContainerCranes = original.getNumberOfContainerCranes();
  }

  public boolean equals(Object other) {

    if (!(other instanceof SeaportPG)) {
      return false;
    }

    SeaportPG otherSeaportPG = (SeaportPG) other;

    if (!(getNumberOfROROBerths() == otherSeaportPG.getNumberOfROROBerths())) {
      return false;
    }

    if (!(getNumberOfContainerBerths() == otherSeaportPG.getNumberOfContainerBerths())) {
      return false;
    }

    if (getPierLength() == null) {
      if (otherSeaportPG.getPierLength() != null) {
        return false;
      }
    } else if (!(getPierLength().equals(otherSeaportPG.getPierLength()))) {
      return false;
    }

    if (getPierWidth() == null) {
      if (otherSeaportPG.getPierWidth() != null) {
        return false;
      }
    } else if (!(getPierWidth().equals(otherSeaportPG.getPierWidth()))) {
      return false;
    }

    if (getPierDepth() == null) {
      if (otherSeaportPG.getPierDepth() != null) {
        return false;
      }
    } else if (!(getPierDepth().equals(otherSeaportPG.getPierDepth()))) {
      return false;
    }

    if (getSupportedTurningRadius() == null) {
      if (otherSeaportPG.getSupportedTurningRadius() != null) {
        return false;
      }
    } else if (!(getSupportedTurningRadius().equals(otherSeaportPG.getSupportedTurningRadius()))) {
      return false;
    }

    if (getHighTideDraft() == null) {
      if (otherSeaportPG.getHighTideDraft() != null) {
        return false;
      }
    } else if (!(getHighTideDraft().equals(otherSeaportPG.getHighTideDraft()))) {
      return false;
    }

    if (getLowTideDraft() == null) {
      if (otherSeaportPG.getLowTideDraft() != null) {
        return false;
      }
    } else if (!(getLowTideDraft().equals(otherSeaportPG.getLowTideDraft()))) {
      return false;
    }

    if (!(getNumberOfContainerCranes() == otherSeaportPG.getNumberOfContainerCranes())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends SeaportPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(SeaportPG original) {
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


  private transient SeaportPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new SeaportPGImpl(SeaportPGImpl.this);
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
      properties[0]= new PropertyDescriptor("number_of_RORO_berths", SeaportPG.class, "getNumberOfROROBerths", null);
      properties[1]= new PropertyDescriptor("number_of_container_berths", SeaportPG.class, "getNumberOfContainerBerths", null);
      properties[2]= new PropertyDescriptor("pier_length", SeaportPG.class, "getPierLength", null);
      properties[3]= new PropertyDescriptor("pier_width", SeaportPG.class, "getPierWidth", null);
      properties[4]= new PropertyDescriptor("pier_depth", SeaportPG.class, "getPierDepth", null);
      properties[5]= new PropertyDescriptor("supported_turning_radius", SeaportPG.class, "getSupportedTurningRadius", null);
      properties[6]= new PropertyDescriptor("high_tide_draft", SeaportPG.class, "getHighTideDraft", null);
      properties[7]= new PropertyDescriptor("low_tide_draft", SeaportPG.class, "getLowTideDraft", null);
      properties[8]= new PropertyDescriptor("number_of_container_cranes", SeaportPG.class, "getNumberOfContainerCranes", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(SeaportPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements SeaportPG, Cloneable, LockedPG
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
         return SeaportPGImpl.this;
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
      return new SeaportPGImpl(SeaportPGImpl.this);
    }

    public boolean equals(Object object) { return SeaportPGImpl.this.equals(object); }
    public long getNumberOfROROBerths() { return SeaportPGImpl.this.getNumberOfROROBerths(); }
    public long getNumberOfContainerBerths() { return SeaportPGImpl.this.getNumberOfContainerBerths(); }
    public Distance getPierLength() { return SeaportPGImpl.this.getPierLength(); }
    public Distance getPierWidth() { return SeaportPGImpl.this.getPierWidth(); }
    public Distance getPierDepth() { return SeaportPGImpl.this.getPierDepth(); }
    public Distance getSupportedTurningRadius() { return SeaportPGImpl.this.getSupportedTurningRadius(); }
    public Distance getHighTideDraft() { return SeaportPGImpl.this.getHighTideDraft(); }
    public Distance getLowTideDraft() { return SeaportPGImpl.this.getLowTideDraft(); }
    public long getNumberOfContainerCranes() { return SeaportPGImpl.this.getNumberOfContainerCranes(); }
  public final boolean hasDataQuality() { return SeaportPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return SeaportPGImpl.this.getDataQuality(); }
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
      return SeaportPGImpl.class;
    }

  }

}
