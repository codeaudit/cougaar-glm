/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.lib.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.lib.filter.UTILPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfOplanIds;
import org.cougaar.planning.ldm.plan.NewComposition;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Priority;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.log.Logger;

/**
 * This class contains utility functions for creating
 * Aggregations.
 */

public class UTILAggregate {
  private static String myName = "UTILAggregate";
  protected Logger logger;

  /**
   * Set the logger -- may be misleading though
   *
   * @param log logger to use
   */
  public UTILAggregate (Logger log) { 
    logger = log; 
    alloc = new UTILAllocate (log);
    expand = new UTILExpand (log);
    assetHelper = new UTILAsset (log);
  }

  /**
   * Creates an Aggregation for every parent task.
   *
   * @param ldmf the PlanningFactory
   * @param parentTasks parent tasks
   * @return MPTask representing composition
   */

  public List makeAggregation(UTILPlugin creator,
				     PlanningFactory ldmf,
				     Plan realityPlan,
				     Vector parentTasks, 
				     Verb whatVerb,
				     Vector prepPhrases,
				     Vector directObjects,
				     Vector preferences,
				     MessageAddress sourceClusterID,
				     AspectValue [] aspectValues,
				     double confidence) {
    List stuffToPublish = new ArrayList ();

    NewMPTask mptask = makeMPSubTask(ldmf,
						   realityPlan,
						   parentTasks.elements(),
						   whatVerb,
						   prepPhrases.elements(),
						   assetHelper.makeAssetGroup(ldmf, directObjects),
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
      //      if (logger.isDebugEnabled())
      //	alloc.setLogger (logger);
      boolean isSuccess = !alloc.exceedsPreferences (parentTask, aspectValues);

      if (!isSuccess) {
	logger.warn ("UTILAggregate.makeAggregation - making failed aggregation for " + parentTask);
	expand.showPreferences (parentTask, aspectValues);
      }
	  
      //      if (logger.isDebugEnabled())
      //	UTILAllocate.setLogger (logger);
      AllocationResult estAR = ldmf.newAVAllocationResult(confidence,
							  isSuccess,
							  aspectValues);
      Aggregation agg = ldmf.createAggregation(parentTask.getPlan(),
					       parentTask,
					       comp,
					       estAR);
      if (logger.isDebugEnabled())
	logger.debug ("UTILAggregate.makeAggregation - Making aggregation for task " + parentTask.getUID () + 
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
   * @param ldmf the PlanningFactory
   * @param realityPlan the plan the plan elements are part of
   * @param parentTasks parent tasks
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

  public List makeAggregation(UTILPlugin creator,
				     PlanningFactory ldmf,
				     Plan realityPlan,
				     Vector parentTasks, 
				     Verb whatVerb,
				     Vector prepPhrases,
				     Vector directObjects,
				     Vector preferences,
				     MessageAddress sourceClusterID,
				     Map taskToAspectValues,
				     double confidence) {
    List stuffToPublish = new ArrayList ();

    NewMPTask mptask = makeMPSubTask(ldmf,
						   realityPlan,
						   parentTasks.elements(),
						   whatVerb,
						   prepPhrases.elements(),
						   assetHelper.makeAssetGroup(ldmf, directObjects),
						   preferences.elements(),
						   Priority.UNDEFINED,
						   sourceClusterID);
    // create the new composition
    NewComposition comp = ldmf.newComposition ();
    comp.setCombinedTask(mptask);
    comp.setDistributor(AllocationResultDistributor.DEFAULT);
    // comp.setIsPropagating();
    

    HashSet set = new HashSet();
    Iterator taskIt = parentTasks.iterator();
    Task parent;
    while (taskIt.hasNext()) {
      parent = (Task)taskIt.next();
      if (parent.getContext() != null) {
        set.addAll((ContextOfOplanIds)parent.getContext());
      }
    }
    mptask.setContext(new ContextOfOplanIds(set));


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
	
      //      if (logger.isDebugEnabled())
      //	UTILAllocate.setLogger (logger); // will show comparison of prefs to aspect value
      boolean isSuccess = !alloc.exceedsPreferences (parentTask, aspectValues);

      if (!isSuccess) {
	logger.warn ("UTILAggregate.makeAggregation - making failed aggregation for " + parentTask);
	expand.showPlanElement (parentTask);
      }
	  
      //      if (logger.isDebugEnabled())
      //	UTILAllocate.setLogger (logger);

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
      if (logger.isDebugEnabled())
	logger.debug ("UTILAggregate.makeAggregation - Making aggregation for task " + parentTask.getUID () + 
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
   * @param ldmf PlanningFactory
   * @param t task
   * @return Aggregation
   */
  public Aggregation makeFailedAggregation(UTILPlugin creator,
						  PlanningFactory ldmf, Task t) {
    AllocationResult failedAR  = 
      ldmf.newAllocationResult(1.0, false, 
			       new int[1], new double[1]);

    Aggregation agg = ldmf.createAggregation(t.getPlan(),
					     t,
					     ldmf.newComposition (),
					     failedAR);
    return agg;
  }

  /**
   * Create an MPTask with an enumeration of parent tasks.
   * @param ldmf the PlanningFactory
   * @param source the cluster originating the task
   * @param plan the log plan
   * @param parentTasks parent tasks
   * @param verb the verb
   * @param prepphrases  PrepositionalPhrases
   * @param obj the direct object
   * @return NewMPTask
   */
  public NewMPTask makeMPSubTask (PlanningFactory ldmf,
					 Plan plan,
					 Enumeration parentTasks,
					 Verb verb,
					 Enumeration prepphrases,
					 Asset obj,
					 Enumeration preferences,
					 byte priority,
					 MessageAddress source) {
 
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

  protected UTILAllocate alloc;
  protected UTILExpand expand;
  protected UTILAsset assetHelper;
}
