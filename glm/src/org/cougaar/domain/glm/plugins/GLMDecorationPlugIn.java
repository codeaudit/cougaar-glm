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
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.core.society.UID;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;

import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.debug.GLMDebug;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.oplan.Oplan;

/**
 * Defines common functions described in SimplePlugIn.
 * The plugin is decorated with the proper BasicProcessor at run time
 * by the PlugInDecorator.
 * @see PlugInDecorator
 */
public abstract class GLMDecorationPlugIn extends DecorationPlugIn {

    public Vector                        ClusterOPlans_ = new Vector();
    //    public ClusterOPlan                        oplan_ = null;
    public IncrementalSubscription         oplans_;
    public boolean                         oplanChanged_ = false,
                                           orgActChanged_ = false, 
	                                   clusterOplanChanged_ = false;

    // oplan
    static class OplanPredicate implements UnaryPredicate
    {
	public boolean execute(Object o) {
	    return (o instanceof Oplan);
	}
    } 

    static class OplanOrgActivitiesPredicate implements UnaryPredicate {
	UID oplanUID_;
	public OplanOrgActivitiesPredicate(UID uid) {
	    oplanUID_ = uid;
	}

	public boolean execute(Object o) {
	    if (o instanceof OrgActivity) {
		if (oplanUID_.equals(((OrgActivity)o).getOplanUID())) {
		    return true;
		}
	    }
	    return false;
	}
    }

    protected void setupSubscriptions() {
	super.setupSubscriptions();
	oplans_ = (IncrementalSubscription)subscribe(new OplanPredicate());
	monitorPlugInSubscription(oplans_);
	
	if(didRehydrate()) {
	    doUpdateOplans();
	}
    }
   
    /** Invokes all of the processors used to decorate this plugin.
     *  The first time execute() is invoked, it configures the plugin by
     *  setting the task processor, and unsubscribing to 'self'
     *  (subscribed in setSubscriptions()).
     */
    public synchronized void execute()
    {
	super.execute();
	if (!invoke_) return;
	oplanChanged_ = false;
	orgActChanged_ = false;
	clusterOplanChanged_ = updateOplans();
	orgActChanged_ = updateOrgActivities();
	oplanChanged_ = clusterOplanChanged_ || orgActChanged_;
	runProcessors();
    }

    private boolean updateOplans() {
	boolean oplanChange = false;
        GLMDebug.DEBUG(this.getClass().getName(), clusterId_,"starting updateOplans");
	if (isSubscriptionChanged(oplans_)) {
	    doUpdateOplans();
	    oplanChange = true;
	}
	return oplanChange;
    }

    // Process Oplan subscription
    private void doUpdateOplans() {
	GLMDebug.DEBUG(this.getClass().getName(), clusterId_,"Updating the Oplans!");
  	Enumeration enum;
	// Create new ClusterOPlan objects for each added Oplan
	if (oplans_.getAddedList().hasMoreElements()) {
	    enum = oplans_.getAddedList();
	    while (enum.hasMoreElements()) {
		Oplan oplan = (Oplan)enum.nextElement();
		// Give each ClusterOPlan a subscription to its OrgActivities
		IncrementalSubscription oplanActivities = 
		    (IncrementalSubscription)subscribe(new OplanOrgActivitiesPredicate(oplan.getUID()));
		monitorPlugInSubscription(oplanActivities);
		System.out.println("--- Creating new ClusterOPlan for "+oplan);
		ClusterOPlans_.add(new ClusterOPlan(clusterId_, oplan, oplanActivities));
	    }
	}
	// Remove ClusterOPlan objects that are no longer relevant
	if (oplans_.getRemovedList().hasMoreElements()) {
	    enum = oplans_.getRemovedList();
	    while (enum.hasMoreElements()) {
		Oplan oplan = (Oplan)enum.nextElement();
		Enumeration cluster_oplans = ClusterOPlans_.elements();
		while (cluster_oplans.hasMoreElements()) {
		    ClusterOPlan coplan = (ClusterOPlan)cluster_oplans.nextElement();
		    if (coplan.getOplanUID().equals(oplan.getUID())) {
			// Remove ClusterOPlan from array
			ClusterOPlans_.remove(coplan);
			// Cancel subscription
			unsubscribe(coplan.getOrgActivitySubscription());
			break;
		    }
		}
	    }
	}
	if (ClusterOPlans_.isEmpty()) {
		GLMDebug.ERROR("GLMDecorationPlugIn", clusterId_, "updateOplans no OPLAN");
	}
    }

    // Each ClusterOPlan updates its own OrgActivities if needed
    private boolean updateOrgActivities() {
	Enumeration enum = ClusterOPlans_.elements();
	boolean update = false;
	while (enum.hasMoreElements()) {
	    update = update || ((ClusterOPlan)enum.nextElement()).updateOrgActivities();
	}
	return update;
    }

    public Vector getOPlans() {
	return ClusterOPlans_;
    }

    public ClusterOPlan findOPlan(UID oplanUID) {
        for (Enumeration oplans = getOPlans().elements(); oplans.hasMoreElements(); ) {
            ClusterOPlan coplan = (ClusterOPlan) oplans.nextElement();
            if (coplan.oplan_.getUID().equals(oplanUID)) return coplan;
        }
        return null;
    }

    public ClusterOPlan findOPlan(Oplan oplan) {
        for (Enumeration oplans = getOPlans().elements(); oplans.hasMoreElements(); ) {
            ClusterOPlan coplan = (ClusterOPlan) oplans.nextElement();
            if (coplan.oplan_.same(oplan)) return coplan;
        }
        return null;
    }

    // returns location from first oplan w/ info on that time.
    public GeolocLocation getGeoLoc(long time) {
	Enumeration oplans = getOPlans().elements();
	ClusterOPlan oplan;
	GeolocLocation geo;
	while (oplans.hasMoreElements()) {
	    oplan = (ClusterOPlan)oplans.nextElement();
	    geo = oplan.getGeoLoc(time);
	    if (geo != null) return geo;
	}
	return null;

    }

    public boolean oplanChanged() {
	return oplanChanged_;
    }

    public ClusterOPlan getOperativeOPlan(long time, ContextOfUIDs context) {
	// GLMDebug.DEBUG(this.getClass().getName(), clusterId_,"getOperativeOPlan- context is: " + context);
        for (int i = 0, n = context.size(); i < n; i++) {
            ClusterOPlan oplan = findOPlan(context.get(i));
            if (oplan != null) {
                if ((oplan.getStartTime() <= time) && (oplan.getEndTime() > time)) {
                    return oplan;
                }
            }
        }
        return null;
    }

}
