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

package org.cougaar.lib.gss.plugins;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.util.ConfigFileFinder;

import org.cougaar.lib.callback.UTILAssetCallback;

import org.cougaar.lib.plugin.UTILEntityResolver;
import org.cougaar.lib.gss.GSScheduler;
import org.cougaar.lib.gss.GSSchedulerResult;
import org.cougaar.lib.gss.GSSpecsHandler;
import org.cougaar.lib.gss.GSTaskGroup;
import org.cougaar.lib.param.ParamException;

import org.cougaar.lib.util.UTILAsset;
import org.cougaar.lib.util.UTILPrepPhrase;
import org.cougaar.lib.util.UTILPluginException;
import org.cougaar.lib.util.UTILRuntimeException;
import org.cougaar.lib.util.UIDHashtable;

import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.lib.filter.UTILBufferingPluginAdapter;
import org.cougaar.lib.filter.UTILBufferingThread;
import org.cougaar.lib.filter.UTILTimeoutBufferingThread;

/**
 * Base class for GSS Plugin implementations.<BR>
 *
 * Abstract b/c does not implement:
 * <UL>
 * <LI> makePlanElement
 * <LI> createThreadCallback
 * <LI> addAssetList
 * <LI> freezeTaskGroupForPublish
 * </UL>
 * <B>Note:</B> If you implement freezeTaskGroupForPublish to return true,
 * then you must either:
 * <UL>
 * <LI>Include WITH and WITHGROUP preps in your children, and report a
 * AQ_STATUS = AQ_SUCCESS Auxiliary Query when the frozen tasks can be removed or:
 * <LI>Override removeFrozenTasks
 * </UL>
 */
