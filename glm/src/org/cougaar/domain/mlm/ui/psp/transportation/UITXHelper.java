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
import org.cougaar.util.EmptyEnumeration;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.Position;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

public class UITXHelper {

  protected static void printDebug(String s) {
    System.err.println(s);
  }

  public static final boolean DEBUG = false;

  /** DEBUG disabled! **/
  public static Collection toUITxRoutes(boolean debug, Collection transRoutes) {
    return toUITxRoutes(transRoutes);
  }

  /** 
   * Takes collection of TransportationRoutes and converts to a
   * Collection of UITxRoutes 
   */
  public static Collection toUITxRoutes(Collection transRoutes) {
    Vector uiRoutes = new Vector(transRoutes.size());
    Iterator routeIter = transRoutes.iterator();
    while (routeIter.hasNext()) {
      uiRoutes.addElement(
        toUITxRoute(
            (TransportationRoute)routeIter.next()));
    }
    return uiRoutes;
  }

  /** Takes a TransportationRoute and converts it to a UITxRoute **/
  public static UITxRoute toUITxRoute(TransportationRoute transRoute) {
    if (DEBUG) printDebug("Route: "+transRoute);
    UITxRoute uiRoute = new UITxRoute();
    TransportationNode sourceNode = transRoute.getSource();
    if (sourceNode != null) {
      ItemIdentificationPG sourceIdPG =
        sourceNode.getItemIdentificationPG();
      if (sourceIdPG != null) {
        String sourceId = sourceIdPG.getItemIdentification();
        if (DEBUG) printDebug("  Source Node Id: "+sourceId);
        uiRoute.setSourceNodeID(sourceId);
      } else
        if (DEBUG) printDebug("  Missing Source Node ItemIdPG");
    } else {
      if (DEBUG) printDebug("  Missing Source Node");
    }
    TransportationNode destNode = transRoute.getDestination();
    if (destNode != null) {
      ItemIdentificationPG destIdPG =
        destNode.getItemIdentificationPG();
      if (destIdPG != null) {
        String destId = destIdPG.getItemIdentification();
        if (DEBUG) printDebug("  Destination Node Id: "+destId);
        uiRoute.setDestinationNodeID(destId);
      } else {
        if (DEBUG) printDebug("  Missing Destination Node ItemIdPG");
      }
    } else {
      if (DEBUG) printDebug("  Missing Destination Node");
    }
    Vector linkIDs = new Vector();
    Vector routeLinks = transRoute.getLinks();
    if ((routeLinks != null) && (routeLinks.size() > 0)) {
      Enumeration enLinks = routeLinks.elements();
      do {
        TransportationLink transLink =
          (TransportationLink)enLinks.nextElement();
        ItemIdentificationPG linkIdPG =
          transLink.getItemIdentificationPG();
        if (linkIdPG != null) {
          String linkId = linkIdPG.getItemIdentification();
          if (DEBUG) printDebug("  LinkId: "+linkId);
          linkIDs.add(linkId);
        } else {
          if (DEBUG) printDebug("  Link missing ItemIdPG");
        }
      } while (enLinks.hasMoreElements());
    }
    uiRoute.setLinkIDs(linkIDs);
    return uiRoute;
  }

}
