/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.core.plugin.util.AllocationResultHelper;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import java.text.NumberFormat;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.AlpineAspectType;
import org.cougaar.domain.glm.ldm.plan.PlanScheduleElementType;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.debug.GLMDebug;

/**
 * BasicProcessor supplies generic methods for connecting to the 'owner'
 * plugin and to record objects as they are published by this processor 
 * to the logplan. 
 * @see DecorationPlugIn 
 * @see PlugInDecorator
 */
public abstract class BasicProcessor {

    protected RootFactory ldmFactory_;
    /** 'hook' to plugin's methods */
    protected PlugInDelegate delegate_;
    /** organization cluster identifier */
    protected ClusterIdentifier clusterId_;
    protected Organization myOrganization_ = null;
    // The name of my organization -- used for UsingGLMSource
    protected String myOrgName_ = null;
    protected String className_;
    protected DecorationPlugIn plugin_;
    public AllocationResultAggregator UnlikeTaskARA_ = new UnlikeTaskARA();

    public static NumberFormat demandFormat = NumberFormat.getInstance();
    /** number of msec per day */
    // 86400000 msec/day = 1000msec/sec * 60sec/min *60min/hr * 24 hr/day
    protected static final long MSEC_PER_DAY =  TaskUtils.MSEC_PER_DAY;
    protected static final long MSEC_PER_MIN =  60 * 1000;
    protected static final long MSEC_PER_HOUR = MSEC_PER_MIN *60;
    protected Calendar calendar_ = Calendar.getInstance();


    // like execute - called whenever a subscription has changed
    public abstract void update();

    public BasicProcessor(DecorationPlugIn pi, Organization org) {
	// set constants
	className_ = this.getClass().getName();
	int indx = className_.lastIndexOf(".");
	if (indx > -1) {
	    className_ = className_.substring(indx+1);
	}
	plugin_ = pi;
	myOrganization_ = org;
	myOrgName_ = myOrganization_.getItemIdentificationPG().getItemIdentification();
	delegate_ = plugin_.getMyDelegate();
	clusterId_ = delegate_.getClusterIdentifier();    
 	ldmFactory_ = delegate_.getLDM().getFactory();

	// initialize quantity formater
	demandFormat.setMaximumIntegerDigits(10);
	demandFormat.setMinimumIntegerDigits(2);
	demandFormat.setMinimumFractionDigits(2);
	demandFormat.setMaximumFractionDigits(2);
	demandFormat.setGroupingUsed(false);
    }

    public boolean isSubscriptionChanged(IncrementalSubscription sub) {
	return plugin_.isSubscriptionChanged(sub);
    }

    protected IncrementalSubscription subscribe(UnaryPredicate predicate)
    {
	IncrementalSubscription subscript = (IncrementalSubscription) delegate_.subscribe( predicate );
	plugin_.monitorSubscription(this, subscript);
	return subscript;
    }

    /** Create String defining task identity.
     * @param t task
     * @return String defining task identity. */
    protected String taskKey(Task t){
	return PublicationKey.getTaskKey(t);
    }

    /** Create String defining task identity. Defaults to comparing preferences.
     * @param prev_task previously published task.
     * @param new_task already defined to have the same taskKey as task a.
     * @return null if the two tasks are the same, 
     *         or returns task a modified for a publishChange.
     * @see #taskKey(Task t) */
    protected Task changeTask(Task prev_task, Task new_task) {
	// Checks for changed preferences.
	if(prev_task==new_task) {
	    // happens in GLMSourcing when updating an expansion where some part of it has failed.
//   	    printError("changeTask SAME TASK! \nelement:"+TaskUtils.taskDesc(prev_task)+"\n parent: "+prev_task.getParentTask());
	    // not sure when this happens
	    return new_task;
	}
 	if (!TaskUtils.comparePreferences(new_task, prev_task)) {
	    Enumeration ntPrefs = new_task.getPreferences();
	    ((NewTask)prev_task).setPreferences(ntPrefs);
	    return prev_task;
	}
	return null;
    }

    private String getQuantitySF(Task task) {
	Preference task_pref = task.getPreference(AspectType.QUANTITY);
	if (task_pref != null) {
	    return task_pref.getScoringFunction()+" ";
	} else {
	    return " !!no QUANTITY preference!! ";
	}
    }


    /** wrapper for GLMDebug */
    public void printDebug(String msg) {
	GLMDebug.DEBUG(className_, clusterId_, msg);
    }

    /** wrapper for GLMDebug */
    // p - priorty,  msg - debug message
    public void printDebug(int p, String msg) {
	GLMDebug.DEBUG(className_, clusterId_, msg, p);
    }

    /** wrapper for GLMDebug */
    public void printError(String msg) {
	GLMDebug.ERROR(className_, clusterId_, msg);
    }
 
    /** wrapper for GLMDebug */
    public void printLog(String msg) {
	GLMDebug.LOG(className_, clusterId_, msg);
    }

    public boolean isPrintConcise() {
        return GLMDebug.printMessages(GLMDebug.CONCISE_LEVEL);
    }

