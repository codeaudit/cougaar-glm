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

package org.cougaar.mlm.plugin.sample;

import org.cougaar.glm.ldm.Constants;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.glm.*;
import org.cougaar.glm.ldm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.glm.xml.parser.LocationParser;
import org.cougaar.planning.ldm.plan.*;

import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.plugin.util.ExpanderHelper;
import org.cougaar.core.plugin.util.PluginHelper;

import org.cougaar.util.SingleElementEnumeration;
import org.cougaar.util.ShortDateFormat;
import org.cougaar.util.StringUtility;
import org.cougaar.core.util.UID;
import org.cougaar.core.service.LoggingService;

import org.cougaar.util.UnaryPredicate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * Class <code>StrategicTransportProjectorPlugin</code> is a replacement
 * for <code>StrategicTranportProjectionExpanderPlugin</code>.
 * <p>
 * This class subscribes to the single "Deploy" "DetermineRequirements" 
 * Task and expands it to "Transport" Tasks for all applicable assets.
 * <p>
 * Currently expects only one oplan, one "self" org activity, and one
 * "Deploy" "DetermineRequirements" task.
 * <p>
 * Debug information is now off by default.  See method <code>setDebug()</code>
 */
public class StrategicTransportProjectorPlugin extends SimplePlugin {

  public final static long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

  /** Self Organization Info **/
  protected IncrementalSubscription selfOrgsSub;
  // On rejydration filled in from selfOrgsSub;
  protected Organization selfOrg;
  protected String selfOrgId = "XXXSelfOrgNotSetYet";

  /** Subscription to orgActivities for Deployment **/
  protected IncrementalSubscription orgDeployActsSub;

  /** Subscription to DetermineRequirement Tasks **/
  protected IncrementalSubscription drTasksSub;

  /** Subscription to Transportable Person Assets **/
  protected IncrementalSubscription transAssetsPersonSub;

  /** Subscription to Transportable Equipment Assets **/
  protected IncrementalSubscription transAssetsEquipmentSub;

  /** Subscription to Failed Allocations **/
  protected IncrementalSubscription failedDRAllocsSub;

  /** Subscription to Expansions **/
  protected IncrementalSubscription expansionsSub;

  /** List of determine requirements tasks waiting to be added **/
  protected WaitingTask unhandledDetermineRequirementsTask;

  // Following set from invocation parameters
  /** Expand into an AssetGroup if true, else separate Tasks if false **/
  protected boolean createAssetGroups = true;

  /** delay re-processing DetermineRequirement Tasks by specified millis **/
  protected long delayReprocessMillis;

  /** Defaults optionally set by parameters. **/
  protected int defaultAdjustDurationDays;

  /** XML file containing description of GeolocLocation used for Task fromLocation **/
  protected String originFile = null;

  /** Number of days added to the Oplan C Day used for Task startDate **/
  protected int offsetDays = 0;

  /** GeolocLocation specified in XML file used for Task fromLocation  **/
  protected GeolocLocation overrideFromLocation = null;


  /** Holder for info required to handle the determine requirements task. **/
  protected DeployPlan singleDeployPlan = null;


  /**
   * Subscribe.
   */
  protected void setupSubscriptions() {
    if (logger.isInfoEnabled()) {
      printInfo(getClass()+" setting up subscriptions");
    }

    setDefaults(getParameters().elements());

    selfOrgsSub = (IncrementalSubscription) subscribe(newSelfOrgPred());

    orgDeployActsSub = (IncrementalSubscription) 
      subscribe(newOrgDeployActsPred());

    drTasksSub = (IncrementalSubscription) 
      subscribe(newDRTasksPred());
                
    transAssetsPersonSub = (IncrementalSubscription) 
      subscribe(newTransPersonPred());

    transAssetsEquipmentSub = (IncrementalSubscription) 
      subscribe(newTransEquipmentPred());

    failedDRAllocsSub = (IncrementalSubscription) 
      subscribe(newFailedDRAllocPred());
  
    expansionsSub = (IncrementalSubscription) subscribe(newExpansionsPred());

    if (originFile != null) {
      overrideFromLocation = getGeoLoc(originFile);
    }

    unhandledDetermineRequirementsTask = null;

    // Fill in state from subscriptions if rehydrating
    if (didRehydrate()) {
      // Try to fill in self org info
      if (!selfOrgsSub.isEmpty()) {
        watchAddedSelfOrgs(selfOrgsSub.elements());;
      }

      //Initialize deployPlan 
      if (!orgDeployActsSub.isEmpty()) {
        OrgActivity selfOrgAct = findSelfOrgDeployAct(orgDeployActsSub.elements());
        if (selfOrgAct != null) {
          replaceDeployPlan(selfOrgAct);
        }
      }

      // Need to fill in unhandled
      Task drTask = findSingleDetermineRequirementsTask(drTasksSub.elements());
      // Assume we only worry about dr task where plan element is null
      if ((drTask != null) && (drTask.getPlanElement() == null)) {
        // Put on unhandled list so we know about it
        unhandledDetermineRequirementsTask = new WaitingTask(delayReprocessMillis,
                                                             drTask);
        // Have a deploy plan so we should be able to process the task.
        // Set up alarm so that we handle in execute loop
        if (getDeployPlan() != null) {
          wakeAfterRealTime(delayReprocessMillis);
        }
      }
      else if (drTask != null) { // it had a plan element - fix for bug #2376
	  DeployPlan dp = getDeployPlan ();
	  dp.detReqsTask = drTask;
	  dp.expandedWorkflow = 
	      (NewWorkflow) ((Expansion) drTask.getPlanElement()).getWorkflow();

	  if (dp.expandedWorkflow == null) 
	      logger.error ("??? No workflow found for d.r. task " + 
			    drTask.getUID());
      }
    }

  }        

