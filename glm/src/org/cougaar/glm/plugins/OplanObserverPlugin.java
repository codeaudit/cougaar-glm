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

package org.cougaar.glm.plugins;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.oplan.ForcePackage;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

public class OplanObserverPlugin extends SimplePlugin {

  private String myId;
  private boolean trackOplan;
  private boolean trackForcePackage;
  private boolean trackOrgActivity;

  private IncrementalSubscription oplans;
  private static UnaryPredicate oplansPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Oplan);
      }
    };
  }

  private IncrementalSubscription forcepackages;
  private static UnaryPredicate forcepackagesPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof ForcePackage);
      }
    };
  }

  private IncrementalSubscription orgactivities;
  private static UnaryPredicate orgactivitiesPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof OrgActivity);
      }
    };
  }

  public void setupSubscriptions() {
    Vector params = getParameters();
    if (params != null)
      for (int i = 0; i < params.size(); i++) {
        String param = (String)params.elementAt(i);
        trackOplan = (trackOplan || param.toLowerCase().equals("oplan"));
        trackForcePackage = (trackForcePackage || param.toLowerCase().equals("forcepackage"));
        trackOrgActivity = (trackOrgActivity || param.toLowerCase().equals("orgactivity"));
      }

    myId = getMessageAddress().toAddress();

    oplans = (IncrementalSubscription)
      subscribe(oplansPred());
    forcepackages = (IncrementalSubscription)
      subscribe(forcepackagesPred());
    orgactivities = (IncrementalSubscription)
      subscribe(orgactivitiesPred());
  }

  public void execute() {
    Enumeration e = null;

    if (oplans.hasChanged() && trackOplan) {
      e = oplans.getAddedList();
      while (e.hasMoreElements()) {
        Oplan o = (Oplan)e.nextElement();
        display("new Oplan = " + o.getOplanId());
      }    

      e = oplans.getChangedList();
      while (e.hasMoreElements()) {
        Oplan o = (Oplan)e.nextElement();
        display("changed Oplan = " + o.getOplanId());
      }    

      e = oplans.getRemovedList();
      while (e.hasMoreElements()) {
        Oplan o = (Oplan)e.nextElement();
        display("removed Oplan = " + o.getOplanId());
      }    
    }

    if (forcepackages.hasChanged() && trackForcePackage) {
      e = forcepackages.getAddedList();
      while (e.hasMoreElements()) {
        ForcePackage o = (ForcePackage)e.nextElement();
        display("new ForcePackage = " + o.getForcePackageId());
      }    

      e = forcepackages.getChangedList();
      while (e.hasMoreElements()) {
        ForcePackage o = (ForcePackage)e.nextElement();
        display("changed ForcePackage = " + o.getForcePackageId());
      }    

      e = forcepackages.getRemovedList();
      while (e.hasMoreElements()) {
        ForcePackage o = (ForcePackage)e.nextElement();
        display("removed ForcePackage = " + o.getForcePackageId());
      }    
    }

    if (orgactivities.hasChanged() && trackOrgActivity) {
      e = orgactivities.getAddedList();
      while (e.hasMoreElements()) {
        OrgActivity o = (OrgActivity)e.nextElement();
        display("new OrgActivity = " + o.getOrgID());
      }    

      e = orgactivities.getChangedList();
      while (e.hasMoreElements()) {
        OrgActivity o = (OrgActivity)e.nextElement();
        display("changed OrgActivity = " + o.getOrgID());
      }    

      e = orgactivities.getRemovedList();
      while (e.hasMoreElements()) {
        OrgActivity o = (OrgActivity)e.nextElement();
        display("removed OrgActivity = " + o.getOrgID());
      }    
    }

  }

  private void display(String output) {
    System.out.println("\n <" + myId + "> found a " + output);
  }

}
