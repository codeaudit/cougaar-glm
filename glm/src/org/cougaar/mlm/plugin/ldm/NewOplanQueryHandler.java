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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.util.DBProperties;


/** Reads oplan info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates oplan maintained by SQLOplanPlugin.
 */
public class NewOplanQueryHandler extends NewQueryHandler {
  private static final String QUERY_NAME = "OplanInfoQuery";

  public NewOplanQueryHandler(DBProperties adbp, NewOplanPlugin plugin) {
    super(adbp, plugin);
  }

  public Collection executeQueries(Statement statement) throws SQLException {
    String query = dbp.getQuery(QUERY_NAME, dbp);
    ResultSet rs = statement.executeQuery(query);
    Collection result = new ArrayList(1);
    while (rs.next()) {
      result.add(processRow(rs));
    }
    rs.close();
    if (result.isEmpty())
	System.err.println("Returning empty collection of oplans");
    return result;
  }

  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  private Oplan processRow(ResultSet rs) throws SQLException {
    try {
      String opName = getString(rs, 1);
      String priority = getString(rs, 2);
      String oplanId = dbp.getProperty(OplanReaderPlugin.OPLAN_ID_PARAMETER);
      Date cDay = plugin.getCDay();
      return new Oplan(null, // Filled in by caller
                       oplanId,
                       opName,
                       priority,
                       cDay);
    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
      return new Oplan();
    }
  }
}




