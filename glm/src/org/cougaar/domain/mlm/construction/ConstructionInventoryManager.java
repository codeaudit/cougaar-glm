/*
 * <copyright>
 *  Copyright 1997-2001 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.construction;
 
import org.cougaar.*;
import org.cougaar.core.cluster.*;
import org.cougaar.domain.planning.ldm.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.util.ConfigFileFinder;
import org.cougaar.util.UnaryPredicate;

import java.io.*;
import java.lang.*;
import java.util.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.plugins.inventory.*;
import org.cougaar.domain.glm.ldm.asset.*;

/** Allocate SUPPLY tasks for construction supplies to local construction inventory
 *  (if there is any) or to the closest supplier.
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: ConstructionInventoryManager.java,v 1.3 2001-05-16 21:12:27 mdavis Exp $
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
