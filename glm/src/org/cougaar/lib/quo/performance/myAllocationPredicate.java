/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */
package org.cougaar.lib.quo.performance;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

/**
 * A predicate that matches allocations of "CODE" tasks
 */
class myAllocationPredicate implements UnaryPredicate
{
  public boolean execute(Object o) {
    boolean ret = false;
    if (o instanceof Allocation) {
      Task t = ((Allocation)o).getTask();
      ret = (t != null) && (t.getVerb().equals(Verb.getVerb("CODE")));
    }
    return ret;
  }
}
