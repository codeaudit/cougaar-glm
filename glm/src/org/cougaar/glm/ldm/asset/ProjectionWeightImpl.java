/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.ldm.asset;

import java.io.Serializable;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

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
