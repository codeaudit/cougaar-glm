/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import java.io.Serializable;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.ProjectionWeight;
import org.cougaar.glm.plugins.DecorationPlugin;
import org.cougaar.glm.plugins.TaskUtils;

public class ProjectionWeightImpl implements ProjectionWeight, Serializable {

    protected int wdrawSwitchOverDay_ = 60;
    protected int refillSwitchOverDay_ = 60;
    protected boolean noProjections_ = false;

    public ProjectionWeightImpl(boolean turnOffProjections) {
	noProjections_ = turnOffProjections;
    }

    public ProjectionWeightImpl(int wdrawSwitchOverDay,  int refillSwitchOverDay, boolean turnOffProjections) {
	wdrawSwitchOverDay_ = wdrawSwitchOverDay;
	refillSwitchOverDay_ = refillSwitchOverDay;
	noProjections_ = turnOffProjections;
    }

    public ProjectionWeightImpl() {}

    public double getProjectionWeight(Task task, int imputedDay) {
	// because ProjectSupply tasks span a long time period, their start time is not
	//  useful for deteriming when to credit them -- for withdrawals, we have created
	//  ProjectWithdraw tasks, but for refill tasks we are passing in the raw ProjectSupply
	//  tasks (this is a lonnng story)
	Verb task_verb = task.getVerb();
	int day = imputedDay;
	double weight = 0.0;
	
	if (noProjections_) {
	    return getSupplyTaskWeight(task);
	} else {
	    if (task_verb.equals(Constants.Verb.SUPPLY)) {
		if(day > refillSwitchOverDay_){
		    weight = 0.0;
		} else {
		    weight = 1.0;
		}
	    } else if (task_verb.equals(Constants.Verb.PROJECTSUPPLY)) {
		if(day > refillSwitchOverDay_){
		    weight = 1.0;
		} else {
		    weight = 0.0;
		}
	    } else if (task_verb.equals(Constants.Verb.PROJECTWITHDRAW)) {
	        // Count ProjectSupply tasks if beyond the refill switchover day OR task came from Demand Projector
	        PrepositionalPhrase pp =task.getPrepositionalPhrase(Constants.Preposition.REFILL);
		if ((day > wdrawSwitchOverDay_) || (pp == null)) {
		    weight = 1.0;
		} else {
		    weight = 0.0;
		}
	    } else if (task_verb.equals(Constants.Verb.WITHDRAW)) {
		if (day > wdrawSwitchOverDay_) {
		    weight = 0.0;
		} else {
		    weight = 1.0;
		}
	    }
	}
	return weight;
    }

    public double getSupplyTaskWeight(Task task) {
	// If only Supply tasks are expected in the System then a switchOver day does not make sense.
	// Weight Supply/Refill tasks as 1 any other task as 0
	Verb task_verb = task.getVerb();
	double weight = 0.0;
	if (task_verb.equals(Constants.Verb.SUPPLY) ||
	    task_verb.equals(Constants.Verb.WITHDRAW)) {
	    weight = 1.0;
	}
	return weight;
    }

    public String toString() {
         return getClass().getName()
              + "[wdrawSwitchoverDay="
              + wdrawSwitchOverDay_
              + ", refillSwitchoverDay="
              + refillSwitchOverDay_
              + "]";
    }
}
