/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;


import java.util.*;
import java.io.*;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.Filters;

import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.society.UID;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AssetGroup;
import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.domain.glm.ldm.GLMFactory;
import org.cougaar.domain.glm.ldm.plan.QueryRequest;
import org.cougaar.domain.glm.ldm.oplan.*;

import org.cougaar.lib.util.UTILAsset;

public class GetOplanPlugIn extends SimplePlugIn {

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
    alpFactory = (GLMFactory)getFactory("alp");

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
	  if (!ow.getOwner().equals(getCluster().getClusterIdentifier())) {
	    requestOplan(ow);
	  }
	}
      }
      // Don't be clever about changes for now, just get the whole thing again.
      Collection changes = oplanCoupons.getChangedCollection();
      if (changes != null) {
	for (Iterator changeIterator = changes.iterator(); changeIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) changeIterator.next();
	  if (!ow.getOwner().equals(getCluster().getClusterIdentifier())) {
	    requestOplan(ow);
	  }
	}
      }
      Collection deletes = oplanCoupons.getRemovedCollection();
      if (deletes != null) {
	for (Iterator delIterator = deletes.iterator(); delIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) delIterator.next();
	  if (!ow.getOwner().equals(getCluster().getClusterIdentifier())) {
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
    String orgID = getClusterIdentifier().toString();
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
				      getClusterIdentifier());
    } catch (RuntimeException e) {
      e.printStackTrace();
      return;
    }
    if (qr == null){
    } else {
      publishAdd(qr);
      //System.out.println("GetOplanPlugIn: published QueryRequest");
    }
  }

  /**
   * Removes all matching Oplan components from the logplan
   **/
  private void removeOplan(OplanCoupon ow) {

    // BOGUS See comment above
    String orgID = getClusterIdentifier().toString();
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



