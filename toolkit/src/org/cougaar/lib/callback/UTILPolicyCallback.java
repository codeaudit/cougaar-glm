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

import org.cougaar.domain.planning.ldm.policy.Policy;

import org.cougaar.util.UnaryPredicate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class UTILPolicyCallback extends UTILFilterCallbackAdapter {

  /////////////////////////////////////////////////////////////////
  /// CONSTRUCTORS
  /////////////////////////////////////////////////////////////////

  /**
   * Constructor. Takes as the argument the pointer to the listener
   * (which in most cases is a plugin that implements that listener
   * interface.
   */
  public UTILPolicyCallback (UTILPolicyListener listener) {
    super (listener);
  }

  /////////////////////////////////////////////////////////////////
  /// OVERRIDING SUPERCLASS METHODS
  /////////////////////////////////////////////////////////////////

  /**
   * Return the predicate.  The superclass constructor calls this
   * function to actually setup the plugin predicate subscription.
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Policy) {
	  Policy p = (Policy)o;
	  return ((UTILPolicyListener)myListener).interestingPolicy(p); 
	}
	return false;
      }
    };
  }
}

