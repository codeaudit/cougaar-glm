/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.inventory;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.plugins.AssetUtils;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.planning.ldm.plan.AspectRate;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Constraint;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewConstraint;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TaskScoreTable;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * SupplyExpander expands supply tasks into withdraw tasks and if needed
 * Transport and Load Tasks.  The SupplyExpander also updates the allocation
 * result whenever one of the subtasks are changed.
 * It runs for new(added) and changed Supply tasks.
 * This processor should be included IFF inventory is being handled in this cluster.
 */

public class SupplyExpander extends InventoryProcessor {
  private static Logger logger = Logging.getLogger(SupplyExpander.class);

  /**
   * Define an ARA that can deal with the expansion of a
   * ProjectSupply task. Mostly, we just clone the result of the
   * ProjectWithdraw task.
   */
  private static class ProjectionARA implements AllocationResultAggregator {
    public AllocationResult calculate(Workflow wf, TaskScoreTable tst, AllocationResult currentar) {
      if (tst.size() != 1)
        throw new IllegalArgumentException("projectionARA: multiple subtasks");
      AllocationResult ar = (AllocationResult) tst.getAllocationResult(0);
      if (ar == null)
        return null;
      if (ar.isEqual(currentar))
        return currentar;
      return (AllocationResult) ar.clone();
    }
  }

  private static class SupplyARA implements AllocationResultAggregator {
    public AllocationResult calculate(Workflow wf, TaskScoreTable tst, AllocationResult currentar) {
      AspectValue[] merged = new AspectValue[AlpineAspectType.LAST_ALPINE_ASPECT + 1];
      long startTime = Long.MAX_VALUE;
      long endTime = Long.MIN_VALUE;
      boolean success = true;
      float rating = 0.0f;
      int tstSize = tst.size();
      AllocationResult withdrawAR = null; // Remember this when we see it
      long time;
      Task parentTask = wf.getParentTask();
      PlanElement pe = parentTask.getPlanElement();
      AllocationResultHelper helper = new AllocationResultHelper(parentTask, null);
      AllocationResult bestAR = helper.getAllocationResult();
      AspectValue[] curr = bestAR.getAspectValueResults();

      for (int i = 0; i < curr.length; i++) {
        AspectValue av = curr[i];
        int type = av.getAspectType();
        merged[type] = av;
        switch (type) {
          case START_TIME:
            startTime = (long) av.getValue();
            break;
          case END_TIME:
            endTime = (long) av.getValue();
            break;
        }
      }
      for (int i = 0; i < tstSize; i++) {
        AllocationResult ar = tst.getAllocationResult(i);
        if (ar == null)
          return null; // bail if undefined
        Task t = tst.getTask(i);
        Verb verb = t.getVerb();
        boolean isWithdraw = verb.equals(Constants.Verb.Withdraw) || verb.equals(Constants.Verb.ProjectWithdraw);
        if (isWithdraw) {
          if (ar == null)
            return null;
          withdrawAR = ar;
        }
        AspectValue[] avs = ar.getAspectValueResults();
        success = success && ar.isSuccess();
        rating += ar.getConfidenceRating();
        for (int j = 0; j < avs.length; j++) {
          int type = avs[j].getAspectType();
          switch (type) {
            case AspectType.START_TIME:
              break;
            case AspectType.END_TIME:
              break;
            case AspectType.QUANTITY:
              if (isWithdraw)
                merged[AspectType.QUANTITY] = avs[j];
              break;
            default:
              if (!isWithdraw)
                merged[type] = avs[j];
          }
        }
      }
      List mergedPhasedResults = new ArrayList();
      List withdrawPhasedResults = withdrawAR.getPhasedAspectValueResults();
      for (int i = 0, n = withdrawPhasedResults.size(); i < n; i++) {
        AspectValue[] oneResult = (AspectValue[]) withdrawPhasedResults.get(i);
        mergedPhasedResults.add(merge(merged, oneResult));
      }
      return new AllocationResult(rating / tstSize, success, merge(merged, null), mergedPhasedResults);
    }

