/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
//package org.cougaar.lib.quo.performance;
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
 * @version $Id: DevelopmentAllocatorPlugIn.java,v 1.6 2001-11-01 16:57:24 psharma Exp $
 **/
public class DevelopmentAllocatorPlugIn extends CommonUtilPlugIn
{
    private IncrementalSubscription allCodeTasks;   // Tasks that I'm interested in
    private IncrementalSubscription allProgrammers;  // Programmer assets that I allocate to

    protected int CPUCONSUME;
    protected int MESSAGESIZE;
    protected String FILENAME;
    protected int MAXCOUNT;
    protected int  OUTSTANDING_MESSAGES;
    protected String VERB;
    
    protected boolean DEBUG = false;

    protected Date startTime;
    private  int count = 0;
    private long minDelta=0;
    private FileWriter fw;

    private AspectValue allocAspectVal;
    double allocNum = 0;

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
	VERB=getParameterValue(p, "VERB");
	System.out.println("parseParameter:  " + VERB);
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
			return task.getVerb().equals(Verb.getVerb(VERB));
		    }
		return false;
	    }
	};

     
    /**
     * Establish subscription for tasks and assets
     **/
    public void setupSubscriptions() {
	parseParameter();
	allProgrammers = (IncrementalSubscription)subscribe(allProgrammersPredicate);
	allCodeTasks =  (IncrementalSubscription)subscribe(codeTaskPredicate);
    }

    /**
     * Top level plugin execute loop.  Handle changes to my subscriptions.
     **/
    public void execute() {
	allocateTasks(allCodeTasks.getAddedList());
	//ignoring the first task;
	allocateTasks(allCodeTasks.getChangedList());
    }
  
    private void allocateTasks(Enumeration task_enum) {
	while (task_enum.hasMoreElements()) {
	    Task task = (Task)task_enum.nextElement();
	    //debug(DEBUG, FILENAME, fw, "DevelopmentAllocatorPlugIn: Got task from blackboard.." 
	    //+ task.getPreferredValue(AspectType._ASPECT_COUNT ) + " with verb " + task.getVerb());
	    startTime = new Date();
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
    
	int end = after;    int duration = 3;  int earliest = 0;
	end = earliest + duration;  int desired_delivery =10; //bogus
	boolean onTime = (end <= desired_delivery);
        
	// select an available programmer at random
	Vector programmers = new Vector(allProgrammers.getCollection());
	boolean allocated = false;
    	while ((!allocated) && (programmers.size() > 0)) {
	    int stuckee = (int)Math.floor(Math.random() * programmers.size());
	    ProgrammerAsset asset = (ProgrammerAsset)programmers.elementAt(stuckee);
	    
	    // Create an estimate that reports that we did just what we  were asked to do
	    allocNum =  task.getPreferredValue(AspectType._ASPECT_COUNT );
	    int []aspect_types = {AspectType._ASPECT_COUNT};
	    double []results = {allocNum};
	    AllocationResult estAR =  theLDMF.newAllocationResult(1.0, onTime,aspect_types, results  );

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
	    } else {
		pe.setEstimatedResult(estAR);
		publishChange(pe, Collections.singleton(cr));
	    }
	    publishRemove(task);
	    printTheChange();
	    allocated = true;
	    breakFromLoop(count, MAXCOUNT);
	}

	return end;
    }

    protected void printTheChange(){
	Date endTime = new Date();
	long delta = endTime.getTime() - startTime.getTime();
	debug(DEBUG, FILENAME, fw, getMsgStr(count++, delta, minDelta));
    
    }
   
 
}











