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
package org.cougaar.glm.execution.cluster;

import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.core.plugin.util.PluginHelper;
import org.cougaar.core.util.UID;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.util.FilteredEnumeration;
import org.cougaar.util.UnaryPredicate;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.execution.common.*;
import org.cougaar.glm.ldm.asset.InventoryPG;

/**
 * Receives reports from the EventGenerator and incorporates them into the logplan
 **/
public class PSP_EventMonitor
  extends PSP_Base
  implements PlanServiceProvider
{
  public PSP_EventMonitor() throws RuntimePSPException {
    super();
  }

  public PSP_EventMonitor(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  public PlanServiceProvider pspClone() throws RuntimePSPException {
    return new PSP_EventMonitor();
  }

  protected Context createContext() {
    return new MyContext();
  }

  protected static class MyContext extends Context {
  protected void execute() throws IOException {
    Report report = (Report) reader.readEGObject();
    delegate.openTransaction();
    try {
      if (report instanceof InventoryReport) {
        processInventoryReport((InventoryReport) report);
        return;
      }
      if (report instanceof FailureConsumptionReport) {
        processFailureConsumptionReport((FailureConsumptionReport) report);
        return;
      }
      if (report instanceof TaskEventReport) {
        processTaskEventReport((TaskEventReport) report);
        return;
      }
      processUnknownReport(report);
    } finally {
      delegate.closeTransaction();
    }
  }

  private void processInventoryReport(InventoryReport anInventoryReport) {
//      System.out.println("PSP_EventMonitor: Received InventoryReport " + anInventoryReport);
    final String itemIdentifier = anInventoryReport.theItemIdentification;

    // Look for Inventory items in the LogPlan where the itemIdentification
    // matches that in the InventoryReport
    UnaryPredicate searchPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Inventory) {
          Inventory inv = (Inventory) o;
	  ItemIdentificationPG iipg = inv.getItemIdentificationPG();
	  if (iipg != null) {
	      return iipg.getItemIdentification().equals(itemIdentifier);
	  }
        }
        return false;
      }
    };

    Collection matches = delegate.query(searchPredicate);
    if (matches.size() > 0) {
//        System.out.println("PSP_EventMonitor: Found a match for InventoryReport");
      Inventory inv = (Inventory) matches.iterator().next();
      InventoryPG invpg = inv.getInventoryPG();
      if (invpg != null) {
	  invpg.addInventoryReport(anInventoryReport);

	  delegate.publishChange(inv);
      }
    }
  }

//    private static UnaryPredicate demandRateFilter = new UnaryPredicate() {
//      public boolean execute(Object o) {
//        PrepositionalPhrase pp = (PrepositionalPhrase) o;
//        if (pp.getPreposition().equals(Constants.Preposition.DEMANDRATE)) {
//          return false;
//        } else {
//          return true;
//        }
//      }
//    };

  private void processFailureConsumptionReport(FailureConsumptionReport aFailureConsumptionReport)
  {
//      System.out.println("PSP_EventMonitor: Received FailureConsumptionReport " + aFailureConsumptionReport);
    final UID taskUID = aFailureConsumptionReport.theTaskUID;
    UnaryPredicate searchPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task task = (Task) o;
          return task.getUID().equals(taskUID);
        }
        return false;
      }
    };
    Collection matches = delegate.query(searchPredicate);
    if (matches.size() > 0) {
      Task task = (Task) matches.iterator().next();
      NewTask supplyTask = factory.newTask();
      supplyTask.setVerb(Constants.Verb.Supply);
      supplyTask.setDirectObject(task.getDirectObject());
      // Copy all but the demand rate pp
      Enumeration fe = task.getPrepositionalPhrases();
//          new FilteredEnumeration(, demandRateFilter);
      supplyTask.setPrepositionalPhrases(fe);
      supplyTask.setPlan(task.getPlan());
      supplyTask.setContext(task.getContext());
      Vector prefs = new Vector(3);
      Preference pref;
      ScoringFunction sf;
      AspectValue av;
      av = new AspectValue(AspectType.START_TIME, aFailureConsumptionReport.theReceivedDate);
      sf = new ScoringFunction.StepScoringFunction(av, 0.00, 0.99);
      pref = factory.newPreference(AspectType.START_TIME, sf, 1.0);
      prefs.addElement(pref);
      av = new AspectValue(AspectType.END_TIME, aFailureConsumptionReport.theReceivedDate);
      sf = new ScoringFunction.StepScoringFunction(av, 0.00, 0.99);
      pref = factory.newPreference(AspectType.END_TIME, sf, 1.0);
      prefs.addElement(pref);
      av = new AspectValue(AspectType.QUANTITY, aFailureConsumptionReport.theQuantity);
      sf = new ScoringFunction.StrictValueScoringFunction(av);
      pref = factory.newPreference(AspectType.QUANTITY, sf, 1.0);
      prefs.addElement(pref);
      supplyTask.setPreferences(prefs.elements());
      delegate.publishAdd(supplyTask);
    }
  }

  private void processTaskEventReport(TaskEventReport aTaskEventReport)
  {
//      System.out.println("PSP_EventMonitor: Received TaskEventReport " + aTaskEventReport);
    final UID taskUID = aTaskEventReport.theTaskEventId.theTaskUID;
    Collection c = delegate.query(new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof PlanElement) {
          PlanElement pe = (PlanElement) o;
          Task task = pe.getTask();
          return task != null && task.getUID().equals(taskUID);
        }
        return false;
      }
    });
    if (c.size() > 0) {
      PlanElement pe = (PlanElement) c.iterator().next();
      AllocationResult obsAR = pe.getObservedResult();
      AllocationResult newAR =
        new AllocationResult(1.0, true,
                             new int[] {aTaskEventReport.theTaskEventId.theAspectType},
                             new double[] {aTaskEventReport.theAspectValue});
      if (obsAR != null) newAR = new AllocationResult(newAR, obsAR);
      pe.setObservedResult(newAR);
      delegate.publishChange(pe);
    }
  }

  private void processUnknownReport(Report aReport)
  {
    System.out.println("PSP_EventMonitor: Received unknown Report " + aReport);
  }
}
}
