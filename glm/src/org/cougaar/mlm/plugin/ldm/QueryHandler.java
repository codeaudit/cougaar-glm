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

import org.cougaar.util.StateModelException;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.util.Parameters;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;


public abstract class QueryHandler {
  public QueryHandler() {}
	
  protected LDMSQLPlugin myLDMPlugin;
  protected MessageAddress myMessageAddress;
  protected ClusterServesPlugin myComponent;
  protected PlanningFactory ldmf;
  protected Properties myParameters;
  protected BlackboardService subscriber;
  protected LDMServesPlugin ldm;

  protected void initialize( LDMSQLPlugin ldmplugin,
                             MessageAddress cid,
                             ClusterServesPlugin comp,
                             PlanningFactory aldmf,
                             Properties params,
                             BlackboardService sub) {
    myLDMPlugin = ldmplugin;
    ldm = comp.getLDM();
    myMessageAddress = cid;
    myComponent = comp;
    ldmf = aldmf;
    myParameters = params;
    subscriber = sub;
  }
	
  protected LDMServesPlugin getLDM() {
    return ldm;
  }

  /** Called on QueryHandler load to allow it to start any
   * threads or other once-only intialization.
   * It is a good idea for subclasses to call their super.start() if
   * they override it.
   **/
  public void start() {}

  /** use this method to find the values of parameters **/
  public final String getParameter(String parameterKey) {
    String val = myParameters.getProperty(parameterKey);
    return Parameters.replaceParameters(val, myParameters);
  }

  /** this method is called before a query is started,
   * before even getQuery.  The default method is empty
   * but may be overridden.
   **/
  public void startQuery() {}
                        
  /** Construct and return an SQL query to be used by the Database engine.
   * Subclasses are required to implement this method.
   **/
  public abstract String getQuery();
    
  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  public abstract void processRow(Object[] rowdata);

  /** this method is called when a query is complete, 
   * afer the last call to processRow.  The default method is empty
   * but may be overridden by subclasses.
   **/
  public void endQuery() {}

  protected void publishAdd(Object o) {
    subscriber.publishAdd(o);
  }
  protected void publishRemove(Object o) {
    subscriber.publishRemove(o);
  }
  protected void publishChange(Object o) {
    subscriber.publishChange(o);
  }

}
