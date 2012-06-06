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
/** Primary client interface for LiquidSupplyPG.
 *  @see NewLiquidSupplyPG
 *  @see LiquidSupplyPGImpl
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


public interface LiquidSupplyPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** The maximum volume of liquid that can be stored per day **/
  Volume getStoreVolumePerDay();
  /** The maximum volume of liquid that can be distributed per day **/
  Volume getDistributeVolumePerDay();
  /** The maximum volume of liquid that can be issued per day **/
  Volume getIssueVolumePerDay();
  /** The maximum volume of liquid  that can be pumped per minute **/
  Volume getPumpVolumePerMinute();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newLiquidSupplyPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewLiquidSupplyPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.LiquidSupplyPG.class;
  String assetSetter = "setLiquidSupplyPG";
  String assetGetter = "getLiquidSupplyPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  LiquidSupplyPG nullPG = new Null_LiquidSupplyPG();

/** Null_PG implementation for LiquidSupplyPG **/
final class Null_LiquidSupplyPG
  implements LiquidSupplyPG, Null_PG
{
  public Volume getStoreVolumePerDay() { throw new UndefinedValueException(); }
  public Volume getDistributeVolumePerDay() { throw new UndefinedValueException(); }
  public Volume getIssueVolumePerDay() { throw new UndefinedValueException(); }
  public Volume getPumpVolumePerMinute() { throw new UndefinedValueException(); }
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
    return LiquidSupplyPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for LiquidSupplyPG **/
final class Future
  implements LiquidSupplyPG, Future_PG
{
  public Volume getStoreVolumePerDay() {
    waitForFinalize();
    return _real.getStoreVolumePerDay();
  }
  public Volume getDistributeVolumePerDay() {
    waitForFinalize();
    return _real.getDistributeVolumePerDay();
  }
  public Volume getIssueVolumePerDay() {
    waitForFinalize();
    return _real.getIssueVolumePerDay();
  }
  public Volume getPumpVolumePerMinute() {
    waitForFinalize();
    return _real.getPumpVolumePerMinute();
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
    return LiquidSupplyPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private LiquidSupplyPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof LiquidSupplyPG) {
      _real=(LiquidSupplyPG) real;
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
