/* 
 * <copyright>
 * Copyright 1999,2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.glm.plugins.projection;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.measure.CostRate;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.measure.MassTransferRate;
import org.cougaar.planning.ldm.measure.Rate;
import org.cougaar.planning.ldm.measure.TimeRate;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.glm.ldm.plan.PlanScheduleType;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;

import org.cougaar.glm.plugins.AssetUtils;
import org.cougaar.glm.plugins.BasicProcessor;
import org.cougaar.glm.plugins.DecorationPlugin;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;

/**
 * Projects demand for assets consumed by some consumer.
 */
public abstract class GenerateDemandExpander extends BasicProcessor {

  /** Plugin Parameter for creation of Supply instead of ProjectSupply tasks. 
      Values true/false, e.g. MyProjectionPlugin(+BulkWaterSupplyTasks) */
  public final static String create_supply_tasks_param = "SupplyTasks";

  /** Subscription for GenerateDemand tasks. */
  protected IncrementalSubscription        generateDemandSubscription_;
  /** Subscription to changes in publications by this processor */
  protected UnaryPredicate                 ownTasksPredicate_;
  protected IncrementalSubscription        ownSubscription_;
  protected UnaryPredicate                 ownTasksPEPredicate_;
  protected IncrementalSubscription        ownPESubscription_;
  /** Subscription to policies */
  protected IncrementalSubscription        policySubscription_;
  protected Hashtable                    policyTable_;
  protected Vector                       resourceTypes_;
  /** Table used for Diff-Based replanning of ProjectSupply tasks */
  protected Hashtable                    publishedProjectionTable_;

  public GenerateDemandExpander(DecorationPlugin pi, Organization org,
				Vector types, UnaryPredicate pred) {
    super(pi, org);
    resourceTypes_ = types;
    ownTasksPredicate_ = pred;
    ownTasksPEPredicate_ = new TasksPlanElementPredicate(ownTasksPredicate_);
    initializeSubscriptions();
    publishedProjectionTable_ = new Hashtable();
  }

  static class TasksPlanElementPredicate implements UnaryPredicate
  {
    UnaryPredicate taskPredicate_;

