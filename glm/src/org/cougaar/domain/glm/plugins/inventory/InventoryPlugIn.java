package org.cougaar.domain.glm.plugins.inventory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.plugin.util.PlugInHelper;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.plan.Agency;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.Aggregation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.Composition;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.NewComposition;
import org.cougaar.domain.planning.ldm.plan.NewMPTask;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.util.Enumerator;
import org.cougaar.util.UnaryPredicate;

public abstract class InventoryPlugIn extends GLMDecorationPlugIn {

    public static class InventoryItemInfo {
        public double[] levels;
        public GregorianCalendar reportBase;
        public int reportStepKind;

        public InventoryItemInfo(double[] levels, GregorianCalendar reportBase, int reportStepKind) {
            this.levels = levels;
            this.reportBase = reportBase;
            this.reportStepKind = reportStepKind;
        }
    }

    private static class InventoryTypeHashEntry {
        Vector invBins = new Vector();
        UnaryPredicate dueOutPredicate;
	ProjectionWeight projectionWeight;
	boolean fillToCapacity;
        public InventoryTypeHashEntry(UnaryPredicate predicate, ProjectionWeight weight) {
            dueOutPredicate = predicate;
	    projectionWeight = weight;
	    fillToCapacity = false;
        }
    }

    public static class WithdrawTaskPredicate implements UnaryPredicate {
        String supplyType_;
        public WithdrawTaskPredicate(String type) {
            supplyType_ = type;
        }
	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task)o;
		if (task.getVerb().equals(Constants.Verb.WITHDRAW) ||
		    task.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {		 
                    if (TaskUtils.isDirectObjectOfType(task, supplyType_) ||
                        TaskUtils.isTaskPrepOfType(task, supplyType_)) {
                        // 		    if (TaskUtils.getQuantity(task) > 0.0){
                        return true;
                        // 		    }
                    }
		}
	    }
	    return false;
	}
    };

    /** Subscription for aggregatable support requests. **/
    private IncrementalSubscription detReqSubscription_;

    /** Subscription for the aggregated support request **/
    private CollectionSubscription aggMILSubscription_;

    /** Subscription for the MIL tasks **/
    private IncrementalSubscription milSubscription_;

    /** The aggMIL task found/created during the current transaction **/
    private Task aggMILTask_ = null;

    // Hashtable - keys asset id (nsn), elements array of levels 
    // filled in from file during initialization.  Used to create inventories as needed.
    public Hashtable inventoryInitHash_ = new Hashtable();

    /** key is type id, value is an Inventory */
    // cleared in initInventories() and added to initInventories and publishInventories
    private Hashtable inventoryHash_ = new Hashtable();
    private Hashtable inventoryTypeHash_ = new Hashtable();
    private Hashtable MILTaskHash_ = new Hashtable();

