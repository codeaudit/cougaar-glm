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

package org.cougaar.lib.util;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;

import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.NewComposition;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Priority;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cougaar.lib.filter.UTILPlugin;

/**
 * This class contains utility functions for creating
 * Aggregations.
 */

public class UTILAggregate {
  private static String myName = "UTILAggregate";

  private static boolean debug = false;

  /**
   * Set to true to see debug output -- tie to myExtraExtraOutput?
   * @param dbg -- debug switch
   */
  public static void setDebug (boolean dbg) { debug = dbg; }

  /**
   * Creates an Aggregation for every parent task.
   *
   * @param ldmf the RootFactory
   * @param pts an enum of parent tasks
   * @param subtasks a vector of subtasks created from the parent tasks
   * @param estimated AllocationResult
   * @param Vector filled with created aggregation plan elements
   * @return MPTask representing composition
   */

  public static List makeAggregation(UTILPlugin creator,
									 RootFactory ldmf,
									 Plan realityPlan,
									 Vector parentTasks, 
									 Verb whatVerb,
									 Vector prepPhrases,
									 Vector directObjects,
									 Vector preferences,
									 ClusterIdentifier sourceClusterID,
									 AspectValue [] aspectValues,
									 double confidence) {
    List stuffToPublish = new ArrayList ();

    NewMPTask mptask = UTILAggregate.makeMPSubTask(ldmf,
												   realityPlan,
												   parentTasks.elements(),
												   whatVerb,
												   prepPhrases.elements(),
												   UTILAsset.makeAssetGroup(ldmf, directObjects),
												   preferences.elements(),
												   Priority.UNDEFINED,
												   sourceClusterID);

    // create the new composition
    NewComposition comp = ldmf.newComposition ();
    comp.setCombinedTask(mptask);
    comp.setDistributor(AllocationResultDistributor.DEFAULT);
    // comp.setIsPropagating();

    // create the new MP task that represents all subtasks
    mptask.setComposition (comp);
    mptask.setParentTasks (parentTasks.elements());
    stuffToPublish.add (mptask);
    stuffToPublish.add (comp);

    // create aggregations for each parent task
    for (Iterator i = parentTasks.iterator(); i.hasNext();){
      Task parentTask = (Task)i.next();
      if (debug)
		UTILAllocate.setDebug (true);
      boolean isSuccess = !UTILAllocate.exceedsPreferences (parentTask, aspectValues);

      if (!isSuccess) {
		creator.showDebugIfFailure ();
		System.out.println ("UTILAggregate.makeAggregation - making failed aggregation for " + parentTask);
		UTILExpand.showPlanElement (parentTask);
      }
	  
      if (debug)
		UTILAllocate.setDebug (false);
      AllocationResult estAR = ldmf.newAVAllocationResult(confidence,
														  isSuccess,
														  aspectValues);
      Aggregation agg = ldmf.createAggregation(parentTask.getPlan(),
											   parentTask,
											   comp,
											   estAR);
      if (debug)
		System.out.println ("UTILAggregate.makeAggregation - Making aggregation for task " + parentTask.getUID () + 
							" agg " + agg.getUID());
	
      stuffToPublish.add (agg);
      comp.addAggregation(agg);
    }

    return stuffToPublish;
  }

  /**
   * Creates an Aggregation for every parent task.
   *                                                                               <p>
   * Note that the direct object of the MPTask will usually contain an asset group
   * whose members are the direct objects of the parents.                          <br>
   * If one wishes to rescind a parent task, both the list of parents on the MPTask<br>
   * and the direct object should be repaired.
   *                                                                               <p>
   * By default does not propagate rescinds past an aggregation.
   *                                                                               <p>
   * @param creator the plugin that created the aggregations, told when there is 
   *  a failure
   * @param ldmf the RootFactory
   * @param realityPlan the plan the plan elements are part of
   * @param parentTasks an enum of parent tasks
   * @param whatVerb the verb to give the MPTask
   * @param prepPhrases preps to attach to the MPTask
   * @param directObjects the direct objects of the parent tasks (usually) that will
   *  be put into an asset group, which will be the direct object of the MPTask
   * @param preferences the preferences for the MPTask
   * @param sourceClusterID which cluster is creating these things
   * @param taskToAspectValues maps task to an array of AspectValues
   * @param confidence what confidence to set the on the allocation results of the aggregations
   * @return List of items to publish, which will include the MPTask 
   *   representing composition, all the compositions, and the aggregation plan elements
   */

