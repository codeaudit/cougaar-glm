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
/** Implementation of DeckPG.
 *  @see DeckPG
 *  @see NewDeckPG
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

public class DeckPGImpl extends java.beans.SimpleBeanInfo
  implements NewDeckPG, Cloneable
{
  public DeckPGImpl() {
  }

  // Slots

  private int theLevel;
  public int getLevel(){ return theLevel; }
  public void setLevel(int level) {
    theLevel=level;
  }
  private String theShipId;
  public String getShipId(){ return theShipId; }
  public void setShipId(String ship_id) {
    theShipId=ship_id;
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
  private Volume theMaximumVolume;
  public Volume getMaximumVolume(){ return theMaximumVolume; }
  public void setMaximumVolume(Volume maximum_volume) {
    theMaximumVolume=maximum_volume;
  }
  private Area theMaximumFootprintArea;
  public Area getMaximumFootprintArea(){ return theMaximumFootprintArea; }
  public void setMaximumFootprintArea(Area maximum_footprint_area) {
    theMaximumFootprintArea=maximum_footprint_area;
  }
  private Mass theMaximumWeight;
  public Mass getMaximumWeight(){ return theMaximumWeight; }
  public void setMaximumWeight(Mass maximum_weight) {
    theMaximumWeight=maximum_weight;
  }
  private long theMaximumPassengers;
  public long getMaximumPassengers(){ return theMaximumPassengers; }
  public void setMaximumPassengers(long maximum_passengers) {
    theMaximumPassengers=maximum_passengers;
  }
  private boolean theRefrigeration;
  public boolean getRefrigeration(){ return theRefrigeration; }
  public void setRefrigeration(boolean refrigeration) {
    theRefrigeration=refrigeration;
  }
  private Duration theTimeToLoad;
  public Duration getTimeToLoad(){ return theTimeToLoad; }
  public void setTimeToLoad(Duration time_to_load) {
    theTimeToLoad=time_to_load;
  }
  private Duration theTimeToUnload;
  public Duration getTimeToUnload(){ return theTimeToUnload; }
  public void setTimeToUnload(Duration time_to_unload) {
    theTimeToUnload=time_to_unload;
  }
  private Duration theTimeToRefuel;
  public Duration getTimeToRefuel(){ return theTimeToRefuel; }
  public void setTimeToRefuel(Duration time_to_refuel) {
    theTimeToRefuel=time_to_refuel;
  }
  private long theMaximumContainers;
  public long getMaximumContainers(){ return theMaximumContainers; }
  public void setMaximumContainers(long maximum_containers) {
    theMaximumContainers=maximum_containers;
  }
  private boolean theIsPrepositioned;
  public boolean getIsPrepositioned(){ return theIsPrepositioned; }
  public void setIsPrepositioned(boolean is_prepositioned) {
    theIsPrepositioned=is_prepositioned;
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


  public DeckPGImpl(DeckPG original) {
    theLevel = original.getLevel();
    theShipId = original.getShipId();
    theMaximumLength = original.getMaximumLength();
    theMaximumWidth = original.getMaximumWidth();
    theMaximumHeight = original.getMaximumHeight();
    theMaximumVolume = original.getMaximumVolume();
    theMaximumFootprintArea = original.getMaximumFootprintArea();
    theMaximumWeight = original.getMaximumWeight();
    theMaximumPassengers = original.getMaximumPassengers();
    theRefrigeration = original.getRefrigeration();
    theTimeToLoad = original.getTimeToLoad();
    theTimeToUnload = original.getTimeToUnload();
    theTimeToRefuel = original.getTimeToRefuel();
    theMaximumContainers = original.getMaximumContainers();
    theIsPrepositioned = original.getIsPrepositioned();
    theCargoRestrictions = original.getCargoRestrictions();
    thePermittedCargoCategoryCodes = original.getPermittedCargoCategoryCodes();
  }

  public boolean equals(Object other) {

    if (!(other instanceof DeckPG)) {
      return false;
    }

    DeckPG otherDeckPG = (DeckPG) other;

    if (!(getLevel() == otherDeckPG.getLevel())) {
      return false;
    }

    if (getShipId() == null) {
      if (otherDeckPG.getShipId() != null) {
        return false;
      }
    } else if (!(getShipId().equals(otherDeckPG.getShipId()))) {
      return false;
    }

    if (getMaximumLength() == null) {
      if (otherDeckPG.getMaximumLength() != null) {
        return false;
      }
    } else if (!(getMaximumLength().equals(otherDeckPG.getMaximumLength()))) {
      return false;
    }

    if (getMaximumWidth() == null) {
      if (otherDeckPG.getMaximumWidth() != null) {
        return false;
      }
    } else if (!(getMaximumWidth().equals(otherDeckPG.getMaximumWidth()))) {
      return false;
    }

    if (getMaximumHeight() == null) {
      if (otherDeckPG.getMaximumHeight() != null) {
        return false;
      }
    } else if (!(getMaximumHeight().equals(otherDeckPG.getMaximumHeight()))) {
      return false;
    }

    if (getMaximumVolume() == null) {
      if (otherDeckPG.getMaximumVolume() != null) {
        return false;
      }
    } else if (!(getMaximumVolume().equals(otherDeckPG.getMaximumVolume()))) {
      return false;
    }

    if (getMaximumFootprintArea() == null) {
      if (otherDeckPG.getMaximumFootprintArea() != null) {
        return false;
      }
    } else if (!(getMaximumFootprintArea().equals(otherDeckPG.getMaximumFootprintArea()))) {
      return false;
    }

    if (getMaximumWeight() == null) {
      if (otherDeckPG.getMaximumWeight() != null) {
        return false;
      }
    } else if (!(getMaximumWeight().equals(otherDeckPG.getMaximumWeight()))) {
      return false;
    }

    if (!(getMaximumPassengers() == otherDeckPG.getMaximumPassengers())) {
      return false;
    }

    if (!(getRefrigeration() == otherDeckPG.getRefrigeration())) {
      return false;
    }

    if (getTimeToLoad() == null) {
      if (otherDeckPG.getTimeToLoad() != null) {
        return false;
      }
    } else if (!(getTimeToLoad().equals(otherDeckPG.getTimeToLoad()))) {
      return false;
    }

    if (getTimeToUnload() == null) {
      if (otherDeckPG.getTimeToUnload() != null) {
        return false;
      }
    } else if (!(getTimeToUnload().equals(otherDeckPG.getTimeToUnload()))) {
      return false;
    }

    if (getTimeToRefuel() == null) {
      if (otherDeckPG.getTimeToRefuel() != null) {
        return false;
      }
    } else if (!(getTimeToRefuel().equals(otherDeckPG.getTimeToRefuel()))) {
      return false;
    }

    if (!(getMaximumContainers() == otherDeckPG.getMaximumContainers())) {
      return false;
    }

    if (!(getIsPrepositioned() == otherDeckPG.getIsPrepositioned())) {
      return false;
    }

    if (getCargoRestrictions() == null) {
      if (otherDeckPG.getCargoRestrictions() != null) {
        return false;
      }
    } else if (!(getCargoRestrictions().equals(otherDeckPG.getCargoRestrictions()))) {
      return false;
    }

    if (getPermittedCargoCategoryCodes() == null) {
      if (otherDeckPG.getPermittedCargoCategoryCodes() != null) {
        return false;
      }
    } else if (!(getPermittedCargoCategoryCodes().equals(otherDeckPG.getPermittedCargoCategoryCodes()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends DeckPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(DeckPG original) {
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


  private transient DeckPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new DeckPGImpl(DeckPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[17];
  static {
    try {
      properties[0]= new PropertyDescriptor("level", DeckPG.class, "getLevel", null);
      properties[1]= new PropertyDescriptor("ship_id", DeckPG.class, "getShipId", null);
      properties[2]= new PropertyDescriptor("maximum_length", DeckPG.class, "getMaximumLength", null);
      properties[3]= new PropertyDescriptor("maximum_width", DeckPG.class, "getMaximumWidth", null);
      properties[4]= new PropertyDescriptor("maximum_height", DeckPG.class, "getMaximumHeight", null);
      properties[5]= new PropertyDescriptor("maximum_volume", DeckPG.class, "getMaximumVolume", null);
      properties[6]= new PropertyDescriptor("maximum_footprint_area", DeckPG.class, "getMaximumFootprintArea", null);
      properties[7]= new PropertyDescriptor("maximum_weight", DeckPG.class, "getMaximumWeight", null);
      properties[8]= new PropertyDescriptor("maximum_passengers", DeckPG.class, "getMaximumPassengers", null);
      properties[9]= new PropertyDescriptor("refrigeration", DeckPG.class, "getRefrigeration", null);
      properties[10]= new PropertyDescriptor("time_to_load", DeckPG.class, "getTimeToLoad", null);
      properties[11]= new PropertyDescriptor("time_to_unload", DeckPG.class, "getTimeToUnload", null);
      properties[12]= new PropertyDescriptor("time_to_refuel", DeckPG.class, "getTimeToRefuel", null);
      properties[13]= new PropertyDescriptor("maximum_containers", DeckPG.class, "getMaximumContainers", null);
      properties[14]= new PropertyDescriptor("is_prepositioned", DeckPG.class, "getIsPrepositioned", null);
      properties[15]= new PropertyDescriptor("cargo_restrictions", DeckPG.class, "getCargoRestrictions", null);
      properties[16]= new PropertyDescriptor("permitted_cargo_category_codes", DeckPG.class, "getPermittedCargoCategoryCodes", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(DeckPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements DeckPG, Cloneable, LockedPG
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
         return DeckPGImpl.this;
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
      return new DeckPGImpl(DeckPGImpl.this);
    }

    public boolean equals(Object object) { return DeckPGImpl.this.equals(object); }
    public int getLevel() { return DeckPGImpl.this.getLevel(); }
    public String getShipId() { return DeckPGImpl.this.getShipId(); }
    public Distance getMaximumLength() { return DeckPGImpl.this.getMaximumLength(); }
    public Distance getMaximumWidth() { return DeckPGImpl.this.getMaximumWidth(); }
    public Distance getMaximumHeight() { return DeckPGImpl.this.getMaximumHeight(); }
    public Volume getMaximumVolume() { return DeckPGImpl.this.getMaximumVolume(); }
    public Area getMaximumFootprintArea() { return DeckPGImpl.this.getMaximumFootprintArea(); }
    public Mass getMaximumWeight() { return DeckPGImpl.this.getMaximumWeight(); }
    public long getMaximumPassengers() { return DeckPGImpl.this.getMaximumPassengers(); }
    public boolean getRefrigeration() { return DeckPGImpl.this.getRefrigeration(); }
    public Duration getTimeToLoad() { return DeckPGImpl.this.getTimeToLoad(); }
    public Duration getTimeToUnload() { return DeckPGImpl.this.getTimeToUnload(); }
    public Duration getTimeToRefuel() { return DeckPGImpl.this.getTimeToRefuel(); }
    public long getMaximumContainers() { return DeckPGImpl.this.getMaximumContainers(); }
    public boolean getIsPrepositioned() { return DeckPGImpl.this.getIsPrepositioned(); }
    public String getCargoRestrictions() { return DeckPGImpl.this.getCargoRestrictions(); }
    public String getPermittedCargoCategoryCodes() { return DeckPGImpl.this.getPermittedCargoCategoryCodes(); }
  public final boolean hasDataQuality() { return DeckPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return DeckPGImpl.this.getDataQuality(); }
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
      return DeckPGImpl.class;
    }

  }

}
