/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.SupplyClassPG;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.LocationSchedulePG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.util.MutableTimeSpan;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Provides convenience methods.
 */
public class AssetUtils {

  /**
   * Cache for isAssetOfType. Keys are Strings, values are either
   * null (unknown), an Asset class (the type), or Object (== assetFail).
   */
  private static final HashMap assetTypes = new HashMap(89);
  private static final Object assetFail = new Object();
  private static Logger logger = Logging.getLogger(AssetUtils.class);

  /**
   * @param type java Class name (assumed to be in package org.cougaar.planning.ldm.asset)
   * @return true if asset is and instance of type.
   */
  public static boolean isAssetOfType(Asset a, String type) {
    Class cl;
    synchronized (assetTypes) {
      Object at = assetTypes.get(type);
      if (at == null) {
        try {
          cl = Class.forName("org.cougaar.glm.ldm.asset." + type);
          assetTypes.put(type, cl);
        } catch (ClassNotFoundException cnfe) {
          assetTypes.put(type, assetFail);
          if (logger.isErrorEnabled()) {
            logger.error("isAssetOfType: " + cnfe);
          }
          return false;
        }
      } else if (at == assetFail) {
        // failed before
        return false;
      } else {
        cl = (Class) at;
      }
    }
    return cl.isInstance(a);
  }

  /**
   * @param type String describing class of resource
   * @return true if Asset has SupplyClassPG and SupplyType equals type
   */
  public static boolean isSupplyClassOfType(Asset asset, String type) {
    boolean result = false;
    if (asset == null) {
      return false;
    }
    SupplyClassPG pg = (SupplyClassPG) asset.searchForPropertyGroup(SupplyClassPG.class);
    if (pg != null) {
      result = type.equals(pg.getSupplyType());
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("isSupplyClassOfType(): Asset without SupplyClassPG " + assetDesc(asset));
      }
    }
    return result;
  }

  public static String assetDesc(Asset asset) {
    String nsn = getAssetIdentifier(asset);
    return nsn + "(" + getPartNomenclature(asset) + ")";
  }

  public static String getPartNomenclature(Asset part) {
    String nomen = "Unknown part name";
    TypeIdentificationPG tip = part.getTypeIdentificationPG();
    if (tip != null) {
      nomen = tip.getNomenclature();
    }
    return nomen;
  }

  public static String getAssetIdentifier(Asset asset) {

    if (asset == null) {
      return null;
    } else {
      TypeIdentificationPG tip = asset.getTypeIdentificationPG();
      if (tip != null) {
        return tip.getTypeIdentification();
      } else {
        if (logger.isErrorEnabled()) {
          logger.error("asset: " + asset + " has null getTypeIdentificationPG()");
        }
        return null;
      }
    }
  }

  // Determines if an orginazation provides a supporting role for another organization.
  public static boolean isOrgSupporting(Organization org, Organization support_org, Role role) {
    RelationshipSchedule rel_sched = org.getRelationshipSchedule();
    // Ask for matching relationships where support_org is the provider.
    // MutableTimeSpan() will create a TimeSpan from the beginning of time to the end of time and
    // therefore look at all relationships.
    Collection c = rel_sched.getMatchingRelationships(role, support_org, new MutableTimeSpan());
    return !c.isEmpty();
  }

  public static Enumeration getSupportingOrgs(Organization myOrg, Role role, long time) {
    return getSupportingOrgs(myOrg, role, time, time);
  }

  public static Enumeration getSupportingOrgs(Organization myOrg, Role role, long start, long end) {
    RelationshipSchedule rel_sched = myOrg.getRelationshipSchedule();
    Collection c = rel_sched.getMatchingRelationships(role, start, end);
    Vector support_orgs = new Vector();
    Iterator i = c.iterator();
    Relationship r;
    while (i.hasNext()) {
      r = (Relationship) i.next();
      support_orgs.add(rel_sched.getOther(r));
    }
    return support_orgs.elements();
  }

  public static Enumeration getGeolocLocationAtTime(Organization org, long time) {
    LocationSchedulePG lspg = org.getLocationSchedulePG();
    Vector geolocs = new Vector();
    try {
      Schedule ls = lspg.getSchedule();
      Iterator i = ls.getScheduleElementsWithTime(time).iterator();
      while (i.hasNext()) {
        LocationScheduleElement lse = (LocationScheduleElement) i.next();
        geolocs.add((GeolocLocation) lse.getLocation());
      }
    } catch (NullPointerException npe) {
      // Not all organizations have LocationSchedulePG's
      //  	    GLMDebug.DEBUG("AssetUtils",
      //  			   "getGeolocLocationAtTime(), LocationSchedulePG NOT found on "+org);
    }
    return geolocs.elements();
  }

  /**
   * getPreviousGeolocLocation - given a time, return the organization's prior location
   * Uses LocationSchedulePG and HomeLocation.
   */
  public static GeolocLocation getPreviousGeolocLocation(Organization org, long time) {
    System.out.println(99);
    LocationSchedulePG lspg = org.getLocationSchedulePG();
    GeolocLocation previous = null;
    try {
      ArrayList ls = new ArrayList(lspg.getSchedule());

      for (Iterator i = ls.iterator(); i.hasNext();) {
        LocationScheduleElement lse = (LocationScheduleElement) i.next();
        if (lse.included(time)) {
          break;
        }
        previous = (GeolocLocation) lse.getLocation();
      }
    } catch (NullPointerException npe) {
      // Not all organizations have LocationSchedulePG's
      //  	    GLMDebug.DEBUG("AssetUtils",
      //  			   "getGeolocLocationAtTime(), LocationSchedulePG NOT found on "+org);
    }

    // Assume org at home if previous location not found in the LocationSchedulePG
    if ((previous == null) && (org.hasMilitaryOrgPG())) {
      previous = (GeolocLocation) org.getMilitaryOrgPG().getHomeLocation();
    }
    return previous;
  }

  public static void printRelationshipSchedule(Organization myOrg) {
    RelationshipSchedule sched = myOrg.getRelationshipSchedule();
    if (logger.isDebugEnabled()) {
      logger.debug("____________________________________________________________");
    }
    for (Iterator iterator = new ArrayList(sched).iterator(); iterator.hasNext();) {
      Relationship r = (Relationship) iterator.next();
      if (logger.isDebugEnabled()) {
        logger.debug(r.getRoleA() + ", " + r.getRoleB() + ", start: " + r.getStartTime() + ", end: " + r.getEndTime());
      }
    }
  }
}

