/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.assessor;

import org.cougaar.core.cluster.Alarm;
import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.measure.CountRate;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.measure.FlowRate;
import org.cougaar.domain.planning.ldm.plan.Alert;
import org.cougaar.domain.planning.ldm.plan.AlertImpl;
import org.cougaar.domain.planning.ldm.plan.AlertParameter;
import org.cougaar.domain.planning.ldm.plan.NewAlert;
import org.cougaar.domain.planning.ldm.plan.NewAlertParameter;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Inventory;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.policy.ACRPolicy;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;

/**
 * Watch for supply tasks for MEI consumers/sonsumed and estimate the
 * apparent failure/consumption rate. Upon command through a GUI,
 * update the consumer spec for the consumer/consumed to match the
 * apparent rate. Runs daily and uses a sliding window of the past 14
 * days.
 **/
public class ConsumptionAssessorPlugIn extends SimplePlugIn {
  private static final String ALERT_TEXT = "Failure/Consumption Rate Deviation";
  private static final int YES_PARAM  = 0;
  private static final int NO_PARAM  = 1;
  private static final int BUCKET_PARAM  = 2;
  private static final int FACTOR_PARAM  = 3;

  private static final int WINDOW_SIZE_PARAM       = 0;
  private static final int ASSESSMENT_PERIOD_PARAM = 1;
  private static final int MAX_DEVIATION_PARAM     = 2;
  private static final int N_FIXED_PARAMS          = 3;

  private MessageFormat alertMessageFormat =
    new MessageFormat("Failure/Consumption Rate Deviation\n\n"
                      + "Consumer:          {0}\n"
                      + "Consumable:        {1}\n"
                      + "Projected rate:    {2,number,#,###.##}\n"
                      + "Apparent rate:     {3,number,#,###.##}\n"
                      + "Deviation:         {4,number,0.##}\n"
                      + "Total Consumption: {5,number,#,###}\n"
                      + "Total Projection:  {6,number,#,###}\n"
                      + "Current Adjustment:{7,number,#0.##}\n"
                      + "New Adjustment:    {8,number,#0.##}\n\n"
                      + "Do you want to change the rate to {3,number,#,###.##}?\n");

  private Alarm timer;
  private long assessmentPeriod;
  private long windowBegin;
  private long windowEnd;
  private long windowSize;
  private double maxDeviation;
  private HashMap alerts = new HashMap();
  private String selfOrgName;
  private Set resourceTypes = null; // Null means all

  /**
   * The key to a particular consumption assessment consisting of the
   * consumer and consumed assets.
   **/
  private static class Bucket implements java.io.Serializable {
    Asset consumer;
    Asset consumed;
    public Bucket(Task task) {
      consumer = getConsumer(task);
      consumed = getConsumed(task);
    }
    public Bucket(Asset consumer, Asset consumed) {
      this.consumer = consumer;
      this.consumed = consumed;
    }
    public int hashCode() {
      return consumer.hashCode() + consumed.hashCode();
    }

    public boolean matches(Task task) {
        return getConsumer(task).equals(consumer) && getConsumed(task).equals(consumed);
    }

    public boolean equals(Object o) {
      if (o instanceof Bucket) {
        Bucket other = (Bucket) o;
        return consumer.equals(other.consumer) && consumed.equals(other.consumed);
      }
      return false;
    }
    public String toString() {
      return consumer + "/" + consumed;
    }
  }

