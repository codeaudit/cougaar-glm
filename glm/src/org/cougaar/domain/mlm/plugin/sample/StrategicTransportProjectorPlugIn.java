/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.domain.glm.ldm.Constants;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.ldm.oplan.TimeSpan;
import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.ExpanderHelper;

import org.cougaar.util.SingleElementEnumeration;
import org.cougaar.util.ShortDateFormat;
import org.cougaar.util.StringUtility;
import org.cougaar.core.society.UID;

import org.cougaar.util.UnaryPredicate;

/**
 * Class <code>StrategicTransportProjectorPlugIn</code> is a replacement
 * for <code>StrategicTranportProjectionExpanderPlugIn</code>.
 * <p>
 * This class subscribes to the single "Deploy" "DetermineRequirements" 
 * Task and expands it to "Transport" Tasks for all applicable assets.
 * <p>
 * Currently expects only one oplan, one "self" org activity, and one
 * "Deploy" "DetermineRequirements" task.
 * <p>
 * Debug information is now off by default.  See method <code>setDebug()</code>
 */
public class StrategicTransportProjectorPlugIn extends SimplePlugIn {
        
  /** Expand into an AssetGroup if true, else separate Tasks if false **/
  protected boolean createAssetGroups = true;

  /** delay re-adding DetermineRequirement Tasks by specified millis **/
  protected long delayReAddDetermineRequirementsMillis;

  /** Defaults optionally set by parameters. **/
  protected int defaultAdjustDurationDays;

  /** Self Organization Info **/
  protected IncrementalSubscription selfOrgsSub;
  protected Organization selfOrg;
  protected String selfOrgId = "XXXSelfOrgNotSetYet";

  /** Subscription to orgActivities for Deployment **/
  protected IncrementalSubscription orgDeployActsSub;

  /** Subscription to DetermineRequirement Tasks **/
  protected IncrementalSubscription drTasksSub;

  /** Subscription to Transportable Assets **/
  protected IncrementalSubscription transAssetsSub;

  /** Subscription to Failed Allocations **/
  protected IncrementalSubscription failedDRAllocsSub;

  /** List of determine requirements tasks waiting to be added **/
  protected TaskWaitingList waitingAddDetermineRequirementsTasks;

  /**
   * Subscribe.
   */
  protected void setupSubscriptions() {
    setDebug();
    if (DEBUG) {
      printDebug(getClass()+" setting up subscriptions");
    }

    setDefaults(getParameters().elements());

    selfOrgsSub = (IncrementalSubscription) subscribe(newSelfOrgPred());

    orgDeployActsSub = (IncrementalSubscription) 
      subscribe(newOrgDeployActsPred());

    drTasksSub = (IncrementalSubscription) 
      subscribe(newDRTasksPred());
                
    transAssetsSub = (IncrementalSubscription) 
      subscribe(newTransAssetsPred());

    failedDRAllocsSub = (IncrementalSubscription) 
      subscribe(newFailedDRAllocPred());
  
    waitingAddDetermineRequirementsTasks = new TaskWaitingList();
  }        

