/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.ldm;


import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanContributor;
import org.cougaar.glm.ldm.oplan.OplanCoupon;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.OrgRelation;
import org.cougaar.glm.ldm.plan.QueryRequest;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

//import org.cougaar.lib.util.UTILAsset;

public class GetOplanPlugin extends SimplePlugin {

  /** Subscription to hold collection of input tasks **/

  private Vector roles = new Vector();
  private String relation = null;
  
  private IncrementalSubscription orgActivities;
  private GLMFactory alpFactory;


  private IncrementalSubscription oplanCoupons;
  private static UnaryPredicate oplanCouponPred = new UnaryPredicate() {
    public boolean execute(Object o) { 
      if (o instanceof OplanCoupon) {
        return true;
      } else {
        return false;
      }
    }
  };

  protected void setupSubscriptions() {
    alpFactory = (GLMFactory)getFactory("glm");

    oplanCoupons = (IncrementalSubscription)subscribe(oplanCouponPred);
  }

  public synchronized void execute() {

    if (oplanCoupons.hasChanged()) {
      Collection adds = oplanCoupons.getAddedCollection();
      if (adds != null) {
	for (Iterator addIterator = adds.iterator(); addIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) addIterator.next();
	  // We shouldn't run the plugin in the same cluster where
	  // the oplan is published, but if we do, just ignore the coupon
	  if (!ow.getOwner().equals(getMessageAddress())) {
	    requestOplan(ow);
	  }
	}
      }
      // Don't be clever about changes for now, just get the whole thing again.
      Collection changes = oplanCoupons.getChangedCollection();
      if (changes != null) {
	for (Iterator changeIterator = changes.iterator(); changeIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) changeIterator.next();
	  if (!ow.getOwner().equals(getMessageAddress())) {
	    requestOplan(ow);
	  }
	}
      }
      Collection deletes = oplanCoupons.getRemovedCollection();
      if (deletes != null) {
	for (Iterator delIterator = deletes.iterator(); delIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) delIterator.next();
	  if (!ow.getOwner().equals(getMessageAddress())) {
	    removeOplan(ow);
	  }
	}
      }
    }
  }

  private void requestOplan(final OplanCoupon oplanCoupon) {

    // BOGUS We make the assumption that the _only_ organization in
    // this cluster has the same name as the cluster. This assumption
    // is false on both counts.
    String orgID = getMessageAddress().toString();
    OplanPredicate oplanPred = 
      new OplanPredicate(oplanCoupon.getOplanUID(), orgID);

    // Used to find local objects which originate from the original query -
    // used to reconcile results of the query with local objects.
    OplanPredicate localPred = 
      new OplanPredicate(oplanCoupon.getOplanUID(), orgID);
    QueryRequest qr;
    try {
      qr = alpFactory.newQueryRequest(oplanPred,
                                      localPred,
				      oplanCoupon.getHomeClusterID(),
				      getMessageAddress());
    } catch (RuntimeException e) {
      e.printStackTrace();
      return;
    }
    if (qr == null){
    } else {
      publishAdd(qr);
      //System.out.println("GetOplanPlugin: published QueryRequest");
    }
  }

  /**
   * Removes all matching Oplan components from the logplan
   **/
  private void removeOplan(OplanCoupon ow) {

    // BOGUS See comment above
    String orgID = getMessageAddress().toString();
    OplanPredicate op = new OplanPredicate(ow.getOplanUID(), orgID);

    Collection oplanObjs = query(op);
    for (Iterator it = oplanObjs.iterator(); it.hasNext();) {
      publishRemove(it.next());
    }
  }

  private static class OplanPredicate implements UnaryPredicate {
    private UID _oplanUID;
    private String _orgID;

    public OplanPredicate(UID oplanUID, String orgID) {
      _oplanUID = oplanUID;
      _orgID = orgID;
    }

    public boolean execute(Object o) {
      if (o instanceof Oplan) {
	if (((Oplan) o).getUID().equals(_oplanUID)) {
	  return true;
	}
	return false;
      }

      if (o instanceof OplanContributor) {
	OplanContributor oc = (OplanContributor) o;
	//System.out.println("Predicate: found OplanContributor");
	if (oc.getOplanUID().equals(_oplanUID))  {
	  //System.out.println("Predicate: has correct OplanUID " + oc.getOplanUID());
	  if (oc instanceof OrgActivity) {
	    if (((OrgActivity) oc).getOrgID().equals(_orgID)) {
	      //System.out.println("Predicate: correct OrgID in OrgActivity");
	      return true;
	    } else {
	      return false;
	    }
	  } else if (oc instanceof OrgRelation) {
	    if (((OrgRelation) oc).getOrgID().equals(_orgID)) {
	      return true;
	    } else {
	      return false;
	    }
	  }
	  //System.out.println("Predicate: found a good one! " + o);
	  return true;
	}
      }
      //System.out.println("Predicate: Sorry, play again soon");
      return false;
    }
  }
}



