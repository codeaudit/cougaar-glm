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


package org.cougaar.glm.ldm.lps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;


import org.cougaar.core.blackboard.Envelope;
import org.cougaar.core.blackboard.EnvelopeTuple;
import org.cougaar.core.domain.EnvelopeLogicProvider;
import org.cougaar.core.domain.LogicProvider;
import org.cougaar.core.domain.RootPlan;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.LocalPG;
import org.cougaar.planning.ldm.asset.LocationSchedulePG;
import org.cougaar.planning.ldm.asset.LocationSchedulePGImpl;
import org.cougaar.planning.ldm.asset.NewLocationSchedulePG;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.util.EmptyEnumeration;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.cougaar.util.UnaryPredicate;



/**
 * OPlanWatcherLP tracks OPlan changes, reconciling other objects to the OPlan(s).
 * In particular, it updates the local Org asset with the required LocationSchedule changes.
 **/
public class OPlanWatcherLP
implements LogicProvider, EnvelopeLogicProvider
{
  private static final Logger logger = Logging.getLogger(OPlanWatcherLP.class);
  private final RootPlan rootplan;
  private final PlanningFactory ldmf;


  public OPlanWatcherLP(
      RootPlan rootplan,
      PlanningFactory ldmf) {
    this.rootplan = rootplan;
    this.ldmf = ldmf;
  }


  public void init() {
  }


  /**  
   * Catch interesting Oplan and Oplan component activity.
   */
  public void execute(EnvelopeTuple o, Collection changes) {
    Object obj = o.getObject();
    if (obj instanceof OrgActivity) {
      processOrgActivity((OrgActivity) obj, o.getAction());
    }
    // else do nothing
  }


    
  private void processOrgActivity(OrgActivity oa, int action) {
    // find the matching Organization
    final String orgID = oa.getOrgID();

    if (logger.isDebugEnabled()) {
      logger.debug("OplanWatcherLP() - OrgActivity = " + oa +
		   " action = " + action);
    }

    Organization org = null;
    UnaryPredicate pred = 
      new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o instanceof Organization) {
            return orgID.equals(((Organization)o).getMessageAddress().toString());
          } 
          return false;
        }
      };
    Enumeration en = rootplan.searchBlackboard(pred);
    if (en != null && en.hasMoreElements()) {
      org = (Organization)en.nextElement();
    } else {
      return;
    }



    /*
      // something like this would work if the world was sane... 
      // right now, we'd have to do something even uglier like
      //   findAsset("UIC/"+orgID);  
      // bleah!
    Asset a = logplan.findAsset(orgID);


    if (! (a instanceof Organization)) {
      logger.error("OPlanWatcher() found non org asset " +
                   a + " in logplan by name: "+orgID);
      return;
    }
    Organization org = (Organization) a;
    */


    // get the pg
    LocationSchedulePG lspg = org.getLocationSchedulePG();
    if (lspg == null) {
      org.setLocationSchedulePG(lspg = new LocationSchedulePGImpl());
    }
    
    // get the schedule
    Schedule oldls = lspg.getSchedule();
    Schedule newls;
    if (oldls == null) {
      newls = ldmf.newLocationSchedule(EmptyEnumeration.getEnumeration());
    } else {
      newls = ldmf.newLocationSchedule(oldls.getAllScheduleElements());
    }


    final UID oaUID = oa.getUID();  // the uuid of the org activity.
    
    // find old element(s) in ls with matching oaUIDs
    Collection found = newls.filter(new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof OrgActivity.OAScheduleElement) {
	  return oaUID.equals(((OrgActivity.OAScheduleElement)o).getOrgActivityUID());
	} else {
	  return false;
	}
      }
    });

    switch (action) {
    case Envelope.ADD:    
      {
	if (found.size()>0) {
	  logger.warn("OPlanWatcher() saw redundant OrgActivity add for "+
		      oa);
	}
	ScheduleElement se = oa.getNormalizedScheduleElement();
	if (se != null) {
	  newls.add(se);
	}
      }
    break;
    case Envelope.CHANGE:
      {
	newls.removeAll(found);
	ScheduleElement se = oa.getNormalizedScheduleElement();
	if (se != null) {
	  newls.add(se);
	}
      }
    break;
    case Envelope.REMOVE:
      newls.removeAll(found);
      break;
    default:
      logger.warn("OPlanWatcher() somehow caught random envelope type "+
		  action);
      return;
    }
  
    // MIK - deactive change marking.  Various other LPs misunderstand changed
    // orgs as relationship changes, leading to oplan repropagation, leading to
    // reactivation of this LP leading to a spiraling recursion of death.
    // The *right* thing to do is to publish with details, and change all the
    // other LPs to pay attention to what changed, only reacting when/as 
    // appropriate.  Yet another pre-demo hack.


    // RAY - Turned this back on since some plugins need to see
    // updated location schedules. The offending plugin
    // (PropagationPlugin) checks explicitly for
    // RelationshipSchedule.RelationshipScheduleChangeReport
    // ChangeReports and re-propagates IFF the changes include such a
    // ChangeReport


    // mark the object as having changed...maybe we should forward the changes?


    // Mark the org as only having changed local things
    // AssetReportPlugin - which is responsible for forwarding the changes to the org
    // to other agents - will look for this change report, and avoid propogating 
    // the change if it was only such LocalPG changes
    Collection changes = new ArrayList();
    changes.add(new LocalPG.LocalPGChangeReport());
    ((NewLocationSchedulePG)lspg).setSchedule(newls);
    rootplan.change(org, changes);
  }
}
