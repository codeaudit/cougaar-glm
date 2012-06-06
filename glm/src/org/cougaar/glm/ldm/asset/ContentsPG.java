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
/** Primary client interface for ContentsPG.
 *  @see NewContentsPG
 *  @see ContentsPGImpl
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


public interface ContentsPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  Collection getTypeIdentifications();
  /** test to see if an element is a member of the type_identifications Collection **/
  boolean inTypeIdentifications(String element);

  /** array getter for beans **/
  String[] getTypeIdentificationsAsArray();

  /** indexed getter for beans **/
  String getIndexedTypeIdentifications(int index);

  Collection getNomenclatures();
  /** test to see if an element is a member of the nomenclatures Collection **/
  boolean inNomenclatures(String element);

  /** array getter for beans **/
  String[] getNomenclaturesAsArray();

  /** indexed getter for beans **/
  String getIndexedNomenclatures(int index);

  Collection getWeights();
  /** test to see if an element is a member of the weights Collection **/
  boolean inWeights(Mass element);

  /** array getter for beans **/
  Mass[] getWeightsAsArray();

  /** indexed getter for beans **/
  Mass getIndexedWeights(int index);

  Collection getReceivers();
  /** test to see if an element is a member of the receivers Collection **/
  boolean inReceivers(String element);

  /** array getter for beans **/
  String[] getReceiversAsArray();

  /** indexed getter for beans **/
  String getIndexedReceivers(int index);


  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newContentsPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewContentsPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.ContentsPG.class;
  String assetSetter = "setContentsPG";
  String assetGetter = "getContentsPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  ContentsPG nullPG = new Null_ContentsPG();

/** Null_PG implementation for ContentsPG **/
final class Null_ContentsPG
  implements ContentsPG, Null_PG
{
  public Collection getTypeIdentifications() { throw new UndefinedValueException(); }
  public boolean inTypeIdentifications(String element) { return false; }
  public String[] getTypeIdentificationsAsArray() { return null; }
  public String getIndexedTypeIdentifications(int index) { throw new UndefinedValueException(); }
  public Collection getNomenclatures() { throw new UndefinedValueException(); }
  public boolean inNomenclatures(String element) { return false; }
  public String[] getNomenclaturesAsArray() { return null; }
  public String getIndexedNomenclatures(int index) { throw new UndefinedValueException(); }
  public Collection getWeights() { throw new UndefinedValueException(); }
  public boolean inWeights(Mass element) { return false; }
  public Mass[] getWeightsAsArray() { return null; }
  public Mass getIndexedWeights(int index) { throw new UndefinedValueException(); }
  public Collection getReceivers() { throw new UndefinedValueException(); }
  public boolean inReceivers(String element) { return false; }
  public String[] getReceiversAsArray() { return null; }
  public String getIndexedReceivers(int index) { throw new UndefinedValueException(); }
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
    return ContentsPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for ContentsPG **/
final class Future
  implements ContentsPG, Future_PG
{
  public Collection getTypeIdentifications() {
    waitForFinalize();
    return _real.getTypeIdentifications();
  }
  public boolean inTypeIdentifications(String element) {
    waitForFinalize();
    return _real.inTypeIdentifications(element);
  }
  public String[] getTypeIdentificationsAsArray() {
    waitForFinalize();
    return _real.getTypeIdentificationsAsArray();
  }
  public String getIndexedTypeIdentifications(int index) {
    waitForFinalize();
    return _real.getIndexedTypeIdentifications(index);
  }
  public Collection getNomenclatures() {
    waitForFinalize();
    return _real.getNomenclatures();
  }
  public boolean inNomenclatures(String element) {
    waitForFinalize();
    return _real.inNomenclatures(element);
  }
  public String[] getNomenclaturesAsArray() {
    waitForFinalize();
    return _real.getNomenclaturesAsArray();
  }
  public String getIndexedNomenclatures(int index) {
    waitForFinalize();
    return _real.getIndexedNomenclatures(index);
  }
  public Collection getWeights() {
    waitForFinalize();
    return _real.getWeights();
  }
  public boolean inWeights(Mass element) {
    waitForFinalize();
    return _real.inWeights(element);
  }
  public Mass[] getWeightsAsArray() {
    waitForFinalize();
    return _real.getWeightsAsArray();
  }
  public Mass getIndexedWeights(int index) {
    waitForFinalize();
    return _real.getIndexedWeights(index);
  }
  public Collection getReceivers() {
    waitForFinalize();
    return _real.getReceivers();
  }
  public boolean inReceivers(String element) {
    waitForFinalize();
    return _real.inReceivers(element);
  }
  public String[] getReceiversAsArray() {
    waitForFinalize();
    return _real.getReceiversAsArray();
  }
  public String getIndexedReceivers(int index) {
    waitForFinalize();
    return _real.getIndexedReceivers(index);
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
    return ContentsPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private ContentsPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof ContentsPG) {
      _real=(ContentsPG) real;
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
