/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
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
package org.cougaar.mlm.construction;

import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.InventoryBG;
import org.cougaar.glm.ldm.asset.NewInventoryPG;
import org.cougaar.glm.ldm.asset.NewReportSchedulePG;
import org.cougaar.glm.ldm.asset.NewScheduledContentPG;
import org.cougaar.glm.ldm.asset.ProjectionWeight;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;
import org.cougaar.glm.ldm.plan.Agency;
import org.cougaar.glm.plugins.inventory.InventoryPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;

import java.util.GregorianCalendar;

/** Plugin for Construction Inventory.  Contains methods to setup and create construction
 *  inventory.
 */

public class ConstructionInventoryPlugin extends InventoryPlugin {

    //Hardcoded required inventory levels
    protected double capacity = 1000000;
    protected double init_level = 0.0;
    protected double erq = 0.0;
    protected double min_reorder = 0.0;

    /** Constructor */
    public ConstructionInventoryPlugin() {
      super();
    }
    
    /** Establishes and sets up Construction Inventory Decorator */
    protected void decoratePlugin() {
      ConstructionInventoryDecorator decorator = new ConstructionInventoryDecorator(this, myOrganization_);
      decorator.decoratePlugin(myOrganization_);
    }


    /** Sets up fields needed for construction inventory.
     *  Initializes inventory.
     */
    public Inventory createInventory(String supplytype, Asset resource) {
      String id = resource.getTypeIdentificationPG().getTypeIdentification();
      if (logger.isDebugEnabled()) {
        logger.debug("*****ConstructionInventoryPlugin:  createInventory()" + "<" + supplytype + "> createInventory for " + id);
      }
      GregorianCalendar reportBaseDate = null;
      int reportStepKind = 0;
      boolean success = false;
      InventoryBG bg = null;
      NewInventoryPG invpg = (NewInventoryPG) PropertyGroupFactory.newInventoryPG();
      InventoryItemInfo info = null;
      double[] levels = null;

      info = (InventoryItemInfo) inventoryInitHash_.get(id);

      invpg.setResource(resource);
      Agency agency = myOrganization_.getOrganizationPG().getAgency();

      if (inventoryInitHash_.isEmpty()) {
        double[] temp_levels = {capacity, init_level, erq, min_reorder};
        levels = (double[]) temp_levels;
      }
      if ((info != null) && (levels == null)) {
        //GLMDebug.DEBUG(className_, clusterId_,"*****ConstructionInventoryPlugin: info not null and levels null");
        levels = (double[]) info.levels;
      }

      // Try to initialize the inventory behavior group, if it fails this cluster
      // does not handle the item.
      if (levels != null) {
        bg = new ConstructionInventoryBG(invpg);
        success = ((ConstructionInventoryBG) bg).initialize(levels);
        if (info != null) {
          if (info.reportBase != null) {
            reportBaseDate = info.reportBase;
            reportStepKind = info.reportStepKind;
          }
        }
      }

      if (!success) {
        if (logger.isDebugEnabled()) {
          logger.debug("createInventory(), cannot create inventory for " + id);
        }
        return null;
      }

      Inventory inventory = null;
      inventory = (Inventory) theLDMF.createAsset("Inventory");
      if (inventory == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("*****ConstructionInventoryPlugin" + "<" + supplytype + "> createInventory - fail to create inventory for " + id);
        }
        return null;
      }
      invpg.setInvBG(bg);
      bg.setProjectionWeight(getProjectionWeight(supplytype));
      inventory.addOtherPropertyGroup(invpg);
      long time = getMyDelegate().currentTimeMillis();
      invpg.resetInventory(inventory, time);
      invpg.clearContentSchedule(inventory);

      NewTypeIdentificationPG ti = (NewTypeIdentificationPG) inventory.getTypeIdentificationPG();
      ti.setTypeIdentification("InventoryAsset");
      ti.setNomenclature("Inventory Asset");

      ((NewItemIdentificationPG) inventory.getItemIdentificationPG()).setItemIdentification("Inventory:" + id);

      NewScheduledContentPG scp;
      scp = (NewScheduledContentPG) inventory.getScheduledContentPG();
      scp.setAsset(resource);

      if (reportBaseDate != null) {
        NewReportSchedulePG nrsp = PropertyGroupFactory.newReportSchedulePG();
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