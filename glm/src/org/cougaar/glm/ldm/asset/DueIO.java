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
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.glm.plugins.*;
import org.cougaar.glm.debug.*;

public class DueIO implements InventoryTask {
    Task task_ = null;

    // Did the supplier fulfill this request
    boolean filled_ = true;
    
    protected DueIO(Task request, boolean filled) {
	task_ = request;
	filled_ = filled;
    }
    
    public boolean getFilled() {
	return filled_;
    }
    
    public boolean setFilled(boolean filled) {
	filled_ = filled;
	return filled_;
    }


    public Task getTask() {
	return task_;
    }

    public void setTask(Task task) {
	task_ = task;
    }

    public String toString() {
	return new String(TaskUtils.shortTaskDesc(task_)+": Filled-"+filled_);
    }
}
