/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBNT Solutions LLC,
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 2000 by
 *             BBNT Solutions LLC,
 *             all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import java.io.Serializable;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.ProjectionWeight;
import org.cougaar.glm.plugins.DecorationPlugIn;
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
