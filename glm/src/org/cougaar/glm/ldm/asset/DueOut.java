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
