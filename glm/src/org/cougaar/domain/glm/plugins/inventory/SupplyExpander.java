/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBNT Solutions LLC,
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 2000 by
 *             BBNT Solutions LLC,
 *             all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.inventory;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.CountRate;
import org.cougaar.domain.planning.ldm.measure.FlowRate;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.Constraint;
import org.cougaar.domain.planning.ldm.plan.Disposition;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.NewConstraint;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.util.UnaryPredicate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.asset.Inventory;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.plan.AlpineAspectType;
import org.cougaar.domain.glm.plan.GeolocLocation;
import org.cougaar.domain.glm.plugins.AssetUtils;
import org.cougaar.domain.glm.plugins.ScheduleUtils;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;

/** SupplyExpander expands supply tasks into withdraw tasks and if needed 
 *  Transport and Load Tasks.  The SupplyExpander also updates the allocation
 *  result whenever one of the subtasks are changed.
 *
 *  It runs for new(added) and changed Supply tasks.
 *  This processor should be included IFF inventory is being handled in this cluster.
 **/

public class SupplyExpander extends InventoryProcessor {

    // subPlanElements_ is a subscription that looks for ANTS output task
    // This allows allocation results on the sub-task to be propagated
    protected IncrementalSubscription        supplyExpansionElements_;
    protected IncrementalSubscription        projectExpansionElements_;
    
    public static final long                 TRANSPORT_TIME = 24 * MSEC_PER_HOUR; // second day
    public static final long                 LOAD_TIME      = 4 * MSEC_PER_HOUR; // 4 hours
    public static final Verb                 WITHDRAWVERB = new Verb(Constants.Verb.WITHDRAW);
    public static final Verb                 PROJECTWITHDRAWVERB = new Verb(Constants.Verb.PROJECTWITHDRAW);
    public static final Verb                 TRANSPORTVERB = new Verb(Constants.Verb.TRANSPORT);
    public static final Verb                 LOADVERB = new Verb(Constants.Verb.LOAD);
    protected boolean addTransport; // Add load tasks when expanding supply tasks
    protected boolean addLoad;      // Add transport tasks when expanding supply tasks

    /** Constructor takes this processor's plugin, organization and the type of 
     *  supply tasks that shall be handled.
     **/
    public SupplyExpander(InventoryPlugIn plugin, Organization org, String type)
    {
	super(plugin, org, type);
	supplyType_ = type;
	supplyExpansionElements_ = subscribe(new SupplyExpansionTaskPredicate(supplyType_));
	projectExpansionElements_ = subscribe(new ProjectionExpansionTaskPredicate(supplyType_));
        addTransport = getBooleanParam(supplyType_ + "Transport");
        addLoad = getBooleanParam(supplyType_ + "Load");

    }

    private boolean getBooleanParam(String paramName) {
        Boolean bool = (Boolean) inventoryPlugIn_.getParam(paramName);
        return (bool != null && bool.booleanValue());
    }

    // Subscribe to Single supplyType_ Supply Task
    static class SupplyExpansionTaskPredicate implements UnaryPredicate
    {
	String supplyType_;

	public SupplyExpansionTaskPredicate(String type) {
	    supplyType_ = type;
	}

	public boolean execute(Object o) {
	    if (o instanceof PlanElement ) {
		Task task = ((PlanElement)o).getTask();
		Verb task_verb = task.getVerb();
		if (task_verb.equals(Constants.Verb.TRANSPORT) ||
		    task_verb.equals(Constants.Verb.LOAD) ||
		    task_verb.equals(Constants.Verb.WITHDRAW)) {
		    return TaskUtils.isDirectObjectOfType(task, supplyType_);
		}
	    }
	    return false;
	}
    };

    static class ProjectionExpansionTaskPredicate implements UnaryPredicate
    {
	String supplyType_;

	public ProjectionExpansionTaskPredicate(String type) {
	    supplyType_ = type;
	}

