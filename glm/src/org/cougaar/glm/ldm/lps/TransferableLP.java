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


package org.cougaar.glm.ldm.lps;

import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.planning.ldm.plan.TransferableAssignment;
import org.cougaar.planning.ldm.plan.TransferableRescind;
import org.cougaar.planning.ldm.plan.TransferableTransfer;
import org.cougaar.planning.ldm.plan.NewTransferableAssignment;
import org.cougaar.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.planning.ldm.plan.NewTransferableVerification;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Collection;

/** TransferableLP is a "LogPlan Logic Provider":
  *
  * it provides the logic to capture
  * PlanElements that are Transferable and send TransferableAssignment 
  * directives to the proper remote cluster.
  *
  **/

public class TransferableLP
  extends LogPlanLogicProvider
  implements EnvelopeLogicProvider, RestartLogicProvider
{

  public TransferableLP(LogPlanServesLogicProvider logplan,
			ClusterServesLogicProvider cluster) {
    super(logplan,cluster);
  }


  /**
   * @param Object Envelopetuple,
   *          where tuple.object
   *             == PlanElement with an Allocation to an Organization  ADDED to LogPlan
   *
   * If the test returned true i.e. it was an TransferableTransfer...
   * create an TransferableAssignment task and send itto a remote Cluster (Organization).
   **/
  public void execute(EnvelopeTuple o, Collection changes) {
    Object obj = o.getObject();
    if (obj instanceof TransferableTransfer) {
      TransferableTransfer tt = (TransferableTransfer) obj;
      ClusterIdentifier dest = ((Organization)tt.getAsset()).getClusterIdentifier();
      if (o.isAdd()) {
        processTransferableTransferAdded(tt, dest);
      } else if (o.isRemove()) {
        processTransferableTransferRemoved(tt, dest);
      }
    }
  }

  // RestartLogicProvider implementation

  /**
   * Cluster restart handler. Resend all our Transferables to the
   * restarted cluster. Also send TransferableVerification messages
   * for all the Transferables we have received from the restarted
   * cluster. The restarted cluster will rescind them if they are no
   * longer valid.
   **/
  public void restart(final ClusterIdentifier cid) {
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof TransferableTransfer) {
          TransferableTransfer tt = (TransferableTransfer) o;
          ClusterIdentifier dest = ((Organization)tt.getAsset()).getClusterIdentifier();
          return cid.equals(dest);
        }
        return false;
      }
    };
    Enumeration enum = logplan.searchBlackboard(pred);
    while (enum.hasMoreElements()) {
      TransferableTransfer tt = (TransferableTransfer) enum.nextElement();
      processTransferableTransferAdded(tt, cid);
    }
    pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Transferable) {
          Transferable transferable = (Transferable) o;
          return transferable.isFrom(cid);
        }
        return false;
      }
    };
    for (enum = logplan.searchBlackboard(pred); enum.hasMoreElements(); ) {
      Transferable transferable = (Transferable) enum.nextElement();
      NewTransferableVerification nav = ldmf.newTransferableVerification(transferable);
      nav.setSource(cluster.getClusterIdentifier());
      nav.setDestination(cid);
      logplan.sendDirective(nav);
    }
  }
    

  private void processTransferableTransferAdded(TransferableTransfer tt,
                                                ClusterIdentifier dest)
  {
    // create an TransferableAssignment task
    NewTransferableAssignment nta = ldmf.newTransferableAssignment();
    nta.setTransferable(tt.getTransferable());
    nta.setDestination(dest);

    // Give the directive to the logplan for tranmission
    logplan.sendDirective(nta);
  }
  
  private void processTransferableTransferRemoved(TransferableTransfer tt,
                                                  ClusterIdentifier dest)
  {
    // create an TransferableRescind task
    NewTransferableRescind ntr = ldmf.newTransferableRescind();
    ntr.setTransferableUID(tt.getTransferable().getUID());
    ntr.setDestination(dest);

    // Give the directive to the logplan for tranmission
    logplan.sendDirective(ntr);
  }
}
