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

import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.debug.GLMDebug;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.plugins.AssetUtils;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.util.UnaryPredicate;


/** Common code for all sourcing processors
  */
public class InventoryProcessor extends org.cougaar.glm.plugins.BasicProcessor {

    protected InventoryPlugin                          inventoryPlugin_;
    protected IncrementalSubscription        supplyTasks_;
    protected IncrementalSubscription        projectionTasks_;
    protected IncrementalSubscription        refillTasks_;
    protected IncrementalSubscription        myProjectionTasks_;
    protected String                         supplyType_;
    protected GeolocLocation                 thisGeoloc_;
    protected long                        oplanStartTime_;
    protected long                        oplanEndTime_;
    protected long                        startTime_;
    protected String                      GEOLOC = "ABC";
    public static NumberFormat demandFormat = NumberFormat.getInstance();

    public InventoryProcessor(InventoryPlugin plugin, org.cougaar.glm.ldm.asset.Organization org, String type )
    {
	super(plugin, org);
	inventoryPlugin_ = plugin;
	supplyType_ = type;

	NewGeolocLocation newGeoloc = GLMFactory.newGeolocLocation();
	newGeoloc.setCountryStateCode("25");
	newGeoloc.setCountryStateName("MASS");
	newGeoloc.setGeolocCode(GEOLOC);
	newGeoloc.setInstallationTypeCode("CTY");
	newGeoloc.setName("CAMBRIDGE");
	newGeoloc.setLatitude(Latitude.newLatitude(42.37, Latitude.DEGREES));
	newGeoloc.setLongitude(Longitude.newLongitude(71.10, Longitude.DEGREES));
	
	thisGeoloc_ = (GeolocLocation)newGeoloc;
	if (org.getMilitaryOrgPG() != null) {
	    GeolocLocation geoloc = (GeolocLocation)org.getMilitaryOrgPG().getHomeLocation();
	    if (geoloc != null) {
		thisGeoloc_ = geoloc;
	    }
	}
	// initialize quantity formater
	demandFormat.setMaximumIntegerDigits(10);
	demandFormat.setMinimumIntegerDigits(2);
	demandFormat.setMinimumFractionDigits(2);
	demandFormat.setMaximumFractionDigits(2);
	demandFormat.setGroupingUsed(false);

//  	printDebug("GeolocLocation : "+thisGeoloc_);	
	initialize();
    }


    static class SupplyTaskPredicate implements UnaryPredicate
    {
	String supplyType_;
	String orgName_;

