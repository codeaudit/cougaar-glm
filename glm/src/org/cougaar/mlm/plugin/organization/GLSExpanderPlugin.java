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
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TaskScoreTable;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.planning.service.LDMService;
import org.cougaar.util.UnaryPredicate;


/**
 * The GLSExpanderPlugin will take the intial GetLogSupport task received by
 * a cluster and expand it into getlogsupport for subordinates.
 * 
 * Componentized in Cougaar 8.3 now extends ComponentPlugin instead of SimplePlugin.
 * Note the significant changes can be found in setupSubscriptions().
 **/

public class GLSExpanderPlugin extends ComponentPlugin implements GLSConstants {
  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription myGLSTasks;
  private IncrementalSubscription myRegisterServicesTasks;
  private IncrementalSubscription myFindProvidersTasks;
  
  /** Subscription to the Expansions I create */
  private IncrementalSubscription myGLSExpansions;
  private IncrementalSubscription myRegisterServicesExpansions;
  private IncrementalSubscription myFindProvidersExpansions;
  
  /**
   * Subscription to the DetermineRequirements tasks I create 
   **/
  private IncrementalSubscription myDetermineRequirementsTasks;
  
  /**
   * The Socrates subscription
   **/
  private IncrementalSubscription mySelfOrgs;
  
  /** for knowing when we get our self org asset **/
  private Organization mySelfOrgAsset = null;
  
  /**
   * Parameters are the types of determinerequirements to generate.
   **/
  private String[] myParams = null;
  private PlanningFactory theLDMF = null;
  
  /**
   * Override the setupSubscriptions() in ComponentPlugin
   * Get an LDMService for factory calls
   * Use the blackboard service inherited from ComponentPlugin
   **/
  protected void setupSubscriptions() {
    String me = getAgentIdentifier().toString();
    logger = LoggingServiceWithPrefix.add(logger, me + ": ");
    
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
    mySelfOrgs = (IncrementalSubscription) blackboard.subscribe(mySelfOrgAssetPred);
    
    if (blackboard.didRehydrate()) {
      processOrgAssets(mySelfOrgs.elements()); // May already be there
    }
  }
  
  private UnaryPredicate myDetermineRequirementsPredicate =  new UnaryPredicate() {
    public boolean execute (Object o) {
      if (o instanceof Task) {
	Task task = (Task) o;
	Verb verb = task.getVerb();
	return  (Constants.Verb.DetermineRequirements.equals(verb));
      }
      return false;
    }
  };
  

  private class TaskPredicate implements UnaryPredicate {
    private Verb myVerb;

    public TaskPredicate(Verb verb) {
      myVerb = verb;
    }

    public boolean execute(Object o) {
	  
      if (o instanceof Task) {
	Task task = (Task) o;
	Verb verb = task.getVerb();
	if (verb.equals(myVerb)) {
	  PrepositionalPhrase pp = task.getPrepositionalPhrase(FOR_ORGANIZATION);
	  if (pp != null) {
	    return pp.getIndirectObject().equals(mySelfOrgAsset);
	  }
	}
      }
      return false;
    }
  }

  private static class ExpansionPredicate implements UnaryPredicate {
    private UnaryPredicate myTaskPredicate;

    public ExpansionPredicate(UnaryPredicate taskPredicate) {
      myTaskPredicate = taskPredicate;
    }

    public boolean execute(Object o) {
      if (o instanceof Expansion) {
	Expansion exp = (Expansion) o;
	return myTaskPredicate.execute(exp.getTask());
      }
      return false;
    }
  }


  private void setupSubscriptions2() {
    UnaryPredicate taskPredicate = new TaskPredicate(GET_LOG_SUPPORT);
    myGLSTasks = 
      (IncrementalSubscription) blackboard.subscribe(taskPredicate);
    myGLSExpansions = 
      (IncrementalSubscription) blackboard.subscribe(new ExpansionPredicate(taskPredicate));

    taskPredicate = new TaskPredicate(PROPAGATE_REGISTER_SERVICES);
    myRegisterServicesTasks = 
      (IncrementalSubscription) blackboard.subscribe(taskPredicate);
    myRegisterServicesExpansions = 
      (IncrementalSubscription) blackboard.subscribe(new ExpansionPredicate(taskPredicate));

    taskPredicate = new TaskPredicate(PROPAGATE_FIND_PROVIDERS);
    myFindProvidersTasks = 
      (IncrementalSubscription) blackboard.subscribe(taskPredicate);
    myFindProvidersExpansions = 
      (IncrementalSubscription) blackboard.subscribe(new ExpansionPredicate(taskPredicate));
    
    myDetermineRequirementsTasks = 
      (IncrementalSubscription) blackboard.subscribe(myDetermineRequirementsPredicate);
    
  }
  
