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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */

package org.cougaar.lib.quo.performance;
//package org.cougaar.lib.quo;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.UnaryPredicate;

/**
 * Adds multiple task and removes them after allocation result for the 
 * previous task arrives
 */
public class SimpleAddRescindPlugin extends CommonUtilPlugin {

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
    protected boolean LOG = false;
    protected int BURST_TIME=0;
    protected  String VERB;//="CODE1";

    private Date startTime;
    private Task t, changedMind;
    private int  sequenceNum=1;
    private AspectValue aspectVal;

    private  int count = 1;
    private long minDelta;
  
    private FileWriter fw;
    //    private double lastReceived=0;

    private int wakeUpCount, taskAllocationCount;
    private int expectedTask;
    private int receivedTask;

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
	LOG=getParameterBooleanValue(p, "LOG");
	BURST_TIME=getParameterIntValue(p, "BURST_TIME");
	VERB=getParameterValue(p, "VERB");
    }
   
    
    public  UnaryPredicate myAllocationPredicate = new UnaryPredicate() {
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
	for(int i = 0; i < OUTSTANDING_MESSAGES; i++) {
	    // System.out.println("enetring loop");
	    addTask();
	}
	allocations = (IncrementalSubscription)subscribe(myAllocationPredicate);
    }

    /**
     * This Plugin has no subscriptions so this method does nothing
     */
    protected void execute () {
	wakeUpCount++;
	
	System.out.println("....Wokenup @"+ System.currentTimeMillis() + "   wakeUpcount: " + wakeUpCount);
	//publishRemove(t);
	allocateChangedtasks(allocations.getChangedList()); // Process changed allocations
    }

    protected void addTask() {
	publishAsset(what_to_code, "The next Killer App", "e something java");
	t = makeTask(what_to_code, VERB);
	setPreference(t, AspectType._ASPECT_COUNT, sequenceNum);
	
	publishAdd(t);
		
    }
  
    public void publishAsset(Asset asset, String nameOfAsset, String itemIdentification){
	asset = theLDMF.createPrototype("AbstractAsset", nameOfAsset);
	NewItemIdentificationPG iipg = 
	    (NewItemIdentificationPG)theLDMF.createPropertyGroup("ItemIdentificationPG");
	iipg.setItemIdentification(itemIdentification);
	asset.setItemIdentificationPG(iipg);
	publishAdd(asset); 
    }

    
    protected void  allocateChangedtasks(Enumeration allo_enum){
	
	
	
	 AllocationResult est, rep;
	double val=0;
	//double arr[] = null;
	//double received = 0;
	while (allo_enum.hasMoreElements()) {
	    taskAllocationCount++;
	    int taskCount = (int)t.getPreferredValue(AspectType._ASPECT_COUNT);
	    debug(DEBUG,"ManagerPlugin:allocateChangedTasks ....taskAllocationCount.." + 
		  taskAllocationCount + " for task# " + taskCount);
	    Allocation alloc = (Allocation)allo_enum.nextElement() ;
	    est=null; rep=null;
	    est = alloc.getEstimatedResult();
	    rep = alloc.getReportedResult();
	    if (rep!=null){
		//debug(DEBUG, FILENAME, fw,"ManagerPlugin:allocateChangedTasks ........" + received);
		receivedTask= (int)t.getPreferredValue(AspectType._ASPECT_COUNT);
		affert(expectedTask, receivedTask);
		printTheChange();
		debug(DEBUG, t.getVerb() + "=>expectedTask:received::" + expectedTask + ":"+ receivedTask);
		waitFor(BURST_TIME);
		for(int i = 0; i < OUTSTANDING_MESSAGES; i++) {
		    //addTask();
		    //sequenceNum++; 
		    changeTasks(t);
		    publishRemove(t);
		}
	    }
	    breakFromLoop(count, MAXCOUNT);
	}
	//lastReceived = received;
    }
    
    protected void setPreference ( Task t, int aspectType, int sequenceOfTask){
	startTime = new Date(); // Add a start_time and end_time strict preference
	aspectVal = AspectValue.newAspectValue(aspectType, sequenceOfTask);
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
	//debug(DEBUG, FILENAME, fw,"\nManagerPlugin::Changing task " + t.getVerb() + " with num  " 
	//  +t.getPreferredValue(AspectType._ASPECT_COUNT )); 
	publishChange(t);
	expectedTask++;
    }

    protected void printTheChange(){
	Date endTime = new Date();
	long delta = endTime.getTime() - startTime.getTime();
	if (count == 1)
	    minDelta = delta;
	else
	    minDelta = Math.min(minDelta, delta);
	int taskCount = (int)t.getPreferredValue(AspectType._ASPECT_COUNT);
	String msg=t.getVerb() +"=>"+taskCount+","+delta+","+ minDelta;
	log(LOG, FILENAME, fw, msg);
	count++;
    }

  // renamed to "affert" from "assert".  Better option would be to use the real jdk1.4 assert
  // functionality once we've got a full set of complient compilers.
    protected void affert( int expectedTask, int receivedTask){
	    if (expectedTask != receivedTask){
		System.out.println("ERROR!!!!! expectedTask is different from receivedTask");
		
	    }
	}


   }