	public SupplyTaskPredicate(String type, String myOrg_) {
	    supplyType_ = type;
	    orgName_ = myOrg_;
	}

	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task)o;
		if (task.getVerb().equals(Constants.Verb.SUPPLY)) {
		    if (TaskUtils.isDirectObjectOfType(task, supplyType_)) {
			if (!TaskUtils.isMyRefillTask(task, orgName_)) {
			    if (TaskUtils.getQuantity(task) > 0) {
				return true;
			    }
			}
		    }
		}	
	    }
	    return false;
	}
    }

    static class ProjectionTaskPredicate implements UnaryPredicate
    {
	String supplyType_;
	String orgName_;

	public ProjectionTaskPredicate(String type, String orgname) {
	    supplyType_ = type;
	    orgName_ = orgname;
	}

	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task)o;
		if (task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
		    if (TaskUtils.isDirectObjectOfType(task, supplyType_)) {
			if (!TaskUtils.isMyInventoryProjection(task, orgName_)) {
			    return true;
			}
		    }
		}	
	    }
	    return false;
	}
    }


    static class MyProjectionTaskPredicate implements UnaryPredicate
    {
	String supplyType_;
	String orgName_;

	public MyProjectionTaskPredicate(String type, String orgname) {
	    supplyType_ = type;
	    orgName_ = orgname;
	}

	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task)o;
		if (task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
		    if (TaskUtils.isTaskPrepOfType(task, supplyType_)) {
			if (TaskUtils.isMyInventoryProjection(task, orgName_)) {
			    return true;
			}
		    }
		}	
	    }
	    return false;
	}
    }


    static class RefillTaskPredicate implements UnaryPredicate
    {
	String supplyType_;
	String orgName_;

	public RefillTaskPredicate(String type, String myOrg_) {
	    supplyType_ = type;
	    orgName_ = myOrg_;
	}

	public boolean execute(Object o) {
	    if (o instanceof Task ) {
		Task task = (Task)o;
		if (task.getVerb().equals(Constants.Verb.SUPPLY)) {
		    if (TaskUtils.isDirectObjectOfType(task, supplyType_)) {
			if (TaskUtils.isMyRefillTask(task, orgName_)) {
			    if (TaskUtils.getQuantity(task) > 0) {
				return true;
			    }
			}
		    }
		}	
	    }
	    return false;
	}
    }

    /**
     *  Set up subscriptions, 
     *  get the this plugin's organization UIC, and 
     *  initialize the OPLAN object.
     */
    private void initialize()
    {
	// Subscribe to Single supplyType_ Supply Task
        supplyTasks_ = subscribe(new SupplyTaskPredicate(supplyType_, myOrgName_));

	// Subscribe to Single supplyType_ ProjectSupply Task
        projectionTasks_ = subscribe(new ProjectionTaskPredicate(supplyType_, myOrgName_));

	// Subscribe to Refill tasks for local inventory
	refillTasks_ = subscribe(new RefillTaskPredicate(supplyType_, myOrgName_));

	// Subscribe to Projection tasks created by this Organization
	myProjectionTasks_ = subscribe(new MyProjectionTaskPredicate(supplyType_, myOrgName_));

    }
 
    /** overwriting GLMDebug messages */
    public void printDebug(String msg) {
	GLMDebug.DEBUG(className_, clusterId_, msg);
    }

    // p - priorty,  msg - debug message
    public void printDebug(int p, String msg) {
	GLMDebug.DEBUG(className_, clusterId_, msg, p);
    }

    public void printError(String msg) {
	GLMDebug.ERROR(className_, clusterId_, msg);
    }
 
    public void printLog(String msg) {
	GLMDebug.LOG(className_, clusterId_, msg);
    }
  
    public void update() {
//  	long alp_time = getAlpTime();
	startTime_ = getAlpTime();
//          Collection oplans = ((GLMDecorationPlugin)inventoryPlugin_).getOPlans();
//          if (oplans.isEmpty()) {
//              oplanStartTime_ = alp_time;
//  	    oplanEndTime_ = TimeUtils.addNDays(oplanStartTime_, 180);
//  	} else {
//              oplanStartTime_ = TimeSpan.MAX_VALUE;
//              oplanEndTime_ = TimeSpan.MIN_VALUE;
//              for (Iterator i = oplans.iterator(); i.hasNext(); ) {
//                  ClusterOPlan oplan = (ClusterOPlan) i.next();
//                  oplanStartTime_ = Math.min(oplanStartTime_, oplan.getStartTime());
//                  oplanEndTime_ = Math.max(oplanEndTime_, oplan.getEndTime());
//              }
//  	}

//  	if (alp_time < oplanStartTime_) {
//  	    // normalize to a day boundary!
//  	    startTime_ = TimeUtils.addNDays(oplanStartTime_, 0);
//  	} else {
//  	    startTime_ = TimeUtils.addNDays(alp_time, 0);
//  	}
    }

    protected void publishChangeProjection(Inventory inventory, Enumeration tasks) {
	if (!tasks.hasMoreElements()) {
	    return;
	}
	Task parent_task = inventoryPlugin_.findOrMakeMILTask(inventory);
	Schedule published_schedule = createProjectionSchedule(parent_task);
	Schedule newtask_schedule  = newObjectSchedule(tasks);
	Enumeration tasks_to_publish = diffProjections(published_schedule, newtask_schedule);
	Task task;
	while (tasks_to_publish.hasMoreElements()) {
	    task = (Task)tasks_to_publish.nextElement();
	    plugin_.publishAddToExpansion(parent_task, task);
//  	    printDebug("publishChangeProjection(), Publishing new Projections: "+
//  		       TaskUtils.projectionDesc(task));
	}

    }

    protected Schedule createProjectionSchedule(Task maintainInv) {
	Task task;
	Vector published_tasks = new Vector();
	PlanElement pe = maintainInv.getPlanElement();
	Date now = new Date(getAlpTime());
	if ((pe != null) && (pe instanceof Expansion)) {
	    Expansion expansion;
	    expansion = (Expansion)pe;
	    Workflow wf = expansion.getWorkflow();
	    Enumeration tasks = wf.getTasks();
	    while (tasks.hasMoreElements()) {
		task = (Task)tasks.nextElement();
		if (task.getVerb().equals(Constants.Verb.PROJECTSUPPLY) && task.beforeCommitment(now)) {
		    published_tasks.add(task);
		}
	    }
	}
	return newObjectSchedule(published_tasks.elements());
    }

    public long roundTimeUp(long time) {
	long num_days = (long)Math.ceil(time/MSEC_PER_DAY);
	return num_days*MSEC_PER_DAY;
    }

    public long roundTimeDown(long time) {
	long num_days = (long)Math.floor(time/MSEC_PER_DAY);
	return num_days*MSEC_PER_DAY;
    }

    public Preference createTransportStartPref(long start) {
	AspectValue startAV = AspectValue.newAspectValue(AspectType.START_TIME, start);
	ScoringFunction startSF = ScoringFunction.createNearOrAbove(startAV, 0);
	return ldmFactory_.newPreference(AspectType.START_TIME, startSF);
    }

    public Preference createTransportEndPref(long end) {
	AspectValue endAV = AspectValue.newAspectValue(AspectType.END_TIME, end);
	ScoringFunction endSF = ScoringFunction.createNearOrBelow(endAV, 0);
	return ldmFactory_.newPreference(AspectType.END_TIME, endSF);
    }

    // The convenience function createStrategicTransportEndPreference only
    // uses required delivery date to generate early, best and late as specified by TOPS
    public Preference createStrategicTransportEndPref(long rdd) {
	// 3 day intervals are specified by TOPS
	long best = rdd;
	long end = best+(3*MSEC_PER_DAY);
	long early = best-(3*MSEC_PER_DAY);
	AspectValue lateEndAV = AspectValue.newAspectValue(AspectType.END_TIME, end);
	AspectValue bestEndAV = AspectValue.newAspectValue(AspectType.END_TIME, best);
	AspectValue earlyEndAV = AspectValue.newAspectValue(AspectType.END_TIME, early);
	ScoringFunction endSF = ScoringFunction.createStrictlyBetweenWithBestValues(earlyEndAV, bestEndAV, lateEndAV);
	return ldmFactory_.newPreference(AspectType.END_TIME, endSF);
    }

    // The convenience function createStrategicTransportStartPreference only
    // uses required delivery date to generate early, best and late as specified by TOPS
    public Preference createStrategicTransportStartPref(long rdd) {
	// 3 and 10 day intervals are specified by TOPS
	long best = rdd-(10*MSEC_PER_DAY);
//  	long end = best+(3*MSEC_PER_DAY);
//  	long early = best-(3*MSEC_PER_DAY);
//  	AspectValue lateEndAV = AspectValue.newAspectValue(AspectType.START_TIME, end);
	AspectValue bestEndAV = AspectValue.newAspectValue(AspectType.START_TIME, best);
//  	AspectValue earlyEndAV = AspectValue.newAspectValue(AspectType.START_TIME, early);
//  	ScoringFunction startSF = ScoringFunction.createStrictlyBetweenWithBestValues(earlyEndAV, bestEndAV, lateEndAV);
	ScoringFunction startSF = ScoringFunction.createNearOrAbove(bestEndAV, 0);
	return ldmFactory_.newPreference(AspectType.START_TIME, startSF);
    }

    // convert COUGAAR time to date since start of TPFDD