  public static List makeAggregation(UTILPlugin creator,
				     RootFactory ldmf,
				     Plan realityPlan,
				     Vector parentTasks, 
				     Verb whatVerb,
				     Vector prepPhrases,
				     Vector directObjects,
				     Vector preferences,
				     ClusterIdentifier sourceClusterID,
				     Map taskToAspectValues,
				     double confidence) {
    List stuffToPublish = new ArrayList ();

    NewMPTask mptask = UTILAggregate.makeMPSubTask(ldmf,
						   realityPlan,
						   parentTasks.elements(),
						   whatVerb,
						   prepPhrases.elements(),
						   UTILAsset.makeAssetGroup(ldmf, directObjects),
						   preferences.elements(),
						   Priority.UNDEFINED,
						   sourceClusterID);
    // create the new composition
    NewComposition comp = ldmf.newComposition ();
    comp.setCombinedTask(mptask);
    comp.setDistributor(AllocationResultDistributor.DEFAULT);
    // comp.setIsPropagating();

    // create the new MP task that represents all subtasks
    mptask.setComposition (comp);
    mptask.setParentTasks (parentTasks.elements());
    stuffToPublish.add (mptask);
    stuffToPublish.add (comp);

    AllocationResult lastAR = null;
    AspectValue []   lastAV = null;
    // create aggregations for each parent task
    for (Iterator i = parentTasks.iterator(); i.hasNext();){
      Task parentTask = (Task)i.next();
      // get the aspect values specific to this task
      AspectValue [] aspectValues = (AspectValue []) taskToAspectValues.get (parentTask);
	
      if (debug)
	UTILAllocate.setDebug (true); // will show comparison of prefs to aspect value
      boolean isSuccess = !UTILAllocate.exceedsPreferences (parentTask, aspectValues);

      if (!isSuccess) {
	creator.showDebugIfFailure ();
	System.out.println ("UTILAggregate.makeAggregation - making failed aggregation for " + parentTask);
	UTILExpand.showPlanElement (parentTask);
      }
	  
      if (debug)
	UTILAllocate.setDebug (false);

      AllocationResult estAR;

      if (aspectValues == lastAV)
	estAR = lastAR; // avoid creating a new allocation result if we have the same aspect value array
      else
	estAR = ldmf.newAVAllocationResult(confidence,
					   isSuccess,
					   aspectValues);

      Aggregation agg = ldmf.createAggregation(parentTask.getPlan(),
					       parentTask,
					       comp,
					       estAR);
      if (debug)
	System.out.println ("UTILAggregate.makeAggregation - Making aggregation for task " + parentTask.getUID () + 
			    " agg " + agg.getUID());
	
      stuffToPublish.add (agg);
      comp.addAggregation(agg);

      lastAV = aspectValues;
      lastAR = estAR;
    }

    return stuffToPublish;
  }

  /**
   * Creates a failed Aggregation.
   *
   * Sets the estimated allocation result of the plan element to an 
   * empty failed alloc result.
   *
   * @param ldmf RootFactory
   * @param t task
   * @return Aggregation
   */
  public static Aggregation makeFailedAggregation(UTILPlugin creator,
						  RootFactory ldmf, Task t) {
    AllocationResult failedAR  = 
      ldmf.newAllocationResult(1.0, false, 
			       new int[1], new double[1]);

    Aggregation agg = ldmf.createAggregation(t.getPlan(),
					     t,
					     ldmf.newComposition (),
					     failedAR);
    if (creator != null)
      creator.showDebugIfFailure ();

    return agg;
  }

  /**
   * Create an MPTask with an enumeration of parent tasks.
   * @param ldmf the RootFactory
   * @param source the cluster originating the task
   * @param plan the log plan
   * @param parents an enum of parent tasks
   * @param verb the verb
   * @param prepphrases an enum of PrepositionalPhrases
   * @param obj the direct object
   * @param penalty the penalty function associated with the new task
   * @return NewMPTask
   */
  public static NewMPTask makeMPSubTask (RootFactory ldmf,
					 Plan plan,
					 Enumeration parentTasks,
					 Verb verb,
					 Enumeration prepphrases,
					 Asset obj,
					 Enumeration preferences,
					 byte priority,
					 ClusterIdentifier source) {
 
    NewMPTask mpt = ldmf.newMPTask();
    mpt.setPlan(plan);
    mpt.setParentTasks(parentTasks);
    mpt.setVerb(verb);
    if (prepphrases != null)
      mpt.setPrepositionalPhrases(prepphrases);
    mpt.setDirectObject(obj);
    mpt.setPreferences(preferences);
    mpt.setPriority(priority);
    mpt.setSource(source);
    return mpt;
  }
}










