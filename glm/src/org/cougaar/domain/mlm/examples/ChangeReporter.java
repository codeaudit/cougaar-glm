/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.examples;

import java.util.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.cluster.*;

/**
 * ChangeReporter subscribes for everything, and prints
 * any detailed ChangeReports associated with any changed 
 * objects.
 **/

public class ChangeReporter extends SimplePlugIn
{
  private IncrementalSubscription stuff = null;

  protected void setupSubscriptions() {
    UnaryPredicate trueP = new UnaryPredicate() {
        public boolean execute(Object o) { return true; }
      };
    stuff = (IncrementalSubscription) subscribe(trueP);
  }

  protected void execute() {
    Collection c = stuff.getChangedCollection();
    if (c != null && c.size()>0) {
      for (Iterator it = c.iterator(); it.hasNext(); ) {
        Object o = it.next();

        Collection crs = stuff.getChangeReports(o);

        if (crs != null && crs.size()>0) {
          synchronized (System.out) {
            System.out.println(o.toString()+" change reports:");
            for (Iterator crit = crs.iterator(); crit.hasNext(); ) {
              ChangeReport cr = (ChangeReport) crit.next();
              System.out.println("\t"+cr);
            }
          }
        } else {
          System.out.println(o.toString()+" changed without details");
        }
      }
    }
  }
}
