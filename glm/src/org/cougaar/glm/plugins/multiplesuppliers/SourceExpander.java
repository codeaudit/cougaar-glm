/*
 * <copyright>
 *  Copyright 1997-2001 TASC 
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

import org.cougaar.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.service.LoggingService;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;
import org.cougaar.lib.util.*;

import org.cougaar.glm.*;
import org.cougaar.glm.ldm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.asset.*;

import java.util.*;
import java.text.*;

/**
 * The Base SourceExpander class.
 */
public class SourceExpander extends SimplePlugin {
  // The implemented approach for SOURCE logic requires all suppliers to have the SOURCE plugins.
  // An alternate approach sends SUPPLY tasks to clusters that don't have MultipleSupplierCapable role.
  // The downside of this approach is that all SUPPLY plugins must ignore SUPPLY tasks whose parent task
  // is a SOURCE task. In this alternate approach, SOURCE-capable clusters must have a role of MultipleSupplierCapable.

  private IncrementalSubscription inputTasks  = null;
  private IncrementalSubscription expansions  = null;
  private IncrementalSubscription orgAssets   = null;
  private String clusterName = null;

  static class InputTasksP implements UnaryPredicate {
    public boolean execute(Object o) {
      if ( o instanceof Task ) {
        Task task = (Task)o;
        if ( task.getWorkflow() == null) {
          return true;
        }
      }
      return false;
    }
  }

  static class ExpansionsP implements UnaryPredicate {
    public boolean execute( Object o) {
      if ( o instanceof Expansion) {
        return true;
      }
      return false;
    }
  }

  static class OrganizationAssetsP implements UnaryPredicate {
    public boolean execute( Object o) {
      if ( o instanceof Organization)
        return true;
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
    prefHelper = new UTILPreference (logger);
    expandHelper = new UTILExpand   (logger);
  }

  /**
   * Get the logging service, for subclass use.
   */
  protected LoggingService getLoggingService() {  return logger; }

  /**
   * This method is meant to be overridden by subclasses for those instances where
   * the subclass needs to do any initialization at the beginning of each execute() loop.
   * The default implementation does nothing.
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
   * Sublass implementations should override this method to provide the Vector of Organizations
   * that should be used as suppliers for this task.
   *
   * @param task the task that suppliers are needed for.
   * @return (ordered) list of Organizations that can serve as supplier for the input Task.
   */
  protected Vector getSuppliersList( Task task) {
    Vector returnVector = new Vector();
    // default to SELF as only supplier
    returnVector.add( Utility.findNamedOrganization( getClusterName(), getOrganizationAssets()));
    return returnVector;
  }

  /**
   * Get next supplier from a list of suppliers in the WITHSUPPLIERS phrase of a task.
   *
   * @param task the Task that contains a list of suppliers in a WITHSUPPLIERS phrase.
   * @param curSupplier the last supplier that was used.
   * @return the next supplier to use
   */
  protected Organization getNextSupplier( Task task, Organization curSupplier) {
    Organization nextSupplier = null;

    String curSupplierName = curSupplier.getItemIdentificationPG().getNomenclature();

    PrepositionalPhrase pPhrase = prepHelper.getPrepNamed( task, Grammar.WITHSUPPLIERS);
    Vector suppliers = (Vector)pPhrase.getIndirectObject();
    Enumeration suppliersEnum = suppliers.elements();

    // Start by getting past the curSupplier in the supplierList.
    // If curSupplier is null, start at the beginning of the supplierList.
    if ( curSupplier != null) {
      while ( suppliersEnum.hasMoreElements()) {
        Organization supplier = (Organization)suppliersEnum.nextElement();
        String tmpSupplierName = supplier.getItemIdentificationPG().getNomenclature();
        if ( tmpSupplierName.equals( curSupplierName)) {
          // Found curSupplier in list, the nextElement will be the next supplier.
          break;
        }
      }
    }

    if ( suppliersEnum.hasMoreElements()) {
      nextSupplier = (Organization)suppliersEnum.nextElement();
    }

    return nextSupplier;
  }

  protected String getClusterName() {
    return clusterName;
  }

  public boolean getOrganizationAssetsHasChanged() {
    return orgAssets.hasChanged();
  }
  public Enumeration getOrganizationAssets() {
    return orgAssets.elements();
  }
  public Enumeration getOrganizationAssetsAdded() {
    return orgAssets.getAddedList();
  }
  public Enumeration getOrganizationAssetsChanged() {
    return orgAssets.getChangedList();
  }
  public Enumeration getOrganizationAssetsRemoved() {
    return orgAssets.getRemovedList();
  }

  public Enumeration getInputTasks() {
    return inputTasks.elements();
  }

