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

import org.cougaar.core.blackboard.Directive;
import org.cougaar.core.domain.LogicProvider;
import org.cougaar.core.domain.MessageLogicProvider;
import org.cougaar.core.domain.RootPlan;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.planning.ldm.plan.TransferableAssignment;
import org.cougaar.planning.ldm.plan.TransferableRescind;
import org.cougaar.planning.ldm.plan.TransferableVerification;
import org.cougaar.util.UnaryPredicate;



/**
  * ReceiveTransferableLP Adds or modifies transferables to agents.
  * @author  ALPINE <alpine-software@bbn.com>
  *
  **/

public class ReceiveTransferableLP 
implements LogicProvider, MessageLogicProvider
{

  private final RootPlan rootplan;
  private final PlanningFactory ldmf;

  public ReceiveTransferableLP(
      RootPlan rootplan,
      PlanningFactory ldmf) {
    this.rootplan = rootplan;
    this.ldmf = ldmf;
  }

  public void init() {
  }

  private static UnaryPredicate makeTp(final Transferable t) {
    return new UnaryPredicate() {
        public boolean execute(Object o) {
          return (o instanceof Transferable) && ((Transferable)o).same(t);
        }};
  }

  /**
   * Adds/removes Transferables to/from LogPlan... Side-effect = other subscribers
   * also updated.
   **/
  public void execute(Directive dir, Collection changes) {
    if (dir instanceof TransferableAssignment) {
      processTransferableAssignment((TransferableAssignment) dir, changes);
    } else if (dir instanceof TransferableRescind) {
      processTransferableRescind((TransferableRescind) dir, changes);
    } else if (dir instanceof TransferableVerification) {
      processTransferableVerification((TransferableVerification) dir, changes);
    }
  }

  private void processTransferableRescind(TransferableRescind tr, Collection changes) {
    final UID uid = tr.getTransferableUID();
    Transferable t = (Transferable) rootplan.findUniqueObject(uid);
    if (t != null) rootplan.remove(t);
  }

  private void processTransferableVerification(TransferableVerification tv, Collection changes) {
    final UID uid = tv.getTransferableUID();
    if (rootplan.findUniqueObject(uid) == null) {
      // create and send a TransferableRescind task
      NewTransferableRescind ntr = ldmf.newTransferableRescind();
      ntr.setTransferableUID(uid);
      ntr.setDestination(tv.getSource());
      rootplan.sendDirective(ntr);
    }
  }

  private void processTransferableAssignment(TransferableAssignment ta, Collection changes) {
    Transferable tat = (Transferable) ta.getTransferable();
    Transferable t = (Transferable) rootplan.findUniqueObject(tat.getUID());
    try {
      if (t != null) {
        // shouldn't really need the clone...
        Transferable tatc = (Transferable) tat.clone();
        t.setAll(tatc);         // local slots
        reconcile(t, tatc);     // may do fancy things with various objects
        rootplan.change(t, changes);
      } else {
        Transferable tatc = (Transferable) tat.clone();
        rootplan.add(tatc);
        reconcile(null, tatc);
      }
    }
    catch (Exception excep) {
      excep.printStackTrace();
    }
  }

  // we should really break this out into an abstract api, but it
  // scares me to put logprovider code into object like Oplan.
  /** Reconcile any component state of the blackboard with regard to 
   * @param t Old version of transferrable from blackboard.  May be null.
   * @param pt Putative new version of the transferrable.
   **/
  private void reconcile(Transferable t, Transferable pt) {
  }
}