  /**
   * The predicate for the Socrates subscription
   **/
  private static UnaryPredicate mySelfOrgAssetPred = new UnaryPredicate() {
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
    

    if (myRegisterServicesTasks == null) {
      return; // Still waiting for ourself
    }

    if (myRegisterServicesTasks.hasChanged()) {
      Collection adds = myRegisterServicesTasks.getAddedCollection();
      if (logger.isDebugEnabled()) {
        logger.debug("Expanding " + adds.size()
                     + " PropagateRegisterServices tasks");
      }

      handleNewTasks(adds);

      Collection changes = myRegisterServicesTasks.getChangedCollection();
      handleChangedTasks(changes);
    }

    if (myFindProvidersTasks.hasChanged()) {
      Collection adds = myFindProvidersTasks.getAddedCollection();
      if (logger.isDebugEnabled()) {
        logger.debug("Expanding " + adds.size()
                     + " PropagateFindProviders tasks");
      }

      handleNewTasks(adds);

      Collection changes = myFindProvidersTasks.getChangedCollection();
      handleChangedTasks(changes);
    }

    if (myGLSTasks.hasChanged()) {
      Collection adds = myGLSTasks.getAddedCollection();
      if (logger.isDebugEnabled()) {
        logger.debug("Expanding " + adds.size()
                     + " GLS tasks");
      }

      handleNewTasks(adds);

      Collection changes = myGLSTasks.getChangedCollection();
      handleChangedTasks(changes);
    }

    if (myRegisterServicesExpansions.hasChanged()) {
      PluginHelper.updateAllocationResult(myRegisterServicesExpansions);
    }

    if (myFindProvidersExpansions.hasChanged()) {
      PluginHelper.updateAllocationResult(myFindProvidersExpansions);
    }

    if (myGLSExpansions.hasChanged()) {
      PluginHelper.updateAllocationResult(myGLSExpansions);
    }
  }
  
  private void processOrgAssets(Enumeration e) {
    if (e.hasMoreElements()) {
      mySelfOrgAsset = (Organization) e.nextElement();
      // Setup our other subscriptions now that we know ourself
      if (myRegisterServicesTasks == null) {
        setupSubscriptions2();
      }
    }
  }

  private void handleNewTasks(Collection adds) {

    for (Iterator iterator = adds.iterator(); iterator.hasNext();) {
      Task task = (Task) iterator.next();
      if (task.getPlanElement () != null) {
	if (logger.isWarnEnabled())
	  logger.warn("GLSExpanderPlugin.execute - strange, task " + 
		      task.getUID() + 
		      "\nhas already been expanded with p.e.:\n" +
		      task.getPlanElement() + 
		      "\nSo skipping already expanded task.");
      }
      else {
	expand(task);
      }
    }
  }

  private void handleChangedTasks(Collection changes) {
    // Should never get changed GLS tasks so don't even try to handle them.
    // Last attempt to process changes led to months of debugging.
    for (Iterator iterator = changes.iterator(); iterator.hasNext();) {
      logger.error(getAgentIdentifier() + " ignoring a changed task " +
		     (Task) iterator.next());
    }
  } 

  /**
   * Expand a task into a GLS for subordinates plus
   * DETERMINEREQUIREMENTS for all types specified by params.
   * @param task The Task to expand.
   **/
  public void expand(Task task) {
    Verb taskVerb = task.getVerb();

    if (taskVerb.equals(GET_LOG_SUPPORT)) {
      expandGLS(task);
    } else if (taskVerb.equals(PROPAGATE_REGISTER_SERVICES)) {
      expandRegisterServices(task);
    } else if (taskVerb.equals(PROPAGATE_FIND_PROVIDERS)) {
      expandFindProviders(task);
    }
  }

