/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.strategictransport;

// Simple scheduler to generate multileg expansions for transporation tasks
// To be used as a TOPS stub in conjunction with GSS for AIR, GROUND, SEA

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import java.util.*;
import org.cougaar.util.UnaryPredicate;

public class SimpleMultilegExpanderPlugIn extends org.cougaar.core.plugin.SimplePlugIn
{

  // Subscription for all 'Transport' tasks
  private IncrementalSubscription allTransportTasks;
  private UnaryPredicate allTransportTasksPredicate = new UnaryPredicate() { 
    public boolean execute(Object o) {
      return (o instanceof Task) && (((Task)o).getVerb().equals(Constants.Verb.Transport));
    }
  };

  // Subscription for all 'Transport' expansions
  private IncrementalSubscription allTransportExpansions;
  private UnaryPredicate allTransportExpansionsPredicate = new UnaryPredicate() { 
    public boolean execute(Object o) {
      return (o instanceof Expansion) && 
      (((Expansion)o).getTask().getVerb().equals(Constants.Verb.Transport));
    }
  };

  public void setupSubscriptions() 
  {
    // Subscribe to all 'Transport' tasks
    allTransportTasks = (IncrementalSubscription)subscribe(allTransportTasksPredicate);

    // Subscribe to all 'Transport' expansions
    allTransportExpansions = (IncrementalSubscription)subscribe(allTransportExpansionsPredicate);
  }

  public void execute() 
  {
    //Handle new transport tasks
    for(Enumeration t_added = allTransportTasks.getAddedList(); 
	t_added.hasMoreElements();)
      {
	Task task = (Task)t_added.nextElement();
	handleTask(task);
      }
    
    // Handle modified transport tasks by unallocating them and reallocating them
    for(Enumeration t_changed = allTransportTasks.getChangedList(); 
	t_changed.hasMoreElements();)
      {
	Task task = (Task)t_changed.nextElement();
	reHandleTask(task);
      }

    // Copy reported results to expected results on expansions
    for(Enumeration e_changed = allTransportExpansions.getChangedList();
	e_changed.hasMoreElements();)
	{
	    Expansion exp = (Expansion)e_changed.nextElement();
	    exp.setEstimatedResult(exp.getReportedResult());
	}

  }

  // Generate multi-leg schedule for task, storing schedule information
  protected void handleTask(Task task) 
  {
      AllocationResult estAR = null;
      NewWorkflow new_wf = theLDMF.newWorkflow();
      new_wf.setParentTask(task);

      // Hard code some POD/POD locations/duration for Sea travel
      long travel_days = 16l;
      GeolocLocation POE = createGeolocLocation("UZXJ", 32.0836, -81.1167);
      GeolocLocation POD = createGeolocLocation("LWEV", 27, 49.6667);
      String mode_verb = "TransportBySea";

      // If we're going by air (passengers), then use different locations/duration
      if (goesByAirMode(task.getDirectObject())) {
	  mode_verb = "TransportByAir";
	  POE = createGeolocLocation("LEXG", 32.0097, -81.1461);
	  POD = createGeolocLocation("FFTJ", 26.26389, 50.15833);
	  travel_days = 3l;
      }

      double to_date_double = 
	  task.getPreference(AspectType.END_TIME).getScoringFunction().getBest().getValue();
      Date to_date = new Date((long)to_date_double);
      Date from_date = new Date(to_date.getTime() - (travel_days * ONE_DAY));
      Location from_loc = 
	  (Location)task.getPrepositionalPhrase(Constants.Preposition.FROM).getIndirectObject();
      Location to_loc = 
	  (Location)task.getPrepositionalPhrase(Constants.Preposition.TO).getIndirectObject();
      
      // Assume one day for ground transportation on either side
      Date poe_date = new Date(from_date.getTime() + ONE_DAY);
      Date pod_date = new Date(to_date.getTime() - ONE_DAY);


      // GROUND leg FORT TO POE
      Task leg1 = createSubTask(task, "TransportByGround", from_loc, POE, from_date, poe_date);
      new_wf.addTask(leg1);
      ((NewTask)leg1).setWorkflow(new_wf);

      // SEA/AIR leg POE TO POD
      Task leg2 = createSubTask(task, mode_verb, POE, POD, poe_date, pod_date);
      new_wf.addTask(leg2);
      ((NewTask)leg2).setWorkflow(new_wf);

      // GROUND leg POD to TAA
      Task leg3 = createSubTask(task, "TransportByGround", POD, to_loc, pod_date, to_date);
      new_wf.addTask(leg3);
      ((NewTask)leg3).setWorkflow(new_wf);

      Expansion new_exp = theLDMF.createExpansion(task.getPlan(), 
						  task,
						  new_wf,
						  estAR);

      publishAdd(leg1);
      publishAdd(leg2);
      publishAdd(leg3);
      publishAdd(new_wf);
      publishAdd(new_exp);
      System.out.println("Publishing expansion (3 legs) of task : " + task);

  }

