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
/** Primary client interface for CSSCapabilityPG.
 *  @see NewCSSCapabilityPG
 *  @see CSSCapabilityPGImpl
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


public interface CSSCapabilityPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** a set of CSS capabilities that this asset advertises. **/
  Collection getCapabilities();
  /** test to see if an element is a member of the capabilities Collection **/
  boolean inCapabilities(CSSCapability element);

  /** array getter for beans **/
  CSSCapability[] getCapabilitiesAsArray();

  /** indexed getter for beans **/
  CSSCapability getIndexedCapabilities(int index);


  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newCSSCapabilityPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewCSSCapabilityPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.CSSCapabilityPG.class;
  String assetSetter = "setCSSCapabilityPG";
  String assetGetter = "getCSSCapabilityPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  CSSCapabilityPG nullPG = new Null_CSSCapabilityPG();

/** Null_PG implementation for CSSCapabilityPG **/
final class Null_CSSCapabilityPG
  implements CSSCapabilityPG, Null_PG
{
  public Collection getCapabilities() { throw new UndefinedValueException(); }
  public boolean inCapabilities(CSSCapability element) { return false; }
  public CSSCapability[] getCapabilitiesAsArray() { return null; }
  public CSSCapability getIndexedCapabilities(int index) { throw new UndefinedValueException(); }
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
    return CSSCapabilityPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for CSSCapabilityPG **/
final class Future
  implements CSSCapabilityPG, Future_PG
{
  public Collection getCapabilities() {
    waitForFinalize();
    return _real.getCapabilities();
  }
  public boolean inCapabilities(CSSCapability element) {
    waitForFinalize();
    return _real.inCapabilities(element);
  }
  public CSSCapability[] getCapabilitiesAsArray() {
    waitForFinalize();
    return _real.getCapabilitiesAsArray();
  }
  public CSSCapability getIndexedCapabilities(int index) {
    waitForFinalize();
    return _real.getIndexedCapabilities(index);
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
    return CSSCapabilityPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private CSSCapabilityPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof CSSCapabilityPG) {
      _real=(CSSCapabilityPG) real;
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
