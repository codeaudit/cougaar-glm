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
/** Primary client interface for RailLinkPG.
 *  @see NewRailLinkPG
 *  @see RailLinkPGImpl
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


public interface RailLinkPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  long getNumberOfTracks();
  Distance getTrackGauge();
  double getMaximumGradeD2O();
  double getMaximumGradeO2D();
  Mass getMaximumAxleWeight();
  Distance getMaximumCarHeight();
  Distance getMaximumCarWidth();
  Distance getMaximumCarLength();
  Distance getMaximumTrainLength();
  Speed getMaximumTrainSpeed();
  String getLinkName();
  Distance getLinkLength();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newRailLinkPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewRailLinkPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.RailLinkPG.class;
  String assetSetter = "setRailLinkPG";
  String assetGetter = "getRailLinkPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  RailLinkPG nullPG = new Null_RailLinkPG();

/** Null_PG implementation for RailLinkPG **/
final class Null_RailLinkPG
  implements RailLinkPG, Null_PG
{
  public long getNumberOfTracks() { throw new UndefinedValueException(); }
  public Distance getTrackGauge() { throw new UndefinedValueException(); }
  public double getMaximumGradeD2O() { throw new UndefinedValueException(); }
  public double getMaximumGradeO2D() { throw new UndefinedValueException(); }
  public Mass getMaximumAxleWeight() { throw new UndefinedValueException(); }
  public Distance getMaximumCarHeight() { throw new UndefinedValueException(); }
  public Distance getMaximumCarWidth() { throw new UndefinedValueException(); }
  public Distance getMaximumCarLength() { throw new UndefinedValueException(); }
  public Distance getMaximumTrainLength() { throw new UndefinedValueException(); }
  public Speed getMaximumTrainSpeed() { throw new UndefinedValueException(); }
  public String getLinkName() { throw new UndefinedValueException(); }
  public Distance getLinkLength() { throw new UndefinedValueException(); }
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
    return RailLinkPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for RailLinkPG **/
final class Future
  implements RailLinkPG, Future_PG
{
  public long getNumberOfTracks() {
    waitForFinalize();
    return _real.getNumberOfTracks();
  }
  public Distance getTrackGauge() {
    waitForFinalize();
    return _real.getTrackGauge();
  }
  public double getMaximumGradeD2O() {
    waitForFinalize();
    return _real.getMaximumGradeD2O();
  }
  public double getMaximumGradeO2D() {
    waitForFinalize();
    return _real.getMaximumGradeO2D();
  }
  public Mass getMaximumAxleWeight() {
    waitForFinalize();
    return _real.getMaximumAxleWeight();
  }
  public Distance getMaximumCarHeight() {
    waitForFinalize();
    return _real.getMaximumCarHeight();
  }
  public Distance getMaximumCarWidth() {
    waitForFinalize();
    return _real.getMaximumCarWidth();
  }
  public Distance getMaximumCarLength() {
    waitForFinalize();
    return _real.getMaximumCarLength();
  }
  public Distance getMaximumTrainLength() {
    waitForFinalize();
    return _real.getMaximumTrainLength();
  }
  public Speed getMaximumTrainSpeed() {
    waitForFinalize();
    return _real.getMaximumTrainSpeed();
  }
  public String getLinkName() {
    waitForFinalize();
    return _real.getLinkName();
  }
  public Distance getLinkLength() {
    waitForFinalize();
    return _real.getLinkLength();
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
    return RailLinkPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private RailLinkPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof RailLinkPG) {
      _real=(RailLinkPG) real;
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
