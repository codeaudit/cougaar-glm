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

public interface UIAsset extends UIUniqueObject {

  /**
    Get the ids of allocations that apply to this asset.
    Note that this returns IDs and not the actual allocations,
    because otherwise there would be a circularity, i.e.
    asset->allocation->asset.  Note that in org.cougaar.planning.ldm.asset.Asset,
    that this is called the RoleSchedule.
    @return UUID[] - the array of UUIDs of allocations
  */

  UUID[] getAllocations();

  /**
     Get the schedule that indicates the availability time frame
     for the asset in this cluster.
     If this asset is an aggregate asset, the schedule
     also indicates the asset quantity.
     @return UISchedule - the schedule for this asset in this cluster
  */

  UISchedule getAvailableSchedule();

  /**
     Get TypeIdentificationPG TypeIdentification,
     which is a string of the form type/id.  For example:
     "NSN/0913801298439"
  */
     
  String getTypeIdentification();

  /** Get human readable form of the TypeIdentificationPG.
   */

  String getTypeIdentificationPGNomenclature();

  /** Get alternate forms of type identification, such as LINs or DODICs.
   */

  String getAlternateTypeIdentification();

  /** Get a string of the form type/id, for example:
     "SN/105G13F7YM0G".  This identifies objects which have
     serial numbers, bumper numbers, etc.
  */

  String getItemIdentification();

  /** Get a human readable form of the ItemIdentificationPG.
   */

  String getItemIdentificationPGNomenclature();

  /** Get property names for non-null properties of this asset.
      The property heirarchy is flattened, with property names
      being separated by underbars.  For example,
      ManagedAssetPG_numberOfPersonnel
   */

  //  String[] getPropertyNames();

  /** Get property values corresponding to the property names.
   */

  //  String[] getPropertyValues();

  /* Get asset for aggregate assets.
   */

  UIAsset getAsset();

  /* Get quantity for aggregate assets.
   */

  long getQuantity();

  UIPropertyNameValue[] getPropertyNameValues();

  UIPropertyNameValue getPropertyNameValue(int i);
}

