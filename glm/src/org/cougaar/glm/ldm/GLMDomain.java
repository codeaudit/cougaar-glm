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

package org.cougaar.glm.ldm;

import java.util.ArrayList;
import java.util.Collection;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.domain.DomainAdapter;
import org.cougaar.core.domain.Factory;
import org.cougaar.core.domain.RootPlan;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.DomainService;
import org.cougaar.glm.ldm.asset.AssetFactory;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;
import org.cougaar.glm.ldm.lps.DetailRequestLP;
import org.cougaar.glm.ldm.lps.OPlanWatcherLP;
import org.cougaar.glm.ldm.lps.ReceiveTransferableLP;
import org.cougaar.glm.ldm.lps.TransferableLP;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.service.LDMService;


/**
 * COUGAAR Domain package definition.
 **/

public class GLMDomain extends DomainAdapter {

  public static final String GLM_NAME = "glm";

  private MessageAddress self;
  private AgentIdentificationService agentIdService;
  private DomainService domainService;
  private LDMService ldmService;

  public String getDomainName() {
    return GLM_NAME;
  }

  public GLMDomain() {
    super();
  }

  public void setAgentIdentificationService(AgentIdentificationService ais) {
    this.agentIdService = ais;
    if (ais == null) {
      // Revocation
    } else {
      this.self = ais.getMessageAddress();
    }
  }

  public void setDomainService(DomainService domainService) {
    this.domainService = domainService;
  }

  public void setLDMService(LDMService ldmService) {
    this.ldmService = ldmService;
  }

  public void initialize() {
    super.initialize();
    Constants.Role.init();    // Insure that our Role constants are initted
  }

  public void unload() {
    ServiceBroker sb = getBindingSite().getServiceBroker();
    if (ldmService != null) {
      sb.releaseService(
          this, LDMService.class, ldmService);
      ldmService = null;
    }
    if (domainService != null) {
      sb.releaseService(
          this, DomainService.class, domainService);
      domainService = null;
    }
    if (agentIdService != null) {
      sb.releaseService(
          this, AgentIdentificationService.class, agentIdService);
      agentIdService = null;
    }
    super.unload();
  }

  public Collection getAliases() {
    ArrayList l = new ArrayList(3);
    l.add("glm");
    l.add("mlm");
    l.add("alp");
    return l;
  }

  protected void loadFactory() {
    LDMServesPlugin ldm = ldmService.getLDM();
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
    RootPlan rootplan = (RootPlan) getXPlanForDomain("root");
    if (rootplan == null) {
      throw new RuntimeException("Missing \"root\" plan!");
    }

    PlanningFactory ldmf = (PlanningFactory) 
      domainService.getFactory("planning");
    if (ldmf == null) {
      throw new RuntimeException("Missing \"planning\" factory!");
    }
    GLMFactory glmFactory = (GLMFactory) 
      domainService.getFactory("glm");

    addLogicProvider(new ReceiveTransferableLP(rootplan, ldmf));
    addLogicProvider(new TransferableLP(rootplan, self, ldmf));
    addLogicProvider(new DetailRequestLP(rootplan, self, glmFactory));
    addLogicProvider(new OPlanWatcherLP(rootplan, ldmf));
  }

}
