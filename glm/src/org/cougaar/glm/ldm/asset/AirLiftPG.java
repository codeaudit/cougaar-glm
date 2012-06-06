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
/** Primary client interface for AirLiftPG.
 *  @see NewAirLiftPG
 *  @see AirLiftPGImpl
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


public interface AirLiftPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  Volume getMaximumBulkVolume();
  Mass getMaximumBulkWeight();
  Volume getMaximumOversizeVolume();
  Mass getMaximumOversizeWeight();
  Volume getMaximumOutsizeVolume();
  Mass getMaximumOutsizeWeight();
  int getMaximumPallets();
  /** Maximum length of Cargo allowed **/
  Distance getMaximumLength();
  /** Maximum width of Cargo allowed **/
  Distance getMaximumWidth();
  /** Maximum height of Cargo allowed **/
  Distance getMaximumHeight();
  String getCargoRestrictions();
  String getPermittedCargoCategoryCodes();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newAirLiftPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewAirLiftPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.AirLiftPG.class;
  String assetSetter = "setAirLiftPG";
  String assetGetter = "getAirLiftPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  AirLiftPG nullPG = new Null_AirLiftPG();

/** Null_PG implementation for AirLiftPG **/
final class Null_AirLiftPG
  implements AirLiftPG, Null_PG
{
  public Volume getMaximumBulkVolume() { throw new UndefinedValueException(); }
  public Mass getMaximumBulkWeight() { throw new UndefinedValueException(); }
  public Volume getMaximumOversizeVolume() { throw new UndefinedValueException(); }
  public Mass getMaximumOversizeWeight() { throw new UndefinedValueException(); }
  public Volume getMaximumOutsizeVolume() { throw new UndefinedValueException(); }
  public Mass getMaximumOutsizeWeight() { throw new UndefinedValueException(); }
  public int getMaximumPallets() { throw new UndefinedValueException(); }
  public Distance getMaximumLength() { throw new UndefinedValueException(); }
  public Distance getMaximumWidth() { throw new UndefinedValueException(); }
  public Distance getMaximumHeight() { throw new UndefinedValueException(); }
  public String getCargoRestrictions() { throw new UndefinedValueException(); }
  public String getPermittedCargoCategoryCodes() { throw new UndefinedValueException(); }
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
    return AirLiftPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for AirLiftPG **/
final class Future
  implements AirLiftPG, Future_PG
{
  public Volume getMaximumBulkVolume() {
    waitForFinalize();
    return _real.getMaximumBulkVolume();
  }
  public Mass getMaximumBulkWeight() {
    waitForFinalize();
    return _real.getMaximumBulkWeight();
  }
  public Volume getMaximumOversizeVolume() {
    waitForFinalize();
    return _real.getMaximumOversizeVolume();
  }
  public Mass getMaximumOversizeWeight() {
    waitForFinalize();
    return _real.getMaximumOversizeWeight();
  }
  public Volume getMaximumOutsizeVolume() {
    waitForFinalize();
    return _real.getMaximumOutsizeVolume();
  }
  public Mass getMaximumOutsizeWeight() {
    waitForFinalize();
    return _real.getMaximumOutsizeWeight();
  }
  public int getMaximumPallets() {
    waitForFinalize();
    return _real.getMaximumPallets();
  }
  public Distance getMaximumLength() {
    waitForFinalize();
    return _real.getMaximumLength();
  }
  public Distance getMaximumWidth() {
    waitForFinalize();
    return _real.getMaximumWidth();
  }
  public Distance getMaximumHeight() {
    waitForFinalize();
    return _real.getMaximumHeight();
  }
  public String getCargoRestrictions() {
    waitForFinalize();
    return _real.getCargoRestrictions();
  }
  public String getPermittedCargoCategoryCodes() {
    waitForFinalize();
    return _real.getPermittedCargoCategoryCodes();
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
    return AirLiftPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private AirLiftPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof AirLiftPG) {
      _real=(AirLiftPG) real;
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
