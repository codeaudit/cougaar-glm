/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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
 */
package org.cougaar.glm.plugins;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.MutableTimeSpan;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.plan.GeolocLocation;

/**
 * Object for holding meta-data about the OPlan on the blackboard.
 **/
public class ClusterOPlan implements Serializable {
  MessageAddress clusterId_;
  long startTime_, endTime_;
  Vector orgActivities_ = null;
  Oplan oplan_;
  //      IncrementalSubscription orgActivitySubscription_;

  public ClusterOPlan(MessageAddress id, Oplan op/*, IncrementalSubscription sub*/) {
    //  	System.out.println("--- Creating ClusterOPlan for "+id+", oplan "+op);
    clusterId_ = id;
    oplan_ = op;
    startTime_ = oplan_.getCday().getTime();
    endTime_ = startTime_ + 1;
    //    endTime_ = oplan_.getEndDay().getTime();
    //  	orgActivitySubscription_ = sub;
    //  	updateOrgActivities(orgActivitySubscription_.elements());
    //  	updateOPlanTimes();
  }

  /* If OPlan does not change but OrgActivities for the OPlan change
   * then get the updated OrgActivities.
   */
  public boolean updateOrgActivities(IncrementalSubscription orgActivitySubscription_) {
    // Only update OrgActivities if subscription has changed
    if (orgActivitySubscription_.getChangedList().hasMoreElements()
	|| orgActivitySubscription_.getAddedList().hasMoreElements() 
	|| orgActivitySubscription_.getRemovedList().hasMoreElements()) {
      //  	    System.out.println("--- New/Changed/Removed OrgActivities for "+clusterId_);
      updateOrgActivities(orgActivitySubscription_.elements());
      updateOPlanTimes();
      return true;
    }
    return false;
  }

  // Re-calculate the overall start and end times for the OPlan
  private void updateOPlanTimes() {
    long end_time;
    long newET = Long.MIN_VALUE;
    long start_time;
    long newST = Long.MAX_VALUE;
    OrgActivity activity;
    Enumeration activities = orgActivities_.elements();

    // initialize endTime/startTime values
    if (activities.hasMoreElements()) {
      activity = (OrgActivity)activities.nextElement();
      newET = getEndTime(activity);
      newST = getStartTime(activity);
    }

    // search for first/last times
    while (activities.hasMoreElements()) {
      activity = (OrgActivity)activities.nextElement();
      end_time = getEndTime(activity);
      if ((end_time > newET)) {
	newET = end_time;
      }
      start_time = getStartTime(activity);
      if (start_time < newST) {
	newST = start_time;
      }
    }

    if (newET != endTime_ || newST != startTime_) {
      //System.out.println("ClusterOplan at " + clusterId_ + " updating startTime from " + startTime_ + " to " + newST + ", and endTime from " + endTime_ + " to " + newET);
      synchronized(this) {
	endTime_ = newET;
	startTime_ = newST;
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
	//  		System.out.println("--- Adding OrgActivity for "+clusterId_+", activity "+orgact.getActivityName()+
	//  				   ", "+orgact.getOpTempo()+", "+orgact);
	orgActivities_.add(orgact);
      }
    }
  }

  /* getOplan() returns the OPlan UID this object is handling
   */
  public UID getOplanUID() {
    return oplan_.getUID();
  }

  public long getEndTime(OrgActivity act) {
    return act.getTimeSpan().getEndDate().getTime();
  }

  public long getStartTime(OrgActivity act) {
    return act.getTimeSpan().getStartDate().getTime();
  }

  /* Latest end time of the OrgActivities
   * If no orgActivities are received, end time is not set
   * <b>WARN</b>: This is not safe, particularly if you will be comparing to StartTime. Use #getOplanSpan()
   */
  public long getEndTime() {
    return endTime_;
  }

  /* Earliest start time of the OrgActivities
   * If no orgActivities are received, start time is not set
   * <b>WARN</b>: This is not safe, particularly if you will be comparing to EndTime. Use #getOplanSpan()
   */
  public long getStartTime() {
    return startTime_;
  }

  /**
   * Return the current start and end times (longs) of the OPlan
   **/
  public synchronized TimeSpan getOplanSpan() {
    MutableTimeSpan ts = new MutableTimeSpan();
    try {
      ts.setTimeSpan(startTime_, endTime_);
    } catch (IllegalArgumentException iae) {
      System.err.println(clusterId_ + " ClusterOPlan has invalid oplan start/end times. Start: " + startTime_ + ", End: " + endTime_);
    }
    return ts;
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
  
  public long getOplanCday() {
    return  oplan_.getCday().getTime();
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

  //      /* When disposing of a ClusterOPlan object, need to get the
  //       * OrgActivity subscription to do an 'unsubscribe'
  //       */
  //      public IncrementalSubscription getOrgActivitySubscription() {
  //  	return orgActivitySubscription_;
  //      }

  public String toString() {
    return oplan_.toString();
  }
}





