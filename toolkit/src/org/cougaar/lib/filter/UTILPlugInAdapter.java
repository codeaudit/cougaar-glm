/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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

import org.cougaar.lib.param.ParamTable;
import org.cougaar.lib.util.UTILExpand;
import org.cougaar.lib.util.UTILParamTable;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Implementation of UTILPlugIn interface.
 * 
 * parameters are read when the plugin is loaded.
 *
 * filterCallbacks are added just before the plugin
 * thread is started.
 */

public class UTILPlugInAdapter extends PlugInAdapter implements UTILPlugIn {
  public final void load(Object object) throws StateModelException {
    setThreadingChoice(SINGLE_THREAD);
    super.load(object);
    getEnvData();
  }

  /**
   * This method is called before cycle is ever called to
   * set up safe transactions for container subscriptions.
   *
   * setupFilters () is wrapped in an open/closeTransaction block.
   */
  protected final void prerun() {
    setInstanceVariables ();

    preFilterSetup ();

    try {
      openTransaction();
      setupFilters ();
    } catch (Exception e){
      synchronized (System.err) {
        System.err.println(myClusterName + ".prerun () : Caught" + e);
        e.printStackTrace();
      }
    } finally {
      closeTransaction(false);
    }

    localSetup();
  }
  
  /**
   * set instance variables here that don't depend on 
   * env var parameters.
   */
  private void setInstanceVariables () {
    ldmf = getLDM().getFactory();
    myClusterName = getClusterIdentifier().getAddress();
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
      unsubscribe(callbackObj.getSubscription ());
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

    return (IncrementalSubscription) subscribe (pred); 
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

    return (IncrementalSubscription) subscribe (pred, specialContainer); 
  }

  /** 
   * Allows child classes to read additional data from environment files. 
   * 
   * Derived classes should call super ().
   **/
  public void getEnvData () {

    // Don't care to clone the vector?
    Vector myP = getParameters();

    // create the parameter table
    myParams = createParamTable (myP, getClusterIdentifier());

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

    //System.out.println (getName () + " - Params : " + myParams);
  }

  /**
   * Subclass to return a different ParamTable.
   */
  protected ParamTable createParamTable (Vector envParams, 
					 ClusterIdentifier ident) {
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
  public ParamTable getMyParams () { 
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
	publishChange(cpe);
      }
    }
    else if (!cpe.getTask().getSource ().equals (getClusterIdentifier())) {
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
  protected void cycle() {
    if (myExtraFilterOutput)
      System.out.println (getName () + " : cycle called (a subscription changed)");
    openTransaction();

    synchronized (mySubscriptions) {
      for (int i = 0; i < mySubscriptions.size ();  i++) {
        UTILFilterCallback cb = (UTILFilterCallback) mySubscriptions.elementAt (i);
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
    closeTransaction();
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

  protected Vector mySubscriptions;

  protected RootFactory ldmf;
  protected String myClusterName; 
  protected Plan realityPlan;

  // .env vars
  protected ParamTable myParams;
  protected boolean myExtraOutput;
  protected boolean myExtraExtraOutput;
  protected boolean myExtraFilterOutput;
  
  protected boolean showDebugOnFailure;
  protected boolean skipLowConfidence = true;
  protected double HIGH_CONFIDENCE = 0.99d;
}
