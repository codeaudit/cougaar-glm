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

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.cougaar.lib.quo.performance.assets.*;
import org.cougaar.core.cluster.ChangeReport;
import org.cougaar.core.plugin.Annotation;
import java.io.*;

/**
 * This COUGAAR PlugIn subscribes to tasks in a workflow and allocates
 * the workflow sub-tasks to programmer assets.
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: DevelopmentAllocatorPlugIn.java,v 1.4 2001-10-25 16:04:47 psharma Exp $
 **/
public class DevelopmentAllocatorPlugIn extends org.cougaar.core.plugin.SimplePlugIn
{
    private IncrementalSubscription allCodeTasks;   // Tasks that I'm interested in
    private IncrementalSubscription allProgrammers;  // Programmer assets that I allocate to
    protected int CPUCONSUME=-1;
    protected int MESSAGESIZE=-1;
    protected String MESSAGE = "MESSAGE";
    protected String FILENAME="stdout";
    protected Date startTime;
    private  int count = 0;
    private long minDelta=0;
    private FileWriter fw;
    /**
     * parsing the plugIn arguments and setting the values for CPUCONSUME and MESSAGESIZE
     */
    protected void parseParameter(){
	Vector p = getParameters();
	for(int i = 0; i < p.size(); i++){
	    String s = (String)p.elementAt(i);
	    if (s.indexOf("SLURPFACTOR") != -1){
		s = s.substring(s.indexOf("=")+1, s.length());
		CPUCONSUME = Integer.parseInt(s);
	    }
	    if (s.indexOf("MESSAGESIZE") != -1){
		s = s.substring(s.indexOf("=")+1, s.length());
		MESSAGESIZE = Integer.parseInt(s);
	    }

	    if (s.indexOf("FILE") != -1){
		FILENAME = s.substring(s.indexOf("=")+1, s.length());
		System.out.println("FILENAME:  " + FILENAME);
	    }
	}
    }

    static class MyChangeReport implements ChangeReport {
	private byte[] bytes;
	MyChangeReport(byte[] bytes){
	    this.bytes = bytes;
	}

    }

     
    /**
     * Predicate matching all ProgrammerAssets
     */
    private UnaryPredicate allProgrammersPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		return o instanceof ProgrammerAsset;
	    }
	};

    /**
     * Predicate that matches all Test tasks
     */
    private UnaryPredicate codeTaskPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Task)
		    {
			Task task = (Task)o;
			return task.getVerb().equals(Verb.getVerb("CODE"));
		    }
		return false;
	    }
	};

     
    /**
     * Establish subscription for tasks and assets
     **/
    public void setupSubscriptions() {
	parseParameter();
	allProgrammers =
	    (IncrementalSubscription)subscribe(allProgrammersPredicate);
	allCodeTasks =
	    (IncrementalSubscription)subscribe(codeTaskPredicate);
    }

    /**
     * Top level plugin execute loop.  Handle changes to my subscriptions.
     **/
    public void execute() {
	//System.out.println("DevelopmentAllocatorPlugIn::execute()");
	// process new and changed tasks
	allocateTasks(allCodeTasks.getAddedList());
	//ignoring the first task;
	allocateTasks(allCodeTasks.getChangedList());
	//System.out.println("DevelopmentAllocatorPlugIn::execute " + allCodeTasks.size());
  
    }
  
    private void allocateTasks(Enumeration task_enum) {
	
	while (task_enum.hasMoreElements()) {
	  
	    Task task = (Task)task_enum.nextElement();
	    debug("DevelopmentAllocatorPlugIn: Got task from blackboard" + task);
	    startTime = new Date();
	    System.out.println("startTime: " + startTime);
	    allocateTask(task, startMonth(task));
	}
    }

    /**
     * Extract the start month from a task
     */
    private int startMonth(Task t) {
	return 0;
    }

    /**
     * Find an available ProgrammerAsset for this task.  Task must be scheduled
     * after the month "after"
     */
    private int allocateTask(Task task, int after) {
	if(CPUCONSUME != -1)  //i.e. cpuconsume passed to plugin as a arg
	    consumeCPU(CPUCONSUME);
    
	int end = after;
      
	// select an available programmer at random
	Vector programmers = new Vector(allProgrammers.getCollection());
	boolean allocated = false;
    
	while ((!allocated) && (programmers.size() > 0)) {
	    int stuckee = (int)Math.floor(Math.random() * programmers.size());
	    ProgrammerAsset asset = (ProgrammerAsset)programmers.elementAt(stuckee);
	    int duration = 3;  int earliest = 0;
	    end = earliest + duration;

	    // Create an estimate that reports that we did just what we
	    // were asked to do
	    int desired_delivery =10; //bogus
	    boolean onTime = (end <= desired_delivery);
	    int []aspect_types = {AspectType.START_TIME, AspectType.END_TIME, AspectType.DURATION};
	    double []results = {earliest, end, duration};
	    AllocationResult estAR =  theLDMF.newAllocationResult(1.0, onTime,aspect_types,results);
	    debug("DevelopmentAllocatorPlugIn: Creating AllocationResult");
	    ChangeReport cr = null;
	    if (MESSAGESIZE != -1)
		cr = new MyChangeReport(alterMessageSize(MESSAGESIZE));
	    else 
		cr = new MyChangeReport(alterMessageSize(0));

	    PlanElement pe = task.getPlanElement(); //Allocation  planElement
	    if (pe == null) {
		pe = theLDMF.createAllocation(task.getPlan(), task,
					      asset, estAR, Role.ASSIGNED);
		publishAdd(pe);
		debug(" DevelopmentAllocatorPlugIn: Adding AllocationREsult in response to first task");
	    } else {
		pe.setEstimatedResult(estAR);
		publishChange(pe, Collections.singleton(cr));
		debug("DevelopmentAllocatorPlugIn: Adding AllocationREsult in response to changed task");
	    }
	      Date endTime = new Date();
	            long delta = endTime.getTime() - startTime.getTime();
	            count++;
	            if (count == 1)
	      	  minDelta = delta;
	            else
	      	  minDelta = Math.min(minDelta, delta);
	      debug(count, delta, minDelta);

	    allocated = true;
	}
	return end;
    }


    /**
     * Writes the data to the specified FILENAME
     */
    private void debug(int count, long delta, long minDelta){
	//creates a file handle based on the arguments after parsing
	debug(count + "," + delta + "," + minDelta); 
       
    }
    private void debug(String str) {
	 try {
	    if (FILENAME.equals("stdout")){
		//fw = new FileWriter(FileDescriptor.out); //not working!!!!!!!
		System.out.println("Developer: " + str);
	    }
	    else{
		fw = new FileWriter(FILENAME, true);
		fw.write("Developer: " + str);
		fw.close();
	    }
	}catch (IOException ie) {
	    ie.printStackTrace();
	}
    }
    
  
    /**
     * consume CPU cycles by the argument passed as  parameter
     */
    private void consumeCPU(int cyclesConsumed) {
	//Just using CPU computations    
	int slurp = 0;
	for(int i= 0; i < cyclesConsumed; i++){
	    slurp++;
	}
    }
   

    /**
     * Changing message size  by the argument passed as  parameter
     */
      
    /**
     * Changing message size  by the argument passed as  parameter
     */
    private byte[] alterMessageSize(int val) {
	//Byte b = new Byte (Byte.MAX_VALUE);
	byte[] bytes = new byte[val];
	for(int i = 0; i < val; i++) bytes[i] = 42;
    
	return bytes;
    }
   
}










