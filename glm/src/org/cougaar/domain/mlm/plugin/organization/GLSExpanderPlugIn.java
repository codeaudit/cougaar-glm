/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.organization;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.core.cluster.Subscriber;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.domain.planning.ldm.plan.NewPlanElement;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.asset.*;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.PlugInHelper;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AbstractAsset;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPGImpl;
import java.util.Enumeration;
import java.util.Vector;


import org.cougaar.util.UnaryPredicate;

/**
 * The GLSExpanderPlugIn will take the intial GetLogSupport task received by
 * a cluster and expand it into getlogsupport for subordinates.
 * 
 * Current functionality (MB3.0) allows only for pass-through
 * of Tasks through a Workflow.
 **/

public class GLSExpanderPlugIn extends SimplePlugIn {
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

  //Override the setupSubscriptions() in the SimplePlugIn.
  protected void setupSubscriptions() {
    Vector params = getParameters();
    myParams = (String[]) params.toArray(new String[params.size()]);

    mySelfOrgs = (IncrementalSubscription) subscribe(selfOrgAssetPred);

    if (didRehydrate()) {
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
    expandableTasks = (IncrementalSubscription) subscribe(myTaskPred);

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
    myExpansions = (IncrementalSubscription) subscribe(myExpansionPred);
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
  public synchronized void execute() {
    if (mySelfOrgs.hasChanged()) {
      processOrgAssets(mySelfOrgs.getAddedList());
    }

    if (expandableTasks == null) return; // Still waiting for ourself
      
    if (expandableTasks.hasChanged()) {
      Enumeration e = expandableTasks.getAddedList();
      while (e.hasMoreElements()) {
        Task task = (Task) e.nextElement();
        expand(task);
      }
    }
    if (myExpansions.hasChanged()) {
      PlugInHelper.updateAllocationResult(myExpansions);
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
   **/
  public void expand(Task task) {
    Vector subtasks = new Vector();
    subtasks.addElement(createForSubordinatesTask(task));

    for (int i = 0; i < myParams.length; i++) {
      subtasks.addElement(createDetermineRequirementsTask(task, myParams[i]));
    }
    Expansion exp = PlugInHelper.wireExpansion(task, subtasks, theLDMF);
    //use the helper to publish the expansion and the wf subtasks all in one
    PlugInHelper.publishAddExpansion(getSubscriber(), exp);
  }

  /**
   * Create the for subordinates task resulting from the given
   * parent Task.
   **/
  private NewTask createForSubordinatesTask(Task task) {
    // Create copy of parent Task
    NewTask subtask = createTask(task);
    subtask.setVerb(Constants.Verb.GetLogSupport);

    Vector prepphrases = new Vector();

    // The following is removed because we should depend on the context, instead
//      // get the existing prep phrase(s) - propagate "with OPlan" phrase only
//      Enumeration origpp = task.getPrepositionalPhrases();
//      while (origpp.hasMoreElements()) {
//        PrepositionalPhrase theorigpp = (PrepositionalPhrase) origpp.nextElement();
//        if ((theorigpp.getPreposition().equals(Constants.Preposition.WITH)) &&
//            (theorigpp.getIndirectObject() instanceof Oplan)) {	
//          prepphrases.addElement(theorigpp);
//        }
//      }
    // make the "subordinates" abstract asset and add a prep phrase with it
    Asset subasset_proto = theLDMF.createPrototype(Asset.class, "Subordinates");
    Asset subasset = theLDMF.createInstance(subasset_proto);
    NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(theLDMF.cloneInstance(subasset));
    prepphrases.addElement(newpp);
    subtask.setPrepositionalPhrases(prepphrases.elements());
    return subtask;
  }

  /**
   * Creates a DETERMINEREQUIREMENTS task of the specified type
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

  private NewTask createTask(Task task) {
    NewTask subtask = theLDMF.newTask();
    subtask.setParentTask(task);
    subtask.setSource(this.getCluster().getClusterIdentifier());
    if (task.getDirectObject() != null) {
      subtask.setDirectObject(theLDMF.cloneInstance(task.getDirectObject()));
    } else {
      subtask.setDirectObject(null);
    }

    subtask.setPlan(task.getPlan());
    subtask.setPreferences(task.getPreferences());
    ContextOfUIDs context = (ContextOfUIDs) task.getContext();
    if (context == null) {
      System.err.println(getClass() + " missing context in " + task);
    } else {
      //RAY      subtask.setContext(context);
    }
    return subtask;
  }
}