public abstract class UTILGSSBufferingPluginAdapter 
  extends UTILBufferingPluginAdapter implements UTILGSSBufferingPlugin {
  protected static boolean MAKE_PE_ONLY_IF_FULL = true;
  protected static boolean MAKE_PE_EVEN_WHEN_NOT_FULL = false;

  //Class Constants:
  //////////////////

  /**Keeps track of the GSTaskGroups that have been assigned to frozen
     * tasks by assigning each GSTaskGroup a UID.
     * This UID is placed on the WITHGROUP preposition, and then used
     * in RemoveFrozenTasks, where it is used to unfreeze the task, and
     * is removed from the GSTaskGroupsByUID container.
     **/
  public UIDHashtable GSTaskGroupsByUID = new UIDHashtable();

  /**Status Auxiliary Query Constant*/
  //NOTE: THIS SHOULD BE MOVED TO ALPINE CODE: GET A REAL UNIQUE ID:
  public static final int AQ_STATUS = AuxiliaryQueryType.FAILURE_REASON;
  /**Success Auxiliary Query Constant*/
  public static final String AQ_SUCCESS = "Success";

  /** 
   * A preposition used to keep track of the GSTaskGroup used to make
   * an allocation so that it may be removed from the SchedulerResults' 
   * storage later
   **/
  public static final String WITHGROUP = "WITHGROUP";
  public static final String WITH = "With";
  //Horrible hack above

  /**
   * set up local variables before interestingTask is called.
   **/
  public void preFilterSetup() {
    String gssxmlfile = getXmlFile(); 

    try {
      if (myExtraOutput)
	System.out.println (getName () + " reading from " + 
			    gssxmlfile + " gss xml file");

      scheduler = GSScheduler.parseSpecs (gssxmlfile,
					  new UTILEntityResolver (),
					  getSpecsHandler());
      schedulerResult = new GSSchedulerResult (scheduler);
    } catch (Exception e) {
      System.err.println ("Received error parsing file " + gssxmlfile +
			  e.getMessage());
      e.printStackTrace();
    }

    super.preFilterSetup();
  }

  protected GSSchedulerResult getSchedulerResult () { return schedulerResult; }
  protected GSScheduler       getScheduler       () { return scheduler; }
  public GSSpecsHandler getSpecsHandler() { return new GSSpecsHandler(); }
  /**
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * Override and call super to add new filters, or override 
   * createXXXCallback to change callback behaviour.
   *
   * By default adds asset callback after creating it.
   *
   * @see #createAssetCallback
   */
  public void setupFilters () {
    super.setupFilters ();

    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for generic assets...");

    addFilter (myAssetCallback    = createAssetCallback    ());
  }

  public void localSetup(){
    super.localSetup();

    try {debugGSS = getMyParams().getBooleanParam("debugGSS");}
    catch (Exception e) {debugGSS = false;}
    scheduler.setDebug (debugGSS);

    boolean printUsedCapacities;
    try{ printUsedCapacities = getMyParams().getBooleanParam("PrintUsedCapacities");}
    catch(Exception e){printUsedCapacities = false;};
    scheduler.setPrintUsedCapacities(printUsedCapacities);
  }

  /**
   * get the file containing the gss specs
   *
   * return relative path of env file with which to start the
   * GSS.
   * @return relative path to gss parameters
   */
  protected String getXmlFile () {
    String envFile  = null;
    try {
	envFile = getMyParams().getStringParam ("gssFile");
	//	System.out.println ("envFile = " + envFile);
    } 
    catch (ParamException pe) {
      String e = ("UTILGSSBufferingPluginAdapter.getGSSXmlFile - requires one parameter: " +
		  "gssFile={String}gssfilename  in the " +
		  myClusterName + ".ini file\n" +
		  "Exception : " + pe.getMessage ());
      throw new UTILRuntimeException(e);
    }
    catch (Exception e) {
      throw new UTILRuntimeException (".getGSSXmlFile - Couldn't find file : " + envFile + 
				     "\nException was " + e);
    }
    return envFile;
  }


  /**
   * The listening buffering thread communicates with the plugin
   * across the UTILTimeoutBufferingPlugin interface.
   *
   * This plugin is NOT a workflow listener.
   *
   * @return UTILTimeoutBufferingThread with this as the BufferingPlugin
   * @see org.cougaar.lib.filter.UTILTimeoutBufferingPlugin
   * @see org.cougaar.lib.filter.UTILTimeoutBufferingThread
   * @see org.cougaar.lib.filter.UTILListeningBufferingThread
   */
  protected UTILBufferingThread createBufferingThread () {
    return new 
      UTILTimeoutBufferingThread (this, myExtraOutput, myExtraExtraOutput);
  }

  /** any tasks left over? */
  public boolean anyTasksLeft () {
    return schedulerResult.hasAnyLeftoverTasks ();
  }

  /**
   * Implemented for UTILGSSBufferingPlugin interface
   *
   * @see org.cougaar.lib.gss.plugins.UTILGSSBufferingPlugin
   */
  public boolean interestingTask(Task task){
    boolean interested = scheduler.interestingTask (task);

    if (myExtraExtraOutput)
      System.out.println (getName () + ".interestingTask - finds task " +task.getUID () + 
			  (interested ? " interesting " : " NOT interesting"));
    return interested;
  }

  /******************************************************************************/
  /************************ Asset handling **************************************/
  /******************************************************************************/

  /**
   * Implemented to support UTILAssetListener 
   */
  protected UTILAssetCallback getAssetCallback    () { return myAssetCallback; }

  /**
   * Implemented to support UTILAssetListener  
   * Generic buffering plugin is only interested in all assets...use the GSS.xml 
   * to get more specific filtering
   */
  protected UTILAssetCallback createAssetCallback () { 
    return new UTILAssetCallback  (this); 
  } 

  /** 
   * Utility method for finding all resource assets. 
   *
   * In general, it would be better if plugins could use more
   * specific filters and so this call would return a restricted set of
   * assets. 
   *
   * @return Enumeration of ALL assets found in container
   */
  protected final Iterator getAssets() {
    Collection assets = 
      getAssetCallback().getSubscription ().getCollection();

    if (assets.size() != 0) {
      return assets.iterator();
    }
    return null;
  }

  /**
   * Implemented for UTILAssetListener
   *
   * OVERRIDE to see which assets you
   * think are interesting
   * @param a asset to check for notification
   * @return boolean true if asset is interesting
   */
  public boolean interestingAsset(Asset a) {
    return true;
  }

  /**
   * Implemented for UTILAssetListener
   * Give new assets to scheduler
   *
   * @param newAssets new assets found in the container
   */
  public void handleNewAssets(Enumeration newAssets) {
    List newStuff = new ArrayList ();

    while (newAssets.hasMoreElements ()) {
      Asset asset = (Asset) newAssets.nextElement();
      newStuff.add (asset);

      if (myExtraOutput)
	System.out.println (getName () + " : Got new asset " + asset);
//       if (myExtraExtraOutput && (asset instanceof TransportationRoute)) {
// 	System.out.println (getName () + " : Got new route " + asset + 
// 			    ": " + UTILAsset.getNodeNames ((TransportationRoute) asset));
//       }
    }

    if (getSchedulerResult() == null) {
      throw new UTILPluginException(myClusterName + 
				    "GSSBufferingPlugin found null SchedulerResult.  Probable cause is bad XML file for this cluster");
    }
    getSchedulerResult ().addAssetList (newStuff);
  }

  /**
   * Implemented for UTILAssetListener
   * Place to handle changed assets.
   *
   * Does nothing by default.
   *
   * @param newAssets changed assets found in the container
   */
  public void handleChangedAssets(Enumeration changedAssets) {}

  /******************************************************************************/
  /************************ END Asset handling **********************************/
  /******************************************************************************/

  protected Enumeration showTasks (Enumeration tasks) {
    Vector t = new Vector ();
    System.out.println (getName () + " : Processing these tasks --->");

    while(tasks.hasMoreElements()) {
      Object obj = tasks.nextElement();
      t.add (obj);

      System.out.println ("" + obj);
    }
    return t.elements ();
  }

  public void showDebugIfFailure () {
    if (showDebugOnFailure && 
	!myExtraOutput && !myExtraExtraOutput && !debugGSS) {
      System.out.println (getName () + ".showDebugOnFailure - found " + 
			  " failed PE " + 
			  "so now turning on debug output.");
      myExtraOutput = true;
      myExtraExtraOutput = true;
      // debugGSS      = true;
      scheduler.setDebug (debugGSS);
    }
  }

  /**
   * Runs the GSS.  After running, makes plan elements, BUT asks
   * plugin whether each task group->asset mapping should be made into a 
   * plan element.
   * 
   * WARNING -- plugins must be careful to handle EACH AND EVERY task -- 
   *            preProcessBufferedTasks should not remove any tasks from the
   *            list of buffered tasks unless it plans to make plan elements
   *            for those tasks.   This is important.
   *
   * @param bufferedTasks are given to the scheduler, which will remember
   *        them until plan elements are made from them. 
   */
  public void processTasks (List bufferedTasks) {
    // NOP by default
    preProcessBufferedTasks(bufferedTasks);

    if (getSchedulerResult().getAssets ().isEmpty()) {
      System.out.println (getName () + " - processBufferedTasks : " + 
			  "ERROR - no assets given to scheduler.");
    }

    getScheduler ().schedule (bufferedTasks, getSchedulerResult());
    //    System.err.println("Result of this scheduler run is :" +
    //		       plugin.getSchedulerResult());

    makePlanElementsFromSchedule (MAKE_PE_ONLY_IF_FULL);
  }

  /** 
   * Implemented for UTILGSSBufferingPlugin interface
   * do any preprocessing necessary before passing tasks to scheduler 
   */
  public void preProcessBufferedTasks(List bufferedTasks) {
    // By default, do nothing
    return;
  }


  /**
   * Makes plan elements, BUT does not ask
   * plugin whether each task group->asset mapping should be made into a 
   * plan element.
   *
   * Called after GSS_MAXTIME has been exceeded without plan elements being
   * made for some tasks.
   */
  public void processLeftoverTasks () {
    makePlanElementsFromSchedule (MAKE_PE_EVEN_WHEN_NOT_FULL);
  }

  /**
   * Goes over all the assets in the gss scheduler, and for each one gets the
   * task groups assigned.  For each task group->asset mapping, makes a plan element
   * (see param comment).  If the plugin is not an allocator, freezes task group so
   * there will be no possibility of using an unavailable asset.  (There is a time
   * gap between a non-allocation PE being made and the role schedule of the asset 
   * being set.  This is the only way GSS can tell when an asset is unavailable.)
   *
   * Finally, calls handleImpossibleTasks on any tasks that the GSS could not map 
   * to an asset.
   *
   * sets lastDisposition to be now.
   *
   * @param checkIfFull -- when true, calls isReadyToMakePE to ask plugin
   *        whether a plan elment should be made for the task group->asset mapping
   *        otherwise, calls makePlanElement without asking plugin.
   */
  protected void makePlanElementsFromSchedule (boolean checkIfFull) {
    for (Iterator iter = schedulerResult.getAssets().iterator (); iter.hasNext ();) {
      Asset anAsset = (Asset) iter.next ();
      List taskGroups = schedulerResult.getTaskGroups (anAsset);

      for (Iterator tgIter = taskGroups.iterator (); tgIter.hasNext ();) {
	GSTaskGroup group = (GSTaskGroup) tgIter.next ();

	if (!checkIfFull || isReadyToMakePE (anAsset, group)) {
	  makePlanElement (anAsset, group);
	  if (freezeTaskGroupForPublish(anAsset, group)) 
	    group.setFrozen(true);
	  else
	    tgIter.remove ();
	}
      }
    }
    handleImpossibleTasks (schedulerResult.getUnhandledTasks ());

    // we might want to change this -- one way to handle impossible tasks
    // is to wait for better assets to arrive in the cluster, in which
    // case we don't want to forget about these unhandled tasks
    schedulerResult.clearUnhandledTasks ();
  }

  /** 
   * ask plugin if asset is ready to be disposed 
   * of (allocated, expanded, or aggregated).
   *
   * E.g. for allocation of a ship, is it full?
   */
  public boolean isReadyToMakePE (Asset anAsset, GSTaskGroup group) { return true; }

  /**
   * if no asset could be found to handle the task, handle them in some way -
   * Tasks that did not get handled become failed plan elements of one of the 
   * three species.
   *
   * if showDebugOnFailure (env var) is true (= true by default), 
   * debug print outs turn on automatically
   *
   * @param list of tasks without plan elements after the gss runs
   */
  public void handleImpossibleTasks (List unallocatedTasks) {
    //    if (myExtraOutput)
    if (!unallocatedTasks.isEmpty ())
      System.out.println (getName () + 
			  unallocatedTasks.size () + 
			  " impossible tasks ignored!");
  }

  /**
   * Call this function with all task groups that are being frozen.
   * @param gstg the task group that is being frozen.
   * @return A vector of PrepPhrases that will uniquely define this
   * GSTaskGroup, and can be used later to remove the Frozen tasks and
   * unfreeze the group.
   **/
  public List registerFrozenTaskGroup(GSTaskGroup gstg){
    List prepPhrases = new ArrayList();
    //add this group to the table (the table itself handles multiple
    //assignments gracefully).
    Integer uid = GSTaskGroupsByUID.put(gstg);
    if (myExtraOutput) {
      System.out.println (getName () + ".registerFrozenTaskGroup : GSTaskGroup " + 
			  gstg + " -> " + 
			  uid + ", now " + 
			  GSTaskGroupsByUID.size () + " entries.");
      if (GSTaskGroupsByUID.get (uid) == null)
	System.out.println (getName () + ".registerFrozenTaskGroup : Huh? " + 
			    uid + " not in hashtable?");
    }

    if(uid == null)
      throw new UTILPluginException("Could not determine UID for GSTaskGroup: " + gstg);
      
    prepPhrases.add(UTILPrepPhrase.makePrepositionalPhrase(ldmf,
							   WITHGROUP,
							   uid));
    return prepPhrases;
  }

  /**
   * Call this function on any task that is being created and frozen.
   * This is a shortcut for calling registerFrozenTaskGroup(gstg) and then
   * adding the returned vector of PrepPhrases to the given task.
   * NOTE this function WILL modify the passed in task by adding
   * a WITHGROUP preposition for the task, providing it with a UID that
   * is cached so the group can later be unfrozen.
   **/
  public void registerFrozenTask(Task t, GSTaskGroup gstg){
    List phrases = registerFrozenTaskGroup(gstg);
	
    //Now add the uid to the task:
    for(int i=0;i<phrases.size();i++)
      UTILPrepPhrase.addPrepToTask(t, (PrepositionalPhrase)phrases.get(i));
  }

  /**
   * Cleanup after removing frozen task
   **/
  public void cleanupAfterRemovedFrozenGroup(GSTaskGroup group){}

  /**
   * This function should be called to actually remove the frozen tasks from
   * The scheduler when what ever processing is necessary has been done.
   * @param allocResult The allocation result that generated the
   * possiblyFrozenTasks.  They should/or should not be removed based on
   * the contents of the allocationResult.
   * @param possiblyFrozenTasks a list of Tasks that have been 
   * processed and dependent on the AllocationResults, should now be removed 
   * from the scheduler.
   **/
  public void removeFrozenTasks(AllocationResult allocResult, List possiblyFrozenTasks){
    //If we're not successful, or if the last guy in the chain has been a success,
    //then we want to remove the items from the scheduler:
    if(!allocResult.isSuccess() || allocResult.auxiliaryQuery(AQ_STATUS).equals(AQ_SUCCESS)){
      //We only need to remove the group based upon the first task, as we assume any additional
      //tasks were based upon the same assignment.
      if(possiblyFrozenTasks.size()>0){
	Task task = getRepresentativeTask (possiblyFrozenTasks);
	if (task == null) {
	  if (myExtraOutput)
	    System.out.println(getName() + " : got an expansion with no representative tasks.");
	  return;
	}
		  
	if (myExtraExtraOutput)
	  System.out.println(getName() + ": Attempting to remove task "
			     + task.getUID () + " from GSS scheduler."); 

	// If the agg was handled by GSS
	if (UTILPrepPhrase.hasPrepNamed(task, WITHGROUP)) {
	  // Find appropriate GSTaskGroup in aggregation
	  PrepositionalPhrase ppg = UTILPrepPhrase.getPrepNamed(task, WITHGROUP);

	  Integer GSTGuid = (Integer)ppg.getIndirectObject();
	  if(GSTGuid == null){
	    System.err.println(getName () + ".removeFrozenTasks(): " + 
			       "ERROR - GSTaskGroup UID was null: tasks may remain frozen...");
	    return;
	  }
	  GSTaskGroup group = (GSTaskGroup)GSTaskGroupsByUID.get(GSTGuid);
	  if(group == null){
	    if(myExtraExtraOutput)
	      System.out.println(getName () + ".removeFrozenTasks(): " + 
				 "Group (" + GSTGuid + ") not found in " + 
				 GSTaskGroupsByUID.size () + 
				 " entries. Assuming it has already been unfrozen.");
	    return;
	  }
		    
	  if(myExtraOutput)
	    System.out.println(getName() + ": Removing GSTGuid: " + GSTGuid);

	  //Now try to remove the GSTaskGroup from the UID table:
	  GSTaskGroupsByUID.remove(GSTGuid);
  
	  // And the transport asset that's moving those tasks
	  PrepositionalPhrase ppa = UTILPrepPhrase.getPrepNamed(task, WITH); //Constants.Preposition.WITH);
	  if(ppa == null){
	    System.err.println("UTILGSSBufferingPluginAdapter.removeFrozenTasks(): " + 
			       "ERROR : Could not find prepPhrase WITH in task.");
	    return;
	  }
	  Asset asset = (Asset)ppa.getIndirectObject();
	  if(asset == null){
	    System.err.println("UTILGSSBufferingPluginAdapter.removeFrozenTasks(): " + 
			       "ERROR : WITH preposition was null: tasks may remain frozen...");
	    return;
	  }

	  
	  // if we haven't done this before
	  if (getSchedulerResult().hasTaskGroup(asset, group)) {
	    // remove it from the scheduler result
	    getSchedulerResult().removeTaskGroupForAsset(asset, group);
	    cleanupAfterRemovedFrozenGroup(group);	    
	    if (myExtraOutput)
	      System.out.println(getName() + ": Successfully removed Asset-GSTaskGroup pair " +
				 "given by {" + asset + " --> " +
				 "(Group Representive Task) " + group.representativeTask()
				 + "} from SchedulerResult.");
	  }else
	    if(myExtraExtraOutput)
	      System.out.println(getName() + ": Asset-GSTaskGroup pair given by {" +
				 asset + " --> " + "(Group Representitive Task( " + 
				 group.representativeTask() + "} not found in SchedulerResult,");

	}
      }
    }else if(myExtraExtraOutput){
      System.out.println(getName() + ": Not ready to remove frozen tasks: {" +
			 possiblyFrozenTasks + "}.");
    }   
  }

  protected Task getRepresentativeTask (List possiblyFrozenTasks) {
    return (Task)possiblyFrozenTasks.get(0);
  }

  protected GSScheduler scheduler;
  protected GSSchedulerResult schedulerResult;
  protected UTILAssetCallback myAssetCallback;
  protected boolean debugGSS = true;
}