    public void printConcise(String msg) {
        GLMDebug.DEBUG(className_, clusterId_, msg, GLMDebug.CONCISE_LEVEL);
    }
  
    public long getAlpTime() {
	return delegate_.currentTimeMillis();
    }

    /** build Allocation with an estimated alloc result */
    private Allocation buildAllocation( Task t, Asset a, Role r)
    {
	return ldmFactory_.createAllocation(t.getPlan(), t, a, null, r);
    }
  
    /**
     * Create workflow from subtasks, create new Expansion with Workflow.
     */
    private Expansion buildExpansion( Task parent, Vector subtasks)
    {    
	Workflow wf = buildWorkflow(parent, subtasks);
	Expansion expansion = ldmFactory_.createExpansion(parent.getPlan(), parent, wf, null);

	return expansion;
    }


    /** 
     * Initialize workflow parent task and sets propagating 
     * @param parent parent task of workflow
     * @return NewWorkflow with parent task an propagating set 
     **/
    public NewWorkflow newWorkflow(Task parent)
    {
	NewWorkflow wf = ldmFactory_.newWorkflow();
	wf.setParentTask(parent);
	wf.setIsPropagatingToSubtasks(true);
	return wf;
    }

    /**
     *  Build a workflow from a vector of tasks.
     * @param parent parent task of workflow
     * @param subtasks workflow tasks
     * @return Workflow 
     **/
    public Workflow buildWorkflow(Task parent, Vector subtasks) {
	NewWorkflow wf = newWorkflow(parent);
	NewTask t;
	int i, length = subtasks.size();
	for (i=0; i < length; i++) {
	    t = (NewTask) subtasks.elementAt(i);
	    t.setWorkflow(wf);
	    wf.addTask(t);
	}
	return wf;
    }

    /**
     *  Build a workflow with a single task.
     * @param parent parent task of workflow
     * @param task single workflow task
     * @return Workflow 
     **/
    public Workflow buildWorkflow(Task parent, Task task)
    {
	NewWorkflow wf = newWorkflow(parent);
	NewTask t = (NewTask)task;
	t.setWorkflow(wf);
	wf.addTask(t);
	return wf;
    }

