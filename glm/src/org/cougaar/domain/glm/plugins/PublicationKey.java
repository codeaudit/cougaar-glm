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

import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Task;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.asset.Inventory;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.asset.PhysicalPG;
import org.cougaar.domain.glm.asset.ScheduledContentPG;
import org.cougaar.domain.glm.plan.GeolocLocation;
import org.cougaar.domain.glm.debug.GLMDebug;

/** Provide methods to supply 'keys' uniquely describing published objects. */
public class PublicationKey
{

    protected static String getKey(Object obj) 
    {
	if (obj instanceof Expansion)
	    return getKey((Expansion)obj);
	if (obj instanceof Allocation)
	    return getKey((Allocation)obj);
	if (obj instanceof Asset)
	    return getKey((Asset)obj);
	if (obj instanceof Task)
	    return getKey((Task)obj);
	else 
	    return null;
    }
	
    // Creates 'useful' string for Assets, AggregateAssets, 
    // Organizations, and Inventory objects
    protected static String getKey(Asset a)
    {
	String key = "Asset: TYPE_ID:"+
	    a.getTypeIdentificationPG().getTypeIdentification();

	TypeIdentificationPG ti = a.getTypeIdentificationPG();
	if (ti != null) {
	    key += " NOMEN:"+ti.getTypeIdentification();
	}

	if (a instanceof AggregateAsset) {
	    key += "aggregate QTY ="+((AggregateAsset)a).getQuantity();
	    a = ((AggregateAsset)a).getAsset();
	} else if (a instanceof Organization) {
	    key += " ORG:"+((Organization)a).getClusterIdentifier().getAddress();
	} else {
	    ScheduledContentPG prop = (ScheduledContentPG) a.searchForPropertyGroup(ScheduledContentPG.class);
	    if (prop != null) {
		key +=" INVENTORY ASSET "+getKey(prop.getAsset());
	    }
	    PhysicalPG phys_prop = (PhysicalPG) a.searchForPropertyGroup(PhysicalPG.class);
	    if (phys_prop != null) {
		if (phys_prop.getVolume() != null) {
		    key += " VOL:"+ phys_prop.getVolume().getCubicFeet();
		}
		if (phys_prop.getMass() != null) {
		    key += " MASS:"+ phys_prop.getMass().getTons();
		}
	    }
	}
	return key;
    }

    protected static String getKey(Task t)
    {
	String key = "Task: "+getTotalTaskKey(t);
	return key;
    }

    protected static String getTotalTaskKey(Task task)
    {
	String key = getTaskKey(task);
	double quantity = TaskUtils.getPreferenceBestValue(task, AspectType.QUANTITY);
	if (quantity != Double.NaN) key +=" QTY:"+quantity;
	double end = TaskUtils.getPreferenceBestValue(task, AspectType.END_TIME);
	if (end != Double.NaN) key += " END:" + TimeUtils.dateString(new Date((long) end));
	return key;
    }


    protected static String getKey(Allocation alloc)
    {
	String key = "Allocation of Task: ";
	key += getTaskKey(alloc.getTask());
	key += " To: "+getKey(alloc.getAsset());
	return key;
    }

    // from sourcing
    protected static String getKey(Expansion exp)
    {
	String key = "Expansion: ";
	key+=getTotalTaskKey(exp.getTask());
	if (exp.getWorkflow() == null) {
	    key+= " UNEXPANDED";
	} else {
	    key += " EXPANDED";
	}
	return key;
    }