  private void expandRegisterServices(Task task) {
    Vector subtasks = new Vector(2);
    AllocationResult estResult = 
        PluginHelper.createEstimatedAllocationResult(task, theLDMF, 0.0, true);
    subtasks.addElement(createForSubordinatesTask(task));
    subtasks.addElement(createRegisterServices(task));
    Expansion exp = PluginHelper.wireExpansion(task, subtasks, theLDMF, 
					       estResult);

    // Use allocation result aggregator which ignores determine requirements
    // tasks.
    NewWorkflow workflow = (NewWorkflow) exp.getWorkflow();
    workflow.setAllocationResultAggregator(AGGREGATOR);

    //use the helper to publish the expansion and the wf subtasks all in one
    PluginHelper.publishAddExpansion(blackboard, exp);
  }

  private void expandFindProviders(Task task) {
    Vector subtasks = new Vector(2);
    AllocationResult estResult = 
        PluginHelper.createEstimatedAllocationResult(task, theLDMF, 0.0, true);
    subtasks.addElement(createForSubordinatesTask(task));
    subtasks.addElement(createFindProviders(task));
    Expansion exp = PluginHelper.wireExpansion(task, subtasks, theLDMF, 
					       estResult);

    // Use allocation result aggregator which ignores determine requirements
    // tasks.
    NewWorkflow workflow = (NewWorkflow) exp.getWorkflow();
    workflow.setAllocationResultAggregator(AGGREGATOR);

    //use the helper to publish the expansion and the wf subtasks all in one
    PluginHelper.publishAddExpansion(blackboard, exp);
  }

  private void expandGLS(Task task) {
    Vector subtasks = new Vector();
    AllocationResult estResult = 
        PluginHelper.createEstimatedAllocationResult(task, theLDMF, 0.0, true);
    subtasks.addElement(createForSubordinatesTask(task));
    Expansion exp = PluginHelper.wireExpansion(task, subtasks, theLDMF, 
					       estResult);

    // Use allocation result aggregator which ignores determine requirements
    // tasks.
    NewWorkflow workflow = (NewWorkflow) exp.getWorkflow();
    workflow.setAllocationResultAggregator(AGGREGATOR);

    //use the helper to publish the expansion and the wf subtasks all in one
    PluginHelper.publishAddExpansion(blackboard, exp);

    maybeAddDetermineRequirementsTasks(task);
  }

  private void maybeAddDetermineRequirementsTasks(Task task) {
    SortedSet stages =
      (SortedSet) task
      .getPrepositionalPhrase(FOR_OPLAN_STAGES)
      .getIndirectObject();
    if (stages.isEmpty()) {
      if (logger.isDebugEnabled()) {
        logger.debug("stages.isEmpty() not adding DR tasks");
      }
      return;
    }

    // Check whether DR already exist
    if (myDetermineRequirementsTasks.size() > 0) {
      if (logger.isDebugEnabled()) {
	logger.debug("Already have DetermineRequirements tasks " + 
		     myDetermineRequirementsTasks);
      }
      return;
    }

    Expansion exp = (Expansion) task.getPlanElement();

    if (logger.isDebugEnabled()) {
      logger.debug("Adding DR tasks");
    }
    for (int i = 0; i < myParams.length; i++) {
      NewTask subtask = createDetermineRequirementsTask(task, myParams[i]);
      PluginHelper.wireExpansion(exp, subtask);
      blackboard.publishAdd(subtask);
      if (logger.isDebugEnabled()) {
        logger.debug("Add DR task " + myParams[i]);
      }
    }
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
    subtask.setVerb(task.getVerb());

    Vector prepphrases = new Vector(3);


    // make the "subordinates" abstract asset and add a prep phrase with it
    Asset subasset_proto = theLDMF.createPrototype(Asset.class, "Subordinates");
    Asset subasset = theLDMF.createInstance(subasset_proto);
    NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ORGANIZATION);
    newpp.setIndirectObject(theLDMF.cloneInstance(subasset));
    prepphrases.addElement(newpp);

