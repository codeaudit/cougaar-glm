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
