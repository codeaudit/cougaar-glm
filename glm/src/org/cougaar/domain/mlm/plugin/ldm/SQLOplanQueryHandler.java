/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import java.util.Date;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.util.Parameters;
import org.cougaar.util.TimeSpanSet;

import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.ldm.oplan.TimeSpan;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

/* Abstract base class for query handlers which are invoked on behalf of
 * SQLOplanPlugIn.
 */
public abstract class SQLOplanQueryHandler extends QueryHandler {
  protected SQLOplanPlugIn myPlugIn;
               
  /** Called on QueryHandler load to allow it to start any
   * threads or other once-only intialization.
   * It is a good idea for subclasses to call their super.start() if
   * they override it.
   **/
  public void start() {
    super.start();
    execute();
  }

  public void update() {
    execute();
  }
    
  protected void initialize(LDMSQLPlugIn ldmplugin,
                            ClusterIdentifier cid,
                            ClusterServesPlugIn comp,
                            RootFactory aldmf,
                            Properties params,
                            Subscriber sub) {
    if (!(ldmplugin instanceof SQLOplanPlugIn)) {
      throw new IllegalArgumentException("ldmplugin must be an SQLOplanPlugIn");
    } else {
      myPlugIn = (SQLOplanPlugIn) ldmplugin;
      super.initialize(ldmplugin, cid, comp, aldmf, params, sub);
    }
  }
  
  protected void execute() {
    startQuery();                 // let the query have some state

    String q = getQuery();		
    myPlugIn.executeSQL(q, this);
	  
    endQuery();                   // tell the query it is done.
  }

}




