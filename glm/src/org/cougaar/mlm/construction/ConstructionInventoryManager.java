/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 */

package org.cougaar.mlm.construction;
 
import org.cougaar.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.domain.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.ConfigFileFinder;
import org.cougaar.util.UnaryPredicate;

import java.io.*;
import java.lang.*;
import java.util.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.plugins.*;
import org.cougaar.glm.plugins.inventory.*;
import org.cougaar.glm.ldm.asset.*;

/** Allocate SUPPLY tasks for construction supplies to local construction inventory
 *  (if there is any) or to the closest supplier.
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/
public class ConstructionInventoryManager extends GeneralInventoryManager {

    /** Constructor */
    public ConstructionInventoryManager(InventoryPlugIn plugin, Organization org, String type)
    {
        super(plugin, org, type);
    }

    protected int getPolicyForNextReorderDay(Task refill, int day, Inventory inventory) {
	    // don't bother to ask for a refill earlier than the reported date of the
	    //  failure
	    Allocation alloc = (Allocation)refill.getPlanElement();
	    AllocationResult ar = alloc.getReportedResult();
	    int skipDay = TimeUtils.getDaysBetween(startTime_, (long)TaskUtils.getEndTime(ar));
	    if (day <= skipDay) {
	      // the failure was for amount, not time
	      //  and we had predated the requested date so that it would arrive in time
	      skipDay += 7;
	    }
	    day = skipDay;
	    return day;
    }

  /**  Figures out time it will take (in days) to ship order.
   *   Order ship time currently hardcoded to 2.
   */
  public int getOrderShipTime() {
    return 0; // eventually check for lead time
  }

}
