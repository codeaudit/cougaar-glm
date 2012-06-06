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
/** Primary client interface for AirSelfPropulsionPG.
 *  @see NewAirSelfPropulsionPG
 *  @see AirSelfPropulsionPGImpl
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


public interface AirSelfPropulsionPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** Maximum altitude **/
  Distance getMaximumAltitude();
  /** Cruise altitude **/
  Distance getCruiseAltitude();
  /** Average fuel consumption rate when in flight **/
  FlowRate getFuelConsumptionRate();
  String getEngineType();
  String getFuelType();
  Speed getMaximumSpeed();
  Speed getCruiseSpeed();
  Distance getFullPayloadRange();
  Distance getEmptyPayloadRange();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newAirSelfPropulsionPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewAirSelfPropulsionPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.AirSelfPropulsionPG.class;
  String assetSetter = "setAirSelfPropulsionPG";
  String assetGetter = "getAirSelfPropulsionPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  AirSelfPropulsionPG nullPG = new Null_AirSelfPropulsionPG();

/** Null_PG implementation for AirSelfPropulsionPG **/
final class Null_AirSelfPropulsionPG
  implements AirSelfPropulsionPG, Null_PG
{
  public Distance getMaximumAltitude() { throw new UndefinedValueException(); }
  public Distance getCruiseAltitude() { throw new UndefinedValueException(); }
  public FlowRate getFuelConsumptionRate() { throw new UndefinedValueException(); }
  public String getEngineType() { throw new UndefinedValueException(); }
  public String getFuelType() { throw new UndefinedValueException(); }
  public Speed getMaximumSpeed() { throw new UndefinedValueException(); }
  public Speed getCruiseSpeed() { throw new UndefinedValueException(); }
  public Distance getFullPayloadRange() { throw new UndefinedValueException(); }
  public Distance getEmptyPayloadRange() { throw new UndefinedValueException(); }
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
    return AirSelfPropulsionPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for AirSelfPropulsionPG **/
final class Future
  implements AirSelfPropulsionPG, Future_PG
{
  public Distance getMaximumAltitude() {
    waitForFinalize();
    return _real.getMaximumAltitude();
  }
  public Distance getCruiseAltitude() {
    waitForFinalize();
    return _real.getCruiseAltitude();
  }
  public FlowRate getFuelConsumptionRate() {
    waitForFinalize();
    return _real.getFuelConsumptionRate();
  }
  public String getEngineType() {
    waitForFinalize();
    return _real.getEngineType();
  }
  public String getFuelType() {
    waitForFinalize();
    return _real.getFuelType();
  }
  public Speed getMaximumSpeed() {
    waitForFinalize();
    return _real.getMaximumSpeed();
  }
  public Speed getCruiseSpeed() {
    waitForFinalize();
    return _real.getCruiseSpeed();
  }
  public Distance getFullPayloadRange() {
    waitForFinalize();
    return _real.getFullPayloadRange();
  }
  public Distance getEmptyPayloadRange() {
    waitForFinalize();
    return _real.getEmptyPayloadRange();
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
    return AirSelfPropulsionPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private AirSelfPropulsionPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof AirSelfPropulsionPG) {
      _real=(AirSelfPropulsionPG) real;
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
