/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.asset.NewClusterPG;

import org.cougaar.domain.planning.ldm.plan.AssetTransfer;
import org.cougaar.domain.planning.ldm.plan.Predictor;

import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.util.Enumerator;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.Constants;

import java.util.Collection;
import java.util.Iterator;

/**
 * MCCPredictorPlugIn - 
 * add MCCPredictor to the self org's ClusterPG
 * 
 * Waits to add predictor until after the self org
 * has reported for Service. Delay designed to 
 * test OrgReportPlugIn's support for resending asset
 * transfers when the self org changes.
 */
public class MCCPredictorPlugIn extends SimplePlugIn {
  private IncrementalSubscription orgAssets;
  
  private static UnaryPredicate orgAssetPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          return ((Organization) o).isSelf();
        }
        return false;
      }
    };
  }
  
  private void addPredictor(Enumerator orgs) {
    MCCPredictor predictor = new MCCPredictor();
    while (orgs.hasMoreElements()) {
      // We should only see one copy of self here
      Organization org = (Organization)orgs.nextElement();
      ((NewClusterPG)org.getClusterPG()).setPredictor(predictor);
      publishChange(org);
    }
  }
  
  public void setupSubscriptions() {
    orgAssets = (IncrementalSubscription)subscribe(orgAssetPred());
  }
  
  public void execute() {
    // New copy of myself (should occur only once per society)
    if (orgAssets.hasChanged()) {
      for (Iterator iterator = orgAssets.getCollection().iterator();
           iterator.hasNext();) {
        Organization selfOrg = (Organization) iterator.next();
        
        // Bail if we already have a predictor
        if (selfOrg.getClusterPG().getPredictor() == null) {
          // Wait to add predictor until after we have some customers
          Collection customers = 
            selfOrg.getRelationshipSchedule().getMatchingRelationships(Constants.RelationshipType.CUSTOMER_SUFFIX,
                                                                       TimeSpan.MIN_VALUE,
                                                                       TimeSpan.MAX_VALUE);

          if (!customers.isEmpty()) {
            addPredictor(new Enumerator(orgAssets.getCollection()));
            break;
          }
        }
      }
    }
  }
}





