/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import org.cougaar.util.OptionPane;

import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * Creates connection between client and XML Plan Server.
 */

public class ConnectionHelper {
  private URL url;
  private URLConnection connection;
  static final String DebugPSP_package = "alpine/demo";
  static final String DebugPSP_id = "DEBUG.PSP";
  String clusterURL;
  boolean isDebugPSP = false;

  /**
    * If you create a ConnectionHelper instance with 0 params.  must
    * call setConnection()... to intialize connection
    **/
    public ConnectionHelper() {
    }

  /**
    Connects to the debug PSP at the specified cluster,
    where cluster is specified by URL (host and port).
    */

  public ConnectionHelper(String clusterURL) throws MalformedURLException, IOException {
    this(clusterURL, DebugPSP_package, DebugPSP_id);
    isDebugPSP = true;
  }

  /**
    Connects to the specified PSP at the specified cluster,
    where cluster is specified by URL (host and port).
    */

  public ConnectionHelper(String clusterURL, String PSP_package, String PSP_id) throws MalformedURLException, IOException {
    this.clusterURL = clusterURL;
    url = new URL(clusterURL + PSP_package + "/" + PSP_id);
    connection = url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
  }

  /**
    Connects to the specified PSP at the specified cluster,
    where cluster is specified by URL (host and port).
    */

  public ConnectionHelper(String clusterURL, PSPConnectionInfo info,
                          boolean isApplet)
      throws MalformedURLException, IOException
    {
        this(clusterURL, info, isApplet, "");
    }

  public ConnectionHelper(String clusterURL, PSPConnectionInfo info,
                          boolean isApplet, String suffix)
    throws MalformedURLException, IOException {
    this.clusterURL = clusterURL;
    if (isApplet) {
      url = new URL(clusterURL + "/" + info.getPSPId() + suffix);
    } else {
      url = new URL(clusterURL + info.getPSPPackage() + "/" + info.getPSPId() + suffix);
    }
    System.out.println("url=" + url);
    connection = url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
  }

  public void setConnection(String completeURLString) throws Exception {
      if( connection != null ) {
          new RuntimeException("ConnectionHelper.connection already set!");
      }
      clusterURL = completeURLString; // messy -- but should be okay for now
      url = new URL(completeURLString);
      connection = url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
  }

  /**
   * getURL - returns URL for the connection
   *
   * @return URL for the connection
   */
  public URL getURL() {
    return url;
  }

  /**
    Sends data on the connection.
   */

  public void sendData(String s) throws IOException {
    PrintWriter pw = new PrintWriter(connection.getOutputStream());
    pw.println(s);
    pw.flush();
  }

  /**
    Returns input stream for the connection.
    */

  public InputStream getInputStream() throws IOException {
    return connection.getInputStream();
  }

    public URLConnection getConnection() {
        return connection;
    }

  /**
    Sends data and returns response in buffer.
    */

  public byte[] getResponse() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = getInputStream();
    byte b[] = new byte[512];
    int len;
    while( (len = is.read(b,0,512)) > -1 )
      os.write(b,0,len);
    return os.toByteArray();
  }

  /**
    Returns a list of cluster identifiers from the debug PSP
    located at the cluster specified in this class's constructor.
    */

  public Hashtable getClusterIdsAndURLs() throws MalformedURLException, ClassNotFoundException, IOException {
    ConnectionHelper debugConnection;
    Hashtable results = new Hashtable();

    try {
      if (!isDebugPSP) {
        int i = clusterURL.lastIndexOf(":");
        if (i == -1)
          throw new MalformedURLException("Expected host:port");
        debugConnection = new ConnectionHelper(clusterURL.substring(0,i+1) + "5555/");
      } else
        debugConnection = this;
      debugConnection.sendData("LIST_CLUSTERS");
      ObjectInputStream ois = 
        new ObjectInputStream(debugConnection.getInputStream());
      while (true) {
        String nextName = (String)ois.readObject();
        String nextURL = (String)ois.readObject();
        results.put(nextName, nextURL);
      }
    } catch (java.io.EOFException e) {
      // ignore this one
    }
    return results;
  }

  /**
   * getClusterInfo - get the location of the cluster
   * Only used when running in an application
   */
  public static String getClusterHostPort(java.awt.Component parent) {
      return getClusterHostPort(parent, "Enter location of a cluster.");
  }

  public static String getClusterHostPort(java.awt.Component parent, Object msg) {
      return (String)
          OptionPane.showInputDialog(parent,
                                     msg,
                                     "Cluster Location",
                                     OptionPane.QUESTION_MESSAGE,
                                     null, null, "localhost:5555");
  }

  public static Hashtable getClusterInfo(java.awt.Component parent) {
      return getClusterInfo(parent, "Enter location of a cluster.");
  }

  public static Hashtable getClusterInfo(java.awt.Component parent, Object msg) {
    String host = getClusterHostPort(parent, msg);
    if (host == null) {
      return null;
    }
    try {
      host = host.trim();
      if (host.length() == 0) {
        host = "localhost:5555";
      } else {
        if (host.indexOf(':') < 0) {
          host += ":5555";
        }
      }
      ConnectionHelper connection = new ConnectionHelper("http://" + host + "/");
      return connection.getClusterIdsAndURLs();
    } catch (Exception e) {
      return null;
    }
  }
}

