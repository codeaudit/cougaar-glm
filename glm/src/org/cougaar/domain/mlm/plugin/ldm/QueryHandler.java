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

import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.util.Parameters;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;


public abstract class QueryHandler {
  public QueryHandler() {}
	
  protected LDMSQLPlugIn myLDMPlugIn;
  protected LDMQueryPlugIn myQueryLDMPlugIn;
  protected boolean LDMQueryType = false;
  protected ClusterIdentifier myClusterIdentifier;
  protected ClusterServesPlugIn myComponent;
  protected RootFactory ldmf;
  protected Properties myParameters;
  protected Subscriber subscriber;
  protected LDMServesPlugIn ldm;
	
  protected void initialize( LDMSQLPlugIn ldmplugin,
                             ClusterIdentifier cid,
                             ClusterServesPlugIn comp,
                             RootFactory aldmf,
                             Properties params,
                             Subscriber sub) {
    myLDMPlugIn = ldmplugin;
    ldm = comp.getLDM();
    myClusterIdentifier = cid;
    myComponent = comp;
    ldmf = aldmf;
    myParameters = params;
    subscriber = sub;
  }

  protected void initializeQH( LDMQueryPlugIn ldmplugin,
                               ClusterIdentifier cid,
                               ClusterServesPlugIn comp,
                               RootFactory aldmf,
                               Properties params,
                               Subscriber sub) {
    myQueryLDMPlugIn = ldmplugin;
    ldm = comp.getLDM();
    myClusterIdentifier = cid;
    myComponent = comp;
    ldmf = aldmf;
    myParameters = params;
    subscriber = sub;
    LDMQueryType = true;
  }

	
  protected LDMServesPlugIn getLDM() {
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
