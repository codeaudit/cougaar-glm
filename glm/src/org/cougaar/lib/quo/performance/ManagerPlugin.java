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
 * This COUGAAR Plugin creates and publishes "CODE" tasks and if 
 * allocationResult is a success it keeps producing more task
 * It also reads the Plugin arguments and may alter MessageSize or slurp CPU
 */
public class ManagerPlugin extends CommonUtilPlugin {

    // Two assets to use as direct objects for the CODE tasks
    protected Asset what_to_code;
    protected IncrementalSubscription allocations;   // My allocations
    protected IncrementalSubscription forceExecute;   // My allocations
    protected int CPUCONSUME, MESSAGESIZE, OUTSTANDING_MESSAGES, MAXCOUNT=1  ;
    protected String FILENAME, VERB;
    protected boolean DEBUG = false, LOG = false;
    protected int BURST_TIME=0;
    protected Date startTime;
    protected Task t, changedMind;
    protected int  sequenceNum=1, count = 1;
    protected AspectValue aspectVal;
    protected long minDelta;
    protected FileWriter fw;
    protected int expectedTask,receivedTask;
    
    /**
     * parsing the plugIn arguments and setting the values
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
		    return (t != null) && 
			(t.getVerb().equals(Verb.getVerb(VERB)));	
		}
		return false;
	    }
	};

    /**
     * Using setupSubscriptions to create the initial CODE tasks
     */
    protected void setupSubscriptions() {
	parseParameter(); //read the plugIn arguments
	debug(DEBUG, "ManagerPlugin: setupSubscription  entering loop");
	addTask();
	expectedTask = (int)t.getPreferredValue(AspectType._ASPECT_COUNT);
	allocations = (IncrementalSubscription)subscribe(myAllocationPredicate);
    }

    protected void execute () {
	// Process changed allocations
	allocateChangedtasks(allocations.getChangedList()); 
    }

    protected void  allocateChangedtasks(Enumeration allo_enum){
	AllocationResult est, rep;
	double val=0;
	Task task = null;
	
	while (allo_enum.hasMoreElements()) {
	    Allocation alloc = (Allocation)allo_enum.nextElement() ;
	    est=null; rep=null; task=null;
	    task = alloc.getTask();
	    est = alloc.getEstimatedResult();
	    rep = alloc.getReportedResult();
	    if (rep!=null){
		receivedTask= (int)rep.getValue(AspectType._ASPECT_COUNT);
		if (receivedTask != expectedTask){
		    System.out.println("ERROR: expectedtask != receivedTask::"+
				       expectedTask + ":" +receivedTask);
		}
		else {
		    printTheChange(receivedTask);
		    debug(DEBUG, task.getVerb() +
			  "=>expectedTask:received::" + expectedTask 
			  + ":"+ receivedTask);
		    waitFor(BURST_TIME);
		    for(int i = 0; i < OUTSTANDING_MESSAGES; i++) {
			// This code was written in case one has to add instead of change  
			//addTask();sequenceNum++; 
			changeTasks();
		    }
		}//else
	    }
	    breakFromLoop(count, MAXCOUNT);
	}
    }
    
    //#############HELPER FUNCTIONS
     protected void addTask() {
	publishAsset(what_to_code, "The next Killer App", "e something java");
	t = makeTask(what_to_code, VERB);
	setPreference(t, AspectType._ASPECT_COUNT, sequenceNum);
	publishAdd(t);
    }
  
    public void publishAsset(Asset asset, String nameOfAsset, 
			     String itemIdentification){
	asset = theLDMF.createPrototype("AbstractAsset", nameOfAsset);
	NewItemIdentificationPG iipg = 
	    (NewItemIdentificationPG)theLDMF.createPropertyGroup
	    ("ItemIdentificationPG");
	iipg.setItemIdentification(itemIdentification);
	asset.setItemIdentificationPG(iipg);
	publishAdd(asset); 
    }

    protected void setPreference ( Task t, int aspectType, 
				   int sequenceOfTask){
	startTime = new Date(); 
	aspectVal = AspectValue.newAspectValue(aspectType, sequenceOfTask);
	ScoringFunction scorefcn = 
	    ScoringFunction.createStrictlyAtValue(aspectVal);
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
	// Set the reality plan for the task
	new_task.setPlan(theLDMF.getRealityPlan());
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

    protected void   changeTasks(){
	if(CPUCONSUME != -1)  //i.e. cpuconsume passed to plugin as a arg
	    consumeCPU(CPUCONSUME);
	startTime = new Date();
	if (t.getVerb().equals(VERB))
	    sequenceNum++;
	setPreference(t, AspectType._ASPECT_COUNT, sequenceNum);
	publishChange(t);
	expectedTask++;
    }

    protected void printTheChange(int taskCount){
	Date endTime = new Date();
	long delta = endTime.getTime() - startTime.getTime();
	if (count == 1)
	    minDelta = delta;
	else
	    minDelta = Math.min(minDelta, delta);
	String msg=t.getVerb() +"=>"+taskCount+","+delta+","+ minDelta;
	log(LOG, FILENAME, fw, msg);
	count++;
    }
}












