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

package org.cougaar.mlm.plugin.assessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.io.Serializable;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.util.UID;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.planning.service.LDMService;
import org.cougaar.planning.plugin.util.PluginHelper;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TaskScoreTable;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.glm.ldm.Constants;

import org.cougaar.glm.ldm.plan.AlpineAspectType;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.BulkPOL;
import org.cougaar.glm.plugins.MaintainedItem;

import org.cougaar.glm.plugins.TaskUtils;

public class ReadinessAssessorPlugin extends ComponentPlugin {

  protected long rollupSpan = 10;
  protected StringBuffer debugStart = new StringBuffer();
  private PhasedAggregated allocationAggregator = new PhasedAggregated();

  private IncrementalSubscription readinessTaskSub;
  private final UnaryPredicate readinessTaskPred = 
    new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof Task) {
	    Task t = (Task) o;
	    if (t.getVerb().equals(Constants.Verb.AssessReadiness)) {
	      if (!t.getPrepositionalPhrases().hasMoreElements()) {
		if (t.getPlanElement() == null) {
		  return true;
		}
	      }
	    }
	  }
	  return false;
	}
      };
  
  private IncrementalSubscription projectSupplyTaskSub;
  private final UnaryPredicate projectSupplyTaskPred = 
    new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof Task) {
	    Task t = (Task)o;
	    if (t.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
	      if (t.getPrepositionalPhrase(Constants.Preposition.REFILL) == null) {
		  return true;
	      }
	    }
	  }
	  return false;
	}
      };
      

  private IncrementalSubscription selfOrgSub;
  private final UnaryPredicate selfOrgPred =
    new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof Organization) {
	    return ((Organization)o).isSelf();
	  }
	  return false;
	}
      };


  private IncrementalSubscription readinessPESub;
  // this finds all expanded plan elements. we really only want the ones
  // whose task's parent is in the readinessTaskSub
  private final UnaryPredicate readinessPEPred = 
    new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof Allocation) {
	    Allocation pe = (Allocation) o;
	    Task t = pe.getTask();
	    if (t.getVerb().equals(Constants.Verb.AssessReadiness)) {
	      if (!t.getPrepositionalPhrases().hasMoreElements()) {
		return true;
	      }
	    }
	  }
	  return false;
	}
      };
  

  protected PlanningFactory rootFactory;

  // called by introspection
  public void setLDMService(LDMService service) {
    rootFactory = service.getFactory();
  }

  protected void setupSubscriptions() {
    projectSupplyTaskSub = (IncrementalSubscription) blackboard.subscribe(projectSupplyTaskPred);
    readinessTaskSub = (IncrementalSubscription) blackboard.subscribe(readinessTaskPred);
    selfOrgSub = (IncrementalSubscription) blackboard.subscribe(selfOrgPred);
    readinessPESub = (IncrementalSubscription) blackboard.subscribe(readinessPEPred);

    debugStart.append(getAgentIdentifier());
    debugStart.append(" ReadinessAssessor");
  }


  protected void execute() {


    if (readinessTaskSub.hasChanged()) {
      // only do one per cycle
      Collection added  = readinessTaskSub.getAddedCollection();
      if (added.size() > 0) {
	Task readinessTask = (Task)added.iterator().next();
	if (readinessTask != null) {
	  processReadinessTask(readinessTask);
	}
      }
    }

    if (readinessPESub.hasChanged()) {

      for (Iterator peIt = readinessPESub.getChangedCollection().iterator(); peIt.hasNext();) {
	PlanElement pe = (PlanElement) peIt.next();
	if (PluginHelper.updatePlanElement(pe)) {
	  blackboard.publishChange(pe);
	}
      }
    }
  }

  private void processReadinessTask(Task readinessTask) {

    // hack!
    long earliest = Long.MAX_VALUE;
    long latest = Long.MIN_VALUE;
  
    earliest = Math.round(readinessTask.getPreferredValue(AspectType.START_TIME));
    latest = Math.round(readinessTask.getPreferredValue(AspectType.END_TIME));
    rollupSpan = Math.round(readinessTask.getPreferredValue(AspectType.INTERVAL));
//      System.out.println(debugStart +" got earliest date: " 
//  		       +new Date(earliest).toString() +
//  		       " and rollupSpan: " + rollupSpan);

    expandAndAllocateToSubordinates(readinessTask);
    HashMap pacingItems = new HashMap(13);

    // Sort project supply tasks by Maintained Item and OfType
    System.out.println(debugStart +" sorting " + projectSupplyTaskSub.size() + " ProjectSupply tasks");
    for (Iterator psIterator = projectSupplyTaskSub.iterator(); psIterator.hasNext();) {
      Task psTask = (Task) psIterator.next();

      // find the supply type of the task
      Object directObject = psTask.getDirectObject();

      // find the latest end times
//        long end = Math.round(psTask.getPreferredValue(AspectType.END_TIME));
//        if (end > latest) 
//  	latest = end;

      // Do we really want to bail here, or should we count this task against our readiness?
      if (psTask.getPlanElement() == null) 
	continue;

      // find the asset (pacing item) this task is for
      MaintainedItem pacing = ((MaintainedItem)psTask.getPrepositionalPhrase(Constants.Preposition.MAINTAINING).getIndirectObject());

      // find the buckets for that asset
      HashMap itemBuckets = (HashMap) pacingItems.get(pacing);
      if (itemBuckets == null) {
	//System.out.println(debugStart + ": adding new set of buckets for " + pacing.getTypeIdentification());
	itemBuckets = new HashMap(13);
	pacingItems.put(pacing, itemBuckets);
      }


      // find the bucket for that supply type
      ArrayList results = (ArrayList) itemBuckets.get(directObject);

      if (results == null) {
	//System.out.println(debugStart + ": adding new bucket for " + directObject);
	results = new ArrayList(13);
	itemBuckets.put(directObject, results);
      }

      ArrayList al = splitResult(psTask.getPlanElement().getReportedResult(),
				 psTask.getPreferredValue(AlpineAspectType.DEMANDRATE));
      //System.out.println(debugStart + ": adding result to bucket " + al );
      results.addAll(al);
    }	

    if (pacingItems.size() > 0) {
      ArrayList results = calcResults(readinessTask, pacingItems, earliest, latest);
      System.out.println("\n\n" + debugStart + " - cluster readiness");
      printResults(results);
      // set the allocation result of the toplevel task
    } 
  }

  /**
   * @return an collection of AspectValue[] - part of a phased AllocationResult
   */
  protected ArrayList calcResults(Task parentTask, HashMap tree, long start, long end) {
    ArrayList in = new ArrayList(13);

    // Collection of AspectValue[] for entire Agent
    ArrayList overallResults = new ArrayList(13);
    // Collection of AspectValue[] for a pacing item
    ArrayList pacingResults = new ArrayList(13);
    // Collection of AspectValue[] for a pacing item and supply type
    ArrayList pacingAndSupplyResults = new ArrayList(13);

    int mergeCount = 0;

    for (Iterator itemIt = tree.keySet().iterator(); itemIt.hasNext();) {
      MaintainedItem pacingItem = (MaintainedItem)itemIt.next();
      // make new subtask of toplevel task
      Task pacingTask = createSubTask(parentTask, pacingItem, null);
      publishAddToExpansion(parentTask, pacingTask);

      HashMap itemBuckets = (HashMap)tree.get(pacingItem);
      for (Iterator bucketsIt = itemBuckets.keySet().iterator(); bucketsIt.hasNext();) {
	Asset suppliedItem = (Asset) bucketsIt.next();
	ArrayList bucket = (ArrayList) itemBuckets.get(suppliedItem);

	long lastEnd = start;
	while (lastEnd < end) {
	  long day1 = lastEnd;
	  long dayn = lastEnd + (MILLISPERDAY * rollupSpan);
	  lastEnd = dayn;
	  in.clear();
  	  for ( Iterator rseIt = bucket.iterator(); rseIt.hasNext(); ) {
  	    RateScheduleElement rse = (RateScheduleElement) rseIt.next();
  	    if (inRange(day1, dayn, rse)) {
  	      in.add(rse);
  	      rseIt.remove(); // counted it once. won't need it again
  	    } 
  	  }
	  AspectValue[] avs = newReadinessAspectArray(day1, dayn, average(in));
  	  if (Double.isNaN(avs[2].getValue())) {
	    System.err.println(debugStart + "invalid aspect value " + avs);
	  }
	  pacingAndSupplyResults.add(avs);
	}
	// At this point we have the phased allocation result collection for the lowest level task (pacing item, supply type)
	// make new subtask of pacing item task and fill in its allocation result
	Task pacingAndSupplyTask = createSubTask(pacingTask, pacingItem, suppliedItem);
	if (pacingResults.isEmpty()) {
	  pacingResults.addAll(pacingAndSupplyResults);
	  mergeCount = 1;
	} else {
	  mergeAdd(pacingResults, pacingAndSupplyResults);
	  mergeCount++;
	}
	publishAddToExpansion(pacingTask, pacingAndSupplyTask);
	publishAllocationResult(pacingAndSupplyTask, pacingAndSupplyResults);
	//System.out.println();
	//System.out.println(pacingItem + " " + suppliedItem);
	//printResults(pacingAndSupplyResults);
	pacingAndSupplyResults.clear();
      }

      averageResults(pacingResults, mergeCount);
      // At this point we have enough info to fill in the allocation result collection for the the pacing item task
      // fill in allocation result of pacing item subtask

      if (overallResults.isEmpty()) {
	overallResults.addAll(pacingResults);
      } else {
	merge(overallResults, pacingResults);
      }

      publishAddToExpansion(parentTask, pacingTask);
      publishAllocationResult(pacingTask, pacingResults);

//        System.out.println();  System.out.println();
//        System.out.println(debugStart);
//        System.out.println(pacingItem);
//        printResults(pacingResults);
      pacingResults.clear();
    }
    publishAllocationResult(parentTask, overallResults );
    return overallResults;
  }


  /**
   * pick the minimum readiness value of each phase
   * @param oldList min of previous phased results
   * @param newList new phased result to merge in
   **/
  protected void merge(ArrayList oldList, ArrayList newList) {
    // sure hope these cover the same time span!

    if (oldList.size() != newList.size()) {
      System.err.println(debugStart + ".merge() - bad assumption, Bub. The results have different cardinalities!");
      return;
    }

    for (int i = 0; i < oldList.size(); i++) {
      AspectValue[] oldAV = (AspectValue[]) oldList.get(i);
      AspectValue[] newAV = (AspectValue[]) newList.get(i);

      // arrays should have the same three aspect types
      if (oldAV[0].getValue() != newAV[0].getValue()) {
	System.err.println(debugStart + ".merge() - bad assumption, Bub. The AspectValues have different start dates!");
	return;
      }
      if (oldAV[1].getValue() != newAV[1].getValue()) {
	System.err.println(debugStart + ".merge() - bad assumption, Bub. The AspectValues have different end dates!");
	return;
      }

      // You are the weekest link!
      if (oldAV[2].getValue() > newAV[2].getValue()) {
	oldAV[2] = newAV[2];
      }
    }
  }

  /**
   * add the readiness value of each phase
   * @param runningTotalList running total each phase of phased results
   * @param newList new phased result to add to total
   **/
  protected void mergeAdd(ArrayList runningTotalList, ArrayList newList) {
    // sure hope these cover the same time span!

    if (runningTotalList.size() != newList.size()) {
      System.err.println(debugStart + ".mergeAdd() - bad assumption, Bub. The results have different cardinalities!");
      return;
    }

    for (int i = 0; i < runningTotalList.size(); i++) {
      AspectValue[] oldAV = (AspectValue[]) runningTotalList.get(i);
      AspectValue[] newAV = (AspectValue[]) newList.get(i);

      // arrays should have the same three aspect types
      if (oldAV[0].getValue() != newAV[0].getValue()) {
	System.err.println(debugStart + ".mergeAdd() - bad assumption, Bub. The AspectValues have different start dates!");
	return;
      }
      if (oldAV[1].getValue() != newAV[1].getValue()) {
	System.err.println(debugStart + ".mergeAdd() - bad assumption, Bub. The AspectValues have different end dates!");
	return;
      }

      oldAV[2] = oldAV[2].dupAspectValue(oldAV[2].getValue() + newAV[2].getValue());
    }
  }

  /**
   * divide each qty by the number of items added together to produce the qty.
   **/
  protected void averageResults(ArrayList results, int denominator) {

    // not much point in doing this if denominator is 0
    if (denominator == 0)
      return;

    for (int i = 0; i < results.size(); i++) {
      AspectValue[] avs = (AspectValue[]) results.get(i);
      avs[2]=avs[2].dupAspectValue(avs[2].getValue()/denominator);
    }
  }

  protected static final long MILLISPERDAY = 1000*60*60*24;

  /**
   *  assign a rate value to each day covered by the allocation result ranges
   **/
  private ArrayList splitResult(AllocationResult ar, double preferedRate) {
    ArrayList schedule = new ArrayList(13);
    if (ar == null)
      return schedule;

//      if (!ar.isSuccess()) {
//        AspectValue[] rollup = ar.getAspectValueResults();
//        for (int i=0; i<rollup.length; i++) {
//  	if (rollup[i].getAspectType() == AlpineAspectType.DEMANDRATE) {
//  	  System.out.println(debugStart + " failed task with rollup demand rate of " + rollup[i].getValue());
//  	  break;
//  	}
//        }
//      }

    if (ar.isPhased()) {
      for (Iterator phasedIterator = ar.getPhasedAspectValueResults().iterator(); phasedIterator.hasNext();) {
	long start=-1, end=-1;
	double rate=-1;
	AspectValue[] avs = (AspectValue[])phasedIterator.next();
	//	System.out.println("ReadinessAssessor.splitResult ");
	for (int i=0; i<avs.length; i++) {
	
	  // find the start and end dates and the rate
	  switch (avs[i].getAspectType()) {
	  case AspectType.START_TIME :
	    start = avs[i].longValue();
	    break;
	  case AspectType.END_TIME :
	    end = avs[i].longValue();
	    break;
	  case AspectType.POD :
	    //ignore;
	    break;
	  case AlpineAspectType.DEMANDMULTIPLIER :
	    //ignore;
	    break;
	  case AlpineAspectType.DEMANDRATE : 
	    rate = avs[i].getValue();
	    if (!ar.isSuccess()) {
	      //System.out.println(debugStart + " failed task DemandRate is " + rate + " task Prefered rate is " + preferedRate);
	    }
	    break;
	  default:
	    System.err.println(debugStart + " Unexpected AspectType: " + avs[i].getAspectType());
	    break;
	    }
	  if (start==-1 || end==-1 || rate==-1)
	    continue;
	}

	// don't make one for the last day
	for (long day=start; day<end; day+=MILLISPERDAY) {
	  RateScheduleElement rse;
	  if (preferedRate != 0 ) {
	    rse = new RateScheduleElement(day, rate/preferedRate);
	  } else {
	    rse = new RateScheduleElement(day, 1.0);
	    System.err.println(debugStart + "preferedRate of task is zero. why?");
	  }
//  	  System.out.println("ReadinessAssessor.splitResult() adding " + rse);
	  schedule.add(rse);
	}
      }
    } else {
      System.err.println(debugStart + ".splitResult() allocation result is not phased");
    }
    return schedule;
  }

  protected boolean inRange(long startTime, long endTime, RateScheduleElement rse) {
    if ((rse.date < startTime) || (rse.date >= endTime)) 
      return false;
    return true;
  }

  /** find the average readiness of the RateScheduleElements in the collection **/
  protected double average (Collection rses) {
    // wipe out existing numbers
    int weight = 0;
    double runningTotal = 0;
    for (Iterator rseIt = rses.iterator(); rseIt.hasNext();) {
      RateScheduleElement rse = (RateScheduleElement) rseIt.next();
      runningTotal += rse.readiness;
      weight++;
    }
    if (weight == 0) {
      //System.out.println(debugStart + "average() - no elements in collection - nothing to average");
      return 1.0;
    }
    return runningTotal/weight;
  }

  protected AspectValue[] newReadinessAspectArray(long startTime, long endTime, double readiness) {
    AspectValue[] values = new AspectValue[3];
    values[0] = AspectValue.newAspectValue(AspectType.START_TIME, startTime);
    values[1] = AspectValue.newAspectValue(AspectType.END_TIME, endTime);
    // where do I put READINESS aspecttype?
    values[2] = AspectValue.newAspectValue(AspectType.READINESS, readiness);
    return values;
  }


  private void printResults(ArrayList results) {
    for (int i=0; i<results.size(); i++) {
      AspectValue[] avs = (AspectValue[]) results.get(i);
      StringBuffer buf = new StringBuffer();
      buf.append("Start Date = ");
      buf.append((new Date(avs[0].longValue())).toString());
      buf.append(", End Date = ");
      buf.append((new Date(avs[1].longValue())).toString());
      buf.append(", readiness=");
      buf.append(avs[2].getValue());
      System.out.println(buf.toString());
    }
  }

  public NewTask createSubTask(Task parent, MaintainedItem pacing, Asset supplyType){
    NewTask subtask = rootFactory.newTask();
    subtask.setParentTask(parent);
    subtask.setVerb(parent.getVerb());
    subtask.setPreferences(parent.getPreferences());
    subtask.setContext(parent.getContext());
    Vector preps = new Vector(2);
    NewPrepositionalPhrase prep = null;
    if (pacing !=null) {
      prep = rootFactory.newPrepositionalPhrase();
      prep.setPreposition(Constants.Preposition.FOR);
      prep.setIndirectObject(pacing);
      preps.add(prep);
    }
    if (supplyType != null) {
      prep = rootFactory.newPrepositionalPhrase();
      prep.setPreposition(Constants.Preposition.WITH);
      prep.setIndirectObject(supplyType);
      preps.add(prep);
    }
    subtask.setPrepositionalPhrases(preps.elements());
    blackboard.publishAdd(subtask);
    return subtask;
  }

  private AspectValue[] calcRollup(List phasedResults) {
    long startTime = Long.MAX_VALUE;
    long endTime = Long.MIN_VALUE;
    double runningTotal = 0;

    for (Iterator listIt = phasedResults.iterator(); listIt.hasNext();) {
      AspectValue[] avs = (AspectValue[]) listIt.next();
      if (avs[0].longValue() < startTime) {
	startTime = avs[0].longValue();
      }
      if (avs[1].longValue() > endTime) {
	endTime = avs[1].longValue();
      }
      runningTotal += avs[2].getValue();
    }

    // should I do average or min?
    double avgReadiness = runningTotal/phasedResults.size();
    AspectValue[] rollup = newReadinessAspectArray(startTime, endTime, avgReadiness);
    return rollup;
  }

  protected void publishAllocationResult(Task task, List phasedResults) {
    AspectValue[] rollup = calcRollup(phasedResults);
    AllocationResult ar = rootFactory.newAVPhasedAllocationResult(1, true, 
								  rollup,
								  phasedResults);
    PlanElement pe = task.getPlanElement();
    if (pe == null) {
      // must be an disposition, rather than a expansion or allocation
      pe = rootFactory.createDisposition(task.getPlan(), 
					 task,
					 ar);
      pe.setEstimatedResult(ar);
      blackboard.publishAdd(pe);
    } else {
        pe.setEstimatedResult(ar);
        blackboard.publishChange(pe);
    }
  }

  protected void publishAddToExpansion(Task parent, Task subtask) {
        // Publish new task
//      if (!blackboard.publishAdd(subtask)) {
//        System.err.println("ReadinessAssessorPlugin.publishAddToExpansion fail to publish task" +TaskUtils.taskDesc(subtask));
//      }
    PlanElement pe = parent.getPlanElement();
    Expansion expansion;
    NewWorkflow wf;
    // Parent task has not been yet expanded, create an expansion
    if (pe == null) {
      // Create workflow
      wf = (NewWorkflow)rootFactory.newWorkflow();
      wf.setParentTask(parent);
      wf.setIsPropagatingToSubtasks(true);
      wf.addTask(subtask);
      wf.setAllocationResultAggregator(allocationAggregator);
      ((NewTask) subtask).setWorkflow(wf);
      // Build Expansion
      expansion = rootFactory.createExpansion(parent.getPlan(), parent, wf, null);
      // Publish Expansion
      blackboard.publishAdd(expansion);
    }
    // Task already has expansion, add task to the workflow and publish the change
    else if (pe instanceof Expansion) {
      expansion =(Expansion)pe;
      wf = (NewWorkflow)expansion.getWorkflow();
      wf.addTask(subtask);
      ((NewTask) subtask).setWorkflow(wf);
      blackboard.publishChange(expansion);
    }
    else {
      System.err.println("ReadinessAssessorPlugin.publishAddToExpansion: problem pe not Expansion?" +pe);
    }
  }

  
  /**
   * @param org Organization
   * @return Collection of subordinates
   */
  private Collection findSubordinates(Organization org) {

    Collection subordinates = 
      org.getRelationshipPG().getRelationshipSchedule().getMatchingRelationships(Constants.Role.ADMINISTRATIVESUBORDINATE,
										 TimeSpan.MIN_VALUE,
										 TimeSpan.MAX_VALUE);
    return subordinates;
  }

  private void expandAndAllocateToSubordinates(Task parentTask) {
    Organization selfOrg = getSelfOrg();
    RelationshipSchedule rs = selfOrg.getRelationshipSchedule();

    Collection subordinates =  findSubordinates(selfOrg);

    for (Iterator subOrgIt = subordinates.iterator(); subOrgIt.hasNext();) {

      Relationship rel = (Relationship) subOrgIt.next();
      Organization subOrg = (Organization) rs.getOther(rel);

      // create new task
      Task subtask = createSubTask(parentTask, null, null);

      // add to expanstion
      publishAddToExpansion(parentTask, subtask);
      
      // allocate it to the subordinate
      PlanElement pe = rootFactory.createAllocation(subtask.getPlan(), 
						    subtask,
						    subOrg,
						    null,
						    Constants.Role.BOGUS);
      blackboard.publishAdd(pe);
      System.out.println(debugStart + " allocating task to subordinate organization " + subOrg);
    }
  }

  private Organization getSelfOrg() {
    // better be something here!
    return (Organization)selfOrgSub.iterator().next();
  }


  protected class RateScheduleElement {
    public long date;
    public double readiness;
    
    public RateScheduleElement(long date, double readiness) {
      this.date = date;
      this.readiness = readiness;
    }

    public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("RateScheduleElement[date=");
      buf.append((new Date(date)).toString());
      buf.append(", readiness=");
      buf.append(readiness);
      buf.append("]   ");
      return buf.toString();
    }

    public boolean equals(Object o) {
      if (!(o instanceof RateScheduleElement))
	return false;
      RateScheduleElement other = (RateScheduleElement) o;
      if ((date == other.date) && (readiness == other.readiness))
	return true;
      return false;
    }
  }

  private class PhasedAggregated implements AllocationResultAggregator {
    public AllocationResult calculate(Workflow wf, TaskScoreTable tst, AllocationResult currentar) {
      int count = 0;
      boolean suc = true;
      double rating = 0.0;
      ArrayList mergedPhased = new ArrayList(13);
      
      Enumeration tasks = wf.getTasks();
      if (tasks == null || (! tasks.hasMoreElements())) return null;
      
      while (tasks.hasMoreElements()) {
        Task t = (Task) tasks.nextElement();
        count++;
        AllocationResult ar = tst.getAllocationResult(t);
        if (ar == null) {
          return null; // bail if undefined
        }

	ArrayList currentPhased = null;
	// better be phased!
	if (ar.isPhased()) {

	  if (mergedPhased.isEmpty()) {
	    mergedPhased.addAll(ar.getPhasedAspectValueResults());
	  } else {
	    currentPhased = new ArrayList(ar.getPhasedAspectValueResults());
	    mergeAdd(mergedPhased, currentPhased);
	  }
	}

        suc = suc && ar.isSuccess();
        rating += ar.getConfidenceRating();
      } // end of looping through all subtasks
      averageResults(mergedPhased, tst.size());
      AspectValue[] acc = calcRollup(mergedPhased);

      rating /= count;

      boolean delta = false;
      
      // only check the defined aspects and make sure that the currentar is not null
      if (currentar == null) {
        delta = true;		// if the current ar == null then set delta true
      } else {
        int[] caraspects = currentar.getAspectTypes();
        if (caraspects.length != acc.length) {
          //if the current ar length is different than the length of the new
          // calculations (acc) there's been a change
          delta = true;
        } else {
          for (int i = 0; i < caraspects.length; i++) {
            int da = caraspects[i];
	    for (int j = 0; j < acc.length; j++) {
	      if (acc[j].getAspectType() == da) {
		if (acc[j].getValue() != currentar.getValue(da)) {
		  delta = true;
		  break;
		}
	      }
            }
          }
        }
      
        if (!delta) {
	  if (currentar.isSuccess() != suc) {
	    delta = true;
	  } else if (Math.abs(currentar.getConfidenceRating() - rating) > SIGNIFICANT_CONFIDENCE_RATING_DELTA) {
	    delta = true;
	  }
        }
      }

      if (delta) {
        AllocationResult artoreturn =  
	  rootFactory.newAVPhasedAllocationResult(1, true, 
						  acc,
						  mergedPhased);
        return artoreturn;
      } else {
        return currentar;
      }
    }
  }
}
