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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

import org.cougaar.util.DBProperties;

/** For OrgCode based agents: Reads oplan info from a database table. 
 * Grabs active stages of parent in case there are none for this agent.
 */
public class OrgCodeActiveStagesQueryHandler extends NewActiveStagesQueryHandler {
  public static final String ORG_CODE = ":org_code:";

  private static final String QUERY_NAME = "ActiveStagesQuery";

  private int minRequiredStage = 0;

  public OrgCodeActiveStagesQueryHandler(DBProperties adbp, NewOplanPlugin plugin) {
    super(adbp, plugin);
  }

  public Collection executeQueries(Statement statement) throws SQLException {
    String myOrgCode = plugin.getOrgCode();  

    String myRollups = computeAllRollupSuperiors(myOrgCode);
    dbp.put(ORG_CODE, myRollups);

    String query = dbp.getQuery(QUERY_NAME, dbp);
    ResultSet rs = statement.executeQuery(query);
    if (rs.next()) {
      Collection result = Collections.singleton(processRow(rs));
      rs.close();
      return result;
    } else {
	System.out.println("No results from ActiveStagesQueryHandler");
      return Collections.EMPTY_SET;
    }
  }
    
  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  public Object processRow(ResultSet rs) {
    try {
      return (Number) rs.getObject(1);
    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
      return new Integer(0);
    }
  }

  // OrgCodes reflect hierarchy of agents in length of the code.
  // So my superiors code + 1 char at end is my code.
  // Hence I can get all of my superiors by iteratively chopping
  // off the last char on my orgCode
  private String computeAllRollupSuperiors(String myOrgCode) {
    StringBuffer sb = new StringBuffer("'" + myOrgCode + "'");
    
    // Spot to chop of String
    int chopLen = myOrgCode.length() - 1;
    // So we never enter this loop if orgCode was only 1 char
    while (chopLen > 0) {
      //shorten orgCode by 1 each time to get next rollup
      String nextRollup = myOrgCode.substring(0, chopLen);
      sb.append(", '" + nextRollup + "'");
      chopLen--;
    }
    return sb.toString();
  }
}