  /**
   * Execute.
   */
  protected void execute() {
                  
    if (selfOrgsSub.hasChanged()) {
      if (logger.isInfoEnabled()) {
        printInfo("selfOrgs hasChanged");
      }
      watchAddedSelfOrgs(selfOrgsSub.getAddedList());
    }

    if (orgDeployActsSub.hasChanged()) {
      if (logger.isInfoEnabled()) {
        printInfo("orgDeployActs hasChanged");
      }
      watchChangedOrgDeployActs(orgDeployActsSub.getChangedList());
      watchRemovedOrgDeployActs(orgDeployActsSub.getRemovedList());
      watchAddedOrgDeployActs(orgDeployActsSub.getAddedList());
    }


    if (drTasksSub.hasChanged()) {
      if (logger.isInfoEnabled()) {
        printInfo("DetermineReqs Tasks hasChanged");
      }
      watchChangedDetermineRequirementsTasks(
          drTasksSub.getChangedList());
      watchRemovedDetermineRequirementsTasks(
          drTasksSub.getRemovedList());
      watchAddedDetermineRequirementsTasks(
          drTasksSub.getAddedList());
    } else if (getDeployPlan() != null) {
      // Process any unhandled dr tasks
      watchDueDetermineRequirementsTasks();
    }

    
    if (transAssetsPersonSub.hasChanged()) {
      if (logger.isInfoEnabled()) {
        printInfo("Transportable Assets hasChanged");
      }
      watchChangedTransportableAssets(transAssetsPersonSub.getChangedList());
      watchRemovedTransportableAssets(transAssetsPersonSub.getRemovedList());
      watchAddedTransportableAssets(transAssetsPersonSub.getAddedList());
    }

    if (transAssetsEquipmentSub.hasChanged()) {
      if (logger.isInfoEnabled()) {
        printInfo("Transportable Assets hasChanged");
      }
      watchChangedTransportableAssets(transAssetsEquipmentSub.getChangedList());
      watchRemovedTransportableAssets(transAssetsEquipmentSub.getRemovedList());
      watchAddedTransportableAssets(transAssetsEquipmentSub.getAddedList());
    }

    if (failedDRAllocsSub.hasChanged()) {
      if (logger.isInfoEnabled()) {
        printInfo("Failed DR Subtask Allocation hasChanged");
      }
      watchFailedDispositions(failedDRAllocsSub.getChangedList());
    }

    if (expansionsSub.hasChanged()) {
      PluginHelper.updateAllocationResult(expansionsSub);
    }
                                    
  }

  /**
   * Only one plan with one "Deployment" DetermineRequirements Task expected!
   */

  protected DeployPlan getDeployPlan() {
    return singleDeployPlan;
  }
  protected void setDeployPlan(DeployPlan dp) {
    singleDeployPlan = dp;
  }

  /**
   * These methods related to having a single oplan, a single self 
   * organization, a single self OrgActivity, and a single 
   * "Deployment" "DetermineRequirements" Task.
   */

  protected void watchAddedSelfOrgs(Enumeration eSelfOrgs) {
    if (eSelfOrgs.hasMoreElements()) {
      this.selfOrg = (Organization)eSelfOrgs.nextElement();
      this.selfOrgId = getOrgID(selfOrg);
      if (logger.isInfoEnabled()) {
        printInfo("Found Self: "+this.selfOrgId);
      }
      if (eSelfOrgs.hasMoreElements()) {
        printError(
           "Expecting only one \"SELF\" Organization! ignoring the others");
      }
    }
  }

  protected OrgActivity findSelfOrgDeployAct(Enumeration eOrgActs) {
    while (true) {
      if (!(eOrgActs.hasMoreElements())) {
        // self not listed
        return null;
      }
      OrgActivity orgAct = (OrgActivity)eOrgActs.nextElement();
      if (selfOrgId.equals(orgAct.getOrgID())) {
        // found self org activity
        return orgAct;
      }
    }
  }
  
  protected void watchAddedOrgDeployActs(Enumeration eOrgActs) {
    OrgActivity selfOrgAct = findSelfOrgDeployAct(eOrgActs);
    if (selfOrgAct != null) {
      watchAddedSelfOrgDeployAct(selfOrgAct);
    }
  }

  protected void watchChangedOrgDeployActs(Enumeration eOrgActs) {
    OrgActivity selfOrgAct = findSelfOrgDeployAct(eOrgActs);
    if (selfOrgAct != null) {
      watchChangedSelfOrgDeployAct(selfOrgAct);
    }
  }

  protected void watchRemovedOrgDeployActs(Enumeration eOrgActs) {
    OrgActivity selfOrgAct = findSelfOrgDeployAct(eOrgActs);
    if (selfOrgAct != null) {
      watchRemovedSelfOrgDeployAct(selfOrgAct);
    }
  }

  protected void watchDueDetermineRequirementsTasks() {
    if ((unhandledDetermineRequirementsTask != null) &&
        (unhandledDetermineRequirementsTask.isDue())) {
      if (logger.isInfoEnabled()) {
        printInfo("watchDueDetermineRequirementsTask - resubmitting dr task");
      }
      handleDetermineRequirementsTask(unhandledDetermineRequirementsTask.task);
    }
    else if (logger.isInfoEnabled ()) {
        printDebug("watchDueDetermineRequirementsTask - ignoring call to watch due, since unhandled d.r. is " +
		   unhandledDetermineRequirementsTask + 
		   ((unhandledDetermineRequirementsTask == null) ? "" : " or isDue is " + 
		    unhandledDetermineRequirementsTask.isDue ()));
    }
  }
  
  protected Task findSingleDetermineRequirementsTask(Enumeration eTasks) {
    if (!(eTasks.hasMoreElements())) {
      // no task listed
      return null;
    }
    Task drTask = (Task)eTasks.nextElement();
    if (eTasks.hasMoreElements()) {
      // should be only one determine requirements task
      printError("Expecting only one DetermineReqs Task! ignoring the others");
    }
    return drTask;
  }

  protected void watchAddedDetermineRequirementsTasks(Enumeration eTasks) {
    Task drTask = findSingleDetermineRequirementsTask(eTasks);
    if (drTask != null) {
      handleDetermineRequirementsTask(drTask);
    }
  }

  protected void watchChangedDetermineRequirementsTasks(Enumeration eTasks) {
    Task drTask = findSingleDetermineRequirementsTask(eTasks);
    if (drTask != null) {
      handleDetermineRequirementsTask(drTask);
    }
    else if (logger.isInfoEnabled ())
      printInfo ("watchChangedDetermineRequirementsTasks - could not find D.R. task?");
  }

