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
 *   Allocates subtasks to local or remote assets using a simple
 *       scheduling algorithm
 *   Watches for deletion of expired tasks and modifies its scheduling
 *       data to account for the deleted tasks
 **/
public class TestDeletionPlugIn extends SimplePlugIn {
    /** Subscription to exp tasks **/
    private IncrementalSubscription expTasks;

    /** Subscription to subtasks **/
    private IncrementalSubscription subTasks;

    /** Something to allocate subtasks to. **/
    private Asset theAsset;

    /** The Role we use for allocations **/
    private Role testProviderRole;

    /** The verb that we use **/
    private static Verb testDeletionVerb = Verb.getVerb("TestDeletion");

    /** The preposition used to specify task level **/
    private static final String LEVEL = "AtLevel";

    /** The preposition used to specify task duration **/
    private static final String DURATION = "OfDuration";

    private long minRootTaskDelay     =  3 * 86400000L;
    private long maxRootTaskDelay     = 14 * 86400000L;
    private long minRootTaskDuration  =  3 * 86400000L;
    private long maxRootTaskDuration  = 14 * 86400000L;
    private long minInterTaskInterval =  1 * 86400000L;
    private long maxInterTaskInterval =  3 * 86400000L;
    private Alarm newRootTimer;
    private Random random = new Random();

    private boolean testTask(Object o, int oddness) {
        if (o instanceof Task) {
            Task task = (Task) o;
            if (task.getVerb().equals(testDeletionVerb)) {
                int level = getLevel(task);
                return level % 2 == oddness;
            }
        }
        return false;
    }

    public void setupSubscriptions() {
        expTasks = (IncrementalSubscription) subscribe(new UnaryPredicate() {
            public boolean execute(Object o) {
                return testTask(o, 0);
            }
        });
        subTasks = (IncrementalSubscription) subscribe(new UnaryPredicate() {
            public boolean execute(Object o) {
                return testTask(o, 1);
            }
        });
        Role.create("TestDeletionProvider", "TestDeletionCostumer");
        testProviderRole = Role.getRole("TestDeletionProvider");
        Asset proto = theLDMF.createPrototype(Asset.class, "TestDeletion");
        theAsset = theLDMF.createInstance(proto);
        publishAdd(theAsset);
        setNewRootTimer();
    }

    public void execute() {
        System.out.println("TestDeletionPlugIn.execute()");
        if (expTasks.hasChanged()) {
            handleExpTasksAdded(expTasks.getAddedList());
            handleExpTasksRemoved(expTasks.getRemovedList());
        }
        if (subTasks.hasChanged()) {
            handleSubTasksAdded(subTasks.getAddedList());
            handleSubTasksRemoved(expTasks.getRemovedList());
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

    private void handleExpTasksAdded(Enumeration addedTasks) {
        while (addedTasks.hasMoreElements()) {
            Task expTask = (Task) addedTasks.nextElement();
            System.out.println("Exp task added: " + expTask);
            expandTask(expTask);
        }
    }

    private void handleExpTasksRemoved(Enumeration removedTasks) {
        while (removedTasks.hasMoreElements()) {
            Task expTask = (Task) removedTasks.nextElement();
            System.out.println("Exp task removed: " + expTask);
            // There's nothing to do
        }
    }

    private void handleSubTasksAdded(Enumeration addedTasks) {
        while (addedTasks.hasMoreElements()) {
            Task subtask = (Task) addedTasks.nextElement();
            System.out.println("subtask added: " + subtask);
            allocateSubtask(subtask);
        }
    }

    private void handleSubTasksRemoved(Enumeration removedTasks) {
        while (removedTasks.hasMoreElements()) {
            Task subtask = (Task) removedTasks.nextElement();
            System.out.println("subtask removed:. " + subtask);
            advanceScheduleStartTime(subtask);
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
            NewTask subtask = createTask(getLevel(expTask) + 1, startTime, startTime + nominalDuration, duration);
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
        Expansion exp = PlugInHelper.wireExpansion(expTask, subs, theLDMF);
        NewWorkflow wf = (NewWorkflow) exp.getWorkflow();
        wf.setConstraints(constraints.elements());
        publishAdd(exp);
    }

    private void advanceScheduleStartTime(Task task) {
    }

    private static int getLevel(Task task) {
        PrepositionalPhrase pp = task.getPrepositionalPhrase(LEVEL);
        Integer io = (Integer) pp.getIndirectObject();
        return io.intValue();
    }

    private static long getDuration(Task task) {
        PrepositionalPhrase pp = task.getPrepositionalPhrase(DURATION);
        Long io = (Long) pp.getIndirectObject();
        return io.longValue();
    }

    private static int getSubtaskCount(Task task) {
        return 5;
    }

    private void allocateSubtask(Task subtask) {
        AllocationResult ar =
            PlugInHelper
            .createEstimatedAllocationResult(subtask, theLDMF, 1.0, true);
        Allocation alloc =
            theLDMF.createAllocation(subtask.getPlan(), subtask, theAsset,
                                     ar, testProviderRole);
        publishAdd(alloc);
    }

    private NewTask createTask(int level, long startTime, long endTime, long duration) {
        NewTask task = theLDMF.newTask();
        task.setVerb(testDeletionVerb);
        NewPrepositionalPhrase pp;
        Vector phrases = new Vector(2);
        pp = theLDMF.newPrepositionalPhrase();
        pp.setPreposition(DURATION);
        pp.setIndirectObject(new Long(duration));
        phrases.addElement(pp);
        pp = theLDMF.newPrepositionalPhrase();
        pp.setPreposition(LEVEL);
        pp.setIndirectObject(new Integer(level));
        phrases.addElement(pp);
        task.setPrepositionalPhrases(phrases.elements());
	task.setDirectObject(theAsset);
        ScoringFunction sf;
        Preference pref;
        long slop = ((endTime - startTime) - duration) / 2L;
        double slope = 1.0 / slop; // Slope such that score reaches 1.0 in slop msec
        sf = new ScoringFunction.AboveScoringFunction(new AspectValue(AspectType.START_TIME, startTime), slope);
        pref = theLDMF.newPreference(AspectType.START_TIME, sf);
        task.setPreference(pref);
        sf = new ScoringFunction.BelowScoringFunction(new AspectValue(AspectType.END_TIME, endTime), slope);
        pref = theLDMF.newPreference(AspectType.END_TIME, sf);
        task.setPreference(pref);
	task.addObservableAspect(AspectType.START_TIME);
	task.addObservableAspect(AspectType.END_TIME);
        return task;
    }

    private void addRootTask() {
        long startTime = currentTimeMillis() + randomLong(minRootTaskDelay, maxRootTaskDelay);
        long duration = randomLong(minRootTaskDuration, maxRootTaskDuration);
        long endTime = startTime + 2L * duration;
        NewTask task = createTask(0, startTime, endTime, duration);
        System.out.println("Adding " + task);
        publishAdd(task);
    }
}
