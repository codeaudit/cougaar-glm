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

package org.cougaar.lib.gss.plugins;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.NewComposition;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

import org.cougaar.core.util.UniqueObject;

import org.cougaar.lib.callback.UTILAggregationCallback;
import org.cougaar.lib.callback.UTILAssetCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;

import org.cougaar.lib.gss.GSTaskGroup;

import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.lib.util.UTILAggregate;
import org.cougaar.lib.util.UTILPluginException;
import org.cougaar.lib.util.UTILPrepPhrase;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.lib.filter.UTILAggregatorPlugin;

/**
 * Abstract because deriving classes need to define 
 * <UL>
 * <LI>NewMPTask getMPTask()
 * </UL>
 * <B>Note:</B> If you redefine needToRescind to return true (in a subclass), 
 * then you must redefine:
 * <UL>
 * <LI>public boolean handleRescindedAggregation(Aggregation)
 * <UL>
 * To properly handle the rescinded Aggregation, and then call super (that is, 
 * the function defined here).
 */
public abstract class UTILGSSAggregatorPlugin 
  extends UTILGSSBufferingPluginAdapter implements UTILAggregatorPlugin {

    /** The following (up to ----------------------) is a copy of the 
     * methods etc. implemented in UTILAggregatorPluginAdapter
     *
     * (See also UTILGSSBufferingPluginAdapter for copied/redefined methods)
     */
    
    /**
     * By default listens for all Assets (please override
     * createAssetCallback to make more specific).  
     * Also listens for aggregations.
     * 
     * Fill in empty functions as needed to add functionality
     *
     * Abstract because does not define :
     *
     * - processTasks (for GSS doesn't need to)
     * - handleFailedAggregation
     */
    
    protected UTILAggregationCallback getAggCallback() { return myAggCallback; }
    /**
     * Override to replace with a callback that has a different predicate
     * or different behaviour when triggered.
     */
    protected UTILAggregationCallback createAggCallback () { 
	return new UTILAggregationCallback  (this); 
    } 
    
    /**
     * The idea is to add subscriptions (via the filterCallback), and when 
     * they change, to have the callback react to the change, and tell 
     * the listener (many times the plugin) what to do.
     *
     * Override and call super to add new filters, or override 
     * createXXXCallback to change callback behaviour.
     *
     * By default adds allocation callback after creating it.
     *
     * @see #createAggCallback
     */
    public void setupFilters () {
	super.setupFilters ();
	
	if (myExtraOutput)
	    System.out.println (getName () + " : Filtering for Aggregations...");
	
	addFilter (myAggCallback    = createAggCallback());
    }
    
    /**
     * Provide the callback that is paired with the buffering thread, which is a
     * listener.  The buffering thread is the listener to the callback
     *
     * @return a WorkflowCallback with the buffering thread as its listener
     */
    protected UTILFilterCallback createThreadCallback (UTILGenericListener 
						       bufferingThread) {
	if (myExtraOutput)
	    System.out.println (getName () + " Filtering for Generic Workflows...");
	
	myWorkflowCallback = new UTILWorkflowCallback  (bufferingThread); 
	return myWorkflowCallback;
    } 
    
    protected UTILFilterCallback getWorkflowCallback () {
	return myWorkflowCallback;
    }
    
    /**
     * Implemented for UTILAggregationListener
     *
     * OVERRIDE to see which task notifications you
     * think are interesting
     *
     * By default, interested in tasks with TRANSPORT verbs
     *
     * @param t task to check for notification
     * @return boolean true if task is interesting
     * @see org.cougaar.planning.ldm.plan.Verb
     */
    public boolean interestingParentTask (Task t) { 
	return interestingTask (t);
    }
    
    /**
     * Implemented for UTILAggregationListener
     *
     * define conditions for rescinding tasks.
     * @param alloc allocation to check for
     * @return boolean true if task needs to be rescinded
     * @see org.cougaar.lib.filter.UTILAllocatorPluginAdapter
     */
    public boolean needToRescind (Aggregation agg) { 
      return false;
    }
    
    public boolean handleRescindedAggregation(Aggregation agg){
	//update the frozen tasks:
	Task agg_t = agg.getTask();
	
	MPTask mission = agg.getComposition().getCombinedTask ();
	Vector tasks = new Vector();
	tasks.add(mission);
	removeFrozenTasks(agg.getReportedResult(), tasks);
	return false;
    }

    /** 
     * Remove aggregation from cluster's log plan
     *
     * Done by 
     *  1) publishRemove of aggregation
     *  2) publishChange of task
     * @param aggregation to remove
     */
    public void publishRemovalOfAggregation (Aggregation aggToRemove) {
      Task changedTask = aggToRemove.getTask ();
      publishRemove (aggToRemove);
      publishChange (changedTask);
    }

    /**
     * Implemented for UTILAggregationListener
     *
     * Updates and publishes allocation result of aggregation.
     * Also must remove the GSTaskGroup from the GSS SchedulerResult
     * storage
     *
     * @param aggregation to report
     */
    public void reportChangedAggregation(Aggregation agg) {

	/*
      Task agg_t = agg.getTask();

      if (agg.getReportedResult().isSuccess ()) {
	MPTask mission = agg.getComposition().getCombinedTask ();

	if (myExtraOutput)
	  System.err.println(myClusterName + " - GSSAggregator found a successful Aggregation - "
			     + agg_t.getUID () + " mission " + 
			     mission.getUID ());

	// If the agg was handled by GSS, i.e. was NOT a prepo mission
	if (UTILPrepPhrase.hasPrepNamed(mission, WITHGROUP)) {
	  // Find appropriate GSTaskGroup in aggregation
	  PrepositionalPhrase ppg = UTILPrepPhrase.getPrepNamed(mission, WITHGROUP);
	  GSTaskGroup group = (GSTaskGroup)ppg.getIndirectObject();
	  
	  // And the transport asset that's moving those tasks
	  PrepositionalPhrase ppa = UTILPrepPhrase.getPrepNamed(mission, Constants.Preposition.WITH);
	  Asset asset = (Asset)ppa.getIndirectObject();
	  
	  // if we haven't done this before
	  if (getSchedulerResult().hasTaskGroup(asset, group)) {
	    // remove it from the scheduler result
	    getSchedulerResult().removeTaskGroupForAsset(asset, group);
	    
	    if (myExtraOutput)
	      System.err.println("Successfully aggregated and removed task group " +
				 "represented by task :" + group.representativeTask()
				 + ": from SchedulerResult");
	  }
	}
      }
	*/

      if (myExtraOutput) {
	System.out.println (getName() + ".reportChangedAggregation on task: " + 
			    agg.getTask().getUID() + " " + 
			    "EstimatedResult: " +
			    agg.getEstimatedResult().auxiliaryQuery(AuxiliaryQueryType.PORT_NAME));
	System.out.println (getName() + ".reportChangedAggregation on task: " + 
			    agg.getTask().getUID() + " " + 
			    "ReportedResult: " +
			    agg.getReportedResult().auxiliaryQuery(AuxiliaryQueryType.PORT_NAME));
      }
      updateAllocationResult (agg);
    }
    
    /**
     * What to do with a successful aggregation. 
     * 
     * Called after updateAllocationResult when needToRescind returns FALSE.
     *
     * @param agg the returned successful aggregation
     * @see #needToRescind
     * @see org.cougaar.lib.filter.UTILAggregatorPluginAdapter#needToRescind
     */
    public void handleSuccessfulAggregation(Aggregation agg) {
	//update the frozen tasks:
	Task agg_t = agg.getTask();
	
	MPTask mission = agg.getComposition().getCombinedTask ();
	Vector tasks = new Vector();
	tasks.add(mission);
	removeFrozenTasks(agg.getReportedResult(), tasks);
    }

  /** 
   * Implemented for UTILAggregationListener
   *
   * Called when an aggregation is removed from the cluster.
   * I.e. an upstream cluster removed an allocation, and this 
   * has resulted in this aggregation being removed.
   *
   * If the plugin maintains some local state of the availability
   * of assets, it should update them here.
   *
   * Does nothing by default.
   */
  public void handleRemovedAggregation (Aggregation agg) {}

  protected UTILWorkflowCallback    myWorkflowCallback;
  protected UTILAggregationCallback myAggCallback;

    /* ---------------------------------------------------- */
    /* End of UTILAggregatorPluginAdapter code */

  /** 
   * Implemented for UTILBufferingPluginAdapter
   * 
   */ 
  public void handleIllFormedTask(Task bad_t) {
      
  }
   
    /** 
     * Implemented for UTILAggregationListener
     *
     * An aggregation has failed.  It's up to the plugin how to deal with the
     * failure.
     *
     * Does nothing by default.
     */
    //public void handleFailedAggregation (Aggregation agg) {}


  /** if the asset is ready to be disposed, dispose of it */
  // !!FIXIT!! make sure this is only ever one aggregation worth of tasks!
  public void makePlanElement (Asset anAsset, GSTaskGroup group) {
    if (myExtraOutput)
      System.err.println(myClusterName + 
			 "GSSAggregator creating aggregation of " +
			 group.getTasks().size() + " tasks with asset " +
			 anAsset.getUID());

    setupAggregation(anAsset, group);

    if (myExtraOutput)
      UTILAggregate.setDebug (true);
    List aggResults = UTILAggregate.makeAggregation(this,
						    ldmf,
						    realityPlan,
						    group.getTasks(),
						    getVerbForAgg(),
						    getPrepPhrasesForAgg(anAsset, group),
						    getDirectObjectsForAgg(group.getTasks()),
						    getPreferencesForAgg(anAsset, group),
						    getClusterIdentifier(),
						    getAVsForAgg(anAsset, group),
						    UTILAllocate.HIGHEST_CONFIDENCE);

    if (myExtraOutput)
      UTILAggregate.setDebug (false);

    prePublishAdjust(aggResults);

    // aggResults includes MPTask
    publishList(aggResults);

    if (myExtraOutput)
      System.err.println(myClusterName + 
			 "GSSAggregator finished aggregating " +
			 group.getTasks().size() + " tasks with asset " +
			 anAsset.getUID());
    cleanupAggregation(anAsset, group, findMPTask(aggResults));
  }

    /** 
     * !!FIXIT!! There has to be a better way to accomplish the following:
     * We need to update not only the MPTask with the 'real' start times of
     * the mission, but also the tasks that compose the mission itself. 
     * Therefore, we use this task to get the actual start so it can be
     * propagated to the other tasks.
     */
  protected MPTask findMPTask(List results) {
    Iterator i = results.iterator();
    while (i.hasNext()) {
      Object next = i.next();
      if (next instanceof MPTask)
	return ((MPTask)next);
    }
    throw new UTILPluginException(myClusterName + " GSSAggregator couldn't find MPTask in list of Aggregation products");
  }

  /**
   * Pre-aggregation work
   * It is expected that this will only be called at the moment before 
   * an aggregation is published (i.e. the group and the asset won't change
   * between now and then).
   * Does nothing by default
   */
  protected void setupAggregation(Asset a, GSTaskGroup g) {}

  /**
   * Post-aggregation, pre-publish work
   * Does nothing by default
   */
  protected void prePublishAdjust(List items_to_publish) {}

  /**
   * Post-publish work
   * Does nothing by default
   */
  protected void cleanupAggregation(Asset a, GSTaskGroup g, MPTask t) {}

  /** 
   * The verb representing the action of the new aggregated MPTask;
   */
  protected abstract Verb getVerbForAgg();

  /**
   * The asset group representing the set of direct objects of the aggregated
   * (parent) tasks; i.e. the direct obj. of the MPTask.
   */
  protected Vector getDirectObjectsForAgg(Vector parentTasks) {
    Vector assets = new Vector();

    Iterator pt_i = parentTasks.iterator();
    // prepPhrases and directObjects
    while (pt_i.hasNext()) {
      Task currentTask = (Task)pt_i.next();
      assets.addElement(currentTask.getDirectObject());
    }

    return assets;
  }

  /**
   * The set of preferences that will be applied to the aggregated MPTask.
   */
  protected abstract Vector getPreferencesForAgg(Asset a, GSTaskGroup g);

  /**
   * The set of prep phrases that will be applied to the aggregated MPTask.
   **/
  protected Vector getPrepPhrasesForAgg(Asset a, GSTaskGroup g) {
    // Add the task group to the mptask with a new preposition s.t. the 
    // group can be found later and removed from the scheduler
    List prep_phrases = registerFrozenTaskGroup(g);
	
    //	PrepositionalPhrase g_pp = 
    //	    UTILPrepPhrase.makePrepositionalPhrase(ldmf, WITHGROUP, g);
    //	prep_phrases.add(g_pp);
    return new Vector (prep_phrases);
  }

  /**
   * The set of aspect values that will be applied to the aggregated MPTask's
   * parent task AllocationResults.
   */
  protected AspectValue [] getAVsForAgg(Asset a, GSTaskGroup g) {
    Vector prefs = getAVVectorForAgg(a, g);
    return makeAVsFromPrefs(prefs);
  }

  /**
   * HACK - Can't override []-returning methods, apparently
   * The set of aspect values that will be applied to the aggregated MPTask's
   * parent task AllocationResults.
   */
  protected Vector getAVVectorForAgg(Asset a, GSTaskGroup g) {
    return getPreferencesForAgg(a, g);
  }

  protected AspectValue [] makeAVsFromPrefs(Vector prefs) {
    Vector tmp_av_vec = new Vector(prefs.size());
    Iterator pref_i = prefs.iterator();
    while (pref_i.hasNext()) {
      // do something really simple for now.
      Preference pref = (Preference) pref_i.next();
      int at = pref.getAspectType();

      ScoringFunction sf = pref.getScoringFunction();
      // allocate as if you can do it at the "Best" point
      double result = ((AspectScorePoint)sf.getBest()).getValue();
      if (at == AspectType.START_TIME)
	  result += 1000.0d;
      tmp_av_vec.addElement(new AspectValue(at, result));
    }      

    AspectValue [] avs = new AspectValue[tmp_av_vec.size()];
    Iterator av_i = tmp_av_vec.iterator();
    int i = 0;
    while (av_i.hasNext())
      avs[i++] = (AspectValue)av_i.next();

    // if there were no preferences...return an empty vector (0 elements)
    return avs;
  }

  protected void publishList(List toPublish) {
    // Publish the aggregation results (?) and the new aggregated MPTask
    Iterator i = toPublish.iterator();
    while (i.hasNext()) {
      Object next_o = i.next();
      boolean success = publishAdd(next_o);

      /*
      if (!success) {
	System.err.println(getName () + " - publishAdding mptask or PEs" +
			   //			   next_o.getUID() +
			   " after Mission Aggregation has FAILED");
	return;
      }
      */
    }
  }

  /**
   * if no asset could be found to handle the task, handle them in some way.
   * Tasks that did not get aggregated should alert (eventually); for now,
   * we throw an exception.  (other possible solution is create failed
   * allocation result somehow?)
   */
  public void handleImpossibleTasks (List unaggregatedTasks) {
    // Just create a String list of all the task names and 
    // report them
    if (myExtraOutput) {
      if (unaggregatedTasks.size() > 0) {
	String task_list = "Failed aggregation for " + unaggregatedTasks.size () + 
	  " tasks:\n";
     
	for (Iterator iter = unaggregatedTasks.iterator ();
	     iter.hasNext (); ) {
	  GSTaskGroup tg = (GSTaskGroup) iter.next ();
	  for (Iterator tgIter = tg.getTasks ().iterator (); tgIter.hasNext ();) {
	    Task unallocatedTask = (Task) tgIter.next ();
	    task_list += unallocatedTask.getUID () + "\t";
	    publishAdd (UTILAggregate.makeFailedAggregation (this, ldmf, unallocatedTask)); 
	  }
	}
	System.err.println (getName () + ".handleImpossibleTasks - " + task_list);
      }
    }
  }

  /** 
   * Aggregator returns true since there is a gap in between aggregation and 
   * allocation when the task group isn't taken into account anywhere by the
   * schedule unless we leave it (frozen) in the scheduler lists.
   * 
   * if we need to wait before removing the task group from the scheduler for
   * some publish results, return true.  NOTE that we must then manually remove
   * the group later or it will just take up unnecessary space forever.  i.e. NOTE
   * that "freeze" implies "don't remove".
   */
  public boolean freezeTaskGroupForPublish(Asset asset, GSTaskGroup group) {
    return true;
  }

}
