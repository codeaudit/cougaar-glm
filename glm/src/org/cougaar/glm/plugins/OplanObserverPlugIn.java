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

package org.cougaar.glm.plugins;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Vector;

public class OplanObserverPlugIn extends SimplePlugIn {

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

    myId = getClusterIdentifier().cleanToString();

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