  /**
   * Execute.
   */
  protected void execute() {
                  
    if (selfOrgsSub.hasChanged()) {
      //if (DEBUG) {
      //  printDebug("selfOrgs hasChanged");
      //}
      watchAddedSelfOrgs(selfOrgsSub.getAddedList());
    }

    if (orgDeployActsSub.hasChanged()) {
      //if (DEBUG) {
      //  printDebug("orgDeployActs hasChanged");
      //}
      watchAddedOrgDeployActs(orgDeployActsSub.getAddedList());
      watchChangedOrgDeployActs(orgDeployActsSub.getChangedList());
      watchRemovedOrgDeployActs(orgDeployActsSub.getRemovedList());
    }

    Link l = waitingAddDetermineRequirementsTasks.dueLinks();
    if (l != null) {
      //if (DEBUG) {
      //  printDebug("DetermineReqs Tasks due");
      //}
      watchDueDetermineRequirementsTasks(l);
    }

    if (drTasksSub.hasChanged()) {
      //if (DEBUG) {
      //  printDebug("DetermineReqs Tasks hasChanged");
      //}
      watchAddedDetermineRequirementsTasks(
          drTasksSub.getAddedList());
      watchChangedDetermineRequirementsTasks(
          drTasksSub.getChangedList());
      watchRemovedDetermineRequirementsTasks(
          drTasksSub.getRemovedList());
    }
    
    if (transAssetsSub.hasChanged()) {
      //if (DEBUG) {
      //  printDebug("Transportable Assets hasChanged");
      //}
      watchAddedTransportableAssets(transAssetsSub.getAddedList());
      watchChangedTransportableAssets(transAssetsSub.getChangedList());
      watchRemovedTransportableAssets(transAssetsSub.getRemovedList());
    }

    if (failedDRAllocsSub.hasChanged()) {
      //if (DEBUG) {
      //  printDebug("Failed DR Subtask Allocation hasChanged");
      //}
      watchFailedDispositions(failedDRAllocsSub.getChangedList());
    }

  }

  /**
   * Only one plan with one "Deployment" DetermineRequirements Task expected!
   */
  DeployPlan singleDeployPlan = null;
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
      if (DEBUG) {
        printDebug("Found Self: "+this.selfOrgId);
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

  protected void watchDueDetermineRequirementsTasks(Link lTasks) {
    if (lTasks == null) {
      // no tasks to add
      return;
    }
    if (lTasks.next != null) {
      // should be only one determine requirements task
      printError("Expecting only one DetermineReqs Task! ignoring the others");
    }
    watchAddedDetermineRequirementsTask(lTasks.task);
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
      watchAddedDetermineRequirementsTask(drTask);
    }
  }

  protected void watchChangedDetermineRequirementsTasks(Enumeration eTasks) {
    Task drTask = findSingleDetermineRequirementsTask(eTasks);
    if (drTask != null) {
      watchChangedDetermineRequirementsTask(drTask);
    }
  }

