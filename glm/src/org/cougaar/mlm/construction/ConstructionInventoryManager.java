/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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
 */

package org.cougaar.mlm.construction;
 
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.glm.plugins.inventory.GeneralInventoryManager;
import org.cougaar.glm.plugins.inventory.InventoryPlugin;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Task;

/** Allocate SUPPLY tasks for construction supplies to local construction inventory
 *  (if there is any) or to the closest supplier.
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/
public class ConstructionInventoryManager extends GeneralInventoryManager {

    /** Constructor */
    public ConstructionInventoryManager(InventoryPlugin plugin, Organization org, String type)
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
