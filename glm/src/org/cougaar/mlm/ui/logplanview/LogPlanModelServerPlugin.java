/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.mlm.ui.logplanview;

import org.cougaar.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.domain.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;

import org.cougaar.glm.ldm.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.asset.*;

import java.util.*;
import java.text.*;

/**
 * Clusters should include this plugin in order to attach the LogPlan Viewer.
 */
public class LogPlanModelServerPlugin extends SimplePlugin {
  LogPlanModel logPlanModel = null;
  private String clusterName = null;
  private Vector logPlanObserverList = new Vector();
  private Vector skipVerbs = new Vector();

  private IncrementalSubscription allocations               = null;
  private IncrementalSubscription tasks                     = null;
  private IncrementalSubscription nonAllocationDispositions = null;

  static class AllocationsP implements UnaryPredicate {
    public boolean execute( Object o) {
      if ( o instanceof Allocation)
        return true;
      return false;
    }
  }

  static class NonAllocationDispositionsP implements UnaryPredicate {
    public boolean execute( Object o) {
      if ( o instanceof Disposition) {
        if ( !( o instanceof Allocation)) {
          return true;
        }
      }
      return false;
    }
  }

  static class TasksP implements UnaryPredicate {
    public boolean execute(Object o) {
      if ( o instanceof Task ) {
        return true;
      }
      return false;
    }
  }

  public String getClusterName() {
    return clusterName;
  }

  public LDMServesPlugin getLdm() {
    return getLDM();
  }

  public LogPlanModel getLogPlanModel() {
    return logPlanModel;
  }

  /**
   * Create and return a LogPlanModel.
   * Override this method if you have a extended LogPlanModel and need that class created.
   *
   * @param skipVerbsEnum list of verb Strings to exclude from the logplan (blackboard) output tree.
   * @return the storage model of the logplan (blackboard)
   */
  public LogPlanModel createLogPlanModel( Enumeration skipVerbsEnum) {
    return new LogPlanModel( this, getClusterName(), didRehydrate(), skipVerbsEnum);
  }

  /**
   * Create and return a list of verb Strings to exclude from the logplan (blackboard) output tree.
   */
  public Enumeration createSkipVerbStrings() {
    return null;
  }

  /**
   * Standard method for classes extending SimplePlugin.
   * Store local reference to cluster name. Create subscriptions for tasks, allocations and nonAllocationDispositions.
   * Create local copy of verb Strings to exclude from output. Create the LogPlanModel for this plugin's view and register
   * this plugin with the LogPlans view.
   */
  protected void setupSubscriptions() {
    clusterName = getMessageAddress().getAddress();

    allocations               = (IncrementalSubscription)subscribe( new AllocationsP());
    tasks                     = (IncrementalSubscription)subscribe( new TasksP());
    nonAllocationDispositions = (IncrementalSubscription)subscribe( new NonAllocationDispositionsP());

    Enumeration newSkipVerbStrings = createSkipVerbStrings();
    while ( newSkipVerbStrings != null && newSkipVerbStrings.hasMoreElements()) {
      skipVerbs.add( newSkipVerbStrings.nextElement());
    }

    logPlanModel = createLogPlanModel( skipVerbs.elements());
    LogPlansFrame.addLogPlan( this);
  }

  /**
   * Standard method of classes extending SimplePlugin.
   * This execute method calls the associated logPlanModel's logPlanUpdate() method
   * so it can update itself with logplan changes.
   */
  public void execute() {
    logPlanModel.logPlanUpdate();
  }

  public boolean getAllocationsHasChanged() {
    return allocations.hasChanged();
  }
  public Enumeration getAllocations() {
    return allocations.elements();
  }
  public Enumeration getAllocationsAdded() {
    return allocations.getAddedList();
  }
  public Enumeration getAllocationsChanged() {
    return allocations.getChangedList();
  }
  public Enumeration getAllocationsRemoved() {
    return allocations.getRemovedList();
  }

  public boolean getNonAllocationDispositionsHasChanged() {
    return nonAllocationDispositions.hasChanged();
  }
  public Enumeration getNonAllocationDispositions() {
    return nonAllocationDispositions.elements();
  }
  public Enumeration getNonAllocationDispositionsAdded() {
    return nonAllocationDispositions.getAddedList();
  }
  public Enumeration getNonAllocationDispositionsChanged() {
    return nonAllocationDispositions.getChangedList();
  }
  public Enumeration getNonAllocationDispositionsRemoved() {
    return nonAllocationDispositions.getRemovedList();
  }

  public boolean getTasksHasChanged() {
    return tasks.hasChanged();
  }
  public Enumeration getTasks() {
    return tasks.elements();
  }
  public Enumeration getTasksAdded() {
    return tasks.getAddedList();
  }
  public Enumeration getTasksChanged() {
    return tasks.getChangedList();
  }
  public Enumeration getTasksRemoved() {
    return tasks.getRemovedList();
  }
}
