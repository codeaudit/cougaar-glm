/*
 * <copyright>
 *  Copyright 1997-2001 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plugin.deletion;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.Iterator;
import org.cougaar.core.cluster.Alarm;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.PlugInHelper;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.planning.ldm.DeletionPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;

/**
 * This plugin constructs a logplan consisting of tasks and their
 * dispositions that can be deleted. It does the following:
 *   Inserts root tasks from time to time having timed activity.
 *   Expands root tasks into sequences of subtasks having time
 *       constraints.
 *   Aggregates subtasks of the same time within a time window.
 *   Allocates aggregated tasks to local or remote assets using a simple
 *       scheduling algorithm
 *   Watches for deletion of expired tasks and modifies its scheduling
 *       data to account for the deleted tasks
 **/
public class TestDeletionPlugIn extends SimplePlugIn {
    private abstract class Handler {
        protected IncrementalSubscription tasks;
        public Handler(final int type) {
            tasks = (IncrementalSubscription) subscribe(new UnaryPredicate() {
                public boolean execute(Object o) {
                    return testTask(o, type);
                }
            });
        }
        public abstract void execute();
    }

    private class RootHandler extends Handler {
        public RootHandler() {
            super(TYPE_ROOT);
        }
        public void execute() {
            handleExpTasksAdded(tasks.getAddedList());
            handleExpTasksChanged(tasks.getChangedList());
            handleExpTasksRemoved(tasks.getRemovedList());
        }
    }

    private class SubHandler extends Handler {
        public SubHandler(int subtype) {
            super(TYPE_SUB + subtype);
        }
        public void execute() {
            handleSubTasksAdded(tasks.getAddedList());
            handleSubTasksChanged(tasks.getChangedList());
            handleSubTasksRemoved(tasks.getRemovedList());
        }
    }

    private class AggHandler extends Handler {
        public AggHandler(int subtype) {
            super(TYPE_AGG + subtype);
        }
        public void execute() {
            handleAggTasksAdded(tasks.getAddedList());
            handleAggTasksChanged(tasks.getChangedList());
            handleAggTasksRemoved(tasks.getRemovedList());
        }
    }

    /** Subscriptions to tasks **/
    private Handler[] handlers;

    private IncrementalSubscription[] mpTasks;

    /** Something to allocate subtasks to. **/
    private Asset theRootAsset; // DO of root tasks
    private Asset theExpAsset;  // DO of expansion subtasks
    private Asset theAggAsset;  // DO of aggregation mptasks
    private Asset theAllocAsset;  // subject of allocations

    /** The Role we use for allocations **/
    private Role testProviderRole;

    /** The verb that we use **/
    private static Verb testDeletionVerb = Verb.getVerb("TestDeletion");

    /** The preposition used to specify task level **/
    private static final String LEVEL = "AtLevel";

    /** The preposition used to specify subtask type **/
    private static final String SUBTYPE = "Type";

    private static final int N_SUBTYPES = 3;
    private static final int TYPE_ROOT  = 0;
    private static final int TYPE_SUB   = 1;
    private static final int TYPE_AGG   = TYPE_SUB + N_SUBTYPES;
    private static final int N_TYPES    = TYPE_AGG + N_SUBTYPES;

    /** The preposition used to specify task duration **/
    private static final String DURATION = "OfDuration";

    private long minRootTaskDelay     =  3 * 86400000L;
    private long maxRootTaskDelay     = 14 * 86400000L;
    private long minRootTaskDuration  =  3 * 86400000L;
    private long maxRootTaskDuration  = 14 * 86400000L;
    private long minInterTaskInterval =  1 * 3600000L;
    private long maxInterTaskInterval = 24 * 3600000L;
    private long AGGREGATION_PERIOD   =  5 * 86400000L;
    private Alarm newRootTimer;
    private Random random = new Random();

    private boolean testTask(Object o, int subtype) {
        if (o instanceof Task) {
            Task task = (Task) o;
            if (task.getVerb().equals(testDeletionVerb)) {
                return getSubtype(task) == subtype;
            }
        }
        return false;
    }

