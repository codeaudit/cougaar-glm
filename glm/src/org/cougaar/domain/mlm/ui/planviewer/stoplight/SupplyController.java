/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.data.*;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.XMLClientConfiguration;

/**
 * Displays stoplight table with drill down.
 * <p>
 * Runs the transport or supply stoplight display.
 */

public class SupplyController {
  static final int DISPLAY_WIDTH = 600;
  static final int DISPLAY_HEIGHT = 400;
  static final String REQUEST_TRANSPORT = "Transport";
  static final String REQUEST_SUPPLY = "Supply";
  Society society;
  SocietyStatus societyStatus;
  String PSP_package = XMLClientConfiguration.PSP_package;
  String PSP_id = "STOPLIGHT.PSP";
  static final String[] clustersToQuery =
        { "3ID", "1BDE-3ID", "2BDE-3ID", "3BDE-3ID", "AVNBDE-3ID", "DIVARTY-3ID",
          "ENGBDE-3ID", "3-7-INFBN", "3-69-ARBN", "2-7-INFBN", "3-15-INFBN",
          "4-64-ARBN", "1-64-ARBN" , "2-69-ARBN", "1-30-INFBN", "1-15-INFBN",
          "3-7-CAVSQDN", "1-3-AVNBN", "2-3-AVNBN", "1-9-FABN", "1-41-FABN",
          "1-10-FABN", "10-ENGBN", "317-ENGBN", "11-ENGBN"};


  /** Get cluster host and port from user.
    Get query from user:
    Transportation (sends Transport to TRANSCOM)
    Supply (sends Supply to all clusters)
   */


    public SupplyController() {
        String host = getClusterHostFromUser();
        String request = getRequestFromUser();
        if (request == null)
            System.exit(0);
        worker(request, host);
    }

    /** User supplied request or filename.
      If request, get hostname from user.
      If filename, just read data from file.
     */

    public SupplyController(String s) {
      if (s.equals(REQUEST_TRANSPORT) ||
          s.equals(REQUEST_SUPPLY)) {
        String host = getClusterHostFromUser();
        worker(s, host);
      } else
        displayDataFromFile(s);
    }

    /** Cluster location and request supplied on command line.
     */

  public SupplyController(String request, String host) {
      worker(request, host);
  }

  private void displayDataFromFile(String filename) {
    Hashtable status = null;
    try {
      FileInputStream f = new FileInputStream(filename);
      if (f == null) {
        displayErrorString("Could not read file: " + filename);
        return;
      }
      ObjectInputStream p = new ObjectInputStream(f);
      status = (Hashtable)p.readObject();
    } catch (Exception e) {
      System.out.println("Object read exception: " + e);
      displayErrorString("Could not read file: " + filename);
      return;
    }
    SupplyStoplight s = new SupplyStoplight(status);
    if (s != null)
      displayStoplight(s);
  }

  private void worker(String request, String host) {
    Vector clusters = new Vector(clustersToQuery.length);
    for (int i = 0; i < clustersToQuery.length; i++)
      clusters.addElement(clustersToQuery[i]);
    Vector clusterURLs = null;
    Hashtable clusterHT = getClusterList("http://" + host + ":5555/");
    if (request.equals(REQUEST_TRANSPORT)) {
      String clusterURL = (String)clusterHT.get("TRANSCOM");
      if (clusterURL != null) {
        clusterURLs = new Vector(1);
        clusterURLs.addElement(clusterURL);
      } else {
        displayErrorString("Transport status is obtained from TRANSCOM; this cluster is not in the society");
        System.exit(0);
      }
    } else if (request.equals(REQUEST_SUPPLY)) {
      clusterURLs = new Vector();
      for (int i = 0; i < clusters.size(); i++) {
        Object obj = clusterHT.get(clusters.elementAt(i));
        if (obj != null)
          clusterURLs.addElement(obj);
      }
    } else {
      System.out.println("Unknown request: " + request);
      System.exit(0);
    }
    Hashtable status = getDataFromClusters(clusterURLs, request);
    if (status != null) {
      SupplyStoplight s = new SupplyStoplight(status,
                                              clusterURLs, 
                                              PSP_package, PSP_id, request,
                                              false);
      if (s != null)
        displayStoplight(s);
    }
  }

  /** Used when run as an application to display the stoplight
    chart in a window.
    */

  private void displayStoplight(SupplyStoplight supplyStoplight) {
    JFrame f = new JFrame("Stoplight");
    f.getContentPane().add(supplyStoplight);
    f.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation(screenSize.width/2 - f.getWidth()/2,
                  screenSize.height/2 - f.getHeight()/2);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    f.setVisible(true);
  }

  /** Called from applet.
    For transport requests, we just contact the TRANSCOM cluster.
    Parse the code base which is of the form
    http://hostname:port/alpine/demo/
    and convert it to
    http://hostname:port/$TRANSCOM
    For supply requests, get the list of all clusters,
    and send a request to each cluster.
   */

