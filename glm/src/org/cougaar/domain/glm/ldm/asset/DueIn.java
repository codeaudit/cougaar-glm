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

public class DueIn implements InventoryTask {
    Task duein_ = null;

    // Did the supplier fulfill this request
    boolean filled_ = true;
    
    public DueIn(Task request, boolean filled) {
	duein_ = request;
	filled_ = filled;
    }
    
    public boolean getFilled() {
	return filled_;
    }
    
    public boolean setFilled(boolean filled) {
	filled_ = filled;
// 	GLMDebug.DEBUG("DueOut", "setFilled(), Task filled value: "+filled_+", for task "+TaskUtils.shortTaskDesc(dueout_));
	return filled_;
    }


    public Task getTask() {
	return duein_;
    }

    public void setDueIn(Task task) {
	duein_ = task;
    }
}