//      protected CollectionSubscription inventorySubscription_ = null;

    public InventoryPlugIn() {
	super();
        setExecutionDelay(10000, 60000);
    }

    public synchronized void execute() {
        aggMILTask_ = null;
        if (detReqSubscription_.hasChanged()) {
            aggregateDetermineRequirementsTasks((NewMPTask) getDetermineRequirementsTask(),
                                               detReqSubscription_.getAddedList());
        }
        if (milSubscription_.hasChanged()) {
            //Added tasks are handled at the time of creation
            removeMILTasks(milSubscription_.getRemovedList());
        }
	super.execute();
    }

    protected void setupSubscriptions() {
	super.setupSubscriptions();
//  	inventorySubscription_ = (CollectionSubscription) subscribe(new InventoryPredicate());
        aggMILSubscription_ = (CollectionSubscription) subscribe(new AggMILPredicate(), false);
	// Determine requirements task subscription 
	detReqSubscription_ = (IncrementalSubscription) subscribe(new DetInvReqPredicate());
        milSubscription_ = (IncrementalSubscription) subscribe(new MILPredicate());
	addInventories(query(new InventoryPredicate()));
	addMILTasks(milSubscription_.elements());
    }

    // Predicates

    /**
       Passes Inventory assets that have a valied InventoryPG
     **/
    private static class InventoryPredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    if (o instanceof Inventory) {
		InventoryPG invpg = 
		    (InventoryPG) ((Inventory) o).searchForPropertyGroup(InventoryPG.class);
		if (invpg != null) {
		    return true;    
		}
	    }
	    return false;
	}
    };

    /**
       Passes DetermineRequirements tasks of type MaintainInventory.
     **/
    private static class DetInvReqPredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    if (o instanceof Task) {
		Task t = (Task) o;
		if (t.getVerb().equals(Constants.Verb.DETERMINEREQUIREMENTS)) {
		    return TaskUtils.isTaskOfType(t, "MaintainInventory");
		}
	    }
	    return false;
	}
    }

    /**
       Selects the per-inventory MaintainInventory tasks.
     **/
    private static class MILPredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    if (o instanceof Task) {
                Task t = (Task) o;
                if (t.getVerb().equals(Constants.Verb.MAINTAININVENTORY)) {
                    return t.getDirectObject() != null; // true if this is the agg task
                }
            }
	    return false;
	}
    }

    /**
       Selects the aggregate MaintainInventory task
     **/
    private static class AggMILPredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    if (o instanceof Task) {
		Task t = (Task) o;
		if (t.getVerb().equals(Constants.Verb.MAINTAININVENTORY)) {
                    return t.getDirectObject() == null; // true if this is not the agg task
                }
            }
	    return false;
	}
    }

    /**
       Add some inventories to the inventoryHash_.
     **/
    private void addInventories(Collection inventories) {
        for (Iterator i = inventories.iterator(); i.hasNext(); ) {
            addInventory((Inventory) i.next());
	}
    }

    private void removeInventories(Enumeration inventories) {
	while (inventories.hasMoreElements()) {
            removeInventory((Inventory) inventories.nextElement());
	}
    }

    private void addInventory(Inventory inventory) {
        addInventory(inventory, getInventoryType(inventory));
    }

    private void addInventory(Inventory inventory, String item) {
        inventoryHash_.put(item, inventory);
        getInventoryTypeHashEntry(getAssetType(inventory)).invBins.add(inventory);
    }

    private void removeInventory(Inventory inventory) {
        removeInventory(inventory, getInventoryType(inventory));
    }

    private void removeInventory(Inventory inventory, String item) {
        inventoryHash_.remove(item);
        getInventoryTypeHashEntry(getAssetType(inventory)).invBins.remove(inventory);
    }

    private void addMILTasks(Enumeration milTasks) {
        while (milTasks.hasMoreElements()) {
	    Task task = (Task) milTasks.nextElement();
	    Inventory inventory = (Inventory) task.getDirectObject();
	    MILTaskHash_.put(inventory, task);
	}
    }

    private void removeMILTasks(Enumeration milTasks) {
        while (milTasks.hasMoreElements()) {
	    Task task = (Task) milTasks.nextElement();
	    Inventory inventory = (Inventory) task.getDirectObject();
	    MILTaskHash_.remove(inventory);
	}
    }

    /**
       Get _the_ aggregate MIL task. This is complicated because we
       want to detect when the task has been deleted, but we only want
       to create one of them. The lag between publishing a new task
       and its appearance in the subscription poses a problem because,
       typically, this method is called repeatedly in one transaction.
       We store the task temporarily in a variable (aggMILTask_) to
       prevent multiple creation, but clear the variable at the
       beginning of each new transaction. If the task has not yet been
       created, we try to create it by aggregating all the existing
       per-oplan DetermineRequirements tasks into it. Subsequent
       per-oplan tasks will be aggregated in as they arrive. There
       will be no task if there are no DetermineRequirements tasks to
       be aggregated.
     **/
    public Task getDetermineRequirementsTask() {
        if (aggMILTask_ == null) {
            if (!aggMILSubscription_.isEmpty()) {
                aggMILTask_ = (Task) aggMILSubscription_.elements().nextElement();
            } else if (!detReqSubscription_.isEmpty()) {
                aggMILTask_ = createAggTask(detReqSubscription_.elements());
                publishAdd(aggMILTask_);
            }
        }
        return aggMILTask_;
    }

    /**
       Aggregate some DetermineRequirements tasks
     **/
    private void aggregateDetermineRequirementsTasks(NewMPTask mpTask, Enumeration e) {
        if (!e.hasMoreElements()) return;
        if (mpTask == null) return;
        NewComposition composition = (NewComposition) mpTask.getComposition();
        long minStartTime;
        long maxEndTime;
        try {
            maxEndTime = TaskUtils.getEndTime(mpTask);
        } catch (IllegalArgumentException iae) {
            maxEndTime = Long.MIN_VALUE;
        }
        try {
            minStartTime = TaskUtils.getStartTime(mpTask);
        } catch (IllegalArgumentException iae) {
            minStartTime = Long.MAX_VALUE;
        }
        while (e.hasMoreElements()) {
            Task parent = (Task) e.nextElement();
            if (parent.getPlanElement() != null) continue; // Already aggregated
            minStartTime = Math.min(minStartTime, TaskUtils.getStartTime(parent));
            maxEndTime = Math.max(maxEndTime, TaskUtils.getEndTime(parent));
            if (parent.getPlanElement() != null) continue;
            AllocationResult estAR =
                PlugInHelper.createEstimatedAllocationResult(parent, theLDMF, 1.0, true);
            Aggregation agg =
                theLDMF.createAggregation(parent.getPlan(),
                                          parent,
                                          composition,
                                          estAR);
            publishAdd(agg);
            composition.addAggregation(agg);
        }
        setStartTimePreference(mpTask, minStartTime);
        setEndTimePreference(mpTask, maxEndTime);
        mpTask.setParentTasks(new Enumerator (composition.getParentTasks()));
    }

    private void setStartTimePreference(NewTask mpTask, long newStartTime) {
        ScoringFunction sf;
        Preference pref;
        sf = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.START_TIME,
                                                                   newStartTime));
        pref = theLDMF.newPreference(AspectType.START_TIME, sf);
        mpTask.setPreference(pref);
