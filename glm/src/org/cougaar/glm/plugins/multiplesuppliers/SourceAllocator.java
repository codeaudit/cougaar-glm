/*
 * <copyright>
 *  Copyright 1997-2003 TASC 
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
package org.cougaar.glm.plugins.multiplesuppliers;

import java.util.Enumeration;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.lib.util.UTILPrepPhrase;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

/**
 * The Base SourceAllocator class.
 */
public class SourceAllocator extends SimplePlugin {
  // The implemented approach for SOURCE logic requires all suppliers to have the SOURCE plugins.
  // An alternate approach sends SUPPLY tasks to clusters that don't have MultipleSupplierCapable role.
  // The downside of this approach is that all SUPPLY plugins must ignore SUPPLY tasks whose parent task
  // is a SOURCE task. In this alternate approach, SOURCE-capable clusters must have a role of MultipleSupplierCapable.

  private String clusterName = null;

  private IncrementalSubscription sourceSubtasks  = null;
  private IncrementalSubscription allocations     = null;

  static class SourceSubtasksP implements UnaryPredicate {
    public boolean execute(Object o) {
      if ( o instanceof Task ) {
        Task task = (Task)o;
        if ( task.getVerb().toString().equals( Grammar.SOURCE) && task.getWorkflow() != null) {
          return true;
        }
      }
      return false;
    }
  }

  static class AllocationsP implements UnaryPredicate {
    public boolean execute(Object o) {
      if ( o instanceof Allocation ) {
        return true;
     }
      return false;
    }
  }

  /** 
   * rely upon load-time introspection to set these services - 
   * don't worry about revokation.
   */
  public final void setLoggingService(LoggingService bs) {  
    logger = bs; 

    prepHelper = new UTILPrepPhrase (logger);
  }

  /**
   * Get the logging service, for subclass use.
   */
  protected LoggingService getLoggingService() {  return logger; }

  /**
   * This method is meant to be overridden by subclasses for those instances where
   * the subclass needs to do any initialization at the beginning of each execute() loop.
   * The defualt implementation of this method does nothing.
   */
  protected void transactInit() {
  }

  /**
   * This method is meant to be overridden by subclasses for those instances where
   * the sublass needs to do processing when a task change is detected.
   * The defualt implementation of this method does nothing.
   * @param task a Task that has marked as changed.
   */
  protected void handleChangedTask( Task task) {
  }

  /**
   * This method is meant to be overridden by subclasses for those instances where
   * the sublass needs to do processing when a task removal is detected.
   * The defualt implementation of this method does nothing.
   * @param task a Task that has marked as removed.
   */
  protected void handleRemovedTask( Task task) {
  }

  /**
   * This method is meant to be overridden by subclasses for those instances where
   * the sublass needs to do processing when an allocation removal is detected.
   * The defuat implementation of this method does nothing.
   * @param allocation an Allocation that has marked as removed.
   */
  protected void handleRemovedAllocation( Allocation allocation) {
  }

  public String getClusterName() {
    return clusterName;
  }

  /**
   * Standard SimplePlugin method.
   * Store cluster name and setup subscriptions for SOURCE subtasks and for Allocations.
   */
  protected void setupSubscriptions() {
    clusterName = getMessageAddress().getAddress();
    sourceSubtasks  = (IncrementalSubscription)subscribe( new SourceSubtasksP());
    allocations     = (IncrementalSubscription)subscribe( new AllocationsP());
  }

  public void execute() {
    // Call transactionInit() first.
    // This way subclasses can do initialization at the beginning of each execute() loop.
    transactInit();

    if ( allocations != null && allocations.hasChanged()) {
      Enumeration removedAllocationsEnum = allocations.getRemovedList();
      while ( removedAllocationsEnum.hasMoreElements()) {
        Allocation allocation = (Allocation)removedAllocationsEnum.nextElement();
        handleRemovedAllocation( allocation);
      }

      Enumeration changedAllocationsEnum = allocations.getChangedList();
      while ( changedAllocationsEnum.hasMoreElements()) {
        Allocation allocation = (Allocation)changedAllocationsEnum.nextElement();
        allocatorRollup( allocation);
      }
    }

    if ( sourceSubtasks != null && sourceSubtasks.hasChanged()) {
      Enumeration addedSubtasks = sourceSubtasks.getAddedList();
      while ( addedSubtasks.hasMoreElements()) {
        Task subtask = (Task)addedSubtasks.nextElement();
        allocateTask( subtask);
      }
      Enumeration changedSubtasks = sourceSubtasks.getChangedList();
      while ( changedSubtasks.hasMoreElements()) {
        Task subtask = (Task)changedSubtasks.nextElement();
        handleChangedTask( subtask);
      }
      Enumeration removedSubtasks = sourceSubtasks.getRemovedList();
      while ( removedSubtasks.hasMoreElements()) {
        Task subtask = (Task)removedSubtasks.nextElement();
        handleRemovedTask( subtask);
      }
    }
  }

  /**
   * This plugin's execute() method calls this method when allocations are marked as changed
   * in order to rollup AllocationResults.
   * @param pe the changed PlanElement
   */
  private void allocatorRollup( PlanElement pe) {
    if ( pe.getReportedResult() != null) {
      Task task = pe.getTask();
      String verb = task.getVerb().toString();
      if ( verb.equals( Grammar.SOURCE)) {
        AllocationResult reportedResult = pe.getReportedResult();
        AllocationResult estimatedResult = pe.getEstimatedResult();
        if ( estimatedResult == null || estimatedResult != reportedResult) {
          pe.setEstimatedResult( reportedResult);
          publishChange( pe);
        }
      }
    }
  }

  /**
   * This plugin's execute method calls this method when new SOURCE tasks are detected.
   */
  private boolean allocateTask( Task task) {
    boolean retval = true;

    String verbStr = task.getVerb().toString();
    if (verbStr.equals( Grammar.SOURCE)) {
      PrepositionalPhrase pPhrase = prepHelper.getPrepNamed( task, Grammar.USESUPPLIER);
      if ( pPhrase == null) {
        System.out.println( "SourceAllocator.allocateTask: SOURCE task missing USESUPPLIER phrase");
        return false;
      } else {
        Organization supplier = (Organization)pPhrase.getIndirectObject();
        if ( supplier != null) {
          publishAdd( Utility.createAllocation( getFactory(), task, supplier, 0.0, 0.0));
        } else {
          publishAdd( Utility.makeFailedDisposition( theLDMF, task));
        }
      }
    } else {
      retval = false;
    }
    return retval;
  }

  protected LoggingService logger;
  protected UTILPrepPhrase prepHelper;
}
