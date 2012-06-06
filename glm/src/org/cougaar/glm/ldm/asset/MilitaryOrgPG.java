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
/** Primary client interface for MilitaryOrgPG.
 *  @see NewMilitaryOrgPG
 *  @see MilitaryOrgPGImpl
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


public interface MilitaryOrgPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  String getStandardName();
  /** The UIC (Unit Identification Code) of the unit. **/
  String getUIC();
  /** The Joint Unit Type Code, specifying the type of unit. **/
  String getUTC();
  /** The Army SRC, specifying the type of Unit. **/
  String getSRC();
  /** The numeric code specifying the echelon of the unit. 08:Division, 07:Brigade, 06:Regiment, 05:Battalion, 04:Company. Codes are traditionally printed as two figures with a leading zero if necessary. **/
  String getEchelon();
  /** The requested echelon of support for the unit.  Should map to MilitaryEchelon constants - **/
  String getRequestedEchelonOfSupport();
  /** The 2525A hierarchy designator, used to specify the symbol for the organization on a map. **/
  String getHierarchy2525();
  boolean getIsReserve();
  /** Location of the Org when it is not deployed. Should match AssignedPG.homeStation if defined. **/
  Location getHomeLocation();

  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newMilitaryOrgPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewMilitaryOrgPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.MilitaryOrgPG.class;
  String assetSetter = "setMilitaryOrgPG";
  String assetGetter = "getMilitaryOrgPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  MilitaryOrgPG nullPG = new Null_MilitaryOrgPG();

/** Null_PG implementation for MilitaryOrgPG **/
final class Null_MilitaryOrgPG
  implements MilitaryOrgPG, Null_PG
{
  public String getStandardName() { throw new UndefinedValueException(); }
  public String getUIC() { throw new UndefinedValueException(); }
  public String getUTC() { throw new UndefinedValueException(); }
  public String getSRC() { throw new UndefinedValueException(); }
  public String getEchelon() { throw new UndefinedValueException(); }
  public String getRequestedEchelonOfSupport() { throw new UndefinedValueException(); }
  public String getHierarchy2525() { throw new UndefinedValueException(); }
  public boolean getIsReserve() { throw new UndefinedValueException(); }
  public Location getHomeLocation() { throw new UndefinedValueException(); }
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
    return MilitaryOrgPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for MilitaryOrgPG **/
final class Future
  implements MilitaryOrgPG, Future_PG
{
  public String getStandardName() {
    waitForFinalize();
    return _real.getStandardName();
  }
  public String getUIC() {
    waitForFinalize();
    return _real.getUIC();
  }
  public String getUTC() {
    waitForFinalize();
    return _real.getUTC();
  }
  public String getSRC() {
    waitForFinalize();
    return _real.getSRC();
  }
  public String getEchelon() {
    waitForFinalize();
    return _real.getEchelon();
  }
  public String getRequestedEchelonOfSupport() {
    waitForFinalize();
    return _real.getRequestedEchelonOfSupport();
  }
  public String getHierarchy2525() {
    waitForFinalize();
    return _real.getHierarchy2525();
  }
  public boolean getIsReserve() {
    waitForFinalize();
    return _real.getIsReserve();
  }
  public Location getHomeLocation() {
    waitForFinalize();
    return _real.getHomeLocation();
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
    return MilitaryOrgPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private MilitaryOrgPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof MilitaryOrgPG) {
      _real=(MilitaryOrgPG) real;
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
