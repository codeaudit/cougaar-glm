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

import java.util.Properties;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.util.Parameters;


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
    ldm = ldmplugin.getLDMPlugin();
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
