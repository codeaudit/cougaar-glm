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



