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
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.cougaar.util.Enumerator;
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
 * Root tasks have no OfType preposition.
 * Expansion of root tasks yields subtasks having OfType phrase
 * Aggregation of subtasks produce tasks at the next level. These
 * tasks have no OfType if they are to be allocated to another cluster
 **/
public class TestDeletionPlugIn extends SimplePlugIn {
    /** Subscriptions to tasks **/

    private IncrementalSubscription tasksToExpand;
    private IncrementalSubscription tasksToAggregate;
    private IncrementalSubscription tasksToAllocateLocally;
    private IncrementalSubscription tasksToAllocateRemotely;
    private Vector[] mpTasks;
    private IncrementalSubscription selfOrgs;

    /** Something to allocate subtasks to. **/
    private Asset theRootAsset; // DO of root tasks
    private Asset theExpAsset;  // DO of expansion subtasks
    private Asset theAggAsset;  // DO of aggregation mptasks
    private Asset theAllocAsset;  // subject of allocations

    /** The Role we use for allocations **/
    private Role testProviderRole;

    private Organization selfOrg;

    private Organization provider;

    /**
     * The verbs that we use. Tasks to be expanded are
     * TestDeletionExpand and Tasks to be aggregated are
     * TestDeletionAggregate.
     **/
    
    private static Verb testDeletionExpand = Verb.getVerb("TestDeletionExpand");
    private static Verb testDeletionAggregate = Verb.getVerb("TestDeletionAggregate");

    /** The preposition used to specify task level **/
    private static final String LEVEL = "AtLevel";

    /** The preposition used to specify subtask type **/
    private static final String SUBTYPE = "Type";

    private static final int N_SUBTYPES = 3;
    private static final int TYPE_ROOT = 1; // The subtype that is forwarded to another cluster

    /** The preposition used to specify task duration **/
    private static final String DURATION = "OfDuration";

    private long minRootTaskDelay     =  3 * 86400000L;
    private long maxRootTaskDelay     = 14 * 86400000L;
    private long minRootTaskDuration  =  3 * 86400000L;
    private long maxRootTaskDuration  = 14 * 86400000L;
    private long minInterTaskInterval =  1 * 3600000L;
    private long maxInterTaskInterval = 24 * 3600000L;
    private long AGGREGATION_PERIOD   =  5 * 86400000L;
    private long testDuration         =  0 * 86400000L; // Run test for 120 days
    private int nRoots = 2;     // Generate this many roots (0 means infinite);
    private int rootCount = 0;  // Number of roots so far
    private int level = 0;      // Our level
    private Alarm newRootTimer;
    private long testEnd;
    private boolean useProvider = false;
    private Random random = new Random();