//          mpTask.setCommitmentDate(new Date(newStartTime));

    }

    private void setEndTimePreference(NewTask mpTask, long newEndTime) {
        ScoringFunction sf;
        Preference pref;
        sf = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.END_TIME,
                                                                   newEndTime));
        pref = theLDMF.newPreference(AspectType.END_TIME, sf);
        mpTask.setPreference(pref);
    }

    /**
       Find or make the aggregated MIL task for an inventory. If the
       MILTaskHash_ does not contain an existing task, create a new
       one and link it to the determine requirements tasks.
     **/
    public Task findOrMakeMILTask(Inventory inventory) {
	// Creates the MaintainInventoryLevels Task for this item 
	// if one does not already exist
	NewTask milTask = (NewTask) MILTaskHash_.get(inventory);
	if (milTask == null) {
            Task parent = getDetermineRequirementsTask();
            if (parent == null) {
                /**
                  This might happen just after the last determine
                  requirements task is removed. Because of inertia,
                  the inventory manager might still be trying to
                  refill the inventory although, in fact the demand
                  for that inventory is in the process of
                  disappearing. The caller, getting a null return will
                  simply abandon the attempt to do the refill.
                 **/
                GLMDebug.DEBUG("InventoryPlugIn",
                               "findOrMakeMILTask(), CANNOT CREATE MILTASK, no parent, inventory: "
                               + AssetUtils.assetDesc(inventory
                                                      .getScheduledContentPG()
                                                      .getAsset()));	
                return null; // Can't make one
            }
            milTask = createMILTask(parent, inventory);
            publishAddToExpansion(parent, milTask);
            MILTaskHash_.put(inventory, milTask);
        }
	return milTask;
    }

    public synchronized Inventory findOrMakeInventory(String supplytype, String id) {
	Asset resource = theLDMF.getPrototype(id);
	if (resource == null) {
	    GLMDebug.DEBUG("InventoryPlugIn", "<"+supplytype+"> createInventory fail to get prototype for "+id);
	    resource = theLDMF.createPrototype(supplytype, id);
	}
	if (resource == null) {
	    GLMDebug.ERROR("InventoryPlugIn","<"+supplytype+"> createInventory fail to make prototype for "+id);
	    return null;
	}
	return findOrMakeInventory(supplytype, resource);
    }

    /**
       Override this if you want to use a different predicate for selecting due out tasks
     **/

    protected UnaryPredicate createDueOutPredicate(String supplyType) {
        return new WithdrawTaskPredicate(supplyType);
    }
        
    private InventoryTypeHashEntry getInventoryTypeHashEntry(String supplyType) {
        InventoryTypeHashEntry result =
            (InventoryTypeHashEntry) inventoryTypeHash_.get(supplyType);
        if (result == null) {
	    ProjectionWeight weight = createProjectionWeight(supplyType);
            result = new InventoryTypeHashEntry(createDueOutPredicate(supplyType), weight);
            inventoryTypeHash_.put(supplyType, result);
        }
        return result;
    }

    public ProjectionWeight getProjectionWeight(String supplyType) {
        InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
        return entry.projectionWeight;
    }

    /**
     * Set the ProjectionWeight for a class of supply. This becomes
     * the default ProjectionWeight for new Inventory Assets for the
     * class of supply. The ProjectionWeight of existing Inventory
     * assets is updated to the new ProjectionWeight. Override this if
     * you don't want this behavior.
     **/
    public void setProjectionWeight(String supplyType, ProjectionWeight weight) {
	InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
	entry.projectionWeight = weight;
	Enumeration e = entry.invBins.elements();
	while (e.hasMoreElements()) {
            Inventory inv = (Inventory) e.nextElement();
	    NewInventoryPG invpg = (NewInventoryPG) inv.getInventoryPG();
	    invpg.setProjectionWeight(weight);
	}
    }

    /**
     * Create a default ProjectWeight for a class of supply. This
     * implementation creates a new ProjectionWeightImpl with default
     * settings. Override this if you want a different default.
     **/
    protected ProjectionWeight createProjectionWeight(String supplyType) {
	return new ProjectionWeightImpl();
    }

    public boolean getFillToCapacity(String supplyType) {
	InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
        return entry.fillToCapacity;
    }

    /**
     * Set the fillToCapacity for a class of supply. This becomes
     * the default fillToCapacity for new Inventory Assets for the
     * class of supply. The fillToCapacity of existing Inventory
     * assets is updated to the new value (true/false). Override this if
     * you don't want this behavior.
     **/
    public void setFillToCapacity(String supplyType, boolean fill_to_capacity) {
	InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
	entry.fillToCapacity = fill_to_capacity;
	Enumeration e = entry.invBins.elements();
	while (e.hasMoreElements()) {
            Inventory inv = (Inventory) e.nextElement();
	    NewInventoryPG invpg = (NewInventoryPG) inv.getInventoryPG();
	    invpg.setFillToCapacity(fill_to_capacity);
	}
    }

    public synchronized Inventory findOrMakeInventory(String supplytype, Asset resource) {
	Inventory inventory = null;
	// only need to sync if each processor is running on its own thread.
	// currently, ANTS is single-threaded but could easily be made multi-threaded.
	String id = resource.getTypeIdentificationPG().getTypeIdentification();
	inventory = (Inventory) inventoryHash_.get(id);
	if (inventory == null) {
	    inventory = createInventory(supplytype, resource);
	    if (inventory != null) {
		addInventory(inventory, id);
		getDelegate().publishAdd(inventory);
		GLMDebug.DEBUG("InventoryPlugIn", "findOrMakeInventory(), CREATED inventory bin for: "+
				  AssetUtils.assetDesc(inventory.getScheduledContentPG().getAsset()));
		findOrMakeMILTask(inventory);
	    }
	}
	return inventory;
    }

    public final synchronized UnaryPredicate getDueOutPredicate(String supplyType) {
        InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
        return entry.dueOutPredicate;
    }

    public abstract Inventory createInventory(String supplytype, Asset resource);

    public String getInventoryType(Inventory inventory) {
	ScheduledContentPG scp = inventory.getScheduledContentPG();
	Asset proto = scp.getAsset();
	if (proto == null) {
	    GLMDebug.DEBUG("InventoryPlugIn", "getInventoryType failed to get asset for "+
			      inventory.getScheduledContentPG().getAsset().getTypeIdentificationPG());
	    return "";
	}
	return proto.getTypeIdentificationPG().getTypeIdentification();
    }

    public String getAssetType(Inventory inventory) {
	
// 	Asset a = inventory.getScheduledContentPG().getAsset();
// 	String type = a.getClass().getName();
// 	type = type.substring(type.lastIndexOf('.')+1);
// 	return type;
      InventoryPG invpg = 
	(InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
      Asset a = invpg.getResource();
      SupplyClassPG pg = (SupplyClassPG)a.searchForPropertyGroup(SupplyClassPG.class);
      return pg.getSupplyType();
    }

    public Enumeration getInventoryBins(String assetType) {
        InventoryTypeHashEntry entry = getInventoryTypeHashEntry(assetType);
        return entry.invBins.elements();
    }

    public void initializeInventoryFile(String type) {

	String invFile = getInventoryFile(type);
	Enumeration initialInv = FileUtils.readConfigFile(invFile);
	if (initialInv != null) {
	    stashInventoryInformation(type, initialInv);
	    GLMDebug.DEBUG("InventoryPlugIn", getClusterIdentifier(), 
			      "initializeInventory(), Inventory file is "+invFile+" for "+type);
	}
    }

    private String getInventoryFile(String type) {
	String result = null;
	// if defined in plugin argument list
	String inv_file = null;
	if ((inv_file = (String)getParam(type+"_inv")) != null) {
	    result = inv_file;
	} 
	else {
  	    result = getClusterSuffix(myOrganization_.getClusterPG().getClusterIdentifier().toString()) +
  		"_"+type.toLowerCase()+".inv";
	}
	return result;
    }

    private String getClusterSuffix(String clusterId) {
	String result = null;
	int i = clusterId.lastIndexOf("-");
	if (i == -1) {
	    result = clusterId;
	} 
	else {
	    result = clusterId.substring(i+1);
	}
	return result;
    }

  private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();

  static {
    SimpleDateFormat sdf = (SimpleDateFormat) dateTimeFormat;
    sdf.applyPattern("M/d/yyyy h:mm z");
  }

  private GregorianCalendar parseDate(String s) {
    try {
      GregorianCalendar result = new GregorianCalendar();
      result.setTime(dateTimeFormat.parse(s));
      return result;
    } catch (ParseException pe) {
      pe.printStackTrace();
      return null;
    }
  }

    void stashInventoryInformation(String type, Enumeration initInv){
	String line;
	String item = null;
	double capacity, level, erq, min_reorder;
        GregorianCalendar reportBaseDate;
        int reportStepKind;
	int entries = 0;
	GLMDebug.DEBUG("InventoryPlugIn",getClusterIdentifier(), "<"+type+"> Stashing inventories info...");
	
	while(initInv.hasMoreElements()) {
	    line = (String) initInv.nextElement();
	    // Find the fields in the line, values seperated by ','
	    Vector fields = FileUtils.findFields(line, ',');
	    if (fields.size() < 5)
		continue;
	    item = (String)fields.elementAt(0);
	    capacity = Double.valueOf((String)fields.elementAt(1)).doubleValue();
	    level = Double.valueOf((String)fields.elementAt(2)).doubleValue();
	    erq = Double.valueOf((String)fields.elementAt(3)).doubleValue();
	    min_reorder = Double.valueOf((String)fields.elementAt(4)).doubleValue();
            if (fields.size() < 7) {
              reportBaseDate = null;
              reportStepKind = 0;

            } else {
              reportBaseDate = parseDate((String) fields.elementAt(5));
              reportStepKind = Integer.parseInt((String) fields.elementAt(6));
            }
// 	    GLMDebug.DEBUG("InventoryPlugIn", clusterId_, "collected info for inventory item:"+
// 			      item+" capacity:"+capacity+" level:"+level +" erq:"+erq+" min reorder:"+ min_reorder);
	    double[] levels = {capacity,level,erq,min_reorder};
	    inventoryInitHash_.put(item, new InventoryItemInfo(levels, reportBaseDate, reportStepKind));
	    entries++;
	}
	GLMDebug.DEBUG("InventoryPlugIn", clusterId_, "stashInventory(), number of inventory bins: "+entries+"for "+type);
    }

    /**
       Create a MaintainInventory task for an inventory. This task is
       the parent of all refill tasks for the inventory and is itself
       a subtask of the aggregated determine requirements tasks.
     **/
    private NewTask createMILTask(Task parent, Inventory inventory) {
	NewTask subtask = theLDMF.newTask();
	subtask.setDirectObject(inventory);
	subtask.setParentTask(parent);
	subtask.setVerb(new Verb(Constants.Verb.MAINTAININVENTORY));
        setStartTimePreference(subtask, TaskUtils.getStartTime(parent));
        setEndTimePreference(subtask, TaskUtils.getEndTime(parent));
	return subtask;
    }

    /**
       Create the aggregated determine requrements task. This task is
       the parent of all the per-inventory MaintainInventory tasks.
       It, too, uses the MaintainInventory verb but with no direct
       object. It is an MPTask that combines all the
       DetermineRequirements tasks of type MaintainInventory. The
       Composition of this MPTask is non-propagating so it is
       rescinded only if all the parent tasks are rescinded.
     **/
    private NewMPTask createAggTask(Enumeration parents) {
	NewMPTask mpTask = theLDMF.newMPTask();
        NewComposition composition = theLDMF.newComposition();
        composition.setIsPropagating(false);
        mpTask.setComposition(composition);
        composition.setCombinedTask(mpTask);
	mpTask.setVerb(new Verb(Constants.Verb.MAINTAININVENTORY));
        aggregateDetermineRequirementsTasks(mpTask, parents);
	return mpTask;
    }
}
