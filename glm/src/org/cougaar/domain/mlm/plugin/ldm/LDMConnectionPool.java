/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.ldm;
import java.sql.*;
import java.util.*;
import org.cougaar.util.*;

/** Backward-Compatability adapter to new DBConnectionPool functionality.
 * @deprecated Use DBConnectionPool
 **/
public class LDMConnectionPool
{
  private String url;
  private String user;
  private String password;
  private int nTries;

  /**
   * This method serves as a constructor for the LDMConnectionPool class.
   * @param url url for the database.
   * @param user user to login to the database as.
   * @param password password to login to the database.
   * @param minSize ignored.
   * @param maxSize ignored.
   * @param timeout ignored.
   * @param nTries The number of times to retry a database connection before 
   * deeming that the connection is not accessible.
   **/
  public LDMConnectionPool (String url, String user, String password, int minSize, int maxSize, int timeout, int nTries) 
  {

    this.url = url;
    this.user = user;
    this.password = password;
    this.nTries = nTries;   
  }

  public synchronized void removeStaleConnections() 
  {
  }
   
  /** @deprecated Use DBConnectionPool.getConnection instead. **/
  public synchronized Connection getConnection() 
  {
    Exception savedException = null;
    for (int tries = 0; tries<nTries; tries++) {
      try {
        return DBConnectionPool.getConnection(url, user, password);
      } catch (SQLException e) {
        savedException = e;
      } catch (RuntimeException e) {
        savedException = e;
      }
      try {
        Thread.sleep(1*1000L);     // sleep for a sec and try again.
      } catch (InterruptedException e) {}
    }
    if (savedException != null) {
      System.err.println("LDMConnectionPool.getConnection() caught ("+nTries+"): "+savedException);
      savedException.printStackTrace();
    }
    return null;
  }
   
  public synchronized void closeAll()
  {
  }
   
  private synchronized void removeConnection(Connection conn) 
  {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
   
  protected synchronized void expireLease(Connection conn)
  {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
