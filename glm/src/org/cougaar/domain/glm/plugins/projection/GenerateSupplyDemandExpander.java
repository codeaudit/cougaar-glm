/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.projection;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.LocationSchedulePG;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.debug.GLMDebug;
import org.cougaar.domain.glm.plugins.ClusterOPlan;
import org.cougaar.domain.glm.plugins.GLMDecorationPlugIn;
import org.cougaar.domain.glm.plugins.AssetUtils;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;

/** Specifies how to create SUPPLY demand tasks for demand projection.
 **/
public class GenerateSupplyDemandExpander extends GenerateDemandExpander {

    /** 
     * @see Class#ProjectionTasksPredicate
     **/
    public GenerateSupplyDemandExpander(GLMDecorationPlugIn pi, Organization org, Vector types) {
	super(pi, org, types, new ProjectionTasksPredicate(pi,org,types));
    }

    /** Tasks that published by this processor **/
    static class ProjectionTasksPredicate implements UnaryPredicate
    {
	ClusterIdentifier clusterId_;
	String myOrgName_;
	Vector resourceTypes_;

	public ProjectionTasksPredicate(GLMDecorationPlugIn pi, Organization org, Vector types) {
	    myOrgName_ = org.getItemIdentificationPG().getItemIdentification();
	    clusterId_ = pi.getMyDelegate().getClusterIdentifier();
	    resourceTypes_ = types;
	}

	/** Looking for GenerateSupply tasks created in this cluster, 
	 *  with the correct type of DirectObject, 
	 *  that have a 'USINGSUPPLYSOURCE' for this organization. **/
	public boolean execute(Object o) {
	    if (o instanceof PlanElement) {
		Task task = ((PlanElement)o).getTask();
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
					    if (type.equals((String)enum.nextElement()))
						return true;
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
    };

    /** Create SUPPLY Task w/  prep phrases
     * @param parent_task
     * @param direct_object requested item (consumed part)
     * @param consumer
     * @param start start time
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

	score = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.START_TIME, start));
	prefs.addElement(ldmFactory_.newPreference(AspectType.START_TIME, score));

	score = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.END_TIME, end));
	prefs.addElement(ldmFactory_.newPreference(AspectType.END_TIME, score));

        prefs.addElement(TaskUtils.createDemandRatePreference(ldmFactory_, rate));
        prefs.addElement(TaskUtils.createDemandMultiplierPreference(ldmFactory_, mult));
	return prefs;
    }

    /** Create FOR, TO, MAINTAIN, and OFTYPE prepositional phrases
     *  for use by the subclasses.
     * @param maintain the consumer the task support
     * @param resource
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
	    StringBuffer consumerID = new StringBuffer();
	    if (consumer instanceof Asset) {
		consumerID.append("Asset:").append(((Asset)consumer).getTypeIdentificationPG().getTypeIdentification());
	    } else {
		consumerID.append("Other:").append(consumer.toString());
	    }
	    pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.MAINTAINING, consumerID.toString()));
	}

	return pp_vector;
    }

    protected GeolocLocation getGeolocLocation(Task parent_task, long time) {
	Enumeration geolocs = AssetUtils.getGeolocLocationAtTime(myOrganization_, time);
	if (geolocs.hasMoreElements()) {
	    GeolocLocation geoloc = (GeolocLocation)geolocs.nextElement();
//  	    GLMDebug.DEBUG("GenerateSupplyDemandExpander", clusterId_, "At "+TimeUtils.dateString(time)+ " the geoloc is "+geoloc);
	    return geoloc;
	}
	return null;
    }
}

