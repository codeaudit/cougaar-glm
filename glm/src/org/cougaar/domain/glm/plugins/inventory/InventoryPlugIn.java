package org.cougaar.domain.glm.plugins.inventory;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.util.UnaryPredicate;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.plan.Agency;

public abstract class InventoryPlugIn extends GLMDecorationPlugIn {

  public static class InventoryItemInfo {
    public double[] levels;
    public GregorianCalendar reportBase;
    public int reportStepKind;

    InventoryItemInfo(double[] levels, GregorianCalendar reportBase, int reportStepKind) {
      this.levels = levels;
      this.reportBase = reportBase;
      this.reportStepKind = reportStepKind;
    }
  }

    /** Subscription for expandable support requests. */
    private IncrementalSubscription        detReqSubscription_;
    private IncrementalSubscription        MaintainInventorySubscription_;

    // Hashtable - keys asset id (nsn), elements array of levels 
    // filled in from file during initialization.  Used to create inventories as needed.
    public Hashtable inventoryInitHash_ = new Hashtable();

    /** key is type id, value is an Inventory */
    // cleared in initInventories() and added to initInventories and publishInventories
    private Hashtable inventoryHash_ = new Hashtable();
    private Hashtable inventoryTypeHash_ = new Hashtable();
    private Hashtable MILTaskHash_ = new Hashtable();

    protected RootFactory                  ldmFactory_;

    protected IncrementalSubscription inventorySubscription_ = null;

    public InventoryPlugIn() {
	super();
    }

    public synchronized void execute() {
	initInventories();
	rememberMaintainInventoryTasks();
	super.execute();
    }

    protected void setupSubscriptions() {
	super.setupSubscriptions();
	ldmFactory_ = getDelegate().getLDM().getFactory();
	inventorySubscription_ = (IncrementalSubscription) subscribe(new InventoryPredicate());
	// Determing requirements task subscription 
	detReqSubscription_ = (IncrementalSubscription)subscribe(new DetInvReqPredicate());
	MaintainInventorySubscription_ = (IncrementalSubscription)subscribe(new MaintainInventoryPredicate());
    }

    static class InventoryPredicate implements UnaryPredicate
    {
	String type_;

	public InventoryPredicate() {}

	public boolean execute(Object o) {
	    if (o instanceof Inventory) {
		InventoryPG invpg = 
		    (InventoryPG)((Inventory)o).searchForPropertyGroup(InventoryPG.class);
		if (invpg != null) {
		    return true;    
		}
	    }
	    return false;
	}
    };

    private void initInventories() {
	inventoryHash_.clear();
	inventoryTypeHash_.clear();
	Enumeration list = inventorySubscription_.elements();
	Inventory inventory;
	String item;
	while (list.hasMoreElements()) {
            // get current inventories with current qty
            inventory = (Inventory)list.nextElement();
	    item = getInventoryType(inventory);
	    inventoryHash_.put(item, inventory);
	}
    }

    private void rememberMaintainInventoryTasks() {
	MILTaskHash_.clear();
	Enumeration e = MaintainInventorySubscription_.elements();
	Task task;
	Inventory inventory;
	while (e.hasMoreElements()) {
	    task = (Task)e.nextElement();
	    inventory = (Inventory)task.getDirectObject();
	    MILTaskHash_.put(inventory, task);
	}
    }

    public Task getDetermineRequirementsTask() {
	Task milTask;
	if (detReqSubscription_.isEmpty()) {
	    milTask = null;
// 	    GLMDebug.DEBUG("InventoryPlugIn", getClusterIdentifier(), 
// 			      "getDetermineRequirementsTask(), NO DETERMINE REQUIREMENTS FOR INVENTORY TASK");
	}
	else {
	    Enumeration e = detReqSubscription_.elements();
	    milTask = (Task)e.nextElement();
// 	    GLMDebug.DEBUG("InventoryPlugIn", getClusterIdentifier(), 
// 			      "getDetermineRequirementsTask(), Task is  "+TaskUtils.taskDesc(milTask));
	}
	return milTask;
    }

