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

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;
import java.util.*;

// Simple plugin that says 'yes' to any task fed to it
// Optionally, if arguments are given, will only allocate tasks with given verbs
public class UniversalAllocatorPlugIn extends SimplePlugIn {

    private IncrementalSubscription allTasks;
    private UnaryPredicate allTasksPredicate = new UnaryPredicate() {
	public boolean execute(Object o) { return o instanceof Task; }
    };

    public void setupSubscriptions()
    {
      //System.out.println("In UniversalAllocatorPlugin.setupSubscriptions");
	// Subscribe for all tasks
	allTasks = (IncrementalSubscription)subscribe(allTasksPredicate);

	sink_asset = theLDMF.createPrototype("AbstractAsset", "UniversalSink");
	publishAdd(sink_asset);
    }

    // Is this a task we're interested in? Either we didn't specify a verb,
    // Or the task has a verb among those specified
    private boolean isInterestingTask(Task task) 
    {
	String verb = task.getVerb().toString();
	Vector params = getParameters();
	if (params.size() == 0) return true;
	for(Enumeration e = params.elements();e.hasMoreElements();) {
	    String param = (String)e.nextElement();
	    if (param.equals(verb)) return true;
	}
	return false;
    }

    public void execute() 
    {
      //	System.out.println("In UniversalAllocatorPlugin.execute");

	for(Enumeration e = allTasks.getAddedList();e.hasMoreElements();) 
	    {
		Task task = (Task)e.nextElement();

		if (!isInterestingTask(task))
		    continue;

		AllocationResult allocation_result = computeAllocationResult(task);

		// Allocate task to sink_asset
		Allocation allocation = 
		    theLDMF.createAllocation(theLDMF.getRealityPlan(),
					     task,
					     sink_asset,
					     allocation_result,
					     Role.BOGUS);
		//		System.out.println("Allocating Task " + task + " to " + allocation);
		publishAdd(allocation);
	    }
    }

    // Return an allocation result that gives back a successful/optimistic answer
    // consisting of the best value for every aspect
    private AllocationResult computeAllocationResult(Task task) 
    {
	int num_prefs = 0;
	Enumeration prefs = task.getPreferences();
	while(prefs.hasMoreElements()) {prefs.nextElement(); num_prefs++; }

	int []types = new int[num_prefs];
	double []results = new double[num_prefs];
	prefs = task.getPreferences();

	int index = 0;
	while(prefs.hasMoreElements()) {
	    Preference pref = (Preference)prefs.nextElement();
	    types[index] = pref.getAspectType();
	    results[index] = pref.getScoringFunction().getBest().getValue();
	    //	    System.out.println("Types[" + index + "]= " + types[index] + 
	    //			       " Results[" + index + "]= " + results[index]);
	    index++;
	}

	AllocationResult result = theLDMF.newAllocationResult(1.0, // Rating,
							      true, // Success,
							      types,
							      results);
	return result;
    }

    // Create a single dummy asset to which to allocate all appropriate tasks
    private Asset sink_asset = null;
    
}