  /**
   * Handle removed DetermineRequirements Task.
   * <p>
   * Removed task should remove it's subtasks, so we only need
   * to clean out it's DeployPlan.
   */
  protected void watchRemovedDetermineRequirementsTasks(Enumeration eTasks) {
    Task drTask = findSingleDetermineRequirementsTask(eTasks);
    if (drTask != null) {
      if (logger.isInfoEnabled()) {
        printInfo("Remove DetReqs Task");
      }
      DeployPlan dp = getDeployPlan();
      if (dp == null) {
        // never added?
        return;
      }
      dp.detReqsTask = null;
      dp.expandedWorkflow = null;
    }
  }

  /**
   * Actually handle our subscriptions
   */

  /**
   * Handle added OrgActs with type "Deployment".  We want this 
   * OrgActivity-based <code>DeployPlan</code> saved.
   */
  protected void watchAddedSelfOrgDeployAct(OrgActivity selfOrgAct) {
    if (logger.isInfoEnabled()) {
      printInfo("Adding orgActivity!: "+selfOrgAct+"\nbegin");
    }

    DeployPlan oldDeployPlan = getDeployPlan();
    Task oldDRTask = null;
    if ((oldDeployPlan != null) && (oldDeployPlan.expandedWorkflow != null)) {
      killWorkflow(oldDeployPlan.expandedWorkflow);
      oldDeployPlan.expandedWorkflow = null;
      oldDRTask = oldDeployPlan.detReqsTask;
    }

    replaceDeployPlan(selfOrgAct);

    if (oldDRTask != null) {
	handleDetermineRequirementsTask (oldDRTask);
    }
    else {
	// Re-process existing DETERMINEREQUIREMENTS task in
	// light of the new Org Activity
	watchDueDetermineRequirementsTasks();
    }

    if (logger.isInfoEnabled()) {
      printInfo("Added orgActivity!: "+selfOrgAct+"\ndone");
    }
  }


  /**
   * Handle changed OrgActs.  We may need to change the associated
   * <code>DeployPlan</code> and/or tweek the expansion.
   * <p>
   * For now we do something harsh:<br>
   * <ol>
   *   <li>clear the existing orgAct deploy plan</li>
   *   <li>re-add the orgAct for a new deploy plan</li>
   *   <li>if there was a DRTask, re-add it over the old one.</li>
   * </ol>
   */
  protected void watchChangedSelfOrgDeployAct(OrgActivity selfOrgAct) {
    if (logger.isInfoEnabled()) {
      printInfo("Changed orgActivity!: "+selfOrgAct+"\nbegin");
    }

    watchAddedSelfOrgDeployAct(selfOrgAct);

    if (logger.isInfoEnabled()) {
      printInfo("Changed orgActivity!: "+selfOrgAct+"\ndone");
    }
  }


  /**
   * Handle removed selfOrgAct.  We'll assume that the tasks are 
   * killed off separately (e.g. by removing the DRTask), so all that
   * is needed here is to remove the DeployPlan.
   */
  protected void watchRemovedSelfOrgDeployAct(OrgActivity selfOrgAct) {
    // throw away the deploy plan
    DeployPlan oldDeployPlan = getDeployPlan();
    if (oldDeployPlan != null) {
      if (logger.isInfoEnabled()) {
        printInfo("Remove Deployment Plan!\nOLD:\n"+oldDeployPlan);
      }
      setDeployPlan(null);

      // Kill Associated Expansion
      if (oldDeployPlan.expandedWorkflow != null) {
        killWorkflow(oldDeployPlan.expandedWorkflow);
      }
    }
  }

  /**
   * Take added transportable <code>Assets</code> and expand them to
   * transport tasks.
   * <p>
   * @param eAssets Enumeration of added assets
   */
  protected void watchAddedTransportableAssets(Enumeration eAssets) {
    if (!(eAssets.hasMoreElements())) {
      // no assets changed
      return;
    }
    DeployPlan dp = getDeployPlan();
    if (dp == null) {
      // no deploy plan yet
      if (logger.isInfoEnabled()) {
        printInfo("Assets added, waiting for OrgActivity");
      }
      return;
    }
    Task drTask = dp.detReqsTask;
    if (drTask == null) {
      // no determine requirements task yet
      if (logger.isInfoEnabled()) {
        printInfo("Assets added, waiting for OrgActivity");
      }
      return;
    }
    if (logger.isInfoEnabled()) {
      printInfo("Adding Assets with DP: "+ dp);
    }
    expandDetermineRequirements(dp, drTask, eAssets);
  }

  /**
   * Handle changed transportable <code>Asset</code>s.
   * <p>
   * Do this the hard way:<br>
   * <ol>
   *   <li>remove all subtasks created to transport the assets</li>
   *   <li>make new subtasks to transport them</li>
   * </ol>
   * <p>
   * @param eAssets Enumeration of added assets
   */
  protected void watchChangedTransportableAssets(Enumeration eAssets) {
    if (!(eAssets.hasMoreElements())) {
      // no assets changed
      return;
    }
    if (logger.isInfoEnabled()) {
      printInfo("Changed assets!");
    }
    Vector v = new Vector();
    while (eAssets.hasMoreElements()) {
      v.add(eAssets.nextElement());
    }
    watchRemovedTransportableAssets(v.elements());
    watchAddedTransportableAssets(v.elements());
  }

  /**
   * Handle removed transportable <code>Assets</code> by removing any
   * expanded determine requirements subtasks that depend on them.
   * <p>
   * @param eAssets Enumeration of assets that were removed
   */
  protected void watchRemovedTransportableAssets(Enumeration eAssets) {
    if (!(eAssets.hasMoreElements())) {
      // no assets
      return;
    }
    if (logger.isInfoEnabled()) {
      printInfo("Handle Asset removal");
    }
    // get workflow
    DeployPlan dp = getDeployPlan();
    if (dp == null) {
      // no deploy plan
      return;
    }
    NewWorkflow wf = dp.expandedWorkflow;
    if (wf == null) {
      // no workflow, so no tasks to worry about
      return;
    }
    // remove tasks
    Vector publishRemoveTasks = new Vector();
    Vector workflowRemoveTasks = new Vector();
    while (eAssets.hasMoreElements()) {
      Asset assetForTrans = (Asset)eAssets.nextElement();
      if (logger.isInfoEnabled()) {
        printInfo("Look for removed asset: "+assetForTrans);
      }
      workflowRemoveTasks.clear();
      Enumeration subTE = wf.getTasks();
      while (subTE.hasMoreElements()) {
        Task subT = (Task)subTE.nextElement();
        // FIXME! what if asset group!
        if (subT.getDirectObject() == assetForTrans) {
          if (logger.isInfoEnabled()) {
            printInfo("  Will remove Task: "+subT+
                       " with DO: "+assetForTrans);
          }
          workflowRemoveTasks.add(subT);
          publishRemoveTasks.add(subT);
        }
      }
      for (int i = workflowRemoveTasks.size(); --i >= 0; ) {
        Task removeWFT = (Task)workflowRemoveTasks.elementAt(i);
        if (logger.isInfoEnabled()) {
          printInfo("Remove from workflow Task: "+removeWFT);
        }
        wf.removeTask(removeWFT);
      }
    }
    for (int i = publishRemoveTasks.size(); --i >= 0; ) {
      Task removeT = (Task)publishRemoveTasks.elementAt(i);
      if (logger.isInfoEnabled()) {
        printInfo("publishRemove Task: "+removeT);
      }
      publishRemove(removeT);
    }
  }

