/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects Agency (DARPA)
 * and ALPINE (A BBN Technologies (BBN) and Raytheon Systems Company
 * (RSC) Consortium). This software to be used in accordance with the
 * COUGAAR license agreement.  The license agreement and other
 * information on the Cognitive Agent Architecture (COUGAAR) Project can
 * be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */

package org.cougaar.domain.mlm.construction;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import java.io.Serializable;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.ProjectionWeight;
import org.cougaar.domain.glm.plugins.DecorationPlugIn;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.glm.plugins.TaskUtils;

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