  public SupplyController(URL codeBase, Container container,
                          String request) {
    Vector clusters = new Vector(clustersToQuery.length);
    for (int i = 0; i < clustersToQuery.length; i++)
      clusters.addElement(clustersToQuery[i]);
    ThemeFactory.establishMetalTheme();
    Vector clusterURLs = null;
    String hostName = codeBase.getHost();
    String port = String.valueOf(codeBase.getPort());
    if (request.equals(REQUEST_TRANSPORT)) {
      String clusterURL = "http://" + hostName + ":" + port +
        "/$TRANSCOM/";
      clusterURLs = new Vector(1);
      clusterURLs.addElement(clusterURL);
    } else if (request.equals(REQUEST_SUPPLY)) {
      Hashtable clusterHT = getClusterList("http://" + hostName + ":" + port + "/");
      clusterURLs = new Vector();
      for (int i = 0; i < clusters.size(); i++) {
        Object obj = clusterHT.get(clusters.elementAt(i));
        if (obj != null)
          clusterURLs.addElement(obj);
      }
    } else {
      System.out.println("Unknown request: " + request);
      System.exit(0);
    }
    Hashtable status = getDataFromClusters(clusterURLs, request);
    if (status != null) {
      SupplyStoplight s = new SupplyStoplight(status,
                                              clusterURLs, 
                                              PSP_package, PSP_id, request,
                                              true);
      if (s != null)
        container.add(s);
    }
  }

  /*
    Before sending the first request, ask the user for the location
    of a cluster.
  */
  
  private String getClusterHostFromUser() {
    String clusterHost = "localhost";
    String clusterPort = "5555";
    String s = (String)JOptionPane.showInputDialog(null,
      "Enter location of a cluster Log Plan Server in the form host:port",
                   "Cluster Location",
                    JOptionPane.INFORMATION_MESSAGE,
                    null, null, "localhost:5555");
    if (s == null)
      System.exit(0); // if we don't know where the clusters are located, quit
    s = s.trim();
    if (s.length() != 0) {
      int i = s.indexOf(":");
      if (i != -1) {
        clusterHost = s.substring(0, i);
        clusterPort = s.substring(i+1);
      }
    }
    //    return "http://" + clusterHost + ":" + clusterPort + "/";
    return clusterHost;
  }

  private String getRequestFromUser() {
    Object[] choices = { REQUEST_TRANSPORT, REQUEST_SUPPLY };
    return (String)JOptionPane.showInputDialog(null, "Show status of", 
                                               "Status",
                                               JOptionPane.INFORMATION_MESSAGE,
                                               null, choices, choices[0]);
  }

  private Hashtable getClusterList(String hostAndPort) {
    Hashtable clusterURLs = null;
    try {
      ConnectionHelper connection = new ConnectionHelper(hostAndPort);
      clusterURLs = connection.getClusterIdsAndURLs();
      if (clusterURLs == null) {
        displayErrorString("No clusters");
        System.exit(0);
      }
    } catch (Exception e) {
      //      displayErrorString(e.toString());
      System.out.println(e.toString());
      System.exit(0);
    }
    return clusterURLs;
  }

  private Hashtable getDataFromClusters(Vector clusterURLs, String request) {
    InputStream is = null;
    Hashtable status = null;
    Hashtable totalStatus = null;
    for (int i = 0; i < clusterURLs.size(); i++) {
      String clusterURL = (String)clusterURLs.elementAt(i);
      System.out.println("Getting " + request + " from cluster: " + clusterURL);
      try {
        ConnectionHelper connection = 
          new ConnectionHelper(clusterURL, PSP_package, PSP_id);
        connection.sendData(request);
        is = connection.getInputStream();
        ObjectInputStream p = new ObjectInputStream(is);
        status = (Hashtable)p.readObject();
      } catch (Exception e) {
        System.out.println("Object read exception: " + e);
      }
      System.out.println("Received results");
      if (status == null) {
        System.out.println("No data");
        continue;
      }
      if (status.isEmpty()) {
        System.out.println("No data");
        continue;
      }
      //      printMetrics(status);
      if (totalStatus == null)
        totalStatus = status;
      else
        totalStatus.putAll(status);
    }
    return totalStatus;
  }

  private void printMetrics(Hashtable status) {
    Enumeration keys = status.keys();
    while (keys.hasMoreElements()) {
      UILateStatus uil = (UILateStatus)status.get(keys.nextElement());
      System.out.println(uil.getUnitName() + " " +
                         uil.getEquipmentName() + " " +
                         uil.getEquipmentNomenclature());
      UIStoplightMetrics metrics = uil.getStoplightMetrics();
      ArrayList al = metrics.get();
      for (int i = 0; i < al.size(); i++)
        System.out.println("Quantity: " + al.get(i) +
                           " Days Late: " + i);
    }
  }

  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

    /** If no arguments, query for cluster location
        and request (Transport or Supply); 
        if one argument, it's the request or a filename (of saved data)
        if two arguments, they're the request and cluster location
    */

  public static void main(String[] args) { 
    ThemeFactory.establishMetalTheme();
    if (args.length == 0)
        new SupplyController();
    else if (args.length == 1)
        new SupplyController(args[0]); // specified request or filename
    else if (args.length == 2)
        new SupplyController(args[0], args[1]); // specified request & hostname
    else
        new SupplyController();
  }
}
