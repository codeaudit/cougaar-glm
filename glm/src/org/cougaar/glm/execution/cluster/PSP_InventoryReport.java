/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.cluster;

import org.cougaar.glm.execution.common.*;

import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.lib.planserver.KeepAlive;
import org.cougaar.lib.planserver.UISubscriber;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.ServerPluginSupport;
import org.cougaar.lib.planserver.UseDirectSocketOutputStream;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.ReportSchedulePG;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;

/**
 * Retrieves information from the logplan for inventory items and sends back an inventory report.
 **/
public class PSP_InventoryReport
  extends PSP_Base
  implements PlanServiceProvider
{
  public PSP_InventoryReport() throws RuntimePSPException {
    super();
  }

  public PSP_InventoryReport(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  public PlanServiceProvider pspClone() throws RuntimePSPException {
    return new PSP_InventoryReport();
  }

  /**
   * This PSP is referenced directly (in the URL from the client) and
   * hence this shouldn't be called.
  **/
  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false;
  }

  protected Context createContext() {
    return new MyContext();
  }

  protected static class MyContext extends Context {

  public void execute() {
    try {
      InventoryReportParameters irp = (InventoryReportParameters) reader.readEGObject();
      Inventory inventory = getInventory(irp.theItemIdentification, psc);
      System.out.println("PSP_InventoryReport " + inventory);
      if (inventory != null) {
	  ScheduledContentPG sched_content_pg = inventory.getScheduledContentPG();
	  if (sched_content_pg != null) {
	      Schedule sched = sched_content_pg.getSchedule();
	      if (sched != null) {
		  Collection c = sched.getScheduleElementsWithTime(irp.theReportDate);
		  double quantity = 0.0;
		  if (c.size() > 0) {
		      quantity = ((QuantityScheduleElement) c.iterator().next()).getQuantity();
		  }
		  InventoryReport report = new InventoryReport(irp.theItemIdentification,
							       irp.theReportDate,
							       irp.theReportDate,
							       quantity);
		  writer.writeEGObject(report);
		  writer.flush();
	      }
	  }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Inventory getInventory(final String theItemIdentification, PlanServiceContext psc) {
    Collection c = psc.getServerPluginSupport().queryForSubscriber(new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Inventory) {
          Inventory inventory = (Inventory) o;
          ItemIdentificationPG iipg = inventory.getItemIdentificationPG();
          if (iipg != null) {
            String id = iipg.getItemIdentification();
            return id.equals(theItemIdentification);
          }
        }
        return false;
      }
    });
    if (c.size() > 0) return (Inventory) c.iterator().next();
    return null;
  }
}
}