  protected void watchRemovedDetermineRequirementsTasks(Enumeration eTasks) {
    Task drTask = findSingleDetermineRequirementsTask(eTasks);
    if (drTask != null) {
      watchRemovedDetermineRequirementsTask(drTask);
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
    // We don't know how long this task will take, so we
    // use the default task adjustment duration!
    int adjustDurationDays = defaultAdjustDurationDays;
    DeployPlan newDP = 
      new DeployPlan(selfOrg, selfOrgAct, adjustDurationDays);
    if (DEBUG) {
      printDebug(newDP.toString());
    }
    if (!newDP.isValid()) {
      if (newDP.fromLoc == null)
        printError("This Organization lacks a MilitaryPG HomeLocation!");
      printError("The DeployPlan lacks needed information and is ignored!");
    } else {
      DeployPlan oldDP = getDeployPlan();
      if (oldDP != null) {
        if (DEBUG) {
          printDebug("Replace Deployment Plan!\n"+
                     "OLD:\n"+oldDP+"\n"+
                     "NEW:\n"+newDP);
        }
        // do something special?
      }
      setDeployPlan(newDP);
    }
  }

  /**
   * Little utility to remove a workflow and expansion
   */
  protected void killWorkflow(Workflow wf) {
    if (DEBUG) {
      printDebug("  kill workflow: "+wf);
    }
    Enumeration subtasksE = wf.getTasks();
    while (subtasksE.hasMoreElements()) {
      Task subt = (Task)subtasksE.nextElement();
      if (DEBUG) {
        printDebug("    remove task: "+subt);
      }
      publishRemove(subt);
    }
    // okay to get parent
    Task ptask = wf.getParentTask();
    if (ptask != null) {
      PlanElement ppe = ptask.getPlanElement();
      if (ppe != null) {
        if (DEBUG) {
          printDebug("  remove planElement: "+ppe);
        }
        publishRemove(ppe);
      } else {
        if (DEBUG) {
          printDebug("   no wf parent planElement");
        }
      }
    } else {
      if (DEBUG) {
        printDebug("   no wf parent");
      }
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
    if (DEBUG) {
      printDebug("Changed orgActivity!: "+selfOrgAct+"\nbegin");
    }
    DeployPlan oldDP = getDeployPlan();
    setDeployPlan(null);
    if (DEBUG) {
      printDebug("  re-add org activity");
    }
    watchAddedSelfOrgDeployAct(selfOrgAct);
    if (oldDP == null) {
      // didn't replace anything
      if (DEBUG) {
        printDebug("done");
      }
      return;
    }
    DeployPlan newDP = getDeployPlan();
    if (DEBUG) {
      printDebug("  old dp: "+oldDP+"\n  new dp: "+newDP);
    }
    Task drTask = oldDP.detReqsTask;
    if (drTask != null) {
      if (newDP != null) {
        // kill the old workflow
        if (oldDP.expandedWorkflow != null) {
          killWorkflow(oldDP.expandedWorkflow);
          oldDP.expandedWorkflow = null;
        }
      }
      if (delayReAddDetermineRequirementsMillis > 0) {
        // delay specified millis before re-adding task
        if (DEBUG) {
          printDebug("  delay re-add drTask: "+drTask);
        }
        waitingAddDetermineRequirementsTasks.addLink(
          delayReAddDetermineRequirementsMillis,
          drTask);
        wakeAfterRealTime(delayReAddDetermineRequirementsMillis);
      } else {
        // re-add task immediately
        if (DEBUG) {
          printDebug("  immediate re-add drTask: "+drTask);
        }
        watchAddedDetermineRequirementsTask(drTask);
      }
    }
    if (DEBUG) {
      printDebug("done");
    }
  }

  /**
   * Handle removed selfOrgAct.  We'll assume that the tasks are 
   * killed off separately (e.g. by removing the DRTask), so all that
   * is needed here is to remove the DeployPlan.
   */
  protected void watchRemovedSelfOrgDeployAct(OrgActivity selfOrgAct) {
    // throw away the deploy plan
    DeployPlan oldDP = getDeployPlan();
    if (oldDP != null) {
      if (DEBUG) {
        printDebug("Remove Deployment Plan!\nOLD:\n"+oldDP);
      }
      setDeployPlan(null);
      // do something special?
    }
  }

  /**
   * Handle added DetermineRequirements Task.
   * <p>
   * Carefully check for replacing prior drTasks.
   */
  protected void watchAddedDetermineRequirementsTask(Task drTask) {
    DeployPlan dp = getDeployPlan();
    if (dp == null) {
      // no deploy plan
      return;
    }
    if (DEBUG) {
      printDebug("Add drTask with Deploy Plan: "+dp);
      if ((dp.detReqsTask != null) && (dp.detReqsTask != drTask)) {
        printDebug("  Different drTask!  "+drTask+" != "+dp.detReqsTask);
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
    Enumeration eAssets = transAssetsSub.elements();
    if (eAssets.hasMoreElements()) {
      expandDetermineRequirements(dp, drTask, eAssets);
    } else {
      if (DEBUG) {
        printDebug("No assets to transport");
      }
    }
  }

  /**
   * Handle changed DetermineRequirements Task.  This is also
   * used when an Org Activity has changed.
   * <p>
   * For now we do something harsh:  Re-add the DRTask on top of the 
   * existing one.  This should remove any existing workflow and
   * subtasks, then re-expand the DRTask to a new workflow and
   * subtasks.
   */
  protected void watchChangedDetermineRequirementsTask(Task drTask) {
    watchAddedDetermineRequirementsTask(drTask);
  }

  /**
   * Handle removed DetermineRequirements Task.
   * <p>
   * Removed task should remove it's subtasks, so we only need
   * to clean out it's DeployPlan.
   */
  protected void watchRemovedDetermineRequirementsTask(Task drTask) {
    if (DEBUG) {
      printDebug("Remove DetReqs Task");
    }
    DeployPlan dp = getDeployPlan();
    if (dp == null) {
      // never added?
      return;
    }
    dp.detReqsTask = null;
    dp.expandedWorkflow = null;
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
      if (DEBUG) {
        printDebug("Assets added, waiting for OrgActivity");
      }
      return;
    }
    Task drTask = dp.detReqsTask;
    if (drTask == null) {
      // no determine requirements task yet
      if (DEBUG) {
        printDebug("Assets added, waiting for OrgActivity");
      }
      return;
    }
    if (DEBUG) {
      printDebug("Adding Assets with DP: "+ dp);
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
    if (DEBUG) {
      printDebug("Changed assets!");
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
    if (DEBUG) {
      printDebug("Handle Asset removal");
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
      if (DEBUG) {
        printDebug("Look for removed asset: "+assetForTrans);
      }
      workflowRemoveTasks.clear();
      Enumeration subTE = wf.getTasks();
      while (subTE.hasMoreElements()) {
        Task subT = (Task)subTE.nextElement();
        // FIXME! what if asset group!
        if (subT.getDirectObject() == assetForTrans) {
          if (DEBUG) {
            printDebug("  Will remove Task: "+subT+
                       " with DO: "+assetForTrans);
          }
          workflowRemoveTasks.add(subT);
          publishRemoveTasks.add(subT);
        }
      }
      for (int i = workflowRemoveTasks.size(); --i >= 0; ) {
        Task removeWFT = (Task)workflowRemoveTasks.elementAt(i);
        if (DEBUG) {
          printDebug("Remove from workflow Task: "+removeWFT);
        }
        wf.removeTask(removeWFT);
      }
    }
    for (int i = publishRemoveTasks.size(); --i >= 0; ) {
      Task removeT = (Task)publishRemoveTasks.elementAt(i);
      if (DEBUG) {
        printDebug("publishRemove Task: "+removeT);
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
      if (DEBUG) {
        printDebug("Failed Allocation: "+failedAlloc.toString()+
                   " of Task: "+tFailed.toString());
      }
      if (!(updateFailedTaskPreferences(tFailed,
              adjustDurationDays, earliestDate))) {
        printError("Unable to fix a failed allocation: "+failedAlloc);
      }
    } while (eFailedAllocs.hasMoreElements());
  }
  
  /**
   * Protected methods other than subcription watchers
   */

  protected boolean DEBUG = false;
  protected String debugPrefix;
  protected String errorPrefix;
  /** 
   * Debug is set in this order:<br>
   * <ol>
   *   <li> if parameter "debug=true" is set</li>
   *   <li> if system property "THISCLASS.debug=true" is set</li>
   *   <li> default is false</li>
   * </ol>
   */
  protected boolean setDebug() {
    // set debug prefix string
    String clusterName = getClusterIdentifier().toString();
    if (clusterName.startsWith("<"))
      clusterName = clusterName.substring(1);
    if (clusterName.endsWith(">"))
      clusterName = clusterName.substring(0,clusterName.length()-1);
    debugPrefix = "DEBUG "+clusterName+".StratTrans ";
    errorPrefix = "ERROR "+clusterName+".StratTrans ";
    // first try plugin parameters
    Enumeration eParams = getParameters().elements();
    while (eParams.hasMoreElements()) {
      String sParam = (String)eParams.nextElement();
      if (sParam.startsWith("debug=")) {
        DEBUG = "true".equalsIgnoreCase(
                  sParam.substring("debug=".length()));
        return DEBUG;
      }
    }
    // next try system property
    String sysProp = System.getProperty(this.getClass().getName()+".debug");
    if (sysProp != null) {
      DEBUG = "true".equalsIgnoreCase(sysProp);
      return DEBUG;
    }
    // default
    DEBUG = false;
    return DEBUG;
  }
  protected void setDebug(boolean b) {DEBUG=b;}

  /** 
   * Debug printer which only prints if DEBUG boolean is true.
   */
  protected final void printDebug(String s) {
    System.out.println(
        StringUtility.prefixLines(debugPrefix, s));
  }

  protected final void printError(String s) {
    System.out.println(
        StringUtility.prefixLines(errorPrefix, s));
  }

  protected void setDefaults(Enumeration eParams) {
    delayReAddDetermineRequirementsMillis = 0;
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
            delayReAddDetermineRequirementsMillis = Long.parseLong(val);
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
        } else {
          printError("Unknown parameter: "+name+"="+val);
        }
      }
    }
  }

  protected static String getOrgID(Organization org) {
    try {
      // FOR NOW:
      return org.getClusterPG().getClusterIdentifier().toString();
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
    if (DEBUG) {
      printDebug("Expand task: "+drTask+" with DP: "+ dp);
    }

    // get first and second assets from enumeration
    Asset firstAssetForTransport = null;
    Asset secondAssetForTransport = null;
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
    from.setIndirectObject(dp.fromLoc);
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
           Class.forName( "org.cougaar.domain.planning.ldm.asset.AbstractAsset" ),
           "StrategicTransportation" );
        strans = (AbstractAsset)ldmfactory.createInstance( strans_proto );
      } catch (Exception exc) {
        printError("Unable to create abstract strategictransport\n"+exc);
      }
    }
    pp.setIndirectObject(strans);
    prepphrases.addElement(pp);

    // PREFERENCES
    Vector prefs = new Vector();

    //   START DATE  (startTime)
    AspectValue startAV = 
      new AspectValue(AspectType.START_TIME, dp.startTime.getTime());
    ScoringFunction startSF = 
     ScoringFunction.createNearOrAbove(startAV, 0);
    Preference startPref = 
     theLDMF.newPreference(AspectType.START_TIME, startSF);
    prefs.addElement(startPref);

    /*
    //   END DATE    ((thruTime-thruRange) <= thruTime <= (thruTime+thruRange)
    Date earlyEndDate = 
      ShortDateFormat.adjustDate(dp.thruTime, 0, -dp.thruRange);
    if (earlyEndDate.before(dp.startTime))
      earlyEndDate = dp.startTime;
    Date lateEndDate = 
      ShortDateFormat.adjustDate(dp.thruTime, 0, dp.thruRange);
    AspectValue earlyEndAV = 
      new AspectValue(AspectType.END_TIME, earlyEndDate.getTime());
    AspectValue bestEndAV = 
      new AspectValue(AspectType.END_TIME, dp.thruTime.getTime());
    AspectValue lateEndAV = 
      new AspectValue(AspectType.END_TIME, lateEndDate.getTime());
    */

    //   END DATE    (RDD - 5*range) <= RDD - range <= RDD
    Date lateEndDate = dp.thruTime;
    Date bestEndDate = ShortDateFormat.adjustDate(lateEndDate, 0, -dp.thruRange);
    Date earlyEndDate = ShortDateFormat.adjustDate(lateEndDate, 0, -5*dp.thruRange);

    if (earlyEndDate.before(dp.startTime))
      earlyEndDate = dp.startTime;

    AspectValue earlyEndAV = 
      new AspectValue(AspectType.END_TIME, earlyEndDate.getTime());
    AspectValue bestEndAV = 
      new AspectValue(AspectType.END_TIME, bestEndDate.getTime());
    AspectValue lateEndAV = 
      new AspectValue(AspectType.END_TIME, lateEndDate.getTime());

    ScoringFunction endSF = 
      ScoringFunction.createStrictlyBetweenWithBestValues(
          earlyEndAV, bestEndAV, lateEndAV);
    Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
    prefs.addElement(endPref);

    // Get Workflow
    NewWorkflow wf = dp.expandedWorkflow;
    boolean newWf = (wf == null);
    if (newWf) {
      if (DEBUG) {
        printDebug("New workflow");
      }
      wf = theLDMF.newWorkflow();
      dp.expandedWorkflow = wf;

      wf.setIsPropagatingToSubtasks(true);
      wf.setParentTask(drTask);
    } else {
      if (DEBUG) {
        printDebug("Existing workflow");
      }
      Enumeration eT = wf.getTasks();
      while (eT.hasMoreElements()) {
        Task ewft = (Task)eT.nextElement();
        if (DEBUG) {
          printDebug(" task: "+ewft.getVerb()+
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
      NewTask subtask = theLDMF.newTask();
      subtask.setParentTask( drTask );
      subtask.setDirectObject( assetForTransport );
      subtask.setPrepositionalPhrases( prepphrases.elements() );
      subtask.setVerb( Constants.Verb.Transport );
      subtask.setPlan( drTask.getPlan() );
      subtask.setPreferences( prefs.elements() );
      subtask.setSource( getClusterIdentifier() );

      // add to workflow
      wf.addTask(subtask);
      subtask.setWorkflow(wf);

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
      if (DEBUG) {
        printDebug("Create expansion");
      }
      PlanElement pe = 
        theLDMF.createExpansion(
          drTask.getPlan(),
          drTask,
          wf,
          null);
      publishAdd(pe);
    }

    if (DEBUG) {
      printDebug("Published new DetermineReqs Task Expansion");
    }
  }

  /**
   * If the reported allocation result has been successfully calculated
   * for the workflow set the estimated equal to the reported to send a
   * notification back up.
   * <p>
   * This is currently not used. The NotificationLP is handling propagation 
   * of AllocationResults.
   */
  protected void updateAllocationResult(PlanElement cpe) {
    if (cpe.getReportedResult() != null) {
      // compare allocationresult objects.
      // if they are not equal, re-set the estimated result
      // for now ignore whether the composition of the results are the same.
      AllocationResult reportedresult = cpe.getReportedResult();
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      if ((estimatedresult == null) || 
         (!(estimatedresult.equals(reportedresult)))) {
        cpe.setEstimatedResult(reportedresult);
        publishChange(cpe);
      }
    }
  }

  /**
   * If we get a FailedDisposition (AllocationResult.isSuccess() == false),
   * then we want to change our Preferences a bit and try again...
   * <p>
   * We do this by adjusting the start and end dates back, making sure we
   * don't slide them to before the earliest allowable date.
   * <p>
   * N.B. if this PlugIn was running in a more complex society, we may want to
   * incorporate some thread-safe code here...
   * <p>
   * @param t subtask of determineRequirement's Task
   * @param adjustDurationDays number of days to slide start/end down
   * @param earliestDate Date that neither start nor stop time can be before
   * @return preferences updated
   */
  protected boolean updateFailedTaskPreferences(
      NewTask t, int adjustDurationDays, Date earliestDate) {
    if (DEBUG) {
      printDebug("Adjust task begin/end dates");
    }
    AspectValue beginAspect;
    Date beginDate;
    try {
      // get begin date
      Preference beginPref = t.getPreference(AspectType.START_TIME);
      beginAspect =
        beginPref.getScoringFunction().getBest().getAspectValue();
      beginDate = new Date(beginAspect.longValue());
      if (DEBUG) {
        printDebug("  Start: "+ beginDate);
      }
    } catch (Exception e) {
      printError("Unable to get failed task begin date: "+e.getMessage());
      return false;
    }
    AspectValue endEarliestAspect;
    AspectValue endBestAspect;
    AspectValue endLatestAspect;
    Date oldEndEarliestDate;
    Date oldEndBestDate;
    Date oldEndLatestDate;
    try {
      // get end dates (earliest, best, and latest)
      Preference endPref = t.getPreference(AspectType.END_TIME);
      ScoringFunction endSF = endPref.getScoringFunction();
      Enumeration endVRs = endSF.getValidRanges(null,null);
      AspectScoreRange endASR = (AspectScoreRange)endVRs.nextElement();
      endEarliestAspect = 
        endASR.getRangeStartPoint().getAspectValue();
      endBestAspect =
        endSF.getBest().getAspectValue();
      endLatestAspect =
        endASR.getRangeEndPoint().getAspectValue();
      oldEndEarliestDate = new Date(endEarliestAspect.longValue());
      oldEndBestDate = new Date(endBestAspect.longValue());
      oldEndLatestDate = new Date(endLatestAspect.longValue());
      if (DEBUG) {
        printDebug("  Old End:\n    "+oldEndEarliestDate+" <= "+
                   oldEndBestDate+" <= "+oldEndLatestDate);
      }
    } catch (Exception e) {
      printError("Unable to get failed task end dates: "+e.getMessage());
      return false;
    }
    try {
      // slide end dates
      Date newEndEarliestDate =
        ShortDateFormat.adjustDate(oldEndEarliestDate, 0, adjustDurationDays);
      Date newEndBestDate = 
        ShortDateFormat.adjustDate(oldEndBestDate, 0, adjustDurationDays);
      Date newEndLatestDate = 
        ShortDateFormat.adjustDate(oldEndLatestDate, 0, adjustDurationDays);
      if (DEBUG) {
        printDebug("  New End:\n    "+newEndEarliestDate+" <= "+
                   newEndBestDate+" <= "+newEndLatestDate);
      }
      // check dates
      if (!beginDate.before(newEndEarliestDate)) {
        printError("Refuse to adjust task End Date to before Start Date: "+
                   beginDate);
        return false;
      }
      if ((earliestDate != null) &&
          !earliestDate.before(newEndEarliestDate)) {
        printError("Refuse to adjust task Dates to before: "+earliestDate);
        return false;
      }
      // set the new end dates
      endEarliestAspect.setValue(newEndEarliestDate.getTime());
      endBestAspect.setValue(newEndBestDate.getTime());
      endLatestAspect.setValue(newEndLatestDate.getTime());
      publishChange((Task) t);
    } catch (Exception e) {
      printError("Unable to adjust failed task end dates: "+e.getMessage());
      return false;
    }
    return true;
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
        
  /**
   * This predicate selects transportable assets:<br>
   * <ul>
   *   <li>person</li>
   *   <li>class vii major end item</li>
   *</ul>
   *<p>
   * These are the assets that we will transport.
   **/
  protected static UnaryPredicate newTransAssetsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        while (o instanceof AggregateAsset)
          o = ((AggregateAsset)o).getAsset();
        return ((o instanceof Person) || 
                (o instanceof ClassVIIMajorEndItem));
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
    public GeolocLocation fromLoc;
    public GeolocLocation toLoc;
    public Organization forOrg;
    public Date startTime;
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
    public DeployPlan(
        Organization org, 
        OrgActivity orgAct, 
        int adjustDurationDays) {
      if ((org == null) || 
          (orgAct == null)) {
        //if (DEBUG) {
        //  printDebug("BAD DeployPlan parameter(s)!");
        //}
        return;
      }

      // get oplan id
      this.oplanId = orgAct.getUID();

      // get plan org
      this.forOrg = org;

      // get FROM geographic location
      //   this is taken from the MilitaryOrgPG
      org.cougaar.domain.glm.ldm.asset.MilitaryOrgPG milPG = org.getMilitaryOrgPG();
      if (milPG != null) {
        GeolocLocation geoloc_location = 
          (GeolocLocation)milPG.getHomeLocation();
        if (geoloc_location != null)
          this.fromLoc = (GeolocLocation)milPG.getHomeLocation();
      }
      /*
      //   this is taken from the AssignmentPG
      org.cougaar.domain.glm.ldm.asset.AssignmentPG orgAPG = org.getAssignmentPG();
      if (orgAPG != null) {
        org.cougaar.domain.glm.ldm.asset.Facility orgF = orgAPG.getHomeStation();
        if (orgF instanceof org.cougaar.domain.glm.ldm.asset.TransportationNode) {
          org.cougaar.domain.glm.ldm.asset.PositionPG orgPPG = 
            ((org.cougaar.domain.glm.ldm.asset.TransportationNode)orgF).getPositionPG();
          if (orgPPG != null) {
            org.cougaar.domain.glm.ldm.plan.Position orgP = orgPPG.getPosition();
            if (orgP instanceof GeolocLocation)
              this.fromLoc = (GeolocLocation)orgP;
          } 
        }
      }
      */

      // get TO geographic location
      this.toLoc = orgAct.getGeoLoc();
      
      //  get organization activity information
      TimeSpan actTS = orgAct.getTimeSpan();
      if (actTS != null) {
        // do we want to fix dates to be after System time?
        this.startTime = actTS.getStartDate();
        this.thruTime = actTS.getThruDate();
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
      s += "\n  toLoc:   "+ ((toLoc != null) ? toLoc.getGeolocCode(): "?");
      s += "\n  for: "+getOrgID(forOrg);
      s += "\n  startTime: "+startTime;
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
           (toLoc == dp.toLoc) &&
           (forOrg == dp.forOrg) &&
           startTime.equals(dp.startTime) &&
           thruTime.equals(dp.thruTime) &&
           (adjustDurationDays == dp.adjustDurationDays));
      } catch (NullPointerException ne) {
        // both "this" and "dp" should be "isValid()"!
        return false;
      }
    }
  }

 
  /**
   * Maintain sorted list of {time, tasks}.
   * <p><pre>
   * Lots of different ways to do this:
   *   1) java.util.LinkedList unsorted, traverse when taking "dueLinks()"
   *   2) java.util.LinkedList always sorted
   *   3) java.util.TreeMap
   *   4) write our own that maintains a sorted list.
   * I had code sitting around for (4)...
   * </pre>
   */
  protected static class TaskWaitingList {
    Link head;
    public TaskWaitingList() {
    }
    /**
     * Add link.
     */
    public void addLink(long deltaTimeInFuture, Task task) {
      long newTime = System.currentTimeMillis();
      if (deltaTimeInFuture > 0) {
        newTime += deltaTimeInFuture;
      }
      Link newLink = new Link(newTime, task);
      if (head == null) {
        // new list
        head = newLink;
        return;
      }
      if (head.time >= newTime) {
        // new head
        Link l = newLink;
        l.next = head;
        head = l;
        return;
      }
      Link prevl = null;
      Link currl = head;
      while (true) {
        prevl = currl;
        currl = currl.next;
        if (currl == null) {
          // new tail
          prevl.next = newLink;
          return;
        }
        if (currl.time >= newTime) {
          // insert
          Link newl = newLink;
          prevl.next = newl;
          newl.next = currl;
          return;
        }
      }
    }

    /**
     * Take links with time &lteq; currentTime.
     */
    public Link dueLinks() {
      long upToTime = System.currentTimeMillis();
      if ((head == null) || 
          (head.time > upToTime)) {
        // no links
        return null;
      }
      Link prevl = null;
      Link currl = head;
      while (true) {
        prevl = currl;
        currl = currl.next;
        if (currl == null) {
          // entire list
          Link l = head;
          head = null;
          return l;
        }
        if (currl.time >= upToTime) {
          // up to l
          Link l = head;
          head = currl;
          prevl.next = null;
          return l;
        }
      }
    }

    public String toString() {
      return Link.toString(head);
    }
  }

  /** Used by WaitingTaskList **/
  protected static class Link {
    public long time;
    public Task task;
    public Link next;

    public Link(long xtime, Task xtask) {
      time = xtime;
      task = xtask;
      // maybe need plan info in future?
    }

    public String toString() {
      return toString(this);
    }

    public static String toString(Link l) {
      long now = System.currentTimeMillis();
      String s = "Links {\n";
      for ( ; l != null; l = l.next) {
        s += "  (+";
        s += (l.time-now);
        s += "ms, ";
        if (l.task != null) {
          s += l.task.getUID().getUID();
        }
        s += ")\n";
      }
      s += "}";
      return s;
    }
  }
 
}
