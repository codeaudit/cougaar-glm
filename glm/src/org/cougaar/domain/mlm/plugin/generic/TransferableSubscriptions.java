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

package org.cougaar.domain.mlm.plugin.generic;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.core.cluster.IncrementalSubscription;

/**
 * A class that holds subscriptions for a Transferable and its desired
 * destinaton
 *
 * @author  ALPINE <alpine-software@bbn.com>
 *
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
