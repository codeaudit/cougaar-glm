package org.cougaar.glm.servlet;

import org.cougaar.core.servlet.SimpleServletComponent;
import org.cougaar.core.servlet.SimpleServletSupport;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.NamingService;
import org.cougaar.glm.servlet.GLMStimulatorSupport;
import org.cougaar.core.plugin.*;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.service.SchedulerService;

public class GLMStimulatorServletComponent extends SimpleServletComponent {
  private LDMService ldmService = null;
  private SchedulerService scheduler;

  public final void setLDMService(LDMService s) {
    ldmService = s;
  }
  protected final LDMService getLDMService() {
    return ldmService;
  }
  protected ConfigFinder getConfigFinder() {
    return ((PluginBindingSite) bindingSite).getConfigFinder();
  }


  // rely upon load-time introspection to set these services - 
  //   don't worry about revokation.
  public final void setSchedulerService(SchedulerService ss) {
    scheduler = ss;
  }

  // just like the core, except I can create a servlet support subclass
  protected SimpleServletSupport createSimpleServletSupport() {
    // the agentId is known from "setBindingSite(..)"

    // get the blackboard service (for "query")
    blackboard = (BlackboardService)
      serviceBroker.getService(
          this, 
          BlackboardService.class,
          null);
    if (blackboard == null) {
      throw new RuntimeException(
          "Unable to obtain blackboard service");
    }

    // get the naming service (for "listAgentNames")
    ns = (NamingService)
      serviceBroker.getService(
          this, 
          NamingService.class,
          null);
    if (ns == null) {
      throw new RuntimeException(
          "Unable to obtain naming service");
    }

    // create a new "SimpleServletSupport" instance
    return makeServletSupport ();
  }

  // so a subclass can create a different servlet support just by overriding this method
  // perhaps the core should work like this?
  protected SimpleServletSupport makeServletSupport () {
    // create a new "SimpleServletSupport" instance
    return 
      new GLMStimulatorSupport (
        path,
        agentId,
        blackboard,
        ns,
	getConfigFinder(),
	getLDMService().getFactory(),
	getLDMService().getLDM(),
	scheduler);
  }
}
