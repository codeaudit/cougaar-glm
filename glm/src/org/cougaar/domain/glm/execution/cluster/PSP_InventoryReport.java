package org.cougaar.domain.glm.execution.cluster;

import org.cougaar.domain.glm.execution.common.*;

import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.lib.planserver.KeepAlive;
import org.cougaar.lib.planserver.UISubscriber;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.ServerPlugInSupport;
import org.cougaar.lib.planserver.UseDirectSocketOutputStream;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.glm.asset.Inventory;
import org.cougaar.domain.glm.asset.ReportSchedulePG;
import org.cougaar.domain.glm.asset.ScheduledContentPG;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.glm.plan.QuantityScheduleElement;
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
    Collection c = psc.getServerPlugInSupport().queryForSubscriber(new UnaryPredicate() {
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
