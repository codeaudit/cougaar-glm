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

package org.cougaar.mlm.plugin.organization;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.planning.service.LDMService;
import org.cougaar.util.UnaryPredicate;


/**
 * Vesion of GLSExpander without SD support, for use with minitestconfig, eg.
 * The GLSMiniExpanderPlugin will take the intial GetLogSupport task received by
 * a cluster and expand it into getlogsupport for subordinates.
 * 
 * Componentized in Cougaar 8.3 now extends ComponentPlugin instead of SimplePlugin.
 * Note the significant changes can be found in setupSubscriptions().
 **/

public class GLSMiniExpanderPlugin extends ComponentPlugin {
  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription expandableTasks;
  
  /** Subscription to the Expansions I create */
  private IncrementalSubscription myExpansions;
  
  /**
   * The Socrates subscription
   **/
  private IncrementalSubscription mySelfOrgs;
  
  /** for knowing when we get our self org asset **/
  private Organization selfOrgAsset = null;
  
  /**
   * Parameters are the types of determinerequirements to generate.
   **/
  String[] myParams = null;
  PlanningFactory theLDMF = null;
  
  /**
   * Override the setupSubscriptions() in ComponentPlugin
   * Get an LDMService for factory calls
   * Use the blackboard service inherited from ComponentPlugin
   **/
  protected void setupSubscriptions() {
    
    //System.out.println("setupSubscriptions: "+getAgentIdentifier());
    //get the LDM service to access the object factories from my bindingsite's servicebroker
    LDMService ldmService = null;
    if (theLDMF == null) {
      ldmService = (LDMService) getBindingSite().getServiceBroker().getService(this, LDMService.class,
                                                  new ServiceRevokedListener() {
				         public void serviceRevoked(ServiceRevokedEvent re) {
				       theLDMF = null;
        }
      });
    }
    //use the service
    theLDMF = ldmService.getFactory();
    
    Collection params = getParameters();
    if (params != null) {
      myParams = (String[]) params.toArray(new String[params.size()]);
    } else {
      myParams = new String[0];
    }
    
    // subscribe using the blackboardservice - blackboard variable(representing the service)
    //is inherited from ComponentPlugin
    mySelfOrgs = (IncrementalSubscription) blackboard.subscribe(selfOrgAssetPred);
    
    if (blackboard.didRehydrate()) {
      processOrgAssets(mySelfOrgs.elements()); // May already be there
    }
  }
  
  
  private void setupSubscriptions2() {
    
    /** Predicate for finding input GLS Task. It must be a GLS FOR us **/
    final UnaryPredicate myTaskPred = new UnaryPredicate() {
	public boolean execute(Object o) {
	  
	  if (o instanceof Task) {
	    Task task = (Task) o;
	    Verb verb = task.getVerb();
	    if (verb.equals(Constants.Verb.GetLogSupport)) {
	      PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
	      if (pp != null) {
		return pp.getIndirectObject().equals(selfOrgAsset);
	      }
	    }
	  }
	  return false;
	}
      };
    expandableTasks = (IncrementalSubscription) blackboard.subscribe(myTaskPred);
    
    /** Predicate for watching our expansions **/
    final UnaryPredicate myExpansionPred = new UnaryPredicate() {
	public boolean execute(Object o) {
	  
	  if (o instanceof Expansion) {
	    Expansion exp = (Expansion) o;
	    return myTaskPred.execute(exp.getTask());
	  }
	  return false;
	}
      };
    myExpansions = (IncrementalSubscription) blackboard.subscribe(myExpansionPred);
  }
  
  /**
   * The predicate for the Socrates subscription
   **/
  private static UnaryPredicate selfOrgAssetPred = new UnaryPredicate() {
      public boolean execute(Object o) {
	
	if (o instanceof Organization) {
	  Organization org = (Organization) o;
	  return org.isSelf();
	}
	return false;
      }
    };
  
  /**
   * Plugin execute method is called every time one of our
   * subscriptions has something to do
   **/
  protected void execute() {
    
    if (mySelfOrgs.hasChanged()) {
      processOrgAssets(mySelfOrgs.getAddedList());
    }
    
    if (expandableTasks == null) 
      {
        return; // Still waiting for ourself
      }
    if (expandableTasks.hasChanged()) {
      Enumeration e = expandableTasks.getAddedList();
      while (e.hasMoreElements()) {
        Task task = (Task) e.nextElement();
	if (task.getPlanElement () != null) {
	  logger.warn ("GLSMiniExpanderPlugin.execute - strange, task " +task.getUID() + 
		       "\nhas already been expanded with p.e.:\n"+
		       task.getPlanElement() + "\nSo skipping already expanded task.");
	}
	else {
	  expand(task);
	}
      }
    }
    if (myExpansions.hasChanged()) {
      PluginHelper.updateAllocationResult(myExpansions);
    }
  }
  
