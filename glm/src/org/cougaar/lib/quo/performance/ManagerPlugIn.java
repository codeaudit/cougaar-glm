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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */

package org.cougaar.lib.quo.performance;
//package org.cougaar.lib.quo;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;
import java.io.*;

/**
 * This COUGAAR PlugIn creates and publishes "CODE" tasks and if allocationResult is a success
 * it keeps producing more task
 * It also reads the PlugIn arguments and may alter MessageSize or slurp CPU
 */
public class ManagerPlugIn extends CommonUtilPlugIn {

    // Two assets to use as direct objects for the CODE tasks
    protected Asset what_to_code;
    protected IncrementalSubscription allocations;   // My allocations
    protected IncrementalSubscription forceExecute;   // My allocations

    
    protected int CPUCONSUME;
    protected int MESSAGESIZE;
    protected String FILENAME;
    protected int MAXCOUNT;
    protected int MYCOUNT=1;
    protected int  OUTSTANDING_MESSAGES;
    protected boolean DEBUG = false;
    protected int BURST_TIME=0;
    protected static String VERB;//="CODE1";

    private Date startTime;
    private Task t, changedMind;
    private int  sequenceNum=1;
    private AspectValue aspectVal;

    private  int count = 0;
    private long minDelta=0;
  
    private FileWriter fw;
    private double lastReceived=0;
  
    /**
     * parsing the plugIn arguments and setting the values for CPUCONSUME and MESSAGESIZE
     */
    protected void parseParameter(){
	Vector p = getParameters();
	CPUCONSUME=getParameterIntValue(p, "CPUCONSUME");
	MESSAGESIZE=getParameterIntValue(p, "MESSAGESIZE");
	FILENAME=getParameterValue(p, "FILENAME");
	MAXCOUNT=getParameterIntValue(p, "MAXCOUNT");
	OUTSTANDING_MESSAGES =getParameterIntValue(p, "OUTSTANDING_MESSAGES");
	DEBUG=getParameterBooleanValue(p, "DEBUG");
	BURST_TIME=getParameterIntValue(p, "BURST_TIME");
	VERB=getParameterValue(p, "VERB");
    }
   
