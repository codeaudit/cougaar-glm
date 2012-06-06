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
/** Implementation of RailLinkPG.
 *  @see RailLinkPG
 *  @see NewRailLinkPG
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

public class RailLinkPGImpl extends java.beans.SimpleBeanInfo
  implements NewRailLinkPG, Cloneable
{
  public RailLinkPGImpl() {
  }

  // Slots

  private long theNumberOfTracks;
  public long getNumberOfTracks(){ return theNumberOfTracks; }
  public void setNumberOfTracks(long number_of_tracks) {
    theNumberOfTracks=number_of_tracks;
  }
  private Distance theTrackGauge;
  public Distance getTrackGauge(){ return theTrackGauge; }
  public void setTrackGauge(Distance track_gauge) {
    theTrackGauge=track_gauge;
  }
  private double theMaximumGradeD2O;
  public double getMaximumGradeD2O(){ return theMaximumGradeD2O; }
  public void setMaximumGradeD2O(double maximum_grade_D2O) {
    theMaximumGradeD2O=maximum_grade_D2O;
  }
  private double theMaximumGradeO2D;
  public double getMaximumGradeO2D(){ return theMaximumGradeO2D; }
  public void setMaximumGradeO2D(double maximum_grade_O2D) {
    theMaximumGradeO2D=maximum_grade_O2D;
  }
  private Mass theMaximumAxleWeight;
  public Mass getMaximumAxleWeight(){ return theMaximumAxleWeight; }
  public void setMaximumAxleWeight(Mass maximum_axle_weight) {
    theMaximumAxleWeight=maximum_axle_weight;
  }
  private Distance theMaximumCarHeight;
  public Distance getMaximumCarHeight(){ return theMaximumCarHeight; }
  public void setMaximumCarHeight(Distance maximum_car_height) {
    theMaximumCarHeight=maximum_car_height;
  }
  private Distance theMaximumCarWidth;
  public Distance getMaximumCarWidth(){ return theMaximumCarWidth; }
  public void setMaximumCarWidth(Distance maximum_car_width) {
    theMaximumCarWidth=maximum_car_width;
  }
  private Distance theMaximumCarLength;
  public Distance getMaximumCarLength(){ return theMaximumCarLength; }
  public void setMaximumCarLength(Distance maximum_car_length) {
    theMaximumCarLength=maximum_car_length;
  }
  private Distance theMaximumTrainLength;
  public Distance getMaximumTrainLength(){ return theMaximumTrainLength; }
  public void setMaximumTrainLength(Distance maximum_train_length) {
    theMaximumTrainLength=maximum_train_length;
  }
  private Speed theMaximumTrainSpeed;
  public Speed getMaximumTrainSpeed(){ return theMaximumTrainSpeed; }
  public void setMaximumTrainSpeed(Speed maximum_train_speed) {
    theMaximumTrainSpeed=maximum_train_speed;
  }
  private String theLinkName;
  public String getLinkName(){ return theLinkName; }
  public void setLinkName(String link_name) {
    theLinkName=link_name;
  }
  private Distance theLinkLength;
  public Distance getLinkLength(){ return theLinkLength; }
  public void setLinkLength(Distance link_length) {
    theLinkLength=link_length;
  }


  public RailLinkPGImpl(RailLinkPG original) {
    theNumberOfTracks = original.getNumberOfTracks();
    theTrackGauge = original.getTrackGauge();
    theMaximumGradeD2O = original.getMaximumGradeD2O();
    theMaximumGradeO2D = original.getMaximumGradeO2D();
    theMaximumAxleWeight = original.getMaximumAxleWeight();
    theMaximumCarHeight = original.getMaximumCarHeight();
    theMaximumCarWidth = original.getMaximumCarWidth();
    theMaximumCarLength = original.getMaximumCarLength();
    theMaximumTrainLength = original.getMaximumTrainLength();
    theMaximumTrainSpeed = original.getMaximumTrainSpeed();
    theLinkName = original.getLinkName();
    theLinkLength = original.getLinkLength();
  }

  public boolean equals(Object other) {

    if (!(other instanceof RailLinkPG)) {
      return false;
    }

    RailLinkPG otherRailLinkPG = (RailLinkPG) other;

    if (!(getNumberOfTracks() == otherRailLinkPG.getNumberOfTracks())) {
      return false;
    }

    if (getTrackGauge() == null) {
      if (otherRailLinkPG.getTrackGauge() != null) {
        return false;
      }
    } else if (!(getTrackGauge().equals(otherRailLinkPG.getTrackGauge()))) {
      return false;
    }

    if (!(getMaximumGradeD2O() == otherRailLinkPG.getMaximumGradeD2O())) {
      return false;
    }

    if (!(getMaximumGradeO2D() == otherRailLinkPG.getMaximumGradeO2D())) {
      return false;
    }

    if (getMaximumAxleWeight() == null) {
      if (otherRailLinkPG.getMaximumAxleWeight() != null) {
        return false;
      }
    } else if (!(getMaximumAxleWeight().equals(otherRailLinkPG.getMaximumAxleWeight()))) {
      return false;
    }

    if (getMaximumCarHeight() == null) {
      if (otherRailLinkPG.getMaximumCarHeight() != null) {
        return false;
      }
    } else if (!(getMaximumCarHeight().equals(otherRailLinkPG.getMaximumCarHeight()))) {
      return false;
    }

    if (getMaximumCarWidth() == null) {
      if (otherRailLinkPG.getMaximumCarWidth() != null) {
        return false;
      }
    } else if (!(getMaximumCarWidth().equals(otherRailLinkPG.getMaximumCarWidth()))) {
      return false;
    }

    if (getMaximumCarLength() == null) {
      if (otherRailLinkPG.getMaximumCarLength() != null) {
        return false;
      }
    } else if (!(getMaximumCarLength().equals(otherRailLinkPG.getMaximumCarLength()))) {
      return false;
    }

    if (getMaximumTrainLength() == null) {
      if (otherRailLinkPG.getMaximumTrainLength() != null) {
        return false;
      }
    } else if (!(getMaximumTrainLength().equals(otherRailLinkPG.getMaximumTrainLength()))) {
      return false;
    }

    if (getMaximumTrainSpeed() == null) {
      if (otherRailLinkPG.getMaximumTrainSpeed() != null) {
        return false;
      }
    } else if (!(getMaximumTrainSpeed().equals(otherRailLinkPG.getMaximumTrainSpeed()))) {
      return false;
    }

    if (getLinkName() == null) {
      if (otherRailLinkPG.getLinkName() != null) {
        return false;
      }
    } else if (!(getLinkName().equals(otherRailLinkPG.getLinkName()))) {
      return false;
    }

    if (getLinkLength() == null) {
      if (otherRailLinkPG.getLinkLength() != null) {
        return false;
      }
    } else if (!(getLinkLength().equals(otherRailLinkPG.getLinkLength()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends RailLinkPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(RailLinkPG original) {
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


  private transient RailLinkPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new RailLinkPGImpl(RailLinkPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[12];
  static {
    try {
      properties[0]= new PropertyDescriptor("number_of_tracks", RailLinkPG.class, "getNumberOfTracks", null);
      properties[1]= new PropertyDescriptor("track_gauge", RailLinkPG.class, "getTrackGauge", null);
      properties[2]= new PropertyDescriptor("maximum_grade_D2O", RailLinkPG.class, "getMaximumGradeD2O", null);
      properties[3]= new PropertyDescriptor("maximum_grade_O2D", RailLinkPG.class, "getMaximumGradeO2D", null);
      properties[4]= new PropertyDescriptor("maximum_axle_weight", RailLinkPG.class, "getMaximumAxleWeight", null);
      properties[5]= new PropertyDescriptor("maximum_car_height", RailLinkPG.class, "getMaximumCarHeight", null);
      properties[6]= new PropertyDescriptor("maximum_car_width", RailLinkPG.class, "getMaximumCarWidth", null);
      properties[7]= new PropertyDescriptor("maximum_car_length", RailLinkPG.class, "getMaximumCarLength", null);
      properties[8]= new PropertyDescriptor("maximum_train_length", RailLinkPG.class, "getMaximumTrainLength", null);
      properties[9]= new PropertyDescriptor("maximum_train_speed", RailLinkPG.class, "getMaximumTrainSpeed", null);
      properties[10]= new PropertyDescriptor("link_name", RailLinkPG.class, "getLinkName", null);
      properties[11]= new PropertyDescriptor("link_length", RailLinkPG.class, "getLinkLength", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(RailLinkPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements RailLinkPG, Cloneable, LockedPG
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
         return RailLinkPGImpl.this;
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
      return new RailLinkPGImpl(RailLinkPGImpl.this);
    }

    public boolean equals(Object object) { return RailLinkPGImpl.this.equals(object); }
    public long getNumberOfTracks() { return RailLinkPGImpl.this.getNumberOfTracks(); }
    public Distance getTrackGauge() { return RailLinkPGImpl.this.getTrackGauge(); }
    public double getMaximumGradeD2O() { return RailLinkPGImpl.this.getMaximumGradeD2O(); }
    public double getMaximumGradeO2D() { return RailLinkPGImpl.this.getMaximumGradeO2D(); }
    public Mass getMaximumAxleWeight() { return RailLinkPGImpl.this.getMaximumAxleWeight(); }
    public Distance getMaximumCarHeight() { return RailLinkPGImpl.this.getMaximumCarHeight(); }
    public Distance getMaximumCarWidth() { return RailLinkPGImpl.this.getMaximumCarWidth(); }
    public Distance getMaximumCarLength() { return RailLinkPGImpl.this.getMaximumCarLength(); }
    public Distance getMaximumTrainLength() { return RailLinkPGImpl.this.getMaximumTrainLength(); }
    public Speed getMaximumTrainSpeed() { return RailLinkPGImpl.this.getMaximumTrainSpeed(); }
    public String getLinkName() { return RailLinkPGImpl.this.getLinkName(); }
    public Distance getLinkLength() { return RailLinkPGImpl.this.getLinkLength(); }
  public final boolean hasDataQuality() { return RailLinkPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return RailLinkPGImpl.this.getDataQuality(); }
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
      return RailLinkPGImpl.class;
    }

  }

}
