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

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.util.UTILPreference;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.Date;

/**
 * The class which handles the problem-specific scheduling functionality.
 * Whenever possible, this comes from an XML file, but if necessary
 * can derive from this class.
 *
 */

public class GSSchedulingSpecs implements GSParent {

  private GSAssetFilter assetFilter = null;
  private GSTaskFilter taskFilter = null;
  private GSCapabilityFilter capabilityFilter = null;
  private List capacityConstraints = null;
  private GSTaskDuration taskDuration = null;
  private GSTaskGrouping taskGrouping = null;
  private int taskGroupingMode = -1;
  private Vector assets;


  static public final int NOGROUPING = 0;
  static public final int ALIGNEDGROUPING = 1;
  static public final int UNALIGNEDGROUPING = 2;

  // NOTE: Temporary.  Currently the score returned by
  // scoreForAsset is used as a multiplier to the other
  // score function(s) (i.e. pickBestTime) to avoid scaling
  // issues.  It would probably be better to return a score.
  // This is the "best" (and default) multiplier.
  static public final double NO_PENALTY = 1.0d;

  public GSSchedulingSpecs () { 
    capacityConstraints = new ArrayList ();
  }

  public void setTaskGroupingMode (String tgm) {
    if (tgm.equals ("none"))
      taskGroupingMode = NOGROUPING;
    else if (tgm.equals ("aligned"))
      taskGroupingMode = ALIGNEDGROUPING;
    else if (tgm.equals ("unaligned"))
      taskGroupingMode = UNALIGNEDGROUPING;
  }

  /** currently expect at most one of each type of child */
  public void addChild (Object obj) {

    if (obj instanceof GSTaskFilter) {
      if (taskFilter != null)
        System.out.println ("More than one task filters");
      taskFilter = (GSTaskFilter) obj;
    }

    else if (obj instanceof GSAssetFilter) {
      if (assetFilter != null)
        System.out.println ("More than one asset filters");
      assetFilter = (GSAssetFilter) obj;
    }

    else if (obj instanceof GSCapabilityFilter) {
      if (capabilityFilter != null)
        System.out.println ("More than one capability filters");
      capabilityFilter = (GSCapabilityFilter) obj;
    }

    else if (obj instanceof GSCapacityConstraint)
      capacityConstraints.add (obj);

    else if (obj instanceof GSTaskDuration) {
      if (taskDuration != null)
        System.out.println ("More than one task durations");
      taskDuration = (GSTaskDuration) obj;
    }

    else if (obj instanceof GSTaskGrouping) {
      if (taskGrouping != null)
        System.out.println ("More than one task groupings");
      taskGrouping = (GSTaskGrouping) obj;
    }

    else
      System.out.println ("Child " + obj + " of unknown type for scheduler");
  }
        

  public Vector initialize (Vector allAssets) {
    assets = new Vector();
    for (int i = 0; i < allAssets.size(); i++) {
      Asset a = (Asset) allAssets.elementAt(i);
      if (okayToUse (a))
        assets.addElement (a);
    }
    return assets;
  }


  public boolean interestingTask (Task task) {
    return ((taskFilter == null) ||
            taskFilter.passesFilter (task, task.getSource().getAddress()));
  }


  private boolean okayToUse (Asset asset) {
    boolean passes = true;
    
    if (assetFilter != null)
      passes = assetFilter.passesFilter (asset);

    return ((assetFilter == null) || passes);
  }

  protected boolean wantsOrderedAssets() {
    return false;
  }
  protected List orderAssets(List list) {
    return list;
  }

  public int taskGroupingMode() {
    return taskGroupingMode;
  }


  public boolean isCapable (Asset asset, Task task) {
    if (capabilityFilter == null)
      return true;
    
    Vector tmp_vec = new Vector(2);
    tmp_vec.add(asset);
    tmp_vec.add(task);
    return capabilityFilter.eval (tmp_vec);
  }


  public boolean canBeGrouped (Task task1, Task task2) {
    if (taskGrouping == null)
      return true;
    
    Vector tmp_vec = new Vector(2);
    tmp_vec.add(task1);
    tmp_vec.add(task2);
    return taskGrouping.eval (tmp_vec);
  }


  public long taskDuration (Task task, Asset asset) {
    if (taskDuration == null)
      taskDuration = new GSTaskDuration();
    return ((Long) taskDuration.value (task, asset)).longValue();
  }


  public Date getTaskEarliestStart (Task task) {
    try {
      return UTILPreference.getReadyAt(task);
    } catch (RuntimeException e) {
      System.out.println ("Warning: No START_DATE preference for task " +
			  task.getUID() + ", so using now");
      return new Date();
    }
  }

  public long computePreTaskDuration(Asset a, long task_start, Task t) {
    return 0l;
  }

  public List getCapacityConstraints () {
    return capacityConstraints;
  }

  
  public boolean withinCapacities (Asset asset, double[] values) {
    for (int i = 0; i < values.length; i++) {
      GSCapacityConstraint constraint = (GSCapacityConstraint)capacityConstraints.get(i);
      if (!constraint.withinCapacity (asset, values[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * The group is used in subclasses to handle cases where adding tasks
   * depends on previous tasks (see plugins/UTILGSSGlobalSeaSpecs as an 
   * example).  No, it's not the cleanest solution.
   */
  public double[] incrementCapacities (Task task, double[] values,
				       GSTaskGroup group) {
    double[] newvals = new double [values.length];
    for (int i = 0; i < values.length; i++) {
      GSCapacityConstraint constraint = (GSCapacityConstraint)capacityConstraints.get(i);
      newvals[i] = constraint.incrementCapacity (task, values[i]);
    }
    return newvals;
  }

  public double[] getRequiredCapacities (Task task) {
    double[] newvals = new double [capacityConstraints.size()];
    for (int i = 0; i < newvals.length; i++) {
      GSCapacityConstraint constraint = (GSCapacityConstraint)capacityConstraints.get(i);
      newvals[i] = constraint.incrementCapacity (task, 0.0);
    }
    return newvals;
  }

  /** 
   * return a score of the quality of this asset/group match
   * lower score = better
   * For now, we return a multiplier to be applied to the current score
   * to avoid scaling problems between the different score functions, but
   * this is probably not the best solution.
   */
  public double scoreForAsset (Asset asset, GSTaskGroup group) {
    return NO_PENALTY;
  }

  /** 
   * return a score of the quality of this asset/task match
   * lower score = better
   * For now, we return a multiplier to be applied to the current score
   * to avoid scaling problems between the different score functions, but
   * this is probably not the best solution.
   */
  public double scoreForAssetTaskPair(Asset asset, Task task){
      return NO_PENALTY;
  }

  /** 
   * return a score of the quality of this task/group match
   * lower score = better
   * For now, we return a multiplier to be applied to the current score
   * to avoid scaling problems between the different score functions, but
   * this is probably not the best solution.
   *
   * WARNING: since we want to compute the score of the task vs. the group
   * WITHOUT the task, the group param will be NULL if this is the first task
   * in the group
   */
  public double scoreForTask (Task task, GSTaskGroup group) {
    return NO_PENALTY;
  }
}
