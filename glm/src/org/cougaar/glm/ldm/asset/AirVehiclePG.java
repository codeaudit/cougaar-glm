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
/** Primary client interface for AirVehiclePG.
 *  @see NewAirVehiclePG
 *  @see AirVehiclePGImpl
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


public interface AirVehiclePG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** Minimum runway length required under standard temperature, pressure, etc. **/
  Distance getMinimumRunwayLength();
  /** Can the aircraft be refueled in mid-air **/
  boolean getMidAirRefuelable();
  /** Time required for quick (minimum maintenance) turnaround **/
  Duration getQuickTurnaroundTime();
  /** Mean time required for typical failures **/
  Duration getMeanTimeToRepair();
  /** Used for estimating repair requirements based on mission legs **/
  long getMeanMissionLegsBetweenRepairs();
  /** Used for estimating repair requirements based on flight time **/
  Duration getMeanFlightTimeBetweenRepairs();
  String getVehicleType();
  long getCrewRequirements();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newAirVehiclePG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewAirVehiclePG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.AirVehiclePG.class;
  String assetSetter = "setAirVehiclePG";
  String assetGetter = "getAirVehiclePG";
  /** The Null instance for indicating that the PG definitely has no value **/
  AirVehiclePG nullPG = new Null_AirVehiclePG();

/** Null_PG implementation for AirVehiclePG **/
final class Null_AirVehiclePG
  implements AirVehiclePG, Null_PG
{
  public Distance getMinimumRunwayLength() { throw new UndefinedValueException(); }
  public boolean getMidAirRefuelable() { throw new UndefinedValueException(); }
  public Duration getQuickTurnaroundTime() { throw new UndefinedValueException(); }
  public Duration getMeanTimeToRepair() { throw new UndefinedValueException(); }
  public long getMeanMissionLegsBetweenRepairs() { throw new UndefinedValueException(); }
  public Duration getMeanFlightTimeBetweenRepairs() { throw new UndefinedValueException(); }
  public String getVehicleType() { throw new UndefinedValueException(); }
  public long getCrewRequirements() { throw new UndefinedValueException(); }
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
    return AirVehiclePGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for AirVehiclePG **/
final class Future
  implements AirVehiclePG, Future_PG
{
  public Distance getMinimumRunwayLength() {
    waitForFinalize();
    return _real.getMinimumRunwayLength();
  }
  public boolean getMidAirRefuelable() {
    waitForFinalize();
    return _real.getMidAirRefuelable();
  }
  public Duration getQuickTurnaroundTime() {
    waitForFinalize();
    return _real.getQuickTurnaroundTime();
  }
  public Duration getMeanTimeToRepair() {
    waitForFinalize();
    return _real.getMeanTimeToRepair();
  }
  public long getMeanMissionLegsBetweenRepairs() {
    waitForFinalize();
    return _real.getMeanMissionLegsBetweenRepairs();
  }
  public Duration getMeanFlightTimeBetweenRepairs() {
    waitForFinalize();
    return _real.getMeanFlightTimeBetweenRepairs();
  }
  public String getVehicleType() {
    waitForFinalize();
    return _real.getVehicleType();
  }
  public long getCrewRequirements() {
    waitForFinalize();
    return _real.getCrewRequirements();
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
    return AirVehiclePGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private AirVehiclePG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof AirVehiclePG) {
      _real=(AirVehiclePG) real;
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
