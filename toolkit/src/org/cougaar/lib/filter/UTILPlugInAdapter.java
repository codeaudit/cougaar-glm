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

package org.cougaar.lib.filter;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.core.plugin.PlugInAdapter;

import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILRehydrateReactor;
import org.cougaar.lib.param.ParamMap;
import org.cougaar.lib.param.Param;
import org.cougaar.lib.util.UTILExpand;
import org.cougaar.lib.util.UTILParamTable;
import org.cougaar.lib.xml.parser.ParamParser;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.plugin.LDMService;
import org.cougaar.core.plugin.PluginBindingSite;
import org.cougaar.core.component.StateObject;

/**
 * Implementation of UTILPlugIn interface.
 * 
 * parameters are read when the plugin is loaded.
 *
 * filterCallbacks are added just before the plugin
 * thread is started.
 */

public class UTILPlugInAdapter extends ComponentPlugin implements UTILPlugIn, StateObject {
  /**
   * Implemented for StateObject
   * <p>
   * Get the current state of the Component that is sufficient to
   * reload the Component from a ComponentDescription.
   *
   * @return null if this Component currently has no state
   */
  public Object getState() {
    if (originalAgentID == null)
	  return ((PluginBindingSite)getBindingSite()).getAgentIdentifier();
    else 
	  return originalAgentID;
  }

  protected ClusterIdentifier getOriginalAgentID () {
	return originalAgentID;
  }
  
  /**
   * Implemented for StateObject
   * <p>
   * Set-state is called by the parent Container if the state
   * is non-null.
   * <p>
   * The state Object is whatever this StateComponent provided
   * in it's <tt>getState()</tt> implementation.
   * @param o the state saved before
   */
  public void setState(Object o) {
	originalAgentID = (ClusterIdentifier) o;
  }

  /** true iff originalAgentID is not null -- i.e. setState got called */
  protected boolean didSpawn () {
	boolean val = (originalAgentID != null);
	return val;
  }

  /**
   * This method is called before cycle is ever called to
   * set up safe transactions for container subscriptions.
   *
   * setupFilters () is wrapped in an open/closeTransaction block.
   */
  protected final void setupSubscriptions() {
	if (blackboard.didRehydrate ())
	  justRehydrated ();
	
    getEnvData();
    setInstanceVariables ();

    preFilterSetup ();

	setupFilters ();

    localSetup();
  }
  
  private LDMService ldmService = null;
  public final void setLDMService(LDMService s) {
    ldmService = s;
  }
  protected final LDMService getLDMService() {
    return ldmService;
  }

  /**
   * set instance variables here that don't depend on 
   * env var parameters.
   */
  private void setInstanceVariables () {
    ldmf = getLDMService().getFactory();
    myClusterName = ((PluginBindingSite)getBindingSite()).getAgentIdentifier().getAddress();
	realityPlan = ldmf.getRealityPlan();
    mySubscriptions = new Vector ();
  }

  /**
   * A place to put setup actions that will occur before
   * any filters are active.
   *
   * I.e. objects created here can be referenced in 
   * filterCallback listener methods.
   *
   * Default does nothing.
   */
  public void preFilterSetup () {
  }

  /****************************************************************
   ** Setup Filters...
   **/

  /**
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * The intent is that all "on start-up" filters get added here.
   *
   * Note that there is no restraint on when a subclass can add a 
   * filter, as long as it doesn't happen before setupFilters ().
   * 
   * @see #addFilter ()
   */
  public void setupFilters () {
  }

  /**
   * mySubscriptions is synchronized here and in cycle to make
   * sure that no subscriptions are added while iterating over the 
   * list of current subscriptions (in cycle ()).
   *
   * This method can be called at any time, not just from setupFilters ().
   *
   * @see #setupFilters ()
   */
  public void addFilter  (UTILFilterCallback callbackObj) {
    if (callbackObj == null) {
      if (myExtraFilterOutput)
        System.out.println ("Adding null filter (THIS MAY BE OK.)");
      return;
    }

    synchronized (mySubscriptions) {
      mySubscriptions.addElement (callbackObj);
    }
  }

  /**
   * mySubscriptions is synchronized here and in cycle to make
   * sure that no subscriptions are removed while iterating over the 
   * list of current subscriptions (in cycle ()).
   *
   * This method can be called at any time, not just from setupFilters ().
   *
   * BOZO - should make addFilter automatically do subscribe too.
   * Not enough time now (9-4-99) -- should do later!!!
   * Problem is if we want special containers to be used with subscriptions.
   * A number of API changes would be necessary...
   *
   * @see #setupFilters ()
   */
  public void removeFilter  (UTILFilterCallback callbackObj) {
    synchronized (mySubscriptions) {
      mySubscriptions.removeElement (callbackObj);
	  blackboard.unsubscribe(callbackObj.getSubscription ());
    }
  }