    Enumeration origpp = task.getPrepositionalPhrases();
    while (origpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
      if (app.getPreposition().equals(WITH_C0)) {
        prepphrases.addElement(app);
      } else if (app.getPreposition().equals(FOR_OPLAN_STAGES)) {
        app = copyOplanStagesPhrase(app);
        prepphrases.addElement(app);
      }
    }
    subtask.setPrepositionalPhrases(prepphrases.elements());
    return subtask;
  } 

  /**
   * Create the FindProviders task resulting from the given
   * parent Task.
   * @param task  Parent task to be used in creating an expanded gls task
   * @return NewTask the new expanded task.
   **/
  private NewTask createFindProviders(Task task) {
    // Create copy of parent Task
    NewTask subtask = createTask(task);
    subtask.setVerb(Constants.Verb.FindProviders);

    Vector prepphrases = new Vector();
    Enumeration origpp = task.getPrepositionalPhrases();
    PrepositionalPhrase oplanStagespp = null;

    while (origpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
      if (app.getPreposition().equals(FOR_OPLAN_STAGES)) {
        oplanStagespp = copyOplanStagesPhrase(app);
      }
    }

    if (oplanStagespp != null) {
      prepphrases.addElement(oplanStagespp);
      subtask.setPrepositionalPhrases(prepphrases.elements());
    }

    return subtask;
  } 

  /**
   * Create the RegisterServices task resulting from the given
   * parent Task.
   * @param task  Parent task to be used in creating an expanded gls task
   * @return NewTask the new expanded task.
   **/
  private NewTask createRegisterServices(Task task) {
    // Create copy of parent Task
    NewTask subtask = createTask(task);
    subtask.setVerb(Constants.Verb.RegisterServices);

    Vector prepphrases = new Vector();
    Enumeration origpp = task.getPrepositionalPhrases();
    PrepositionalPhrase oplanStagespp = null;

    while (origpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
      if (app.getPreposition().equals(FOR_OPLAN_STAGES)) {
	oplanStagespp = copyOplanStagesPhrase(app);
      }
    }

    if (oplanStagespp != null) {
      prepphrases.addElement(oplanStagespp);
      subtask.setPrepositionalPhrases(prepphrases.elements());
    }

    return subtask;
  } 

  /**
   * Creates a DETERMINEREQUIREMENTS task of the specified type
   * @return Task The DetermineRequirements task
   **/
  public NewTask createDetermineRequirementsTask(Task task, String ofTypePreposition) {
    NewTask subtask = createTask(task);
    subtask.setVerb(Constants.Verb.DetermineRequirements);
    
    Vector prepphrases = new Vector();
    
    // get the existing prep phrase(s) - look for FOR <Clustername>
    // and add that one to the new subtask
    Enumeration origpp = task.getPrepositionalPhrases();
    while (origpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
      if ((app.getPreposition().equals(FOR_ORGANIZATION)) &&
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

  private PrepositionalPhrase copyOplanStagesPhrase(PrepositionalPhrase origPhrase) {
    if (FOR_OPLAN_STAGES.equals(origPhrase.getPreposition())) {
      NewPrepositionalPhrase newPhrase = theLDMF.newPrepositionalPhrase();
      newPhrase.setPreposition(FOR_OPLAN_STAGES);
      newPhrase.setIndirectObject(new TreeSet((SortedSet) origPhrase.getIndirectObject()));
      return newPhrase;
    } else {
      logger.error("copyOplanStagePhrase passed an invalid PrepositionalPhrase.",
		   new Throwable());
      return null;
    }
  }

      
  /** rely upon load-time introspection to set these services - don't worry about revokation. */
  public final void setLoggingService (LoggingService logger) { this.logger = logger; }

  /**
   * Everybody needs a logger
   **/
  protected LoggingService logger;

  private static AllocationResultAggregator AGGREGATOR;
  
  static {
    AGGREGATOR = new GLSExpansionAggregator();
  }

  private static class GLSExpansionAggregator implements AllocationResultAggregator {
    private static final String UNDEFINED = "UNDEFINED";

    public AllocationResult calculate(Workflow wf, TaskScoreTable tst, AllocationResult currentar) {
      double acc[] = new double[AspectType._ASPECT_COUNT];
      acc[START_TIME] = Double.MAX_VALUE;
      acc[END_TIME] = 0.0;
      // duration is computed from end values of start and end
      acc[COST] = 0.0;
      acc[DANGER] = 0.0;
      acc[RISK] = 0.0;
      acc[QUANTITY] = 0.0;
      acc[INTERVAL] = 0.0;
      acc[TOTAL_QUANTITY] = 0.0;
      acc[TOTAL_SHIPMENTS] = 0.0;
      acc[CUSTOMER_SATISFACTION] = 1.0; // start at best
      acc[READINESS] = 1.0;

      boolean ap[] = new boolean[AspectType._ASPECT_COUNT];

      boolean suc = true;
      double rating = 0.0;
      
      if (tst == null) return null;
      int tstSize = tst.size();
      if (tstSize == 0) return null;
      
      int ignoredTasks = 0;

      String auxqsummary[] = new String[AuxiliaryQueryType.AQTYPE_COUNT];
      // initialize all values to UNDEFINED for comparison purposes below.
      int aql = auxqsummary.length;
      for (int aqs = 0; aqs < aql; aqs++) {
        auxqsummary[aqs] = UNDEFINED;
      }

      int hash = 0;
      for (int i = 0; i < tstSize; i++) {
        Task t = tst.getTask(i);
	
	// Ignore results on DetermineRequirement tasks!
	if (t.getVerb().equals(Constants.Verb.DetermineRequirements)) {
	  ignoredTasks++;
	  continue;
	}

        AllocationResult ar = tst.getAllocationResult(i);
        if (ar == null) return null; // bail if undefined

        suc = suc && ar.isSuccess();
        rating += ar.getConfidenceRating();
        
        int[] definedaspects = ar.getAspectTypes();
        int al = definedaspects.length;
        for (int b = 0; b < al; b++) {
          // accumulate the values for the defined aspects
          switch (definedaspects[b]) {
          case START_TIME:
            acc[START_TIME] = Math.min(acc[START_TIME], ar.getValue(START_TIME));
            ap[START_TIME] = true;
            hash |= (1<<START_TIME);
            break;
          case END_TIME: 
            acc[END_TIME] = Math.max(acc[END_TIME], ar.getValue(END_TIME));
            ap[END_TIME] = true;
            hash |= (1<<END_TIME);
            break;
            // compute duration later
          case COST: 
            acc[COST] += ar.getValue(COST);
            ap[COST] = true;
            hash |= (1<<COST);
            break;
          case DANGER:
            acc[DANGER] = Math.max(acc[DANGER], ar.getValue(DANGER));
            ap[DANGER] = true;
            hash |= (1<<DANGER);
            break;
          case RISK: 
            acc[RISK] = Math.max(acc[RISK], ar.getValue(RISK));
            ap[RISK] = true;
            hash |= (1<<RISK);
            break;
          case QUANTITY:
            acc[QUANTITY] += ar.getValue(QUANTITY);
            ap[QUANTITY] = true;
            hash |= (1<<QUANTITY);
            break;
            // for now simply add the repetitve task values
          case INTERVAL: 
            acc[INTERVAL] += ar.getValue(INTERVAL);
            ap[INTERVAL] = true;
            hash |= (1<<INTERVAL);
            break;
          case TOTAL_QUANTITY: 
            acc[TOTAL_QUANTITY] += ar.getValue(TOTAL_QUANTITY);
            ap[TOTAL_QUANTITY] = true;
            hash |= (1<<TOTAL_QUANTITY);
            break;
          case TOTAL_SHIPMENTS:
            acc[TOTAL_SHIPMENTS] += ar.getValue(TOTAL_SHIPMENTS);
            ap[TOTAL_SHIPMENTS] = true;
            hash |= (1<<TOTAL_SHIPMENTS);
            break;
            //end of repetitive task specific aspects
          case CUSTOMER_SATISFACTION:
            acc[CUSTOMER_SATISFACTION] += ar.getValue(CUSTOMER_SATISFACTION);
            ap[CUSTOMER_SATISFACTION] = true;
            hash |= (1<<CUSTOMER_SATISFACTION);
            break;
          case READINESS:
            acc[READINESS] = Math.min(acc[READINESS], ar.getValue(READINESS));
            ap[READINESS] = true;
            hash |= (1<<READINESS);
            break;
	  }
        }
        
        // Sum up the auxiliaryquery data.  If there are conflicting data
        // values, send back nothing for that type.  If only one subtask
        // has information about a querytype, send it back in the 
        // aggregated result.
        for (int aq = 0; aq < AuxiliaryQueryType.AQTYPE_COUNT; aq++) {
          String data = ar.auxiliaryQuery(aq);
          if (data != null) {
            String sumdata = auxqsummary[aq];
            // if sumdata = null, there has already been a conflict.
            if (sumdata != null) {
              if (sumdata.equals(UNDEFINED)) {
                // there's not a value yet, so use this one.
                auxqsummary[aq] = data;
              } else if (! data.equals(sumdata)) {
                // there's a conflict, pass back null
                auxqsummary[aq] = null;
              }
            }
          }
        }

      } // end of looping through all subtasks
      
      // compute duration IFF defined.
      if (ap[START_TIME] && ap[END_TIME]) {
        acc[DURATION] = acc[END_TIME] - acc[START_TIME];
        ap[DURATION] = true;
        hash |= (1<<DURATION);
      } else {
        // redundant
        acc[DURATION] = 0.0;
        ap[DURATION] = false;
      }

      if (tstSize>0) {
        acc[CUSTOMER_SATISFACTION] /= (tstSize - ignoredTasks);
        rating /= (tstSize - ignoredTasks);

	// Do not propagate fractional confidence, to avoid
	// rounding errors later (no, this shouldnt be happening, but....)
	if (rating < 1.0d)
	  rating = 0.0;
      }

      boolean delta = false;
      
      // only check the defined aspects and make sure that the currentar is not null
      if (currentar == null) {
        delta = true;		// if the current ar == null then set delta true
      } else {
        int[] caraspects = currentar.getAspectTypes();
        if (caraspects.length != acc.length) {
          //if the current ar length is different than the length of the new
          // calculations (acc) there's been a change
          delta = true;
        } else {
          int il = caraspects.length;
          for (int i = 0; i < il; i++) {
            int da = caraspects[i];
            if (ap[da] && acc[da] != currentar.getValue(da)) {
              delta = true;
              break;
            }
          }
        }
      
        if (!delta) {
	  if (currentar.isSuccess() != suc) {
	    delta = true;
	  } else if (Math.abs(currentar.getConfidenceRating() - rating) > SIGNIFICANT_CONFIDENCE_RATING_DELTA) {
	    delta = true;
	  }
        }
      }

      if (delta) {
        int keys[] = _STANDARD_ASPECTS;
        int al = AspectType._ASPECT_COUNT;

        // see if we should compress the results array
        int lc = 0;
        for (int b = 0; b < al; b++) {
          if (ap[b]) lc++;
        }
      
        if (lc < al) {            // need to compress the arrays
          double nv[] = new double[lc];

          // lazy cache the key patterns
          synchronized (hack) {
            Integer ihash = new Integer(hash);
            KeyHolder kh = (KeyHolder) hack.get(ihash);
            if (kh == null) {
              //System.err.println("Caching key "+hash);
              int nk[] = new int[lc];
              int i = 0;
              for (int b = 0; b<al; b++) {
                if (ap[b]) {
                  nv[i] = acc[b];
                  nk[i] = keys[b];
                  i++;
                }
              }
              acc = nv;
              keys = nk;
              kh = new KeyHolder(nk);
              hack.put(ihash,kh);
            } else {
              keys = kh.keys;
              int i = 0;
              for (int b = 0; b<al; b++) {
                if (ap[b]) {
                  nv[i] = acc[b];
                  i++;
                }
              }
              acc = nv;
            }
          }

        }

        AllocationResult artoreturn = new AllocationResult(rating, suc, keys, acc);

        int aqll = auxqsummary.length;
        for (int aqt = 0; aqt < aql; aqt++) {
          String aqdata = auxqsummary[aqt];
          if ( (aqdata !=null) && (aqdata != UNDEFINED) ) {
            artoreturn.addAuxiliaryQueryInfo(aqt, aqdata);
          }
        }
        return artoreturn;
      } else {
        return currentar;
      }
    }
  }

  HashMap hack = new HashMap();
  
  final class KeyHolder {
    public int[] keys;
    public KeyHolder(int keys[]) {
      this.keys = keys;
    }
  }
}



