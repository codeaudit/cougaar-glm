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

package org.cougaar.mlm.examples;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.core.blackboard.AnonymousChangeReport;
import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

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

        Set crs = stuff.getChangeReports(o);

        if (crs != AnonymousChangeReport.SET) {
          if (crs == null) {
            // shouldn't happen:
            synchronized (System.out) {
              System.out.println(o.toString()+" null change reports?");
            }
          } else if (crs.isEmpty()) {
            // shouldn't happen:
            synchronized (System.out) {
              System.out.println(o.toString()+" empty change reports?");
            }
          } else {
            synchronized (System.out) {
              System.out.println(o.toString()+" change reports:");
              for (Iterator crit = crs.iterator(); crit.hasNext(); ) {
                ChangeReport cr = (ChangeReport) crit.next();
                System.out.println("\t"+cr);
              }
            }
          }
        } else {
          System.out.println(o.toString()+" changed without details");
        }
      }
    }
  }
}
