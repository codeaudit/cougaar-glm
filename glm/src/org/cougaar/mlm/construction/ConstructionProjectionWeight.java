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

package org.cougaar.mlm.construction;

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import java.io.Serializable;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.ProjectionWeight;
import org.cougaar.glm.plugins.DecorationPlugin;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.glm.plugins.TaskUtils;

public class ConstructionProjectionWeight implements ProjectionWeight, Serializable {


    protected int switchOverDay_ = 0;

    public ConstructionProjectionWeight(int day) {
	    switchOverDay_ = day;
    }
    public ConstructionProjectionWeight() {}

    public void setSwitchOverDay(int day) {
	    switchOverDay_ = day;
    }

    public double getProjectionWeight(Task task, int imputedDay) {
      // because ProjectSupply tasks span a long time period, their start time is not
	    //  useful for deteriming when to credit them -- for withdrawals, we have created
	    //  ProjectWithdraw tasks, but for refill tasks we are passing in the raw ProjectSupply
	    //  tasks (this is a lonnng story)
	    Verb task_verb = task.getVerb();
	    int day = imputedDay;
	    double weight = 0.0;

	    if (task_verb.equals(Constants.Verb.SUPPLY)) {
        if(day > getRefillSwitchoverDay()){
		       weight = 0.0;
		     } else {
		       weight = 1.0;
		     }
	    } else if (task_verb.equals(Constants.Verb.PROJECTSUPPLY)) {
		    if(day > getRefillSwitchoverDay()){
		      weight = 1.0;
		    } else {
		      weight = 0.0;
		    }
	    } else if (task_verb.equals(Constants.Verb.PROJECTWITHDRAW)) {
		    if (day > getRefillSwitchoverDay()) {
		      weight = 1.0;
		    } else {
		      weight = 0.0;
		    }
	    } else if (task_verb.equals(Constants.Verb.WITHDRAW)) {
		    if (day > getRefillSwitchoverDay()) {
		      weight = 0.0;
		    } else {
		      weight = 1.0;
		    }
	    }
	    return weight;
    }


    public int getRefillSwitchoverDay(){
	    // eventually should be the lead-time from the allocation result for projections
	    return 100;
	    //switchoverDay*5;
    }

}
