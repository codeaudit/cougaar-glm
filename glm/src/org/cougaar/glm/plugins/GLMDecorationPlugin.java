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
package org.cougaar.glm.plugins;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.core.util.UID;

import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.debug.GLMDebug;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.oplan.Oplan;

/**
 * Defines common functions described in SimplePlugin.
 * The plugin is decorated with the proper BasicProcessor at run time
 * by the PluginDecorator.
 * @see PluginDecorator
 */
public abstract class GLMDecorationPlugin extends DecorationPlugin {
    private static final String SYNCHRONOUS_MODE_PROP =
        "org.cougaar.glm.plugins.synchronous";
    /** Map keyed by OPlan UID of ClusterOPlans **/
    public Map                             ClusterOPlans_ = new HashMap();
    public IncrementalSubscription         oplans_;
    public boolean                         oplanChanged_ = false,
                                           orgActChanged_ = false, 
	                                   clusterOplanChanged_ = false;
    /** Map keyed by OPlan UID to an org activity subscription **/
    private Map orgActivitySubscriptionOfOPlanUID = new HashMap();

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

    private void getClusterOPlans() {
        Collection c = query(new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof ClusterOPlan;
            }
        });
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            ClusterOPlan coplan = (ClusterOPlan) i.next();
            ClusterOPlans_.put(coplan.getOplanUID(), coplan);
            //ClusterOPlans_.put(oplanUID, coplan);

            UID oplanUID = coplan.getOplanUID();
            IncrementalSubscription oplanActivities = (IncrementalSubscription)
                orgActivitySubscriptionOfOPlanUID.get(oplanUID);
            if (oplanActivities == null) {
                oplanActivities = (IncrementalSubscription)
                    subscribe(new OplanOrgActivitiesPredicate(oplanUID));
                monitorPluginSubscription(oplanActivities);
                orgActivitySubscriptionOfOPlanUID.put(oplanUID, oplanActivities);
            }
        }
    }


    protected void setupSubscriptions() {
	super.setupSubscriptions();
	oplans_ = (IncrementalSubscription)subscribe(new OplanPredicate());
	monitorPluginSubscription(oplans_);
	
	if(didRehydrate()) {
            getClusterOPlans();
	    doUpdateOplans();
	}
    }

    private static Object syncLock = new Object();
    private static boolean syncLocked = false;
    private static boolean synchronousMode =
        System.getProperty(SYNCHRONOUS_MODE_PROP, "false").equals("true");

    private void syncStart() {
        synchronized (syncLock) {
            while (syncLocked) {
                try {
                    syncLock.wait();
                } catch (InterruptedException ie) {
                }
            }
            syncLocked = true;
        }
        GLMDebug.setDelayedSeparator("---------- BEGIN execute() "
                                     + getShortClassName()
                                     + "("
                                     + clusterId_
                                     + ")"
                                     + " ----------");
    }

    private void syncFinish() {
        GLMDebug.clearDelayedSeparator("------------ END execute() "
                                       + getShortClassName()
                                       + "("
                                       + clusterId_
                                       + ")"
                                       + " ----------");
        synchronized (syncLock) {
            syncLocked = false;
            syncLock.notify();
        }
    }
    private String shortClassName = null;
    private String getShortClassName() {
        if (shortClassName == null) {
            String s = getClass().getName();
            int ix = s.lastIndexOf('.');
            if (ix >= 0) s = s.substring(ix + 1);
            shortClassName = s;
        }
        return shortClassName;
    }
   
    /** Invokes all of the processors used to decorate this plugin.
     *  The first time execute() is invoked, it configures the plugin by
     *  setting the task processor, and unsubscribing to 'self'
     *  (subscribed in setSubscriptions()).
     */
    public synchronized void execute()
    {
        if (synchronousMode) syncStart();
        try {
            super.execute();
            if (!invoke_) return;
            oplanChanged_ = false;
            orgActChanged_ = false;
            clusterOplanChanged_ = updateOplans();
            orgActChanged_ = updateOrgActivities();
            oplanChanged_ = clusterOplanChanged_ || orgActChanged_;
            runProcessors();
        } finally {
            if (synchronousMode) syncFinish();
        }
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
                UID oplanUID = oplan.getUID();
                IncrementalSubscription oplanActivities = (IncrementalSubscription)
                    orgActivitySubscriptionOfOPlanUID.get(oplanUID);
                if (oplanActivities == null) {
                    oplanActivities = (IncrementalSubscription)
                        subscribe(new OplanOrgActivitiesPredicate(oplanUID));
                    monitorPluginSubscription(oplanActivities);
                    orgActivitySubscriptionOfOPlanUID.put(oplanUID, oplanActivities);
                }
                ClusterOPlan coplan = (ClusterOPlan) ClusterOPlans_.get(oplanUID);
                if (coplan == null) {
                    coplan = new ClusterOPlan(clusterId_, oplan);
                    ClusterOPlans_.put(oplanUID, coplan);
                    publishAdd(coplan);
                }
	    }
	}
	// Remove ClusterOPlan objects that are no longer relevant
	if (oplans_.getRemovedList().hasMoreElements()) {
	    enum = oplans_.getRemovedList();
	    while (enum.hasMoreElements()) {
		Oplan oplan = (Oplan)enum.nextElement();
                UID oplanUID = oplan.getUID();
                ClusterOPlan coplan = (ClusterOPlan) ClusterOPlans_.get(oplanUID);
                // Remove ClusterOPlan from array
                ClusterOPlans_.remove(oplanUID);
                // Cancel subscription
                IncrementalSubscription s = (IncrementalSubscription)
                    orgActivitySubscriptionOfOPlanUID.remove(oplanUID);
                if (s != null) unsubscribe(s);
                publishRemove(coplan);
                break;
	    }
	}
	if (ClusterOPlans_.isEmpty()) {
		GLMDebug.ERROR("GLMDecorationPlugin", clusterId_, " updateOplans no OPLAN");
	}
    }

    // Each ClusterOPlan updates its own OrgActivities if needed
    private boolean updateOrgActivities() {
	Iterator enum = ClusterOPlans_.values().iterator();
	boolean update = false;
	while (enum.hasNext()) {
            ClusterOPlan coplan = (ClusterOPlan) enum.next();
            IncrementalSubscription s = (IncrementalSubscription)
                orgActivitySubscriptionOfOPlanUID.get(coplan.getOplanUID());
	    update = update || coplan.updateOrgActivities(s);
	}
	return update;
    }

    public Vector getOPlans() {
	return new Vector(ClusterOPlans_.values());
    }

    public ClusterOPlan findOPlan(UID oplanUID) {
        return (ClusterOPlan) ClusterOPlans_.get(oplanUID);
    }

    public ClusterOPlan findOPlan(Oplan oplan) {
        return findOPlan(oplan.getUID());
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
