/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.glm.ldm.lps;

import org.cougaar.core.cluster.*;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.domain.planning.ldm.plan.TransferableAssignment;
import org.cougaar.domain.planning.ldm.plan.TransferableRescind;
import org.cougaar.domain.planning.ldm.plan.TransferableTransfer;
import org.cougaar.domain.planning.ldm.plan.NewTransferableAssignment;
import org.cougaar.domain.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.domain.planning.ldm.plan.NewTransferableVerification;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.Debug;
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
    System.out.println("Resending transferables to " + cid);
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
    Enumeration enum = logplan.searchWhiteboard(pred);
    while (enum.hasMoreElements()) {
      TransferableTransfer tt = (TransferableTransfer) enum.nextElement();
      System.out.println("Resending " + tt);
      processTransferableTransferAdded(tt, cid);
    }
    System.out.println("Resending finished");
    System.out.println("Verifying received transferables");
    pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Transferable) {
          Transferable transferable = (Transferable) o;
          return transferable.isFrom(cid);
        }
        return false;
      }
    };
    for (enum = logplan.searchWhiteboard(pred); enum.hasMoreElements(); ) {
      Transferable transferable = (Transferable) enum.nextElement();
      NewTransferableVerification nav = ldmf.newTransferableVerification(transferable);
      nav.setSource(cluster.getClusterIdentifier());
      nav.setDestination(cid);
      System.out.println("Verifying " + transferable + " with " + nav);
      logplan.sendDirective(nav);
    }
    System.out.println("Verifying finished");
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
