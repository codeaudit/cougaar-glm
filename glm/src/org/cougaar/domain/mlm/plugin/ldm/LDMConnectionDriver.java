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

import org.cougaar.util.*;
import java.sql.*;

/** @deprecated use DBConnectionPool. **/
public class LDMConnectionDriver 
{
  private String driver;
  private String url;
  private String user;
  private String password;
  private LDMConnectionPool pool;
  private String queryFile;
  private boolean wrkStatus = true;
   
  /** 
   * @param driver JDBC driver.
   * @param url
   * @param user
   * @param password
   * @param minPoolSize ignored
   * @param maxPoolSize ignored
   * @param timeout ignored
   * @param queryFile ignored
   * @param ntries passed to LDMConnectionPool
   * @deprecated use DBConnectionPool directly.
   **/
  public LDMConnectionDriver(String driver, 
                             String url, 
                             String user, 
                             String password,
                             int minPoolSize,
                             int maxPoolSize,
                             int timeout,
                             String queryFile,
                             int nTries)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
  {
    this.url = url;
    this.user = user;
    this.password = password;
    this.driver = driver;
   
    try {
      DBConnectionPool.registerDriver(driver);
    } catch (Exception e) {
      e.printStackTrace();
    }
    pool = new LDMConnectionPool(url, user, password, minPoolSize, maxPoolSize, timeout, nTries);  
   
  }

  public boolean isWorking()
  {
    return wrkStatus;
  }

  public String getDBName()
  {
    return url;
  }

  public String getQueryFile() 
  {
    return queryFile;
  }

  public Connection connect()
  {
    Connection conn;
   
    conn = pool.getConnection();
    if (conn == null)
      wrkStatus = false;

    return (conn);
   
  }

  public synchronized void closeAllConnections()
  {
  }// closeAllConnections


}// LDMConnectionDriver