  /**
   * Implemented for UTILFilterCallbackListener
   *
   * This allows callbacks to make subscriptions.
   */
  public IncrementalSubscription subscribeFromCallback (UnaryPredicate pred) {
    if (myExtraFilterOutput) 
      System.out.println (getName () + " : Subscribing to " + pred);

    return (IncrementalSubscription) blackboard.subscribe (pred); 
  }

  /**
   * Implemented for UTILFilterCallbackListener
   *
   * This allows callbacks to make subscriptions with a special container.
   */
  public IncrementalSubscription subscribeFromCallback (UnaryPredicate pred,
							Collection specialContainer) {
    if (myExtraFilterOutput) 
      System.out.println (getName () + " : Subscribing to " + pred);

    return (IncrementalSubscription) blackboard.subscribe (pred, specialContainer); 
  }

  /** 
   * Allows child classes to read additional data from environment files. 
   * 
   * Derived classes should call super ().
   **/
  public void getEnvData () {

    // Don't care to clone the vector?
    Vector myP;
	if (getParameters () != null)
	  myP = new Vector (getParameters());
	else
	  myP = new Vector ();

    // create the parameter table
	ClusterIdentifier agentID = (didSpawn () ? 
								 getOriginalAgentID () :
								 ((PluginBindingSite)getBindingSite()).getAgentIdentifier());
								 
    myParams = createParamTable (myP, agentID);

    // set instance variables
    try{myExtraExtraOutput = myParams.getBooleanParam("ExtraExtraOutput");}
    catch(Exception e){myExtraExtraOutput = false;}
    try{myExtraOutput = (myParams.getBooleanParam("ExtraOutput")||myExtraExtraOutput);}
    catch(Exception e){myExtraOutput = (false||myExtraExtraOutput);}
    try{myExtraFilterOutput = myParams.getBooleanParam("ExtraFilterOutput");}
    catch(Exception e){myExtraFilterOutput = false;}

    if (myExtraOutput) {
      String optionalEnvFile = null;
      try {
        optionalEnvFile = myParams.getStringParam("envFile");
        if (optionalEnvFile == null)
          optionalEnvFile = myParams.getStringParam("default_envFile");
      } catch (Exception e){}
      System.out.println (getClassName () + ".getEnvData : read param file <" + optionalEnvFile + ">");
    }
    
    if (showParameters)
      System.out.println (getName () + " - Params : " + myParams);
  }

  /**
   * Subclass to return a different ParamTable.
   */
  protected ParamMap createParamTable (Vector envParams, 
					 ClusterIdentifier ident) {
    if (showParameters) {
      System.out.println (getName () + " - creating param table, identifier was " + ident);
      for (Iterator i = envParams.iterator(); i.hasNext();) {
	String runtimeParam = (String)i.next();
	Param p = ParamParser.getParam(runtimeParam);
	if(p != null){
	  String name = p.getName();
	  System.out.println("UTILPlugInAdapter.createParamTable() - got param name " + name
			     + " with value " + p);
	}
      }
    }
    return new UTILParamTable (envParams, ident);
  }


  /** <pre>
   * Place to put any local plugin startup initiallization.
   * This is a good place to read local data from files.
   *
   * By default all plugins have a showDebugOnFailure option.
   * When set to true (default), whenever a plugin makes a failed plan element, the
   * myExtraOutput and myExtraExtraOutput flags are turned on and lots of
   * debug output is generated.
   * This can be turned off by setting showDebugOnFailure to false.
   *
   * This is useful during integration when getting as much as information
   * as possible at the point of failure is critical.
   * </pre>
   */
  public void localSetup () {
    try {showDebugOnFailure = getMyParams().getBooleanParam("showDebugOnFailure");}
    catch (Exception e) {showDebugOnFailure = true;}
    try {skipLowConfidence = getMyParams().getBooleanParam("skipLowConfidence");}
    catch (Exception e) {skipLowConfidence = true;}
    try {HIGH_CONFIDENCE = getMyParams().getFloatParam("HIGH_CONFIDENCE");}
    catch (Exception e) {HIGH_CONFIDENCE = 0.99d;}
  }

  /** 
   * Accessor to param table.  Child classes can use this to
   * add additional parameters.
   * @return ParamTable
   */
  public ParamMap getMyParams () { 
    return myParams; 
  }