  /**
   * Standard SimplePlugin method.
   * Store cluster name and setup subscriptions for input Tasks, Expansions and Allocations.
   */
  protected void setupSubscriptions() {
    clusterName = getClusterIdentifier().getAddress();

    orgAssets   = (IncrementalSubscription)subscribe( new OrganizationAssetsP());
    inputTasks  = (IncrementalSubscription)subscribe( new InputTasksP());
    expansions  = (IncrementalSubscription)subscribe( new ExpansionsP());
  }

  public synchronized void execute() {
    // Call transactionInit() first.
    // This way subclasses can do initialization at the beginning of each execute() loop.
    transactInit();

    if ( inputTasks != null && inputTasks.hasChanged()) {
      Enumeration addedInputTasksEnum = inputTasks.getAddedList();
      while ( addedInputTasksEnum.hasMoreElements()) {
        Task tmpTask = (Task)addedInputTasksEnum.nextElement();
        expandTask( tmpTask);
      }
      Enumeration changedInputTasksEnum = inputTasks.getChangedList();
      while ( changedInputTasksEnum.hasMoreElements()) {
        Task task = (Task)changedInputTasksEnum.nextElement();
        handleChangedTask( task);
      }
    }

    if ( expansions != null && expansions.hasChanged()) {
      Enumeration changedExpansions = expansions.getChangedList();
      while ( changedExpansions.hasMoreElements()) {
        Expansion expansion = (Expansion)changedExpansions.nextElement();
        expanderRollup( expansion);
      }

      Enumeration removedExpansions = expansions.getRemovedList();
      while ( removedExpansions.hasMoreElements()) {
        Expansion expansion = (Expansion)removedExpansions.nextElement();
        Workflow workflow = expansion.getWorkflow();
        if ( workflow != null) {
          Enumeration subtasks = workflow.getTasks();
          if ( subtasks != null) {
            while ( subtasks.hasMoreElements()) {
             publishRemove( subtasks.nextElement());
            }
          }
        }
      }
    }
  }

