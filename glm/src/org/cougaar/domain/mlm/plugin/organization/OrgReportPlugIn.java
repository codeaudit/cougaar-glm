/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.organization;

import org.cougaar.domain.planning.plugin.AssetReportPlugIn;

import org.cougaar.domain.planning.ldm.plan.AssetTransfer;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.domain.glm.ldm.asset.Organization;

/**
 * OrgReportPlugIn manages REPORTFORDUTY and REPORTFORSERVICE relationships
 * Handles both expansion and allocation of these tasks.
 * @see org.cougaar.domain.mlm.plugin.organization.AllocatorPlugInImpl
 * @see org.cougaar.core.plugin.SimplifiedPlugIn
 * @see org.cougaar.core.plugin.SimplifiedPlugInTest
 */

public class OrgReportPlugIn extends AssetReportPlugIn
{

    /**
   * getTaskPredicate - returns task predicate for task subscription
   * Default implementation subscribes to all non-internal tasks. Derived classes
   * should probably implement a more specific version.
   * 
   * @return UnaryPredicate - task predicate to be used.
   */
  protected UnaryPredicate getTaskPredicate() {
    return allReportTaskPred();
  }

  protected UnaryPredicate getAssetTransferPred() {
    return allReportAssetTransferPred();
  }

  // #######################################################################
  // BEGIN predicates
  // #######################################################################
  
  // predicate for getting allocatable tasks of report for duty
  private static UnaryPredicate allReportTaskPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task) {
          Task task = (Task) o;
	  if (((task.getVerb().equals(Constants.Verb.REPORTFORDUTY)) ||
               (task.getVerb().equals(Constants.Verb.REPORTFORSERVICE))) &&
              (task.getWorkflow() == null) &&
              (task.getPlanElement() == null)) {
	    return true;
          }
	}
	return false;
      }
    };
  }

  private static UnaryPredicate allReportAssetTransferPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof AssetTransfer) {
          Task t = ((AssetTransfer)o).getTask();
          return ((t.getVerb().equals(Constants.Verb.REPORTFORDUTY)) ||
                  (t.getVerb().equals(Constants.Verb.REPORTFORSERVICE)));
        }
        return false;
      }
    };
  }
}
