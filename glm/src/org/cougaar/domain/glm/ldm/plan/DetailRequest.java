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

import org.cougaar.core.society.UID;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.core.cluster.ClusterIdentifier;

/** DetailRequest Interface
  * 
  *
  * @author  ALPINE <alpine-software@bbn.com>
  * @version $Id: DetailRequest.java,v 1.2 2001-04-05 19:27:39 mthome Exp $
  */
public interface DetailRequest {
  
  /**
   * The UID of the object we want to retrieve
   */
  UID getDetailUID();
  
  /** Gets the Cluster where the object resides
    * @return The Cluster the request is being sent to.  
    */
  ClusterIdentifier getSourceCluster();

  /** 
    * @return The ClusterIdentifier where the request originated
    */
  ClusterIdentifier getRequestingCluster();
  
}
  