  /**
   * if the reported allocation result has been successfully calculated
   * set the estimated equal to the reported to send a notification
   * back up.
   *
   * Only reports results if the confidence is above a threshold.
   * This threshold is currently 0.99.  All changes will be reported
   * if skipLowConfidence is set to false.
   *
   * Takes care not to copy nulls from reported aux query results into
   * estimated fields.
   */
  public final void updateAllocationResult(PlanElement cpe) {
    if (myExtraExtraOutput)
      System.out.println (getName () + " : Received changed pe " + 
			  cpe.getUID () + " for task " + 
			  cpe.getTask ().getUID());
    AllocationResult reportedresult = cpe.getReportedResult();
    if (reportedresult != null) {
      // compare entire allocationresults.
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      double confidence = reportedresult.getConfidenceRating ();
      boolean nullEstimated  = (estimatedresult == null);
      // if we are not ignoring low confidence reported values
      boolean highConfidence = (!skipLowConfidence || confidence > HIGH_CONFIDENCE);

      if ( nullEstimated  || 
	   (highConfidence &&
	   (! estimatedresult.isEqual(reportedresult)))) { 
	if (myExtraExtraOutput)
          System.out.println (getName () + " : Swapping Alloc Results for task " + 
                              cpe.getTask ().getUID ());
        if ((myExtraOutput || myExtraExtraOutput) &&
            !reportedresult.isSuccess ())
          System.out.println (getName () + " : " + 
                              cpe.getTask ().getUID () + " failed to allocate.");

        cpe.setEstimatedResult(reportedresult);

	if (!(estimatedresult == null)){
	  if (myExtraExtraOutput) {
	    System.out.println (getName() + " auxiliaryQueries for task " +
				cpe.getTask().getUID()); 	  
	    for (int i= 0; i<AuxiliaryQueryType.LAST_AQTYPE+1; i++) {
	      System.out.println ("\tEstimatedResult - " + i +  ", " +
				  estimatedresult.auxiliaryQuery(i) + 
				  "\tReportedResult - " + i + ", " +
				  reportedresult.auxiliaryQuery(i));
	    }
	  }
	  for (int i = 0; i < AuxiliaryQueryType.LAST_AQTYPE+1; i++){
	    if ((estimatedresult.auxiliaryQuery(i) != null) &&
		(reportedresult.auxiliaryQuery(i) == null)) {
	      cpe.getEstimatedResult().addAuxiliaryQueryInfo(i, estimatedresult.auxiliaryQuery(i));
	    }
	  }
	}
	blackboard.publishChange(cpe);
      }
    }
    else if (!cpe.getTask().getSource ().equals (((PluginBindingSite)getBindingSite()).getAgentIdentifier())) {
      System.out.println ("ERROR! " + getName () + 
                          " : "     + cpe.getTask ().getUID () + 
                          " has a null reported allocation.");
    }
  }
  
    /**
     * Turns on debug output if a failed plan element is generated.
     */
    public void showDebugIfFailure () {
      if (showDebugOnFailure && 
	  !myExtraOutput && !myExtraExtraOutput) {
	System.out.println (getName () + ".showDebugOnFailure - found " + 
			    " failed PE " + 
			    "so now turning on debug output.");
	myExtraOutput = true;
	myExtraExtraOutput = true;
      }
    }

  /** 
   * Called every time one of the filterCallback subscriptions
   * change.
   *
   * What the plugin does in response to a changed subscription.
   *
   * Directs the filterCallback with the changed subscription
   * to react to the change in some way.
   */
  protected void execute() {
    if (myExtraFilterOutput)
      System.out.println (getName () + " : cycle called (a subscription changed)");

    synchronized (mySubscriptions) {
      for (int i = 0; i < mySubscriptions.size ();  i++) {
        UTILFilterCallback cb = (UTILFilterCallback) mySubscriptions.elementAt (i);
		if (blackboard.didRehydrate ()) {
		  if (cb instanceof UTILRehydrateReactor) {
			((UTILRehydrateReactor)cb).reactToRehydrate();
			// don't react to a changed filter, since react to rehydrate should
			// already deal with new items in the container
			continue;
		  }
		}
		
        if (cb.getSubscription ().hasChanged ()) {
          if (myExtraFilterOutput)
            System.out.println ("\tFilter# " + i + 
                                " of Subscription " + 
                                cb.getSubscription() + 
                                " has changed.");
          cb.reactToChangedFilter ();
        }
      }
    }
  }

  /** 
   * Automatic support for persistent state.
   *
   * Calls rehydrate if appropriate 
   */
  protected void justRehydrated () {
	if (myExtraExtraOutput)
	  System.out.println (getName () + ".justRehydrated.");

	persistentState = findState ();
	
	// tell subclasses about rehydrated state
	if (persistentState != null) {
	  if (myExtraOutput || true)
		System.out.println (getName () + ".justRehydrated - found state.");
	  rehydrateState (persistentState.stuff);
	}
	else {
	  if (myExtraOutput || true)
		System.out.println (getName () + ".justRehydrated - no state found.");
	}
  }