  /**
   * Handle failed allocations to tasks that are subtasks to a
   * DetermineRequirements task we expanded.
   * <p>
   * If a subtask failed then we'll simply slide back start/end times
   * by the determineRequirement's DeployPlan adjustDurationDays.
   * <p>
   * Do we want to fix earliestDate against current time?
   * <p>
   * @param eFailedAllocs Enumeration of failed allocations
   */
  protected void watchFailedDispositions(Enumeration eFailedAllocs) {
    if (!(eFailedAllocs.hasMoreElements())) {
      // no failures
      return;
    }
    // get date adjusters
    DeployPlan dp = getDeployPlan();
    if (dp == null) {
      // no deploy plan
      return;
    }
    int adjustDurationDays = dp.adjustDurationDays;
    Date earliestDate = dp.startTime;
    // fix tasks
    do {
      Allocation failedAlloc = (Allocation)eFailedAllocs.nextElement();
      NewTask tFailed = (NewTask)failedAlloc.getTask();
      if (logger.isWarnEnabled()) {
        printWarn("Failed Allocation: "+failedAlloc.toString()+
		  " of Task: "+tFailed.toString());
      }
    } while (eFailedAllocs.hasMoreElements());
  }
  
  /**
   * Protected methods other than subcription watchers
   */

  /**
   * Little utility to remove a workflow and expansion
   */
  protected void killWorkflow(Workflow wf) {
    if (logger.isInfoEnabled()) {
      printInfo("  kill workflow: "+wf);
    }
    
    // Only need to kill subtasks if wf was set to not propagate
    if (!wf.isPropagatingToSubtasks()) {
      Enumeration subtasksE = wf.getTasks();
      while (subtasksE.hasMoreElements()) {
        Task subt = (Task)subtasksE.nextElement();
        if (logger.isInfoEnabled()) {
          printInfo("    remove task: "+subt);
        }
        publishRemove(subt);
      }
    }

    // okay to get parent
    Task ptask = wf.getParentTask();
    if (ptask != null) {
      PlanElement ppe = ptask.getPlanElement();
      if (ppe != null) {
        if (logger.isInfoEnabled()) {
          printInfo("  remove planElement: "+ppe);
          printInfo("  publish change the parent task: "+ptask.getUID());
        }
        publishRemove(ppe);
	//publishChange(ptask); // fix for bug #2329
      } else {
        if (logger.isInfoEnabled()) {
          printInfo("   no wf parent planElement");
        }
      }
    } else {
      if (logger.isInfoEnabled()) {
        printInfo("   no wf parent");
      }
    }
  }

  protected void replaceDeployPlan(OrgActivity selfOrgAct) {
    // We don't know how long this task will take, so we
    // use the default task adjustment duration!
    int adjustDurationDays = defaultAdjustDurationDays;
    Date prepoStartDate = null;

    if (offsetDays > 0) {
      // If OffsetDays was specified as an input parameter, use Oplan C Day + offset as 
      // start date for the Task
      Collection oplanCol = query(new OplanByUIDPred(selfOrgAct.getOplanUID()));
      // Should be exactly one oplan for an OrgActivity
      Oplan oplan = (Oplan) oplanCol.iterator().next();
      prepoStartDate = new Date(oplan.getCday().getTime() + (MILLIS_PER_DAY * offsetDays));
    }

    DeployPlan newDP = 
      new DeployPlan(selfOrg, selfOrgAct, adjustDurationDays, overrideFromLocation,  
                     prepoStartDate);
    if (logger.isInfoEnabled()) {
      printInfo(newDP.toString());
    }

    if (!newDP.isValid()) {
      if (newDP.fromLoc == null)
        printError("This Organization lacks a MilitaryPG HomeLocation!");
      printError("The DeployPlan lacks needed information and is ignored!");
      setDeployPlan(null);
    } else {
      DeployPlan oldDP = getDeployPlan();
      if (oldDP != null) {
        if (logger.isInfoEnabled()) {
          printInfo("Replace Deployment Plan!\n"+
                     "OLD:\n"+oldDP+"\n"+
                     "NEW:\n"+newDP);
        }
      }
      setDeployPlan(newDP);
    }
  }

  /** 
   * Debug printer
   */
  protected final void printDebug(String s) {
    logger.debug (getAgentIdentifier() + " - " + s);
  }

  /** 
   * Info printer
   */
  protected final void printInfo(String s) {
    logger.info (getAgentIdentifier() + " - " + s);
  }

  /** 
   * Warn printer
   */
  protected final void printWarn(String s) {
    logger.warn (getAgentIdentifier() + " - " + s);
  }

  protected final void printError(String s) {
    logger.error (getAgentIdentifier() + " - " + s);
  }

