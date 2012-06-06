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
/** Primary client interface for SupplyPG.
 *  @see NewSupplyPG
 *  @see SupplyPGImpl
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


public interface SupplyPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** The maximum mass of class 1 materiel that can be stored, issued and transloaded per day **/
  Mass getClass1HandleMassPerDay();
  /** The maximum mass of class 2 materiel that can be stored, issued and transloaded per day **/
  Mass getClass2HandleMassPerDay();
  /** The maximum mass of class 3p materiel that can be stored, issued and transloaded per day **/
  Mass getClass3pHandleMassPerDay();
  /** The maximum mass of class 4b materiel that can be stored, issued and transloaded per day **/
  Mass getClass4bHandleMassPerDay();
  /** The maximum mass of class 4 materiel that can be stored, issued and transloaded per day **/
  Mass getClass4HandleMassPerDay();
  /** The maximum mass of class 5 materiel that can be transloaded per day **/
  Mass getClass5TransloadMassPerDay();
  /** The maximum mass of class 7 materiel that can be stored, issued and transloaded per day **/
  Mass getClass7HandleMassPerDay();
  /** The maximum mass of class 9 materiel that can be stored, issued and transloaded per day **/
  Mass getClass9HandleMassPerDay();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newSupplyPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewSupplyPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.SupplyPG.class;
  String assetSetter = "setSupplyPG";
  String assetGetter = "getSupplyPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  SupplyPG nullPG = new Null_SupplyPG();

/** Null_PG implementation for SupplyPG **/
final class Null_SupplyPG
  implements SupplyPG, Null_PG
{
  public Mass getClass1HandleMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass2HandleMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass3pHandleMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass4bHandleMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass4HandleMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass5TransloadMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass7HandleMassPerDay() { throw new UndefinedValueException(); }
  public Mass getClass9HandleMassPerDay() { throw new UndefinedValueException(); }
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
    return SupplyPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for SupplyPG **/
final class Future
  implements SupplyPG, Future_PG
{
  public Mass getClass1HandleMassPerDay() {
    waitForFinalize();
    return _real.getClass1HandleMassPerDay();
  }
  public Mass getClass2HandleMassPerDay() {
    waitForFinalize();
    return _real.getClass2HandleMassPerDay();
  }
  public Mass getClass3pHandleMassPerDay() {
    waitForFinalize();
    return _real.getClass3pHandleMassPerDay();
  }
  public Mass getClass4bHandleMassPerDay() {
    waitForFinalize();
    return _real.getClass4bHandleMassPerDay();
  }
  public Mass getClass4HandleMassPerDay() {
    waitForFinalize();
    return _real.getClass4HandleMassPerDay();
  }
  public Mass getClass5TransloadMassPerDay() {
    waitForFinalize();
    return _real.getClass5TransloadMassPerDay();
  }
  public Mass getClass7HandleMassPerDay() {
    waitForFinalize();
    return _real.getClass7HandleMassPerDay();
  }
  public Mass getClass9HandleMassPerDay() {
    waitForFinalize();
    return _real.getClass9HandleMassPerDay();
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
    return SupplyPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private SupplyPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof SupplyPG) {
      _real=(SupplyPG) real;
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
