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
   * @param o Envelopetuple,
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
    Enumeration en = rootplan.searchBlackboard(pred);
    while (en.hasMoreElements()) {
      TransferableTransfer tt = (TransferableTransfer) en.nextElement();
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
    for (en = rootplan.searchBlackboard(pred); en.hasMoreElements(); ) {
      Transferable transferable = (Transferable) en.nextElement();
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