  /** 
   * Call this method to add your state to what will be persisted 
   *
   * Call from inside of a transaction.
   */
  protected void registerPersistentState (Object obj) {
	if (blackboard.didRehydrate ()) {
	  System.out.println (getName () + ".registerPersistentState - just rehydrated, " + 
						  "so ignoring register request for " + obj);
	  return;
	}
	
	if (persistentState == null) {
	  persistentState = findState ();

	  if (persistentState == null) {
		persistentState = new PersistentState(getClassName ()+"_Persistent_State");
		if (myExtraOutput || true)
		  System.out.println (getName () + ".registerPersistentState - publishingState.");
		publishAdd (persistentState);
	  }
	}

	persistentState.stuff.add (obj);
  }

  /** anything you added with register, you will be informed about here upon rehydration */
  protected void rehydrateState (List stuff) {
	if (myExtraOutput || true)
	  System.out.println (getName () + ".rehydrate - got " + stuff.size () + " persistent items.");
  }
  
  protected PersistentState findState () {
	Collection stuff = blackboard.query (new UnaryPredicate () {
		public boolean execute (Object obj) {
		  boolean myState = (obj instanceof PersistentState);
		  if (!myState) return false;
		  PersistentState state = (PersistentState)obj;
		  boolean match = state.name.startsWith (getClassName());
		  
		  System.out.println (getName () + "findState - found state!  Comparing state name " +
							  state.name + " with " + getClassName() + 
							  ((match) ? " MATCH! " : " no match"));

		  return match;
		}
	  }
										 );

	if (stuff.isEmpty ())
	  return null;
	else
	  return (PersistentState) stuff.iterator().next();
  }
  
  /**
   * Replaces a task in a workflow with a copy of itself.
   *
   * This fixes a problem with rescinded allocations, where 
   * rescinded allocations would appear twice in a container...
   *
   * Chris Allen of TASC told me to do this.
   *
   * @param taskToReplace -- task to replace in workflow
   */
  protected void replaceTaskInWorkflow (Task taskToReplace) {
    replaceTaskInWorkflow (taskToReplace, null);
  }

  /**
   * Replaces a task in a workflow with a copy of itself.
   *
   * This fixes a problem with rescinded allocations, where 
   * rescinded allocations would appear twice in a container...
   *
   * Chris Allen of TASC told me to do this.
   *
   * @param taskToReplace -- task to replace in workflow
   * @param copyOfTask    -- optional task to replace it with
   */
  protected void replaceTaskInWorkflow (Task taskToReplace, Task copyOfTask) {
    NewWorkflow tasksWorkflow = (NewWorkflow) taskToReplace.getWorkflow ();
    tasksWorkflow.removeTask (taskToReplace);
    
    if (copyOfTask == null)
      copyOfTask = UTILExpand.cloneTask (ldmf, taskToReplace);

    if (myExtraOutput)
      System.out.println (getName() + " replacing task " + 
			  taskToReplace.getUID() + " in workflow with " + 
			  copyOfTask.getUID());

    ((NewTask) copyOfTask).setWorkflow (tasksWorkflow);
    tasksWorkflow.addTask (copyOfTask);

    publishChange (tasksWorkflow);
    publishAdd    (copyOfTask);
    publishRemove (taskToReplace);
  }

  protected final boolean publishAdd(Object o) {
    return getBlackboardService().publishAdd(o);
  }
  protected final boolean publishRemove(Object o) {
    return getBlackboardService().publishRemove(o);
  }
  protected final boolean publishChange(Object o) {
    return getBlackboardService().publishChange(o, null);
  }

    /** @return the name of the cluster */
  public String getClusterName () { return myClusterName; }

  /** utility function to get just the name of this class (no package qualification) */
  protected String getClassName () {
      return getClassName (this);
  }

  /** utility function to get just the name of the class of an object (no package) */
  protected String getClassName (Object obj) {
    String classname = obj.getClass().getName ();
    int index = classname.lastIndexOf (".");
    classname = classname.substring (index+1, classname.length ());
    return classname;
  }

    /** @return cluster name and plugin name */
  public String getName () { 
    return getClusterName () + "/" + getClassName (); 
  }

  /** holds persistent state, labeled with name of plugin */
  private static class PersistentState implements Serializable {
	public PersistentState (String name) {	  this.name =name;	}
	  
	String name;
	List stuff  = new ArrayList();
  }
  
  protected Vector mySubscriptions;

  protected RootFactory ldmf;
  protected String myClusterName; 
  protected Plan realityPlan;

  // .env vars
  protected ParamMap myParams;
  protected boolean myExtraOutput;
  protected boolean myExtraExtraOutput;
  protected boolean myExtraFilterOutput;
  protected boolean showParameters = "true".equals(System.getProperty("UTILPlugInAdapter.showParameters","false"));

  protected boolean showDebugOnFailure;
  protected boolean skipLowConfidence = true;
  protected double HIGH_CONFIDENCE = 0.99d;
  protected PersistentState persistentState;
  private ClusterIdentifier originalAgentID = null;
  
}