    public TasksPlanElementPredicate(UnaryPredicate task_predicate) {
      taskPredicate_ = task_predicate;
    }

    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
	return taskPredicate_.execute(((PlanElement)o).getTask());
      }
      return false;
    }
  }

  /** Create Demand Task 
   * @param parent_task
   * @param resource 
   * @param consumer
   * @param end end time
   * @param qty quantity of resource requested
   * @return 'demand' task for given resource
   */
  protected abstract Task newDemandTask(Task parent_task, Asset resource, 
					Object consumer, long end_time,
					double quantity);

  /** Create Projection Task 
   * @param parent_task
   * @param resource 
   * @param consumer
   * @param end end time
   * @param Rate of consumption over time period
   * @return 'demand' task for given resource
   */
  protected abstract Task newProjectionTask(Task parent_task, Asset resource, 
					    Object consumer, long start_time,
					    long end_time, Rate rate, double multiplier);

  static class GenerateDemandPredicate implements UnaryPredicate 
  {
    Vector resourceTypes_;

    public GenerateDemandPredicate(Vector types) {
      resourceTypes_ = types;
    }

    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task t = (Task)o;
	if (t.getVerb().equals(Constants.Verb.GENERATEPROJECTIONS)) {
	  PrepositionalPhrase pp =t.getPrepositionalPhrase(Constants.Preposition.OFTYPE) ;
	  if (pp != null) {
	    Object obj = pp.getIndirectObject();
	    if (obj instanceof String) {
	      String type = (String)obj;
	      Enumeration enum = resourceTypes_.elements();
	      while (enum.hasMoreElements()) {
		if (type.equals((String)enum.nextElement()))
		  return true;
	      }
	    }
	  }
	}
      }
      return false;
    }
  }

  static class PolicyPredicate implements UnaryPredicate
  {
    public boolean execute(Object o) {
      if (o instanceof DemandProjectionPolicy) {
	return true;
      }
      return false;
    }
  }   

  /**
   *  Subscribe for generate demand tasks.
   */
  protected void initializeSubscriptions()
  {    
    generateDemandSubscription_ = subscribe(new GenerateDemandPredicate(resourceTypes_));
    ownSubscription_ = subscribe(ownTasksPredicate_);
    ownPESubscription_ = subscribe(ownTasksPEPredicate_); 
    // DemandProjectionPolicies have two rules 
    //  - max number of resources - now obsolete in Ants because done by IcisMEILDMPlugin
    //  - aggregation period - number of days between output demand tasks
    //        going away with true projection tasks
    policySubscription_ = subscribe(new PolicyPredicate());
  }

  /** 
   **/
  public void update()
  {
    updateGenerateDemandExpansion();
    createPolicyTable(policySubscription_.elements());
    publishedProjectionTable_.clear();

    if (isSubscriptionChanged(policySubscription_)) {
      // reprocess everything
      processTasks(generateDemandSubscription_.elements());
    } else if (isSubscriptionChanged(generateDemandSubscription_)) {
      // process new or changed items
      processTasks(generateDemandSubscription_.getAddedList());
      processTasks(generateDemandSubscription_.getChangedList());
    }
  }

  /** Fill in the policyTable_ with current DemandProjectionPolicies */
  protected void createPolicyTable(Enumeration policies) {
    DemandProjectionPolicy pol;
    String type;

    policyTable_ = new Hashtable();
    while (policies.hasMoreElements()) {
      pol = (DemandProjectionPolicy)policies.nextElement();
      type = pol.getItemConsumed();
      policyTable_.put(type,pol);
    }
  }

  /** 
   * Expands the GenerateDemand tasks,  
   * creates and publishes the expanded task
   * with the associated workflow. 
   * @param tasks tasks to be processed
   * @return boolean - is there an error condition
   */
  public boolean processTasks(Enumeration tasks) {
    Task task;
    boolean is_failed = false;
    while (tasks.hasMoreElements()) {
      task = (Task)tasks.nextElement();
      int num_demand_tasks = createResourceConsumptionTasks(task);
      if (num_demand_tasks == 0) {
	printDebug( "processTask: demands, but no demand tasks "+
		    TaskUtils.taskDesc(task));
	is_failed = true;
	continue;
      }
      //  	    printDebug("processTask: expand "+TaskUtils.taskDesc(task)+
      //  		       " into "+num_demand_tasks+" tasks.");
    }
    return is_failed;
  }
    
  /** 
   * Creates demand tasks for each consumed resource.
   * @param parent_task
   * @return Vector of demand tasks 
   * Converts the task's ConsumerSpec into resourceConsumptionRateSchedules 
   * which is then used to create a series of demand tasks. **/
  protected int createResourceConsumptionTasks(Task parent_task) {
    Asset resource;
    Schedule rate_schedule;
    Vector resource_demand = new Vector();
    ConsumerSpec demand_spec = getDemandSpec(parent_task);
    int num_parts = 0;
    int total_tasks = 0;
    Enumeration consumed_resources = demand_spec.getConsumed();

    while (consumed_resources.hasMoreElements()) {
      resource = (Asset)consumed_resources.nextElement();
      rate_schedule = demand_spec.buildConsumptionRateSchedule(resource);
      if (!rate_schedule.isEmpty()) {
	Double mult = demand_spec.getMultiplier(resource);
	Vector theseTasks = createDemandTasks(parent_task, resource, rate_schedule,
					      mult.doubleValue());
	if (theseTasks.size() > 0) {
	  Task t = (Task) theseTasks.get(0);
	  if (t.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
	    publishChangeProjection(parent_task, resource, theseTasks.elements());
	    total_tasks += theseTasks.size();
	  } else {
	    resource_demand.addAll(theseTasks);
	  }
	}
      }
      num_parts++;
    }
    int num_tasks = resource_demand.size();
    if (num_tasks > 0) {
      publishExpansion(parent_task, resource_demand);
      total_tasks += num_tasks;
    }
    //  	printDebug(3, "createResourceConsumptionTasks for "+demand_spec.getConsumedType()+ 
    //  		   " for "+num_parts+" resources.");
    return total_tasks;
  }

  /** 
   * Create PROJECTSUPPLY tasks representing the demand.
   * @param task parent task (GenerateDemand)
   * @param Vector of ScheduledContentProperties
   *        (critical parts w/ rate schedule) demands
   * @return Vector of PROJECTSUPPLY tasks */
  public Vector createDemandTasks(Task parent_task, Asset resource, 
				  Schedule rate_schedule, double multiplier) {
    if (rate_schedule.isEmpty()) return new Vector(0);
    if (createSupplyTasksForType(parent_task)) {
      printDebug(3, "createDemandTasks(), creating supply tasks for  "+TaskUtils.getTaskItemName(parent_task));
      return createPeriodicDemandTasks(parent_task,resource, rate_schedule);
    }
    printDebug(3, "createDemandTasks(), creating projection tasks for  "+TaskUtils.getTaskItemName(parent_task));
    return createConstantParameterDemandTasks(parent_task,resource, rate_schedule, multiplier);
  }

  /** 
   * Create SUPPLY tasks representing the demand.
   * @param task parent task (GenerateDemand)
   * @param Vector of ScheduledContentProperties
   *        (critical parts w/ rate schedule) demands
   * @return Vector of SUPPLY tasks */
  protected Vector createPeriodicDemandTasks (Task parent_task, Asset resource, 
					      Schedule rate_schedule) {
    Vector supply_tasks = new Vector();

    Asset consumer = (Asset)parent_task.getDirectObject();
    int aggregation_period = getAggregationPeriod(resource.getTypeIdentificationPG().getTypeIdentification());
    long aggregation_time = aggregation_period*MSEC_PER_DAY;
	
    int num_tasks = 0;
    Task task;
    double qty;
    long sched_end = rate_schedule.getEndTime();
    long task_end = rate_schedule.getStartTime();
    while (task_end < sched_end) {
      qty = getTotalDemand(rate_schedule, task_end, task_end+aggregation_time);
      if (qty > 0) {
	task = newDemandTask(parent_task, resource, consumer, 
			     task_end, qty);
	if (task != null) {
	  supply_tasks.addElement(task);
	  num_tasks ++;
	}
      }
      task_end += aggregation_time;
    }
    printDebug(2,"Created "+num_tasks+" "+AssetUtils.assetDesc(resource)+
	       " demand tasks for consumer: "+AssetUtils.assetDesc(consumer));
    return supply_tasks;
  }


  /** 
   * Create PROJECTSUPPLY tasks representing the demand.
   * @param task parent task (GenerateDemand)
   * @param Vector of ScheduledContentProperties
   *        (critical parts w/ rate schedule) demands
   * @return Vector of POJECTSUPPLY tasks */
  protected Vector createConstantParameterDemandTasks(Task parent_task, 
						      Asset resource, 
						      Schedule rate_schedule,
						      double multiplier)
  {
    Vector projection_tasks = new Vector();

    Asset consumer = (Asset) parent_task.getDirectObject();
    int num_tasks = 0;
    // 	RateScheduleElement rse;
    ObjectScheduleElement rse;
    Task task;
    Rate rate;
    long task_start, task_end;
    long now = TimeUtils.pushToEndOfDay(calendar_, getAlpTime());
    Enumeration elements = rate_schedule.getAllScheduleElements();
    while (elements.hasMoreElements()) {
      // 	    rse = (RateScheduleElement)elements.nextElement();
      rse = (ObjectScheduleElement)elements.nextElement();
      task_start = rse.getStartTime();
      task_end = rse.getEndTime();
      if (task_end < now) {
	// Don't do anything with tasks in the past
	continue;
      }
      // 	    rate = rse.getRate();
      rate = (Rate)rse.getObject();
      // old
      // 	    rate = (rse.getQuantity() * ((task_end - qse.getStartTime())/MSEC_PER_DAY);
      if (rate != null) {
	task = newProjectionTask(parent_task, resource, consumer, 
				 task_start, task_end, rate, multiplier);
	if (task != null) {
	  projection_tasks.addElement(task);
	  num_tasks ++;
	}
      }
    }
    printDebug(2,"Created "+num_tasks+" "+AssetUtils.assetDesc(resource)+
	       " demand tasks for consumer: "+AssetUtils.assetDesc(consumer));
    return projection_tasks;
  }

  /**
   * @param sched Object schedule where Object is a rate
   * @param start start time
   * @param end end time
   * @return amount used over given time period (rate*time)
   **/
  protected double getTotalDemand(Schedule sched, long start, long end) {
    double total_demand = 0;
    Iterator iter = sched.getOverlappingScheduleElements(start, end).iterator();

    // 	RateScheduleElement rse;
    ObjectScheduleElement rse;
    Rate rate;
    double duration;
    long start_time, end_time;
    double total_rate = 0;
    while (iter.hasNext()) {
      // 	    rse = (RateScheduleElement)iter.next();
      rse = (ObjectScheduleElement)iter.next();
      start_time = rse.getStartTime();
      if (start_time < start) start_time = start;
      end_time = rse.getEndTime();
      if (end_time > end) end_time = end;
      // duration in days;
      duration = (end_time - start_time)/(double)MSEC_PER_DAY;
      // rate in days
      // 	    rate = rse.getRate();
      rate = (Rate)rse.getObject();
      if (rate instanceof CostRate) {
	total_demand = ((CostRate)rate).getDollarsPerDay()*duration;
      } else if (rate instanceof CountRate) {
	total_demand = ((CountRate)rate).getEachesPerDay()*duration;
      } else if (rate instanceof FlowRate) {
	total_demand = ((FlowRate)rate).getGallonsPerDay()*duration;
      } else if (rate instanceof MassTransferRate) {
	total_demand = ((MassTransferRate)rate).getShortTonsPerDay()*duration;
      } else if (rate instanceof TimeRate) {
	total_demand = ((TimeRate)rate).getHoursPerDay()*duration;
      } else {
	printError("getTotalDemand(), Unknown Rate type found : "+rate);
      }
      // 	    total_demand += rate.getPerDay()*duration;
    }
    return total_demand;

  }


  protected void updateGenerateDemandExpansion() {
    // check allocation results
    if (ownPESubscription_.elements().hasMoreElements()) {
      printLog("updateGenerateDemandExpansion() has elements() added = "+
	       ownPESubscription_.getAddedList().hasMoreElements()+" changed = "+
	       ownPESubscription_.getChangedList().hasMoreElements() );
    }
    updateExpansionResult(ownPESubscription_.elements());
  }

  /** Convenience method
   * @param task GenerateDemand task that has demandSpec
   * @return ConsumerSpec from PrepPhrase "DemandSpec"
   **/
  protected ConsumerSpec getDemandSpec(Task task) {
    PrepositionalPhrase pp = task.getPrepositionalPhrase("DemandSpec");
    if (pp == null) {
      printError("getDemandSpec badly formed task, no demand spec "+task);
      return null;
    }
    return (ConsumerSpec)pp.getIndirectObject();
  }

  /** 
   * Convenience function
   * @param type type id of the resource
   * @return number of days between orders for resources.  
   *          Defaults to one day.
   **/
  protected int getAggregationPeriod(String type) {
    DemandProjectionPolicy policy = (DemandProjectionPolicy)
      policyTable_.get(type);
    if (policy == null) {
      // default to once daily
      return 1;
    }
    return policy.getDaysBetweenDemand();
  }

  private boolean createSupplyTasksForType(Task parent_task) {
    boolean result = false;
    PrepositionalPhrase pp =parent_task.getPrepositionalPhrase(Constants.Preposition.OFTYPE);
    if (pp != null) {
      Object obj = pp.getIndirectObject();
      if (obj instanceof String) {
	Boolean bool = (Boolean)plugin_.getParam((String)obj+create_supply_tasks_param);
	result = (bool != null && bool.booleanValue());
	printDebug("createSupplyTasksForType(), Create Supply tasks for "+(String)obj+", "+bool);
      }
    }
    return result;
  }

  protected void publishChangeProjection(Task parent_task, Asset resource, Enumeration new_tasks) {

    // ownSubscription_ may contain both Supply and ProjectSupply tasks
    if (publishedProjectionTable_.isEmpty()) {
      cachePublishedProjections();
    }
    Enumeration tasks_to_publish = null;
    Vector published_tasks = null;
    published_tasks = (Vector)publishedProjectionTable_.get(resource.getTypeIdentificationPG().getTypeIdentification());

    if (!new_tasks.hasMoreElements() && (published_tasks == null)) {
      // No new tasks and no tasks from previous run.  nothing to do.
      return;
    }
    else if (new_tasks.hasMoreElements() && (published_tasks != null)) {
      Schedule published_schedule = newObjectSchedule(published_tasks.elements());
      Schedule newtask_schedule = newObjectSchedule(new_tasks);
      tasks_to_publish =  diffProjections(published_schedule, newtask_schedule);
    }
    else if (new_tasks.hasMoreElements()) {
      tasks_to_publish = new_tasks;
    } 
    else {
      // Saw demand for asset in previous run but no demand in new run, remove old tasks
      Enumeration e = published_tasks.elements();
      while (e.hasMoreElements()) {
	Task task = (Task)e.nextElement();
	plugin_.publishRemoveFromExpansion(task);
      }
    }
    if (tasks_to_publish != null) {
      Task task;
      while (tasks_to_publish.hasMoreElements()) {
	task = (Task)tasks_to_publish.nextElement();
	plugin_.publishAddToExpansion(parent_task, task);
	//  	        printDebug("publishChangeProjection(), Publishing new Projections: "+
	//  		            TaskUtils.projectionDesc(task));
      }
    }

  }

  private void cachePublishedProjections() {
    Enumeration e = ownSubscription_.elements();
    Task task;
    String assetID;
    Vector v;
    // create table of projection tasks hashed on DO Asset
    while (e.hasMoreElements()) {
      task = (Task)e.nextElement();
      if (task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
	assetID = task.getDirectObject().getTypeIdentificationPG().getTypeIdentification();
	v = (Vector)publishedProjectionTable_.get(assetID);
	if (v == null) {
	  v = new Vector();
	  publishedProjectionTable_.put(assetID, v);
	}
	v.add(task);
      }
    }
  }
	
}
