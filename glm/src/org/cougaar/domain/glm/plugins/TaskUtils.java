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
import org.cougaar.core.plugin.util.PlugInHelper;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.LdmFactory;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.measure.Rate;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.plan.AlpineAspectType;
import org.cougaar.domain.glm.ldm.asset.SupplyClassPG;
import org.cougaar.domain.glm.debug.GLMDebug;

/** Provides convenience methods. */
public class TaskUtils extends PlugInHelper {
    /** number of msec per day */
    // 86400000 msec/day = 1000msec/sec * 60sec/min *60min/hr * 24 hr/day
    public static final long MSEC_PER_DAY =  86400000;
    public static final NumberFormat demandFormat_ = getDemandFormat();

    // TASK utils

    public static NumberFormat getDemandFormat() {
	NumberFormat demandFormat = NumberFormat.getInstance();
	demandFormat.setMaximumIntegerDigits(10);
	demandFormat.setMinimumIntegerDigits(2);
	demandFormat.setMinimumFractionDigits(2);
	demandFormat.setMaximumFractionDigits(2);
	demandFormat.setGroupingUsed(false);
	return demandFormat;
    }

    /** @param task
     *  @param type type identification string
     *  @return true if the task's OFTYPE preposition's indirect object is 
     *  an asset with nomeclature equal to 'type'.*/
    public static boolean isTaskOfType(Task t, String type) {
	PrepositionalPhrase pp =t.getPrepositionalPhrase(Constants.Preposition.OFTYPE) ;
	if (pp != null) {
	    Object obj = pp.getIndirectObject();
	    if (obj instanceof Asset) {
		Asset a = (Asset)obj;
		return a.getTypeIdentificationPG().getTypeIdentification().equals(type);
	    }
	}
	return false;
    }

    public static String getNameOfConsumedItem(Task task) {
	Asset asset = task.getDirectObject();
	// Check for aggregate assets and grab the prototype
	if (asset instanceof Asset) {
	    return asset.getTypeIdentificationPG().getTypeIdentification();
	} else {
	    return "Unknown";
	}
    }

    public static boolean isTaskPrepOfType(Task t, String type) {
	PrepositionalPhrase pp = t.getPrepositionalPhrase(Constants.Preposition.OFTYPE);
	if (pp != null) {
	    Object obj = pp.getIndirectObject();
	    if (obj instanceof String) {
		String s = (String)obj;
		return s.equals(type);
	    }
	}
	return false;
    }

    public static boolean isDirectObjectOfType(Task t, String type) {
        boolean result = false;
	Asset asset = t.getDirectObject();
	// Check for aggregate assets and grab the prototype
	if (asset instanceof AggregateAsset) {
	    asset = ((AggregateAsset)asset).getAsset();
	}
	try {
	    SupplyClassPG pg = (SupplyClassPG)asset.searchForPropertyGroup(SupplyClassPG.class);
	    if (pg != null) {
		result = type.equals(pg.getSupplyType());
	    }
	    else {
		GLMDebug.DEBUG("TaskUtils", "No SupplyClassPG found on asset "+TaskUtils.taskDesc(t));
	    }
	} catch (Exception e) {
	    GLMDebug.ERROR("TaskUtils", "Tasks DO is null "+TaskUtils.taskDesc(t)+"\n"+e);
	}
	return result;
    }

    public static boolean isMyRefillTask(Task task, String myOrgName) {
	PrepositionalPhrase pp =task.getPrepositionalPhrase(Constants.Preposition.REFILL);
	if (pp == null) {
	    return false;
	}
	pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
	if (pp == null) {
	    return false;
	}
	Object io = pp.getIndirectObject();
       if (io instanceof String) {
	    String orgName = (String)io;
	    if ( orgName.equals(myOrgName)) {
		return true;
 	    }
	}
	return false;
    }

    public static boolean isMyInventoryProjection(Task task, String myOrgName) {
	PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
	if (pp == null) {
	return false;
	}
	Object io = pp.getIndirectObject();
       if (io instanceof String) {
	    String orgName = (String)io;
	    if ( orgName.equals(myOrgName)) {
		pp = task.getPrepositionalPhrase(Constants.Preposition.MAINTAINING);
		if (AssetUtils.isAssetOfType((Asset)pp.getIndirectObject(), "Inventory")) {
		    return true;
		}
 	    }
	}
	return false;
    }