	public boolean execute(Object o) {
	    if (o instanceof PlanElement ) {
		Task task = ((PlanElement)o).getTask();
		Verb task_verb = task.getVerb();
		if (task_verb.equals(Constants.Verb.PROJECTWITHDRAW)) {
		    return TaskUtils.isDirectObjectOfType(task, supplyType_);
		}
	    }
	    return false;
	}
    };
    

    /** This method is called everytime a subscription has changed. */
    public void update() {
	if ((inventoryPlugIn_.getDetermineRequirementsTask() == null) ||
	    !needUpdate()) {
	    return;
	}
	super.update(); // set up dates
	handleExpandableTasks(supplyTasks_.getAddedList());
	handleExpandableTasks(supplyTasks_.getChangedList());
	handleExpandableTasks(projectionTasks_.getAddedList());
	handleExpandableTasks(projectionTasks_.getChangedList());
 	updateSupplyResults(supplyExpansionElements_.getAddedList());
	updateSupplyResults(supplyExpansionElements_.getChangedList());
	//
 	updateProjectionResults(projectExpansionElements_.getAddedList());
	updateProjectionResults(projectExpansionElements_.getChangedList());
    }

    public void updateProjectionResults(Enumeration expansion){
	// does nothig for now
    }

    private boolean needUpdate() {
	boolean update = false;
	if (supplyTasks_.elements().hasMoreElements()) {
	    update = true;
	}
	else if (supplyExpansionElements_.getChangedList().hasMoreElements()) {
	    update = true;
	} 
	else if (projectExpansionElements_.getChangedList().hasMoreElements()) {
	    update = true;
	} 
	else if (projectionTasks_.elements().hasMoreElements()) {
	    update = true;
	}
	return update;
    }

    /** Expands an enumeration of Supply tasks **/
    protected void handleExpandableTasks(Enumeration tasks) {
	Task supplyTask;
	Inventory inv = null;
	Asset proto;
	String id;
	int tasksExpanded = 0;
	while (tasks.hasMoreElements()) {
	    supplyTask = (Task)tasks.nextElement();
	    proto = (Asset)supplyTask.getDirectObject();
	    // If we cannot allocate the task to inventory then ignore it.
	    // The external allocator will forward it onto a re-supply cluster.
	    inv = inventoryPlugIn_.findOrMakeInventory(supplyType_, proto);
	    if (inv != null) {
		tasksExpanded++;
//  		printDebug("handleExpandableTasks(), <"+supplyType_+">, Expanding "+TaskUtils.taskDesc(supplyTask));
		expandSupplyTask(supplyTask);
	    } 
 	    else {
		id = proto.getTypeIdentificationPG().getTypeIdentification();
 		printDebug("handleExpandableTasks(), <"+supplyType_+">, could not allocate "+id);
 	    }
	}
//  	printDebug("handleExpandableTasks() <"+supplyType_+"> expanded "+
//  		   tasksExpanded+" tasks.");
    }

