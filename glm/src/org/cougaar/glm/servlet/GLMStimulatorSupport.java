package org.cougaar.glm.servlet;

import org.cougaar.core.servlet.SimpleServletSupportImpl;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.NamingService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.domain.*;
import org.cougaar.core.service.SchedulerService;

public class GLMStimulatorSupport extends SimpleServletSupportImpl {
  public GLMStimulatorSupport(
      String path,
      ClusterIdentifier agentId,
      BlackboardService blackboard,
      NamingService ns,
      ConfigFinder configFinder,
      RootFactory ldmf,
      LDMServesPlugin ldm,
      SchedulerService scheduler) {
    super (path, agentId, blackboard, ns);
    this.configFinder = configFinder;
    this.ldmf = ldmf;
    this.ldm = ldm;
    this.scheduler = scheduler;
  }
  protected ConfigFinder configFinder;
  RootFactory ldmf;
  LDMServesPlugin ldm;
  SchedulerService scheduler;

  // I need access to the blackboard so I can publish to it
  public BlackboardService getBlackboardService () { return blackboard; }
  public ConfigFinder getConfigFinder () { return configFinder; }
  public RootFactory getLDMF () { return ldmf; }
  public LDMServesPlugin getLDM () { return ldm; }
  public SchedulerService getSchedulerService () { return scheduler; }
}
