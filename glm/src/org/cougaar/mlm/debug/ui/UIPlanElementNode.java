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


package org.cougaar.mlm.debug.ui;

import java.util.Enumeration;
import java.util.Iterator;

import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AssetTransfer;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.core.mts.MessageAddress;

/** A tree node for a PlanElement.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the children (tasks and dispositions)
  of plan elements.
  */

public class UIPlanElementNode extends UITreeNode {
  PlanElement planElement;

  /** Creates a tree node for a plan element by calling
    the UITreeNode constructor.
    @param planElement plan element for which to create a tree node
    */

  public UIPlanElementNode(PlanElement planElement) {
    super(planElement);
    this.planElement = planElement;
  }

  /** PlanElement is never a leaf.
    @return false
   */

  public boolean isLeaf() {
    return false;
  }


  /** Representation of a PlanElement in the tree.
    @return PlanElement, type of PlanElement, Task Verb
   */

  public String toString() {
    //    return planElement.toString();
    String disp = "disposition";
    if (planElement instanceof Allocation)
      disp = "Allocation";
    else if (planElement instanceof Aggregation)
      disp = "Aggregation";
    else if (planElement instanceof AssetTransfer)
      disp = "Asset Transfer";
    else if (planElement instanceof Expansion)
      disp = "Expansion";
    Task task = planElement.getTask();
    Verb verb = task.getVerb();
    String verbString = "";
    if (verb != null)
      verbString = verb.toString();
    return "Plan Element" + " " + disp + " " + verbString;
  }

  private int loadAssets(AggregateAsset a, int position, String prefix) {
    int count = 0;
    if (a.getAsset() == null)
      return 0; // no nodes added
    int quantity = 1;
    //if ( aggregateProp instanceof SimpleAggregateAssetProperty ) {
    //SimpleAggregateAssetProperty saap = (SimpleAggregateAssetProperty)aggregateProp;
    quantity = (int)a.getQuantity();
    
    insert(new UIStringNode("Number of assets: " + String.valueOf(quantity)),
           position++);
    //int quantityhack = 9999;
    //insert(new UIStringNode("Number of assets: " + String.valueOf(quantityhack)), position++);
    insert(new UIAssetNode(a.getAsset(), prefix), position++);
    count++;
    
    return count+1;// number of assets inserted +1 for count field
  }

  /** The children of all dispositions are:
        current schedule, penalty value, and direct objects (assets)
    A disposition can be an allocation, expansion or asset transfer.
    The children of an allocation are:
        asset, estinated schedule, estimated penalty value
    The children of an expansion are:
        workflow
    The children of an asset transfer are:
        asset, assignor cluster, assignee cluster, schedule
    Any asset can be an aggregate asset, in which case this
    steps through the enumeration of assets and creates a node for each.
    */

  public synchronized void loadChildren() {
    Task task = planElement.getTask();
    ScheduleElement schedule = null;
    //PenaltyValue penaltyValue;
  	AllocationResult allocationresult = null;
    Asset asset;

    int i = 0;

    insert(new UITaskNode(task), i++);

    //Schedule schedule;

    /*
    Enumeration e = planElement.getDirectObjects();
    if (e != null) {
      while (e.hasMoreElements()) {
	asset = (Asset)e.nextElement();
	if (asset != null)
	  if (asset instanceof AggregateAsset)
	    i = i + loadAssets((AggregateAsset)asset, i, "Direct Object: ");
	  else
	    insert(new UIAssetNode(asset, "Direct Object: "), i++);
	else
	  insert(new UIStringNode("null"), i++);
      }
    }
    */
    if (planElement instanceof Allocation) {
      Allocation allocation = (Allocation)planElement;
      asset = (Asset)allocation.getAsset();
      if (asset != null)
	if (asset instanceof AggregateAsset)
	  i = i + loadAssets((AggregateAsset)asset, i, "Allocated Asset: ");
	else
	  insert(new UIAssetNode(asset, "Allocated Asset: "), i++);
      //schedule = allocation.getEstimatedSchedule();
      if (schedule != null)
	insert(new UIScheduleElementNode(schedule, "Estimated Schedule: "), i++);
      allocationresult = planElement.getEstimatedResult();
      if (allocationresult != null)
	insert(new UIPenaltyValueNode(allocationresult, "Estimated Result: "), i++);
      //schedule = allocation.getReportedSchedule();
      if (schedule != null)
	insert(new UIScheduleElementNode(schedule, "Reported Schedule: "), i++);
      allocationresult = planElement.getReportedResult();
      if (allocationresult != null)
	insert(new UIPenaltyValueNode(allocationresult, "Reported Result: "), i++);
    } else if (planElement instanceof Expansion) {
      // uncomment to display estimated schedule and penalty
      //schedule = ((Expansion)planElement).getEstimatedSchedule();
      //if (schedule != null)
      //insert(new UIScheduleElementNode(schedule, "Estimated Schedule: "), i++);
      
      // jlv test - uncomment to show EPV
      allocationresult = planElement.getEstimatedResult();
      if (allocationresult != null)
	insert(new UIPenaltyValueNode(allocationresult, "Estimated Result: "), i++);
      
      Workflow workflow = ((Expansion)planElement).getWorkflow();
      insert(new UIWorkflowNode(workflow), i++);
    } else if (planElement instanceof AssetTransfer) {
      asset = (Asset)((AssetTransfer)planElement).getAsset();
      if (asset != null)
	if (asset instanceof AggregateAsset)
	  i = i + loadAssets((AggregateAsset)asset, i, "");
	else
	  insert(new UIAssetNode(asset, ""), i++);
      MessageAddress assignor = ((AssetTransfer)planElement).getAssignor();
      String assignorString = "Assigned By: ";
      if (assignor != null)
	assignorString = assignorString + assignor.getAddress();
      insert(new UIStringNode(assignorString), i++);
      Asset assignee = ((AssetTransfer)planElement).getAssignee();
      String assigneeString = "Assigned To: ";
      if (assignee != null)
	if (assignee instanceof AggregateAsset)
	  i = i + loadAssets((AggregateAsset)assignee, i, "");
	else
	  insert(new UIAssetNode(assignee, ""), i++);
      Schedule availSchedule = ((AssetTransfer)planElement).getSchedule();
      if ((availSchedule != null) &&
          (availSchedule.size() > 0)) {
        Iterator iterator = availSchedule.iterator();
        while (iterator.hasNext()) {
          schedule = (ScheduleElement)iterator.next();
          insert(new UIScheduleElementNode(schedule, ""), i++);
        }
      }
      allocationresult = ((AssetTransfer)planElement).getEstimatedResult();
      if (allocationresult != null)
	insert(new UIPenaltyValueNode(allocationresult, "Estimated Result: "), i++);
    }
  }


}