    /**
     * Merges an array of AspectValue indexed by AspectType and an
     * unindexed array of AspectValues into an unindexed array of
     * AspectValues.
     */
    private AspectValue[] merge(AspectValue[] rollup, AspectValue[] phased) {
      if (phased != null) {
        rollup = (AspectValue[]) rollup.clone(); // Don't clobber the original
        for (int i = 0; i < phased.length; i++) {
          AspectValue av = phased[i];
          if (av != null)
            rollup[av.getAspectType()] = av;
        }
      }
      int nAspects = 0;
      for (int i = 0; i < rollup.length; i++) {
        if (rollup[i] != null)
          nAspects++;
      }
      AspectValue[] result = new AspectValue[nAspects];
      int aspect = 0;
      for (int i = 0; i < rollup.length; i++) {
        if (rollup[i] != null)
          result[aspect++] = rollup[i];
      }
      return result;
    }
  }

  private IncrementalSubscription projectExpansions_;
  private IncrementalSubscription supplyExpansions_;

  public static final long TRANSPORT_TIME = 24 * MSEC_PER_HOUR; // second day
  public static final long LOAD_TIME = 4 * MSEC_PER_HOUR; // 4 hours
  public static final Verb WITHDRAWVERB = Verb.get(Constants.Verb.WITHDRAW);
  public static final Verb PROJECTWITHDRAWVERB = Constants.Verb.ProjectWithdraw;
  public static final Verb TRANSPORTVERB = Verb.get(Constants.Verb.TRANSPORT);
  public static final Verb LOADVERB = Verb.get(Constants.Verb.LOAD);
  private static AllocationResultAggregator projectionARA = new ProjectionARA();
  private static AllocationResultAggregator supplyARA = new SupplyARA();
  protected boolean addTransport; // Add load tasks when expanding supply tasks
  protected boolean addLoad;      // Add transport tasks when expanding supply tasks

  /**
   * Constructor takes this processor's plugin, organization and the type of
   * supply tasks that shall be handled.
   */
  public SupplyExpander(InventoryPlugin plugin, Organization org, String type) {
    super(plugin, org, type);
    supplyType_ = type;
    //  	supplyExpansionElements_ = subscribe(new SupplyExpansionTaskPredicate(supplyType_));
    //  	projectExpansionElements_ = subscribe(new ProjectionExpansionTaskPredicate(supplyType_));
    projectExpansions_ = subscribe(new ProjectionExpansionPredicate(supplyType_, myOrgName_));
    supplyExpansions_ = subscribe(new SupplyExpansionPredicate(supplyType_, myOrgName_));
    addTransport = getBooleanParam(supplyType_ + "Transport");
    addLoad = getBooleanParam(supplyType_ + "Load");

  }

  private boolean getBooleanParam(String paramName) {
    Boolean bool = (Boolean) inventoryPlugin_.getParam(paramName);
    return (bool != null && bool.booleanValue());
  }

  // Subscribe to Single supplyType_ Supply Task
  static class SupplyExpansionTaskPredicate implements UnaryPredicate {
    String supplyType_;

    public SupplyExpansionTaskPredicate(String type) {
      supplyType_ = type;
    }

    public boolean execute(Object o) {
      if (o instanceof Expansion) {
        Task task = ((Expansion) o).getTask();
        Verb task_verb = task.getVerb();
        if (task_verb.equals(Constants.Verb.TRANSPORT) || task_verb.equals(Constants.Verb.LOAD) ||
            task_verb.equals(Constants.Verb.WITHDRAW)) {
          return TaskUtils.isDirectObjectOfType(task, supplyType_);
        }
      }
      return false;
    }
  }

  static class ProjectionExpansionTaskPredicate implements UnaryPredicate {
    String supplyType_;