    /**
       The goal of this code is to replace any existing expansion of
       the parent into the new set of subtasks, but to do so in a way
       that maximizes the reuse of existing subtasks of the parent.
       This is done by putting all the existing subtasks into a
       hashtable using a key characterizing the ways that the subtask
       cannot be changed (verb, etc.). Then the new tasks are matched
       against the old. Where matches are found the existing task is
       adjusted to match. IF a new task cannot be matched, it is added
       to the expansion. After matching is completed, any old tasks
       that were not matched are removed from the expansion.
     **/
    protected void publishExpansion(Task parent, Vector tasks) {
	if (tasks.isEmpty() ) {
	    printError("publishExpansion - no tasks"+TaskUtils.taskDesc(parent));
	    return;
	}
	Enumeration elements = tasks.elements();
	Task task = (Task)elements.nextElement();
	PlanElement pe = parent.getPlanElement();
	Expansion expansion;
 	if (pe == null) {
	    // In MB5.2 no longer automatically publishes tasks in a workflow.
	    Enumeration enum = tasks.elements();
	    while (enum.hasMoreElements()) {
		publishAddTask((Task)enum.nextElement());
	    }
	    // create new expansion
	    expansion = buildExpansion(parent, tasks);
	    delegate_.publishAdd(expansion);
	    return;
	}
	if (!(pe instanceof Expansion)) {
	    printError("publishExpansion: problem pe not Expansion?"+pe
		       +"\n  Task:"+PublicationKey.getTotalTaskKey(task)
		       +"\n  Parent Task:"+PublicationKey.getTotalTaskKey(parent));
	    return;
	}

	expansion = (Expansion)pe;
	// Expansion previously published - need to change.
	Hashtable published_tasks = createPublishedTable(expansion.getWorkflow().getTasks());

	boolean change_expansion = false;
	NewTask new_task;
        Task prev_task;
	String key;
	PublishedTask published_task;
        Context context = parent.getContext();
	// publish
	elements = tasks.elements();
	while (elements.hasMoreElements()) {
	    new_task = (NewTask)elements.nextElement();
            if (new_task.getContext() == null) new_task.setContext(context);
	    key = taskKey(new_task);
	    // isElementPublished	
	    // check if already published
	    published_task = (PublishedTask) published_tasks.get(key);
	    if (published_task != null) {
		if (published_task.getPublished() ) {
		    printError("WARNING: publishExpansion() Change() Ack! Two PublishedElements with the same key! <"+key+">\n TASK1:"+published_task.getTask()+"\nTASK2:"+new_task);
		} else {
		    prev_task = published_task.getTask();
		    // Check unnecessary because alpTime is always before Commitment time
		    if (prev_task.beforeCommitment(new Date(getAlpTime()))) {
			// check if changed...
			Task t = changeTask(prev_task, new_task);
			if (t != null) {
			    //  			    printDebug( "publishExpansion() Change CHANGE key <"+key+">");
			    delegate_.publishChange(t);
			    change_expansion = true;
			// add new task to hash table for later publishing
			    published_task.setTask(t);
			    
			}
 		    } else {
 			printDebug("publishExpansion() After commitment:"+prev_task.getCommitmentDate()+" task:"+key);
		    }
		}
		published_task.setPublished(true);
	    } else {
		// 		printDebug( "publishExpansion() Change() ADD key <"+key+">");
		publishAddTask(new_task);
		change_expansion = true;
		published_tasks.put(key, new PublishedTask(new_task,true));
	    }
	}
	boolean remove_from_wf = rescindUnpublishedTasks(published_tasks);
	if (change_expansion || remove_from_wf) {
	    // Tasks w/i workflow have been added and/or removed need to
	    // publish change the workflow.
	    // If tasks are only changed, then don't need to change workflow.


	    // RJB -- comment this out for now -- may not need to look at it
	    // 	    printDebug("PublishExpansion CHANGE expansion:"+expansion+" w/ TASK:"+expansion.getTask());
	    NewWorkflow nwf = (NewWorkflow)expansion.getWorkflow();
	    //  	    Enumeration oldTasks = ((Workflow)nwf).getTasks();
	    //  	    while (oldTasks.hasMoreElements()) {
	    //  		printDebug("old task: " + TaskUtils.taskDesc((Task)oldTasks.nextElement()));
	    //  	    }
	    Vector newTasks = new Vector();
	    Enumeration key_list = published_tasks.keys();
	    while (key_list.hasMoreElements()) {
		key = (String)key_list.nextElement();
		PublishedTask ipo = (PublishedTask) published_tasks.get(key);
		NewTask newTask = (NewTask) ipo.getTask();
		//  		printDebug("ipo.getTask(): " + TaskUtils.taskDesc(task));
		if (ipo.getPublished()) {
		    newTasks.addElement(newTask);
                    newTask.setWorkflow(nwf);
		}
	    }
	    nwf.setTasks(newTasks.elements());
	    if (expansion.getEstimatedResult() != null) {
		expansion.setEstimatedResult(buildExpansionResult(expansion));
	    }
	    delegate_.publishChange(expansion);
	}
    }
    /**
     * Reconcile an intended schedule of projections with the
     * currently published schedule of projections so as to reuse as
     * many of the existing projection tasks as possible.
     *
     * Generally as elements from the published schedule are used they
     * are removed from the schedule. Tasks remaining in the schedule
     * are rescinded.
     *
     * There are three regions of interest: before now, around now and
     * after now. These are each handled separately. In the region
     * before now, already published tasks are unconditionally
     * retained and new tasks are unconditionally ignored.
     *
     * In the region around now, tasks may start before now and end
     * after. If both a published task and a new task spanning now
     * exist, then there are two cases: If the demand rates are the
     * same, then the published task is changed to look like the new
     * task (by changing its end time preference). The start time of
     * the published task is unchanged. Think of the existing task
     * ending now and the new task starting now and then splicing the
     * two together into one task. If the rates are different, then
     * the existing task must end when the new task starts. The
     * current code accomplishes this by setting the end time
     * preference of the existing task to the start time of the new.
     * This is not exactly correct since we shouldn't change the past.
     * The times of the tasks should be no less than now.
     *
     * In the region after now, we try to match up the tasks. When a
     * match is possible, the existing task is changed if necessary
     * (and republished) otherwise it is rescinded and the new task
     * added.
     **/
    protected Enumeration diffProjections(Schedule published_schedule, Schedule newtask_schedule) {
	// Chedk for an empty schedule
	if (newtask_schedule.isEmpty()) {
	    printError("publishChangeProjection(), New Task Schedule empty: "+newtask_schedule);
	    return null;	    
	}

	Vector add_tasks = new Vector();
	// Remove from the published schedule of tasks  all tasks that occur BEFORE now but not overlapping now
	// These historical tasks should not be changed
	long now = getAlpTime();
	ObjectScheduleElement ose;
	Iterator historical_tasks =
            published_schedule.getEncapsulatedScheduleElements(TimeSpan.MIN_VALUE, now).iterator();
	while (historical_tasks.hasNext()) {
	    ose = (ObjectScheduleElement)historical_tasks.next();
	    ((NewSchedule)published_schedule).removeScheduleElement(ose);
	}

	// Examine the new task and published task that straddle NOW
	Task published_task = null;
	Task new_task = null;
	Collection c = newtask_schedule.getScheduleElementsWithTime(now);
	if (!c.isEmpty()) {
	    ose = (ObjectScheduleElement)c.iterator().next();
	    new_task = (Task)ose.getObject();
	    ((NewSchedule)newtask_schedule).removeScheduleElement(ose);
	}
	c =  published_schedule.getScheduleElementsWithTime(now);
	if (!c.isEmpty()) {
	    ose = (ObjectScheduleElement)c.iterator().next();
	    published_task = (Task)ose.getObject();
	    ((NewSchedule)published_schedule).removeScheduleElement(ose);
	}
	if (published_task != null && new_task != null) {
	    // Depending upon whether the rate is equal set the end time of the published task to the start or
	    // end time of the new task
	    Rate new_rate = TaskUtils.getRate(new_task);
	    if (new_rate.equals(TaskUtils.getRate(published_task))) {
		// check end times not the same
		((NewTask)published_task).setPreference(new_task.getPreference(AspectType.END_TIME));
                if (isPrintConcise()) printProjection("extend old end", published_task);
		publishChangeTask(published_task);
	    } else {
		// check to make sure start_time is not before now
		// long that is the maximum of now and the start_time
                long when = Math.max(now, TaskUtils.getStartTime(new_task));
		setEndTimePreference((NewTask) published_task, when);
                if (isPrintConcise()) printProjection("truncate old end 1", published_task);
		publishChangeTask(published_task);
                setStartTimePreference((NewTask) new_task, when);
                if (isPrintConcise()) printProjection("truncate new start 1", new_task);
		add_tasks.add(new_task);
	    }
	} else if (new_task != null) {
            setStartTimePreference((NewTask) new_task, now);
            if (isPrintConcise()) printProjection("truncate new start 2", new_task);
	    add_tasks.add(new_task);
	} else if (published_task != null) {
            setEndTimePreference((NewTask) published_task, now);
            publishChangeTask(published_task);
            if (isPrintConcise()) printProjection("truncate old end 2", published_task);
        }

	// Compare new tasks to previously scheduled tasks, if a published task is found that
	// spans the new task's start time then adjust the published task (if needed) and publish
	// the change.  If no task is found than add new_task to list of tasks to be published.
	// When start time of schedule is equal to TimeSpan.MIN_VALUE, schedule is empty
	long start;
	while (!newtask_schedule.isEmpty()) {
	    start = newtask_schedule.getStartTime();
	    ose = (ObjectScheduleElement)ScheduleUtils.getElementWithTime(newtask_schedule, start);
	    if (ose != null) {
		new_task = (Task)ose.getObject();
		((NewSchedule)newtask_schedule).removeScheduleElement(ose);
	    }
	    else {
		printError("publishChangeProjection(), Bad Schedule: "+newtask_schedule);
		return null;
	    }
	    // Get overlapping schedule elements from start to end of new task
	    c = published_schedule.getScheduleElementsWithTime(start);
	    if (!c.isEmpty()) {
		// change the task to look like new task
		ose = (ObjectScheduleElement)c.iterator().next();
		published_task = (Task)ose.getObject();
		((NewSchedule)published_schedule).removeScheduleElement(ose);
		
		published_task = changeTask(published_task, new_task);
		if (published_task != null) {
                    if (isPrintConcise()) printProjection("replace with", published_task);
		    publishChangeTask(published_task);
//  		    printDebug("publishChangeProjection(), Publishing changed Projections: "+
//  			       TaskUtils.projectionDesc(new_task));
		}
	    }
	    else {
		// no task exists that covers this timespan, publish it
		add_tasks.add(new_task);
	    }
	}
	// Rescind any tasks that were not accounted for
	Enumeration e = published_schedule.getAllScheduleElements();
	while (e.hasMoreElements()) {
            Task task = (Task) ((ObjectScheduleElement) e.nextElement()).getObject();
            if (isPrintConcise()) printProjection("remove", task);
	    plugin_.publishRemoveFromExpansion(task);
	}
        if (isPrintConcise()) {
            for (Enumeration enum = add_tasks.elements(); enum.hasMoreElements(); ) {
                Task task = (Task) enum.nextElement();
                printProjection("add", task);
            }
        }
	return add_tasks.elements();
    }