  protected void setDefaults(Enumeration eParams) {
    delayReprocessMillis = 0;
    defaultAdjustDurationDays = -2;
    while (eParams.hasMoreElements()) {
      String sParam = (String)eParams.nextElement();
      int sep = sParam.indexOf('=');
      if (sep > 0) {
        String name=sParam.substring(0, sep).trim();
        String val=sParam.substring(sep+1).trim();
        if (name.equals("debug")) {
        } else if (name.equalsIgnoreCase("defaultAdjustDurationDays")) {
          try {
            defaultAdjustDurationDays=Integer.parseInt(val);
          } catch (Exception e) {
            printError("Invalid integer for "+sParam);
          }
        } else if (name.equalsIgnoreCase("createAssetGroups")) {
          createAssetGroups = (val.equalsIgnoreCase("true"));
        } else if (name.equalsIgnoreCase("delayMillis")) {
          try {
            delayReprocessMillis = Long.parseLong(val);
          } catch (Exception e) {
            printError("Invalid long for "+sParam);
          }
        } else if (name.equalsIgnoreCase("defaultDurationDays")) {
          // old name for param!
          try {
            defaultAdjustDurationDays=(-(Integer.parseInt(val)));
          } catch (Exception e) {
            printError("Invalid number for "+sParam);
          }
        } else if (name.equalsIgnoreCase("OffsetDays") || name.equalsIgnoreCase("OffsetDay")) {
	  offsetDays = Integer.parseInt(val);
	} else if (name.equalsIgnoreCase("OriginFile")) {
	    if (!val.equalsIgnoreCase("HOME") && !val.equalsIgnoreCase("NONE")) {
	      originFile = val;
	    }
	} else {
          printError("Unknown parameter: "+name+"="+val);
        }
      }
    }
  }

  protected static String getOrgID(Organization org) {
    try {
      // FOR NOW:
      return org.getClusterPG().getMessageAddress().toString();
      // FOR LATER:
      //return org.getItemIdentificationPG().getItemIdentifier().toString();
    } catch (Exception e) {
      return null;
    }
  }

  protected static AbstractAsset strans = null;

