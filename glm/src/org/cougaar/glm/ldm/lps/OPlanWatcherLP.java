/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.glm.ldm.lps;

import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.planning.ldm.plan.Directive;
import org.cougaar.planning.ldm.plan.TransferableAssignment;
import org.cougaar.planning.ldm.plan.TransferableRescind;
import org.cougaar.planning.ldm.plan.TransferableTransfer;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.planning.ldm.plan.NewTransferableRescind;
import org.cougaar.planning.ldm.plan.TransferableVerification;

import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanContributor;
import org.cougaar.glm.ldm.oplan.OrgActivity;

import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.plan.*;

import java.util.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.core.util.UID;



/**
 * OPlanWatcherLP tracks OPlan changes, reconciling other objects to the OPlan(s)
 **/

public class OPlanWatcherLP extends LogPlanLogicProvider
  implements EnvelopeLogicProvider
{
  public OPlanWatcherLP(LogPlanServesLogicProvider logplan,
                        ClusterServesLogicProvider cluster) {
    super(logplan,cluster);
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

    Organization org = null;
    Enumeration enum = logplan.searchLogPlan(new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o instanceof Organization) {
            return orgID.equals(((Organization)o).getMessageAddress().toString());
          } 
          return false;
        }});
    if (enum != null && enum.hasMoreElements()) {
      org = (Organization)enum.nextElement();
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
      System.err.println("OPlanWatcher("+cluster.getMessageAddress()+
                         ") found non org asset "+a+" in logplan by name: "+orgID);
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
    Schedule ls = lspg.getSchedule();
    if (ls == null) {
      ls = ldmf.newLocationSchedule(EmptyEnumeration.getEnumeration());
      ((NewLocationSchedulePG)lspg).setSchedule(ls);
    }

    // now that we have it, lock it so nobody bashes it
    synchronized (ls) {
      final UID oaUID = oa.getUID();  // the uuid of the org activity.
      
      // find old element(s) in ls with matching oaUIDs
      Collection found = ls.filter(new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o instanceof OrgActivity.OAScheduleElement) {
            return oaUID.equals(((OrgActivity.OAScheduleElement)o).getOrgActivityUID());
          } else {
            return false;
          }
        }
      });

      switch (action) {
      case Envelope.ADD:        // 
        {
          if (found.size()>0) {
            System.err.println("Warning: OPlanWatcher saw redundant OrgActivity add for "+
                               oa);
          }
          ScheduleElement se = oa.getNormalizedScheduleElement();
          if (se != null) {
            ls.add(se);
          }
        }
        break;
      case Envelope.CHANGE:
        {
          ls.removeAll(found);
          ScheduleElement se = oa.getNormalizedScheduleElement();
          if (se != null) {
            ls.add(se);
          }
        }
        break;
      case Envelope.REMOVE:
        ls.removeAll(found);
        break;
      default:
        System.err.println("Warning: OPlanWatcher somehow caught random envelope type "+
                           action);
        return;
      }
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
    logplan.change(org, null);  

  }
}