    public static String taskDesc(Task task) {
	return "[ " +task.getUID() +  "  " +
 	    PublicationKey.getTotalTaskKey(task)
//  	    +" "+
//    	    task.getVerb()+"("+
//  	    demandFormat_.format(TaskUtils.getQuantity(task))+" "+
//  	    getTaskItemName(task)+") "+
//  	    TimeUtils.dateString(TaskUtils.getEndTime(task))+"]"
	    ;
    }


    public static String shortTaskDesc(Task task) {
	return task.getUID() + "["+
	    demandFormat_.format(TaskUtils.getQuantity(task))+"; "+
	    TimeUtils.dateString(TaskUtils.getEndTime(task))+"]";
    }

    public static String projectionDesc(Task task) {
	return task.getUID()+"[ Start:"+TimeUtils.dateString(TaskUtils.getStartTime(task))+
	    " End:"+TimeUtils.dateString(TaskUtils.getEndTime(task))+
	    " Rate:"+TaskUtils.getRate(task)+" ]";
    }

    public static String getTaskItemName(Task task){
	Asset prototype = (Asset)task.getDirectObject();
	if (prototype == null) return "null";
	return AssetUtils.assetDesc(prototype);
    }

    public static String getDirectObjectID(Task task) {
	return task.getDirectObject().getTypeIdentificationPG().getTypeIdentification();
    }

    // true if ==
    public static boolean comparePreferences(Task a, Task b) {
	Preference p;
	int at;
	AspectScorePoint asp;
	Enumeration ae = a.getPreferences();
	while (ae.hasMoreElements()) {
	    p = (Preference)ae.nextElement();
	    at = p.getAspectType();
	    if ( p.getScoringFunction().getBest().getValue() != getPreferenceBestValue(b,at)) {
// 		GLMDebug.DEBUG("TaskUtils","compare AspectType:"+at+" a:"+p.getScoringFunction().getBest().getValue()+" b:"+getPreferenceBestValue(b,at));
		return false;
	    }
	}
	Enumeration be = b.getPreferences();
	while (be.hasMoreElements()) {
	    p = (Preference)be.nextElement();
	    at = p.getAspectType();
	    if ( p.getScoringFunction().getBest().getValue() != getPreferenceBestValue(a,at)) {
		GLMDebug.DEBUG("TaskUtils","compare AspectType:"+at+" b:"+p.getScoringFunction().getBest().getValue()+" a:"+getPreferenceBestValue(a,at));
		return false;
	    }
	}
	return true;
	
    }


    public static  String getPriority(Task task) {
	byte priority = task.getPriority();
	switch (priority) {
	case Priority.VERY_HIGH:
	    return "1";
	case Priority.HIGH:
	    return "3";
	case Priority.MEDIUM:
	    return "5";
	case Priority.LOW:
	    return "7";
	case Priority.VERY_LOW:
	    return "9";
	}
	return "1"; // default to high priority
    }

    public static int getNumericPriority(Task task) {
	byte priority = task.getPriority();
	switch (priority) {
	case Priority.VERY_HIGH:
	    return 1;
	case Priority.HIGH:
	    return 3;
	case Priority.MEDIUM:
	    return 5;
	case Priority.LOW:
	    return 7;
	case Priority.VERY_LOW:
	    return 9;
	}
	return 1; // default to high priority
    }


    // TASK PREFERENCE UTILS

    public static double getQuantity(Task task) {
	return getPreferenceBestValue(task, AspectType.QUANTITY);
    }

    public static Rate getRate(Task task) {
        AspectValue best = getPreferenceBest(task, AlpineAspectType.DEMANDRATE);
        if (best == null)
            GLMDebug.ERROR("TaskUtils", "getRate(), Task is not Projection :"+taskDesc(task));
	return ((AspectRate) best).getRateValue();
    }

    public static double getMultiplier(Task task) {
        AspectValue best = getPreferenceBest(task, AlpineAspectType.DEMANDMULTIPLIER);
        if (best == null)
            GLMDebug.ERROR("TaskUtils", "getRate(), Task is not Projection :"+taskDesc(task));
	return best.getValue();
    }

    public static double getRefillQuantity(Task refillTask){
        PlanElement pe = refillTask.getPlanElement();
        double qty;
        if ((pe instanceof Allocation) && (pe.getReportedResult()!= null)) {
            // get actual allocation results
            AllocationResult ar = pe.getReportedResult();

            if (!ar.isSuccess()) {
                qty = 0.0;
            } else {
                qty = getQuantity(ar);
            }
        } else {
            // get requested results until actual results are available
            qty=TaskUtils.getPreference(refillTask, AspectType.QUANTITY);;
        }
        return qty;
  }

