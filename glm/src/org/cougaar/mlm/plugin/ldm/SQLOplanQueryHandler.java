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
import org.cougaar.planning.ldm.PlanningFactory;


/* Abstract base class for query handlers which are invoked on behalf of
 * OplanReaderPlugin
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