  private void processOrgAssets(Enumeration e) {
    if (e.hasMoreElements()) {
      selfOrgAsset = (Organization) e.nextElement();
      // Setup our other subscriptions now that we know ourself
      if (expandableTasks == null) {
        setupSubscriptions2();
      }
    }
  }
  
  /**
   * Expand a task into a GLS for subordinates plus
   * DETERMINEREQUIREMENTS for all types specified by params.
   * @param task The Task to expand.
   **/
  public void expand(Task task) {
    Vector subtasks = new Vector();
    subtasks.addElement(createForSubordinatesTask(task));
    
    for (int i = 0; i < myParams.length; i++) {
      subtasks.addElement(createDetermineRequirementsTask(task, myParams[i]));
    }

    AllocationResult estResult = 
        PluginHelper.createEstimatedAllocationResult(task, theLDMF, 0.0, true);
    Expansion exp = PluginHelper.wireExpansion(task, subtasks, theLDMF, 
					       estResult);

    //use the helper to publish the expansion and the wf subtasks all in one
    PluginHelper.publishAddExpansion(blackboard, exp);
  }

  /**
   * Create the for subordinates task resulting from the given
   * parent Task.
   * @param task  Parent task to be used in creating an expanded gls task
   * @return NewTask the new expanded task.
   **/
  private NewTask createForSubordinatesTask(Task task) {
    // Create copy of parent Task
    NewTask subtask = createTask(task);
    subtask.setVerb(Constants.Verb.GetLogSupport);

    Vector prepphrases = new Vector();


    // make the "subordinates" abstract asset and add a prep phrase with it
    Asset subasset_proto = theLDMF.createPrototype(Asset.class, "Subordinates");
    Asset subasset = theLDMF.createInstance(subasset_proto);
    NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(theLDMF.cloneInstance(subasset));
    prepphrases.addElement(newpp);

    Enumeration origpp = task.getPrepositionalPhrases();
    while (origpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
      if (app.getPreposition().equals("WithC0")) {
// 	long c0 = ((Long)app.getIndirectObject()).longValue();
//         newpp.setPreposition("WithC0");
//         newpp.setIndirectObject(new Long(c0));
        prepphrases.addElement(app);
      }
    }
    subtask.setPrepositionalPhrases(prepphrases.elements());
    return subtask;
  } 

  /**
   * Creates a DETERMINEREQUIREMENTS task of the specified type
   * @return Task The DetermineRequirements task
   **/
  public Task createDetermineRequirementsTask(Task task, String ofTypePreposition) {
    NewTask subtask = createTask(task);
    subtask.setVerb(Constants.Verb.DetermineRequirements);
    
    Vector prepphrases = new Vector();
    
    // get the existing prep phrase(s) - look for FOR <Clustername>
    // and add that one to the new subtask
    Enumeration origpp = task.getPrepositionalPhrases();
    while (origpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
      if ((app.getPreposition().equals(Constants.Preposition.FOR)) &&
          (app.getIndirectObject() instanceof Asset)) {	
        prepphrases.addElement(app);
      }
    }
    Asset io_proto = theLDMF.createPrototype(AbstractAsset.class, ofTypePreposition);
    Asset indirectobj = theLDMF.createInstance(io_proto);
    NewPrepositionalPhrase pp = theLDMF.newPrepositionalPhrase();
    pp.setPreposition(Constants.Preposition.OFTYPE);
    pp.setIndirectObject(indirectobj);
    prepphrases.addElement(pp);
    subtask.setPrepositionalPhrases(prepphrases.elements());
    return subtask;
  }

  /**
   * Create a subtask of the parent
   * @param task the Parent
   * @return Newtask the newly created subtask
   **/
  private NewTask createTask(Task task) {
    NewTask subtask = theLDMF.newTask();
    subtask.setParentTask(task);
    subtask.setSource(this.getAgentIdentifier());
    if (task.getDirectObject() != null) {
      subtask.setDirectObject(theLDMF.cloneInstance(task.getDirectObject()));
    } else {
      subtask.setDirectObject(null);
    }
    
    subtask.setPlan(task.getPlan());
    synchronized (task) {
      subtask.setPreferences(task.getPreferences());
    }
    return subtask;
  }

  /** rely upon load-time introspection to set these services - don't worry about revokation. */
  public final void setLoggingService (LoggingService logger) { this.logger = logger; }

  /**
   * Everybody needs a logger
   **/
  protected LoggingService logger;
}


