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
/** Implementation of RoadLinkPG.
 *  @see RoadLinkPG
 *  @see NewRoadLinkPG
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

public class RoadLinkPGImpl extends java.beans.SimpleBeanInfo
  implements NewRoadLinkPG, Cloneable
{
  public RoadLinkPGImpl() {
  }

  // Slots

  private int theDirection;
  public int getDirection(){ return theDirection; }
  public void setDirection(int direction) {
    theDirection=direction;
  }
  private boolean theInUrbanArea;
  public boolean getInUrbanArea(){ return theInUrbanArea; }
  public void setInUrbanArea(boolean in_urban_area) {
    theInUrbanArea=in_urban_area;
  }
  private int theNumberOfLanes;
  public int getNumberOfLanes(){ return theNumberOfLanes; }
  public void setNumberOfLanes(int number_of_lanes) {
    theNumberOfLanes=number_of_lanes;
  }
  private int theMaximumHeight;
  public int getMaximumHeight(){ return theMaximumHeight; }
  public void setMaximumHeight(int maximum_height) {
    theMaximumHeight=maximum_height;
  }
  private int theMaximumWidth;
  public int getMaximumWidth(){ return theMaximumWidth; }
  public void setMaximumWidth(int maximum_width) {
    theMaximumWidth=maximum_width;
  }
  private int theLinkID;
  public int getLinkID(){ return theLinkID; }
  public void setLinkID(int link_ID) {
    theLinkID=link_ID;
  }
  private int theStateCode;
  public int getStateCode(){ return theStateCode; }
  public void setStateCode(int state_code) {
    theStateCode=state_code;
  }
  private String theRoute1;
  public String getRoute1(){ return theRoute1; }
  public void setRoute1(String route_1) {
    theRoute1=route_1;
  }
  private String theRoute2;
  public String getRoute2(){ return theRoute2; }
  public void setRoute2(String route_2) {
    theRoute2=route_2;
  }
  private String theMedian;
  public String getMedian(){ return theMedian; }
  public void setMedian(String median) {
    theMedian=median;
  }
  private String theAccessType;
  public String getAccessType(){ return theAccessType; }
  public void setAccessType(String access_type) {
    theAccessType=access_type;
  }
  private int theTruckRoute;
  public int getTruckRoute(){ return theTruckRoute; }
  public void setTruckRoute(int truck_route) {
    theTruckRoute=truck_route;
  }
  private int theFunctionalClass;
  public int getFunctionalClass(){ return theFunctionalClass; }
  public void setFunctionalClass(int functional_class) {
    theFunctionalClass=functional_class;
  }
  private float theMaximumConvoySpeed;
  public float getMaximumConvoySpeed(){ return theMaximumConvoySpeed; }
  public void setMaximumConvoySpeed(float maximum_convoy_speed) {
    theMaximumConvoySpeed=maximum_convoy_speed;
  }
  private float theConvoyTravelTime;
  public float getConvoyTravelTime(){ return theConvoyTravelTime; }
  public void setConvoyTravelTime(float convoy_travel_time) {
    theConvoyTravelTime=convoy_travel_time;
  }
  private long theNumberOfBridgesUnderHS20;
  public long getNumberOfBridgesUnderHS20(){ return theNumberOfBridgesUnderHS20; }
  public void setNumberOfBridgesUnderHS20(long number_of_bridges_under_HS20) {
    theNumberOfBridgesUnderHS20=number_of_bridges_under_HS20;
  }
  private Speed theMaxSpeed;
  public Speed getMaxSpeed(){ return theMaxSpeed; }
  public void setMaxSpeed(Speed max_speed) {
    theMaxSpeed=max_speed;
  }
  private Capacity theMaxCapacity;
  public Capacity getMaxCapacity(){ return theMaxCapacity; }
  public void setMaxCapacity(Capacity max_capacity) {
    theMaxCapacity=max_capacity;
  }
  private Mass theMaxWeight;
  public Mass getMaxWeight(){ return theMaxWeight; }
  public void setMaxWeight(Mass max_weight) {
    theMaxWeight=max_weight;
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


  public RoadLinkPGImpl(RoadLinkPG original) {
    theDirection = original.getDirection();
    theInUrbanArea = original.getInUrbanArea();
    theNumberOfLanes = original.getNumberOfLanes();
    theMaximumHeight = original.getMaximumHeight();
    theMaximumWidth = original.getMaximumWidth();
    theLinkID = original.getLinkID();
    theStateCode = original.getStateCode();
    theRoute1 = original.getRoute1();
    theRoute2 = original.getRoute2();
    theMedian = original.getMedian();
    theAccessType = original.getAccessType();
    theTruckRoute = original.getTruckRoute();
    theFunctionalClass = original.getFunctionalClass();
    theMaximumConvoySpeed = original.getMaximumConvoySpeed();
    theConvoyTravelTime = original.getConvoyTravelTime();
    theNumberOfBridgesUnderHS20 = original.getNumberOfBridgesUnderHS20();
    theMaxSpeed = original.getMaxSpeed();
    theMaxCapacity = original.getMaxCapacity();
    theMaxWeight = original.getMaxWeight();
    theLinkName = original.getLinkName();
    theLinkLength = original.getLinkLength();
  }

  public boolean equals(Object other) {

    if (!(other instanceof RoadLinkPG)) {
      return false;
    }

    RoadLinkPG otherRoadLinkPG = (RoadLinkPG) other;

    if (!(getDirection() == otherRoadLinkPG.getDirection())) {
      return false;
    }

    if (!(getInUrbanArea() == otherRoadLinkPG.getInUrbanArea())) {
      return false;
    }

    if (!(getNumberOfLanes() == otherRoadLinkPG.getNumberOfLanes())) {
      return false;
    }

    if (!(getMaximumHeight() == otherRoadLinkPG.getMaximumHeight())) {
      return false;
    }

    if (!(getMaximumWidth() == otherRoadLinkPG.getMaximumWidth())) {
      return false;
    }

    if (!(getLinkID() == otherRoadLinkPG.getLinkID())) {
      return false;
    }

    if (!(getStateCode() == otherRoadLinkPG.getStateCode())) {
      return false;
    }

    if (getRoute1() == null) {
      if (otherRoadLinkPG.getRoute1() != null) {
        return false;
      }
    } else if (!(getRoute1().equals(otherRoadLinkPG.getRoute1()))) {
      return false;
    }

    if (getRoute2() == null) {
      if (otherRoadLinkPG.getRoute2() != null) {
        return false;
      }
    } else if (!(getRoute2().equals(otherRoadLinkPG.getRoute2()))) {
      return false;
    }

    if (getMedian() == null) {
      if (otherRoadLinkPG.getMedian() != null) {
        return false;
      }
    } else if (!(getMedian().equals(otherRoadLinkPG.getMedian()))) {
      return false;
    }

    if (getAccessType() == null) {
      if (otherRoadLinkPG.getAccessType() != null) {
        return false;
      }
    } else if (!(getAccessType().equals(otherRoadLinkPG.getAccessType()))) {
      return false;
    }

    if (!(getTruckRoute() == otherRoadLinkPG.getTruckRoute())) {
      return false;
    }

    if (!(getFunctionalClass() == otherRoadLinkPG.getFunctionalClass())) {
      return false;
    }

    if (!(getMaximumConvoySpeed() == otherRoadLinkPG.getMaximumConvoySpeed())) {
      return false;
    }

    if (!(getConvoyTravelTime() == otherRoadLinkPG.getConvoyTravelTime())) {
      return false;
    }

    if (!(getNumberOfBridgesUnderHS20() == otherRoadLinkPG.getNumberOfBridgesUnderHS20())) {
      return false;
    }

    if (getMaxSpeed() == null) {
      if (otherRoadLinkPG.getMaxSpeed() != null) {
        return false;
      }
    } else if (!(getMaxSpeed().equals(otherRoadLinkPG.getMaxSpeed()))) {
      return false;
    }

    if (getMaxCapacity() == null) {
      if (otherRoadLinkPG.getMaxCapacity() != null) {
        return false;
      }
    } else if (!(getMaxCapacity().equals(otherRoadLinkPG.getMaxCapacity()))) {
      return false;
    }

    if (getMaxWeight() == null) {
      if (otherRoadLinkPG.getMaxWeight() != null) {
        return false;
      }
    } else if (!(getMaxWeight().equals(otherRoadLinkPG.getMaxWeight()))) {
      return false;
    }

    if (getLinkName() == null) {
      if (otherRoadLinkPG.getLinkName() != null) {
        return false;
      }
    } else if (!(getLinkName().equals(otherRoadLinkPG.getLinkName()))) {
      return false;
    }

    if (getLinkLength() == null) {
      if (otherRoadLinkPG.getLinkLength() != null) {
        return false;
      }
    } else if (!(getLinkLength().equals(otherRoadLinkPG.getLinkLength()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends RoadLinkPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(RoadLinkPG original) {
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


  private transient RoadLinkPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new RoadLinkPGImpl(RoadLinkPGImpl.this);
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[21];
  static {
    try {
      properties[0]= new PropertyDescriptor("direction", RoadLinkPG.class, "getDirection", null);
      properties[1]= new PropertyDescriptor("in_urban_area", RoadLinkPG.class, "getInUrbanArea", null);
      properties[2]= new PropertyDescriptor("number_of_lanes", RoadLinkPG.class, "getNumberOfLanes", null);
      properties[3]= new PropertyDescriptor("maximum_height", RoadLinkPG.class, "getMaximumHeight", null);
      properties[4]= new PropertyDescriptor("maximum_width", RoadLinkPG.class, "getMaximumWidth", null);
      properties[5]= new PropertyDescriptor("link_ID", RoadLinkPG.class, "getLinkID", null);
      properties[6]= new PropertyDescriptor("state_code", RoadLinkPG.class, "getStateCode", null);
      properties[7]= new PropertyDescriptor("route_1", RoadLinkPG.class, "getRoute1", null);
      properties[8]= new PropertyDescriptor("route_2", RoadLinkPG.class, "getRoute2", null);
      properties[9]= new PropertyDescriptor("median", RoadLinkPG.class, "getMedian", null);
      properties[10]= new PropertyDescriptor("access_type", RoadLinkPG.class, "getAccessType", null);
      properties[11]= new PropertyDescriptor("truck_route", RoadLinkPG.class, "getTruckRoute", null);
      properties[12]= new PropertyDescriptor("functional_class", RoadLinkPG.class, "getFunctionalClass", null);
      properties[13]= new PropertyDescriptor("maximum_convoy_speed", RoadLinkPG.class, "getMaximumConvoySpeed", null);
      properties[14]= new PropertyDescriptor("convoy_travel_time", RoadLinkPG.class, "getConvoyTravelTime", null);
      properties[15]= new PropertyDescriptor("number_of_bridges_under_HS20", RoadLinkPG.class, "getNumberOfBridgesUnderHS20", null);
      properties[16]= new PropertyDescriptor("max_speed", RoadLinkPG.class, "getMaxSpeed", null);
      properties[17]= new PropertyDescriptor("max_capacity", RoadLinkPG.class, "getMaxCapacity", null);
      properties[18]= new PropertyDescriptor("max_weight", RoadLinkPG.class, "getMaxWeight", null);
      properties[19]= new PropertyDescriptor("link_name", RoadLinkPG.class, "getLinkName", null);
      properties[20]= new PropertyDescriptor("link_length", RoadLinkPG.class, "getLinkLength", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(RoadLinkPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements RoadLinkPG, Cloneable, LockedPG
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
         return RoadLinkPGImpl.this;
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
      return new RoadLinkPGImpl(RoadLinkPGImpl.this);
    }

    public boolean equals(Object object) { return RoadLinkPGImpl.this.equals(object); }
    public int getDirection() { return RoadLinkPGImpl.this.getDirection(); }
    public boolean getInUrbanArea() { return RoadLinkPGImpl.this.getInUrbanArea(); }
    public int getNumberOfLanes() { return RoadLinkPGImpl.this.getNumberOfLanes(); }
    public int getMaximumHeight() { return RoadLinkPGImpl.this.getMaximumHeight(); }
    public int getMaximumWidth() { return RoadLinkPGImpl.this.getMaximumWidth(); }
    public int getLinkID() { return RoadLinkPGImpl.this.getLinkID(); }
    public int getStateCode() { return RoadLinkPGImpl.this.getStateCode(); }
    public String getRoute1() { return RoadLinkPGImpl.this.getRoute1(); }
    public String getRoute2() { return RoadLinkPGImpl.this.getRoute2(); }
    public String getMedian() { return RoadLinkPGImpl.this.getMedian(); }
    public String getAccessType() { return RoadLinkPGImpl.this.getAccessType(); }
    public int getTruckRoute() { return RoadLinkPGImpl.this.getTruckRoute(); }
    public int getFunctionalClass() { return RoadLinkPGImpl.this.getFunctionalClass(); }
    public float getMaximumConvoySpeed() { return RoadLinkPGImpl.this.getMaximumConvoySpeed(); }
    public float getConvoyTravelTime() { return RoadLinkPGImpl.this.getConvoyTravelTime(); }
    public long getNumberOfBridgesUnderHS20() { return RoadLinkPGImpl.this.getNumberOfBridgesUnderHS20(); }
    public Speed getMaxSpeed() { return RoadLinkPGImpl.this.getMaxSpeed(); }
    public Capacity getMaxCapacity() { return RoadLinkPGImpl.this.getMaxCapacity(); }
    public Mass getMaxWeight() { return RoadLinkPGImpl.this.getMaxWeight(); }
    public String getLinkName() { return RoadLinkPGImpl.this.getLinkName(); }
    public Distance getLinkLength() { return RoadLinkPGImpl.this.getLinkLength(); }
  public final boolean hasDataQuality() { return RoadLinkPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return RoadLinkPGImpl.this.getDataQuality(); }
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
      return RoadLinkPGImpl.class;
    }

  }

}
