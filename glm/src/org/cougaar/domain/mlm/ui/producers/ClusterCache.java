/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.XMLClientConfiguration;

/**
 * Connects to debug PSP to get list of clusters and their URLs
 * Then keeps table of producers created for each cluster, so views/consumers
 * can avoid duplicating producers.
 *
 * Caveat Emptor - has not been tested with more than one producer. Several of
 * the utility functions don't preform as advertised.  Would need to be 
 * rewritten if used in a less limited environment.
 */

public class ClusterCache {

  // Vector of valid cluster IDs
  static Vector clusterNames = null;
  // Hashtable mapping cluster IDs to URL
  static Hashtable clusterURLs = null;
  // Hashtable mapping cluster IDs to vector of active Producers that cluster
  static Hashtable clusterProducers = null;
  // If we're an applet, need to hold onto the codebase
  static URL codebase = null;
  
  
  /**
   * initCache -  initialize list of clusters
   * 
   * @param cb URL specifying host with logplan server. If null, defaults to
   * localhost.
   */
  public static void initCache(URL cb) {
    System.out.println("Connecting to COUGAAR logplan server");
    ConnectionHelper conn = null;
    clusterNames = new Vector();
    clusterURLs = new Hashtable();
    clusterProducers = new Hashtable();
    String url = "http://localhost:5558/";
    
    if (cb == null) {
      String host = (String)JOptionPane.showInputDialog(null,
                                                        "Enter location of a cluster.",
                                                        "Cluster Location",
                                                        JOptionPane.INFORMATION_MESSAGE,
                                                        null, null, "localhost");
      if (host == null)
        System.exit(0);
      host = host.trim();
      if (host.length() == 0)
        host = "localhost";
      url = "http://" + host + ":5558/";
    } else {
      codebase = cb;
    }
    try {
      conn = new ConnectionHelper(url,
                                  XMLClientConfiguration.PSP_package,
                                  XMLClientConfiguration.DebugPSP_id);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
    }
    try {
      clusterURLs = conn.getClusterIdsAndURLs();
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
    }
    Enumeration keys = clusterURLs.keys();
    while (keys.hasMoreElements()) {
      String cluster = (String)keys.nextElement();
      System.out.println(cluster + " " + clusterURLs.get(cluster));
      clusterNames.add(cluster);
    }
  }
  
  /**
   * isApplet - returns true if running within in an applet.
   *
   * @return boolean - True is running in an applet, else false.
   */
  public static boolean isApplet() {
    return (codebase != null); 
  } 
  
  /**
   * getCodeBase - returns code base if running inside an applet.
   *
   * @return URL code base, null if running as an application.
   */
  public static URL getCodeBase() {
    return codebase;
  }
  
  /**
   * getClusterNames - returns all cluster names
   * 
   * @return Vector of cluster names
   */
  public static Vector getClusterNames() {
    return (Vector)clusterNames.clone();
  }
  
  /**
   * getClusterURL - returns the URL associated with a cluster name,
   *
   * @param clusterName String specifying the name of the cluster
   * @return associated URL
   */
  public static String getClusterURL(String clusterName) {
    if (clusterURLs!=null) {
      return (String)clusterURLs.get(clusterName);
    }
    return null;
  }
  
  /**
   * isValidClusterName - returns boolean indicating whether or
   * not cache contains info on the specified cluster name.
   *
   * @param clusterName String specifying the cluster name to 
   * check for.
   * @return boolean - true if cache has info on the cluster, else false
   */
  public static boolean isValidClusterName(String clusterName) {
    if (clusterNames!=null) {
      if (clusterNames.indexOf(clusterName)>=0) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * addProducerForCluster - add an MLMProducer
   * N.B. - no specification for what the producer produces. Not very
   * useful in a multi-producer environment.
   *
   * @param clusterName String specifying the target cluster
   * @param prod MLMProducer to be added
   */
  public static void addProducerForCluster(String clusterName, 
                                           MLMProducer prod) {
    Vector prods = (Vector)clusterProducers.get(clusterName);

    if (prods == null) {
      prods = new Vector();
      prods.add(prod);
      clusterProducers.put(clusterName,prods);
    } else {
      // BOZO - should check for duplicates.
      prods.add(prod);
    }
  }
  
  /**
   * getClusterProducers - returns all producers for the specified cluster.
   *
   * @param clusterName String specifying the target cluster
   * @return Vector of MLMProducers for the target cluster
   */
  public static Vector getClusterProducers(String clusterName) {
    Vector producers;
    if (clusterProducers != null) {
      producers = (Vector)clusterProducers.get(clusterName);
    } else {
      producers = null;
    }

    if (producers != null) {
      producers = (Vector)producers.clone();
    }
    
    return producers;
  }
  
  /**
   * getClusterProducer - return MLMProducer with the specified name
   * Intended to allow full class name or just the in-package class name
   *
   * BOZO - implementation of above takes match if the class name of the 
   * MLMProducer includes the specified className, i.e. MLMProducerSamantha
   * could be returned if MLMProducerSam is specified. Doesn't seem important
   * to fix for now since no one's using the functionality.
   * 
   * Why do we want to match on the in-package class name anyway?
   *
   * @param clusterName String specifying the cluster
   * @param className String specifying the class of the MLMProducer
   * @return MLMProducer which matches the specified className. null if no
   * match found.
   */
  public static MLMProducer getClusterProducer(String clusterName,
                                               String className) {
    Vector prods = getClusterProducers(clusterName);
    // return first producer that matches classname
    if (prods!=null) {
      for (int i = 0; i<prods.size(); i++) {
        if (prods.get(i).getClass().getName().indexOf(className) > 0) {
          return (MLMProducer)prods.get(i);
        }
      }
    }
    return null;
  }
  
  /**
   * producerExits - returns true if an MLMProducer with the specified
   * class name exists.
   *
   * BOZO - uses getClusterProducer() so is prone to all the same problems.
   *
   * @param clusterName String specifying the target cluster
   * @param className String specifying the MLMProducer class.
   * @return boolean true if MLMProducer found, else false
   */
  public static boolean producerExists(String clusterName, 
                                       String className) {
    return (getClusterProducer(clusterName,className)==null);
  }
  

}
                                   





