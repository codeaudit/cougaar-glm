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

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Agency;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.PluginDecorator;
import org.cougaar.glm.plugins.inventory.ExternalAllocator;
import org.cougaar.glm.plugins.inventory.InventoryPlugin;
import org.cougaar.glm.plugins.inventory.SupplyExpander;
import org.cougaar.glm.plugins.inventory.WithdrawAllocator;

 /** Establishes necessary processes for Construction Inventory.
  *
  **/
 public class ConstructionInventoryDecorator extends  PluginDecorator {
    private static final String            CLASSNAME = "ConstructionInventoryDecorator";
    private static final String          CONSTRUCTION = "ClassIVConstructionMaterial";

    InventoryPlugin thisPlugin_;
    Service serv_;
    Agency agency_;
    Organization cluster_;

    /** Constructor */
    public ConstructionInventoryDecorator (InventoryPlugin plugin, Organization cluster) {
	super(plugin);
	thisPlugin_ = plugin;
	cluster_ = cluster;
	serv_ = cluster_.getOrganizationPG().getService();
	agency_ = cluster_.getOrganizationPG().getAgency();
    }

    /**  Reads in construction inventory items from inventory file.
     *   Sets up SupplyExpander, WithdrawAllocator, ConstructionInventoryManager and
     *   ExternalAllocator.
     */
    public void decoratePlugin(Organization cluster) {
	if (needConstructionInventory()) {
            //SAE - Reads in inventory file items
            //      Inventory file name needs to be of format <org>_<type>.inv
            //      where type for construction is classivconstructionmaterial
            //      see CONSTRUCTION string defined above.
            //      such as ENG_classivconstructionmaterial.inv
	    thisPlugin_.initializeInventoryFile(CONSTRUCTION);

	    addTaskProcessor(new SupplyExpander(thisPlugin_, cluster_, CONSTRUCTION));
	    addTaskProcessor(new WithdrawAllocator(thisPlugin_, cluster_, CONSTRUCTION, ConstructionConstants.CONSTRUCTIONSUPPLYPROVIDER));
	    addTaskProcessor(new ConstructionInventoryManager(thisPlugin_, cluster_, CONSTRUCTION));
	}
	addTaskProcessor(new ExternalAllocator(thisPlugin_, cluster_, CONSTRUCTION, ConstructionConstants.CONSTRUCTIONSUPPLYPROVIDER));

//  	if (needSimpleTransportAllocator() ) {
//  	    addTaskProcessor(new TerminalTransportAllocator(thisPlugin_, cluster_));
//  	}

//  	if (needTransportAllocator() ) {
//  	    addTaskProcessor(new TransportAllocator(thisPlugin_, cluster_));
//  	}


    }

    private boolean isServiceCluster() {
	return serv_ != null;
    }

    /** Return true indicating that construction inventory is needed
     *  at cluster if parameter to plugin contains +ClassIVConstructionMaterialInventory
     */
    private boolean needConstructionInventory() {

	    Boolean add = need("ClassIVConstructionMaterialInventory");
	    if (add == null)
        return false;
      else
	      return add.booleanValue();
    }

//      private boolean needSimpleTransportAllocator() {
//  	Boolean add = need("SimpleTransportAllocator");
//  	if (add == null)
//  	    return false;
//  	else
//  	    return add.booleanValue();
// 	return (agency_ != null && agency_.equals(Agency.TRANSCOM));
//      }

//      private boolean needTransportAllocator() {
//  	Boolean add = need("TransportAllocator");
//  	if (add == null)
//  	    return false;
//  	else
//  	    return add.booleanValue();
//      }


}
