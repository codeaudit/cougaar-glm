package org.cougaar.domain.glm.plugins;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.debug.GLMDebug;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ClusterOPlan implements Serializable {
    ClusterIdentifier clusterId_;
    long startTime_ = -1, endTime_ = -1;
    Vector orgActivities_;
    Oplan oplan_;

    public ClusterOPlan(ClusterIdentifier id, Oplan op) {
	clusterId_ = id;
	oplan_ = op;
	updateOrgActivities(oplan_.getOrgActivities());
	updateOPlanTimes();
    }

    private void updateOPlanTimes() {
	long end_time;
	long start_time;
	OrgActivity activity;
	Enumeration activities = orgActivities_.elements();

	// initialize endTime/startTime values
	if (activities.hasMoreElements()) {
	    activity = (OrgActivity)activities.nextElement();
	    endTime_ = getEndTime(activity);
	    startTime_ = getStartTime(activity);
	}
	// search for first/last times
	while (activities.hasMoreElements()) {
	    activity = (OrgActivity)activities.nextElement();
	    end_time = getEndTime(activity);
	    if ((end_time > endTime_)) {
		endTime_ = end_time;
	    }
	    start_time = getStartTime(activity);
	    if (start_time < startTime_) {
		startTime_ = start_time;
	    }
	}
    }

    public void updateOrgActivities(Enumeration activities) {
	orgActivities_ = new Vector();
	OrgActivity orgact;
	String cluster_name = clusterId_.toString();
	while (activities.hasMoreElements()) {
	    // only deal w/ org activities for this cluster
	    orgact = (OrgActivity)activities.nextElement();
	    if (orgact.getOrgID().equals(cluster_name)) {
		orgActivities_.add(orgact);
	    }
	}
    }
    
    public long getEndTime(OrgActivity act) {
	return act.getTimeSpan().getEndDate().getTime();
    }

    public long getStartTime(OrgActivity act) {
	return act.getTimeSpan().getStartDate().getTime();
    }

    public long getEndTime() {
	return endTime_;
    }


    public long getStartTime() {
	return startTime_;
    }

    public GeolocLocation getGeoLoc(long time) {
	OrgActivity oa = getOrgActivity(time);
	if (oa != null) {
	    return oa.getGeoLoc();
	}
	return null;
    }

    public String getOpTempo(long time) {
	OrgActivity oa = getOrgActivity(time);
	if (oa != null) {
	    return oa.getOpTempo();
	}
	return null;
    }

    public OrgActivity getOrgActivity(long t) {
	long end_time, start_time;
	Enumeration enum = orgActivities_.elements();
	OrgActivity oa;
	while (enum.hasMoreElements()) {
	    oa = (OrgActivity)enum.nextElement();
	    start_time = getStartTime(oa);
	    end_time = getEndTime(oa);
	    if ((t >= start_time) && (t < end_time)) {
		return oa;
	    }
	}
	return null;
    }
}
