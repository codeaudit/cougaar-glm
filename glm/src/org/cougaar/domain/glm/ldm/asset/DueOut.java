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
package org.cougaar.domain.glm.ldm.asset;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.debug.*;

public class DueOut implements InventoryTask {
    Task dueout_ = null;
    // Last epoch, did we successfull fulfill this request
    boolean previouslyFilled_ = true;
    // Did we fulfill the request this run
    boolean filled_ = true;
    
    public DueOut(Task request, boolean filled) {
	dueout_ = request;
	previouslyFilled_ = filled;
    }
    
    public boolean getFilled() {
	return filled_;
    }
    
    public boolean setFilled(boolean filled) {
	filled_ = filled;
// 	GLMDebug.DEBUG("DueOut", "setFilled(), Task filled value: "+filled_+", for task "+TaskUtils.shortTaskDesc(dueout_));
	return filled_;
    }

    public boolean getPreviouslyFilled() {
	return previouslyFilled_;
    }

    public Task getTask() {
	return dueout_;
    }

    public void setDueOut(Task task) {
	dueout_ = task;
    }

    public String toString() {
	return new String(TaskUtils.shortTaskDesc(dueout_)+": Filled-"+filled_+
			  ", Previously Filled-"+previouslyFilled_);
    }
}
