/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

import java.util.Vector;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.PluginDecorator;
import org.cougaar.glm.plugins.projection.GenerateSupplyDemandExpander;
import org.cougaar.util.UnaryPredicate;

/**
 * Construction Decorator Stub
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/

public class ConstructionProjectionDecorator extends PluginDecorator {
    private static final String            CLASSNAME = "ConstructionProjectionDecorator";
    // move to the plug in
    Service serv_;
    ConstructionProjectionPlugin thisPlugin_;
    Organization cluster_;
    
    /**  @param plugin to be configured */
    public ConstructionProjectionDecorator(ConstructionProjectionPlugin plugin) {
	    super(plugin);
	    thisPlugin_ = plugin;
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
    public void decoratePlugin(Organization cluster) {
      cluster_ = cluster;
	    Service serv_ = cluster_.getOrganizationPG().getService();
	    // assuming that 'end' clusters have agencies
	    String theater_ = thisPlugin_.getTheater();

	    addTaskProcessor( new ConstructionGenerateDemandProjector(thisPlugin_, cluster_));
    	// This processor is in GLM and creates supply tasks when the flag '+ConstructionSupplyTasks' is in
	    // the parameter list of this plugin

      // CDW - not supply, or is it?      - no part_types
      // Vector part_types = thisPlugin_.getParameters(); this get us nothing...
      Vector partTypes = new Vector();
      partTypes.add("ClassIVConstructionMaterial");
      System.out.println("Decorating with part_type of " + partTypes.elementAt(0));
      addTaskProcessor( new GenerateSupplyDemandExpander(thisPlugin_, cluster_, partTypes));
    }

}
