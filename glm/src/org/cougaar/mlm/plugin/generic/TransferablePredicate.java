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

package org.cougaar.mlm.plugin.generic;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.util.UnaryPredicate;

/**
 * TransferablePredicate - Unary Predicate that listens for a specific
 * implementation of a Transferable
 * 
 *
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