    protected void setEndTimePreference(NewTask task, long end) {
        AspectValue av = new TimeAspectValue(AspectType.END_TIME, end);
        ScoringFunction score = ScoringFunction.createStrictlyAtValue(av);
        task.setPreference(ldmFactory_.newPreference(AspectType.END_TIME, score));
    }

    protected void setStartTimePreference(NewTask task, long start) {
        AspectValue av = new TimeAspectValue(AspectType.START_TIME, start);
        ScoringFunction score = ScoringFunction.createStrictlyAtValue(av);
        task.setPreference(ldmFactory_.newPreference(AspectType.START_TIME, score));
    }

    private void printProjection(String msg, Task task) {
        printConcise("diffProjections() "
                     + task.getUID()
                     + " " + msg + " "
                     + TaskUtils.getDailyQuantity(task)
                     + " "
                     + TimeUtils.dateString(TaskUtils.getStartTime(task))
                     + " to "
                     + TimeUtils.dateString(TaskUtils.getEndTime(task)));
    }

   public static Schedule newObjectSchedule(Enumeration tasks) {
       Vector os_elements = new Vector();
       ScheduleImpl s = new ScheduleImpl();
       s.setScheduleElementType(PlanScheduleElementType.OBJECT);
       s.setScheduleType(ScheduleType.OTHER);
       Task task;
       while (tasks.hasMoreElements()) {
	   task = (Task)tasks.nextElement();
	   os_elements.add(new ObjectScheduleElement(TaskUtils.getStartTime(task),
						  TaskUtils.getEndTime(task), task));
       }
       s.setScheduleElements(os_elements.elements());
       return s;
    }

