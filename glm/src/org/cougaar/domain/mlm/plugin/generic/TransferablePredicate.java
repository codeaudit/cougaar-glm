/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.generic;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.domain.glm.asset.Organization;
import java.util.Vector;
import java.util.Enumeration;

/**
 * TransferablePredicate - Unary Predicate that listens for a specific
 * implementation of a Transferable
 * 
 *
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: TransferablePredicate.java,v 1.1 2000-12-15 20:17:46 mthome Exp $
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

};
