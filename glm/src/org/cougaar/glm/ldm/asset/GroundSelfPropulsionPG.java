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
/** Primary client interface for GroundSelfPropulsionPG.
 *  @see NewGroundSelfPropulsionPG
 *  @see GroundSelfPropulsionPGImpl
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


public interface GroundSelfPropulsionPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** Type of traction (wheeled, tracked, half-tracked) **/
  String getTractionType();
  /** Average fuel consumption per mile (eg: gallons per mile) **/
  Volume getFuelConsumptionPerMile();
  String getEngineType();
  String getFuelType();
  Speed getMaximumSpeed();
  Speed getCruiseSpeed();
  Distance getFullPayloadRange();
  Distance getEmptyPayloadRange();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newGroundSelfPropulsionPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewGroundSelfPropulsionPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.GroundSelfPropulsionPG.class;
  String assetSetter = "setGroundSelfPropulsionPG";
  String assetGetter = "getGroundSelfPropulsionPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  GroundSelfPropulsionPG nullPG = new Null_GroundSelfPropulsionPG();

/** Null_PG implementation for GroundSelfPropulsionPG **/
final class Null_GroundSelfPropulsionPG
  implements GroundSelfPropulsionPG, Null_PG
{
  public String getTractionType() { throw new UndefinedValueException(); }
  public Volume getFuelConsumptionPerMile() { throw new UndefinedValueException(); }
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
    return GroundSelfPropulsionPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for GroundSelfPropulsionPG **/
final class Future
  implements GroundSelfPropulsionPG, Future_PG
{
  public String getTractionType() {
    waitForFinalize();
    return _real.getTractionType();
  }
  public Volume getFuelConsumptionPerMile() {
    waitForFinalize();
    return _real.getFuelConsumptionPerMile();
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
    return GroundSelfPropulsionPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private GroundSelfPropulsionPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof GroundSelfPropulsionPG) {
      _real=(GroundSelfPropulsionPG) real;
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
