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

import java.util.Enumeration;
import java.util.Vector;

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
    public boolean                         oplanChanged_ = false;

    // oplan
    static class OplanPredicate implements UnaryPredicate
    {
	public boolean execute(Object o) {
	    return (o instanceof Oplan);
	}
    } 

    protected void setupSubscriptions() {
	super.setupSubscriptions();
	oplans_ = (IncrementalSubscription)
	                     subscribe(new OplanPredicate());
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

	oplanChanged_ = updateOplans();
	runProcessors();
    }

    private boolean updateOplans() {
        GLMDebug.DEBUG(this.getClass().getName(), clusterId_,"starting updateOplans");
	if (isSubscriptionChanged(oplans_)) {
	    doUpdateOplans();
	    return true;
	}
	return false;
    }

    private void doUpdateOplans() {
	GLMDebug.DEBUG(this.getClass().getName(), clusterId_,"Updating the Oplans!");
	Enumeration enum = oplans_.elements();
	ClusterOPlans_ = new Vector();
	while (enum.hasMoreElements()) {
	    //		oplan_ = new ClusterOPlan(clusterId_, (Oplan)enum.nextElement());
	    ClusterOPlans_.add(new ClusterOPlan(clusterId_, (Oplan)enum.nextElement()));
	}
	if (ClusterOPlans_.isEmpty()) {
		GLMDebug.ERROR("GLMDecorationPlugIn", clusterId_, "updateOplans no OPLAN");
	    //		oplan_ = null;
	}
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
