/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Speed;
import org.cougaar.core.society.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.AbstractPrinter;
import org.cougaar.util.EmptyEnumeration;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.Position;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

/**
 * Reads Transportation Routes, Network, Links and Nodes from logplan
 * and writes back in AbstractPrinter formats.
 * <p>
 * @see this$QueryCode#getQueryCodes() for list of valid query codes
 */

public class PSP_TransportLinksNodes
    extends PSP_BaseAdapter
    implements PlanServiceProvider, UISubscriber {
  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   */
  public PSP_TransportLinksNodes() {
    super();
    setDebug();
  }

  public PSP_TransportLinksNodes( String pkg, String id ) 
    throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  protected static UnaryPredicate getTransportGraphPred(){
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof TransportationGraph);
      }
    };
  }

  protected static UnaryPredicate getTransportRoutePred(){
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof TransportationRoute);
      }
    };
  }

  /**
   * Valid PSP Query codes
   */
  protected static class QueryCode {
    /**
     * ID CODES:
     * TypeID: 
     *   routes  = 1
     *   network = 2
     *   links   = 3
     *   nodes   = 4
     */
    private int typeID;
    public QueryCode(String s) { 
      String type = s;
      if (type.equalsIgnoreCase("routes"))
        typeID = 1;
      else if (type.equalsIgnoreCase("network"))
        typeID = 2;
      else if (type.equalsIgnoreCase("links"))
        typeID = 3;
      else if (type.equalsIgnoreCase("nodes"))
        typeID = 4;
    }
    //public static Vector getQueryCodes();
    protected QueryCode() {}
    public String getTypeID() {
      switch (typeID) {
        case 1: return "routes";
        case 2: return "network";
        case 3: return "links";
        case 4: return "nodes";
        default: return null;
      }
    }
    public boolean isValid() { return (typeID > 0); }
    public boolean isRoutes()  {return (typeID == 1);}
    public boolean isNetwork() {return (typeID == 2);}
    public boolean isLinks()   {return (typeID == 3);}
    public boolean isNodes()   {return (typeID == 4);}
  }

  protected AbstractPrinter getAbstractPrinter(
      PrintStream out,
      HttpInput query_parameters,
      String defaultFormat) throws Exception {
    String format = defaultFormat;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = ((String)params.nextElement()).toLowerCase();
      if (p.startsWith("format=")) {
        format = p.substring("format=".length()).trim();
        if ((format.length() <= 0) ||
            !AbstractPrinter.isValidFormat(format)) {
          throw new RuntimePSPException("Invalid format!: "+format);
        }
      }
    }
    return AbstractPrinter.createPrinter(format, out);
  }

  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    execute(getAbstractPrinter(out, query_parameters, "html"), 
            query_parameters, psc, psu);
  }

  /**
   * Accept URL parameters as defined in <code>QueryCode</code>
   */
  public void execute(AbstractPrinter pr,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    // get query type
    QueryCode queryType;
    Enumeration params = query_parameters.getURLParameters().elements();
    do {
      if (!params.hasMoreElements()) {
        pr.printObject(new UITTException("Unrecognized QUERY TYPE"));
      }
      String p = (String)params.nextElement();
      queryType = new QueryCode(p);
    } while (!queryType.isValid());

    Object retObj;

    // get data
    if (queryType.isRoutes()) {
      retObj = searchTransportRoutes(psc);
    } else {
      Vector transGraphs = searchTransportGraphs(psc);
      if (queryType.isNetwork()) {
        UITxNetwork uiNet = new UITxNetwork();
        uiNet.setAllGroundNodes(getUITxNodes(transGraphs));
        uiNet.setAllGroundLinks(getUITxLinks(transGraphs));
        retObj = uiNet;
      } else if (queryType.isLinks()) {
        retObj = getUITxLinks(transGraphs);
      } else if (queryType.isNodes()) {
        retObj = getUITxNodes(transGraphs);
      } else {
        retObj = null;
      }
    }

    if (retObj == null)
      retObj = new UITTException("No Data Found");

    // send to output
    pr.printObject(retObj);
  }

  /** 
   * @return Vector of TransportationRoute instances
   */
  protected Vector searchTransportRoutes(PlanServiceContext psc) {
    Collection col = 
      psc.getServerPlugInSupport().queryForSubscriber(
        getTransportRoutePred());
    if (col instanceof Vector) {
      return (Vector)col;
    } else if (col == null) {
      return new Vector();
    } else {
      return new Vector(col);
    }
  }

  /**
   * @return Vector of TransportationGraph instances 
   */
  protected Vector searchTransportGraphs(PlanServiceContext psc) {
    Collection col = 
      psc.getServerPlugInSupport().queryForSubscriber(
        getTransportGraphPred());
    if (col instanceof Vector) {
      return (Vector)col;
    } else if (col == null) {
      return new Vector();
    } else {
      return new Vector(col);
    }
  }

  protected String getClusterNameFromUID(String uuid) {
    int i = uuid.indexOf("/");
    String nme = uuid.substring(0,i);
    return nme;
  }
  /** Get rid of "<..>" prefix/suffix **/
  public String getClusterIDAsString( String rawCID) {
    String id  = rawCID;

    if( id.startsWith("<") ) id = id.substring(1);
    if( id.endsWith(">") ) id = id.substring(0,id.length()-1);

    return id;
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   */
  public boolean returnsXML() {
    return false;
  }

  public boolean returnsHTML() {
    return true;
  }

  /** 
   * Any PlanServiceProvider must be able to provide DTD of its
   * output IFF it is an XML PSP... ie.  returnsXML() == true;
   * or return null
   */
  public String getDTD()  {
    return null;
  }

  public void subscriptionChanged(Subscription subscription) {
  }

  /**
   * Debug
   */

  protected boolean DEBUG;
  protected boolean setDebug() {
    // try system property
    String sysProp = System.getProperty(this.getClass().getName()+".debug");
    if (sysProp != null) {
      DEBUG = "true".equalsIgnoreCase(sysProp);
      return DEBUG;
    }
    // default
    DEBUG = false;
    return DEBUG;
  }
  protected void setDebug(boolean b) {DEBUG=b;}
  //protected void UITXHelper.printDebug(String s) {
  //  System.err.println(s);
  //}

  /**
   * Routes
   */

  protected Collection getUITxRoutes(Vector transRoutes) {
    HashSet routeSet = new HashSet();
    Enumeration en = transRoutes.elements();
    while (en.hasMoreElements()) {
      TransportationRoute transRoute = 
        (TransportationRoute)en.nextElement();
      routeSet.add(transRoute);
    }
    return UITXHelper.toUITxRoutes(DEBUG, routeSet);
  }


  /**
   * Links
   */

  protected Collection getTransportationLinks(
      TransportationGraph transGraph) {
    if (transGraph instanceof TransportationRoute)  {
      if (DEBUG) UITXHelper.printDebug("Get TransportationLinks(TransportationRoute)");
      return ((TransportationRoute)transGraph).getLinks();
    } else {
      if (DEBUG) UITXHelper.printDebug("Get TransportationLinks("+
                            transGraph.getClass()+")");
      HashSet allLinks = new HashSet();
      Collection transNodes = getTransportationNodes(transGraph);
      Iterator nodeIter = transNodes.iterator();
      while (nodeIter.hasNext()) {
        Vector vNodeLinks =
          ((TransportationNode)nodeIter.next()).getLinks();
        allLinks.addAll(vNodeLinks);
      }
      return allLinks;
    }
  }

  protected Collection getUITxLinks(Vector transGraphs) {
    HashSet linkSet = new HashSet();
    Enumeration en = transGraphs.elements();
    while (en.hasMoreElements()) {
      TransportationGraph transGraph = 
        (TransportationGraph)en.nextElement();
      linkSet.addAll(getTransportationLinks(transGraph));
    }
    return toUITxLinks(linkSet);
  }

  protected Collection toUITxLinks(Collection transLinks) {
    Vector uiLinks = new Vector(transLinks.size());
    Iterator linkIter = transLinks.iterator();
    while (linkIter.hasNext()) {
      TransportationLink transLink = 
        (TransportationLink)linkIter.next();
      if (DEBUG) UITXHelper.printDebug("Link: "+transLink);
      UITxLink uiLink = new UITxLink();
      ItemIdentificationPG linkIdPG = 
        transLink.getItemIdentificationPG();
      if (linkIdPG != null) {
        String linkId = linkIdPG.getItemIdentification();
        if (DEBUG) UITXHelper.printDebug("  LinkId: "+linkId);
        uiLink.setLinkID(linkId);
      } else {
        if (DEBUG) UITXHelper.printDebug("  Missing ItemIdPG");
      }
      TransportationNode sourceNode = transLink.getOrigin();
      if (sourceNode != null) {
        ItemIdentificationPG sourceIdPG =
          sourceNode.getItemIdentificationPG();
        if (sourceIdPG != null) {
          String sourceId = sourceIdPG.getItemIdentification();
          if (DEBUG) UITXHelper.printDebug("  Source Node Id: "+sourceId);
          uiLink.setSourceNodeID(sourceId);
        } else
          if (DEBUG) UITXHelper.printDebug("  Missing Source Node ItemIdPG");
      } else {
        if (DEBUG) UITXHelper.printDebug("  Missing Source Node");
      }
      TransportationNode destNode = transLink.getDestination();
      if (destNode != null) {
        ItemIdentificationPG destIdPG = 
          destNode.getItemIdentificationPG();
        if (destIdPG != null) {
          String destId = destIdPG.getItemIdentification();
          if (DEBUG) UITXHelper.printDebug("  Destination Node Id: "+destId);
          uiLink.setDestinationNodeID(destId);
        } else {
          if (DEBUG) UITXHelper.printDebug("  Missing Destination Node ItemIdPG");
        }
      } else {
        if (DEBUG) UITXHelper.printDebug("  Missing Destination Node");
      }
      if (transLink instanceof TransportationRoadLink) {
        RoadLinkPG rlPG = 
          ((TransportationRoadLink)transLink).getRoadLinkPG();
        if (rlPG != null) {
          Speed maxSpeed = rlPG.getMaxSpeed();
          if (maxSpeed != null) {
            int speed = (int)(maxSpeed.getMilesPerHour());
            if (DEBUG) UITXHelper.printDebug("  Speed(MPH): "+speed);
            uiLink.setSpeed(speed);
          } else {
            if (DEBUG) UITXHelper.printDebug("  Missing max speed");
          }
          int numberOfLanes = rlPG.getNumberOfLanes();
          if (DEBUG) UITXHelper.printDebug("  Number Of Lanes: "+numberOfLanes);
          uiLink.setNumberOfLanes(numberOfLanes);
        } else {
          if (DEBUG) UITXHelper.printDebug("  Missing RoadLinkPG");
        }
      } else {
        if (DEBUG) UITXHelper.printDebug("  Link not RoadLink: "+
                              transLink.getClass());
      }
      if (DEBUG) UITXHelper.printDebug("UITxLink:\n"+uiLink);
      uiLinks.add(uiLink);
    }
    return uiLinks;
  }

  /**
   * Nodes
   */

  protected Collection getTransportationNodes(TransportationGraph transGraph) {
    return transGraph.getNodes();
  }

  protected Collection getUITxNodes(Vector transGraphs) {
    HashSet nodeSet = new HashSet();
    Enumeration en = transGraphs.elements();
    while (en.hasMoreElements()) {
      TransportationGraph transGraph = 
        (TransportationGraph)en.nextElement();
      nodeSet.addAll(getTransportationNodes(transGraph));
    }
    return toUITxNodes(nodeSet);
  }

  protected Collection toUITxNodes(Collection transNodes) {
    Collection uiNodes = new Vector();
    Iterator nodeIter = transNodes.iterator();
    while (nodeIter.hasNext()) {
      TransportationNode transNode = 
        (TransportationNode)nodeIter.next();
      if (DEBUG) UITXHelper.printDebug("Node: "+transNode);
      UITxNode uiNode = new UITxNode();
      ItemIdentificationPG nodeIdPG = 
        transNode.getItemIdentificationPG();
      if (nodeIdPG != null) {
        String nodeId = nodeIdPG.getItemIdentification();
        if (DEBUG) UITXHelper.printDebug("  Id: "+nodeId);
        uiNode.setId(nodeId);
      } else {
        if (DEBUG) UITXHelper.printDebug("  Missing ItemIdPG");
      }
      String descName = transNode.getDescription();
      if (DEBUG) UITXHelper.printDebug("  ReadableName: "+descName);
      uiNode.setReadableName(descName);
      GeolocLocation loc = transNode.getGeolocLocation();
      if (loc != null) {
        String sloc = loc.getGeolocCode();
        if (DEBUG) UITXHelper.printDebug("  Geoloc: "+sloc);
        uiNode.setGeoloc(sloc);
        Latitude lat = loc.getLatitude();
        if (lat != null) {
          float latDegrees = (float)lat.getDegrees();
          if (DEBUG) UITXHelper.printDebug("  Latitude: "+latDegrees);
          uiNode.setLatitude(latDegrees);
        } else {
          if (DEBUG) UITXHelper.printDebug("  Missing Latitude");
        }
        Longitude lon = loc.getLongitude();
        if (lon != null) {
          float lonDegrees = (float)lon.getDegrees();
          if (DEBUG) UITXHelper.printDebug("  Longitude: "+lonDegrees);
          uiNode.setLongitude(lonDegrees);
        } else {
          if (DEBUG) UITXHelper.printDebug("  Missing Longitude");
        }
      } else {
        if (DEBUG) UITXHelper.printDebug("  Missing Geoloc");
      }
      if (DEBUG) UITXHelper.printDebug("UITxNode:\n"+uiNode);
      uiNodes.add(uiNode);
    }
    return uiNodes;
  }

}
