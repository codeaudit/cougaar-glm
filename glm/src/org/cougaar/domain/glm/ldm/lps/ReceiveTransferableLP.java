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

package org.cougaar.domain.glm.ldm.lps;

import org.cougaar.core.cluster.*;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.planning.ldm.plan.TransferableAssignment;
import org.cougaar.domain.planning.ldm.plan.TransferableRescind;
import org.cougaar.domain.planning.ldm.plan.TransferableTransfer;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.domain.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.domain.planning.ldm.plan.TransferableVerification;

import java.util.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.core.society.UID;



/**
  * ReceiveTransferableLP Adds or modifies transferables to cluster
  * @author  ALPINE <alpine-software@bbn.com>
  *
  **/

public class ReceiveTransferableLP extends LogPlanLogicProvider implements MessageLogicProvider
{
  public ReceiveTransferableLP(LogPlanServesLogicProvider logplan,
			       ClusterServesLogicProvider cluster) {
    super(logplan,cluster);
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
    Transferable t = (Transferable) logplan.findUniqueObject(uid);
    logplan.remove(t);
  }

  private void processTransferableVerification(TransferableVerification tv, Collection changes) {
    final UID uid = tv.getTransferableUID();
    if (logplan.findUniqueObject(uid) == null) {
      // create and send a TransferableRescind task
      NewTransferableRescind ntr = ldmf.newTransferableRescind();
      ntr.setTransferableUID(uid);
      ntr.setDestination(tv.getSource());
      logplan.sendDirective(ntr);
    }
  }

  private void processTransferableAssignment(TransferableAssignment ta, Collection changes) {
    Transferable tat = (Transferable) ta.getTransferable();
    Transferable t = (Transferable) logplan.findUniqueObject(tat.getUID());
    try {
      if (t != null) {
        // shouldn't really need the clone...
        Transferable tatc = (Transferable) tat.clone();
        t.setAll(tatc);         // local slots
        reconcile(t, tatc);     // may do fancy things with various objects
        logplan.change(t, changes);
      } else {
        Transferable tatc = (Transferable) tat.clone();
        logplan.add(tatc);
        reconcile(null, tatc);
      }
    }
    catch (Exception excep) {
      excep.printStackTrace();
    }
  }

  // we should really break this out into an abstract api, but it
  // scares me to put logprovider code into object like Oplan.
  /** Reconcile any component state of the logplan with regard to 
   * @param t Old version of transferrable from logplan.  May be null.
   * @param pt Putative new version of the transferrable.
   **/
  private void reconcile(Transferable t, Transferable pt) {
  }
}




