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

package org.cougaar.mlm.examples;

import java.util.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;

/**
 * ChangeReporter subscribes for everything, and prints
 * any detailed ChangeReports associated with any changed 
 * objects.
 **/

public class ChangeReporter extends SimplePlugin
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
