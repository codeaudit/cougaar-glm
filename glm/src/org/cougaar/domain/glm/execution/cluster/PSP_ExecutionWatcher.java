package org.cougaar.domain.glm.execution.cluster;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.measure.FlowRate;
import org.cougaar.domain.planning.ldm.measure.CountRate;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.predicate.TaskPredicate;
import org.cougaar.domain.planning.ldm.predicate.PlanElementPredicate;
import org.cougaar.lib.planserver.KeepAlive;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.UISubscriber;
import org.cougaar.util.UnaryPredicate;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Inventory;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.ReportSchedulePG;
import org.cougaar.domain.glm.execution.common.*;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.AssetUtils;

/**
 * Watches the logplan for Inventory assets with an
 * ExecutionReportSchedulePG. Sends the schedule information to the
 * EventGenerator. The EventGenerator queries this PSP for
 * InventoryStatus at the scheduled times.
 **/
public class PSP_ExecutionWatcher
  extends PSP_Base
  implements PlanServiceProvider, KeepAlive
{
  public PSP_ExecutionWatcher() throws RuntimePSPException {
    super();
  }

  public PSP_ExecutionWatcher(String pkg, String id) throws RuntimePSPException {
    super(pkg, id);
  }

  public PlanServiceProvider pspClone() throws RuntimePSPException {
    return new PSP_ExecutionWatcher();
  }

    public String getConnectionACKMessage() {
        return null;
    }

  protected Context createContext() {
    return new MyContext();
  }

  static private class MyContext extends Context implements UISubscriber {
  private long nextTimeUpdate = 0L; // Force first time update immediately
  private long theTimeStep = 10000L; // The minimum (real) time between non-time rate updates
  private double theExecutionRate = -1.0; // Insure not equal
  private ArrayList schedules = new ArrayList();
  private ArrayList scheduleRescinds = new ArrayList();
  private ArrayList failureConsumptionRates = new ArrayList();
  private ArrayList failureConsumptionRateRescinds = new ArrayList();
  private ArrayList taskEventReports = new ArrayList();
  private ArrayList taskEventReportRescinds = new ArrayList();
  private IncrementalSubscription inventorySubscription;
  private IncrementalSubscription projectionTaskSubscription;
  private IncrementalSubscription peSubscription;
  private IncrementalSubscription orgSubscription;
  private ArrayList changes = new ArrayList();
  private boolean timeStatusOnly;

  private static class Change extends Vector {
    public IncrementalSubscription subscription;
    public static final int ADD = 0;
    public static final int CHANGE = 1;
    public static final int REMOVE = 2;
    public int kind;
    public Change(IncrementalSubscription subscription,
                  Enumeration enumeration,
                  int kind)
    {
      this.subscription = subscription;
      this.kind = kind;
      while (enumeration.hasMoreElements()) {
        add(enumeration.nextElement());
      }
    }
  }

  private static UnaryPredicate inventoryPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Inventory) {
        Inventory inventory = (Inventory) o;
        ReportSchedulePG rspg = inventory.getReportSchedulePG();
        if (rspg != null && rspg.getBase() != null) {
          ItemIdentificationPG iipg = inventory.getItemIdentificationPG();
          if (iipg != null) {
            String id = iipg.getItemIdentification();
            if (id != null)  {
              return true;
            }
          }
        }
      }
      return false;
    }
  };

  /**
   * Find plan elements with allocation result for tasks having
   * observable aspects.
   **/
  private static UnaryPredicate pePredicate = new PlanElementPredicate() {
    public boolean execute(PlanElement pe) {
      Task task = pe.getTask();
      return task.getObservableAspects().hasMoreElements();
    }
  };

  private static UnaryPredicate projectionTaskPredicate = new TaskPredicate() {
    public boolean execute(Task task) {
      if (task.getVerb().equals(Constants.Verb.ProjectSupply)) {
        return (task.getPrepositionalPhrase(Constants.Preposition.MAINTAINING) != null);
      }
      return false;
    }
  };

  private static UnaryPredicate orgPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Organization);
    }
  };

  // Implementation of UISubscriber

  public synchronized void subscriptionChanged(Subscription subscription) {
    IncrementalSubscription is = (IncrementalSubscription) subscription;
    changes.add(new Change(is, is.getAddedList(), Change.ADD));
    changes.add(new Change(is, is.getChangedList(), Change.CHANGE));
    changes.add(new Change(is, is.getRemovedList(), Change.REMOVE));
  }

  private void inventorySubscriptionChanged(Enumeration enum, int kind) {
    while (enum.hasMoreElements()) {
      Inventory inventory = (Inventory) enum.nextElement();
      ItemIdentificationPG iipg = inventory.getItemIdentificationPG();
      ReportSchedulePG rspg = inventory.getReportSchedulePG();
      String id = iipg.getItemIdentification();
      switch (kind) {
      case Change.ADD:
        schedules.add(new InventoryReportSchedule(id, rspg));
        break;
      case Change.CHANGE:
        break;                  // Ignore changes they never affect the report schedule
      case Change.REMOVE:
        scheduleRescinds.add(new InventoryReportSchedule.Rescind(id));
        break;
      }
      notify();
    }
  }

  /**
   * For each task in the enumeration, this method creates a
   * TaskEventReport for each observable aspect of the task. For
   * debugging, we hoke up the observable aspect enumeration to
   * include start time, end time, and quantity.
   **/
  private void peSubscriptionChanged(Enumeration enum, int kind) {
    while (enum.hasMoreElements()) {
      PlanElement pe = (PlanElement) enum.nextElement();
      AllocationResult ar = pe.getEstimatedResult();
      if (ar == null) continue; // No result to observe yet. Catcha latah.
      Task task = pe.getTask();
      Enumeration observableAspects = task.getObservableAspects();
//        //+++++++++++ debug +++++++++++++//
//        observableAspects = new Vector(Arrays.asList(new Integer[] {
//          new Integer(AspectType.END_TIME),
//          new Integer(AspectType.QUANTITY),
//        })).elements();
//        //+++++++++++ debug +++++++++++++//
      while (observableAspects.hasMoreElements()) {
        int aspectType = ((Integer) observableAspects.nextElement()).intValue();
        if (kind == Change.REMOVE) {
          taskEventReportRescinds.add(new TaskEventReport.Rescind(new TaskEventId(task.getUID(), aspectType)));
        } else {
          AllocationResult obsAR = pe.getObservedResult();
          if (obsAR != null) {
            double obsValue = TaskUtils.getARAspectValue(obsAR, aspectType);
            if (obsValue != Double.NaN) {
              continue;         // Already have observed value, don't send this again
            }
          }
          double value = TaskUtils.getARAspectValue(ar, aspectType);
          if (Double.isNaN(value)) {
            System.out.println("Allocation result has NaN for " + aspectType + ": " + task);
            continue;           // Ignore
          }
//            double value = TaskUtils.getPreferenceBestValue(task, aspectType);
          long observationTime = (long) ((aspectType == AspectType.START_TIME) ?
                                         TaskUtils.getStartTime(ar) :
                                         TaskUtils.getEndTime(ar));
          String shortDescription =
            task.getVerb() + " " + task.getDirectObject().toString();
          taskEventReports.add(new TaskEventReport(new TaskEventId(task.getUID(), aspectType),
                                                   task.getVerb().toString(),
                                                   value,
                                                   observationTime,  // reportDate
                                                   observationTime,  // receivedDate
                                                   task.toString(),
                                                   shortDescription));
        }
      }
      notify();
    }
  }


  private void projectionTaskSubscriptionChanged(Enumeration enum, int kind) {
    while (enum.hasMoreElements()) {
      Task task = (Task) enum.nextElement();
      PrepositionalPhrase pp;
      pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
      String orgName = (String) pp.getIndirectObject();
      boolean isLocal = false;
      for (Enumeration orgs = orgSubscription.elements(); orgs.hasMoreElements(); ) {
        Organization org = (Organization) orgs.nextElement();
        if (org.isSelf()) {
          if (org.getItemIdentificationPG().getItemIdentification().equals(orgName)) {
            isLocal = true;
            break;
          }
        }
      }
      if (isLocal) {
        pp = task.getPrepositionalPhrase(Constants.Preposition.MAINTAINING);
        Asset asset = (Asset) pp.getIndirectObject();
        if (asset instanceof Inventory) continue;
        if (kind == Change.REMOVE) {
          FailureConsumptionRate.Rescind fcrr =
            new FailureConsumptionRate.Rescind(task.getUID());
          failureConsumptionRateRescinds.add(fcrr);
        } else {
          Rate rate = TaskUtils.getRate(task);
          String rateUnits = null;
          double rateValue = 0.0;
          if (rate instanceof CountRate) {
            rateValue = ((CountRate) rate).getValue(CountRate.EACHES_PER_DAY);
            rateUnits = "Eaches/Day";
          } else if (rate instanceof FlowRate) {
            rateValue = ((FlowRate) rate).getValue(FlowRate.GALLONS_PER_DAY);
            rateUnits = "Gallons/Day";
          }

          double multiplier = TaskUtils.getMultiplier(task);
          if (rateUnits != null) {
            FailureConsumptionRate fcr =
              new FailureConsumptionRate(task.getUID(),
                                         TaskUtils.getDirectObjectID(task),
					 AssetUtils.getPartNomenclature(task.getDirectObject()),
                                         TaskUtils.getStartTime(task),
                                         TaskUtils.getEndTime(task),
                                         rateValue,
                                         rateUnits,
                                         multiplier,
                                         asset.getTypeIdentificationPG().getTypeIdentification(),
                                         asset.getTypeIdentificationPG().getNomenclature());
            failureConsumptionRates.add(fcr);
          }
        }
        notify();
      } else {
      }
    }
  }

  /**
   * New org added. We can ignore this because a projection task can't
   * be created until after its organization has been added so we will
   * never see things in the reverse order.
   **/
  private void orgSubscriptionChanged(Enumeration enum, int kind) {
  }          

  protected void execute() throws IOException, InterruptedException {
    try {
      EGObject object = reader.readEGObject();
      if (object instanceof ExecutionWatcherParameters) {
	  ExecutionWatcherParameters params=(ExecutionWatcherParameters)object;
	  theTimeStep = params.theTimeStep;
	  timeStatusOnly = params.timeStatusOnly;
      } else {
        System.err.println("ExecutionWatcher: Unexpected parameter type: " + object);
      }
    } catch (IOException ioe) {
      //Normal termination is eof
    }
    if (theTimeStep < 10000L) theTimeStep = 10000L;

    if(!timeStatusOnly) {
	inventorySubscription = (IncrementalSubscription) sps.subscribe(this, inventoryPredicate);
	projectionTaskSubscription =
	    (IncrementalSubscription) sps.subscribe(this, projectionTaskPredicate);
	peSubscription = (IncrementalSubscription) sps.subscribe(this, pePredicate);
	orgSubscription = (IncrementalSubscription) sps.subscribe(this, orgPredicate);	
    }

    maybeSendExecutionTimeStatus(true);
    while (true) {
      try {
        synchronized (this) {
          if (changes.size() == 0){
            wait(2000L);          // Wait for notify, but at most 2 seconds
          }
          if (changes.size() > 0) {
            sendChanges();               // Send changes
          }
        }
        maybeSendExecutionTimeStatus(false);
      } catch (IOException ioe) {
        return;
        // Quietly leave if io error occurs; it's the usual termination condition
      }
    }
  }

  private void sendChanges()
    throws InterruptedException, IOException
  {
    for (Iterator i = changes.iterator(); i.hasNext(); ) {
      Change change = (Change) i.next();
      if (change.subscription == inventorySubscription) {
        inventorySubscriptionChanged(change.elements(), change.kind);
        continue;
      }
      if (change.subscription == peSubscription) {
        peSubscriptionChanged(change.elements(), change.kind);
        continue;
      }
      if (change.subscription == projectionTaskSubscription) {
        projectionTaskSubscriptionChanged(change.elements(), change.kind);
        continue;
      }
      if (change.subscription == orgSubscription) {
        orgSubscriptionChanged(change.elements(), change.kind);
        continue;
      }
    }
    changes.clear();
    synchronized (out) {        // Prevent the d..n harness from interrupting
      sendIfNotEmpty(schedules);
      sendIfNotEmpty(failureConsumptionRates);
      sendIfNotEmpty(taskEventReports);
      sendIfNotEmpty(scheduleRescinds);
      sendIfNotEmpty(failureConsumptionRateRescinds);
      sendIfNotEmpty(taskEventReportRescinds);
      writer.flush();
    }
  }

    private void sendIfNotEmpty(List list) throws IOException {
      if (list.size() > 0) {
        writer.writeEGObject(new EGObjectArray(list));
        list.clear();
      }
    }

  private void maybeSendExecutionTimeStatus(boolean force) throws IOException {
    long now = System.currentTimeMillis();
    double newExecutionRate = cluster.getExecutionRate();
    if (force || now > nextTimeUpdate || newExecutionRate != theExecutionRate) {
      nextTimeUpdate = now + theTimeStep;
      theExecutionRate = newExecutionRate;
      ExecutionTimeStatus ack = new ExecutionTimeStatus(cluster.currentTimeMillis(),
                                                        theExecutionRate);
      synchronized (out) {        // Prevent the d..n harness from interrupting
        writer.writeEGObject(ack);
        writer.flush();
      }
    }
  }
}
}
