/*
 * <copyright>
 *  Copyright 1997-2001 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

 package org.cougaar.domain.mlm.construction;

import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.plugins.inventory.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.Constants;

import java.io.*;
import java.lang.*;
import java.util.*;

 /** Establishes necessary processes for Construction Inventory.
  * @author  ALPINE <alpine-software@bbn.com>
  * @version $Id: ConstructionInventoryDecorator.java,v 1.2 2001-04-10 17:39:08 bdepass Exp $
  **/
 public class ConstructionInventoryDecorator extends  PlugInDecorator {
    private static final String            CLASSNAME = "ConstructionInventoryDecorator";
    private static final String          CONSTRUCTION = "ClassIVConstructionMaterial";

    InventoryPlugIn thisPlugIn_;
    Service serv_;
    Agency agency_;
    Organization cluster_;

    /** Constructor */
    public ConstructionInventoryDecorator (InventoryPlugIn plugin, Organization cluster) {
	super(plugin);
	thisPlugIn_ = plugin;
	cluster_ = cluster;
	serv_ = cluster_.getOrganizationPG().getService();
	agency_ = cluster_.getOrganizationPG().getAgency();
    }

    /**  Reads in construction inventory items from inventory file.
     *   Sets up SupplyExpander, WithdrawAllocator, ConstructionInventoryManager and
     *   ExternalAllocator.
     */
    public void decoratePlugIn(Organization cluster) {
	if (needConstructionInventory()) {
            //SAE - Reads in inventory file items
            //      Inventory file name needs to be of format <org>_<type>.inv
            //      where type for construction is classivconstructionmaterial
            //      see CONSTRUCTION string defined above.
            //      such as ENG_classivconstructionmaterial.inv
	    thisPlugIn_.initializeInventoryFile(CONSTRUCTION);

	    addTaskProcessor(new SupplyExpander(thisPlugIn_, cluster_, CONSTRUCTION));
	    addTaskProcessor(new WithdrawAllocator(thisPlugIn_, cluster_, CONSTRUCTION, ConstructionConstants.CONSTRUCTIONSUPPLYPROVIDER));
	    addTaskProcessor(new ConstructionInventoryManager(thisPlugIn_, cluster_, CONSTRUCTION));
	}
	addTaskProcessor(new ExternalAllocator(thisPlugIn_, cluster_, CONSTRUCTION, ConstructionConstants.CONSTRUCTIONSUPPLYPROVIDER));

//  	if (needSimpleTransportAllocator() ) {
//  	    addTaskProcessor(new TerminalTransportAllocator(thisPlugIn_, cluster_));
//  	}

//  	if (needTransportAllocator() ) {
//  	    addTaskProcessor(new TransportAllocator(thisPlugIn_, cluster_));
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