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
package org.cougaar.mlm.construction;

import org.cougaar.glm.plugins.inventory.InventoryPlugIn;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.glm.ldm.plan.Agency;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.debug.*;



import java.util.GregorianCalendar;
import java.io.*;

/** PlugIn for Construction Inventory.  Contains methods to setup and create construction
 *  inventory.
 */

public class ConstructionInventoryPlugIn extends InventoryPlugIn {

    //Hardcoded required inventory levels
    protected double capacity = 1000000;
    protected double init_level = 0.0;
    protected double erq = 0.0;
    protected double min_reorder = 0.0;

    /** Constructor */
    public ConstructionInventoryPlugIn() {
      super();
    }
    
    /** Establishes and sets up Construction Inventory Decorator */
    protected void decoratePlugIn() {
      ConstructionInventoryDecorator decorator = new ConstructionInventoryDecorator(this, myOrganization_);
      decorator.decoratePlugIn(myOrganization_);
    }


    /** Sets up fields needed for construction inventory.
     *  Initializes inventory.
     */
    public Inventory createInventory(String supplytype, Asset resource) {
      String id = resource.getTypeIdentificationPG().getTypeIdentification();
      GLMDebug.DEBUG("*****ConstructionInventoryPlugIn:  createInventory()", "<"+supplytype+"> createInventory for "+id);
      GregorianCalendar reportBaseDate = null;
      int reportStepKind = 0;
      boolean success = false;
      InventoryBG bg = null;
      NewInventoryPG invpg = (NewInventoryPG)org.cougaar.glm.ldm.asset.PropertyGroupFactory.newInventoryPG();
      InventoryPlugIn.InventoryItemInfo info = null;
      double [] levels = null;

      info = (InventoryPlugIn.InventoryItemInfo) inventoryInitHash_.get(id);

      invpg.setResource(resource);
      Agency agency = myOrganization_.getOrganizationPG().getAgency();

      if (inventoryInitHash_.isEmpty()) {
        double [] temp_levels = {capacity, init_level, erq, min_reorder};
        levels = (double [])temp_levels;
      }
      if ((info != null) && (levels == null)) {
        //GLMDebug.DEBUG(className_, clusterId_,"*****ConstructionInventoryPlugIn: info not null and levels null");
        levels = (double[])info.levels;
      }

      // Try to initialize the inventory behavior group, if it fails this cluster
      // does not handle the item.
      if (levels != null) {
        bg  = new ConstructionInventoryBG(invpg);
        success = ((ConstructionInventoryBG)bg).initialize(levels);
        if (info != null) {
          if (info.reportBase != null) {
            reportBaseDate = info.reportBase;
            reportStepKind = info.reportStepKind;
          }
        }
      }

      if (!success) {
        GLMDebug.DEBUG("*****ConstructionInventoryPlugIn", getClusterIdentifier(), "createInventory(), cannot create inventory for "+id);
        return null;
      }

      Inventory inventory = null;
      inventory=(Inventory) theLDMF.createAsset("Inventory");
      if (inventory == null) {
        GLMDebug.DEBUG("*****ConstructionInventoryPlugIn","<"+supplytype+"> createInventory - fail to create inventory for "+id);
        return null;
      }
      invpg.setInvBG(bg);
      bg.setProjectionWeight(getProjectionWeight(supplytype));
      inventory.addOtherPropertyGroup(invpg);
      long time = getMyDelegate().currentTimeMillis();
      invpg.resetInventory(inventory, time);
      invpg.clearContentSchedule(inventory);

      NewTypeIdentificationPG ti = (NewTypeIdentificationPG)inventory.getTypeIdentificationPG();
      ti.setTypeIdentification("InventoryAsset");
      ti.setNomenclature("Inventory Asset");

      ((NewItemIdentificationPG) inventory.getItemIdentificationPG()).setItemIdentification("Inventory:" + id);

      NewScheduledContentPG scp;
      scp = (NewScheduledContentPG)inventory.getScheduledContentPG();
      scp.setAsset(resource);

      if (reportBaseDate != null) {
        NewReportSchedulePG nrsp = org.cougaar.glm.ldm.asset.PropertyGroupFactory.newReportSchedulePG();
        nrsp.setBase(info.reportBase);
        nrsp.setStep(info.reportStepKind);
        inventory.setReportSchedulePG(nrsp);
        //System.out.println("setReportSchedulePG " + info.reportBase);
      }
      return inventory;
    }

    /**
       Don't call this directly. Use the base class
       getProjectionWeight which will invoke this if necessary.
     **/
    protected ProjectionWeight createProjectionWeight(String supplyType) {
      return new ConstructionProjectionWeight(2);
    }

    
}