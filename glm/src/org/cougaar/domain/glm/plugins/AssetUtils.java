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
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.util.Enumerator;
import org.cougaar.util.MutableTimeSpan;
import org.cougaar.util.TimeSpan;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.asset.SupplyClassPG;
import org.cougaar.domain.glm.debug.GLMDebug;

/** Provides convenience methods. */
public class AssetUtils {

    /**
     *  @param type java Class name (assumed to be in package org.cougaar.domain.planning.ldm.asset)
     *  @return true if asset is and instance of type.*/
    public static boolean isAssetOfType(Asset a, String type) {
	Class cl;
	try {
	    cl = Class.forName("org.cougaar.domain.glm.asset."+type);
	} catch ( ClassNotFoundException cnfe) {
	    GLMDebug.ERROR("AssetUtils",null, "isAssetOfType: "+cnfe);
	    return false;
	}
	return cl.isInstance(a);
    }

    /**
     *  @param type String describing class of resource
     *  @return true if Asset has SupplyClassPG and SupplyType equals type */
    public static boolean isSupplyClassOfType(Asset asset, String type) {
	boolean result = false;
	SupplyClassPG pg = (SupplyClassPG)asset.searchForPropertyGroup(SupplyClassPG.class);
	if (pg != null) {
	    result = type.equals(pg.getSupplyType());
	} else {
	    GLMDebug.DEBUG("AssetUtils", null, "isSupplyClassOfType(): Asset without SupplyClassPG "+
			   assetDesc(asset));
	}
	return result;
    }

    public static String assetDesc(Asset asset){
	String nsn = getAssetIdentifier(asset);
	return nsn+"("+getPartNomenclature(asset)+")";
    }

    public static String getPartNomenclature(Asset part){
	String nomen="Unknown part name";
	TypeIdentificationPG tip = part.getTypeIdentificationPG();
	if (tip!= null) {
	    nomen = tip.getNomenclature();
	}
	return nomen;
    }

    public static String getAssetIdentifier(Asset asset) {
	String nsn = null;
	if (asset == null) {
	    return null;
	} else {
	    TypeIdentificationPG tip = asset.getTypeIdentificationPG();
	    if (tip!= null) {
		return tip.getTypeIdentification();
	    }else {
		GLMDebug.ERROR("AssetUtils",null,"asset: "+asset+" has null getTypeIdentificationPG()");
		return null;
	    }
	}
    }

   // Determines if an orginazation provides a supporting role for another organization.
    public static boolean isOrgSupporting(Organization org, Organization support_org, Role role) {
	RelationshipSchedule rel_sched = org.getRelationshipSchedule();
	// Ask for matching relationships where support_org is the provider.
	// MutableTimeSpan() will create a TimeSpan from the beginning of time to the end of time and
	// therefore look at all relationships.
	Collection c  = rel_sched.getMatchingRelationships(role, support_org, new MutableTimeSpan());
	return !c.isEmpty();
    }

    public static Enumeration getSupportingOrgs(Organization myOrg, Role role, long start, long end) {
	RelationshipSchedule rel_sched = myOrg.getRelationshipSchedule();
	Collection c = rel_sched.getMatchingRelationships(role, start, end);
	Vector support_orgs = new Vector();
	Iterator i = c.iterator();
	Relationship r;
	while (i.hasNext()) {
	    r = (Relationship)i.next();
	    support_orgs.add(rel_sched.getOther(r));
	}
	return support_orgs.elements();
    }
    
    public static void printRelationshipSchedule(Organization myOrg) {
	RelationshipSchedule sched = myOrg.getRelationshipSchedule();
	Enumeration enum = sched.getAllScheduleElements();
	Relationship r;
	GLMDebug.DEBUG("AssetUtils",null,"____________________________________________________________");
	while (enum.hasMoreElements()) {
	    r = (Relationship)enum.nextElement();
	    GLMDebug.DEBUG("AssetUtils",null,r.getRoleA()+", "+r.getRoleB()+", start: "+r.getStartTime()+", end: "+
			   r.getEndTime());
	}
    }
}

