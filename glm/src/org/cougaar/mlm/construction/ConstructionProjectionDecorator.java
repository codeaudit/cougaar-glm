/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.glm.ldm.asset.ClassVIIMajorEndItem;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.PlugInDecorator;
import org.cougaar.glm.plugins.projection.GenerateSupplyDemandExpander;

/**
 * Construction Decorator Stub
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/

public class ConstructionProjectionDecorator extends PlugInDecorator {
    private static final String            CLASSNAME = "ConstructionProjectionDecorator";
    // move to the plug in
    Service serv_;
    ConstructionProjectionPlugIn thisPlugIn_;
    Organization cluster_;
    
    /**  @param plugin to be configured */
    public ConstructionProjectionDecorator(ConstructionProjectionPlugIn plugin) {
	    super(plugin);
	    thisPlugIn_ = plugin;
    }

    /** Predicate for MEIs - used to define consumers for Construction items.
     *  I'm not sure what consumes construction items but this predicate needs
     *  to look for it.
     **/
    static class ConsumerPredicate implements UnaryPredicate {
      public boolean execute(Object o) {
      //CDW - consumer is the org
      //	    if (o instanceof ConstructionConsumer) {
	      if (o instanceof Organization) {
		      return true;
	      }
	      return false;
	    }
    }

    /** 
     *  Customizes the given ConstructionProjectionPlugin with one or more BasicProcessor 
     *  depending on information from the organizational asset 
     *  this cluster represents.
     *  @param cluster the given plugin is loaded into
     */
    public void decoratePlugIn(Organization cluster) {
      cluster_ = cluster;
	    Service serv_ = cluster_.getOrganizationPG().getService();
	    // assuming that 'end' clusters have agencies
	    String theater_ = thisPlugIn_.getTheater();

	    addTaskProcessor( new ConstructionGenerateDemandProjector(thisPlugIn_, cluster_));
    	// This processor is in GLM and creates supply tasks when the flag '+ConstructionSupplyTasks' is in
	    // the parameter list of this plugin

      // CDW - not supply, or is it?      - no part_types
      // Vector part_types = thisPlugIn_.getParameters(); this get us nothing...
      Vector partTypes = new Vector();
      partTypes.add("ClassIVConstructionMaterial");
      System.out.println("Decorating with part_type of " + partTypes.elementAt(0));
      addTaskProcessor( new GenerateSupplyDemandExpander(thisPlugIn_, cluster_, partTypes));
    }

}
