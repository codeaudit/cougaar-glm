/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.glm.plugins.inventory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.util.PlugInHelper;
import org.cougaar.glm.debug.*;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.plan.Agency;
import org.cougaar.glm.plugins.*;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Composition;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.NewComposition;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.util.Enumerator;
import org.cougaar.util.UnaryPredicate;

public abstract class InventoryPlugIn extends GLMDecorationPlugIn {

    public void recordCustomerForTask(Task task){
	recordCustomer(taskConsumerName(task));
    }

   private String taskConsumerName(Task task){
	PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
	if (pp == null) {
	return "unknown consumer";
	}
	Object io = pp.getIndirectObject();
       if (io instanceof String) {
	   return (String)io;
       } else {
	   return "unknown consumer";
       }
    }

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
	boolean maintainAtCapacity;
        public InventoryTypeHashEntry(UnaryPredicate predicate, ProjectionWeight weight) {
            dueOutPredicate = predicate;
	    projectionWeight = weight;
	    fillToCapacity = false;
	    maintainAtCapacity = false;
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
    protected Hashtable inventoryInitHash_ = new Hashtable();

    /** key is type id, value is an Inventory */
    // cleared in initInventories() and added to initInventories and publishInventories
    private Hashtable inventoryHash_ = new Hashtable();
    private Hashtable inventoryTypeHash_ = new Hashtable();
    private Hashtable MILTaskHash_ = new Hashtable();

//      protected CollectionSubscription inventorySubscription_ = null;

    public InventoryPlugIn() {
	super();
//  	setExecutionDelay(30000,30000);
    }

    public synchronized void execute() {
	String clusterId="unitialized clusterId";
	if(myOrganization_!=null){
	    clusterId = myOrganization_.getClusterPG().getClusterIdentifier().toString();
	}
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
		    (InventoryPG) ((Inventory) o).getInventoryPG();
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
	String assetType = getAssetType(inventory);
	if (assetType == null) {
	    GLMDebug.ERROR("InventoryPlugIn","addInventory failed to add "+item);
	}
	else {
	    getInventoryTypeHashEntry(assetType).invBins.add(inventory);
	}
    }

    private void removeInventory(Inventory inventory) {
        removeInventory(inventory, getInventoryType(inventory));
    }

    private void removeInventory(Inventory inventory, String item) {
        inventoryHash_.remove(item);
	String assetType = getAssetType(inventory);
	if (assetType == null) {
	    GLMDebug.ERROR("InventoryPlugIn","removeInventory failed to remove "+item);
	}
	else {
	    getInventoryTypeHashEntry(assetType).invBins.remove(inventory);
	}
    }

    private void addMILTasks(Enumeration milTasks) {
        while (milTasks.hasMoreElements()) {
	    Task task = (Task) milTasks.nextElement();
	    Inventory inventory = (Inventory) task.getDirectObject();
	    MILTaskHash_.put(inventory, task);
	}
    }

    private void removeMILTasks(Enumeration milTasks) {
        boolean clear = false;

        while (milTasks.hasMoreElements()) {
	    Task task = (Task) milTasks.nextElement();
	    Inventory inventory = (Inventory) task.getDirectObject();
	    MILTaskHash_.remove(inventory);
            clear = true;
        }

        // clear the recorded customers if MaintainInventory has been rescinded
        // ????? Should I wait until all mil tasks have been rescinded?
        if (clear) {
            clearRecordedCustomers();
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
	if (id == null) return null;
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

    public boolean getMaintainAtCapacity(String supplyType) {
	InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
        return entry.maintainAtCapacity;
    }

   /**
     * Set the maintainAtCapacity for a class of supply. This becomes
     * the default maintainAtCapacity for new Inventory Assets for the
     * class of supply. The maintainAtCapacity of existing Inventory
     * assets is updated to the new value (true/false). Override this if
     * you don't want this behavior.
     **/
    public void setMaintainAtCapacity(String supplyType, boolean maintain_at_capacity) {
	InventoryTypeHashEntry entry = getInventoryTypeHashEntry(supplyType);
	entry.maintainAtCapacity = maintain_at_capacity;
	Enumeration e = entry.invBins.elements();
	while (e.hasMoreElements()) {
            Inventory inv = (Inventory) e.nextElement();
	    NewInventoryPG invpg = (NewInventoryPG) inv.getInventoryPG();
	    invpg.setMaintainAtCapacity(maintain_at_capacity);
	    
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
	(InventoryPG)inventory.getInventoryPG();
      if (invpg == null ) return null;
      Asset a = invpg.getResource();
      if (a == null) return null;
      SupplyClassPG pg = (SupplyClassPG)a.searchForPropertyGroup(SupplyClassPG.class);
      return pg.getSupplyType();
    }

    public Enumeration getInventoryBins(String assetType) {
        InventoryTypeHashEntry entry = getInventoryTypeHashEntry(assetType);
        return entry.invBins.elements();
    }

    public void initializeInventoryFile(String type) {

	String invFile = getInventoryFile(type);
	Enumeration initialInv = FileUtils.readConfigFile(invFile, getConfigFinder());
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

  private void stashInventoryInformation(String type, Enumeration initInv){
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
