/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN)  Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
  * @version $Id: QueryRequest.java,v 1.2 2001-03-22 14:13:48 ngivler Exp $
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
  
