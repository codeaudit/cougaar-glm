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
  //  protected TriggerModel tm;
  //  protected SubscriptionWatcher watcher;

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

  /*
  public void load() {
    super.load();
    
    // create a blackboard watcher
    this.watcher = 
      new SubscriptionWatcher() {
        public void signalNotify(int event) {
          // gets called frequently as the blackboard objects change
          super.signalNotify(event);
          tm.trigger();
        }
        public String toString() {
          return "ThinWatcher("+GLMStimulatorServletComponent.this.toString()+")";
        }
      };

    // create a callback for running this component
    Trigger myTrigger = 
      new Trigger() {
        private boolean didPrecycle = false;
        // no need to "sync" when using "SyncTriggerModel"
        public void trigger() {
          watcher.clearSignal(); 
          if (didPrecycle) {
            try {
              awakened = watcher.clearSignal();
              cycle();
            } finally {
              awakened = false;
            }
          } else {
            didPrecycle = true;
            precycle();
          }
        }
        public String toString() {
          return "Trigger("+GLMStimulatorServletComponent.this.toString()+")";
        }
      };

    // create the trigger model
    this.tm = new SyncTriggerModelImpl(scheduler, myTrigger);

    // activate the blackboard watcher
    blackboard.registerInterest(watcher);

    // activate the trigger model
    tm.initialize();
    tm.load();
  }

  public void start() {
    super.start();
    tm.start();
    // Tell the scheduler to run me at least this once
    tm.trigger();
  }

  public void suspend() {
    super.suspend();
    tm.suspend();
  }

  public void resume() {
    super.resume();
    tm.resume();
  }

  public void stop() {
    super.stop();
    tm.stop();
  }

  public void halt() {
    super.halt();
    tm.halt();
  }
  
  public void unload() {
    super.unload();
    if (tm != null) {
      tm.unload();
      tm = null;
    }
    blackboard.unregisterInterest(watcher);
  }

  //
  // implement basic "callback" actions
  //

  protected void precycle() {
    try {
      blackboard.openTransaction();
      setupSubscriptions();
    
      // run execute here so subscriptions don't miss out on the first
      // batch in their subscription addedLists
      execute();                // MIK: I don't like this!!!
    } catch (Throwable t) {
      System.err.println("Error: Uncaught exception in "+this+": "+t);
      t.printStackTrace();
    } finally {
      blackboard.closeTransaction();
    }
  }      
  
  protected void cycle() {
    // do stuff
    try {
      blackboard.openTransaction();
      execute();
    } catch (Throwable t) {
      System.err.println("Error: Uncaught exception in "+this+": "+t);
      t.printStackTrace();
    } finally {
      blackboard.closeTransaction();
    }
  }
  */

  /**
   * Called once after initialization, as a "pre-execute()".
   */
  //  protected void setupSubscriptions() {}
  
  /**
   * Called every time this component is scheduled to run.
   */
  //  protected void execute() {
  //    
  //  }
}
