/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class GSSchedulerResult {
  public GSSchedulerResult (GSScheduler sched) {
    myScheduler = sched;
    aligned   = (myScheduler.getSpecs ().taskGroupingMode() == 
		 GSSchedulingSpecs.ALIGNEDGROUPING);
    unaligned = (myScheduler.getSpecs ().taskGroupingMode() == 
		 GSSchedulingSpecs.UNALIGNEDGROUPING);
    myHashMap = new HashMap ();
    myUnhandled = null;
  }

  public boolean getAligned () { return aligned; }
  public boolean getUnaligned () { return unaligned; }
  public void setUnhandledTasks (List unallocated) { myUnhandled = unallocated; }

  /**
   * 
   * @return list of unhandled tasks
   */
  public List getUnhandledTasks () {
    return myUnhandled;
  }

  public void clearUnhandledTasks () {
    myUnhandled = new ArrayList ();
  }

  public void putTaskGroups (Asset anAsset, List groups) { 
    myHashMap.put (anAsset, groups); 
  }

  public List getTaskGroups (Asset anAsset) {
    return (List) myHashMap.get (anAsset); 
  }

  public boolean hasAnyLeftoverTasks () {
    for (Iterator i = myHashMap.values ().iterator ();
	 i.hasNext ();) {
      List groups = (List) i.next ();
      for (Iterator l = groups.iterator (); l.hasNext ();) {
	GSTaskGroup tg = (GSTaskGroup) l.next ();
	if (!tg.getTasks ().isEmpty () && !tg.isFrozen ())
	  return true;
      }
    }
    return false;
  }

  public int numLeftoverTasks () {
    int total = 0;

    for (Iterator i = myHashMap.values ().iterator ();
	 i.hasNext ();) {
      List groups = (List) i.next ();
      for (Iterator l = groups.iterator (); l.hasNext ();) {
	GSTaskGroup tg = (GSTaskGroup) l.next ();
	total += tg.getTasks ().size ();
      }
    }
    
    return total;
  }

  /**
   * Does not contain GSScheduler.UNALLOCATEDKEY
   * @return set of assets used in the schedule
   * @see GSScheduler#UNALLOCATEDKEY
   */
  public List getAssets () {
    Map copy = new HashMap (myHashMap);
    List assetList = new ArrayList (copy.keySet ());
    if (myScheduler.getSpecs().wantsOrderedAssets()) {
      assetList = myScheduler.getSpecs().orderAssets(assetList);
    }
    if (assetList.contains (GSScheduler.UNALLOCATEDKEY))
      assetList.remove (GSScheduler.UNALLOCATEDKEY);
    return assetList;
  }


  public void removeTaskGroupForAsset (Asset anAsset, GSTaskGroup tg) {
    getTaskGroups (anAsset).remove (tg);
  }

  /** 
   * test to use before calling removeTaskGroupForAsset 
   * Used by UTILGSSAggregatorPlugIn to know when to let scheduler
   * forget about a task group.
   * 
   * This is part of the problem where there can be a time gap between
   * the aggregation of tasks and their allocation.  This is the case
   * since this is performed by different plugins.
   */
  public boolean hasTaskGroup (Asset anAsset, GSTaskGroup tg) {
    return ((myHashMap.get (anAsset) != null) &&
	    getTaskGroups (anAsset).contains (tg));
  }

  public void addAssetList (List newAssets) {
    List legalAssets = 
      myScheduler.getSpecs ().initialize (new Vector(newAssets));
    for (Iterator iter = legalAssets.iterator ();
	 iter.hasNext ();)
      myHashMap.put (iter.next (), new ArrayList ());
  }

  /** 
   * problematic -- what if before disposition?  What do we do with the
   * currently associated tasks?
   */
  //  public void removeAsset (Asset asset) {xxx.remove (asset); }

  /** 
   * Debugging
   */
  public String toString() {
    String s = "-----Current Scheduler Result-----\n";
    s += "-----------Assignments-----------\n";
    List as = getAssets();
    Iterator as_i = as.iterator();
    while (as_i.hasNext()) {
      System.err.println("Getting next asset");
      Asset a = (Asset)as_i.next();
      s += ">>>>Asset :" + a.getUID() + ":\n";
      List tgs = getTaskGroups(a);
      Iterator tgs_i = tgs.iterator();
      while (tgs_i.hasNext()) {
      System.err.println("Getting next taskgroup");
	s += "]]]]]Task :" + (GSTaskGroup)tgs_i.next() + ":\n";
      }
    }

    s += "---------Unhandled Tasks---------\n";
    List uts = getUnhandledTasks();
    Iterator uts_i = uts.iterator();
    while (uts_i.hasNext()) {
      System.err.println("Getting next task");
      s += (GSTaskGroup)uts_i.next() + "\n";
    }

    return s;
  }

    public void printUsedCapacities(){
	List assets = getAssets();
	Iterator a_i = assets.iterator();
	System.out.println("**** Results has " + myUnhandled.size() + " unhandled tasks.");
	while(a_i.hasNext()){
	    Asset a = (Asset)a_i.next();
	    List gstaskgroups = getTaskGroups(a);
	    Iterator gstg_i = gstaskgroups.iterator();
	    int groupNumber = 0;
	    while(gstg_i.hasNext()){
		groupNumber++;
		GSTaskGroup gstg = (GSTaskGroup)gstg_i.next();
		System.out.println("*** Capacities for " + a + " Task group " + groupNumber +
				   (gstg.isFrozen()?"F":"") + " @ " + gstg.getTasks().size() + " tasks");
		double levels[] = gstg.getCapacityLevels ();
		for(int i=0;i<levels.length;i++){
		    String capName = getCapacityConstraintName(i);
		    String capUnits = getCapacityConstraintUnits(i);
		    String totalCap = "Undefined"; // getTotalCapByName(a, capName);
		    System.out.println("** " + capName + " in " + capUnits + " @ " + levels[i] + " of " + totalCap);
		}
	    }
	}
    }

//     public String getTotalCapByName(Asset a, String capName){
// 	if(a == null)
// 	    return "Null Asset";
// 	DeckPG dpg = (DeckPG) a.searchForPropertyGroup(DeckPG.class);
// 	if(dpg != null){
// 	    if(capName.equals("MaximumWeight")){
// 		if(dpg.getMaximumWeight() != null)
// 		    return dpg.getMaximumWeight().getTons() + " in tons";
// 		else
// 		    return "Deck does not have weight";
// 	    }else if(capName.equals("MaximumFootprintArea")){
// 		if(dpg.getMaximumFootprintArea()!=null)
// 		    return dpg.getMaximumFootprintArea().getSquareFeet() + " in square feet";
// 		else
// 		    return "Deck does not have area";
// 	    }else if(capName.equals("MaximumVolume")){
// 		if(dpg.getMaximumVolume() != null)
// 		    return dpg.getMaximumVolume().getCubicFeet() + " in cubic feet";
// 		else
// 		    return "Deck does not have volume";
// 	    }else
// 		return "Unknown capName: " + capName;
// 	}
// 	ContainPG cpg = (ContainPG) a.searchForPropertyGroup(ContainPG.class);
// 	if (cpg != null){ 
// 	    if(capName.equals("MaximumWeight")){
// 		if(cpg.getMaximumWeight() != null)
// 		    return cpg.getMaximumWeight().getTons() + " in tons";
// 		else
// 		    return "Container does not have weight";
// 	    }else if(capName.equals("MaximumFootprintArea")){
// 		if(cpg.getMaximumFootprintArea() != null)
// 		    return cpg.getMaximumFootprintArea().getSquareFeet() + " in square feet";
// 		else
// 		    return "Container does not have area";
// 	    }else if(capName.equals("MaximumVolume")){
// 		if(cpg.getMaximumVolume() != null)
// 		    return cpg.getMaximumVolume().getCubicFeet() + " in cubic feet";
// 		else
// 		    return "Container does not have volume";
// 	    }else
// 		return "Unknown capName: " + capName;
// 	}
// 	return "No PGs";
//     }

    public String getCapacityConstraintName(int i){
	List capacityConstraints = myScheduler.getSpecs().getCapacityConstraints();
	GSCapacityConstraint cc = (GSCapacityConstraint)capacityConstraints.get(i);
	return ((GSAssetAccessorImpl)cc.getAssetAccessor()).getPropertyField();
    }
    public String getCapacityConstraintUnits(int i){
	List capacityConstraints = myScheduler.getSpecs().getCapacityConstraints();
	GSCapacityConstraint cc = (GSCapacityConstraint)capacityConstraints.get(i);
	return ((GSAssetAccessorImpl)cc.getAssetAccessor()).getUnits();
    }

  protected Map myHashMap = null;
  protected GSScheduler myScheduler;
  protected boolean aligned;
  protected boolean unaligned;
  protected List myUnhandled;
}
