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

package org.cougaar.lib.callback;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.util.UnaryPredicate;

import java.util.Collection;


/**
 * Root callback listener.  The only thing the callback needs from
 * all listeners is that they be able to create a subscription.
 * 
 * Note that this could extend org.cougaar.core.plugin.PluginDelegate, and
 * then we could get the subscribe method, but we would also get 
 * all the other delegate methods, cluttering the interface.
 * 
 * However, if we find that we need listeners to have a lot of the 
 * methods from PluginDelegate (e.g. publishAdd, publishChange),
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






