/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.generic;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.core.cluster.IncrementalSubscription;

/**
 * A class that holds subscriptions for a Transferable and its desired
 * destinaton
 *
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: TransferableSubscriptions.java,v 1.2 2001-04-05 19:27:49 mthome Exp $
 */

public class TransferableSubscriptions {
  
  private IncrementalSubscription transferableSubscription;
  private UnaryPredicate destinationPredicate;

  TransferableSubscriptions(IncrementalSubscription transferable,
			    UnaryPredicate destination) {
    transferableSubscription = transferable;
    destinationPredicate = destination;
  }

  public IncrementalSubscription getTransferableSubscription() {
    return transferableSubscription;
  }

  public UnaryPredicate getDestinationPredicate() {
    return destinationPredicate;
  }
};