    private UnaryPredicate expandPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Task) {
                Task task = (Task) o;
                return (task.getVerb().equals(testDeletionExpand)
                        && getLevel(task) == level);
            }
            return false;
        }
    };

    private UnaryPredicate aggregatePredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Task) {
                Task task = (Task) o;
                return (task.getVerb().equals(testDeletionAggregate)
                        && getLevel(task) == level);
            }
            return false;
        }
    };

    private UnaryPredicate allocateLocallyPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Task) {
                Task task = (Task) o;
                if (task.getVerb().equals(testDeletionExpand) && getLevel(task) == level + 1) {
                    return !useProvider || getSubtype(task) != TYPE_ROOT;
                }
            }
            return false;
        }
    };

    private UnaryPredicate allocateRemotelyPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Task) {
                Task task = (Task) o;
                if (task.getVerb().equals(testDeletionExpand) && getLevel(task) == level + 1) {
                    return useProvider && getSubtype(task) == TYPE_ROOT;
                }
            }
            return false;
        }
    };

    public void setupSubscriptions() {
        Vector params = getParameters();
        switch (params.size()) {
        default:
        case 4: testDuration = parseInterval((String) params.elementAt(3));
        case 3: nRoots = Integer.parseInt((String) params.elementAt(2));
        case 2: useProvider = ((String) params.elementAt(1)).trim().toLowerCase().equals("true");
        case 1: level = Integer.parseInt((String) params.elementAt(0));
        case 0: break;
        }
        System.out.println(" useProvider=" + ((String) params.elementAt(0)).trim().toLowerCase());
        System.out.println(" useProvider=" + useProvider);
        System.out.println("      nRoots=" + nRoots);
        System.out.println("testDuration=" + testDuration);
        Role.create("TestDeletionProvider", "TestDeletionCustomer");
        testProviderRole = Role.getRole("TestDeletionProvider");
        theRootAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestRoot"));
        theExpAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestExp"));
        theAggAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestAgg"));
        theAllocAsset = theLDMF.createInstance(theLDMF.createPrototype(Asset.class, "TestAlloc"));
        publishAdd(theRootAsset);
        publishAdd(theExpAsset);
        publishAdd(theAggAsset);
        publishAdd(theAllocAsset);
        selfOrgs = (IncrementalSubscription) subscribe(new UnaryPredicate() {
            public boolean execute(Object o) {
                if (o instanceof Organization) {
                    return ((Organization) o).isSelf();
                }
                return false;
            }
        });
        if (!useProvider) setupSubscriptions2();
    }

    private void setupSubscriptions2() {
        mpTasks = new Vector[N_SUBTYPES];
        for (int i = 0; i < mpTasks.length; i++) {
            mpTasks[i] = new Vector();
        }
        tasksToExpand = (IncrementalSubscription) subscribe(expandPredicate);
        tasksToAggregate = (IncrementalSubscription) subscribe(aggregatePredicate);
        tasksToAllocateLocally = (IncrementalSubscription) subscribe(allocateLocallyPredicate);
        tasksToAllocateRemotely = (IncrementalSubscription) subscribe(allocateRemotelyPredicate);
        testEnd = currentTimeMillis() + testDuration;
        setNewRootTimer();
    }

    public void execute() {
        System.out.println("TestDeletionPlugIn.execute()");
        if (useProvider) {
            if (provider == null) {
                if (selfOrgs.hasChanged()) {
                    if (selfOrg == null) {
                        checkSelfOrgs(selfOrgs.getAddedList());
                    }
                    if (selfOrg == null) return;
                    checkProvider();
                }
            }
            if (provider == null) return;
        }
        if (tasksToExpand.hasChanged()) {
            handleExpTasksAdded(tasksToExpand.getAddedList());
            handleExpTasksChanged(tasksToExpand.getChangedList());
            handleExpTasksRemoved(tasksToExpand.getRemovedList());
        }
        if (tasksToAggregate.hasChanged()) {
            handleSubTasksAdded(tasksToAggregate.getAddedList());
            handleSubTasksChanged(tasksToAggregate.getChangedList());
            handleSubTasksRemoved(tasksToAggregate.getRemovedList());
        }
        if (tasksToAllocateLocally.hasChanged()) {
            handleAggTasksAdded(tasksToAllocateLocally.getAddedList(), false);
            handleAggTasksChanged(tasksToAllocateLocally.getChangedList(), false);
            handleAggTasksRemoved(tasksToAllocateLocally.getRemovedList(), false);
        }
        if (tasksToAllocateRemotely.hasChanged()) {
            handleAggTasksAdded(tasksToAllocateRemotely.getAddedList(), true);
            handleAggTasksChanged(tasksToAllocateRemotely.getChangedList(), true);
            handleAggTasksRemoved(tasksToAllocateRemotely.getRemovedList(), true);
        }
            
        if (newRootTimer != null && newRootTimer.hasExpired()) {
            newRootTimer = null;
            addRootTask();
            rootCount++;
            setNewRootTimer();
        }
    }

    private void checkSelfOrgs(Enumeration orgs) {
        if (orgs.hasMoreElements()) {
            selfOrg = (Organization) orgs.nextElement();
        }
    }

    private void checkProvider() {
        Collection c = selfOrg.getRelationshipSchedule().getMatchingRelationships(testProviderRole);
        if (c.size() > 0) {
            Relationship relationship = (Relationship) c.iterator().next();
            if (relationship.getRoleA().equals(testProviderRole)) {
                provider = (Organization) relationship.getA();
            } else {
                provider = (Organization) relationship.getB();
            }
            setupSubscriptions2(); // Ready to go
        }
    }

    private long randomLong(long min, long max) {
        return min + (long) (random.nextDouble() * (max - min));
    }

    private void setNewRootTimer() {
        if ((testDuration <= 0L || currentTimeMillis() < testEnd)
            && (nRoots < 0 || rootCount < nRoots)) {
            long interval = randomLong(minInterTaskInterval, maxInterTaskInterval);
            newRootTimer = wakeAfter(interval);
            System.out.println("Next wakeup after " + (interval/3600000.0) + " hours");
        } else {
            System.out.println("No wakeup: " + testDuration + ", " + nRoots);
        }
    }

    private void handleExpTasksAdded(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task expTask = (Task) tasks.nextElement();
            System.out.println("Exp task added: " + format(expTask));
            expandTask(expTask);
        }
    }

    private void handleExpTasksChanged(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task expTask = (Task) tasks.nextElement();
            System.out.println("Exp task changed: " + format(expTask));
        }
    }

    private void handleExpTasksRemoved(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task expTask = (Task) tasks.nextElement();
            System.out.println("Exp task removed: " + format(expTask));
            // There's nothing to do
        }
    }

    private void handleSubTasksAdded(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task subtask = (Task) tasks.nextElement();
            System.out.println("subtask added: " + format(subtask));
            aggregateSubtask(subtask);
        }
    }

    private void handleSubTasksChanged(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task subtask = (Task) tasks.nextElement();
            System.out.println("subtask changed: " + format(subtask));
        }
    }

    private void handleSubTasksRemoved(Enumeration tasks) {
        while (tasks.hasMoreElements()) {
            Task subtask = (Task) tasks.nextElement();
            System.out.println("subtask removed:. " + format(subtask));
        }
    }

    private void handleAggTasksAdded(Enumeration tasks, boolean remote) {
        while (tasks.hasMoreElements()) {
            Task aggtask = (Task) tasks.nextElement();
            System.out.println("aggtask added: " + format(aggtask));
            allocateAggtask(aggtask, remote);
        }
    }

    private void handleAggTasksChanged(Enumeration tasks, boolean remote) {
        while (tasks.hasMoreElements()) {
            Task aggtask = (Task) tasks.nextElement();
            System.out.println("aggtask changed: " + format(aggtask));
            reallocateAggtask(aggtask, remote);
        }
    }

    private void handleAggTasksRemoved(Enumeration tasks, boolean remote) {
        while (tasks.hasMoreElements()) {
            Task aggtask = (Task) tasks.nextElement();
            System.out.println("aggtask removed:. " + format(aggtask));
            int subtype = getSubtype(aggtask);
            mpTasks[subtype].remove(aggtask);
            advanceScheduleStartTime(aggtask, remote);
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
            NewTask subtask = createTask(testDeletionAggregate, getLevel(expTask), i, theExpAsset,
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
        int subtype = getSubtype(subtask);
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
        NewMPTask mpTask = createMPTask(testDeletionExpand, getLevel(subtask) + 1, subtype, theAggAsset,
                                        startTime, endTime, endTime - startTime);
        mpTasks[subtype].add(mpTask);
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
        NewComposition comp = (NewComposition) mpTask.getComposition();
        comp.addAggregation(agg);
        mpTask.setParentTasks(new Enumerator(comp.getParentTasks()));
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

    private void advanceScheduleStartTime(Task task, boolean remote) {
    }

    private static int getLevel(Task task) {
        PrepositionalPhrase pp = task.getPrepositionalPhrase(LEVEL);
        if (pp == null) {
            System.out.println("No LEVEL for " + format(task));
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

    private void reallocateAggtask(Task subtask, boolean remote) {
        AllocationResult ar =
            PlugInHelper
            .createEstimatedAllocationResult(subtask, theLDMF, 1.0, true);
        Allocation alloc = (Allocation) subtask.getPlanElement();
        alloc.setEstimatedResult(ar);
        publishChange(alloc);
    }

    private void allocateAggtask(Task subtask, boolean remote) {
        int subtype = getSubtype(subtask);
        Asset asset;
        if (remote) {
            asset = provider;
            System.out.println("Using provider " + provider);
        } else {
            asset = theAllocAsset;
        }
        if (asset == null) return; // Wait 'til out provider checks reports for service
        AllocationResult ar =
            PlugInHelper
            .createEstimatedAllocationResult(subtask, theLDMF, 1.0, true);
        Allocation alloc =
            theLDMF.createAllocation(subtask.getPlan(), subtask, asset,
                                     ar, testProviderRole);
        publishAdd(alloc);
    }

    private NewMPTask createMPTask(Verb verb, int level, int subtype, Asset asset,
                                   long startTime, long endTime, long duration)
    {
        NewMPTask task = theLDMF.newMPTask();
        fillTask(task, verb, level, subtype, asset, startTime, endTime, duration);
        NewComposition composition = theLDMF.newComposition();
        composition.setCombinedTask(task);
        task.setComposition(composition);
        return task;
    }
    private NewTask createTask(Verb verb, int level, int subtype, Asset asset,
                               long startTime, long endTime, long duration)
    {
        NewTask task = theLDMF.newTask();
        fillTask(task, verb, level, subtype, asset, startTime, endTime, duration);
        return task;
    }
    private void fillTask(NewTask task, Verb verb, int level, int subtype, Asset asset,
                          long startTime, long endTime, long duration)
    {
        task.setVerb(verb);
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
        NewTask task = createTask(testDeletionExpand, level, TYPE_ROOT, theRootAsset, startTime, endTime, duration);
        System.out.println("Adding " + format(task));
        publishAdd(task);
    }

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HHmm");

    private static String format(Task task) {
        return task.getDirectObject().getTypeIdentificationPG().getTypeIdentification()
            + ", level="
            + getLevel(task)
            + ", subtype="
            + getSubtype(task)
            + ", start="
            + dateFormat.format(new Date(TaskUtils.getStartTime(task)))
            + ", end="
            + dateFormat.format(new Date(TaskUtils.getEndTime(task)));
    }
}
