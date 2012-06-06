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
/** Primary client interface for PackagePG.
 *  @see NewPackagePG
 *  @see PackagePGImpl
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


public interface PackagePG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  long getCountPerPack();
  String getUnitOfIssue();
  Distance getPackLength();
  Distance getPackWidth();
  Distance getPackHeight();
  Area getPackFootprintArea();
  Volume getPackVolume();
  Mass getPackMass();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newPackagePG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewPackagePG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.PackagePG.class;
  String assetSetter = "setPackagePG";
  String assetGetter = "getPackagePG";
  /** The Null instance for indicating that the PG definitely has no value **/
  PackagePG nullPG = new Null_PackagePG();

/** Null_PG implementation for PackagePG **/
final class Null_PackagePG
  implements PackagePG, Null_PG
{
  public long getCountPerPack() { throw new UndefinedValueException(); }
  public String getUnitOfIssue() { throw new UndefinedValueException(); }
  public Distance getPackLength() { throw new UndefinedValueException(); }
  public Distance getPackWidth() { throw new UndefinedValueException(); }
  public Distance getPackHeight() { throw new UndefinedValueException(); }
  public Area getPackFootprintArea() { throw new UndefinedValueException(); }
  public Volume getPackVolume() { throw new UndefinedValueException(); }
  public Mass getPackMass() { throw new UndefinedValueException(); }
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
    return PackagePGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for PackagePG **/
final class Future
  implements PackagePG, Future_PG
{
  public long getCountPerPack() {
    waitForFinalize();
    return _real.getCountPerPack();
  }
  public String getUnitOfIssue() {
    waitForFinalize();
    return _real.getUnitOfIssue();
  }
  public Distance getPackLength() {
    waitForFinalize();
    return _real.getPackLength();
  }
  public Distance getPackWidth() {
    waitForFinalize();
    return _real.getPackWidth();
  }
  public Distance getPackHeight() {
    waitForFinalize();
    return _real.getPackHeight();
  }
  public Area getPackFootprintArea() {
    waitForFinalize();
    return _real.getPackFootprintArea();
  }
  public Volume getPackVolume() {
    waitForFinalize();
    return _real.getPackVolume();
  }
  public Mass getPackMass() {
    waitForFinalize();
    return _real.getPackMass();
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
    return PackagePGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private PackagePG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof PackagePG) {
      _real=(PackagePG) real;
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
