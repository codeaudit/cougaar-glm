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

import java.util.Date;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.util.Parameters;
import org.cougaar.util.TimeSpanSet;

import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.glm.ldm.plan.GeolocLocation;


/* Abstract base class for query handlers which are invoked on behalf of
 * SQLOplanPlugin.
 */
public abstract class SQLOplanQueryHandler extends QueryHandler {
  protected SQLOplanBase myPlugin;
               
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
    
  protected void initialize(LDMSQLPlugin ldmplugin,
                            MessageAddress cid,
                            ClusterServesPlugin comp,
                            PlanningFactory aldmf,
                            Properties params,
                            BlackboardService sub) {
    if (!(ldmplugin instanceof SQLOplanBase)) {
      throw new IllegalArgumentException("ldmplugin must be an SQLOplanBase");
    } else {
    myPlugin = (SQLOplanBase) ldmplugin;
      super.initialize(ldmplugin, cid, comp, aldmf, params, sub);
    }
  }
  
  protected void execute() {
    startQuery();                 // let the query have some state

    String q = getQuery();		
    myPlugin.executeSQL(q, this);
	  
    endQuery();                   // tell the query it is done.
  }

}




