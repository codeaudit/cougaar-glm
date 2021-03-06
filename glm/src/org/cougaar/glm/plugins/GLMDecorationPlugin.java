/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
package org.cougaar.glm.plugins;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.util.UnaryPredicate;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Defines common functions described in SimplePlugin.
 * The plugin is decorated with the proper BasicProcessor at run time
 * by the PluginDecorator.
 * @see PluginDecorator
 */
public abstract class GLMDecorationPlugin extends DecorationPlugin {
  private static final String SYNCHRONOUS_MODE_PROP = "org.cougaar.glm.plugins.synchronous";
  /**
   * Map keyed by OPlan UID of ClusterOPlans *
   */
  public Map ClusterOPlans_ = new HashMap();
  public IncrementalSubscription oplans_;
  public boolean oplanChanged_ = false, orgActChanged_ = false, clusterOplanChanged_ = false;
  /**
   * Map keyed by OPlan UID to an org activity subscription *
   */
  private Map orgActivitySubscriptionOfOPlanUID = new HashMap();

  // oplan
  static class OplanPredicate implements UnaryPredicate {
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
        if (oplanUID_.equals(((OrgActivity) o).getOplanUID())) {
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
    for (Iterator i = c.iterator(); i.hasNext();) {
      ClusterOPlan coplan = (ClusterOPlan) i.next();
      ClusterOPlans_.put(coplan.getOplanUID(), coplan);
      //ClusterOPlans_.put(oplanUID, coplan);

      UID oplanUID = coplan.getOplanUID();
      IncrementalSubscription oplanActivities = (IncrementalSubscription) orgActivitySubscriptionOfOPlanUID.get(oplanUID);
      if (oplanActivities == null) {
        oplanActivities = (IncrementalSubscription) subscribe(new OplanOrgActivitiesPredicate(oplanUID));
        monitorPluginSubscription(oplanActivities);
        orgActivitySubscriptionOfOPlanUID.put(oplanUID, oplanActivities);
      }
    }
  }


  protected void setupSubscriptions() {
    super.setupSubscriptions();
    oplans_ = (IncrementalSubscription) subscribe(new OplanPredicate());
    monitorPluginSubscription(oplans_);

    if (didRehydrate()) {
      getClusterOPlans();
      doUpdateOplans();
    }
  }

  private static Object syncLock = new Object();
  private static boolean syncLocked = false;
  private static boolean synchronousMode = System.getProperty(SYNCHRONOUS_MODE_PROP, "false").equals("true");

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
    if (logger.isDebugEnabled()) {
      logger.debug("---------- BEGIN execute() " + getShortClassName() + "(" + clusterId_ + ")" + " ----------");
    }
  }

  private void syncFinish() {
    if (logger.isDebugEnabled()) {
      logger.debug("------------ END execute() " + getShortClassName() + "(" + clusterId_ + ")" + " ----------");
    }
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
      if (ix >= 0)
        s = s.substring(ix + 1);
      shortClassName = s;
    }
    return shortClassName;
  }

  /**
   * Invokes all of the processors used to decorate this plugin.
   * The first time execute() is invoked, it configures the plugin by
   * setting the task processor, and unsubscribing to 'self'
   * (subscribed in setSubscriptions()).
   */
  public synchronized void execute() {
    if (synchronousMode)
      syncStart();
    try {
      super.execute();
      if (!invoke_)
        return;
      oplanChanged_ = false;
      orgActChanged_ = false;
      clusterOplanChanged_ = updateOplans();
      orgActChanged_ = updateOrgActivities();
      oplanChanged_ = clusterOplanChanged_ || orgActChanged_;
      runProcessors();
    } finally {
      if (synchronousMode)
        syncFinish();
    }
  }

  private boolean updateOplans() {
    boolean oplanChange = false;
    if (logger.isDebugEnabled()) {
      logger.debug("starting updateOplans");
    }
    if (isSubscriptionChanged(oplans_)) {
      doUpdateOplans();
      oplanChange = true;
    }
    return oplanChange;
  }

  // Process Oplan subscription
  private void doUpdateOplans() {
    if (logger.isDebugEnabled()) {
      logger.debug("Updating the Oplans!");
    }
    Enumeration en;
    // Create new ClusterOPlan objects for each added Oplan
    if (oplans_.getAddedList().hasMoreElements()) {
      en = oplans_.getAddedList();
      while (en.hasMoreElements()) {
        Oplan oplan = (Oplan) en.nextElement();
        UID oplanUID = oplan.getUID();
        IncrementalSubscription oplanActivities = (IncrementalSubscription) orgActivitySubscriptionOfOPlanUID.get(oplanUID);
        if (oplanActivities == null) {
          oplanActivities = (IncrementalSubscription) subscribe(new OplanOrgActivitiesPredicate(oplanUID));
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
      en = oplans_.getRemovedList();
      while (en.hasMoreElements()) {
        Oplan oplan = (Oplan) en.nextElement();
        UID oplanUID = oplan.getUID();
        ClusterOPlan coplan = (ClusterOPlan) ClusterOPlans_.get(oplanUID);
        // Remove ClusterOPlan from array
        ClusterOPlans_.remove(oplanUID);
        // Cancel subscription
        IncrementalSubscription s = (IncrementalSubscription) orgActivitySubscriptionOfOPlanUID.remove(oplanUID);
        if (s != null)
          unsubscribe(s);
        publishRemove(coplan);
        break;
      }
    }
    if (ClusterOPlans_.isEmpty()) {
      if (logger.isErrorEnabled()) {
        logger.error(" updateOplans no OPLAN");
      }
    }
  }

  // Each ClusterOPlan updates its own OrgActivities if needed
  private boolean updateOrgActivities() {
    Iterator en = ClusterOPlans_.values().iterator();
    boolean update = false;
    while (en.hasNext()) {
      ClusterOPlan coplan = (ClusterOPlan) en.next();
      IncrementalSubscription s = (IncrementalSubscription) orgActivitySubscriptionOfOPlanUID.get(coplan.getOplanUID());
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
      oplan = (ClusterOPlan) oplans.nextElement();
      geo = oplan.getGeoLoc(time);
      if (geo != null)
        return geo;
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
