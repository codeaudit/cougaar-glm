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

package org.cougaar.lib.filter;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.core.component.StateObject;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.planning.service.LDMService;
import org.cougaar.core.service.LoggingService;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILRehydrateReactor;
import org.cougaar.lib.param.ParamMap;
import org.cougaar.lib.param.Param;
import org.cougaar.lib.util.*;
import org.cougaar.lib.xml.parser.ParamParser;

import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;

/**
 * <pre>
 * Implementation of UTILPlugin interface.
 * 
 * parameters are read when the plugin is loaded.
 *
 * filterCallbacks are added just before the plugin
 * thread is started.
 * </pre>
 */
public class UTILPluginAdapter extends ComponentPlugin implements UTILPlugin, StateObject {
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
      return getAgentIdentifier();
    else 
      return originalAgentID;
  }

  protected MessageAddress getOriginalAgentID () {
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
    originalAgentID = (MessageAddress) o;
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

    paramParser = new ParamParser();
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
    myClusterName = getAgentIdentifier().getAddress();
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
   * <pre>
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * The intent is that all "on start-up" filters get added here.
   *
   * Note that there is no restraint on when a subclass can add a 
   * filter, as long as it doesn't happen before setupFilters ().
   * 
   * </pre>
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
      if (isDebugEnabled ())
        debug ("Adding null filter (THIS MAY BE OK.)");
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
    if (isDebugEnabled ()) 
      debug (getName () + " : Subscribing to " + pred);

    return (IncrementalSubscription) blackboard.subscribe (pred); 
  }

  /**
   * Implemented for UTILFilterCallbackListener
   *
   * This allows callbacks to make subscriptions with a special container.
   */
  public IncrementalSubscription subscribeFromCallback (UnaryPredicate pred,
							Collection specialContainer) {
    if (isDebugEnabled ()) 
      debug (getName () + " : Subscribing to " + pred);

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
    MessageAddress agentID = (didSpawn () ? 
				 getOriginalAgentID () :
				 getAgentIdentifier());
								 
    myParams = createParamTable (myP, agentID);

    // set instance variables

    if (isInfoEnabled()) {
      String optionalEnvFile = null;
      try {
        optionalEnvFile = myParams.getStringParam("envFile");
        if (optionalEnvFile == null)
          optionalEnvFile = myParams.getStringParam("default_envFile");
      } catch (Exception e){}
      info (getClassName () + ".getEnvData : read param file <" + optionalEnvFile + ">");
    }
    
    if (isDebugEnabled())
      debug (getName () + " - Params : " + myParams);
  }

  /**
   * Subclass to return a different ParamTable.
   */
  protected ParamMap createParamTable (Vector envParams, 
				       MessageAddress ident) {
    if (isDebugEnabled()) {
      debug (getName () + " - creating param table, identifier was " + ident);
      for (Iterator i = envParams.iterator(); i.hasNext();) {
	String runtimeParam = (String)i.next();
	Param p = paramParser.getParam(runtimeParam);
	if(p != null){
	  String name = p.getName();
	  debug("UTILPluginAdapter.createParamTable() - got param name " + name
		+ " with value " + p);
	}
      }
    }
    return new UTILParamTable (envParams, ident, logger);
  }


  /** <pre>
   * Place to put any local plugin startup initiallization.
   * This is a good place to read local data from files.
   *
   * This is useful during integration when getting as much as debugrmation
   * as possible at the point of failure is critical.
   * </pre>
   */
  public void localSetup () {
    try {
      if (getMyParams().hasParam ("skipLowConfidence"))
	skipLowConfidence = getMyParams().getBooleanParam("skipLowConfidence");
      else 
	skipLowConfidence = true;

      if (getMyParams().hasParam("HIGH_CONFIDENCE"))
	HIGH_CONFIDENCE = getMyParams().getFloatParam("HIGH_CONFIDENCE");
      else
	HIGH_CONFIDENCE = 0.99d;
    } catch (Exception e) {}
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
   * <pre>
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
   * </pre>
   */
  public final void updateAllocationResult(PlanElement cpe) {
    if (isDebugEnabled ())
      debug (getName () + " : Received changed pe " + 
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
	if (isDebugEnabled ())
          debug (getName () + " : Swapping Alloc Results for task " + 
                              cpe.getTask ().getUID ());
        if (isWarnEnabled() && !reportedresult.isSuccess ())
          warn (getName () + " : " + 
		cpe.getTask ().getUID () + " failed to allocate.");

        cpe.setEstimatedResult(reportedresult);

	if (!(estimatedresult == null)){
	  if (isDebugEnabled ()) {
	    debug (getName() + " auxiliaryQueries for task " +
				cpe.getTask().getUID()); 	  
	    for (int i= 0; i<AuxiliaryQueryType.LAST_AQTYPE+1; i++) {
	      debug ("\tEstimatedResult - " + i +  ", " +
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
    else if (!cpe.getTask().getSource().equals(getAgentIdentifier())) {
      error ("ERROR! " + getName() + 
	     " : "     + cpe.getTask().getUID() + 
	     " has a null reported allocation.");
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
    if (isDebugEnabled ())
      debug (getName () + " : cycle called (a subscription changed)");

    synchronized (mySubscriptions) {
      for (int i = 0; i < mySubscriptions.size ();  i++) {
        UTILFilterCallback cb = (UTILFilterCallback) mySubscriptions.elementAt (i);
	if (blackboard.didRehydrate () && !checkedDidRehydrate) {
	  if (cb instanceof UTILRehydrateReactor) {
	    ((UTILRehydrateReactor)cb).reactToRehydrate();
	    // don't react to a changed filter, since react to rehydrate should
	    // already deal with new items in the container
	    continue;
	  }
	}
		
        if (cb.getSubscription ().hasChanged ()) {
          if (isDebugEnabled ())
            debug ("\tFilter# " + i + 
		   " of Subscription " + 
		   cb.getSubscription() + 
		   " has changed.");
          cb.reactToChangedFilter ();
        }
      }

      checkedDidRehydrate = true; // so we don't get those Failed to find persisted state messages
    }
  }

  /** 
   * Automatic support for persistent state.
   *
   * Calls rehydrate if appropriate 
   */
  protected void justRehydrated () {
    if (isInfoEnabled ())
      info (getName () + ".justRehydrated.");

    persistentState = findState ();
	
    // tell subclasses about rehydrated state
    if (persistentState != null) {
      if (isDebugEnabled())
	debug (getName () + ".justRehydrated - found state.");
      rehydrateState (persistentState.stuff);
    }
    else {
      if (isDebugEnabled())
	debug (getName () + ".justRehydrated - no state found.");
    }
  }

  /** 
   * Call this method to add your state to what will be persisted 
   *
   * Call from inside of a transaction.
   */
  protected void registerPersistentState (Object obj) {
    if (blackboard.didRehydrate ()) {
      debug (getName () + ".registerPersistentState - just rehydrated, " + 
	     "so ignoring register request for " + obj);
      return;
    }
	
    if (persistentState == null) {
      persistentState = findState ();

      if (persistentState == null) {
	persistentState = new PersistentState(getClassName ()+"_Persistent_State");
	if (isDebugEnabled())
	  debug (getName () + ".registerPersistentState - publishingState.");
	publishAdd (persistentState);
      }
    }

    persistentState.stuff.add (obj);
  }

  /** anything you added with register, you will be debugrmed about here upon rehydration */
  protected void rehydrateState (List stuff) {
    if (isDebugEnabled())
      debug (getName () + ".rehydrate - got " + stuff.size () + " persistent items.");
  }
  
  protected PersistentState findState () {
    Collection stuff = blackboard.query (new UnaryPredicate () {
	public boolean execute (Object obj) {
	  boolean myState = (obj instanceof PersistentState);
	  if (!myState) return false;
	  PersistentState state = (PersistentState)obj;
	  boolean match = state.name.startsWith (getClassName());
		  
	  debug (getName () + "findState - found state!  Comparing state name " +
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
      copyOfTask = expandHelper.cloneTask (ldmf, taskToReplace);

    if (isDebugEnabled())
      debug (getName() + " replacing task " + 
	     taskToReplace.getUID() + " in workflow with " + 
	     copyOfTask.getUID());

    ((NewTask) copyOfTask).setWorkflow (tasksWorkflow);
    tasksWorkflow.addTask (copyOfTask);

    publishChange (tasksWorkflow);
    publishAdd    (copyOfTask);
    publishRemove (taskToReplace);
  }

  protected final void publishAdd(Object o) {
    getBlackboardService().publishAdd(o);
  }
  protected final void publishRemove(Object o) {
    getBlackboardService().publishRemove(o);
  }
  protected final void publishChange(Object o) {
    getBlackboardService().publishChange(o, null);
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

  /** 
   * rely upon load-time introspection to set these services - 
   * don't worry about revokation.
   */
  public final void setLoggingService(LoggingService bs) {  
    logger = bs; 

    assetHelper = new UTILAsset (logger);
    prepHelper = new UTILPrepPhrase (logger);
    prefHelper = new UTILPreference (logger);
    aggregateHelper = new UTILAggregate (logger);
    allocHelper = new UTILAllocate   (logger);
    expandHelper = new UTILExpand     (logger);
    verifyHelper = new UTILVerify     (logger);
  }

  /**
   * Get the logging service, for subclass use.
   */
  protected LoggingService getLoggingService() {  return logger; }

  //
  // specific "isEnabledFor(..)" shorthand methods:
  //
  protected boolean isInfoEnabled() { return logger.isInfoEnabled (); }
  protected boolean isDebugEnabled()  { return logger.isDebugEnabled ();  }
  protected boolean isWarnEnabled()  { return logger.isWarnEnabled ();  }
  protected boolean isErrorEnabled() { return logger.isErrorEnabled (); }
  protected boolean isFatalEnabled() { return logger.isFatalEnabled (); }

  //
  // specific "level" shorthand methods:
  //

  /**
   * Equivalent to "log(info, ..)".
   */
  protected void info(String message) { logger.info (message); }

  /**
   * Equivalent to "log(debug, ..)".
   */
  protected void debug(String message) { logger.debug (message); }

  /**
   * Equivalent to "log(WARN, ..)".
   */
  protected void warn(String message) { logger.warn (message); }

  /**
   * Equivalent to "log(ERROR, ..)".
   */
  protected void error(String message) { logger.error (message); }

  /**
   * Equivalent to "log(FATAL, ..)".
   */
  protected void fatal(String message) { logger.fatal (message); }

  /** @return cluster name and plugin name */
  public String getName () { 
    if (myClusterName == null) {
      myName = null;
      myClusterName = getAgentIdentifier().getAddress();
    }
    if (myName == null)
      myName = getClusterName () + "/" + getClassName ();
    return myName;
  }

  /** holds persistent state, labeled with name of plugin */
  private static class PersistentState implements Serializable {
    public PersistentState (String name) {	  this.name =name;	}
	  
    String name;
    List stuff  = new ArrayList();
  }
  
  protected Vector mySubscriptions;

  protected PlanningFactory ldmf;
  protected String myClusterName; 
  protected Plan realityPlan;

  // .env vars
  protected ParamMap myParams;
  protected ParamParser paramParser;
  protected boolean showinfoOnFailure;
  protected boolean skipLowConfidence = true;
  protected double HIGH_CONFIDENCE = 0.99d;
  protected PersistentState persistentState;
  private MessageAddress originalAgentID = null;
  protected String myName;
  protected LoggingService logger;

  protected UTILAsset assetHelper;
  protected UTILAggregate aggregateHelper;
  protected UTILPreference prefHelper;
  protected UTILPrepPhrase prepHelper;
  protected UTILAllocate allocHelper;
  protected UTILExpand expandHelper;
  protected UTILVerify verifyHelper;

  protected boolean checkedDidRehydrate = false;
}