   public static String getTaskKey(Task task)
    {
	String key = "VERB:"+task.getVerb().toString();
	
	Asset direct_obj = task.getDirectObject();
	if (direct_obj != null) {
	    key += " "+getKey(direct_obj);
	}

	// This whole prepositional phrase section should be replaced by the following
	// if I knew an easy way to sort objects...
 	Enumeration pps = task.getPrepositionalPhrases();
	Enumeration sorted_phrases = sortByPrepostion(pps);
	while (sorted_phrases.hasMoreElements()) {
	    PrepositionalPhrase pp = (PrepositionalPhrase)sorted_phrases.nextElement();
	    String preposition = pp.getPreposition();
	    Object indirect_obj = pp.getIndirectObject();
	    String description = ""+indirect_obj;
	    if (pp.equals(Constants.Preposition.TO) || pp.equals(Constants.Preposition.FROM)) {
		if (indirect_obj instanceof GeolocLocation) {
		    description = ((GeolocLocation)indirect_obj).getGeolocCode();
		} else {
		    GLMDebug.DEBUG("PublicationKey", null, preposition+" on "+taskDesc(task)+" geoloc:"+indirect_obj, 1);
		}
	    } else if (pp.equals(Constants.Preposition.FOR)) {
		if (indirect_obj instanceof Organization) {
		    description = ((Organization)indirect_obj).getItemIdentificationPG().getItemIdentification();
		} else if (indirect_obj instanceof String) {
		    description = (String)indirect_obj;
		} else {
		    printError("getTaskKey() FOR prep phrase not an org or string id. key:"+key+" TASK:"+taskDesc(task));
		}
	    } else if (pp.equals(Constants.Preposition.MAINTAINING)) {
		if (indirect_obj instanceof Asset) {
		    description = ((Asset)indirect_obj).getTypeIdentificationPG().getTypeIdentification() +
			"/UID:" + ((Asset)indirect_obj).getUID();
		} else {
		    printError("getTaskKey() Maintaining prep phrase not an asset:"+key+" TASK:"+taskDesc(task));
		}
	    }
	    key += " "+preposition+" "+description;
	}
		
	double time =  TaskUtils.getPreferenceBestValue(task, AspectType.START_TIME);
	if (time != Double.NaN) { 
	    key += " START:" + TimeUtils.dateString(new Date((long) time));
	} else {
	    time =  TaskUtils.getPreferenceBestValue(task, AspectType.END_TIME);
	    if (time != Double.NaN) {
		key += " END:" + TimeUtils.dateString(new Date((long) time));
	    }
	}
	return key;
    }



// 	PrepositionalPhrase pp0 = task.getPrepositionalPhrase("DemandSpec");
// 	if (pp0 != null) {
// 	    key += " DEMAND SPEC:"+pp0.getIndirectObject();
// 	}
	
// 	PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
// 	Object ind_obj;
// 	if (pp != null) {
// 	    ind_obj = pp.getIndirectObject();
// 	    String parent_string;
// 	    if (ind_obj instanceof Organization) {
// 		parent_string = ((Organization)ind_obj).getItemIdentificationPG().getItemIdentification();
// 	    } else if (ind_obj instanceof java.lang.String) {
// 		parent_string = (String)ind_obj;
// 	    } else {
// 		printError("getTaskKey() FOR prep phrase not an org or string id. key:"+key+" TASK:"+taskDesc(task));
// 		return key;
// 	    }
// 	    key += " FOR:"+parent_string;
// 	}

// 	pp = task.getPrepositionalPhrase(Constants.Preposition.USINGSUPPLYSOURCE);
// 	if (pp != null) {
// 	    ind_obj = pp.getIndirectObject();
// 	    if (ind_obj instanceof String) {
// 		key += " USINGSUPPLYSOURCE:"+ind_obj;
// 	    } else if (ind_obj instanceof Asset) {
// 		key += " USINGSUPPLYSOURCE: "+getKey(ind_obj);
// 	    } else {
// 		printError("getTaskKey() USINGSUPPLYSOURCE -- not an inventory object or a string"+ind_obj);
// 	    }
// 	}

// 	// if maintaining
// 	pp = task.getPrepositionalPhrase(Constants.Preposition.MAINTAINING);
// 	if (pp != null) {
// 	    ind_obj = pp.getIndirectObject();
// 	    if (!(ind_obj instanceof Asset)) {
// 		printError("getTaskKey() Maintaining prep phrase not an asset:"+key+" TASK:"+taskDesc(task));
// 		return key;
// 	    }
// 	    key += " MAINTAINING:"+((Asset)ind_obj).getTypeIdentificationPG().getTypeIdentification() +
// 	      "/UID:" + ((Asset)ind_obj).getUID();
// 	}



// 	// if to
// 	pp = task.getPrepositionalPhrase(Constants.Preposition.TO);
// 	if (pp != null) {
// 	    GeolocLocation geo = (GeolocLocation)pp.getIndirectObject();
// 	    if (geo != null) {
// 		key += " TO:"+geo.getGeolocCode();
// 	    }
// 	    else 
//  		GLMDebug.DEBUG("PublicationKey", null, "TO task: "+taskDesc(task)+" geoloc:"+geo, 1);
// 	}

// 	// if from
// 	pp = task.getPrepositionalPhrase(Constants.Preposition.FROM);
// 	if (pp != null) {
// 	    GeolocLocation geo = (GeolocLocation)pp.getIndirectObject();
// 	    if (geo != null) {
// 		key += " FROM:"+geo.getGeolocCode();
// 	    }
// 	    else 
//  		GLMDebug.ERROR("PublicationKey","FROM task: "+taskDesc(task)+" geoloc:"+geo);
// 	}

// 	// if oftype
// 	pp = task.getPrepositionalPhrase(Constants.Preposition.OFTYPE);
// 	if (pp != null) {
// 	    Object io = pp.getIndirectObject();
// 	    if (io != null) {
// 		key += " OFTYPE:"+io;
// 	    } else 
//  		GLMDebug.ERROR("PublicationKey","OFTYPE task: "+key+" OFTYPE w/ no io");
// 	}

	// try to get a time factor









    

    // there should be a much better sort available
    private static Enumeration sortByPrepostion(Enumeration pps) {
	Vector preps = new Vector();
	while (pps.hasMoreElements()) {
	    preps.add(pps.nextElement());
	}
	Vector sorted = new Vector();
	while (!preps.isEmpty()) {
	    pps = preps.elements();
	    PrepositionalPhrase best = (PrepositionalPhrase) pps.nextElement();
	    if (pps.hasMoreElements()) {
		PrepositionalPhrase next = (PrepositionalPhrase)pps.nextElement();
		if (next.getPreposition().compareTo(best.getPreposition()) < 0) {
		    best = next;
		}
	    }
	    sorted.add(best);
	    preps.remove(best);
	}
	return sorted.elements();
    }

    private static void printError(String msg) {
	GLMDebug.ERROR("PublicationKey",msg);
    }

    // utility functions
    public static String taskDesc(Task task) {
	return task.getUID() + ": "
	    + task.getVerb()+"("+
	    TaskUtils.getQuantity(task)+" "+
	    TaskUtils.getTaskItemName(task)+") "+
	    new Date(TaskUtils.getStartTime(task));
    }

}