    public ProjectionExpansionTaskPredicate(String type) {
      supplyType_ = type;
    }

    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
        Task task = ((PlanElement) o).getTask();
        Verb task_verb = task.getVerb();
        if (task_verb.equals(Constants.Verb.PROJECTWITHDRAW)) {
          return TaskUtils.isDirectObjectOfType(task, supplyType_);
        }
      }
      return false;
    }
  }

  static class ProjectionExpansionPredicate implements UnaryPredicate {
    String supplyType_;
    UnaryPredicate taskPredicate;

    public ProjectionExpansionPredicate(String type, String orgname) {
      supplyType_ = type;
      taskPredicate = new InventoryProcessor.ProjectionTaskPredicate(type, orgname);
    }

    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
        Task task = ((PlanElement) o).getTask();
        return taskPredicate.execute(task);
      }
      return false;
    }
  }

  static class SupplyExpansionPredicate implements UnaryPredicate {
    String supplyType_;
    UnaryPredicate taskPredicate;

    public SupplyExpansionPredicate(String type, String orgname) {
      supplyType_ = type;
      taskPredicate = new InventoryProcessor.SupplyTaskPredicate(type, orgname);
    }

    public boolean execute(Object o) {
      if (o instanceof Expansion) {
        Task task = ((Expansion) o).getTask();
        return taskPredicate.execute(task);
      }
      return false;
    }
  }


  /**
   * This method is called everytime a subscription has changed.
   */
  public void update() {
    if ((inventoryPlugin_.getDetermineRequirementsTask() == null) || !needUpdate()) {
      return;
    }
    super.update(); // set up dates
    handleExpandableTasks(supplyTasks_.getAddedList());
    updateExpansion(supplyTasks_.getChangedList());
    handleExpandableTasks(projectionTasks_.getAddedList());
    updateExpansion(projectionTasks_.getChangedList());
    PluginHelper.updateAllocationResult(projectExpansions_);
    handleExpansionChanges(supplyExpansions_.getChangedList());
    PluginHelper.updateAllocationResult(supplyExpansions_);
  }

  private boolean needUpdate() {
    if (supplyTasks_.hasChanged())
      return true;
    if (projectionTasks_.hasChanged())
      return true;
    if (supplyExpansions_.hasChanged())
      return true;
    if (projectExpansions_.hasChanged())
      return true;
    return false;
  }

  /**
   * Expands an enumeration of Supply tasks *
   */
  protected void handleExpandableTasks(Enumeration tasks) {
    Task supplyTask;
    Inventory inv = null;
    Asset proto;
    String id;
    int tasksExpanded = 0;
    while (tasks.hasMoreElements()) {
      supplyTask = (Task) tasks.nextElement();
      proto = (Asset) supplyTask.getDirectObject();
      // If we cannot allocate the task to inventory then ignore it.
      // The external allocator will forward it onto a re-supply cluster.
      inv = inventoryPlugin_.findOrMakeInventory(supplyType_, proto);
      if (inv != null) {
        tasksExpanded++;
        //  		printDebug("handleExpandableTasks(), <"+supplyType_+">, Expanding "+TaskUtils.taskDesc(supplyTask));
        expandSupplyTask(supplyTask);
      } else {
        id = proto.getTypeIdentificationPG().getTypeIdentification();
        if (logger.isDebugEnabled()) {
          logger.debug("handleExpandableTasks(), <" + supplyType_ + ">, could not allocate " + id);
        }
      }
    }
    //  	printDebug("handleExpandableTasks() <"+supplyType_+"> expanded "+
    //  		   tasksExpanded+" tasks.");
  }

  protected void updateExpansion(Enumeration tasks) {
    Task supplyTask;
    Inventory inv = null;
    Asset proto;
    while (tasks.hasMoreElements()) {
      supplyTask = (Task) tasks.nextElement();
      proto = (Asset) supplyTask.getDirectObject();
      inv = inventoryPlugin_.findOrMakeInventory(supplyType_, proto);
      if (inv != null) {
        PlanElement pe = supplyTask.getPlanElement();
        if (pe instanceof Expansion) {
          if (logger.isDebugEnabled()) {
            logger.debug("updateExpansion(), Withdraw REPLAN, Supply task changed " + TaskUtils.taskDesc(supplyTask));
          }
          publishRemoveExpansion((Expansion) pe);
          expandSupplyTask(supplyTask);
        }
      }
    }
  }

  /**
   * Expands a Supply task into a withdraw task *
   */
  protected void expandSupplyTask(Task parentTask) {
    if (parentTask.getVerb().equals(Constants.Verb.SUPPLY)) {
      expandRealSupplyTask(parentTask);
    } else if (parentTask.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
      expandProjectionTask(parentTask);
    }
  }

  /**
   * Expand a ProjectSupply into a ProjectWithdraw
   * InventoryBG will sort out the details of dates and quantities
   * later.
   */
  private void expandProjectionTask(Task parent_task) {
    Vector expand_tasks = new Vector();
    NewTask withdrawTask = createProjectSupplyWithdrawTask(parent_task);
    expand_tasks.addElement(withdrawTask);
    publishExpansion(parent_task, expand_tasks);
    Expansion expansion = (Expansion) parent_task.getPlanElement();
    NewWorkflow wf = (NewWorkflow) expansion.getWorkflow();
    wf.setAllocationResultAggregator(projectionARA);
    AllocationResult ar = new AllocationResultHelper(parent_task, null).getAllocationResult(1.0);
    expansion.setEstimatedResult(ar);
  }

  private void expandRealSupplyTask(Task parentTask) {
    Vector expand_tasks = new Vector();
    NewTask withdrawTask = createSupplyWithdrawTask(parentTask);
    expand_tasks.addElement(withdrawTask);
    NewTask transportTask = null;
    NewTask loadTask = null;
    withdrawTask.addObservableAspect(AspectType.END_TIME);
    withdrawTask.addObservableAspect(AspectType.QUANTITY);
    if (addLoad) {
      loadTask = createLoadTask(parentTask, withdrawTask);
      loadTask.addObservableAspect(AspectType.START_TIME);
      loadTask.addObservableAspect(AspectType.END_TIME);
      loadTask.addObservableAspect(AspectType.QUANTITY);
      expand_tasks.addElement(loadTask);
    }
    if (addTransport) {
      transportTask = createTransportTask(parentTask, withdrawTask);
      transportTask.addObservableAspect(AspectType.START_TIME);
      transportTask.addObservableAspect(AspectType.END_TIME);
      transportTask.addObservableAspect(AspectType.QUANTITY);
      expand_tasks.addElement(transportTask);
    }
    ((NewTask) parentTask).addObservableAspect(AspectType.END_TIME);
    publishExpansion(parentTask, expand_tasks);
    Expansion expansion = (Expansion) parentTask.getPlanElement();
    AllocationResult ar = new AllocationResultHelper(parentTask, null).getAllocationResult(1.0);
    expansion.setEstimatedResult(ar);
    NewWorkflow wf = (NewWorkflow) expansion.getWorkflow();
    wf.setAllocationResultAggregator(supplyARA);
    NewConstraint constraint;
    if (transportTask != null) {
      // Constraint start of transport
      constraint = ldmFactory_.newConstraint();
      constraint.setConstrainedTask(transportTask);
      constraint.setConstrainedAspect(AspectType.START_TIME);
      if (loadTask != null) {
        // Constrain load before transport
        constraint.setConstrainingTask(loadTask);
        constraint.setConstrainingAspect(AspectType.END_TIME);
      } else {
        // Constrain withdraw before transport
        constraint.setConstrainingTask(withdrawTask);
        constraint.setConstrainingAspect(AspectType.END_TIME);
      }
      constraint.setConstraintOrder(Constraint.COINCIDENT); // Artificial
      wf.addConstraint(constraint);
      // Constraint QUANTITY of transport
      constraint = ldmFactory_.newConstraint();
      constraint.setConstrainedTask(transportTask);
      constraint.setConstrainedAspect(AspectType.QUANTITY);
      if (loadTask != null) {
        // Constrain transport quantity equal to load quantity
        constraint.setConstrainingTask(loadTask);
        constraint.setConstrainingAspect(AspectType.QUANTITY);
      } else {
        // Constrain transport quantity equal to withdraw quantity
        constraint.setConstrainingTask(withdrawTask);
        constraint.setConstrainingAspect(AspectType.QUANTITY);
      }
      constraint.setConstraintOrder(Constraint.EQUALTO); // Artificial
      wf.addConstraint(constraint);
    }
    if (loadTask != null) {
      // Constraint start of load
      constraint = ldmFactory_.newConstraint();
      constraint.setConstrainedTask(loadTask);
      constraint.setConstrainedAspect(AspectType.START_TIME);
      constraint.setConstrainingTask(withdrawTask);
      constraint.setConstrainingAspect(AspectType.END_TIME);
      constraint.setConstraintOrder(Constraint.COINCIDENT); // Artificial
      wf.addConstraint(constraint);
      // Constraint quantity of load
      constraint = ldmFactory_.newConstraint();
      constraint.setConstrainedTask(loadTask);
      constraint.setConstrainedAspect(AspectType.QUANTITY);
      constraint.setConstrainingTask(withdrawTask);
      constraint.setConstrainingAspect(AspectType.QUANTITY);
      constraint.setConstraintOrder(Constraint.EQUALTO); // Artificial
      wf.addConstraint(constraint);
    }
    // Constraint end of parentTask
    constraint = ldmFactory_.newConstraint();
    constraint.setConstrainedTask(parentTask);
    constraint.setConstrainedAspect(AspectType.END_TIME);
    if (transportTask != null) {
      constraint.setConstrainingTask(transportTask);
    } else if (loadTask != null) {
      constraint.setConstrainingTask(loadTask);
    } else {
      constraint.setConstrainingTask(withdrawTask);
    }
    constraint.setConstrainingAspect(AspectType.END_TIME);
    constraint.setConstraintOrder(Constraint.COINCIDENT); // Artificial
    wf.addConstraint(constraint);
    // Constraint quantity of parent task
    constraint = ldmFactory_.newConstraint();
    constraint.setConstrainedTask(parentTask);
    constraint.setConstrainedAspect(AspectType.QUANTITY);
    if (transportTask != null) {
      constraint.setConstrainingTask(transportTask);
    } else if (loadTask != null) {
      constraint.setConstrainingTask(loadTask);
    } else {
      constraint.setConstrainingTask(withdrawTask);
    }
    constraint.setConstrainingAspect(AspectType.QUANTITY);
    constraint.setConstraintOrder(Constraint.EQUALTO); // Artificial
    wf.addConstraint(constraint);
  }

  /**
   * Handle changes to our expansions of Supply tasks. Basically, a
   * failure causes all unfailed subtasks to be rescinded. The will
   * get recreated if the incoming supply task ever gets revived.
   */
  protected void handleExpansionChanges(Enumeration expansions) {
    AllocationResult ar;
    while (expansions.hasMoreElements()) {
      Expansion exp = (Expansion) expansions.nextElement();
      ar = exp.getReportedResult();
      if (ar != null && !ar.isSuccess()) {
        NewWorkflow wf = (NewWorkflow) exp.getWorkflow();
        for (Enumeration tasks = wf.getTasks(); tasks.hasMoreElements();) {
          Task subtask = (Task) tasks.nextElement();
          PlanElement pe = subtask.getPlanElement();
          if (pe != null) { // Null if being rescinded by customer
            ar = pe.getEstimatedResult();
            if (!ar.isSuccess())
              continue;
          }
          wf.removeTask(subtask);
          delegate_.publishRemove(subtask);
        }
      }
    }
  }

  /**
   * creates a Withdraw task from a Supply task *
   */
  protected NewTask createWithdrawTask(Task parent_task) {

    // Create new task
    Asset prototype = parent_task.getDirectObject();
    NewTask subtask = ldmFactory_.newTask();
    // attach withdraw task to parent and fill it in
    subtask.setDirectObject(prototype);
    subtask.setParentTask(parent_task);
    subtask.setPlan(parent_task.getPlan());
    subtask.setPrepositionalPhrases(parent_task.getPrepositionalPhrases());
    subtask.setPriority(parent_task.getPriority());
    subtask.setSource(clusterId_);
    if (parent_task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
      subtask.setVerb(PROJECTWITHDRAWVERB);
    } else {
      subtask.setVerb(WITHDRAWVERB);
    }
    // Copy all preferences that aren't used for repetitive tasks
    Vector prefs = new Vector();
    int aspect_type;
    Preference pref;
    Enumeration preferences = parent_task.getPreferences();
    while (preferences.hasMoreElements()) {
      pref = (Preference) preferences.nextElement();
      aspect_type = pref.getAspectType();
      // Quanity added to withdraw by task specific method.
      // Inerval and DemandRate are not added to withdraw task.
      if ((aspect_type != AspectType.QUANTITY) && (aspect_type != AspectType.INTERVAL) &&
          (aspect_type != AlpineAspectType.DEMANDRATE)) {
        prefs.addElement(pref);
      }
    }
    subtask.setPreferences(prefs.elements());
    return subtask;
  }

  protected NewTask createSupplyWithdrawTask(Task parent_task) {

    NewTask subtask = createWithdrawTask(parent_task);
    long anticipation = 0L;
    if (addTransport)
      anticipation += TRANSPORT_TIME;
    if (addLoad)
      anticipation += LOAD_TIME;
    // Add preferences for QUANTITY
    double quantity = TaskUtils.getPreference(parent_task, AspectType.QUANTITY);
    Preference p_qty = createQuantityPreference(AspectType.QUANTITY, quantity);
    subtask.addPreference(p_qty);
    addEndTimePref(subtask, TaskUtils.getEndTime(parent_task) - anticipation);
    //   	printDebug(1, "CreateSupplyWithdrawTask() "+
    //   		   TaskUtils.taskDesc(subtask)+" with end date:"+
    //   		   TimeUtils.dateString(TaskUtils.getEndTime(subtask)));
    return subtask;
  }

  protected NewTask createProjectSupplyWithdrawTask(Task parent_task) {
    NewTask subtask = createWithdrawTask(parent_task);
    Preference pref = parent_task.getPreference(AlpineAspectType.DEMANDRATE);
    if (pref.getScoringFunction().getBest().getAspectValue() instanceof AspectRate) {
    } else {
      if (logger.isErrorEnabled()) {
        logger.error("SupplyExpander DEMANDRATE preference not AspectRate:" + pref);
      }
    }
    subtask.addPreference(pref);
    //    	 printDebug(1, "CreateProjectSupplyWithdrawTask() with start date:"+
    //    		    TimeUtils.dateString(TaskUtils.getStartTime(subtask))+", with end dat:"+
    //  		    TimeUtils.dateString(TaskUtils.getEndTime(subtask))+", quantity is "+quantity);

    return subtask;
  }

  private static final String THEATER_TRANSPORT = "TheaterTransportation";

  // task coming in is the parent task
  private NewTask createTransportTask(Task parent_task, Task wdraw_task) {

    // This is the part to transport
    Asset part = parent_task.getDirectObject();
    // This is the number of parts to transport
    int quantity = (int) TaskUtils.getPreference(wdraw_task, AspectType.QUANTITY);

    NewTask subtask = ldmFactory_.newTask();

    AggregateAsset aggAsset = (AggregateAsset) ldmFactory_.createAggregate(part, quantity);

    subtask.setParentTask(parent_task);

    // START TIME & END TIME
    long parent_end = TaskUtils.getEndTime(parent_task);
    long start = parent_end - TRANSPORT_TIME;
    long end = parent_end;

    Preference startPref = createTransportStartPref(start);
    subtask.addPreference(startPref);

    Preference endPref = createTransportEndPref(end);
    subtask.addPreference(endPref);

    // Fill in preposition phrases.
    Vector pps = new Vector();

    // From

    NewPrepositionalPhrase prep_phrase = ldmFactory_.newPrepositionalPhrase();
    prep_phrase.setPreposition(Constants.Preposition.FROM);
    Enumeration geolocs = AssetUtils.getGeolocLocationAtTime(myOrganization_, start);
    GeolocLocation geoloc = null;
    if (geolocs.hasMoreElements()) {
      geoloc = (GeolocLocation) geolocs.nextElement();
    } else {
      try {
        //  		printDebug("SupplyExpander, Using HomeLocation for transport");
        geoloc = (GeolocLocation) myOrganization_.getMilitaryOrgPG().getHomeLocation();
      } catch (NullPointerException npe) {
        if (logger.isErrorEnabled()) {
          logger.error("SupplyExpander, Unable to find Location for Transport");
        }
      }
    }
    //  	printDebug("SupplyExpander, At "+TimeUtils.dateString(start)+ " the geoloc is "+geoloc);
    prep_phrase.setIndirectObject(geoloc);
    pps.addElement(prep_phrase);

    // To
    PrepositionalPhrase pp_to = parent_task.getPrepositionalPhrase(Constants.Preposition.TO);
    if (pp_to != null) {
      prep_phrase = ldmFactory_.newPrepositionalPhrase();
      prep_phrase.setPreposition(Constants.Preposition.TO);
      prep_phrase.setIndirectObject((GeolocLocation) pp_to.getIndirectObject());
      pps.addElement(prep_phrase);
    } else {
      // ???????????????????
      // What to do in this case?  Transport to nowhere?  just return at this point?
      if (logger.isErrorEnabled()) {
        logger.error("Missing TO Preposition on input task " + parent_task);
      }
      return null;
    }

    // Ready at
    //  	prep_phrase = ldmFactory_.newPrepositionalPhrase();
    //  	prep_phrase.setPreposition( Preposition.READYAT );
    // CHECK SCHEDULES -- RJB
    // 	Schedule s = ldmFactory_.newSimpleSchedule(allocated_date, allocated_date);
    //  	Schedule s = ldmFactory_.newSimpleSchedule(allocated_date, addNDays(allocated_date,1));
    //  	prep_phrase.setIndirectObject(s);
    //  	pps.addElement(prep_phrase);

    // For
    prep_phrase = ldmFactory_.newPrepositionalPhrase();
    prep_phrase.setPreposition(Constants.Preposition.FOR);
    String orgName = myOrganization_.getItemIdentificationPG().getItemIdentification();
    prep_phrase.setIndirectObject(orgName);
    // 	prep_phrase.setIndirectObject(myOrganization_);
    pps.addElement(prep_phrase);


    String ofTypeString = THEATER_TRANSPORT;
    // if parent task has an ofType <commodity> copy it into the Transport task
    // this allows users of the transport task to do commodity based routing
    PrepositionalPhrase pp_commodityType = parent_task.getPrepositionalPhrase(Constants.Preposition.OFTYPE);
    if (pp_commodityType != null) {
      String ind_obj = (String) pp_commodityType.getIndirectObject();
      ofTypeString = ind_obj + "_" + ofTypeString;
    }

    // OfType
    prep_phrase = ldmFactory_.newPrepositionalPhrase();
    prep_phrase.setPreposition(Constants.Preposition.OFTYPE);
    Asset transport_asset = ldmFactory_.createPrototype("AbstractAsset", ofTypeString);
    NewTypeIdentificationPG tipg = (NewTypeIdentificationPG) transport_asset.getTypeIdentificationPG();
    tipg.setTypeIdentification(ofTypeString);
    prep_phrase.setIndirectObject(transport_asset);
    pps.addElement(prep_phrase);


    // Fill in verb.
    subtask.setDirectObject(aggAsset);
    subtask.setPrepositionalPhrases(pps.elements());
    subtask.setVerb(TRANSPORTVERB);
    subtask.setPlan(parent_task.getPlan());

    // Quantity Preference
    Preference quantity_pf = createQuantityPreference(AspectType.QUANTITY, quantity);
    subtask.addPreference(quantity_pf);

    subtask.setSource(clusterId_);
    //  	printDebug("Created transport task:" + taskDesc(subtask));
    return subtask;
  }

  // LOAD Task
  private NewTask createLoadTask(Task parent_task, Task wdraw_task) {

    NewTask subtask = ldmFactory_.newTask();
    Asset asset = parent_task.getDirectObject();

    // Quantity Preference
    int quantity = (int) TaskUtils.getPreference(wdraw_task, AspectType.QUANTITY);
    Preference quantity_pf = createQuantityPreference(AspectType.QUANTITY, quantity);
    subtask.addPreference(quantity_pf);

    //  	// START TIME & END TIME
    addStartTimePref(subtask, TaskUtils.getEndTime(wdraw_task));
    addEndTimePref(subtask, TaskUtils.getEndTime(wdraw_task) + LOAD_TIME);

    // For
    Vector pps = new Vector();
    NewPrepositionalPhrase prep_phrase = ldmFactory_.newPrepositionalPhrase();
    prep_phrase.setPreposition(Constants.Preposition.FOR);
    String orgName = myOrganization_.getItemIdentificationPG().getItemIdentification();
    prep_phrase.setIndirectObject(orgName);
    // 	prep_phrase.setIndirectObject(myOrganization_);
    pps.addElement(prep_phrase);

    subtask.setParentTask(parent_task);
    subtask.setDirectObject(asset);

    // Fill in verb.
    subtask.setPrepositionalPhrases(pps.elements());
    subtask.setVerb(LOADVERB);
    subtask.setPlan(parent_task.getPlan());

    subtask.setSource(clusterId_);
    //  	printDebug("Created transport task:" + taskDesc(subtask));
    return subtask;
  }

  /**
   * Creates a start and end preference and attaches them to a task *
   */
  protected void addEndTimePref(NewTask task, long end) {
    Preference p_end = createDateBeforePreference(AspectType.END_TIME, end);
    task.addPreference(p_end);
  }

  protected void addStartTimePref(NewTask task, long start) {
    Preference p_start = createDateAfterPreference(AspectType.START_TIME, start);
    task.addPreference(p_start);
  }

}
