
package org.cougaar.domain.mlm.construction;

import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.asset.ClassVIIMajorEndItem;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.plan.Service;
import org.cougaar.domain.glm.plugins.PlugInDecorator;
import org.cougaar.domain.glm.plugins.projection.GenerateSupplyDemandExpander;

/**
 * Construction Decorator Stub
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: ConstructionProjectionDecorator.java,v 1.2 2001-04-10 17:39:09 bdepass Exp $
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
    };

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