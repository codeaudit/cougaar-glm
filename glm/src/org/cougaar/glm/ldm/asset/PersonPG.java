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
/** Primary client interface for PersonPG.
 *  @see NewPersonPG
 *  @see PersonPGImpl
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


public interface PersonPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** Unique identifier for a person within a country (e.g. SSN). **/
  String getIDCode();
  /** Country of citizenship. **/
  String getNationality();
  /** A set of skills that this person has. **/
  Collection getSkills();
  /** test to see if an element is a member of the skills Collection **/
  boolean inSkills(Skill element);

  /** array getter for beans **/
  Skill[] getSkillsAsArray();

  /** indexed getter for beans **/
  Skill getIndexedSkills(int index);


  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newPersonPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewPersonPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.PersonPG.class;
  String assetSetter = "setPersonPG";
  String assetGetter = "getPersonPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  PersonPG nullPG = new Null_PersonPG();

/** Null_PG implementation for PersonPG **/
final class Null_PersonPG
  implements PersonPG, Null_PG
{
  public String getIDCode() { throw new UndefinedValueException(); }
  public String getNationality() { throw new UndefinedValueException(); }
  public Collection getSkills() { throw new UndefinedValueException(); }
  public boolean inSkills(Skill element) { return false; }
  public Skill[] getSkillsAsArray() { return null; }
  public Skill getIndexedSkills(int index) { throw new UndefinedValueException(); }
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
    return PersonPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for PersonPG **/
final class Future
  implements PersonPG, Future_PG
{
  public String getIDCode() {
    waitForFinalize();
    return _real.getIDCode();
  }
  public String getNationality() {
    waitForFinalize();
    return _real.getNationality();
  }
  public Collection getSkills() {
    waitForFinalize();
    return _real.getSkills();
  }
  public boolean inSkills(Skill element) {
    waitForFinalize();
    return _real.inSkills(element);
  }
  public Skill[] getSkillsAsArray() {
    waitForFinalize();
    return _real.getSkillsAsArray();
  }
  public Skill getIndexedSkills(int index) {
    waitForFinalize();
    return _real.getIndexedSkills(index);
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
    return PersonPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private PersonPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof PersonPG) {
      _real=(PersonPG) real;
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
