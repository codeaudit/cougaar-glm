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


package org.cougaar.glm.ldm.lps;

import java.util.Collection;
import java.util.Enumeration;

import org.cougaar.core.blackboard.EnvelopeTuple;
import org.cougaar.core.domain.EnvelopeLogicProvider;
import org.cougaar.core.domain.LogicProvider;
import org.cougaar.core.domain.RestartLogicProvider;
import org.cougaar.core.domain.RestartLogicProviderHelper;
import org.cougaar.core.domain.RootPlan;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.NewTransferableAssignment;
import org.cougaar.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.planning.ldm.plan.NewTransferableVerification;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.planning.ldm.plan.TransferableTransfer;
import org.cougaar.util.UnaryPredicate;

/** TransferableLP is a "LogPlan Logic Provider":
  *
  * it provides the logic to capture
  * PlanElements that are Transferable and send TransferableAssignment 
  * directives to the proper remote agent.
  *
  **/

public class TransferableLP
implements LogicProvider, EnvelopeLogicProvider, RestartLogicProvider
{
  private final RootPlan rootplan;
  private final MessageAddress self;
  private final PlanningFactory ldmf;

  public TransferableLP(
      RootPlan rootplan,
      MessageAddress self,
      PlanningFactory ldmf) {
    this.rootplan = rootplan;
    this.self = self;
    this.ldmf = ldmf;
  }

  public void init() {
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
      MessageAddress dest = ((Organization)tt.getAsset()).getMessageAddress();
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
   * restarted agent. Also send TransferableVerification messages
   * for all the Transferables we have received from the restarted
   * agent. The restarted agent will rescind them if they are no
   * longer valid.
   **/
  public void restart(final MessageAddress cid) {
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof TransferableTransfer) {
          TransferableTransfer tt = (TransferableTransfer) o;
          MessageAddress dest = 
            ((Organization)tt.getAsset()).getMessageAddress();
          return 
            RestartLogicProviderHelper.matchesRestart(
                self, cid, dest);
        }
        return false;
      }
    };
    Enumeration enum = rootplan.searchBlackboard(pred);
    while (enum.hasMoreElements()) {
      TransferableTransfer tt = (TransferableTransfer) enum.nextElement();
      MessageAddress dest = ((Organization)tt.getAsset()).getMessageAddress();
      processTransferableTransferAdded(tt, dest);
    }
    pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Transferable) {
          Transferable transferable = (Transferable) o;
          // we can't use "isFrom(..)", since we'll later need the
          // specific destination if we want to send a verification
          MessageAddress dest = transferable.getSource();
          return 
            RestartLogicProviderHelper.matchesRestart(
                self, cid, dest);
        }
        return false;
      }
    };
    for (enum = rootplan.searchBlackboard(pred); enum.hasMoreElements(); ) {
      Transferable transferable = (Transferable) enum.nextElement();
      NewTransferableVerification nav = ldmf.newTransferableVerification(transferable);
      nav.setSource(self);
      nav.setDestination(transferable.getSource());
      rootplan.sendDirective(nav);
    }
  }

  private void processTransferableTransferAdded(TransferableTransfer tt,
                                                MessageAddress dest)
  {
    // create an TransferableAssignment task
    NewTransferableAssignment nta = ldmf.newTransferableAssignment();
    nta.setTransferable(tt.getTransferable());
    nta.setDestination(dest);

    // Give the directive to the blackboard for tranmission
    rootplan.sendDirective(nta);
  }
  
  private void processTransferableTransferRemoved(TransferableTransfer tt,
                                                  MessageAddress dest)
  {
    // create an TransferableRescind task
    NewTransferableRescind ntr = ldmf.newTransferableRescind();
    ntr.setTransferableUID(tt.getTransferable().getUID());
    ntr.setDestination(dest);

    // Give the directive to the blackboard for tranmission
    rootplan.sendDirective(ntr);
  }
}
