/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm;

import java.util.*;

import org.cougaar.core.cluster.BlackboardServesLogicProvider;
import org.cougaar.core.cluster.ClusterServesLogicProvider;
import org.cougaar.core.cluster.LogPlan;
import org.cougaar.core.cluster.LogPlanServesLogicProvider;
import org.cougaar.core.cluster.XPlanServesBlackboard;
import org.cougaar.domain.planning.ldm.Domain;
import org.cougaar.domain.planning.ldm.Factory;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.glm.ldm.lps.*;

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