    private Hashtable createPublishedTable(Enumeration tasks) {
	Hashtable task_table = new Hashtable();
	while (tasks.hasMoreElements()) {
	    Task task = (Task)tasks.nextElement();
	    // create new key for each part of the workflow
	    task_table.put(taskKey(task),  new PublishedTask(task,false));
	}
	return task_table;
    }

    /** Rescinds all elements in the given hashtable that
     * have not been republished since the last call to 
     */
    private boolean rescindUnpublishedTasks(Hashtable tasks)
    {
	PublishedTask ipo;
	Task task;
	String key;
	Object obj;
	boolean removed_task = false; 

	Enumeration key_list = tasks.keys();
	while (key_list.hasMoreElements()) {
	    key = (String)key_list.nextElement();
	    ipo = (PublishedTask) tasks.get(key);
	    task = ipo.getTask();
	    // Taking out commitment check temporarily until it is decided
	    // how this field is to be used
	    if (!(ipo.getPublished()) && task.beforeCommitment(new Date(getAlpTime())) ) {
		// if (!(ipo.getPublished())) {
		//   		printDebug("rescindUnpublishedTasks() removing key <"+key+">");
		delegate_.publishRemove(ipo.getTask());
		removed_task = true;
	    }
	}
	return removed_task;
    }
 
    protected void publishAddTask(Task task){
// 	delegate_.publishAdd(task);
	if (!plugin_.publishAddObject(task)) {

	    printError("publishAddTask fail to publish task "+TaskUtils.taskDesc(task));
	}
    }

    protected void publishRemoveTask(Task task){
	delegate_.publishRemove(task);
    }

    protected void publishAsset(Asset asset) {
	delegate_.publishAdd(asset);
    }

    protected void publishRemoveAsset(Asset asset) {
	delegate_.publishRemove(asset);
    }

    protected void publishChangeAsset(Asset asset) {
	delegate_.publishChange(asset);
    }

    protected void publishChangeTask(Task task) {
	delegate_.publishChange(task);
    }

    protected void publishRemoveAllocation(Allocation alloc) {
	delegate_.publishRemove(alloc);
    }

    protected void publishRemoveExpansion(Expansion exp) {
	delegate_.publishRemove(exp);
    }

    protected boolean publishAllocation(Task task, Asset asset, org.cougaar.domain.planning.ldm.plan.Role role) {
	return publishAllocation(task, asset, role, null, false);
    }



    protected boolean publishAllocation(Task task, Asset asset, Role role, AllocationResult ar) {
	return publishAllocation (task, asset, role, ar, true);
    }

    protected boolean publishAllocation(Task task, Asset asset, Role role, AllocationResult ar, boolean needAR){
	// Commitment Date is an important piece of replanning but will not be part of Demo99
	if (!(task.beforeCommitment(new Date(getAlpTime())))) {
	    printDebug(2,"publishAllocation: return ... after commitment"+task.getCommitmentDate()+" task:"+task+" to Asset "+asset);
	    // too late to change
	    return false;
	}
	// 	printDebug(2,"publishAllocation: "+TaskUtils.taskDesc(task)+" with plan element: "+task.getPlanElement());
	    
	if (asset == null) {
	    printError("publishAllocation null asset! for task "+task);
	    return false;
	}
	
 	PlanElement pe = task.getPlanElement();
	Allocation alloc;
	if (pe == null) {
	    // new allocation
	    alloc = buildAllocation(task, asset, role);
	    if (ar != null){
		alloc.setEstimatedResult(ar);
	    } else if (needAR) {
		printDebug(2,"publishing allocation without estimated result: " + alloc.getUID());
	    }
	    if (!plugin_.publishAddObject(alloc)) {
		printError("publishAalloc fail to publish alloc "+alloc);
	    }
	    return true;
	}
	if (!(pe instanceof Allocation)) {
	    printDebug(1,"publishAllocation: plan element not allocation:"+pe);
	    printDebug(1,"publishAllocation: task:"+task);
	    return false;
	}

	boolean change_alloc = false;
	boolean add_alloc = false;

	alloc = (Allocation) pe;

	// check task
	Task published_task = alloc.getTask();
	if (published_task != task) {
	    if (taskKey(published_task).equals(taskKey(task))) {
		// Commitment Date is an important part of replanning but is not part of Demo99
		if (published_task.beforeCommitment(new Date(getAlpTime()))) {
		    // check if changed...
		    if (changeTask(published_task, task) != null) {
			//  		    printDebug(2,"publishAllocation CHANGED task"+ task.getUID());
			change_alloc = true;
		    }
		    //  		printDebug("after changeTask: " + TaskUtils.taskDesc(published_task));
		    task = published_task;
		}
	    } else {
		printError("new task? publishAllocation old:"+published_task+" \n new:"+task);
	    }
	}

	// check asset
	if (alloc.getAsset() != asset) {
	    delegate_.publishRemove(alloc);
	    printDebug(1,"publishAlloc removing and adding in same transaction for ASSET:"+alloc.getAsset()
		       +" vs. "+asset+"  task:"+TaskUtils.taskDesc(task));
	    // PAS this will probably cause an error.
	    alloc = buildAllocation(task ,asset, role);
	    add_alloc = true;
	}

	// check role
	if (!add_alloc && !alloc.getRole().equals(role)) {
	    printDebug("publishAllocation CHANGE role " +task);
	    delegate_.publishRemove(alloc);
	    printDebug("publishAlloc removing and adding in same transaction for ROLE: "+alloc.getRole()
		       +" not equal to "+role+"  task:"+TaskUtils.taskDesc(task));
	    // PAS this will probably cause an error.
	    alloc = buildAllocation(task ,asset, role);
	    add_alloc = true;
	}

	// check allocation result
	AllocationResult alloc_ar = alloc.getEstimatedResult();
	if ((ar != null) && 
	    ((alloc_ar == null) || !ar.isEqual(alloc_ar))) {
	    alloc.setEstimatedResult(ar);
	    // 	    printDebug("publishAllocation CHANGE result" +task);
	    change_alloc = true;
	    // need to publish change
	} else {
	    if ((ar == null) && (alloc_ar != null)) {
		alloc.setEstimatedResult(createEstimatedAllocationResult(task));
		// 		printDebug("publishAllocation CHANGE result (new)" +task);
		change_alloc = true;
	    }
	}

	if (add_alloc) {
	    if (!plugin_.publishAddObject(alloc)) {
		printError("publishAalloc fail to publish alloc "+alloc);
	    }
	    return true;
	} else if (change_alloc) {
	    // 	    printDebug("publishAlloc publishChange "+pe);
	    delegate_.publishChange(pe);
	    return true;
	}
	return false;
    }

