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

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.agent.ClusterServesLogicProvider;

import org.cougaar.core.domain.*;
import org.cougaar.core.component.BindingSite;

import org.cougaar.core.domain.DomainAdapter;
import org.cougaar.core.domain.DomainBindingSite;

import org.cougaar.glm.ldm.lps.*;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.ldm.asset.AssetFactory;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;

import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;


/**
 * COUGAAR Domain package definition.
 **/

public class GLMDomain extends DomainAdapter {
  public static final String GLM_NAME = "glm";

  public String getDomainName() {
    return GLM_NAME;
  }

  public GLMDomain() {
    super();
  }

  public void initialize() {
    super.initialize();
    Constants.Role.init();    // Insure that our Role constants are initted
  }

  public void load() {
    super.load();
  }

  public Collection getAliases() {
    ArrayList l = new ArrayList(3);
    l.add("glm");
    l.add("mlm");
    l.add("alp");
    return l;
  }

  protected void loadFactory() {
    DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();
    if (bindingSite == null) {
      throw new RuntimeException(
          "Binding site for the domain has not be set.\n" +
          "Unable to initialize domain Factory without a binding site.");
    } 

    LDMServesPlugin ldm = 
      bindingSite.getClusterServesLogicProvider().getLDM();
    PlanningFactory ldmf = (PlanningFactory) ldm.getFactory("planning");
    if (ldmf == null) {
      throw new RuntimeException("Missing \"planning\" factory!");
    }

    // ensure GLM aspect types are reserved in the AspectValue registry
    AlpineAspectType.init();

    Factory f = new GLMFactory();
    ldmf.addAssetFactory(new AssetFactory());
    ldmf.addPropertyGroupFactory(new PropertyGroupFactory());
    setFactory(f);
  }

  protected void loadXPlan() {
    // no glm-specific plan
  }

  protected void loadLPs() {
    DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();
    if (bindingSite == null) {
      throw new RuntimeException(
          "Binding site for the domain has not be set.\n" +
          "Unable to initialize domain LPs without a binding site.");
    } 

    ClusterServesLogicProvider cluster = 
      bindingSite.getClusterServesLogicProvider();
    RootPlan rootplan = (RootPlan) 
      bindingSite.getXPlanForDomain("root");
    if (rootplan == null) {
      throw new RuntimeException("Missing \"root\" domain plan!");
    }
    MessageAddress self = cluster.getMessageAddress();
    PlanningFactory ldmf = (PlanningFactory) cluster.getFactory("planning");
    if (ldmf == null) {
      throw new RuntimeException("Missing \"planning\" factory!");
    }
    GLMFactory glmFactory = (GLMFactory) cluster.getFactory("glm");

    addLogicProvider(new ReceiveTransferableLP(rootplan, ldmf));
    addLogicProvider(new TransferableLP(rootplan, self, ldmf));
    addLogicProvider(new DetailRequestLP(rootplan, self, glmFactory));
    addLogicProvider(new OPlanWatcherLP(rootplan, ldmf));
  }

}
