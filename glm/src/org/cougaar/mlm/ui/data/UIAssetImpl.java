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

package org.cougaar.mlm.ui.data;

import java.util.*;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.AssetIntrospection;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

public class UIAssetImpl implements UIAsset, XMLUIPlanObject {
  Asset asset;
  UUID assetUUID;

  public UIAssetImpl(Asset asset) {
    this.asset = asset;
    if (asset.getUID() == null)
      System.out.println("WARNING: Asset has no UID");
    else
      assetUUID = new UUID(asset.getUID().toString());
  }

  /**
    Get the ids of allocations that apply to this asset.
    Note that this returns IDs and not the actual allocations,
    because otherwise there would be a circularity, i.e.
    asset->allocation->asset.  Note that in org.cougaar.planning.ldm.asset.Asset,
    that this is called the RoleSchedule.
    @return UUID[] - the array of UUIDs of allocations
  */

  public UUID[] getAllocations() {
    RoleSchedule rs = asset.getRoleSchedule();
    synchronized(rs) {
      int l = rs.size();
      UUID[] ids = new UUID[l];
      int i = 0;
      for (Iterator it = rs.iterator(); it.hasNext(); i++) {
        ids[i] = new UUID(((PlanElement)it.next()).getUID().toString());
      }
      return ids;
    }
  }

  public UUID getAllocation(int ai) {
    RoleSchedule rs = asset.getRoleSchedule();
    synchronized(rs) {
      int l = rs.size();
      UUID[] ids = new UUID[l];
      int i = 0;
      for (Iterator it = rs.iterator(); it.hasNext(); i++) {
        PlanElement pe = (PlanElement)it.next();
        if (i == ai)
          return new UUID((pe).getUID().toString());
      }
    }
    return null;
  }

  /**
     Get the schedule that indicates the availability time frame
     for the asset in this cluster.
     If this asset is an aggregate asset, the schedule
     also indicates the asset quantity.
     @return UISchedule - the schedule for this asset in this cluster
  */

  public UISchedule getAvailableSchedule() {
    RoleSchedule roleSchedule = asset.getRoleSchedule();
    if (roleSchedule != null) {
      Schedule schedule = roleSchedule.getAvailableSchedule();
      if (schedule != null)
        return new UIScheduleImpl(schedule);
    }
    return null;
  }

  /** The following are from TypeIdentificationPG.
   */

  /**
     Get TypeIdentificationPG TypeIdentification,
     which is a string of the form type/id.  For example:
     "NSN/0913801298439"
  */
     
  public String getTypeIdentification() {
    return asset.getTypeIdentificationPG().getTypeIdentification();
  }

  /** Get human readable form of the TypeIdentificationPG.
   */

  public String getTypeIdentificationPGNomenclature() {
    return asset.getTypeIdentificationPG().getNomenclature();
  }

  /** Get alternate forms of type identification, such as LINs or DODICs.
   */

  public String getAlternateTypeIdentification() {
    return asset.getTypeIdentificationPG().getAlternateTypeIdentification();
  }

  /** The following are from ItemIdentificationPG.
   */

  /** Get a string of the form type/id, for example:
     "SN/105G13F7YM0G".  This identifies objects which have
     serial numbers, bumper numbers, etc.
  */

  public String getItemIdentification() {
    return asset.getItemIdentificationPG().getItemIdentification();
  }

  public String getItemIdentificationPGNomenclature() {
    return asset.getItemIdentificationPG().getNomenclature();
  }

  public UIPropertyNameValue[] getPropertyNameValues() {
    Vector props = 
      AssetIntrospection.fetchAllProperties(asset);
    int l = props.size();
    Vector v = new Vector(l);
    for (int i = 0; i < l; i++) {
      PropertyNameValue pnv = (PropertyNameValue)props.get(i);
      v.add(new UIPropertyNameValue(pnv.name, pnv.value));
    }
    return (UIPropertyNameValue[])v.toArray(new UIPropertyNameValue[l]);
  }

  public UIPropertyNameValue getPropertyNameValue(int i) {
    UIPropertyNameValue[] pnv = getPropertyNameValues();
    return pnv[i];
  }

  //  public String getPropertyValue(int i) {
  //    return null;
  //  }

  public UUID getUUID() {
    return assetUUID;
  }

  public UIAsset getAsset() {
    if (asset instanceof AggregateAsset)
      return new UIAssetImpl(((AggregateAsset)asset).getAsset());
    else
      return null;
  }

  public long getQuantity() {
    if (asset instanceof AggregateAsset)
      return ((AggregateAsset)asset).getQuantity();
    else
      return 1;
  }

  /** Creates XML version of this object for user interface.
    @return Element - an element of an XML document
   */
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }

}