    public AllocationResult buildExpansionResult(Expansion expansion) {
	return buildExpansionResult(expansion, true);
    }

    public AllocationResult buildExpansionResult(Expansion expansion, boolean like_tasks) {
	Hashtable table = new Hashtable();

	Task task;
	PlanElement sub_pe;
	Workflow wf = expansion.getWorkflow();
	Enumeration tasks = wf.getTasks();
	while (tasks.hasMoreElements()) {
	    task = (Task) tasks.nextElement();
	    sub_pe = task.getPlanElement();
	    if (sub_pe != null) {
		AllocationResult ar = sub_pe.getReportedResult();
		if (ar == null) {
		    // task has been expanded or allocated... 
		    // but no results returned.
		    // Or allocated to final asset, in which case
		    // only the estimated gets set...
		    ar = sub_pe.getEstimatedResult();
		    if (ar == null) {
			// 			printDebug("buildExpansionResult() No reported or estimated Result pe:"+sub_pe+" .\nparent:"+task);
			return null;
		    }
		}
		table.put(task, ar);
	    }
	    // else hasn't been allocated or expanded yet.
	}
	if (like_tasks) {
	    return AllocationResultAggregator.DEFAULT.calculate(wf, new TaskScoreTable(table), 
								expansion.getEstimatedResult());
	} else {
	    return UnlikeTaskARA_.calculate(wf, new TaskScoreTable(table), 
					    expansion.getEstimatedResult());
	}
    }

  /** Does the right computation for workflows which are made up of
   * equally important tasks with no inter-task constraints.
   * START_TIME is minimized.
   * END_TIME is maximized.
   * DURATION is overall END_TIME - overall START_TIME.
   * COST is summed.
   * DANGER is maximized.
   * RISK is maximized.
   * QUANTITY is maximized.
   * INTERVAL is summed.
   * TOTAL_QUANTITY is maximized.
   * TOTAL_SHIPMENTS is summed.
   * CUSTOMER_SATISFACTION is averaged.
   * Any extended aspect types are ignored.
   * 
   * For AuxiliaryQuery information, if all the query values are the same
   * across subtasks or one subtask has query info it will be place in the 
   * aggregate result.  However, if there are conflicting query values, no
   * information will be put in the aggregated result.
   * 
   * returns null when there are no subtasks or any task has no result.
   **/
  public class UnlikeTaskARA implements AllocationResultAggregator {
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

      int count = 0;
      boolean suc = true;
      double rating = 0.0;
      
      Enumeration tasks = wf.getTasks();
      if (tasks == null || (! tasks.hasMoreElements())) return null;
      
      String auxqsummary[] = new String[AuxiliaryQueryType.AQTYPE_COUNT];
      // initialize all values to UNDEFINED for comparison purposes below.
      final String UNDEFINED = "UNDEFINED";
      for (int aqs = 0; aqs < auxqsummary.length; aqs++) {
        auxqsummary[aqs] = UNDEFINED;
      }