    // updates allocation results on the expansion of supply
    // to supply (by inventory) and transport
    private void updateSupplyResults(Enumeration sub_elements) {
	PlanElement pe;
	Task task;
	long time = delegate_.currentTimeMillis();
//  	printDebug("updateSupplyResults() <"+supplyType_+"> Updating allocation Results");
	while (sub_elements.hasMoreElements()) {
	    pe = (PlanElement)sub_elements.nextElement();
	    // SupplyDebug.DEBUG("SupplyExpander", clusterId_, "for task:"+pe.getTask());
	    task = pe.getTask();
	    // if we are past the commit date on the task, no changes are allowed.
	    Date date = task.getCommitmentDate();
	    if (!task.beforeCommitment(new Date(getAlpTime()))) {
		continue;
	    }
	    Workflow wf = task.getWorkflow();
	    if (wf != null) {
		Task parent= wf.getParentTask();
		pe = parent.getPlanElement();
		if (pe == null) {
		    // expansion removed - not an error, 
		    // the infrastructure should be removing the sub_elements soon
		    continue;
		}
		// check if done before
		// false -> use unlike tasks allocation result aggregator (doesn't add quantities)
		AllocationResult new_result = buildExpansionResult((Expansion)pe, false);
		if (new_result != null)  {
		    AllocationResult est_result = pe.getEstimatedResult();
		    if (est_result == null ) {
			pe.setEstimatedResult(new_result);
			supplyExpansionElements_.getSubscriber().publishChange(pe);
		    } else if (!est_result.isEqual(new_result)) {
    			if (new_result.isSuccess()) {
			    pe.setEstimatedResult(new_result);
			    supplyExpansionElements_.getSubscriber().publishChange(pe);
			} else {
			    printDebug("updateSupplyResults() <"+
				       supplyType_+"> failed expansion pe "+
				       pe+" task:"+TaskUtils.taskDesc(parent));
			    // Replace expansion with an expansion with only the failed sub-tasks
			    Enumeration sub_tasks = ((Expansion)pe).getWorkflow().getTasks();
			    Task subtask;
			    PlanElement subpe;
			    Vector newtasks = new Vector();
			    while (sub_tasks.hasMoreElements()) {
				subtask = (Task)sub_tasks.nextElement();
				subpe = subtask.getPlanElement();
				if (subpe != null) {
				    if (subpe.getReportedResult() != null) {
					if (!subpe.getReportedResult().isSuccess()) {
					    printDebug(" <"+supplyType_+
						       "> failed expansion due to :"+
						       TaskUtils.taskDesc(subtask));
					    newtasks.add(subtask);
					}
				    } else if ((subpe.getEstimatedResult() != null) 
					       &&  (!subpe.getEstimatedResult().isSuccess())) {
					printDebug("updateSupplyResults() <"+
						   supplyType_+"> failed expansion due to :"+
						   TaskUtils.taskDesc(subtask));
					newtasks.add(subtask);
				    }
				}
			    }
			    publishExpansion(pe.getTask(), newtasks);
			}
		    }
		}
	    } else if (pe instanceof Disposition){
		// what to do here?? RJB
		// this must have come from GSM which does failed allocs - no need to propagate.
	    } else {
		printError("Task without a Workflow  \ntriggered by: "+TaskUtils.taskDesc(task));
	    }
	}
    }


    /** Expands a Supply task into a withdraw task **/
    protected void expandSupplyTask(Task parentTask) {
	if (parentTask.getVerb().equals(Constants.Verb.SUPPLY)) {
            expandRealSupplyTask(parentTask);
        } else {
            expandProjectionTask(parentTask);
        }
    }

