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
/** Primary client interface for RoadLinkPG.
 *  @see NewRoadLinkPG
 *  @see RoadLinkPGImpl
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


public interface RoadLinkPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  int getDirection();
  boolean getInUrbanArea();
  int getNumberOfLanes();
  int getMaximumHeight();
  int getMaximumWidth();
  int getLinkID();
  int getStateCode();
  String getRoute1();
  String getRoute2();
  String getMedian();
  String getAccessType();
  int getTruckRoute();
  int getFunctionalClass();
  float getMaximumConvoySpeed();
  float getConvoyTravelTime();
  long getNumberOfBridgesUnderHS20();
  Speed getMaxSpeed();
  Capacity getMaxCapacity();
  Mass getMaxWeight();
  String getLinkName();
  Distance getLinkLength();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newRoadLinkPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewRoadLinkPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.RoadLinkPG.class;
  String assetSetter = "setRoadLinkPG";
  String assetGetter = "getRoadLinkPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  RoadLinkPG nullPG = new Null_RoadLinkPG();

/** Null_PG implementation for RoadLinkPG **/
final class Null_RoadLinkPG
  implements RoadLinkPG, Null_PG
{
  public int getDirection() { throw new UndefinedValueException(); }
  public boolean getInUrbanArea() { throw new UndefinedValueException(); }
  public int getNumberOfLanes() { throw new UndefinedValueException(); }
  public int getMaximumHeight() { throw new UndefinedValueException(); }
  public int getMaximumWidth() { throw new UndefinedValueException(); }
  public int getLinkID() { throw new UndefinedValueException(); }
  public int getStateCode() { throw new UndefinedValueException(); }
  public String getRoute1() { throw new UndefinedValueException(); }
  public String getRoute2() { throw new UndefinedValueException(); }
  public String getMedian() { throw new UndefinedValueException(); }
  public String getAccessType() { throw new UndefinedValueException(); }
  public int getTruckRoute() { throw new UndefinedValueException(); }
  public int getFunctionalClass() { throw new UndefinedValueException(); }
  public float getMaximumConvoySpeed() { throw new UndefinedValueException(); }
  public float getConvoyTravelTime() { throw new UndefinedValueException(); }
  public long getNumberOfBridgesUnderHS20() { throw new UndefinedValueException(); }
  public Speed getMaxSpeed() { throw new UndefinedValueException(); }
  public Capacity getMaxCapacity() { throw new UndefinedValueException(); }
  public Mass getMaxWeight() { throw new UndefinedValueException(); }
  public String getLinkName() { throw new UndefinedValueException(); }
  public Distance getLinkLength() { throw new UndefinedValueException(); }
  public boolean equals(Object object) { throw new UndefinedValueException(); }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return RoadLinkPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for RoadLinkPG **/
final class Future
  implements RoadLinkPG, Future_PG
{
  public int getDirection() {
    waitForFinalize();
    return _real.getDirection();
  }
  public boolean getInUrbanArea() {
    waitForFinalize();
    return _real.getInUrbanArea();
  }
  public int getNumberOfLanes() {
    waitForFinalize();
    return _real.getNumberOfLanes();
  }
  public int getMaximumHeight() {
    waitForFinalize();
    return _real.getMaximumHeight();
  }
  public int getMaximumWidth() {
    waitForFinalize();
    return _real.getMaximumWidth();
  }
  public int getLinkID() {
    waitForFinalize();
    return _real.getLinkID();
  }
  public int getStateCode() {
    waitForFinalize();
    return _real.getStateCode();
  }
  public String getRoute1() {
    waitForFinalize();
    return _real.getRoute1();
  }
  public String getRoute2() {
    waitForFinalize();
    return _real.getRoute2();
  }
  public String getMedian() {
    waitForFinalize();
    return _real.getMedian();
  }
  public String getAccessType() {
    waitForFinalize();
    return _real.getAccessType();
  }
  public int getTruckRoute() {
    waitForFinalize();
    return _real.getTruckRoute();
  }
  public int getFunctionalClass() {
    waitForFinalize();
    return _real.getFunctionalClass();
  }
  public float getMaximumConvoySpeed() {
    waitForFinalize();
    return _real.getMaximumConvoySpeed();
  }
  public float getConvoyTravelTime() {
    waitForFinalize();
    return _real.getConvoyTravelTime();
  }
  public long getNumberOfBridgesUnderHS20() {
    waitForFinalize();
    return _real.getNumberOfBridgesUnderHS20();
  }
  public Speed getMaxSpeed() {
    waitForFinalize();
    return _real.getMaxSpeed();
  }
  public Capacity getMaxCapacity() {
    waitForFinalize();
    return _real.getMaxCapacity();
  }
  public Mass getMaxWeight() {
    waitForFinalize();
    return _real.getMaxWeight();
  }
  public String getLinkName() {
    waitForFinalize();
    return _real.getLinkName();
  }
  public Distance getLinkLength() {
    waitForFinalize();
    return _real.getLinkLength();
  }
  public boolean equals(Object object) {
    waitForFinalize();
    return _real.equals(object);
  }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return RoadLinkPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private RoadLinkPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof RoadLinkPG) {
      _real=(RoadLinkPG) real;
      notifyAll();
    } else {
      throw new IllegalArgumentException("Finalization with wrong class: "+real);
    }
  }
  private synchronized void waitForFinalize() {
    while (_real == null) {
      try {
        wait();
      } catch (InterruptedException _ie) {
        // We should really let waitForFinalize throw InterruptedException
        Thread.interrupted();
      }
    }
  }
}
}
