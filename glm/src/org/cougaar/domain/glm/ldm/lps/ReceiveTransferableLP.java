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

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.planning.ldm.plan.TransferableAssignment;
import org.cougaar.domain.planning.ldm.plan.TransferableRescind;
import org.cougaar.domain.planning.ldm.plan.TransferableTransfer;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.domain.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.domain.planning.ldm.plan.TransferableVerification;

import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OplanContributor;
import java.util.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.core.society.UID;



/**
  * ReceiveTransferableLP Adds or modifies transferables to cluster
  * @author  ALPINE <alpine-software@bbn.com>
  * @version $Id: ReceiveTransferableLP.java,v 1.2 2001-01-03 14:33:13 mthome Exp $
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

  private static UnaryPredicate makeOplanIDp(final UID opid) {
    return new UnaryPredicate() {
        public boolean execute(Object o) {
          return (o instanceof OplanContributor) && 
            opid.equals( ((OplanContributor)o).getUID() );
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
   * the newly transfered putative transferable.  Only Oplans
   * actually activate this code.
   * @param t Old version of transferrable from logplan.  May be null.
   * @param pt Putative new version of the transferrable.
   **/
  private void reconcile(Transferable t, Transferable pt) {
    if (pt instanceof Oplan) {
      reconcileOplan((Oplan)t, (Oplan) pt);
    } else if (pt instanceof OplanContributor) {
      reconcileOplanContributor((OplanContributor) t, (OplanContributor) pt);
    }
  }

  private void reconcileOplanContributor(OplanContributor opc, OplanContributor popc) {
    Oplan pot = (Oplan) logplan.findUniqueObject(popc.getOplanUID());
    if (pot != null) {
      reconcileOplan(null, pot);
    }
  }

  // op may be null!
  private void reconcileOplan(Oplan op, Oplan pot) {
    UID opid = pot.getUID();

    // get the right transferrables from the logplan
    Enumeration tmpe = logplan.searchLogPlan(makeOplanIDp(opid));
    // turn the enum into a collection so we can search multiple times.
    Collection ocs = Translations.toCollection(tmpe);
    // matches is the set of objects from ocs that are found in the oplan subs
    Collection matches = new ArrayList(ocs.size());

    // iterate over the oplan subs, adding to or changing the logplan
    // as needed
    reconcileOplanSubs(pot.getOrgRelations(), ocs, matches);
    reconcileOplanSubs(pot.getOrgActivities(), ocs, matches);
    reconcileOplanSubs(pot.getForcePackages(), ocs, matches);
    
    // take out the subs that aren't found in putative oplan
    ocs.removeAll(matches);
    // remove non-matches from the logplan
    for (Iterator i=ocs.iterator(); i.hasNext();) {
      logplan.remove(i.next());
    }
    // done.
  }

  /** update a set of sub oplan bits
   * @param oss a set of newly transfered subOplan bits.
   * @param lpts existing logplan subOplan bits.
   * @param matches a set of existing logplan subOplan that have been found
   * in the newly transfered oplan, to be added to by this method.
   **/
  private void reconcileOplanSubs(Enumeration oss, Collection lpts, Collection matches) {
    while (oss.hasMoreElements()) {
      // os is a new suboplan part to find
      final Transferable os = (Transferable)oss.nextElement();
      
      // see if we've got a match
      Object found = Filters.findElement(lpts, new UnaryPredicate() {
          public boolean execute(Object o) {
            return os.same((Transferable)o);
          }});
      
      if (found != null) {
        // found is an existing suboplan part which needs to get updated
        if (! found.equals(os)) { // match ids but content has changed.
          ((OplanContributor)found).setAll(os);       // update
          // have to rely on implicit ChangeReports to catch details.
          // top-level transferrable will have user-defined changereports.
          logplan.change(os, null);   // notify watchers.
        } // else do nothing

        // add it to our match list
        matches.add(os);
      } else {
        // no match - needs to be added
        logplan.add(os);
      }
    }
  }
}