  public static double getWithdrawQuantity(Task withdrawTask) {
       PlanElement pe = withdrawTask.getPlanElement();
       double qty = Double.NaN;
       // if there is an observed result use it, otherwise use the preference
       if ((pe instanceof Allocation) && (pe.getObservedResult()!= null)) {
	   // get actual allocation results
            AllocationResult ar = pe.getObservedResult();
	    qty = getQuantity(ar);
       }
       if (Double.isNaN(qty)) {
            // get requested results until observed results are available
            qty=TaskUtils.getPreference(withdrawTask, AspectType.QUANTITY);;
       }
        return qty;
  }

  public static long getRefillTime(Task refillTask){
	PlanElement pe = refillTask.getPlanElement();
	long end_time;
	if ((pe instanceof Allocation) && (pe.getReportedResult()!= null)) {
	    // get actual allocation results
	    AllocationResult ar = pe.getReportedResult();

	    if (!ar.isSuccess()) {
		end_time = (long)getEndTime(ar);
	    } else {
		end_time = (long)getEndTime(ar);
	    }
	} else {
	    // get requested results until actual results are available
	    end_time = TaskUtils.getEndTime(refillTask);
	}
	return end_time;
  }

  public static Date getRefillDate(Task refillTask){
      return new Date(getRefillTime(refillTask));
  }

     /** return the preference of the given aspect type.  Returns null if
      *  the task does not have the given aspect. */
    public static double getPreference(Task t, int aspect_type) {
	Preference p = t.getPreference(aspect_type);
	if (p == null) return Double.NaN;

	AspectScorePoint asp = p.getScoringFunction().getBest();
	return asp.getValue();
    }

    public static NewTask addPrepositionalPhrase(NewTask task, PrepositionalPhrase pp)
    {
	Enumeration enum = task.getPrepositionalPhrases();
	if (!enum.hasMoreElements())
	    task.setPrepositionalPhrase(pp);
	else {
	    Vector phrases = new Vector();
	    while (enum.hasMoreElements()) {
		phrases.addElement(enum.nextElement());
	    }
	    phrases.addElement(pp);
	    task.setPrepositionalPhrases(phrases.elements());
	}
	return task;
    } 

    public static void replacePrepositionalPhrase(NewTask task, PrepositionalPhrase pp)
    {
	String prep = pp.getPreposition();
	if (task.getPrepositionalPhrase(prep) == null) {
	    // its new, just add to the list
	    addPrepositionalPhrase(task, pp);
	    return;
	}

	PrepositionalPhrase phrase;
	Enumeration enum = task.getPrepositionalPhrases();
	Vector phrases = new Vector();
	while (enum.hasMoreElements()) {
	    phrase = (PrepositionalPhrase)enum.nextElement();
	    if (!phrase.getPreposition().equals(prep)) {
		phrases.addElement(phrase);
	    }
	}
	phrases.addElement(pp);
	task.setPrepositionalPhrases(phrases.elements());
    } 

    public static double getQuantity(AllocationResult ar) {
	return getARAspectValue(ar, AspectType.QUANTITY);
    }

    public static String arDesc(AllocationResult ar) {
	return "(AR: "+ (long)ar.getValue(AspectType.QUANTITY) +"; "+
	    TimeUtils.dateString((long)ar.getValue(AspectType.START_TIME))+","+
	    TimeUtils.dateString((long)ar.getValue(AspectType.END_TIME))+")";
    }

    public static boolean isProjection(Task t) {
        return t.getPreference(AlpineAspectType.DEMANDRATE) != null;
    }

    public static boolean isSupply(Task t) {
	return !isProjection(t);
    }

    public static Preference createDemandRatePreference(LdmFactory ldmf, Rate rate) {
      ScoringFunction sf = ScoringFunction
        .createStrictlyAtValue(new AspectRate(AlpineAspectType.DEMANDRATE,
                                              rate));
      return ldmf.newPreference(AlpineAspectType.DEMANDRATE, sf);
    }

    public static Preference createDemandMultiplierPreference(LdmFactory ldmf, double mult) {
      ScoringFunction sf = ScoringFunction
        .createStrictlyAtValue(new AspectValue(AlpineAspectType.DEMANDMULTIPLIER,
                                               mult));
      return ldmf.newPreference(AlpineAspectType.DEMANDMULTIPLIER, sf);
    }
}