  private IncrementalSubscription selfOrgs;
  private static UnaryPredicate selfOrgPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization) {
        return ((Organization) o).isSelf();
      }
      return false;
    }
  };

  private boolean taskPredicate(Object o, Verb verb, Set resourceTypes) {
    if (o instanceof Task) {
      Task task = (Task) o;
      if (task.getVerb().equals(verb)) {
        boolean maintainingOk = false;
        boolean forOk = false;
        boolean forSelf = false;
        boolean isRefill = false;
        boolean resourceTypesOk = resourceTypes == null;
        for (Enumeration e = task.getPrepositionalPhrases(); e.hasMoreElements(); ) {
          PrepositionalPhrase pp = (PrepositionalPhrase) e.nextElement();
          String prep = pp.getPreposition();
          if (prep.equals(Constants.Preposition.MAINTAINING)) {
            if (pp.getIndirectObject() instanceof Inventory) return false;
            maintainingOk = true;
          } else if (prep.equals(Constants.Preposition.FOR)) {
            String orgName = (String) pp.getIndirectObject();
            forSelf = orgName.equals(selfOrgName);
            if (forSelf && isRefill) return false;
            forOk = true;
          } else if (prep.equals(Constants.Preposition.REFILL)) {
            isRefill = true;
            if (forSelf) return false;
          } else if (prep.equals(Constants.Preposition.OFTYPE)) {
            Object type = pp.getIndirectObject();
            if (resourceTypes != null && !resourceTypes.contains(type)) {
                return false;
            }
            resourceTypesOk = true;
          }
          if (maintainingOk && resourceTypesOk && forOk && !(isRefill && forSelf)) return true;
        }
      }
    }
    return false;
  }

  private CollectionSubscription projectionTasks;
  private UnaryPredicate projectionTaskPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return taskPredicate(o, Constants.Verb.ProjectSupply, resourceTypes);
    }
  };

  private CollectionSubscription supplyTasks;
  private UnaryPredicate supplyTaskPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return taskPredicate(o, Constants.Verb.Supply, null);
    }
  };

  private IncrementalSubscription alertSubscription;
  private static UnaryPredicate alertPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Alert) {
        Alert alert = (Alert) o;
        return (alert.getType() == Alert.CONSUMPTION_DEVIATION_TYPE);
      }
      return false;
    }
  };

  public void setupSubscriptions() {
    List params = getParameters();
    windowSize = parseInterval((String) params.get(WINDOW_SIZE_PARAM));
    assessmentPeriod = parseInterval((String) params.get(ASSESSMENT_PERIOD_PARAM));
    maxDeviation = Double.parseDouble((String) params.get(MAX_DEVIATION_PARAM));
    int nParams = params.size();
    if (nParams > N_FIXED_PARAMS) {
      resourceTypes = new HashSet();
      for (int i = N_FIXED_PARAMS; i < nParams; i++) {
        resourceTypes.add(params.get(i));
      }
    }
    selfOrgs = (IncrementalSubscription) subscribe(selfOrgPredicate);
    processSelfOrgs(selfOrgs.elements());
    alertSubscription = (IncrementalSubscription) subscribe(alertPredicate);
    initializeAlertsMap(alertSubscription.elements());
  }

  public void processSelfOrgs(Enumeration enum) {
    if (enum.hasMoreElements()) {
      Organization self = (Organization) enum.nextElement();
      selfOrgName = self.getItemIdentificationPG().getItemIdentification();
      projectionTasks = (CollectionSubscription) subscribe(projectionTaskPredicate, false);
      supplyTasks = (CollectionSubscription) subscribe(supplyTaskPredicate, false);
      startTimer();
    }
  }

  private void startTimer() {
    timer = wakeAfter(assessmentPeriod);
  }

  public void execute() {
    if (selfOrgName == null) {
      if (selfOrgs.hasChanged()) {
        processSelfOrgs(selfOrgs.getAddedList());
      }
    } else {
      if (timer.hasExpired()) {
        computeConsumptionRates();
        startTimer();
      }
    }
    if (alertSubscription.hasChanged()) {
      processAlerts(alertSubscription.getChangedList());
    }
  }

  private void processAlerts(Enumeration enum) {
    while (enum.hasMoreElements()) {
      Alert alert = (Alert) enum.nextElement();
      if (alert.getAcknowledged()) {
        AlertParameter response = (AlertParameter) alert.getOperatorResponse();
        if ("Yes".equals(response.getParameter())) {
          Bucket bucket = (Bucket) alert.getAlertParameters()[BUCKET_PARAM].getParameter();
          Double newFactor = (Double) alert.getAlertParameters()[FACTOR_PARAM].getParameter();
          ACRPolicy policy = (ACRPolicy) theLDMF.newPolicy(ACRPolicy.class.getName());
          try {
            policy.setConsumerTypeIdentification(bucket.consumer
                                                 .getTypeIdentificationPG()
                                                 .getTypeIdentification());
            policy.setConsumedTypeIdentification(bucket.consumed
                                                 .getTypeIdentificationPG()
                                                 .getTypeIdentification());
            policy.setAdjustmentFactor(newFactor.doubleValue());
            publishAdd(policy);
          } catch (RuleParameterIllegalValueException rpive) {
            rpive.printStackTrace();
          }
        } else {
//            System.out.println("Operator responded " + response.getParameter());
        }
      }
    }
  }

  /**
   * Loop through all projection tasks for MEI consumers
   **/
  private void computeConsumptionRates() {
    String types = " of all types";
    if (resourceTypes != null) {
      types = " of type";
      for (Iterator i = resourceTypes.iterator(); i.hasNext(); ) {
        types += " " + i.next();
      }
    }
//      System.out.println("Starting consumption assessment for " + selfOrgName + types);
    HashMap buckets = new HashMap();
    windowEnd = currentTimeMillis(); // Window ends now
    windowBegin = windowEnd - windowSize;
    Enumeration enum = projectionTasks.elements();
//      if (!enum.hasMoreElements()) {
//          System.out.println("No projections for " + selfOrgName);
//      }
    while (enum.hasMoreElements()) {
      Task projectionTask = (Task) enum.nextElement();
      long startTime = TaskUtils.getStartTime(projectionTask);
      long endTime = TaskUtils.getEndTime(projectionTask);
      if (startTime > windowBegin || endTime < windowEnd) {
          continue;               // Rate doesn't apply for entire window
      }
      Bucket bucket = new Bucket(projectionTask);
      List projectionTasks = (List) buckets.get(bucket);
      if (projectionTasks == null) {
        projectionTasks = new ArrayList();
        buckets.put(bucket, projectionTasks);
      }
      projectionTasks.add(projectionTask);
    }
//      if (buckets.isEmpty()) {
//        System.out.println("No valid projections for " + selfOrgName);
//      }
//      if (supplyTasks.isEmpty()) {
//        System.out.println("No supply tasks for " + selfOrgName);
//      }
    for (Iterator keys = buckets.keySet().iterator(); keys.hasNext(); ) {
      Bucket bucket = (Bucket) keys.next();
      List projectionTasks = (List) buckets.get(bucket);
//        System.out.println("Bucket " + bucket + " has " + projectionTasks.size() + " projections");
      checkConsumption(bucket, projectionTasks);
    }
  }

  private static Asset getConsumer(Task task) {
    return (Asset) task.getPrepositionalPhrase(Constants.Preposition.MAINTAINING)
      .getIndirectObject();
  }

  private static Asset getConsumed(Task task) {
    return task.getDirectObject();
  }

  private void checkConsumption(Bucket bucket, List projectionTasks) {
//      System.out.println("checkConsumption " + bucket);
    double totalConsumption = 0.0;
    double totalProjection = 0.0;
    double totalBase = 0.0;
    // Now we accumulate the statistics about this Consumer/Consumed rate
    for (Iterator i = projectionTasks.iterator(); i.hasNext(); ) {
      Task projectionTask = (Task) i.next();
      PrepositionalPhrase pp;
      Rate rate = TaskUtils.getRate(projectionTask);
      double projectedRate;
      if (rate instanceof CountRate) {
        projectedRate = ((CountRate) rate).getValue(CountRate.EACHES_PER_DAY);
      } else if (rate instanceof FlowRate) {
        projectedRate = ((FlowRate) rate).getValue(FlowRate.GALLONS_PER_DAY);
      } else {
        System.err.println("Don't understand this rate: " + rate);
        continue;                   // Don't understand this kind of rate
      }
      double multiplier = TaskUtils.getMultiplier(projectionTask);
      Enumeration tasks = supplyTasks.elements();
      boolean valid = false;
      double thisConsumption = 0.0;
      while (tasks.hasMoreElements()) {
        Task supplyTask = (Task) tasks.nextElement();
        long endTime = TaskUtils.getEndTime(supplyTask);
        if (!bucket.matches(supplyTask)) {
            continue; // Not for this bucket
        }
        if (endTime < windowBegin) {
          valid = true;
          continue;               // Outside the window
        }
        if (endTime > windowEnd) {
          continue;               // Outside the window
        }
        double q = TaskUtils.getQuantity(supplyTask);
//          if (q == 0.0) System.err.println("Supply task has 0 quantity: " + supplyTask);
        thisConsumption += q;
      }
      if (valid) {
        double thisProjection = projectedRate * windowSize / TaskUtils.MSEC_PER_DAY;
        double thisBase = thisProjection / multiplier;
        totalConsumption += thisConsumption;
        totalProjection += thisProjection;
        totalBase += thisBase;
//        } else {
//            System.out.println("No valid supply tasks for " + selfOrgName + " of " + bucket);
      }
    }
//      if (totalConsumption == 0.0) {
//          System.out.println("No consumption for " + selfOrgName + " of " + bucket);
//      }

    double apparentRate = totalConsumption  * TaskUtils.MSEC_PER_DAY / windowSize;
    double projectedRate = totalProjection * TaskUtils.MSEC_PER_DAY / windowSize;
    double currentAdjustmentFactor = totalProjection / totalBase;
    double newAdjustmentFactor = (apparentRate / projectedRate) * currentAdjustmentFactor;
    double d = (apparentRate + projectedRate);
    if (d < Double.MIN_VALUE) return; // Rates are too small to consider
    double deviation = (apparentRate - projectedRate) / d;
    if (Math.abs(deviation) > maxDeviation) {
      sendAlert(bucket,
                deviation, apparentRate, projectedRate,
                currentAdjustmentFactor, newAdjustmentFactor,
                totalConsumption, totalProjection);
    } else {
//    System.out.println("Minor deviation " + deviation + " for " + selfOrgName + " of " + bucket);
      removeAlert(bucket);
    }
  }

  private void initializeAlertsMap(Enumeration e) {
    while (e.hasMoreElements()) {
      Alert alert = (Alert) e.nextElement();
      Bucket bucket = (Bucket) alert.getAlertParameters()[2].getParameter();
      alerts.put(bucket, alert);
    }
  }

  private void removeAlert(Bucket bucket) {
    Alert alert = (Alert) alerts.get(bucket);
    if (alert != null) publishRemove(alert);
  }
  
  /**
   * Publish an alert requesting adjustment of this rate.
   **/
  private void sendAlert(Bucket bucket,
                         double deviation,
                         double apparentRate,
                         double projectedRate,
                         double currentAdjustmentFactor,
                         double newAdjustmentFactor,
                         double totalConsumption,
                         double totalProjection)
  {
    removeAlert(bucket);
    Double newFactor = new Double(newAdjustmentFactor);
    Object[] args = new Object[] {
      bucket.consumer.getTypeIdentificationPG().getNomenclature(),
      bucket.consumed.getTypeIdentificationPG().getNomenclature(),
      new Double(projectedRate),
      new Double(apparentRate),
      new Double(deviation),
      new Double(totalConsumption),
      new Double(totalProjection),
      new Double(currentAdjustmentFactor),
      newFactor
    };
    NewAlert alert = theLDMF.newAlert();
    alert.setAlertText(alertMessageFormat.format(args));
    alert.setType(Alert.CONSUMPTION_DEVIATION_TYPE);
    AlertParameter[] alertParameters = new AlertParameter[] {
      theLDMF.newAlertParameter(),
      theLDMF.newAlertParameter(),
      theLDMF.newAlertParameter(),
      theLDMF.newAlertParameter()
    };
    ((NewAlertParameter) alertParameters[YES_PARAM]).setDescription("Yes");
    ((NewAlertParameter) alertParameters[YES_PARAM]).setParameter("Yes");
    ((NewAlertParameter) alertParameters[NO_PARAM]).setDescription("No");
    ((NewAlertParameter) alertParameters[NO_PARAM]).setParameter("No");
    ((NewAlertParameter) alertParameters[BUCKET_PARAM]).setParameter(bucket);
    ((NewAlertParameter) alertParameters[BUCKET_PARAM]).setVisible(false);
    ((NewAlertParameter) alertParameters[FACTOR_PARAM]).setParameter(newFactor);
    ((NewAlertParameter) alertParameters[FACTOR_PARAM]).setVisible(false);
    alert.setAlertParameters(alertParameters);
    alert.setOperatorResponseRequired(true);
    publishAdd(alert);
    alerts.put(bucket, alert);
    System.out.println(alert.getAlertText());
  }
}