  /**
   * Expand a DetermineRequirements <code>Task</code>.
   * <p>
   * All the expanded tasks are very similar.  Each task instance
   * is separately allocated, but the contents are shared (shallow copy)
   * <p>
   * Here we find the first and second assets from 
   * the <code>assetEnum</code>, optionally creating an
   * <code>AssetGroup</code>, then we expand to tasks.
   * <p>
   * @param drTask DetermineRequirements Task to expand
   * @param assetsEnum Enumeration of assets for transport
   */
  protected void expandDetermineRequirements(
      DeployPlan dp, Task drTask, 
      Enumeration assetsEnum) {
    if (logger.isDebugEnabled()) {
      printDebug("Expand task: "+drTask+" with DP: "+ dp);
    }

    // this is protection for problem where database may indicate zero items
    // of a certain type.
    assetsEnum = removeZeroQuantityAggregates (assetsEnum).elements();

    // get first and second assets from enumeration
    Asset firstAssetForTransport = null;
    Asset secondAssetForTransport = null;
    boolean isPersonAsset = false;
    while (true) {
      if (!(assetsEnum.hasMoreElements())) {
        if (firstAssetForTransport != null) {
          // okay, need to transport a single asset
          break;
        } else {
          // bad, nothing to transport?
          printError("No assets to Transport?!!!");
          return;
        }
      }
      Asset a = (Asset)assetsEnum.nextElement();
      if (a == null) {
        printError("Null asset for Transport?!!!");
        continue;
      }
      if (firstAssetForTransport == null) {
        // the first of the assets
        firstAssetForTransport = a;

	// Is this a Person or Equipment? Use first element to determine batch
	Object o = a;
	while (o instanceof AggregateAsset) {
          o = ((AggregateAsset)o).getAsset();
	}
        isPersonAsset = o instanceof Person;
      } else {
        // okay, need to transport multiple assets
        secondAssetForTransport = a;
        break;
      }
    }

    // have one or more assets to transport

    // check if we're grouping assets
    if (createAssetGroups &&
        (secondAssetForTransport != null)) {
      // make vector of assets
      Vector v = new Vector();
      v.addElement(firstAssetForTransport);
      v.addElement(secondAssetForTransport);
      while (assetsEnum.hasMoreElements()) {
        Asset a = (Asset)assetsEnum.nextElement();
        if (a == null) {
          printError("Null asset for Transport?!!!");
        } else {
          v.addElement(a);
        }
      }
      // create group
      AssetGroup ag = (AssetGroup) theLDMF.createAsset(AssetGroup.class);
      try {
        NewTypeIdentificationPG typeIdPG = 
          (NewTypeIdentificationPG)theLDMF.createPropertyGroup(
             TypeIdentificationPGImpl.class);
        typeIdPG.setTypeIdentification("trans_group");
        typeIdPG.setNomenclature("strat-proj-plugin_asset_group");
        ag.setTypeIdentificationPG(typeIdPG);
      } catch (Exception eFailedTypePG) {
        // don't care?
      }
      ag.setAssets(v);
      // pretend that we have only the asset group
      firstAssetForTransport = ag;
      secondAssetForTransport = null;
    }

    // do expansion

    // PREPOSITIONAL PHRASES
    Vector prepphrases = new Vector();

    //   TO  (my activity deployment location)
    NewPrepositionalPhrase to = theLDMF.newPrepositionalPhrase();
    to.setPreposition(Constants.Preposition.TO);
    to.setIndirectObject(dp.toLoc);
    prepphrases.addElement(to);        

    //   FROM  (my home location)
    NewPrepositionalPhrase from = theLDMF.newPrepositionalPhrase();
    from.setPreposition(Constants.Preposition.FROM);
    // HACK - assume that all the assets in the Enumeration are of the same type
    if (isPersonAsset || (dp.fromPrepoLoc == null)) {
      from.setIndirectObject(dp.fromLoc);
    } else {
      from.setIndirectObject(dp.fromPrepoLoc);
    }
    prepphrases.addElement(from);
                
    //   FOR  (me)
    NewPrepositionalPhrase forpp = theLDMF.newPrepositionalPhrase();
    forpp.setPreposition(Constants.Preposition.FOR);
    Object forOrgID;
    try {
      forOrgID = 
	dp.forOrg.getItemIdentificationPG().getItemIdentification();
    } catch (Exception eForOrg) {
      printError("SELF "+dp.forOrg+" Lacks ItemIdentification!  Using Org!");
      forOrgID = dp.forOrg;
    }
    forpp.setIndirectObject(forOrgID);
    prepphrases.addElement(forpp);
 
    //   OFTYPE  (StrategicTransport)
    // create prepositionalphrase oftype strategictransportation
    NewPrepositionalPhrase pp = theLDMF.newPrepositionalPhrase();
    pp.setPreposition(Constants.Preposition.OFTYPE);
    if (strans == null) {
      try {
        RootFactory ldmfactory = getFactory();
        Asset strans_proto = ldmfactory.createPrototype(
           Class.forName( "org.cougaar.planning.ldm.asset.AbstractAsset" ),
           "StrategicTransportation" );
        strans = (AbstractAsset)ldmfactory.createInstance( strans_proto );
      } catch (Exception exc) {
        printError("Unable to create abstract strategictransport\n"+exc);
      }
    }
    pp.setIndirectObject(strans);
    prepphrases.addElement(pp);

    // Kludge - add "PREPO" prepositional phrase if we aren't using the Org's from loc
    if ((dp.fromPrepoLoc != null) &&  !isPersonAsset) {
	pp = theLDMF.newPrepositionalPhrase();
	pp.setPreposition("PREPO");
	prepphrases.add(pp);
    }

    // PREFERENCES
    Vector prefs = new Vector();

    //   START DATE  (startTime)
    AspectValue startAV;
    if (isPersonAsset) {
      startAV = AspectValue.newAspectValue(AspectType.START_TIME, dp.startTime.getTime());
    } else {
      startAV = AspectValue.newAspectValue(AspectType.START_TIME, dp.prepoStartTime.getTime());
    }
    ScoringFunction startSF = 
     ScoringFunction.createNearOrAbove(startAV, 0);
    Preference startPref = 
     theLDMF.newPreference(AspectType.START_TIME, startSF);
    prefs.addElement(startPref);

    //   END DATE    (RDD - 5*range) <= RDD - range <= RDD
    Date lateEndDate = dp.thruTime;
    Date bestEndDate = ShortDateFormat.adjustDate(lateEndDate, 0, -dp.thruRange);
    Date earlyEndDate = ShortDateFormat.adjustDate(lateEndDate, 0, -5*dp.thruRange);

    if (earlyEndDate.before(dp.startTime))
      earlyEndDate = dp.startTime;

    AspectValue earlyEndAV = 
      AspectValue.newAspectValue(AspectType.END_TIME, earlyEndDate.getTime());
    AspectValue bestEndAV = 
      AspectValue.newAspectValue(AspectType.END_TIME, bestEndDate.getTime());
    AspectValue lateEndAV = 
      AspectValue.newAspectValue(AspectType.END_TIME, lateEndDate.getTime());

    ScoringFunction endSF = 
      ScoringFunction.createStrictlyBetweenWithBestValues(
          earlyEndAV, bestEndAV, lateEndAV);
    Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
    prefs.addElement(endPref);

    // Get Workflow
    NewWorkflow wf = dp.expandedWorkflow;
    boolean newWf = (wf == null);
    if (newWf) {
      wf = theLDMF.newWorkflow();
      dp.expandedWorkflow = wf;

      wf.setIsPropagatingToSubtasks(true);
      wf.setParentTask(drTask);
      if (logger.isDebugEnabled()) {
        printDebug("made new workflow - " + wf);
      }
    } else {
      if (logger.isDebugEnabled()) {
        printDebug("Using existing workflow - " + wf + " that has these tasks : ");
      }
      Enumeration eT = wf.getTasks();
      while (eT.hasMoreElements()) {
        Task ewft = (Task)eT.nextElement();
        if (logger.isDebugEnabled()) {
          printDebug(" task: "+ewft.getVerb()+ " (" + ewft.getUID() + ") " +
                     " asset: "+ewft.getDirectObject());
        }
      }
    }

    // Make sure we can modify the PlanElement!

    // create the subtasks.  first add our "first" and "second"
    // assets, then use the enumeration.
    boolean useAssetsEnum = false;
    Asset assetForTransport = firstAssetForTransport;
    while (true) {
      // Create transport task 
      NewTask subtask = makeTransportTask (drTask, assetForTransport, prepphrases, prefs, wf);

      // publish task
      publishAdd(subtask);

      // next asset
      if (useAssetsEnum) {
        // third and later assets
        if (!(assetsEnum.hasMoreElements())) {
          // done!
          break;
        }
        assetForTransport = (Asset)assetsEnum.nextElement();
        if (assetForTransport == null) {
          printError("Null asset for Transport?!!!");
          continue;
        }
      } else {
        // second asset
        if (secondAssetForTransport == null) {
          // done!  only one asset to transport.
          break;
        }
        assetForTransport = secondAssetForTransport;
        // next time use the enumeration
        useAssetsEnum = true;
      }
    }

    if (newWf) {
      // make the expansion
      if (logger.isDebugEnabled()) {
        printDebug("Create expansion of determine requirement task " + drTask.getUID());
      }
      AllocationResult estimatedResult = 
        PluginHelper.createEstimatedAllocationResult(drTask, theLDMF, 1.0, true);
      PlanElement pe = 
        theLDMF.createExpansion(
          drTask.getPlan(),
          drTask,
          wf,
          estimatedResult);
      publishAdd(pe);

      if (logger.isDebugEnabled()) {
	printDebug("Published new DetermineReqs Task Expansion for d.r. task " + drTask.getUID());
      }
    }
    else {
      if (logger.isDebugEnabled()) {
	printDebug("Publish changed expansion of d.r. task " + drTask.getUID());
      }
	
      publishChange (wf.getParentTask().getPlanElement());
    }
  }

  protected NewTask makeTransportTask (Task drTask, Asset assetForTransport, Vector prepphrases, Vector prefs, NewWorkflow wf) {
    NewTask subtask = theLDMF.newTask();
    subtask.setParentTask( drTask );
    subtask.setDirectObject( assetForTransport );
    subtask.setPrepositionalPhrases( prepphrases.elements() );
    subtask.setVerb( Constants.Verb.Transport );
    subtask.setPlan( drTask.getPlan() );
    subtask.setPreferences( prefs.elements() );
    subtask.setSource( getMessageAddress() );

    // add to workflow
    wf.addTask(subtask);
    subtask.setWorkflow(wf);

    return subtask;
  } 

  /** 
   * Filter out invalid zero quantity aggregates from those to make transport tasks for 
   *
   * @param assets original list of assets published to blackboard
   * @return Vector of valid assets
   */
  protected Vector removeZeroQuantityAggregates (Enumeration assets) {
    Vector validAssets = new Vector();

    for (;assets.hasMoreElements ();) {
      Object asset = assets.nextElement ();
      
      if (asset instanceof AggregateAsset) {
	AggregateAsset aggregate = (AggregateAsset) asset;
	if (aggregate.getQuantity() > 0)
	  validAssets.add (asset);
	else if (logger.isInfoEnabled()) // ignore zero quantity aggregates
	  logger.info (getAgentIdentifier () + " - removeZeroQuantityAggregates - NOTE : " + 
		       " ignoring zero quantity aggregate of : " + aggregate.getAsset() + 
		       " since it's quantity is " + aggregate.getQuantity());
      }
      else
	validAssets.add (asset);
    }

    return validAssets;
  }

