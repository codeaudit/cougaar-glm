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

public class DueOut extends DueIO implements InventoryTask {
    boolean previouslyFilled_;
    int day_;
    
    public DueOut(Task request, boolean filled, int day) {
        super(request, TaskUtils.isProjection(request) ? true : filled);
        if (day < 0) throw new IllegalArgumentException("Negative dueout day = " + day);
        day_ = day;
        previouslyFilled_ = filled;
    }

    public boolean getPreviouslyFilled() {
        return previouslyFilled_;
    }

    public void setDueOut(Task task) {
	setTask(task);
    }

    public int getDay() {
        return day_;
    }
}
