/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.construction;

import java.io.Serializable;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.ProjectionWeight;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

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
