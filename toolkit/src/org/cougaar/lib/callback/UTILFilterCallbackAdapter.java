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
import org.cougaar.core.blackboard.Subscription;

import org.cougaar.util.UnaryPredicate;

import java.util.Collection;
import java.util.HashSet;

import org.cougaar.util.log.Logger;

/**
 * Root callback implementation.  All current callbacks extend
 * this.
 */

public class UTILFilterCallbackAdapter implements UTILFilterCallback {
  /**
   * Constructor records the passed-in listener, and then creates
   * and stores a subscription, which gets a predicate from the
   * the getPredicate () method.
   */
  public UTILFilterCallbackAdapter (UTILFilterCallbackListener listener, Logger logger) {
    myListener = listener;
    this.logger = logger;
    mySub = myListener.subscribeFromCallback(getPredicate ());
  }


  /**
   * <pre>
   * Exactly like other constructor, but allows passing in a 
   * container to use for the objects that match the predicate.  
   *
   * This is useful when subscribing to a large set
   * of objects, and a special container (e.g. a descendant
   * of com.objectspace.jgl.OrderedSet) would make lookup more efficient.
   *
   * </pre>
   * @param useContainer - not used, just makes this a different constructor
   */
  public UTILFilterCallbackAdapter (UTILFilterCallbackListener listener,
				    boolean useContainer, Logger logger) {
    myListener = listener;
    mySub = myListener.subscribeFromCallback(getPredicate (), getCollection ());
    this.logger = logger;
  }

  /** 
   * Used by plugins to ask the subscription direct questions,
   * like "give me all your elements." 
   * @see org.cougaar.lib.filter.UTILAllocatorPluginAdapter#getAssets
   */
  public IncrementalSubscription getSubscription () { 
    return mySub;
  }

  /** 
   * Subclass to provide particular filter.
   *
   * This default method matches *everything*.  Please override.
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) { return true; }
    };
  }


  /**
   * This default returns the same container ALPINE uses by default.
   *
   * @see java.util.Collection
   */
  protected Collection getCollection () {
    return new HashSet (); 
  }


  /** 
   * The filter has matched something, this defines what to do
   * next.
   *
   * This default does nothing.  Please override.
   */
  public void reactToChangedFilter () {}

  /** 
   * instance variables
   */

  /** the listener paired with this callback */ 
  protected UTILFilterCallbackListener myListener = null;

  /** the subscription created with the getPredicate predicate */
  protected IncrementalSubscription mySub = null;

  protected Logger logger;
}
