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

package org.cougaar.lib.callback;

import java.util.Collection;
import java.util.HashSet;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.lib.util.UTILVerify;
import org.cougaar.util.UnaryPredicate;
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
    verify = new UTILVerify (logger);
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
  protected UTILVerify verify;
}