    public void setupSubscriptions() {
        Role.create("TestDeletionProvider", "TestDeletionCostumer");
        testProviderRole = Role.getRole("TestDeletionProvider");
        theRootAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestRoot"));
        theExpAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestExp"));
        theAggAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestAgg"));
        theAllocAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestAlloc"));
        publishAdd(theRootAsset);
        publishAdd(theExpAsset);
        publishAdd(theAggAsset);
        publishAdd(theAllocAsset);
        mpTasks = new IncrementalSubscription[N_SUBTYPES];
        handlers = new Handler[1 + N_SUBTYPES + N_SUBTYPES];
        int i = 0;
        handlers[i++] = new RootHandler();
        for (int type = 0; type < N_SUBTYPES; type++) {
            handlers[i++] = new SubHandler(type);
        }
        for (int type = 0; type < N_SUBTYPES; type++) {
            AggHandler handler = new AggHandler(type);
            mpTasks[type] = handler.tasks;
            handlers[i++] = handler;
        }
        setNewRootTimer();
    }

    public void execute() {
        System.out.println("TestDeletionPlugIn.execute()");
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].execute();
        }
        if (newRootTimer.hasExpired()) {
            addRootTask();
            setNewRootTimer();
        }
    }

    private long randomLong(long min, long max) {
        return min + (long) (random.nextDouble() * (max - min));
    }

    private void setNewRootTimer() {
	long interval = randomLong(minInterTaskInterval, maxInterTaskInterval);
        newRootTimer = wakeAfter(interval);
	System.out.println("Next wakeup after " + (interval/3600000.0) + " hours");
    }

    private void handleExpTasksAdded(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task expTask = (Task) tasks.nextElement();
            System.out.println("Exp task added: " + expTask);
            expandTask(expTask);
        }
    }

    private void handleExpTasksChanged(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task expTask = (Task) tasks.nextElement();
            System.out.println("Exp task changed: " + expTask);
        }
    }

    private void handleExpTasksRemoved(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task expTask = (Task) tasks.nextElement();
            System.out.println("Exp task removed: " + expTask);
            // There's nothing to do
        }
    }

    private void handleSubTasksAdded(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task subtask = (Task) tasks.nextElement();
            System.out.println("subtask added: " + subtask);
            aggregateSubtask(subtask);
        }
    }

    private void handleSubTasksChanged(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task subtask = (Task) tasks.nextElement();
            System.out.println("subtask changed: " + subtask);
        }
    }

    private void handleSubTasksRemoved(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task subtask = (Task) tasks.nextElement();
            System.out.println("subtask removed:. " + subtask);
        }
    }

    private void handleAggTasksAdded(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task aggtask = (Task) tasks.nextElement();
            System.out.println("aggtask added: " + aggtask);
            allocateAggtask(aggtask);
        }
    }

    private void handleAggTasksChanged(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task aggtask = (Task) tasks.nextElement();
            System.out.println("aggtask changed: " + aggtask);
            reallocateAggtask(aggtask);
        }
    }

    private void handleAggTasksRemoved(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task aggtask = (Task) tasks.nextElement();
            System.out.println("aggtask removed:. " + aggtask);
            advanceScheduleStartTime(aggtask);
        }
    }

    /**
     * Tasks are expanded into an number of subtasks. Constraints are
     * erected between the tasks restricting them to strictly
     * sequential execution. The duration of each subtask is between
     * 50% and 100% of equal fractions of the parent task duration.
     **/
    private void expandTask(Task expTask) {
        int nsubs = getSubtaskCount(expTask);
        long parentDuration = getDuration(expTask);
        long nominalDuration = parentDuration / nsubs;
        long parentStart = TaskUtils.getStartTime(expTask);
        long startTime = parentStart;
        Vector subs = new Vector(nsubs);
        Vector constraints = new Vector(nsubs + 1);
        Task previousTask = null;
        for (int i = 0; i < nsubs; i++) {
            long duration = (long) (random.nextDouble() * nominalDuration);
            NewTask subtask = createTask(getLevel(expTask), TYPE_SUB + i, theExpAsset,
                                         startTime, startTime + nominalDuration, duration);
            publishAdd(subtask);
            startTime += nominalDuration;
            subs.addElement(subtask);
            if (previousTask == null) {
                NewConstraint constraint = theLDMF.newConstraint();
                constraint.setConstrainingTask(expTask);
                constraint.setConstrainingAspect(AspectType.START_TIME);
                constraint.setConstrainedTask(subtask);
                constraint.setConstrainedAspect(AspectType.START_TIME);
                constraint.setConstraintOrder(Constraint.AFTER);
                constraints.addElement(constraint);
            } else {
                NewConstraint constraint = theLDMF.newConstraint();
                constraint.setConstrainingTask(previousTask);
                constraint.setConstrainingAspect(AspectType.END_TIME);
                constraint.setConstrainedTask(subtask);
                constraint.setConstrainedAspect(AspectType.START_TIME);
                constraint.setConstraintOrder(Constraint.AFTER);
                constraints.addElement(constraint);
            }
            previousTask = subtask;
        }
        NewConstraint constraint = theLDMF.newConstraint();
        constraint.setConstrainingTask(expTask);
        constraint.setConstrainingAspect(AspectType.END_TIME);
        constraint.setConstrainedTask(previousTask);
        constraint.setConstrainedAspect(AspectType.END_TIME);
        constraint.setConstraintOrder(Constraint.BEFORE);
        constraints.addElement(constraint);
        AllocationResult ar =
            PlugInHelper.createEstimatedAllocationResult(expTask, theLDMF, 1.0, true);
        Expansion exp = PlugInHelper.wireExpansion(expTask, subs, theLDMF, ar);
        NewWorkflow wf = (NewWorkflow) exp.getWorkflow();
        wf.setConstraints(constraints.elements());
        publishAdd(exp);
    }

    /**
     * Find an aggregation for which the timespan can accomodate the
     * subtask. If a suitable aggregation is not found, create a new
     * one.
     **/
    private void aggregateSubtask(Task subtask) {
        int subtype = getSubtype(subtask) - TYPE_SUB;
        long startTime = TaskUtils.getStartTime(subtask);
        long endTime = TaskUtils.getEndTime(subtask);
        for (Iterator i = mpTasks[subtype].iterator(); i.hasNext(); ) {
            NewMPTask mpTask = (NewMPTask) i.next();
            long mpStartTime = TaskUtils.getStartTime(mpTask);
            long mpEndTime = TaskUtils.getEndTime(mpTask);
            if (mpStartTime + AGGREGATION_PERIOD > endTime
                || mpEndTime - AGGREGATION_PERIOD < startTime) {
                createAggregation(subtask, mpTask);
                return;
            }
        }
        NewMPTask mpTask = createMPTask(getLevel(subtask), TYPE_AGG + subtype, theAggAsset,
                                        startTime, endTime, endTime - startTime);
        publishAdd(mpTask);
        createAggregation(subtask, mpTask);
    }

    private void createAggregation(Task subtask, NewMPTask mpTask) {
        long startTime = TaskUtils.getStartTime(subtask);
        if (startTime < TaskUtils.getStartTime(mpTask)) {
            setStartTimePreference(mpTask, startTime);
        }
        long endTime = TaskUtils.getEndTime(subtask);
        if (endTime > TaskUtils.getEndTime(mpTask)) {
            setEndTimePreference(mpTask, endTime);
        }
        AllocationResult ar =
            PlugInHelper.createEstimatedAllocationResult(subtask, theLDMF, 1.0, true);
        Aggregation agg = theLDMF.createAggregation(subtask.getPlan(), subtask,
                                                    mpTask.getComposition(),
                                                    ar);
        ((NewComposition) mpTask.getComposition()).addAggregation(agg);
        publishChange(mpTask);
        publishAdd(agg);
    }

    private void setStartTimePreference(NewTask mpTask, long newStartTime) {
        ScoringFunction sf;
        Preference pref;
        sf = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.START_TIME,
                                                                   newStartTime));
        pref = theLDMF.newPreference(AspectType.START_TIME, sf);
        mpTask.setPreference(pref);
    }

    private void setEndTimePreference(NewTask mpTask, long newEndTime) {
        ScoringFunction sf;
        Preference pref;
        sf = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.END_TIME,
                                                                   newEndTime));
        pref = theLDMF.newPreference(AspectType.END_TIME, sf);
        mpTask.setPreference(pref);
    }

    private void advanceScheduleStartTime(Task task) {
    }

    private static int getLevel(Task task) {
        PrepositionalPhrase pp = task.getPrepositionalPhrase(LEVEL);
        if (pp == null) {
            System.out.println("No LEVEL for " + task);
            return 0;
        }
        Integer io = (Integer) pp.getIndirectObject();
        return io.intValue();
    }

    private static int getSubtype(Task task) {
        PrepositionalPhrase pp = task.getPrepositionalPhrase(SUBTYPE);
        Integer io = (Integer) pp.getIndirectObject();
        return io.intValue();
    }

    private static long getDuration(Task task) {
        PrepositionalPhrase pp = task.getPrepositionalPhrase(DURATION);
        Long io = (Long) pp.getIndirectObject();
        return io.longValue();
    }

    private static int getSubtaskCount(Task task) {
        return N_SUBTYPES;
    }

    private void reallocateAggtask(Task subtask) {
        AllocationResult ar =
            PlugInHelper
            .createEstimatedAllocationResult(subtask, theLDMF, 1.0, true);
        Allocation alloc = (Allocation) subtask.getPlanElement();
        alloc.setEstimatedResult(ar);
    }

    private void allocateAggtask(Task subtask) {
        AllocationResult ar =
            PlugInHelper
            .createEstimatedAllocationResult(subtask, theLDMF, 1.0, true);
        Allocation alloc =
            theLDMF.createAllocation(subtask.getPlan(), subtask, theAllocAsset,
                                     ar, testProviderRole);
        publishAdd(alloc);
    }

    private NewMPTask createMPTask(int level, int subtype, Asset asset,
                                   long startTime, long endTime, long duration)
    {
        NewMPTask task = theLDMF.newMPTask();
        fillTask(task, level, subtype, asset, startTime, endTime, duration);
        NewComposition composition = theLDMF.newComposition();
        composition.setCombinedTask(task);
        task.setComposition(composition);
        return task;
    }
    private NewTask createTask(int level, int subtype, Asset asset,
                               long startTime, long endTime, long duration)
    {
        NewTask task = theLDMF.newTask();
        fillTask(task, level, subtype, asset, startTime, endTime, duration);
        return task;
    }
    private void fillTask(NewTask task, int level, int subtype, Asset asset,
                          long startTime, long endTime, long duration)
    {
        task.setVerb(testDeletionVerb);
        NewPrepositionalPhrase pp;
        Vector phrases = new Vector(3);

        pp = theLDMF.newPrepositionalPhrase();
        pp.setPreposition(DURATION);
        pp.setIndirectObject(new Long(duration));
        phrases.addElement(pp);

        pp = theLDMF.newPrepositionalPhrase();
        pp.setPreposition(LEVEL);
        pp.setIndirectObject(new Integer(level));
        phrases.addElement(pp);

        pp = theLDMF.newPrepositionalPhrase();
        pp.setPreposition(SUBTYPE);
        pp.setIndirectObject(new Integer(subtype));
        phrases.addElement(pp);

        task.setPrepositionalPhrases(phrases.elements());
	task.setDirectObject(asset);
        ScoringFunction sf;
        Preference pref;
        long slop = ((endTime - startTime) - duration) / 2L;
        if (slop <= 0) {
            sf = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.START_TIME,
                                                                       startTime));
        } else {
            double slope = 1.0 / slop; // Slope such that score reaches 1.0 in slop msec
            sf = new ScoringFunction.AboveScoringFunction(new AspectValue(AspectType.START_TIME,
                                                                          startTime), slope);
        }
        pref = theLDMF.newPreference(AspectType.START_TIME, sf);
        task.setPreference(pref);
        if (slop <= 0) {
            sf = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.END_TIME,
                                                                       endTime));
        } else {
            double slope = 1.0 / slop; // Slope such that score reaches 1.0 in slop msec
            sf = new ScoringFunction.BelowScoringFunction(new AspectValue(AspectType.END_TIME,
                                                                          endTime), slope);
        }
        pref = theLDMF.newPreference(AspectType.END_TIME, sf);
        task.setPreference(pref);
	task.addObservableAspect(AspectType.START_TIME);
	task.addObservableAspect(AspectType.END_TIME);
    }

    private void addRootTask() {
        long startTime = currentTimeMillis() + randomLong(minRootTaskDelay, maxRootTaskDelay);
        long duration = randomLong(minRootTaskDuration, maxRootTaskDuration);
        long endTime = startTime + 2L * duration;
        NewTask task = createTask(0, TYPE_ROOT, theRootAsset, startTime, endTime, duration);
        System.out.println("Adding " + task);
        publishAdd(task);
    }
}
