/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.UnaryPredicate;

/** QueryRequest Interface
  * 
  *
  * @author  ALPINE <alpine-software@bbn.com>
  * @version $Id: QueryRequest.java,v 1.3 2001-04-05 19:27:42 mthome Exp $
  */
public interface QueryRequest {
  
  /**
   * @return Predicate containing the query
   */
  UnaryPredicate getQueryPredicate();

  /**
   * @return Predicate containing the query
   */
  UnaryPredicate getLocalQueryPredicate();
  
  /** Gets the Cluster where the object resides
    * @return The Cluster the request is being sent to.  
    */
  ClusterIdentifier getSourceCluster();

  /** 
    * @return The ClusterIdentifier where the request originated
    */
  ClusterIdentifier getRequestingCluster();
  
}
  
