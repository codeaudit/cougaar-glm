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
/** Primary client interface for SeaportPG.
 *  @see NewSeaportPG
 *  @see SeaportPGImpl
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


public interface SeaportPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  long getNumberOfROROBerths();
  long getNumberOfContainerBerths();
  Distance getPierLength();
  Distance getPierWidth();
  Distance getPierDepth();
  Distance getSupportedTurningRadius();
  Distance getHighTideDraft();
  Distance getLowTideDraft();
  long getNumberOfContainerCranes();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newSeaportPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewSeaportPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.SeaportPG.class;
  String assetSetter = "setSeaportPG";
  String assetGetter = "getSeaportPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  SeaportPG nullPG = new Null_SeaportPG();

/** Null_PG implementation for SeaportPG **/
final class Null_SeaportPG
  implements SeaportPG, Null_PG
{
  public long getNumberOfROROBerths() { throw new UndefinedValueException(); }
  public long getNumberOfContainerBerths() { throw new UndefinedValueException(); }
  public Distance getPierLength() { throw new UndefinedValueException(); }
  public Distance getPierWidth() { throw new UndefinedValueException(); }
  public Distance getPierDepth() { throw new UndefinedValueException(); }
  public Distance getSupportedTurningRadius() { throw new UndefinedValueException(); }
  public Distance getHighTideDraft() { throw new UndefinedValueException(); }
  public Distance getLowTideDraft() { throw new UndefinedValueException(); }
  public long getNumberOfContainerCranes() { throw new UndefinedValueException(); }
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
    return SeaportPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for SeaportPG **/
final class Future
  implements SeaportPG, Future_PG
{
  public long getNumberOfROROBerths() {
    waitForFinalize();
    return _real.getNumberOfROROBerths();
  }
  public long getNumberOfContainerBerths() {
    waitForFinalize();
    return _real.getNumberOfContainerBerths();
  }
  public Distance getPierLength() {
    waitForFinalize();
    return _real.getPierLength();
  }
  public Distance getPierWidth() {
    waitForFinalize();
    return _real.getPierWidth();
  }
  public Distance getPierDepth() {
    waitForFinalize();
    return _real.getPierDepth();
  }
  public Distance getSupportedTurningRadius() {
    waitForFinalize();
    return _real.getSupportedTurningRadius();
  }
  public Distance getHighTideDraft() {
    waitForFinalize();
    return _real.getHighTideDraft();
  }
  public Distance getLowTideDraft() {
    waitForFinalize();
    return _real.getLowTideDraft();
  }
  public long getNumberOfContainerCranes() {
    waitForFinalize();
    return _real.getNumberOfContainerCranes();
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
    return SeaportPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private SeaportPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof SeaportPG) {
      _real=(SeaportPG) real;
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
