/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 1999-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.projection;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.debug.GLMDebug;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.plugins.AssetUtils;
import org.cougaar.glm.plugins.GLMDecorationPlugin;
import org.cougaar.glm.plugins.MaintainedItem;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.measure.Rate;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;

/** Specifies how to create SUPPLY demand tasks for demand projection.
 **/
public class GenerateSupplyDemandExpander extends GenerateDemandExpander {

  /** 
   * @see ProjectionTasksPredicate
   **/
  public GenerateSupplyDemandExpander(GLMDecorationPlugin pi, Organization org, Vector types) {
    super(pi, org, types, new ProjectionTasksPredicate(pi,org,types));
  }

  /** Tasks that published by this processor **/
  public static class ProjectionTasksPredicate implements UnaryPredicate
  {
    MessageAddress clusterId_;
    String myOrgName_;
    Vector resourceTypes_;

    public ProjectionTasksPredicate(GLMDecorationPlugin pi, Organization org, Vector types) {
      myOrgName_ = org.getItemIdentificationPG().getItemIdentification();
      clusterId_ = pi.getMyDelegate().getMessageAddress();
      resourceTypes_ = types;
    }

    /** Looking for GenerateSupply tasks created in this cluster, 
     *  with the correct type of DirectObject, 
     *  that have a 'USINGSUPPLYSOURCE' for this organization. **/
    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task task = (Task)o;
	if (task.getVerb().equals(Constants.Verb.SUPPLY) || 
	    task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
	  if (task.getParentTaskUID() != null) {
	    if (task.getSource().equals(clusterId_)) {
	      if (!TaskUtils.isMyRefillTask(task, myOrgName_)) {
				// OFTYPE check
		PrepositionalPhrase pp =task.getPrepositionalPhrase(Constants.Preposition.OFTYPE) ;
		if (pp != null) {
		  Object obj = pp.getIndirectObject();
		  if (obj instanceof String) {
		    String type = (String)obj;
		    Enumeration enum = resourceTypes_.elements();
		    while (enum.hasMoreElements()) {
		      if (type.equals((String)enum.nextElement())) {
			return true;
		      }
		    }	
		  } else {
		    GLMDebug.ERROR("GenerateSupplyDemandExpander", 
				   "ProjectionTasksPredicate unrec OFTYPE :"+obj);
		  }
		}
	      }
	    }
	  }
	}	
      }
      return false;
    }
  }

  /** Create SUPPLY Task w/  prep phrases
   * @param direct_object requested item (consumed part)
   * @param end end time
   * @param qty quantity of item requested
   * @return supply task for given asset.
   */
  protected Task newDemandTask(Task parent_task, Asset direct_object, 
			       Object consumer, long end, double qty)
  {
    /* create prepositions for the new Supply task */
    Vector pp_vector = demandTaskPrepPhrases(consumer, direct_object, end, parent_task);
    if (pp_vector == null) return null; // Don't need a task

    Vector prefs = createSupplyPreferences(end, qty);

    NewTask t =  (NewTask)buildTask(parent_task, 
				    Constants.Verb.SUPPLY, 
				    direct_object,
				    pp_vector, 
				    prefs.elements());
    //t.setCommitmentDate(end);
    t.setCommitmentDate(new Date(end));
    return t;
  }

  protected Vector createSupplyPreferences(long end, double qty) {
    Vector prefs = new Vector();
    prefs.addElement(createDateAfterPreference(AspectType.START_TIME, 
					       end - 3*MSEC_PER_DAY));
    prefs.addElement(createDateBeforePreference(AspectType.END_TIME, 
						end));
    prefs.addElement(createQuantityPreference(AspectType.QUANTITY, 
					      qty));
    return prefs;
  }

  /** Create PROJECTSUPPLY Task w/  prep phrases
   * @param parent_task
   * @param direct_object requested item (consumed part)
   * @param consumer
   * @param start start time
   * @param end end time
   * @param rate of consumption for item requested
   * @return supply task for given asset.
   */
  protected Task newProjectionTask(Task parent_task, Asset direct_object, 
				   Object consumer, long start, long end, Rate rate,
				   double multiplier)
  {
    /* create prepositions for the new Supply task */
    Vector pp_vector = demandTaskPrepPhrases(consumer, direct_object, end, parent_task);
    if (pp_vector == null) return null; // Don't need a task

    Vector prefs = createProjectionPreferences(start, end, rate, multiplier);

    NewTask t =  (NewTask)buildTask(parent_task, 
				    Constants.Verb.PROJECTSUPPLY, 
				    direct_object,
				    pp_vector, 
				    prefs.elements());
    //t.setCommitmentDate(end);
    t.setCommitmentDate(new Date(end));
    return t;
  }

  protected Vector createProjectionPreferences(long start, long end, Rate rate, double mult) {
    ScoringFunction score;
    Vector prefs = new Vector();

    score = ScoringFunction.createStrictlyAtValue(AspectValue.newAspectValue(AspectType.START_TIME, start));
    prefs.addElement(ldmFactory_.newPreference(AspectType.START_TIME, score));

    score = ScoringFunction.createStrictlyAtValue(AspectValue.newAspectValue(AspectType.END_TIME, end));
    prefs.addElement(ldmFactory_.newPreference(AspectType.END_TIME, score));

    prefs.addElement(TaskUtils.createDemandRatePreference(ldmFactory_, rate));
    prefs.addElement(TaskUtils.createDemandMultiplierPreference(ldmFactory_, mult));
    return prefs;
  }

  /** Create FOR, TO, MAINTAIN, and OFTYPE prepositional phrases
   *  for use by the subclasses.
   * @param consumer the consumer the task support
   * @param time - used to find the OPlan and the geoloc for the TO preposition
   * @return Vector of PrepostionalPhrases
   * @see #createDemandTasks
   **/
  protected Vector demandTaskPrepPhrases(Object consumer, Asset resource, 
					 long time, Task parent_task) 
  {
    /* create prepositions for the new demand task */
    Vector pp_vector = new Vector();
    PrepositionalPhrase pp =parent_task.getPrepositionalPhrase(Constants.Preposition.OFTYPE) ;
    if (pp != null) {
      Object obj = pp.getIndirectObject();
      if (obj instanceof String) {
	pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.OFTYPE, (String)obj));
      }
    }				

    pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.FOR, myOrgName_));

    // when oplan id added to tasks....
    // 	pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.FOROPLAN, oplan));

    GeolocLocation geoloc = getGeolocLocation(parent_task, time);
    if (geoloc != null) {
      pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.TO, geoloc));
      //    	    printDebug("demandTaskPrepPhrases(), GeolocLocation is "+geoloc+" for "+TaskUtils.shortTaskDesc(parent_task));
    }
    else { // Try to use HomeLocation
      try {
	//    		printDebug("demandTaskPrepPhrases(), Using HomeLocation for transport");
	geoloc = (GeolocLocation)myOrganization_.getMilitaryOrgPG().getHomeLocation();
	pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.TO, geoloc));
      } catch (NullPointerException npe) {
	printError("demandTaskPrepPhrases(), Unable to find Location for Transport");
      }
    }  

    if (consumer != null) {
      MaintainedItem itemID;
      if (consumer instanceof Asset) {
	TypeIdentificationPG tip = ((Asset)consumer).getTypeIdentificationPG();
	ItemIdentificationPG iip = ((Asset)consumer).getItemIdentificationPG();
	if (iip != null) {
	  itemID = MaintainedItem.findOrMakeMaintainedItem("Asset", tip.getTypeIdentification(),
							   iip.getItemIdentification(), tip.getNomenclature());
	} else {
	  itemID = MaintainedItem.findOrMakeMaintainedItem("Asset", tip.getTypeIdentification(), 
							   null, tip.getNomenclature());
	}
      } else {
	itemID = MaintainedItem.findOrMakeMaintainedItem("Other", consumer.toString(), null, null);
      }
      pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.MAINTAINING, itemID));
    }

    return pp_vector;
  }

  protected GeolocLocation getGeolocLocation(Task parent_task, long time) {
    Enumeration geolocs = AssetUtils.getGeolocLocationAtTime(myOrganization_, time);
    if (geolocs.hasMoreElements()) {
      GeolocLocation geoloc = (GeolocLocation)geolocs.nextElement();
//    GLMDebug.DEBUG("GenerateSupplyDemandExpander", clusterId_, "At "+TimeUtils.dateString(time)+ " the geoloc is "+geoloc);
      return geoloc;
    }
    return null;
  }
}

