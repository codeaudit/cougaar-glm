/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.generic;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.util.UnaryPredicate;

/**
 * TransferablePredicate - Unary Predicate that listens for a specific
 * implementation of a Transferable
 * 
 *
 * @author  ALPINE <alpine-software@bbn.com>
 *
 */

public class TransferablePredicate implements UnaryPredicate {

  protected Class transferableClass = null;

  public TransferablePredicate(Class cl) {
    transferableClass = cl;
  }


  public boolean equals(Object other) {
    if (other instanceof TransferablePredicate) {
      TransferablePredicate tp = (TransferablePredicate) other;
      if (tp.getTransferableClass().equals(transferableClass))
	return true;
    }
    return false;
  }

  public int hashCode() {
    return transferableClass.hashCode();
  }

  public Class getTransferableClass() {
    return transferableClass;
  }

  public boolean execute(Object o) {
    if (o instanceof Transferable) {
      if ( transferableClass.isInstance(o)) {
	return true;
      }
    }
    return false;
  }

}
