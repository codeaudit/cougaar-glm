/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.util.UnaryPredicate;

import java.util.Collection;


/**
 * Root callback listener.  The only thing the callback needs from
 * all listeners is that they be able to create a subscription.
 * 
 * Note that this could extend org.cougaar.core.plugin.PlugInDelegate, and
 * then we could get the subscribe method, but we would also get 
 * all the other delegate methods, cluttering the interface.
 * 
 * However, if we find that we need listeners to have a lot of the 
 * methods from PlugInDelegate (e.g. publishAdd, publishChange),
 * then we might want to revisit this.
 *
 * GWFV 6/1/99
 */

public interface UTILFilterCallbackListener {
  /** only thing we need from listener is way to make a subscription */
  IncrementalSubscription subscribeFromCallback(UnaryPredicate pred);


  /** 
   * Sometimes a listener wants to use a special container to put the
   * objects in. 
   *
   * See the second constructor in UTILFilterCallbackAdapter
   */
  IncrementalSubscription subscribeFromCallback(UnaryPredicate pred,
						Collection specialContainer);

}






