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
/** Primary client interface for AirConditionPG.
 *  @see NewAirConditionPG
 *  @see AirConditionPGImpl
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


public interface AirConditionPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** Altitude of the lowest clouds **/
  Distance getCeiling();
  Speed getWindSpeed();
  Temperature getTemperature();
  double getPrecipitationRate();
  Heading getWindHeading();
  Distance getVisibility();
  double getBarometricPressure();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newAirConditionPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewAirConditionPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.AirConditionPG.class;
  String assetSetter = "setAirConditionPG";
  String assetGetter = "getAirConditionPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  AirConditionPG nullPG = new Null_AirConditionPG();

/** Null_PG implementation for AirConditionPG **/
final class Null_AirConditionPG
  implements AirConditionPG, Null_PG
{
  public Distance getCeiling() { throw new UndefinedValueException(); }
  public Speed getWindSpeed() { throw new UndefinedValueException(); }
  public Temperature getTemperature() { throw new UndefinedValueException(); }
  public double getPrecipitationRate() { throw new UndefinedValueException(); }
  public Heading getWindHeading() { throw new UndefinedValueException(); }
  public Distance getVisibility() { throw new UndefinedValueException(); }
  public double getBarometricPressure() { throw new UndefinedValueException(); }
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
    return AirConditionPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for AirConditionPG **/
final class Future
  implements AirConditionPG, Future_PG
{
  public Distance getCeiling() {
    waitForFinalize();
    return _real.getCeiling();
  }
  public Speed getWindSpeed() {
    waitForFinalize();
    return _real.getWindSpeed();
  }
  public Temperature getTemperature() {
    waitForFinalize();
    return _real.getTemperature();
  }
  public double getPrecipitationRate() {
    waitForFinalize();
    return _real.getPrecipitationRate();
  }
  public Heading getWindHeading() {
    waitForFinalize();
    return _real.getWindHeading();
  }
  public Distance getVisibility() {
    waitForFinalize();
    return _real.getVisibility();
  }
  public double getBarometricPressure() {
    waitForFinalize();
    return _real.getBarometricPressure();
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
    return AirConditionPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private AirConditionPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof AirConditionPG) {
      _real=(AirConditionPG) real;
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