    private void expandProjectionTask(Task parent_task) {
	double quantity = 0;
	 if (parent_task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
	     Rate r = TaskUtils.getRate(parent_task);
	     if (r instanceof FlowRate) {
		 quantity = ((FlowRate)r).getGallonsPerDay();
	     } else if (r instanceof CountRate) {
		 quantity = ((CountRate)r).getEachesPerDay();
	     }
	 }
	 if (quantity <= 0) {
	     printError("expandProjectionTask(), Quantity cannot be obtained from task: "+
			TaskUtils.taskDesc(parent_task));
	     return;
	 }
	long start_time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getStartTime(parent_task));
	long orig_end_time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getEndTime(parent_task));
	// End Time is up to but not including
  	long end_time = TimeUtils.addNDays(orig_end_time, -1);
	long task_end;
	Vector expand_tasks = new Vector();
	NewTask withdrawTask = null;
	if (start_time <= end_time) {
	    while (start_time <= end_time) {
		task_end = TimeUtils.addNDays(start_time, 1);
//  		withdrawTask = createProjectSupplyWithdrawTask(parent_task, start_time, task_end, quantity);
  		withdrawTask = createProjectSupplyWithdrawTask(parent_task, start_time, start_time, quantity);
		expand_tasks.addElement(withdrawTask);
		start_time = task_end;
	    }
	}
	else { // Projection tasks spans a single day
	    withdrawTask = createProjectSupplyWithdrawTask(parent_task, start_time, start_time, quantity);
	    expand_tasks.addElement(withdrawTask);
	}
        publishExpansion(parent_task, expand_tasks);
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
        NewWorkflow wf = (NewWorkflow) ((Expansion) parentTask.getPlanElement()).getWorkflow();
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

    /** creates a Withdraw task from a Supply task **/
    protected NewTask createWithdrawTask(Task parent_task) {

	// Create new task
	Asset prototype = parent_task.getDirectObject();
	NewTask subtask = ldmFactory_.newTask();
	// attach withdraw task to parent and fill it in
	subtask.setDirectObject( prototype);
	subtask.setParentTask( parent_task );
	subtask.setPlan( parent_task.getPlan() );
	subtask.setPrepositionalPhrases( parent_task.getPrepositionalPhrases() );
	subtask.setPriority(parent_task.getPriority());
	subtask.setSource( clusterId_ );
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
	    pref = (Preference)preferences.nextElement();
	    aspect_type = pref.getAspectType();
	    // Quanity added to withdraw by task specific method.
	    // Inerval and DemandRate are not added to withdraw task.
	    if ((aspect_type != AspectType.QUANTITY) && 
		(aspect_type != AspectType.INTERVAL) &&
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
        if (addTransport) anticipation += TRANSPORT_TIME;
        if (addLoad) anticipation += LOAD_TIME;
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

     protected NewTask createProjectSupplyWithdrawTask(Task parent_task, long start, long end, double quantity) {
	 NewTask subtask = createWithdrawTask(parent_task);
	 Preference p_qty = createQuantityPreference(AspectType.QUANTITY, quantity);
	 subtask.setPreference(p_qty);
	 // Overwrite the default start preference 
	 Preference pref = createDateAfterPreference(AspectType.START_TIME, start);
	 subtask.setPreference(pref);
	 // Overwrite the default end preference 
	 pref = createDateBeforePreference(AspectType.END_TIME, end);
	 subtask.setPreference(pref);
//    	 printDebug(1, "CreateProjectSupplyWithdrawTask() with start date:"+
//    		    TimeUtils.dateString(TaskUtils.getStartTime(subtask))+", with end dat:"+
//  		    TimeUtils.dateString(TaskUtils.getEndTime(subtask))+", quantity is "+quantity);

	 return subtask;
     }

    private static final String THEATER_TRANSPORT = "TheaterTransportation";

    // task coming in is the parent task
    private NewTask createTransportTask( Task parent_task, Task wdraw_task) {

	// This is the part to transport
	Asset part = parent_task.getDirectObject();
	// This is the number of parts to transport
	int quantity = (int)TaskUtils.getPreference(wdraw_task, AspectType.QUANTITY);

  	NewTask subtask = ldmFactory_.newTask();
	
	AggregateAsset aggAsset = (AggregateAsset)ldmFactory_.createAggregate(part, quantity);

	subtask.setParentTask( parent_task );
	// Fill in preposition phrases.
	Vector pps = new Vector();

	// From
	PrepositionalPhrase pp_to = parent_task.getPrepositionalPhrase(Constants.Preposition.TO);
	NewPrepositionalPhrase prep_phrase = ldmFactory_.newPrepositionalPhrase();
 	prep_phrase.setPreposition( Constants.Preposition.FROM );
//  	prep_phrase.setIndirectObject(thisGeoloc_);
	prep_phrase.setIndirectObject((GeolocLocation)pp_to.getIndirectObject());
	pps.addElement(prep_phrase);

	// To
	if (pp_to != null) { 
	    prep_phrase = ldmFactory_.newPrepositionalPhrase();
	    prep_phrase.setPreposition( Constants.Preposition.TO );
	    prep_phrase.setIndirectObject((GeolocLocation)pp_to.getIndirectObject());
	    pps.addElement(prep_phrase);
	}
	else {
	    // ???????????????????
	    // What to do in this case?  Transport to nowhere?  just return at this point?
	    printError("Missing TO Preposition on input task "+parent_task);
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
	prep_phrase.setPreposition( Constants.Preposition.FOR );
	String orgName = myOrganization_.getItemIdentificationPG().getItemIdentification();
	prep_phrase.setIndirectObject(orgName);
// 	prep_phrase.setIndirectObject(myOrganization_);
	pps.addElement(prep_phrase);
	
	// OfType
	prep_phrase = ldmFactory_.newPrepositionalPhrase();
	prep_phrase.setPreposition( Constants.Preposition.OFTYPE );
	Asset transport_asset = 
	    ldmFactory_.createPrototype("AbstractAsset", THEATER_TRANSPORT);
	NewTypeIdentificationPG tipg = 
	    (NewTypeIdentificationPG)transport_asset.getTypeIdentificationPG();
	tipg.setTypeIdentification(THEATER_TRANSPORT);
	prep_phrase.setIndirectObject(transport_asset);
	pps.addElement(prep_phrase);

	// Fill in verb.
	subtask.setDirectObject( aggAsset );
	subtask.setPrepositionalPhrases( pps.elements() );
	subtask.setVerb(TRANSPORTVERB);
	subtask.setPlan( parent_task.getPlan() );

	// START TIME & END TIME
        long parent_end =  TaskUtils.getEndTime(parent_task);
//  	long start = parent_end - MSEC_PER_DAY +  (MSEC_PER_MIN*5);
//  	long end  = parent_end - (MSEC_PER_MIN*5);

  	long start = parent_end - TRANSPORT_TIME;
 	long end  = parent_end;

	Preference startPref = createTransportStartPref(start);
    	subtask.addPreference(startPref);

	Preference endPref = createTransportEndPref(end);
	subtask.addPreference(endPref);
	
	// 	PenaltyFunction trans_pf = 
	// 	    ldmFactory_.newDesiredSchedule(allocated_date, sourced_date, late_date);

	// Quantity Preference 
	Preference quantity_pf = createQuantityPreference(AspectType.QUANTITY, quantity);
	subtask.addPreference(quantity_pf);

	subtask.setSource( clusterId_ );
	//  	printDebug("Created transport task:" + taskDesc(subtask));
	return subtask;
    }

    // LOAD Task
    private NewTask createLoadTask (Task parent_task, Task wdraw_task) {

	NewTask subtask = ldmFactory_.newTask();
	Asset asset = parent_task.getDirectObject();

	// Quantity Preference 
	int quantity = (int)TaskUtils.getPreference(wdraw_task, AspectType.QUANTITY);
	Preference quantity_pf = createQuantityPreference(AspectType.QUANTITY, quantity);
	subtask.addPreference(quantity_pf);

//  	// START TIME & END TIME
	addStartTimePref(subtask, TaskUtils.getEndTime(wdraw_task));
	addEndTimePref(subtask, TaskUtils.getEndTime(wdraw_task) + LOAD_TIME);

	// For
	Vector pps = new Vector();
	NewPrepositionalPhrase prep_phrase = ldmFactory_.newPrepositionalPhrase();
	prep_phrase.setPreposition( Constants.Preposition.FOR );
	String orgName = myOrganization_.getItemIdentificationPG().getItemIdentification();
	prep_phrase.setIndirectObject(orgName);
// 	prep_phrase.setIndirectObject(myOrganization_);
	pps.addElement(prep_phrase);

	subtask.setParentTask( parent_task );
	subtask.setDirectObject(asset);

	// Fill in verb.
	subtask.setPrepositionalPhrases( pps.elements() );
	subtask.setVerb(LOADVERB);
	subtask.setPlan(parent_task.getPlan());

	subtask.setSource( clusterId_ );
	//  	printDebug("Created transport task:" + taskDesc(subtask));
	return subtask;
    }

    /** Creates a start and end preference and attaches them to a task **/
    protected void addEndTimePref(NewTask task, long end) {
	Preference p_end = createDateBeforePreference(AspectType.END_TIME, end);
	task.addPreference(p_end);
    }

    protected void addStartTimePref(NewTask task, long start) {
 	Preference p_start = createDateAfterPreference(AspectType.START_TIME, start);
 	task.addPreference(p_start);
    }

}