    public Task findOrMakeMILTask(Inventory inventory) {
	// Creates the MaintainInventoryLevels Task for this item 
	// if one does not already exist
	NewTask milTask = (NewTask)MILTaskHash_.get(inventory);
	if (milTask == null) {
	    // Get Parent
	    Task parent = getDetermineRequirementsTask();
	    ScheduledContentPG scpg = inventory.getScheduledContentPG();
	    if (parent != null) {
		milTask = createMILTask(parent, inventory);
		publishAddToExpansion(parent, milTask);
		GLMDebug.DEBUG("InventoryPlugIn", "findOrMakeMILTask(), Created MaintainInventoryTask for "+
				  AssetUtils.assetDesc(scpg.getAsset()));
		MILTaskHash_.put(inventory, milTask);
	    }
// 	    else {
// 		GLMDebug.ERROR("InventoryPlugIn", "findOrMakeMILTask(), CANNOT CREATE MILTASK, no parent, inventory: "+
// 				  AssetUtils.assetDesc(scpg.getAsset()));	
// 	    }
	}
	return milTask;
    }


    public synchronized Inventory findOrMakeInventory(String supplytype, String id) {
	Asset resource = ldmFactory_.getPrototype(id);
	if (resource == null) {
	    GLMDebug.DEBUG("InventoryPlugIn", "<"+supplytype+"> createInventory fail to get prototype for "+id);
	    resource = ldmFactory_.createPrototype(supplytype, id);
	}
	if (resource == null) {
	    GLMDebug.ERROR("InventoryPlugIn","<"+supplytype+"> createInventory fail to make prototype for "+id);
	    return null;
	}
	return findOrMakeInventory(supplytype, resource);
    }

    public synchronized Inventory findOrMakeInventory(String supplytype, Asset resource) {
	Inventory inventory = null;
	// only need to sync if each processor is running on its own thread.
	// currently, ANTS is single-threaded but could easily be made multi-threaded.
	String id = resource.getTypeIdentificationPG().getTypeIdentification();
	inventory = (Inventory)inventoryHash_.get(id);
	if (inventory == null) {
	    inventory = createInventory(supplytype, resource);
	    if (inventory != null) {
		inventoryHash_.put(id, inventory);
		getDelegate().publishAdd(inventory);
		GLMDebug.DEBUG("InventoryPlugIn", "findOrMakeInventory(), CREATED inventory bin for: "+
				  AssetUtils.assetDesc(inventory.getScheduledContentPG().getAsset()));
		findOrMakeMILTask(inventory);
	    }
	}
	return inventory;
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
	
	Vector invBinsOfType;
	// If the inventory bins of this type have not already been collected,
	// sort out the Inventory assets of the given type and save the vector.
	if ((invBinsOfType = (Vector)inventoryTypeHash_.get(assetType))==null) {
	    invBinsOfType = new Vector();
	    Enumeration list = inventoryHash_.elements();
	    Inventory inv;
	    while (list.hasMoreElements()) {
		inv = (Inventory) list.nextElement();
		if (assetType.equals(getAssetType(inv))) {
		    invBinsOfType.add(inv);
		}
	    }
	    inventoryTypeHash_.put(assetType, invBinsOfType);
	}
	return invBinsOfType.elements();
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

    // ********************************************************
    //                                                        *
    // INITIALIZATION Section                                 *
    //                                                        *
    // ********************************************************

    static class DetInvReqPredicate implements UnaryPredicate
    {

	// Predicate defining expandable Determine Reqs. 
	public boolean execute(Object o) {
	    if (o instanceof Task) {
		Task t = (Task)o;
		if (t.getVerb().equals(Constants.Verb.DETERMINEREQUIREMENTS)) {
		    return TaskUtils.isTaskOfType(t, "MaintainInventory");
		}
	    }
	    return false;
	}
    }

    static class MaintainInventoryPredicate implements UnaryPredicate
    {

	// Predicate defining expandable Determine Reqs. 
	public boolean execute(Object o) {
	    if (o instanceof Task) {
		Task t = (Task)o;
		if (t.getVerb().equals(Constants.Verb.MAINTAININVENTORY)) {
		    return true;
		}
	    }
	    return false;
	}
    }

    private NewTask createMILTask(Task parent, Inventory inventory) {
	NewTask subtask = getMyDelegate().getFactory().newTask();
	subtask.setDirectObject(inventory);
	subtask.setParentTask(parent);
	subtask.setSource(getMyDelegate().getClusterIdentifier());
	subtask.setVerb(new Verb(Constants.Verb.MAINTAININVENTORY));
	return subtask;
    }
}
