/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import org.cougaar.util.*;
import java.sql.*;

/** @deprecated use DBConnectionPool. **/
public class LDMConnectionDriver 
{
  private String driver;
  private String url;
  private String user;
  private String password;
  private String queryFile;
   
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
  }

  public boolean isWorking()
  {
    return true;
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
   
    //conn = pool.getConnection();
    //if (conn == null)
    // wrkStatus = false;
    // return (conn);

    try {
      return DBConnectionPool.getConnection(url, user, password);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public synchronized void closeAllConnections() { }
}