      while (tasks.hasMoreElements()) {
        Task t = (Task) tasks.nextElement();
        count++;
        AllocationResult ar = tst.getAllocationResult(t);
        if (ar == null) {
          return null; // bail if undefined
        }

        suc = suc && ar.isSuccess();
        rating += ar.getConfidenceRating();
        
        int[] definedaspects = ar.getAspectTypes();
        for (int b = 0; b < definedaspects.length; b++) {
          // accumulate the values for the defined aspects
          switch (definedaspects[b]) {
            case START_TIME: acc[START_TIME] = Math.min(acc[START_TIME], ar.getValue(START_TIME));
                    break;
            case END_TIME: acc[END_TIME] = Math.max(acc[END_TIME], ar.getValue(END_TIME));
                    break;
            // compute duration later
            case COST: acc[COST] += ar.getValue(COST);
                    break;
            case DANGER: acc[DANGER] = Math.max(acc[DANGER], ar.getValue(DANGER));
                    break;
            case RISK: acc[RISK] = Math.max(acc[RISK], ar.getValue(RISK));
                    break;
            case QUANTITY: acc[QUANTITY] = Math.max(acc[QUANTITY], ar.getValue(QUANTITY));
                    break;
            // for now simply add the repetitve task values
            case INTERVAL: acc[INTERVAL] += ar.getValue(INTERVAL);
                    break;
            case TOTAL_QUANTITY: acc[TOTAL_QUANTITY] = Math.max(acc[TOTAL_QUANTITY], ar.getValue(TOTAL_QUANTITY));
                    break;
            case TOTAL_SHIPMENTS: acc[TOTAL_SHIPMENTS] += ar.getValue(TOTAL_SHIPMENTS);
                    break;
            //end of repetitive task specific aspects
            case CUSTOMER_SATISFACTION: acc[CUSTOMER_SATISFACTION] += ar.getValue(CUSTOMER_SATISFACTION);
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
      
      acc[DURATION] = acc[END_TIME] - acc[START_TIME];
      acc[CUSTOMER_SATISFACTION] /= count;

      rating /= count;

      boolean delta = false;
      //for (int i = 0; i <= _LAST_ASPECT; i++) {
        //if (acc[i] != currentar.getValue(i)) {
          //delta = true;
          //break;
        //}
      //}
      
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
          for (int i = 0; i < caraspects.length; i++) {
            int da = caraspects[i];
            if (acc[da] != currentar.getValue(da)) {
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
        AllocationResult artoreturn = new AllocationResult(rating, suc, _STANDARD_ASPECTS, acc);
        for (int aqt = 0; aqt < auxqsummary.length; aqt++) {
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



    // If estimated result != reported result,
    // copy reported to estimated.
    public void updateAllocationResult(PlanElement pe) {
        if (TaskUtils.updatePlanElement(pe)) delegate_.publishChange(pe);
    }

    /** When the published sub-tasks of an expansion get the allocation result updated,
     *  try to update the allocation result on the whole expansion.
     *  Do not update the allocation result until all subtasks have been updated.
     **/
    protected void updateExpansionResult(Enumeration planelements) {
        while (planelements.hasMoreElements()) {
            updateAllocationResult((PlanElement) planelements.nextElement());
        }
    }

    protected void publishFailedDisposition(Task task, AllocationResult ar) {
 	PlanElement pe = task.getPlanElement();
	Disposition fa;
	if (pe == null) {
	    fa = ldmFactory_.createFailedDisposition(task.getPlan(), task, ar);
	    if (fa == null) {
		printError("publishFailedDisposition... no fa?: "+task);
	    }
	    delegate_.publishAdd(fa);
	} else {	   
	    if (!(pe instanceof Disposition)) {
		printError("publishFailedDisposition: plan element not Failed allocation:"+pe);
		printError("publishFailedDisposition: task:"+task);
		return;
	    }
	    fa = (Disposition) pe;
	    // check difference between last fa and this ar.
	    if (!ar.isEqual(fa.getEstimatedResult())) {
		fa.setEstimatedResult(ar);
		delegate_.publishChange(fa);
	    }
	}
    }

    public String arDesc(AllocationResult ar) {
	try{
	return "(AR: "+ (long)ar.getValue(AspectType.QUANTITY) +"; "+
	    TimeUtils.dateString((long)ar.getValue(AspectType.START_TIME))+","+
	    TimeUtils.dateString((long)ar.getValue(AspectType.END_TIME))+")";
	} catch (Exception e){
	return "(AR: "+ ar +
	    TimeUtils.dateString((long)ar.getValue(AspectType.START_TIME))+","+
	    TimeUtils.dateString((long)ar.getValue(AspectType.END_TIME))+")";
	}
    }

    // UTILITIES
    public void printQuantitySchedule (Schedule sched, int priority) {
	Enumeration elements = sched.getAllScheduleElements();
	QuantityScheduleElement qse;

	while (elements.hasMoreElements()) {
	    qse = (QuantityScheduleElement)elements.nextElement();
	    printDebug(priority,qseString(qse));
	}
    }
    
    public String qseString(QuantityScheduleElement qse) {
	return "    qty: "+demandFormat.format(qse.getQuantity())+
	    " "+TimeUtils.dateString(qse.getStartTime())+" to "+
	    TimeUtils.dateString(qse.getEndTime());
    }

    /** create an AllocationResult that assumes the 'best' possible result
     *  (taken from GLSAllocatorPlugIn) */
    public AllocationResult createEstimatedAllocationResult(Task t) {
        AllocationResultHelper helper = new AllocationResultHelper(t, null);
        return helper.getAllocationResult(0.0, true);
    }


    public PrepositionalPhrase newPrepositionalPhrase(String preposition,
						      Object io) {
	NewPrepositionalPhrase pp = ldmFactory_.newPrepositionalPhrase();
	pp.setPreposition(preposition); 
       	pp.setIndirectObject(io);
	return pp;
    }

    public PrepositionalPhrase newPrepositionalPhrase(String preposition) {
	NewPrepositionalPhrase pp = ldmFactory_.newPrepositionalPhrase();
	pp.setPreposition(preposition); 
	return pp;
    }

    public NewTask buildNewTask(Task input_task, String output_verb, Asset direct_object)
    {
	NewTask newtask = ldmFactory_.newTask();  
	if (input_task == null) {
	    newtask.setPlan(ldmFactory_.getRealityPlan());
	} else {
	    newtask.setParentTask(input_task);
	    newtask.setPlan(input_task.getPlan());
	    newtask.setPrepositionalPhrases(input_task.getPrepositionalPhrases());
	}
	newtask.setDirectObject(direct_object);
	newtask.setVerb(Verb.getVerb(output_verb));
	return newtask;
    }

    public Task buildTask(Task input_task, String output_verb, Asset direct_object, PrepositionalPhrase pp)
    {
	NewTask newtask = buildNewTask(input_task, output_verb, direct_object);
	newtask = TaskUtils.addPrepositionalPhrase(newtask, pp);
	return newtask;
    }

    public Task buildTask(Task input_task, String output_verb, Asset direct_object, Vector preposition_phrases)
    {
	NewTask newtask = buildNewTask(input_task, output_verb, direct_object);
	newtask.setPrepositionalPhrases(preposition_phrases.elements());
	return newtask;
    }

    public Task buildTask(Task input_task, String output_verb, Asset direct_object, Vector preposition_phrases, Preference pref)
    {
	NewTask newtask = buildNewTask(input_task, output_verb, direct_object);
	newtask.setPrepositionalPhrases(preposition_phrases.elements());
	newtask.addPreference(pref);
	return newtask;
    }

    public Task buildTask(Task input_task, String output_verb, Asset direct_object, Vector preposition_phrases, Enumeration prefs)
    {
	NewTask newtask = buildNewTask(input_task, output_verb, direct_object);
	newtask.setPrepositionalPhrases(preposition_phrases.elements());
	newtask.setPreferences(prefs);
	return newtask;
    }

    public Task buildTask(Task input_task, String output_verb, Asset direct_object, Enumeration prefs)
    {
	NewTask newtask = buildNewTask(input_task, output_verb, direct_object);
	newtask.setPreferences(prefs);
	return newtask;
    }


    /** Create a preference with the Strict scoring function at 'value' for the
	given aspect type */
    public Preference createPreference(int aspect, double value) {
	AspectValue av = new AspectValue(aspect,value);
// 	ScoringFunction score = ScoringFunction.createPreferredAtValue(av, 2);
 	ScoringFunction score = ScoringFunction.createStrictlyAtValue(av);
	return ldmFactory_.newPreference(aspect, score);
    }

    /** Create a preference with the scoring NearOrAbove function at 'value' for the
	given aspect type */
    public Preference createQuantityPreference(int aspect, double value) {
	AspectValue av = new AspectValue(aspect,value);
 	ScoringFunction score = ScoringFunction.createNearOrAbove(av, 0);
	return ldmFactory_.newPreference(aspect, score);
    }

    public Preference createDateBeforePreference(int aspect, long value) {
	AspectValue av = new AspectValue(aspect,value);
 	ScoringFunction score = ScoringFunction.createNearOrBelow(av, 0);
	return ldmFactory_.newPreference(aspect, score);
    }

    public Preference createDateAfterPreference(int aspect, long value) {
	AspectValue av = new AspectValue(aspect,value);
 	ScoringFunction score = ScoringFunction.createNearOrAbove(av, 0);
	return ldmFactory_.newPreference(aspect, score);
    }
   


}

/* An object containing the
 * published plan element and a flag set if the plan element is
 * published after the current replan. */
class PublishedTask {

    private Task task_;    
    private boolean publishFlag_;

    /** Constructer
     * @param Task plan element to be published
     * @param boolean is plan element published
     */
    public PublishedTask(Task t, boolean b)
    {
	task_ = t;
	publishFlag_ = b;
    }

    /** Constructer (defaults to pe not published)
     * @param Task plan element to be published
     */
    protected PublishedTask(Task t)
    {
	task_ = t;
	publishFlag_ = true;
    }

    /** @return Task  plan element */
    protected Task getTask()
    {
	return task_;
    }

    protected void setTask(Task task) {
	task_ = task;
    }

    /** @param boolean sets published flag to b.
     */
    protected void setPublished (boolean b)
    {
	publishFlag_ = b;
    }

    /** @return boolean - is plan element published. */
    protected boolean getPublished ()
    {
	return publishFlag_;
    }
}


