/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.sample;


import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.NewComposition;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;

import org.cougaar.planning.ldm.asset.AssetGroup;

import org.cougaar.util.UnaryPredicate;

import java.lang.Math;

import java.util.Enumeration;
import java.util.Vector;

public class MCCAggregatorPlugin extends SimplePlugin
{

    private IncrementalSubscription myTasks;
    private IncrementalSubscription myAggs;
    private Vector tasks_to_aggregate = new Vector();
    private static long ONE_DAY = 86400000L;

    // Predicate for _ALL_ TRANSPORT Tasks
    private static UnaryPredicate taskPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  if ( o instanceof Task ) {
	    Task t = (Task)o;
	    if ( t.getVerb().equals( Constants.Verb.TRANSPORT ))
	      return true;
	  }
	  return false;
	}
      };
    }

    // Predicate for my Aggregations
    private static UnaryPredicate aggPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  return ( o instanceof Aggregation );
	}
      };
    }
   
    public void setupSubscriptions() {
	// Setup Subscription to all incoming Transport Tasks
	myTasks = (IncrementalSubscription)subscribe( taskPred() );
	myAggs = (IncrementalSubscription)subscribe( aggPred() );
    }

    public void execute() {
      if ( myTasks.hasChanged() ) {
	    //System.err.println( "\n" + this + ":myTasks.hasChanged!" );
	    Enumeration newTasks = myTasks.getAddedList();
	    while ( newTasks.hasMoreElements() ) {
		Task newTask = (Task) newTasks.nextElement();
		if (newTask.getPlanElement() == null) {
		  aggregate(newTask);
		}
	    }
	    Enumeration changedTasks = myTasks.getChangedList();
	    while ( changedTasks.hasMoreElements() ) {
		Task changedTask = (Task)changedTasks.nextElement();
		PlanElement oldPE = changedTask.getPlanElement();
		if (oldPE != null) {
		  publishRemove(oldPE);
		}
		aggregate(changedTask);
	    }
	}
	
	if ( myAggs.hasChanged() ) {
	    Enumeration changedAggs = myAggs.getChangedList();
	    while ( changedAggs.hasMoreElements() ) {
		Aggregation agg = (Aggregation)changedAggs.nextElement();
		updateAllocationResult( agg );
	    }
	}

	//} else {
	// This 'auto-purging' on every execute() call is not the right
	// thing here; but until it is discovered why the myTasks collection shows
	// up as hasChanged() == true (always), this will take its place...
	// Purge the waiting list...
	if ( !(tasks_to_aggregate.isEmpty())) {
	    //System.err.println( "\n" + this + ": Purging..." );
	    Enumeration tasks = tasks_to_aggregate.elements();
	    while ( tasks.hasMoreElements() ) {
	      Task failedTask = (Task) tasks.nextElement();
	      Vector tmp_tasks = new Vector(1);
	      tmp_tasks.addElement(failedTask);
	      private_createAggregation(tmp_tasks);
	    }
	    tasks_to_aggregate.removeAllElements();
	}
	wakeAfter( 15000L );
    }

    private void aggregate( Task task ) {
	/*
	 * We want to group like Tasks together here; by "like" we mean:
	 * - Same Verb (Transport)
	 * - Make Aggregations of two Tanks/HET
	 * - Tasks have same locations
	 * - Tasks have same Dates (allow some fuzziness here)
	 *
	 * Initially, this is going to use a simple approach, using 
	 * the DefaultARDistributer.
	 */
	Enumeration waitingTasks = tasks_to_aggregate.elements();
	while ( waitingTasks.hasMoreElements() ) {
	    Task waitingTask = (Task)waitingTasks.nextElement();
	    if ( compare( waitingTask, task ) ) {
		tasks_to_aggregate.remove( waitingTask );
		Vector tmp_tasks = new Vector(2);
		tmp_tasks.addElement( waitingTask );
		tmp_tasks.addElement( task );
		private_createAggregation( tmp_tasks );
		return;
	    }
	}
	tasks_to_aggregate.addElement( task );
    }


    private boolean compare( Task task1, Task task2 ) {
	if ((task1.getVerb().equals( Constants.Verb.TRANSPORT )) &&
	    (task2.getVerb().equals( Constants.Verb.TRANSPORT ))) {
	    Enumeration task1PPs = task1.getPrepositionalPhrases();
	    Enumeration task2PPs = task2.getPrepositionalPhrases();
	    PrepositionalPhrase task1From = null;
	    PrepositionalPhrase task2From = null;
	    PrepositionalPhrase task1To = null;
	    PrepositionalPhrase task2To = null;
	    while ( task1PPs.hasMoreElements() ) {
		PrepositionalPhrase pp = (PrepositionalPhrase)task1PPs.nextElement();
		if ( pp.getPreposition().equals( Constants.Preposition.FROM ))
		    task1From = pp;
		if ( pp.getPreposition().equals( Constants.Preposition.TO ))
		    task1To = pp;
	    }
	    while ( task2PPs.hasMoreElements() ) {
		PrepositionalPhrase pp = (PrepositionalPhrase)task2PPs.nextElement();
		if ( pp.getPreposition().equals( Constants.Preposition.FROM ))
		    task2From = pp;
		if ( pp.getPreposition().equals( Constants.Preposition.TO ))
		    task2To = pp;
	    }
	    if ( (task1From != null) && (task2From != null) &&
		 (task1To != null) && (task2To != null)) {
		GeolocLocation t1FromWhere = (GeolocLocation)task1From.getIndirectObject();
		GeolocLocation t2FromWhere = (GeolocLocation)task2From.getIndirectObject();
		GeolocLocation t1ToWhere = (GeolocLocation)task1To.getIndirectObject();
		GeolocLocation t2ToWhere = (GeolocLocation)task2To.getIndirectObject();
		if ( (t1FromWhere.getGeolocCode().equals(t2FromWhere.getGeolocCode())) &&
		     (t1ToWhere.getGeolocCode().equals(t2ToWhere.getGeolocCode())) ) {
		    Enumeration t1Prefs = task1.getPreferences();
		    Enumeration t2Prefs = task2.getPreferences();
		    // We're really only concerned with the END_TIMEs here...
		    Preference t1EndPref = null;
		    Preference t2EndPref = null;
		    while ( t1Prefs.hasMoreElements() ) {
			Preference pref = (Preference)t1Prefs.nextElement();
			if ( pref.getAspectType() == AspectType.END_TIME )
			    t1EndPref = pref;
		    }
		    while ( t2Prefs.hasMoreElements() ) {
			Preference pref = (Preference)t2Prefs.nextElement();
			if ( pref.getAspectType() == AspectType.END_TIME )
			    t2EndPref = pref;
		    }
		    if ( (t1EndPref != null) &&
			 (t2EndPref != null)) {
			long diff = t1EndPref.getScoringFunction().getBest().getAspectValue().longValue() - 
			    t2EndPref.getScoringFunction().getBest().getAspectValue().longValue();
			
			if (Math.abs( diff ) < ONE_DAY)
			    
			    return true;
		    }
		}
	    }
	} 
	return false;
    }

    //private void createAggregation( Task task1, Task task2 ) {
    private void private_createAggregation( Vector tasks ) {
	//Vector tasks = new Vector();
	//tasks.addElement( task1 );
	//tasks.addElement( task2 );
	if ( !(tasks.isEmpty()) ) {
	    Vector assets = getAssets( tasks );
	    //assets.addElement( task1.getDirectObject() );
	    //assets.addElement( task2.getDirectObject() );
	    
	    NewMPTask combTask = theLDMF.newMPTask();
	    combTask.setParentTasks( tasks.elements() );
	    combTask.setVerb(Verb.get(Constants.Verb.TRANSPORTATIONMISSION));
	    AssetGroup ag = new AssetGroup();
	    ag.setAssets( assets );
	    combTask.setDirectObject( ag );
	    combTask.setPlan( private_getPlan( tasks ) );
	    combTask.setPrepositionalPhrases( private_getPrepositionalPhrases( tasks ) );
	    combTask.setPreferences( private_getPreferences( tasks ) );
	    // set a request for AuxiliaryQueryType FAILED_REASON
	    int[] aqts = {AuxiliaryQueryType.FAILURE_REASON};
	    combTask.setAuxiliaryQueryTypes(aqts);
	    
	    
	    NewComposition comp = theLDMF.newComposition();
	    Vector aggregations = new Vector();
	    for ( Enumeration e = tasks.elements() ; e.hasMoreElements() ; ) {
		Task tmp_task = (Task)e.nextElement();
		//comp.addParentTask( tmp_task );
		Aggregation agg = theLDMF.createAggregation(theLDMF.getRealityPlan(),
							    tmp_task,
							    comp,
							    null);
		aggregations.addElement( agg );
	    }
       	    combTask.setComposition( comp );
	    comp.setCombinedTask( combTask );
	    comp.setAggregations(aggregations);
      
	    for ( Enumeration agg_enum = aggregations.elements() ; agg_enum.hasMoreElements() ; ) {
		Aggregation agg = (Aggregation)agg_enum.nextElement();
		publishAdd( agg );
		//System.err.println( "\n" + this + ": publishadd( " + agg + " )" );
	    }
	    publishAdd( combTask );
	    //System.err.println( "\n" + this + ": publishadd( " + combTask + " )" );
	}
    }
	
    //
    // All of the following will only work under the conditions 
    // where the Tasks belonging to this Vector are from the same Plan,
    // have the same (limited) PrepositionalPhrases (basically TO and FROMs)
    // match, and have the same Preferences (or at least the END_TIME Aspects
    // are the same).
    //
    private Plan private_getPlan( Vector tasks ) {
	return ((Task)tasks.firstElement()).getPlan();
    }

    private Enumeration private_getPrepositionalPhrases( Vector tasks ) {
	return ((Task)tasks.firstElement()).getPrepositionalPhrases();
    }

    private Enumeration private_getPreferences( Vector tasks ) {
	return ((Task)tasks.firstElement()).getPreferences();
    }


    private Vector getAssets( Vector tasks ) {
	Vector assets = new Vector();
	Enumeration task_enum = tasks.elements();
	while ( task_enum.hasMoreElements() ) {
	    Task tmp_task = (Task)task_enum.nextElement();
	    assets.addElement( tmp_task.getDirectObject() );
	}
	return assets;
    }

    private void updateAllocationResult( Aggregation agg ) {
	if ( agg.getReportedResult() != null ) {
	    //System.err.println( "\n" + this + ": updateAllocationResult(" + agg + ")" );
	    // for now just compare the allocation result instances
	    // if they are different objects pass them back up regardless
	    // of their content equalness.
	    AllocationResult reportedresult = agg.getReportedResult();
	    AllocationResult estimatedresult = agg.getEstimatedResult();
	    if ( (estimatedresult == null) || (! (estimatedresult == reportedresult) ) ) {
		agg.setEstimatedResult(reportedresult);
		// Publish the change (let superclass handle transactions)
		publishChange( agg );
	    }
	}
    }

}