//     public int cDay(long start_time) {
// 	return (int)((start_time/MSEC_PER_DAY) - startDay_);
//     }

    // print methods
    public String inventoryDesc(Inventory inv) {
	String output = "inv: ";
	ScheduledContentPG scpg = inv.getScheduledContentPG();
	TypeIdentificationPG tipg = scpg.getAsset().getTypeIdentificationPG();
	if (tipg != null) 
	    output += AssetUtils.assetDesc(scpg.getAsset());
//  	double capacity = inv.getContainPG().getMaximumVolume().getGallons();
	// 	return output+" cap:"+capacity;
	return output;
    }
    
    public String getInventoryType(Inventory inventory) {
	ScheduledContentPG scp = inventory.getScheduledContentPG();
	Asset proto = scp.getAsset();
	if (proto == null) {
	    printError("getInventoryType failed to get asset for "+inventory);
	    return "";
	}
	return proto.getTypeIdentificationPG().getTypeIdentification();
    }

    public void printInventory(Inventory bin, Enumeration dueIns) {
	printInventory(bin,dueIns,0);
    }

    public void printInventory(Inventory bin, Enumeration dueIns, int priority) {
	if (GLMDebug.printMessages(priority) ) {
 	    synchronized(plugin_){
		printInventoryLevels(bin,priority);
		printInventoryDueOuts(bin,priority);
		printInventoryDueIns(bin,dueIns,priority);
 	    }
	}
    }

    public void printInventoryLevels(Inventory bin, int priority) {
	Asset asset = getInventoryAsset(bin);
	if(asset==null){
	    printError("printInventoryLevels: "+bin+" has no asset");
	    return;
	}

	ScheduledContentPG scp = bin.getScheduledContentPG();
	Schedule sched = scp.getSchedule();
	String nsn;
	if (sched == null) {
	    printError("printInventoryLevels()  null sched for bin:"+bin);
	    return;
	}

	printDebug(priority,"\n\n----------------------------------------------------------------\n");
	printDebug(priority,"Inventory for: "+AssetUtils.assetDesc(asset)+ " bin: "+bin.getUID());
	Enumeration elements = sched.getAllScheduleElements();
	QuantityScheduleElement qse;

	while (elements.hasMoreElements()) {
	    qse = (QuantityScheduleElement)elements.nextElement();
	    printDebug(priority,"    qty: "+demandFormat.format(qse.getQuantity())+
		       " "+TimeUtils.dateString(qse.getStartTime())+" to "+
		       TimeUtils.dateString(qse.getEndTime()));
	}
    }

    public void printInventoryDueOuts(Inventory bin, int priority){
	Asset asset = getInventoryAsset(bin);
	printDebug(priority,"Due-outs for "+AssetUtils.assetDesc(asset)+":");
	Enumeration role_sched = bin.getRoleSchedule().getRoleScheduleElements();
	while (role_sched.hasMoreElements()) {
	    PlanElement pe = (PlanElement)role_sched.nextElement();
	    AllocationResult ar =pe.getEstimatedResult();
	    if(ar==null){
		printError("printInventoryDueOuts: allocation without estimated result: "+
			   pe.getUID());
	    }
	    AllocationResult rar =pe.getReportedResult();
	    printDueOut("-",pe.getTask(),(Allocation)pe,ar,rar,priority);
	}
    }

    public void printInventoryDueIns(Inventory bin, Enumeration dueIns,int priority) {
	if(dueIns!=null){
	    Asset asset = getInventoryAsset(bin);
	    String nsn = asset.getTypeIdentificationPG().getTypeIdentification();
	    printDebug(priority,"Due-ins for "+AssetUtils.assetDesc(asset)+":");
	    while (dueIns.hasMoreElements()) {
		PlanElement pe = (PlanElement)dueIns.nextElement();
		if(pe!=null){
		    AllocationResult ar =pe.getEstimatedResult();
		    if(ar==null){
			printError("printInventoryDueIns:allocation without estimated result: "+
				   pe.getUID());
		    }
		    Task task = pe.getTask();
		    String refilling = task.getDirectObject().getTypeIdentificationPG().getTypeIdentification();
		    if (refilling.equals(nsn)){
			AllocationResult rar =pe.getReportedResult();
			printDueOut("+",task,(Allocation)pe,ar,rar,priority);
		    }
		}
	    }
	}
    }

    public void printDueOut(String direction, Task task, Allocation pe, 
				   AllocationResult ar, AllocationResult reportedAr) {
	printDueOut(direction,task,pe,ar,reportedAr,1);
    }

    public void printDueOut(String direction, Task task, Allocation pe, AllocationResult ar,
		     AllocationResult reportedAr, int priority) {
	String successFlg;
	if (reportedAr==null) {
	    // this is for the printouts below
	    reportedAr=ar;
	    successFlg="";
	} else if (reportedAr.isSuccess()){
	    successFlg="--OK";
	} else {
	    successFlg="--FAIL";
	}

	// initialize quantity formater
	demandFormat.setMaximumIntegerDigits(10);
	demandFormat.setMinimumIntegerDigits(2);
	demandFormat.setMinimumFractionDigits(2);
	demandFormat.setMaximumFractionDigits(2);
	demandFormat.setGroupingUsed(false);

	if(direction.equals("-")){
	    printDebug(priority, arTime(reportedAr)+" "+
		       direction+demandFormat.format(reportedAr.getValue(AspectType.QUANTITY))+
		       " "+pe.getUID()+" "+arDesc(reportedAr)+
		       " Task: "+TaskUtils.shortTaskDesc(pe.getTask().getWorkflow().getParentTask())+successFlg);
	} else {
	    printDebug(priority,arTime(reportedAr)+" "+direction+
		       demandFormat.format(reportedAr.getValue(AspectType.QUANTITY))+
		       " "+pe.getUID()+" "+arDesc(reportedAr)+
		       " Task: "+TaskUtils.shortTaskDesc(task)+successFlg);
	}
    }

    public static long arTime(AllocationResult ar) {
	AspectValue[] values = ar.getAspectValueResults();
	long start_time = -1;
	long end_time = -1;
	for (int i = 0; i < values.length; i++) {
	    if (values[i].getAspectType() == AspectType.START_TIME) {
		AspectValue sdav = values[i];
		if (sdav instanceof TimeAspectValue)
		    start_time = ((TimeAspectValue)values[i]).longValue();
		else
		    start_time = sdav.longValue();
	    }
	    if (values[i].getAspectType() == AspectType.END_TIME) {
		AspectValue sdav = values[i];
		if (sdav instanceof TimeAspectValue)
		    end_time = ((TimeAspectValue)values[i]).longValue();
		else
		    end_time = sdav.longValue();
	    }
	}
	if(end_time < 0 ) {
	    return 0;
	}
	return end_time/100000;
    }

    public static String arDates(AllocationResult ar) {
	AspectValue[] values = ar.getAspectValueResults();
	long start_time = -1;
	long end_time = -1;
	for (int i = 0; i < values.length; i++) {
	    if (values[i].getAspectType() == AspectType.START_TIME) {
		AspectValue sdav = values[i];
		if (sdav instanceof TimeAspectValue)
		    start_time = ((TimeAspectValue)values[i]).longValue();
		else
		    start_time = sdav.longValue();
	    }
	    if (values[i].getAspectType() == AspectType.END_TIME) {
		AspectValue sdav = values[i];
		if (sdav instanceof TimeAspectValue)
		    end_time = ((TimeAspectValue)values[i]).longValue();
		else
		    end_time = sdav.longValue();
	    }
	}
	//       String res = " On: ";

	String res = " ";
	if(start_time >= 0) {
	    res = res+TimeUtils.dateString(start_time)+";";
	} else {
	    res = res+"UNKNOWN;";
	}
	if(end_time >= 0) {
	    res = res+" To: "+TimeUtils.dateString(end_time)+";";
	} else {
	    res = res+"To: UNKNOWN;";
	}
	return res;
    }

    // ********************************************************
    //                                                        *
    // INITIALIZATION Section                                 *
    //                                                        *
    // ********************************************************


    // ********************************************************
    //                                                        *
    // Utility        Section                                 *
    //                                                        *
    // ********************************************************

    public Asset getInventoryAsset(Inventory inventory) {
	ScheduledContentPG scp = inventory.getScheduledContentPG();
	return scp.getAsset();
    }

}
