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
import org.cougaar.core.cluster.Subscription;

import org.cougaar.util.UnaryPredicate;

import java.util.Collection;
import java.util.HashSet;

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
  public UTILFilterCallbackAdapter (UTILFilterCallbackListener listener) {
    myListener = listener;
    mySub = myListener.subscribeFromCallback(getPredicate ());
  }


  /**
   * Exactly like other constructor, but allows passing in a 
   * container to use for the objects that match the predicate.  
   *
   * This is useful when subscribing to a large set
   * of objects, and a special container (e.g. a descendant
   * of com.objectspace.jgl.OrderedSet) would make lookup more efficient.
   *
   * @param useContainer - not used, just makes this a different constructor
   */
  public UTILFilterCallbackAdapter (UTILFilterCallbackListener listener,
				    boolean useContainer) {
    myListener = listener;
    mySub = myListener.subscribeFromCallback(getPredicate (), getCollection ());
  }


  public void setExtraDebug      (boolean d) { xdebug  = d; }

  /**
   * Set to true if you want to see when the container changes in
   * ways that *don't* notify the listener.
   *
   * (This can make you feel better, since interestingXXX calls are
   * made even on objects that are about to disappear from your
   * container.  This can let you know that it's OK that even though
   * the listener finds the object interesting, it's not being acted
   * upon.)
   */
  public void setExtraExtraDebug (boolean d) { xxdebug = d; }

  /** 
   * Used by plugins to ask the subscription direct questions,
   * like "give me all your elements." 
   * @see org.cougaar.lib.filter.UTILAllocatorPlugInAdapter#getAssets
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

  protected boolean xdebug  = false;
  protected boolean xxdebug = false;
}
