// Copyright (10/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.domain.glm.packer;

import java.util.*;

import org.cougaar.domain.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.util.Sortings;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Ammunition;

/**
 * Packer - handles packing supply requests
 * 
 */
public abstract class Packer extends GenericPlugin {

  /**
   * Packer - constructor 
   */
  public Packer() {
    super();
  }


  /**
   * getSortFunction - returns comparator to be used in sorting the tasks to be 
   * packed. Default implementation sorts on end time.
   *
   * @return Comparator
   */
  public Comparator getSortFunction() {
    return new SortByEndTime();
  }

  /**
   * getAllocationResultDistributor - returns the AllocationResultDistributor be 
   * used in distributing allocation result for the transport task among the initial 
   * supply tasks. Defaults to 
   * ProportionalDistributor.DEFAULT_PROPORTIONAL_DISTRIBUTOR;
   *
   * @return AllocationResultDistributor
   */
  public AllocationResultDistributor getAllocationResultDistributor() {
    return ProportionalDistributor.DEFAULT_PROPORTIONAL_DISTRIBUTOR;
  }

  /**
   * getPreferenceAggregator - returns  PreferenceAggregator for setting the 
   * start/end times on the transport tasks. Defaults to DefaultPreferenceAggregator.
   *
   * @return PreferenceAggregator
   */
  public PreferenceAggregator getPreferenceAggregator() {
    return new DefaultPreferenceAggregator();
  }

  /**
   * getAggregationClosure - return AggregationClosure to be used for creating
   * transport tasks
   */
  public abstract AggregationClosure getAggregationClosure(ArrayList tasks);


  /**
   * processNewTasks - handle new ammo supply tasks
   * Called within GenericPlugin.execute.
   *
   * @param newTasks Enumeration of the new tasks
   */
  public void processNewTasks(Enumeration newTasks) {
    ArrayList tasks = new ArrayList();

    while (newTasks.hasMoreElements()) {
      tasks.add(newTasks.nextElement());
    }

    if (tasks.size() == 0) {
      return;
    }

   
    doPacking(getAggregationClosure(tasks), tasks,
              getSortFunction(), getPreferenceAggregator(),
              getAllocationResultDistributor());
  }

  /**
   * processChangedTasks - handle changed supply tasks
   * Called within GenericPlugin.execute.
   * **** Tasks are currently ignored ****
   *
   * @param changedTasks Enumeration of changed ammo supply tasks. Ignored.
   */
  public void processChangedTasks(Enumeration changedTasks) {
    if (DEBUG) {
      System.out.println("Packer.processChangedTasks - ignoring " +
                         "changed tasks");
    }
  }

  /**
   * processRemovedTasks - handle removed supply tasks
   * Called within GenericPlugin.execute.
   * **** Tasks are currently ignored ****
   *
   * @param changedTasks Enumeration of removed ammo supply tasks. Ignored.
   */
  public void processRemovedTasks(Enumeration changedTasks) {
    if (DEBUG) {
      System.out.println("Packer.processChangedTasks - ignoring " +
                         "changed tasks");
    }
  }
    
  /** 
    * doPacking - packs specified set of supply tasks.
    * Assumes that it's called within an open/close transaction.
    *
    * @param ac AggregationClosure creates the transport tasks
    * @param tasks ArrayList with the tasks which should be packed
    * @param sortfun BinaryPredicate to be used in sorting the tasks
    * @param prefagg PreferenceAggregator for setting the start/end times on the
    * transport tasks.
    * @param ard AllocationResultDistributor to be used in distributing allocation results
    * for the transport task amount the initial supply tasks.    *
    */
  protected boolean doPacking(AggregationClosure ac,
                              ArrayList tasks,
                              Comparator sortfun,
                              PreferenceAggregator prefagg,
                              AllocationResultDistributor ard) {
    
    // sort them, if appropriate
    if (sortfun != null) {
      tasks = (ArrayList)Sortings.sort(tasks, sortfun);
    }

    if (DEBUG) {
      System.out.println("Packer: about to build the sizer in doPacking.");
    }

    // now we set the double wheel going...
    Sizer sz = new Sizer(tasks, this);

    if (DEBUG) {
      System.out.println("Packer: about to build the filler in doPacking.");
    }

    Filler fil = new Filler(sz, this, ac, ard, prefagg);

    if (DEBUG) {
      System.out.println("Packer: about to run the wheelz in doPacking.");
    }

    boolean success;
    try {
      fil.execute();
      success = true;
    } catch (java.lang.Exception e) {
      System.err.println("Packer: Failure in attempt to pack.");
      System.err.println("Packer: Exception was:");
      System.err.println(e);
      e.printStackTrace();
      success = false;
    } finally {
    }
	     
    // indicate that the action succeeded.
    return success;
  }

  /**
   * SortByEndTime - sorts tasks by end date, earliest first
   */
  private class SortByEndTime implements Comparator {

    /* 
     * compare - compares end date of the 2 tasks.
     * Compares its two arguments for order. Returns a negative integer, zero, or a 
     * positive integer as the first argument is less than, equal
     * to, or greater than the second.
     */
    public int compare(Object first, Object second) {
      Task firstTask = null;
      Task secondTask = null;

      if (first instanceof Task) {
        firstTask = (Task) first;
      } 
      
      if (second instanceof Task) {
        secondTask = (Task) second;
      }

      if ((firstTask == null) &&
          (secondTask == null)) {
        return 0;
      } else if (firstTask == null) {
        return -1;
      } else if (secondTask == null) {
        return 1;
      } else {
        return (firstTask.getPreferredValue(AspectType.END_TIME) >
                secondTask.getPreferredValue(AspectType.END_TIME)) ? 1 : -1;
      }
    }

    /**
     * Indicates whether some other object is "equal to" this Comparator. 
     * This method must obey the general contract of Object.equals(Object). 
     * Additionally, this method can return true only if the specified Object is 
     * also a comparator and it imposes the same ordering as this comparator. Thus,
     * comp1.equals(comp2) implies that sgn(comp1.compare(o1,
     * o2))==sgn(comp2.compare(o1, o2)) for every object reference o1 and o2.
     */
    public boolean equals(Object o) {
      return (o.getClass() == SortByEndTime.class);
    }
  }

      
}









