/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.plugins;

import org.cougaar.domain.glm.debug.GLMDebug;
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




