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

import org.cougaar.glm.plugins.*;
import org.cougaar.glm.plugins.inventory.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.Constants;

import java.io.*;
import java.lang.*;
import java.util.*;

 /** Establishes necessary processes for Construction Inventory.
  * @author  ALPINE <alpine-software@bbn.com>
  *
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
