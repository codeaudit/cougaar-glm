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

package org.cougaar.glm.ldm;

import java.util.*;

import org.cougaar.core.blackboard.BlackboardServesLogicProvider;
import org.cougaar.core.agent.ClusterServesLogicProvider;
import org.cougaar.core.blackboard.LogPlan;
import org.cougaar.core.blackboard.LogPlanServesLogicProvider;
import org.cougaar.core.blackboard.XPlanServesBlackboard;
import org.cougaar.core.domain.Domain;
import org.cougaar.core.domain.Factory;
import org.cougaar.core.domain.LDMServesPlugIn;
import org.cougaar.glm.ldm.lps.*;

/**
 * COUGAAR Domain package definition.
 **/

public class GLMDomain implements Domain {
  public GLMDomain() { }

  public void initialize() {
    // register COUGAAR Verbs, etc... maybe just put 'em in the factory or somesuch
    Constants.Role.init();      // Insure that our Role constants are initted
  }

  public Factory getFactory(LDMServesPlugIn ldm) {
    return new GLMFactory(ldm);
  }

  public XPlanServesBlackboard createXPlan(Collection existingXPlans) {
    for (Iterator plans = existingXPlans.iterator(); plans.hasNext(); ) {
      XPlanServesBlackboard xPlan = (XPlanServesBlackboard) plans.next();
      if (xPlan instanceof LogPlan) return xPlan;
    }
    return new LogPlan();
  }

  public Collection createLogicProviders(BlackboardServesLogicProvider alpplan,
                                         ClusterServesLogicProvider cluster) {
    ArrayList l = new ArrayList(5); // don't let this be too small.

    LogPlan logplan = (LogPlan) alpplan;
    l.add(new ReceiveTransferableLP(logplan, cluster));
    l.add(new TransferableLP(logplan, cluster));
    l.add(new DetailRequestLP(logplan, cluster));
    l.add(new OPlanWatcherLP(logplan, cluster));
    return l;
  }

  public Collection getAliases() {
    ArrayList l = new ArrayList(3);
    l.add("glm");
    l.add("mlm");
    l.add("alp");
    return l;
  }
}