  // Remove task from any expansion PE and re-handle
  protected void reHandleTask(Task task)
  {
    PlanElement plan_element = task.getPlanElement();
    if (plan_element != null) {
      publishRemove(plan_element);
    }
    System.out.println("Rescheduling changed task : " + task);
    handleTask(task);
  }

    // Create a subtask for given parent and verb, start/end locations and dates
    // Copy other information from parent task
    protected Task createSubTask(Task parent,
				 String verb, 
				 Location from_loc,
				 Location to_loc,
				 Date earliest_start,
				 Date latest_end)
    {
	// Set up a new task
	NewTask new_task = theLDMF.newTask();

	// Set up parent relationship of subtask
	new_task.setParentTask(parent);

	// Set up Direct object  for task from parent
	new_task.setDirectObject(parent.getDirectObject());

	// Set up prepositions for task
	Vector prepositions = new Vector();

	// Set up the FROM and TO Prepositional Phrases
	NewPrepositionalPhrase npp = theLDMF.newPrepositionalPhrase();
	npp.setPreposition(Constants.Preposition.FROM);
	npp.setIndirectObject(from_loc);
	prepositions.add(npp);

	npp = theLDMF.newPrepositionalPhrase();
	npp.setPreposition(Constants.Preposition.TO);
	npp.setIndirectObject(to_loc);
	prepositions.add(npp);

	// Copy all prepositions that aren't FROM and TO from the parent task
	for(Enumeration preps = parent.getPrepositionalPhrases();
	    preps.hasMoreElements();) 
	    {
		PrepositionalPhrase pp = (PrepositionalPhrase)preps.nextElement();
		if ((!pp.getPreposition().equals(Constants.Preposition.TO)) &&
		    (!pp.getPreposition().equals(Constants.Preposition.FROM))) {
		    prepositions.add(pp);
		}
	    }

	new_task.setPrepositionalPhrases(prepositions.elements());
	
	// Set the verb as given
	new_task.setVerb(new Verb(verb));

	// Set the plan for the task
	new_task.setPlan(parent.getPlan());

	// Establish preferences for task
	Vector preferences = new Vector();

	// Set up START_TIME preference
	ScoringFunction start_scorefcn = 
	    ScoringFunction.createNearOrAbove
	    (new AspectValue(AspectType.START_TIME, 
			     (double)earliest_start.getTime()), 
	     0.0d);

	Preference start_pref = theLDMF.newPreference(AspectType.START_TIME, start_scorefcn);
	preferences.addElement(start_pref);
	
	// Set up preference for END_TIME
	ScoringFunction end_scorefcn = 
	    ScoringFunction.createStrictlyBetweenWithBestValues
	    (new AspectValue(AspectType.END_TIME, 
			     (double)(latest_end.getTime() - (0l*ONE_DAY))),
	     new AspectValue(AspectType.END_TIME,
			     (double)(latest_end.getTime() + (2l*ONE_DAY))),
	     new AspectValue(AspectType.END_TIME, 
			     (double)(latest_end.getTime() + (7l*ONE_DAY))));

	Preference end_pref = theLDMF.newPreference(AspectType.END_TIME, end_scorefcn);
	preferences.addElement(end_pref);
	
	// Copy all preferences that aren't START_TIME and END_TIME
	for(Enumeration prefs = parent.getPreferences();prefs.hasMoreElements();) 
	    {
		Preference pref = (Preference)prefs.nextElement();
		if ((pref.getAspectType() != AspectType.START_TIME) &&
		    (pref.getAspectType() != AspectType.END_TIME)) {
		    preferences.addElement(pref);
		}
	    }

	new_task.setPreferences(preferences.elements());

	return new_task;
    }

    // Create a simple geoloc code for a code and lat/logn
    protected GeolocLocation createGeolocLocation(String  code, 
						  double latitude, double longitude)
    {
	NewGeolocLocation loc = new GeolocLocationImpl();
	loc.setGeolocCode(code);
	loc.setLatitude(Latitude.newLatitude(latitude));
	loc.setLongitude(Longitude.newLongitude(longitude));
	return loc;
    }

    // People go by air, all others go by sea...
    private boolean goesByAirMode(Asset asset)
    {
	if (asset instanceof AggregateAsset) {
	    return goesByAirMode(((AggregateAsset)asset).getAsset());
	}
	else 
	    return (asset instanceof Person);
    }

    // Constants

    private final static long ONE_DAY = 86400000;
				 

}
