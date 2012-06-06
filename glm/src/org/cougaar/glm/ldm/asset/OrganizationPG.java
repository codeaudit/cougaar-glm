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
/** Primary client interface for OrganizationPG.
 *  @see NewOrganizationPG
 *  @see OrganizationPGImpl
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


public interface OrganizationPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** one of the values of Service. **/
  Service getService();
  /** the agency or other major organization the asset is associated with.  For military Organizations, this is the CINC at the top of their command structure, e.g. USTC, CENTCOM. **/
  Agency getAgency();
  Collection getRoles();
  /** test to see if an element is a member of the roles Collection **/
  boolean inRoles(Role element);

  /** array getter for beans **/
  Role[] getRolesAsArray();

  /** indexed getter for beans **/
  Role getIndexedRoles(int index);


  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newOrganizationPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewOrganizationPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.OrganizationPG.class;
  String assetSetter = "setOrganizationPG";
  String assetGetter = "getOrganizationPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  OrganizationPG nullPG = new Null_OrganizationPG();

/** Null_PG implementation for OrganizationPG **/
final class Null_OrganizationPG
  implements OrganizationPG, Null_PG
{
  public Service getService() { throw new UndefinedValueException(); }
  public Agency getAgency() { throw new UndefinedValueException(); }
  public Collection getRoles() { throw new UndefinedValueException(); }
  public boolean inRoles(Role element) { return false; }
  public Role[] getRolesAsArray() { return null; }
  public Role getIndexedRoles(int index) { throw new UndefinedValueException(); }
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
    return OrganizationPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for OrganizationPG **/
final class Future
  implements OrganizationPG, Future_PG
{
  public Service getService() {
    waitForFinalize();
    return _real.getService();
  }
  public Agency getAgency() {
    waitForFinalize();
    return _real.getAgency();
  }
  public Collection getRoles() {
    waitForFinalize();
    return _real.getRoles();
  }
  public boolean inRoles(Role element) {
    waitForFinalize();
    return _real.inRoles(element);
  }
  public Role[] getRolesAsArray() {
    waitForFinalize();
    return _real.getRolesAsArray();
  }
  public Role getIndexedRoles(int index) {
    waitForFinalize();
    return _real.getIndexedRoles(index);
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
    return OrganizationPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private OrganizationPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof OrganizationPG) {
      _real=(OrganizationPG) real;
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
