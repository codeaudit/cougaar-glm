/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.supply;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.data.UISupplyStatus;
import org.cougaar.domain.mlm.ui.data.UIUnitStatus;
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
  String PSP_id = "EQUIPMENTTRANSPORT.PSP";
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
    Vector clusters = new Vector(clustersToQuery.length);
    for (int i = 0; i < clustersToQuery.length; i++)
      clusters.addElement(clustersToQuery[i]);
    Vector clusterURLs = null;
    Hashtable clusterHT = getClusterList(getClusterHostFromUser());
    String request = getRequestFromUser();
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
    SupplyStoplight s = doDisplay(null, clusterURLs, request);
    if (s != null)
      displayStoplight(s);
  }

  /** Used when run as an application to display the stoplight
    chart in a window.
    */
  private void displayStoplight(SupplyStoplight supplyStoplight) {
    JFrame f = new JFrame("Stoplight");
    f.getContentPane().add(supplyStoplight);
    f.setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation(screenSize.width/2 - DISPLAY_WIDTH/2, 
                  screenSize.height/2 - DISPLAY_HEIGHT/2);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    f.setVisible(true);
  }

  /** For debugging, run off of canned results; don't contact society.
   */

  public SupplyController(UISupplyStatus status) {
    SupplyStoplight s = doDisplay(status, null, REQUEST_TRANSPORT);
    if (s != null)
      displayStoplight(s);
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
      Hashtable clusterHT = getClusterList(hostName + ":" + port);
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
    SupplyStoplight s = doDisplay(null, clusterURLs, request);
    if (s != null)
      container.add(s);
  }

  /** Common code to put up the frame.
   */

  private SupplyStoplight doDisplay(UISupplyStatus status, Vector clusterURLs, 
                                    String request) {
    SupplyStoplight supplyStoplight = null;
    if (clusterURLs != null)
      supplyStoplight = 
        displayStatuses(getDataFromClusters(clusterURLs, request),
                        clusterURLs, request);
    else
      supplyStoplight = displayStatuses(status, clusterURLs, request);
    return supplyStoplight;
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
    return "http://" + clusterHost + ":" + clusterPort + "/";
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

  /** Use StatusHelper to extract society, equipment and status
    info and then display that in a stoplight chart.
p    */

  private SupplyStoplight displayStatuses(UISupplyStatus status, 
                                          Vector clusterURLs, String request) {
    if (status == null) {
      displayErrorString("No status returned from society");
      return null;
    }
    if (status.getUnitStatuses().size() == 0) {
      displayErrorString("No status returned from society");
      return null;
    }
    StatusHelper helper = new StatusHelper(status);
    // pass PSP info to stoplight chart so that it can do updates
    return new SupplyStoplight(helper.getSociety(),
                               helper.getEquipmentInfo(),
                               helper.getSocietyStatus(),
                               clusterURLs, PSP_package, PSP_id, request);
  }

  private UISupplyStatus getDataFromClusters(Vector clusterURLs, 
                                             String request) {
    InputStream is = null;
    UISupplyStatus status = null;
    UISupplyStatus bnStatus = null;
    for (int i = 0; i < clusterURLs.size(); i++) {
      String clusterURL = (String)clusterURLs.elementAt(i);
      System.out.println("Getting status from cluster: " + clusterURL);
      try {
        ConnectionHelper connection = 
          new ConnectionHelper(clusterURL, PSP_package, PSP_id);
        connection.sendData(request);
        is = connection.getInputStream();
        ObjectInputStream p = new ObjectInputStream(is);
        if (status == null)
          status = (UISupplyStatus)p.readObject();
        else
          bnStatus = (UISupplyStatus)p.readObject();
      } catch (Exception e) {
        //      displayErrorString("Object read exception: " + e);
        System.out.println("Object read exception: " + e);
        bnStatus = null;
      }
      System.out.println("Received results");
      // add this battalion's status to statuses from entire society
      if (bnStatus != null) {
        Vector bnStatuses = bnStatus.getUnitStatuses();
        for (int j = 0; j < bnStatuses.size(); j++)
          status.addUnitStatus((UIUnitStatus)bnStatuses.elementAt(j));
      }
    }
    // for debugging
    //    Vector v = status.getUnitStatuses();
    //    for (int i = 0; i < v.size(); i++) {
    //      UIUnitStatus s = (UIUnitStatus)v.elementAt(i);
    //      System.out.println("Status: " + s.getUnitName() + " " +
    //                   s.getEquipmentName() + " " +
    //                   s.getEquipmentNomenclature() + " " + s.getStatus());
    //    }
    return status;
  }

  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

  public static void main(String[] args) { 
    ThemeFactory.establishMetalTheme();
    // for debugging
    if (args.length != 0) {
      if (args[0].equals("-debug")) {
        UISupplyStatus t = new UISupplyStatus();
        t.addUnitStatus(new UIUnitStatus("3-7-INFBN", "DF2", "Fuel-DF2", UIUnitStatus.GREEN));
        t.addUnitStatus(new UIUnitStatus("1BDE-3ID", "DF2", "Fuel-DF2", UIUnitStatus.RED));
        t.addUnitStatus(new UIUnitStatus("1-64-ARBN", "JP5", "Fuel-JP5", UIUnitStatus.YELLOW));
        t.addUnitStatus(new UIUnitStatus("317-ENGBN", "JP8", "Fuel-JP8", UIUnitStatus.YELLOW));
        t.addUnitStatus(new UIUnitStatus("3-7-INFBN", "M198 Howitzer Towed 155MM", "Howitzer", UIUnitStatus.GREEN));
        t.addUnitStatus(new UIUnitStatus("1-64-ARBN", "Light Armored Vehicle", "Armor", UIUnitStatus.YELLOW));
        t.addUnitStatus(new UIUnitStatus("2BDE-3ID", "Light Armored Vehicle", "Armor", UIUnitStatus.RED));
        t.addUnitStatus(new UIUnitStatus("2BDE-3ID", "M198 Howitzer Towed 155MM", "Howitzer", UIUnitStatus.RED));
        t.addUnitStatus(new UIUnitStatus("317-ENGBN", "Truck Cargo 5 Ton", "Truck", UIUnitStatus.RED));
        t.addUnitStatus(new UIUnitStatus("1-3-AVNBN", "DODIC/H163", "Ammo-H163", UIUnitStatus.GREEN));
        t.addUnitStatus(new UIUnitStatus("10-ENGBN", "DODIC/A131", "Ammo-A131", UIUnitStatus.YELLOW));
        t.addUnitStatus(new UIUnitStatus("1-10-FABN", "DODIC/C380", "Ammo-C380", UIUnitStatus.RED));
        t.addUnitStatus(new UIUnitStatus("1-10-FABN", "NSN/5632", "Stuff-5632", UIUnitStatus.YELLOW));
        t.addUnitStatus(new UIUnitStatus("3ID", "NSN/5632", "Stuff-5632", UIUnitStatus.RED));
        t.addUnitStatus(new UIUnitStatus("3-7-CAVSQDN", "NSN/5632", "Stuff-5632", UIUnitStatus.RED));
        new SupplyController(t);
      } 
    } else
      new SupplyController();
  }
}