  private void handleDetermineRequirementsTask(Task drTask) {
    DeployPlan dp = getDeployPlan();
    if (dp == null) {
      // no deploy plan so put in unhandled
      if (logger.isInfoEnabled()) {
        printInfo("handleDetermineRequirementsTask - no deploy plan so waiting on dr task");
      }
      unhandledDetermineRequirementsTask = new WaitingTask(0, drTask);
      return;
    } else {
      // Clear out the unhandled. Don't want to process multiple times
      unhandledDetermineRequirementsTask = null;
    }

    
    if (logger.isInfoEnabled()) {
      printInfo("Add drTask with Deploy Plan: "+dp);
      if ((dp.detReqsTask != null) && (dp.detReqsTask != drTask)) {
        printInfo("  Different drTask!  "+drTask+" != "+dp.detReqsTask);
      }
    }
    dp.detReqsTask = drTask;
    // replace workflow
    Workflow wf = dp.expandedWorkflow;
    if (wf != null) {
      // remove existing workflow
      dp.expandedWorkflow = null;
      killWorkflow(wf);
    }
    // create tasks for the assets
    Enumeration eAssets = transAssetsPersonSub.elements();
    if (eAssets.hasMoreElements()) {
      expandDetermineRequirements(dp, drTask, eAssets);
    } else {
      if (logger.isInfoEnabled()) {
        printInfo("No assets to transport");
      }
    }
    eAssets = transAssetsEquipmentSub.elements();
    if (eAssets.hasMoreElements()) {
      expandDetermineRequirements(dp, drTask, eAssets);
    } else {
      if (logger.isInfoEnabled()) {
        printInfo("No assets to transport");
      }
    }
  }

  private void reprocessDetermineRequirementsTask(Task drTask) {
    if (delayReprocessMillis > 0) {
      // delay specified millis before re-adding task
      if (logger.isInfoEnabled()) {
        printInfo("  delay re-add drTask: "+drTask);
      }
      unhandledDetermineRequirementsTask = new WaitingTask(delayReprocessMillis,
                                                           drTask);
      wakeAfterRealTime(delayReprocessMillis);
    } else {
      // handle task immediately
      if (logger.isInfoEnabled()) {
        printInfo("  immediate re-add drTask: "+drTask);
      }
      handleDetermineRequirementsTask(drTask);
    }
  }

  /**
   * Predicate generators
   */