    /**
     * Predicate that matches "CHANGE_MIND"
     */
    private UnaryPredicate changedMindPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Task)
		    {
			Task task = (Task)o;
			return task.getVerb().equals(Verb.getVerb("CHANGED_MIND"));
		    }
		return false;
	    }
	};

  
    public  static UnaryPredicate myAllocationPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Allocation) {
		    Task t = ((Allocation)o).getTask();
		    return (t != null) && (t.getVerb().equals(Verb.getVerb(VERB)));	
		}
		return false;
	    }
	};

    /**
     * Using setupSubscriptions to create the initial CODE tasks
     */
    protected void setupSubscriptions() {
	parseParameter(); //read the plugIn arguments
	addTask();
	forceExecute= (IncrementalSubscription)subscribe(changedMindPredicate); 
	allocations = (IncrementalSubscription)subscribe(myAllocationPredicate);
    }

    /**
     * This PlugIn has no subscriptions so this method does nothing
     */
    protected void execute () {
	allocateChangedtasks(allocations.getChangedList()); // Process changed allocations
	allocateChangedMind(forceExecute.getChangedList()); // Process changedMind allocations
    }

    protected void addTask() {
	publishAsset(what_to_code, "The next Killer App", "e something java");
	t = makeTask(what_to_code, VERB);
	setPreference(t, AspectType._ASPECT_COUNT, sequenceNum);
	publishAdd(t);
	//debug(DEBUG, FILENAME, fw,"\nManagerPlugIn::Adding task with num " +
	//    t.getPreferredValue(AspectType._ASPECT_COUNT ) + " and Verb " + VERB); 
	changedMind=makeTask(what_to_code, "CHANGED_MIND");
	publishAdd(changedMind);
    }
  
    public void publishAsset(Asset asset, String nameOfAsset, String itemIdentification){
	asset = theLDMF.createPrototype("AbstractAsset", nameOfAsset);
	NewItemIdentificationPG iipg = 
	    (NewItemIdentificationPG)theLDMF.createPropertyGroup("ItemIdentificationPG");
	iipg.setItemIdentification(itemIdentification);
	asset.setItemIdentificationPG(iipg);
	publishAdd(asset); 
    }

    protected void allocateChangedMind(Enumeration allo_enum){ // Process changed allocations  
	double outstanding=sequenceNum-lastReceived;
	while (allo_enum.hasMoreElements()) {
	    //System.out.println("outstanding tasks: " + outstanding);
	    Task task = (Task)allo_enum.nextElement() ;
	    if (outstanding < OUTSTANDING_MESSAGES) {
		changeTasks(t);
		changeTasks(changedMind);
	    }
	}
    }

    protected void  allocateChangedtasks(Enumeration allo_enum){
	AllocationResult est, rep;
	double val=0;
	double arr[] = null;
	double received = 0;
	while (allo_enum.hasMoreElements()) {
	    Allocation alloc = (Allocation)allo_enum.nextElement() ;
	    est=null; rep=null;
	    est = alloc.getEstimatedResult();
	    rep = alloc.getReportedResult();
	   
	    if (rep!=null){
		arr =rep.getResult();
		received = arr[0];
		//debug(DEBUG, FILENAME, fw,"ManagerPlugIn:allocateChangedTasks ........" + received);
		printTheChange();
		try {
		    Thread.sleep(BURST_TIME);
		} catch (InterruptedException e) {
		    System.out.println(e);
		}
		changeTasks(t);
		changeTasks(changedMind);
	    }
	    breakFromLoop(count, MAXCOUNT);
	}
	lastReceived = received;
    }
    
    protected void setPreference ( Task t, int aspectType, int sequenceOfTask){
	startTime = new Date(); // Add a start_time and end_time strict preference
	aspectVal = new AspectValue(aspectType, sequenceOfTask);
	ScoringFunction scorefcn = ScoringFunction.createStrictlyAtValue(aspectVal);
	Preference pref = theLDMF.newPreference(aspectType, scorefcn);
	((NewTask) t).setPreference(pref);
    }

    /**
     * Create a CODE task.
     * @param what the direct object of the task
     */
    protected Task makeTask(Asset what, String verb) {
	NewTask new_task = theLDMF.newTask();
	new_task.setVerb(new Verb(verb));// Set the verb as given
	new_task.setPlan(theLDMF.getRealityPlan());// Set the reality plan for the task
	new_task.setDirectObject(what);

	NewPrepositionalPhrase npp = theLDMF.newPrepositionalPhrase();
	npp.setPreposition("USING_LANGUAGE");
	if (MESSAGESIZE == -1)
	    npp.setIndirectObject(alterMessageSize(0));
	else
	    npp.setIndirectObject(alterMessageSize(MESSAGESIZE));
	new_task.setPrepositionalPhrase(npp);
	return new_task;
    }

    protected void   changeTasks(Task t){
	if(CPUCONSUME != -1)  //i.e. cpuconsume passed to plugin as a arg
	    consumeCPU(CPUCONSUME);
	startTime = new Date();
	if (t.getVerb().equals(VERB))
	    sequenceNum++;
	setPreference(t, AspectType._ASPECT_COUNT, sequenceNum);
	//debug(DEBUG, FILENAME, fw,"\nManagerPlugIn::Changing task " + t.getVerb() + " with num  " 
	//    +t.getPreferredValue(AspectType._ASPECT_COUNT )); 
	publishChange(t);
    }

    protected void printTheChange(){
	Date endTime = new Date();
	long delta = endTime.getTime() - startTime.getTime();
	debug(DEBUG, FILENAME, fw, getMsgStr(count++, delta, minDelta));
    
    }

    
   
}