  /**
   * This method provides the fundamental logic for the Multiple Suppliers plugins.
   * This method is called by the plugin's execute() method when a Source Task's Expansion
   * is marked as changed. This method will detect if the SOURCE task is completely satisfied.
   * If not, the next supplier will be identified and that Organization will be sent a task
   * to help fulfill the SOURCE request. When the SOURCE task is completely satisfied or when
   * there are no more suppliers to use, this method will rollup the results and notify the original
   * requester.
   *
   * @param expansion the Expansion object that has been marked as changed.
   */
  protected synchronized void expanderRollup( Expansion expansion) {
    Task task = expansion.getTask();
    String verbStr = task.getVerb().toString();
    if ( verbStr.equals( Grammar.SOURCE)) {
      Workflow workflow = expansion.getWorkflow();
      if ( workflow != null) {
        AllocationResult reportedResult = expansion.getReportedResult();
        if ( reportedResult != null) {
          AllocationResult estimatedResult = expansion.getEstimatedResult();
          if ( estimatedResult == null || !Utility.equalResults( estimatedResult, reportedResult)) {

            double prefQuantity = task.getPreferredValue( AspectType.QUANTITY);
            double reportedQuantity = reportedResult.getValue( AspectType.QUANTITY);
            double shortfallCount = prefQuantity - reportedQuantity;

            if ( shortfallCount == 0) {
              // At the point where shortfallCount is 0,
              // make sure there are no unnecessary subtasks at the end of the workflow.
              boolean workflowChanged = false;
              boolean deletedFromEndOfWorkflow = true;
              while ( deletedFromEndOfWorkflow) {
                deletedFromEndOfWorkflow = false;
                Task lastSubtask = Utility.getLastSubtaskInWorkflow( workflow);
                Allocation allocation = (Allocation)lastSubtask.getPlanElement();
                AllocationResult lastSubtaskResult = allocation.getReportedResult();
                if ( lastSubtaskResult.getValue( AspectType.QUANTITY) == 0) {
                  workflowChanged = true;
                  deletedFromEndOfWorkflow = true;
                  ((NewWorkflow)workflow).removeTask( lastSubtask);
                  publishRemove( lastSubtask);
                }
              }
              if ( workflowChanged) {
                publishChange( workflow);
              }

              // Create Estimated Results. Setup default values first.
              double estimatedCost = 0.0;
              double estimatedQuantity = reportedQuantity;
              double estimatedStart = task.getPreferredValue( AspectType.START_TIME);
              double estimatedEnd = task.getPreferredValue( AspectType.END_TIME);
              // Override default values with those in Reported Result if exist there.
              if ( reportedResult.isDefined( AspectType.COST)) {
                estimatedCost = reportedResult.getValue( AspectType.COST);
              }
              if ( reportedResult.isDefined( AspectType.START_TIME)) {
                estimatedStart = reportedResult.getValue( AspectType.START_TIME);
              }
              if ( reportedResult.isDefined( AspectType.END_TIME)) {
                estimatedEnd = reportedResult.getValue( AspectType.END_TIME);
              }

              AllocationResult newEstimatedResult = Utility.createAllocationResult( getFactory(), true,
                                                                                    estimatedStart, estimatedEnd,
                                                                                    estimatedCost, estimatedQuantity);
              expansion.setEstimatedResult( newEstimatedResult);
              publishChange( expansion);
            } else {
              Enumeration subtasks = workflow.getTasks();
              Task lastSubtask = Utility.getLastSubtaskInWorkflow( workflow);

              if ( shortfallCount < 0) {
                // A negative shortfall means we have too many items.
                // Remove the last subtask. We'll rework that one from scratch when this wakes back up.
                ((NewWorkflow)workflow).removeTask( lastSubtask);
                publishRemove( lastSubtask);
                publishChange( workflow);
                publishChange( expansion);
              } else {
                PlanElement planElement = lastSubtask.getPlanElement();
                if ( !( planElement instanceof Allocation)) {
                  expansion.setEstimatedResult( reportedResult);
                  publishChange( expansion);
                } else {
                  Allocation allocation = (Allocation)lastSubtask.getPlanElement();

                  // Need to determine if there is a "next" supplier.
                  // Two cases:
                  // 1. If this is the initial source mode, then the last supplier used was unable
                  //    to completely fill the request. So, look for the next supplier in the suppliers list.
                  // 2. If one of the already used suppliers has retracted one of the allocations, then the
                  //    lastsupplier we used may still have more. Check him first if he did not previously
                  //    report a shortfall.
                  double lastSubtaskPrefQuantity = lastSubtask.getPreferredValue( AspectType.QUANTITY);
                  AllocationResult lastSubtaskResult = allocation.getReportedResult();
                  double lastSubtaskReportedQuantity = lastSubtaskResult.getValue( AspectType.QUANTITY);
                  Organization lastSubtaskSupplier = (Organization)allocation.getAsset();

                  Organization nextSupplier = (Organization)allocation.getAsset();
                  if ( lastSubtaskReportedQuantity < lastSubtaskPrefQuantity) {
                    nextSupplier = getNextSupplier( lastSubtask, lastSubtaskSupplier);
                  }

                  if ( nextSupplier != null) {
                    NewTask subtask = Utility.cloneTask( getFactory(), lastSubtask, null);

                    AspectValue quantityAV = AspectValue.newAspectValue( AspectType.QUANTITY, shortfallCount);
                    ScoringFunction quantitySF = ScoringFunction.createNearOrBelow( quantityAV, shortfallCount);
                    Preference quantityPreference = getFactory().newPreference( AspectType.QUANTITY, quantitySF);
                    prefHelper.replacePreference( subtask, quantityPreference);

                    NewPrepositionalPhrase newPhrase = getFactory().newPrepositionalPhrase();
                    newPhrase.setPreposition( Grammar.USESUPPLIER);
                    newPhrase.setIndirectObject( nextSupplier);
                    prepHelper.replacePrepOnTask( subtask, newPhrase);

                    boolean redirectToCluster = false;
                    // If nextSupplier is myself, set the verb to SUPPLY.
                    // If nextSupplier does not have role of MultipleSupplierCapable, set the verb to SUPPLY.
                    // Otherwise, set the verb to SOURCE.
                    if ( nextSupplier == Utility.findNamedOrganization( getClusterName(), getOrganizationAssets())) {
                      subtask.setVerb( new Verb( Constants.Verb.SUPPLY));
                    } else {
                      subtask.setVerb( new Verb( Grammar.SOURCE));
                    }

                    ((NewWorkflow)workflow).addTask( subtask);
                    subtask.setWorkflow( workflow);
                    publishChange( workflow);
                    publishAdd( subtask);
                  } else {
                    expansion.setEstimatedResult( reportedResult);
                    publishChange( expansion);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Create the original subtask to be sent to the first supplier in order to fill a SOURCE request.
   *
   * @param task the SOURCE task that needs to be expanded.
   */
  protected boolean expandTask( Task task) {
    boolean retval = false;
    NewTask subtask = null;
    Vector subtasks = new Vector();

    String verbStr = task.getVerb().toString();
    if ( verbStr.equals( Grammar.SOURCE)) {
      retval = true;

      Vector mySuppliers = getSuppliersList( task);
      if ( mySuppliers == null) {
        return retval;
      }

      subtask = Utility.createSubtaskFromTask( getFactory(), task);

      Vector usedSuppliersVector = null;
      // Remove any "already used" suppliers from the mySuppliers list, except for SELF.
      // Then add the remaining "new" suppliers to the UsedSuppliersList (if they aren't already in there)
      // so they won't get tasked by other suppliers' source tasks
      NewPrepositionalPhrase usedSuppliersPhrase
          = (NewPrepositionalPhrase)prepHelper.getPrepNamed( subtask, Grammar.WITHUSEDSUPPLIERS);
      if ( usedSuppliersPhrase != null) {
        usedSuppliersVector = (Vector)usedSuppliersPhrase.getIndirectObject();
        Enumeration usedSuppliersEnum = usedSuppliersVector.elements();
        while ( usedSuppliersEnum.hasMoreElements()) {
          String usedSupplierName = (String)usedSuppliersEnum.nextElement();
          Enumeration mySupplierEnum = mySuppliers.elements();
          while ( mySupplierEnum.hasMoreElements()) {
            Organization mySupplier = (Organization)mySupplierEnum.nextElement();
            String mySupplierName = mySupplier.getItemIdentificationPG().getNomenclature();
            if ( mySupplierName.equals( usedSupplierName) && !mySupplierName.equals( getClusterName())) {
              mySuppliers.removeElement( mySupplier);
              break;
            }
          }
        }
      } else {
        usedSuppliersPhrase = getFactory().newPrepositionalPhrase();
        usedSuppliersPhrase.setPreposition( Grammar.WITHUSEDSUPPLIERS);
        usedSuppliersPhrase.setIndirectObject( new Vector());
        prepHelper.replacePrepOnTask( subtask, usedSuppliersPhrase);
        usedSuppliersVector = (Vector)usedSuppliersPhrase.getIndirectObject();
      }

      if ( !usedSuppliersVector.contains( getClusterName())) {
        usedSuppliersVector.add( getClusterName());
      }
      Enumeration suppliersEnum = mySuppliers.elements();
      while ( suppliersEnum.hasMoreElements()) {
        Organization supplier = (Organization)suppliersEnum.nextElement();
        String supplierName = supplier.getItemIdentificationPG().getNomenclature();
        if ( !usedSuppliersVector.contains( supplierName)) {
          usedSuppliersVector.add( supplierName);
        }
      }

      NewPrepositionalPhrase newPhrase = getFactory().newPrepositionalPhrase();
      newPhrase.setPreposition( Grammar.WITHSUPPLIERS);
      newPhrase.setIndirectObject( mySuppliers);
      prepHelper.replacePrepOnTask( subtask, newPhrase);

      // Put in a USESUPPLIER clause in order to allocate this subtask to the first supplier.
      // Note that the SourceAllocator will create a FailedAllocation if the indirect object
      // of the USESUPPLIER phrase is null.
      Organization firstSupplier = null;
      if ( mySuppliers.size() > 0) {
        firstSupplier = (Organization)mySuppliers.elementAt( 0);
      }
      newPhrase = getFactory().newPrepositionalPhrase();
      newPhrase.setPreposition( Grammar.USESUPPLIER);
      newPhrase.setIndirectObject( firstSupplier);
      prepHelper.replacePrepOnTask( subtask, newPhrase);

      boolean redirectToCluster = false;
      // If firstSupplier is myself, set the verb to SUPPLY.
      // If firstSupplier does not have role of MultipleSupplierCapable, set the verb to SUPPLY.
      // Otherwise, set the verb to SOURCE.
      if ( firstSupplier == Utility.findNamedOrganization( getClusterName(), getOrganizationAssets())) {
        subtask.setVerb( new Verb( Constants.Verb.SUPPLY));
      } else {
        subtask.setVerb( new Verb( Grammar.SOURCE));
      }

      subtasks.add( subtask);
      Workflow wf = expandHelper.makeWorkflow( getFactory(), subtasks, task);
      publishAddExpansion( expandHelper.makeExpansion( getFactory(), wf));
    }
    return retval;
  }

  /**
   * Add an Expansion object to the Blackboard. This implementation automatically
   * adds the associated Workflow and each of the Workflow's subtasks to the blackboard.
   * @param expansion the Expansion being added to the Blackboard.
   */
  private boolean publishAddExpansion( Expansion expansion) {
    if ( publishAdd( expansion) == false){
      return false;
    }
    if ( publishAdd( expansion.getWorkflow()) == false) {
      return false;
    }
    Enumeration subtasks = expansion.getWorkflow().getTasks();
    while ( subtasks.hasMoreElements()) {
      if ( publishAdd( subtasks.nextElement()) == false) {
        return false;
      }
    }
    return true;
  }

  protected LoggingService logger;
  protected UTILExpand expandHelper;
  protected UTILPreference prefHelper;
  protected UTILPrepPhrase prepHelper;
}