  /**
   * Self Organization predicate.
   **/
  protected static UnaryPredicate newSelfOrgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          return ((Organization)o).isSelf();
        }
        return false;
      }
    };
  }

  /**
   * OrgActivity of type "Deployment" predicate.
   **/
  protected static UnaryPredicate newOrgDeployActsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return 
          ((o instanceof OrgActivity) &&
            "Deployment".equals(((OrgActivity)o).getActivityType()));
      }
    };
  }

  /**
   * The predicate for our incoming tasks. We are looking for
   * DETERMINEREQUIREMENT tasks of type Asset where the asset type is
   * StrategicTransportation.
   **/
  protected static UnaryPredicate newDRTasksPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task t = (Task) o;
          if (Constants.Verb.DetermineRequirements.equals(t.getVerb())) {
            return (ExpanderHelper.isOfType(t, Constants.Preposition.OFTYPE,
                     "StrategicTransportation"));
          }
        }
        return false;
      }
    };
  }
        
  protected static UnaryPredicate newTransPersonPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        while (o instanceof AggregateAsset)
          o = ((AggregateAsset)o).getAsset();
        return (o instanceof Person);
      }
    };
  }
  protected static UnaryPredicate newTransEquipmentPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        while (o instanceof AggregateAsset)
          o = ((AggregateAsset)o).getAsset();
        return (o instanceof ClassVIIMajorEndItem);
      }
    };
  }
  /**
   * Pred for failed allocations of a expanded DetermineRequirements tasks we
   * published.  We use newDRTasksPred for testing the task.
   **/
  protected static UnaryPredicate newFailedDRAllocPred() {
    return new UnaryPredicate() {
      protected UnaryPredicate myDRTaskPred = newDRTasksPred();
      public boolean execute(Object o) {
        if (o instanceof Allocation) {
          AllocationResult ar = ((Allocation)o).getReportedResult();
          if ((ar != null) && (!(ar.isSuccess()))) {
            Task tFailed = ((Allocation)o).getTask();
            Workflow wf;
            if ((tFailed != null) &&
                !(tFailed instanceof MPTask) &&
                (wf = tFailed.getWorkflow()) != null // all these tasks are part of a WF
                ) {
              Task tParent = wf.getParentTask();
              return myDRTaskPred.execute(tParent);
            }
          }
        } 
        return false;
      }
    };
  }

  /**
   * Test if this object is an expansion of one of our tasks. We use
   * taskPred for the latter.
   **/
  protected static UnaryPredicate newExpansionsPred() {
    return new UnaryPredicate() {
      protected UnaryPredicate myTaskPred = newDRTasksPred();
      public boolean execute(Object o) {
        if (o instanceof Expansion) {
          return myTaskPred.execute(((Expansion) o).getTask());
        } 
        return false;
      }
    };
  }


  /**
   * Predicate to find a specific Oplan by UID
   **/
  protected static class OplanByUIDPred implements UnaryPredicate {
    UID oplanUID;

    OplanByUIDPred (UID uid) {
      oplanUID = uid;
    }
    public boolean execute(Object o) {
      if (o instanceof Oplan) {
	if (oplanUID.equals(((Oplan)o).getUID())) {
	  return true;
	}
      }
      return false;
    }
  }
    

  /**
   * Utility classes
   */


  /**
   * Class <code>DeployPlan</code>
   */
  protected static class DeployPlan {
    /**
     * These fields are used to fill out a determine requirements
     * task.  They are gathered from the oplan and this plugin's 
     * organization information.
     * <p>
     * The "adjustDurationDays" field is the time difference from "thruTime"
     * to initially try starting the deployment tasks.  It should be
     * negative, e.g.<br>
     *   startTime=10/1/99, endTime=10/31/99, adjustDurationDays=-7;<br>
     * would first try starting on 10/21/99.
     */
    public UID oplanId;
    public GeolocLocation fromLoc = null;
    public GeolocLocation fromPrepoLoc = null;
    public GeolocLocation toLoc = null;
    public Organization forOrg;
    public Date startTime;
    public Date prepoStartTime;
    public Date thruTime;
    public int thruRange = 1;
    public int adjustDurationDays;

    /**
     * State info once detReqs tasks and asset changes take place.
     * <p>
     * We sometimes need this info at times when I can't figure out how
     * to otherwise get it, so I'm squirreling it away in here.
     */
    public Task detReqsTask;
    public NewWorkflow expandedWorkflow;

    public DeployPlan() {}

    /**
     * Contructor.  Takes needed fields from parameters.
     * <p>
     * Caller should check this class with isValid() afterwards.
     * <p>
     * @param org Organization for the org's deployment
     * @param orgDeployAct OrgActivity of type "Deploy"
     * @param adjustDurationDays Time needed to do task
     */
    public DeployPlan(Organization org, 
		      OrgActivity orgAct, 
		      int adjustDurationDays,
		      GeolocLocation overrideFromLocation,
		      Date prepoStartDate) {
      if ((org == null) || 
          (orgAct == null)) {
        System.err.println("BAD DeployPlan parameter(s)! org = " + org + 
                             " orgAct = " + orgAct);
        return;
      }

      // get oplan id
      this.oplanId = orgAct.getUID();

      // get plan org
      this.forOrg = org;

      // get FROM geographic location
      //   this is taken from the MilitaryOrgPG
      org.cougaar.glm.ldm.asset.MilitaryOrgPG milPG = org.getMilitaryOrgPG();
      if (milPG != null) {
	this.fromLoc = (GeolocLocation)milPG.getHomeLocation();
      }
      // get TO geographic location
      this.toLoc = orgAct.getGeoLoc();

      // Use this for equipment
      this.fromPrepoLoc = overrideFromLocation;

      //  get organization activity information

      TimeSpan actTS = orgAct.getTimeSpan();
      if (actTS != null) {
        // do we want to fix dates to be after System time?
	// startDate will be null if OffsetDays days wasn't an command line parameter
        this.thruTime = actTS.getEndDate();
	this.startTime = actTS.getStartDate();

	if (prepoStartDate == null) {
	  // Use Deploy OrgActivity startDate instead
	  this.prepoStartTime = actTS.getStartDate();
	} else {
	  // Use OrgActivity startDate if the oplan C day + offsetDays is later than
	  // the Task thruDate.
	  if (prepoStartDate.compareTo(this.thruTime) < 0)
	    this.prepoStartTime = prepoStartDate;
	  else
	    this.prepoStartTime = actTS.getStartDate();
	}
      }
  
      // get adjustment days
      this.adjustDurationDays = adjustDurationDays;
    }

    /**
     * After constructor the caller should check isValid()
     * @return true if all necessary deployment fields are set
     */
    public boolean isValid() {
      return
        ((oplanId != null) &&
         ((fromLoc != null) &&
          (fromLoc.getGeolocCode() != null)) &&
         ((toLoc != null) &&
          (toLoc.getGeolocCode() != null)) &&
         (forOrg != null) &&
         (startTime != null) &&
         (thruTime != null));
    }

    /**
     * toString()
     * @return String representation of contents
     */
    public String toString() {
      String s = "Deployment Plan:";
      s += "\n  PlanId: "+oplanId;
      s += "\n  fromLoc: "+ ((fromLoc != null) ? fromLoc.getGeolocCode(): "?");
      s += "\n  fromPrepoLoc: "+ ((fromPrepoLoc != null) ? fromPrepoLoc.getGeolocCode(): "?");
      s += "\n  toLoc:   "+ ((toLoc != null) ? toLoc.getGeolocCode(): "?");
      s += "\n  for: "+getOrgID(forOrg);
      s += "\n  startTime: "+startTime;
      s += "\n  prepoStartTime: "+prepoStartTime;
      s += "\n  thruTime:  "+thruTime;
      s += "\n  adjustDurationDays: "+adjustDurationDays;
      s += "\n";
      return s;
    }

    public boolean equals(DeployPlan dp) {
      try {
        return 
          (oplanId.equals(dp.oplanId) &&
           (fromLoc == dp.fromLoc) &&
           (fromPrepoLoc == dp.fromPrepoLoc) &&
           (toLoc == dp.toLoc) &&
           (forOrg == dp.forOrg) &&
           startTime.equals(dp.startTime) &&
           prepoStartTime.equals(dp.prepoStartTime) &&
           thruTime.equals(dp.thruTime) &&
           (adjustDurationDays == dp.adjustDurationDays));
      } catch (NullPointerException ne) {
        // both "this" and "dp" should be "isValid()"!
        return false;
      }
    }
  }

  private static class WaitingTask {
    public long time;
    public Task task;

    public WaitingTask(long deltaMillis, Task xtask) {
      time = System.currentTimeMillis() + deltaMillis;
      task = xtask;
      // maybe need plan info in future?
    }

    public boolean isDue() {
      return (time <= System.currentTimeMillis());
    }

    public String toString() {
      long now = System.currentTimeMillis();
      return "  (+" + (time-now) + "ms, " + task.getUID().toString() + ")";
    }
  }


  public GeolocLocation getGeoLoc(String xmlfilename)  {
    Document doc = null;
    try {
      doc = getConfigFinder().parseXMLConfigFile(xmlfilename);
      if (doc == null) {
	printError(" XML Parser could not handle file " + xmlfilename);
	return null;
      }
    } catch (java.io.IOException ioex) {
      printError("geoloc xml error ");
      ioex.printStackTrace();
      return null;
    }

    Node node = doc.getDocumentElement();
    return locationParser.getLocation(getLDM(), node);
  }

  /** rely upon load-time introspection to set these services - don't worry about revokation. */
  public final void setLoggingService (LoggingService logger) { this.logger = logger; }

  LocationParser locationParser = new LocationParser();

  /**
   * Everybody needs a logger
   **/
  protected LoggingService logger;
}
