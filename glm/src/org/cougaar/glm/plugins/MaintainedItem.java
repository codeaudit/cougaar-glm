/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.glm.plugins;

import org.cougaar.glm.debug.GLMDebug;
import java.io.Serializable;
import java.util.HashMap;
/**
 * Trimmed down, representation of an asset
 **/
public class MaintainedItem implements Serializable {
    
  protected String maintainedItemType = null;
  protected String typeIdentification = null;
  protected String itemIdentification = null;
  protected String nomenclature = null;
  private static HashMap cache = new HashMap();

 
  public MaintainedItem() {}

  public MaintainedItem(String type, String typeId, String itemId, String nomen) {
    maintainedItemType = type;
    typeIdentification = typeId;
    itemIdentification = itemId;
    nomenclature = nomen;
  }

  public String getMaintainedItemType() {
    return maintainedItemType;
  }

  public String getTypeIdentification() {
    return typeIdentification;
  }

  public String getItemIdentification() {
    return itemIdentification;
  }

  public String getNomenclature() {
    return nomenclature;
  }

  public static MaintainedItem findOrMakeMaintainedItem(String type, String typeId, String itemId, String nomen) {
    if (type == null || typeId == null) {
      GLMDebug.ERROR("MaintainedItem", "Type and/or TypeIdentification cannot be null");
      return null;
    }
    String key = type+typeId;
    if (itemId != null) key = key+itemId;
    MaintainedItem item = (MaintainedItem)cache.get(key);
    if (item == null) {
      item = new MaintainedItem(type, typeId, itemId, nomen);
      cache.put(key, item);
    }
    return item;
  }

  public String toString() {
    return this.getClass().getName()+": <"+maintainedItemType+">, <"+
	typeIdentification+">, <"+itemIdentification+">, <"+nomenclature+">";
  }

  private transient int _hc = 0;
  public int hashCode() {
    if (_hc != 0) return _hc;
    int hc = 1;
    if (maintainedItemType != null) hc+=maintainedItemType.hashCode();
    if (typeIdentification != null) hc+=typeIdentification.hashCode();
    if (itemIdentification != null) hc+=itemIdentification.hashCode();
    _hc = hc;
    return hc;
  }

  /** Equals for MaintainedItems is equivalent to the test for equal
   *  assets.  Assumes
   **/
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(this.getClass() == o.getClass())) return false;
    MaintainedItem oi = (MaintainedItem)o;
    String oType = oi.getMaintainedItemType();
    if (oType == null || maintainedItemType == null || !(maintainedItemType.equals(oType))) return false;
    String oTypeID = oi.getTypeIdentification();
    if (oTypeID == null || typeIdentification == null || !(typeIdentification.equals(oTypeID))) return false;
    String oItemID = oi.getItemIdentification();
    if (oItemID == itemIdentification) return true;
    if (itemIdentification != null) {
      return itemIdentification.equals(oItemID);
    } else {
      return false;
    }
  }
}




