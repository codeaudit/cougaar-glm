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
 
package org.cougaar.mlm.ui.psp.society;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScheduleElement;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.mlm.ui.psp.plan.*;
import org.cougaar.mlm.ui.psp.transportation.PSP_Itinerary;
import org.cougaar.mlm.ui.psp.transportation.data.*;

public  class  SocietyUI {

  /**
   * Default data format for communication with PSPs.  If true then
   * uses XML, otherwise uses Object Serialization.
   */
  public static boolean DEFAULT_FORMAT_IS_XML = false;

  /** RETURNS RESULTS FROM HARD-WIRED QUERY TO 3-69-ARBN **/
  public static UISimpleInventory getQuantityData(String host_cluster_URL) {
    return InnerSocietyUI.getDummyQuantityData(host_cluster_URL);
  }

  protected static FetchObject objectFetcher = new FetchObject();
  public static void setObjectFetchHelper(FetchObject fo) {
    objectFetcher = fo;
  }
  public static Object fetchObject(String urlstring, boolean useXML) 
      throws Exception { 
    return objectFetcher.fetchObject(urlstring, null, useXML);
  }
  public static Object fetchObject(String urlstring, String postfield, 
      boolean useXML) throws Exception { 
    return objectFetcher.fetchObject(urlstring, postfield, useXML);
  }

  /**
   * <b>SEE other getTaskItineraries() method if you're using TOPS!.</b>
   * <p>
   * Returns "Task Itinerary" objects
   * "Transporting Assets" are computed by tracing the transporation information
   * for a given root transporation task through the transporation society
   *
   * @param host_URL:
   *               Host Cluster URL
   *               Form should be something like this:
   *               http//www.myhost.com:5555
   *               Note on convention: no trailing slash
   * @param cluster_name:
   *               Cluster to obtain UITaskItineraries from.
   * @param mode :  either of these values:
   *      PSP_Itinerary.CANNED // Return canned itineraries
   *      PSP_Itinerary.GSS // Retrive from simple GSS/SimpleMultileg Transporation Society
   *      PSP_Itinerary.LIVE  // Retrieve from full TOPS Society
   * @param interpolate:
   *      Expand routes into transportation legs
   * @param transported_unit_name_filter :
   *      Optional argument.  If provided, return only TaskItineraries which have
   *      the named unit in their TransportedUnitName field.  null disables filter
   *
   * @param input_task_uid:
   *      Optional argument.  if provided filter return only TaskItineraries which
   *      have the named UID in their inputTaskUID field .  null disables filter
   *
   *  The above optional "filter" arguments can be used in conjunction (AND).
   *
   * @return Vector of UITaskItinerary
   **/

  /**
   * <pre>
   *  "###############################################\n"+
   *  "# USAGE FOR getTaskItineraries IS NOW:        #\n"+
   *  "# public static Vector getTaskItineraries(    #\n"+
   *  "#     String host_URL,                        #\n"+
   *  "#     String cluster_name,                    #\n"+
   *  "#     String transported_unit_name_filter,    #\n"+
   *  "#     String input_task_uid_filter,           #\n"+
   *  "#     String mode,                            #\n"+
   *  "#     boolean interpolate,                    #\n"+
   *  "#     boolean sort,                           #\n"+
   *  "#     boolean includeUnload,                  #\n"+
   *  "#     boolean ignoreOrgItineraryElements,     #\n"+
   *  "#     boolean ignoreCarrierItineraryElements, #\n"+
   *  "#     boolean useXML)                         #\n"+
   *  "###############################################");
   * </pre>
   **/
  public static Vector USAGEgetTaskItineraries() {
    System.err.println(
      "###############################################\n"+
      "# USAGE FOR getTaskItineraries IS NOW:        #\n"+
      "# public static Vector getTaskItineraries(    #\n"+
      "#     String host_URL,                        #\n"+
      "#     String cluster_name,                    #\n"+
      "#     String transported_unit_name_filter,    #\n"+
      "#     String input_task_uid_filter,           #\n"+
      "#     String mode,                            #\n"+
      "#     boolean interpolate,                    #\n"+
      "#     boolean sort,                           #\n"+
      "#     boolean includeUnload,                  #\n"+
      "#     boolean ignoreOrgItineraryElements,     #\n"+
      "#     boolean ignoreCarrierItineraryElements, #\n"+
      "#     boolean useXML)                         #\n"+
      "###############################################");
    throw new RuntimeException("SEE ABOVE ERROR!");
  }
  /** @deprecated **/
  public static Vector getTaskItineraries(
      String host_cluster_URL) {
    return USAGEgetTaskItineraries();
  }
  /** @deprecated **/
  public static Vector getTaskItineraries(
      String host_name, 
      String mode) {
    return USAGEgetTaskItineraries();
  }
  /** @deprecated **/
  public static Vector getTaskItineraries(
      String host_URL,
      String cluster_name,
      String transported_unit_name_filter,
      String input_task_uid_filter,
      String mode) {
    return USAGEgetTaskItineraries();
  }
  /** @deprecated **/
  public static Vector getTaskItineraries(
      String host_URL,
      String cluster_name,
      String transported_unit_name_filter,
      String input_task_uid_filter,
      String mode,
      boolean interpolate) {
    return USAGEgetTaskItineraries();
  }
  /** @deprecated **/
  public static Vector getTaskItineraries(
      String host_URL,
      String cluster_name,
      String transported_unit_name_filter,
      String input_task_uid_filter,
      String mode,
      boolean interpolate,
      boolean sort,
      boolean useXML) {
    return USAGEgetTaskItineraries();
  }
  public static Vector getTaskItineraries(
      String host_URL,
      String cluster_name,
      String transported_unit_name_filter,
      String input_task_uid_filter,
      String mode,
      boolean interpolate,
      boolean sort,
      boolean includeUnload,
      boolean ignoreOrgItineraryElements,
      boolean ignoreCarrierItineraryElements,
      boolean useXML) {
    StringBuffer urlsb = new StringBuffer();
    urlsb.append(host_URL);
    urlsb.append("/$");
    urlsb.append(cluster_name);
    urlsb.append("/alpine/demo/ITINERARY.PSP");
    if (useXML) {
      urlsb.append("?FORMAT=XML");
    } else {
      urlsb.append("?FORMAT=DATA");
    }
    if (transported_unit_name_filter != null) {
      urlsb.append("?TransportedUnitName=");
      urlsb.append(transported_unit_name_filter);
    }
    if (input_task_uid_filter != null) {
      urlsb.append("?InputTaskUID=");
      urlsb.append(input_task_uid_filter);
    }
    if (mode != null) {
      urlsb.append("?MODE=");
      urlsb.append(mode);
    }
    if (interpolate) {
      urlsb.append("?interpolate");
    }
    if (sort) {
      urlsb.append("?sort");
    }
    if (includeUnload) {
      urlsb.append("?includeUnload");
    }
    if (ignoreOrgItineraryElements) {
      urlsb.append("?ignoreOrgItineraryElements");
    }
    if (ignoreCarrierItineraryElements) {
      urlsb.append("?ignoreCarrierItineraryElements");
    }
    String urlstring = urlsb.toString();

    System.out.println("Fetch: "+urlstring);
    Object obj;
    try {
      obj = fetchObject(urlstring, useXML);
    } catch (Exception e ) {
      System.out.println("Exception on ItineraryClient: "+e);
      e.printStackTrace();
      return null;
    }
    if (!(obj instanceof Vector)) {
      if (obj != null)
        System.out.println("UNEXPECTED RETURN TYPE: "+obj);
      return null;
    }
    return (Vector)obj;
  }

  /**
   * Get the task itineraries for the TOPS GlobalSea and GlobalAir.<p>
   * Identical to calling "getTaskItineraries" twice with cluster_names
   * "GlobalAir" and "GlobalSea"
   */
  public static Vector getTaskItineraries(
      String host_URL,
      String transported_unit_name_filter,
      String input_task_uid_filter,
      String mode,
      boolean interpolate,
      boolean sort,
      boolean useXML) {
    Vector vSea = 
      getTaskItineraries(
        host_URL, 
       "GlobalSea",
        transported_unit_name_filter,
        input_task_uid_filter,
        mode,
        interpolate,
        sort,
        useXML);
    Vector vAir = 
      getTaskItineraries(
        host_URL, 
        "GlobalAir",
        transported_unit_name_filter,
        input_task_uid_filter,
        mode,
        interpolate,
        sort,
        useXML);
    if (vSea != null) {
      if (vAir != null)
        vSea.addAll(vAir);
      return vSea;
    } else if (vAir != null) {
      return vAir;
    } else {
      return new Vector();
    }
  }

  private static Vector USAGEgetCarrierItineraries() {
    System.err.println(
      "###############################################\n"+
      "# USAGE FOR getCarrierItineraries IS NOW:     #\n"+
      "# public static Vector getCarrierItineraries( #\n"+
      "#     String host_cluster_URL,                #\n"+
      "#     String filterCarrierTypeNomenclature,   #\n"+
      "#     boolean ignoreUnscheduled,              #\n"+
      "#     boolean interpolate,                    #\n"+
      "#     boolean showAssets,                     #\n"+
      "#     boolean useXML)                         #\n"+
      "###############################################");
    throw new RuntimeException("SEE ABOVE ERROR!");
  }
  public static Vector getCarrierItineraries(
      String host_cluster_URL, 
      String filterCarrierTypeNomenclature,
      boolean ignoreUnscheduled, 
      boolean interpolate,
      boolean showAssets,
      boolean useXML) {
    StringBuffer urlsb = new StringBuffer();
    urlsb.append(host_cluster_URL);
    urlsb.append("/alpine/demo/CARRIER_ITINERARY.PSP");
    urlsb.append("?FORMAT=");
    urlsb.append(useXML ? "XML" : "DATA");
    if (filterCarrierTypeNomenclature != null) {
      urlsb.append("?CarrierTypeNomenclature=");
      urlsb.append(URLEncoder.encode(filterCarrierTypeNomenclature));
    }
    if (ignoreUnscheduled) {
      urlsb.append("?ignoreunscheduled");
    } else {
      urlsb.append("?noignoreunscheduled");
    }
    if (interpolate) {
      urlsb.append("?interpolate");
    } else {
      urlsb.append("?nointerpolate");
    }
    if (showAssets) {
      urlsb.append("?showassets");
    } else {
      urlsb.append("?noshowassets");
    }
    String urlstring = urlsb.toString();
    System.out.println("Fetch: "+urlstring);
    Object obj;
    try {
      obj = fetchObject(urlstring, useXML);
    } catch (Exception e ) {
      System.out.println("Exception on CarrierItineraryClient: "+e);
      e.printStackTrace();
      return null;
    }
    if (!(obj instanceof Vector)) {
      if (obj != null)
        System.out.println("UNEXPECTED RETURN TYPE: "+obj);
      return null;
    }
    return (Vector)obj;
  }

  /**
   *
   * Returns single Organization Itinerary representing "post-TAA"
   * movement of a "flagged" organization.  Each UIOrganizationItinerary
   * is composed of UIOrgItineraryElements
   *
   * @param host_cluster_URL:
   *               URL to Cluster where TASK (identified by TaskUID) lives.
   *               Form should be something like this:
   *               http//www.myhost.com:5555/$CLUSTERID
   *               Note on convention: no trailing slash
   *
   * @param OrganizationUID : OrganizationUID for whom
   * @return Vector of UIOrganizationItinerary
   *
   **/
  public static UIOrgItinerary getOrganizationItinerary(
      String host_cluster_URL, String OrganizationID) {
    return getOrganizationItinerary(host_cluster_URL, OrganizationID, 
                                    DEFAULT_FORMAT_IS_XML);
  }
  public static UIOrgItinerary getOrganizationItinerary(
      String host_cluster_URL, String OrganizationID,
      boolean useXML) {
    String urlstring = 
      host_cluster_URL+"/alpine/demo/ORG_ITINERARY.PSP"+
      ("?FORMAT="+(useXML ? "XML" : "DATA"));
    if (OrganizationID != null)
      urlstring += "?orgUID=" +OrganizationID;
    System.out.println("Fetching "+urlstring);
    Object obj;
    try {
      obj = fetchObject(urlstring, useXML);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      return null;
    }
    if (obj instanceof UIOrgItinerary) {
      System.out.println("Got UIOrgItinerary:");
      System.out.println(((UIOrgItinerary)obj).toString());
      return (UIOrgItinerary)obj;
    } else if (obj instanceof UITTException) {
      System.out.println("Got UITTException: " +
         ((UITTException)obj).toString() );
      return null;
    } else {
      if (obj != null)
        System.out.println("Unknown Object returned: "+
          obj.getClass().toString());
      return null;
    }
  }

  protected final static int USE_PSP_MODE =1;
  protected final static int USE_ORG_SERVER_MODE =2;
  protected final static int TRY_ORG_SERVER_FIRST_ELSE_PSP =3;
  protected static int defaultMode = TRY_ORG_SERVER_FIRST_ELSE_PSP;

  /**
   * Returns all Organziation relationships related to the named Org UID.
   *
   * @param host_cluster_URL:
   *               URL to Cluster where OrganizationUID lives.
   *               Form should be something like this:
   *               http//www.myhost.com:5555/$CLUSTERID
   *               Note on convention: no trailing slash
   *
   * @param OrganizationUID.  If provided, all UIOrgRelationships
   *               are filtered thusly:
   *                   getSourceOrganizationUID == OrganizationUID
   *                   || getTargetOrganizationUID == OrganizationUID
   *               If null, no filtering.
   *
   * @return Vector of UIOrgRelationship
   **/
  public static Vector getOrganizationRelationships(
      String host_cluster_URL, String OrganizationUID) {
    Vector v = null;
    if (defaultMode == TRY_ORG_SERVER_FIRST_ELSE_PSP) {
      v = innerGetOrganizationRelationships(
             host_cluster_URL, OrganizationUID, USE_ORG_SERVER_MODE);
      if (v.size() == 0) {
        System.out.println(
            "\nUnable to get ORG RELATIONSHIP data from ORG SERVER"+
            "\nTrying to obtain ORG RELATIONSHIP data from PSP...");
        v = innerGetOrganizationRelationships(
               host_cluster_URL, OrganizationUID, USE_PSP_MODE);
      }
    }
    return v;
  }

  protected static Vector innerGetOrganizationRelationships(
      String host_cluster_URL, String OrganizationUID, int mode) {
    Vector itins = new Vector();
    if (mode == USE_ORG_SERVER_MODE) {
      try {
        String line;
        URL url = new URL(host_cluster_URL + "/cmd/relations");
        URLConnection uc = url.openConnection();
        BufferedReader in = 
          new BufferedReader(new InputStreamReader(uc.getInputStream()));
        while ((line = in.readLine()) != null) {
          // Relation  3ID Supporting MCCGlobalMode 3ID/2 MCCGlobalMode/6
          System.out.println(line);
          StringTokenizer st = new StringTokenizer(line);
          if (!st.hasMoreElements())  
            return null;
          String cmd = st.nextToken();
          if (cmd.equals("Relation") && st.hasMoreElements())  {
            UIOrgRelationship relation = new UIOrgRelationship();
            relation.setProviderOrganizationCID(st.nextToken());
            relation.setRelationship(st.nextToken());
            relation.setTargetOrganizationCID(st.nextToken());
            relation.setProviderOrganizationUID(st.nextToken());
            relation.setTargetOrganizationUID(st.nextToken());
            itins.addElement(relation);
          }
          System.out.println(line);
        } // end while
      } catch (Exception e) {
        System.err.println(
            "ERROR TALKING TO ORGVIEW SERVER FOR ORG RELATIONSHIPS:"+
            e.getMessage());
      }
    } // END -- USE_ORG_SERVER_MODE
    else if (mode == USE_PSP_MODE) {
      boolean useXML = DEFAULT_FORMAT_IS_XML;
      try {
        String urlstring = host_cluster_URL+
          "/alpine/demo/UIASSETS.PSP?ORG_ASSET_RELATIONS?IS_HOST?FORMAT="+
          (useXML ? "XML" : "DATA");
          ///////+ "?ORGID="+OrganizationUID;
        System.out.println("Fetching "+urlstring);
        Object obj = fetchObject(urlstring, useXML);

        if (obj instanceof Vector) {
          itins = (Vector)obj;
          if (OrganizationUID != null) 
            itins = filterByOrgUID(itins,OrganizationUID);
        }
      } catch (EOFException eof) {
        // ignore
      } catch (Exception e) {
        System.err.println("ERROR TALKING TO PSP FOR ORG RELATIONSHIPS:"+
                           e.getMessage());
      }
    }  // END -- USE_PSP_MODE
    return itins;
  }

  /**
   * Returns all Organziation relationships related to the named Org UID.
   * Org relationships are returned in NORMALIZED FORM:   relationship
   * expressed from POV of the named Organization:
   *
   * In the "Normalized" return UIOrgRelationship.  The "named" ORG is the
   * "TargetOrganization".
   *
   *   1.)     getTargetOrganizationUID() == 'OrganizationUID' param
   *   2.)     relationship adjusted accordingly. (superior=>subordinates supporting=>supported)
   *   3.)     getProviderOrganizationUID() ==  cluster
   *
   * @param host_cluster_URL:
   *               URL to Cluster where OrganizationUID lives.
   *               Form should be something like this:
   *               http//www.myhost.com:5555/$CLUSTERID
   *               Note on convention: no trailing slash
   *
   * @param OrganizationUID.  If provided, all UIOrgRelationships
   *               are filtered thusly:
   *                   getSourceOrganizationUID == OrganizationUID
   *                   || getTargetOrganizationUID == OrganizationUID
   *               If null, no filtering.
   *
   * @return Vector of UIOrgRelationship
   **/
  public static Vector getNormalizedOrganizationRelationships(
      String host_cluster_URL,
      String OrganizationUID) {
    Vector results = new Vector();
    Vector v = 
      getOrganizationRelationships(host_cluster_URL, OrganizationUID);
    Enumeration en = v.elements();
    while (en.hasMoreElements()) {
      UIOrgRelationship rel = (UIOrgRelationship)en.nextElement();
      //
      // Normalize fields, if getTargetOrganizationUID() != me (move me into
      // that slot) by swap values with provider (all fields), remap 
      // relationship
      //
      if (!(rel.getTargetOrganizationUID().equalsIgnoreCase(OrganizationUID))) {
        String provider = rel.getProviderOrganizationUID();
        String target = rel.getTargetOrganizationUID();
        rel.setProviderOrganizationUID(target);
        // provider value assumed to be me (if target is not)
        rel.setTargetOrganizationUID(provider); 
        provider = rel.getProviderOrganizationCID();
        target = rel.getTargetOrganizationCID();
        rel.setProviderOrganizationCID(target);
        // provider value assumed to be me (if target is not)
        rel.setTargetOrganizationCID(provider); 
        // INVERT relationship
        Role role = Role.getRole(rel.getRelationship());
        rel.setRelationship(role.getConverse().getName());
      }
      if (rel != null) {
        results.addElement(rel);
        rel = null;
      }
    }
    return results;
  }

  protected static Vector filterByOrgUID(Vector v, String OrganizationUID) {
    Vector results = new Vector();
    Enumeration en = v.elements();
    while (en.hasMoreElements()) {
      UIOrgRelationship rel = (UIOrgRelationship)en.nextElement();
      if (rel.getProviderOrganizationUID().equalsIgnoreCase(OrganizationUID) ||
          rel.getTargetOrganizationUID().equalsIgnoreCase(OrganizationUID))
        results.addElement(rel);
    }
    if (results.size() == 0) {
      System.out.println(
          "Unable to find OrgRelationships which are sourced at: "+
          OrganizationUID+
          ", (searched from list of size=" + v.size() + ")");
    }
    return results;
  }

  /**
   * Returns all Transport/TransportationMission tasks at Cluster identified
   * by host_cluster_URL.
   *
   * @param host_cluster_URL:
   *               URL to Cluster where TASK (identified by TaskUID) lives.
   *               Form should be something like this:
   *               http//www.myhost.com:5555/$CLUSTERID
   *               Note on convention: no trailing slash
   *
   * @return Vector of Strings (TaskUIDs)
   **/
  public static Vector getTTasks(String host_cluster_URL) {
    if (host_cluster_URL == null) 
      return new Vector();
    return QueryChannelClient.readTxTaskUIDs( host_cluster_URL );
  }

  /**
   * Give the UI a UIModifyOrgItinerary "moa" and a Vector of Clusters,
   * it sends the "moa" to each cluster and returns a Vector of
   * results.
   */
  public static Vector sendUIModifyOrgActivityState(
      String host_cluster_URL, Vector clusters, 
      UIModifyOrgActivityState moa) {
    return sendUIModifyOrgActivityState(host_cluster_URL, clusters, 
                                        moa, DEFAULT_FORMAT_IS_XML);
  }
  public static Vector sendUIModifyOrgActivityState(
      String host_cluster_URL, Vector clusters, 
      UIModifyOrgActivityState moa, boolean useXML) {
    Vector vResults = new Vector();
    // check parameters
    if ((host_cluster_URL == null) ||
        (clusters == null) ||
        (clusters.size() < 1)) { 
      moa.makeClean();
      moa.setStatus("Invalid url/clusters");
      vResults.addElement(moa);
      return vResults;
    }
    // convert to xml
    String moaXml = moa.toXMLString();
    moaXml = PSP_ModifyOrgActivity.fixXmlForParamPassing(moaXml);
    // make param string
    String psp_params =
      "/alpine/demo/MODIFY_ORG_ACTIVITY.PSP?NETWORK?FORMAT="+
      (useXML ? "XML" : "DATA") +
      "?XMLDATA="+moaXml;
    // for each cluster, modify
    Enumeration en = clusters.elements();
    while (en.hasMoreElements()) {
      String cluster = (String)en.nextElement();
      String urlstring = 
         host_cluster_URL+"/$"+cluster+psp_params;
      System.out.println("Fetching "+urlstring);
      Object obj;
      try {
        obj = fetchObject(urlstring, useXML);
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace();
        obj = null;
      }
      if (obj instanceof UIModifyOrgActivityState) {
        UIModifyOrgActivityState newMoa = (UIModifyOrgActivityState)obj;
        vResults.addElement(newMoa);
        System.out.println("Got: "+ newMoa);
      } else {
        System.out.println("Unknown: "+
           ((obj != null) ? (""+obj.getClass()) : ""));
      }
    }
    return vResults;
  }

  /**
   * @return Information regarding Military Organization.  This
   * Location is considered the initial location of that organization.
   *
   * @param org_cluster_URL:
   *              URL to Cluster representing the target Military Org.
   *              Form should be something like this:
   *              http//www.myhost.com:5555/$CLUSTERID
   *              Note on convention: no trailing slash
   *
   *
   **/
  public static UIUnit getOrgInfo(String org_cluster_URL) {
    UIUnit result=null;
    if (org_cluster_URL == null)
      return result;
    return (UIUnit)QueryChannelClient.readMilOrgUnit( org_cluster_URL );
  }

  /**
   * @param host_cluster_URL:
   *
   *               URL to Cluster where TASK (identified by TaskUID) lives.
   *               Form should be something like this:
   *               http//www.myhost.com:5555/$CLUSTERID
   *               Note on convention: no trailing slash
   *
   * @return Collection of UITxRoute Objects
   *
   **/
  public static Collection getTxRoutes(String host_cluster_URL) {
    return getTxRoutes(host_cluster_URL, DEFAULT_FORMAT_IS_XML);
  }
  public static Collection getTxRoutes(
      String host_cluster_URL, boolean useXML) {
    String urlstring = host_cluster_URL+
      "/alpine/demo/QUERYTRANSPORT_NETWORK.PSP?ROUTES?FORMAT="+
      (useXML? "XML" : "DATA");
    System.out.println("Fetching "+urlstring);
    Object obj;
    try {
      obj = fetchObject(urlstring, useXML);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      obj = null;
    }
    if (!(obj instanceof Collection)) {
      if (obj != null)
        System.out.println("Got Collection of "+
          ((Collection)obj).size()+" UITxRoutes");
      return null;
    }
    return (Collection)obj;
  }

  /**
   * @param host_cluster_URL:
   *
   *               URL to Cluster where TASK (identified by TaskUID) lives.
   *               Form should be something like this:
   *               http//www.myhost.com:5555/$CLUSTERID
   *               Note on convention: no trailing slash
   *
   * @return Transportation Network instance (from which one can obtain Links and Nodes)
   *
   **/
  public static UITxNetwork getTxNetwork(String host_cluster_URL) {
    return getTxNetwork(host_cluster_URL, DEFAULT_FORMAT_IS_XML);
  }
  public static UITxNetwork getTxNetwork(
      String host_cluster_URL, boolean useXML) {
    String urlstring = host_cluster_URL+
      "/alpine/demo/QUERYTRANSPORT_NETWORK.PSP?NETWORK?FORMAT="+
      (useXML ? "XML" : "DATA");
    System.out.println("Fetching "+urlstring);
    Object obj;
    try {
      obj = fetchObject(urlstring, useXML);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      obj = null;
    }
    if (!(obj instanceof UITxNetwork)) {
      if (obj != null)
        System.out.println("Unknown: "+obj.getClass());
      return null;
    }
    UITxNetwork network = (UITxNetwork)obj;
    System.out.println("Got UITxNetwork:");
    Collection col = network.getAllGroundNodes();
    System.out.println("  Number of UITxNodes: "+
      ((col != null) ? ""+col.size() : "null"));
    col = network.getAllGroundLinks();
    System.out.println("  Number of UITxLinks: "+
      ((col != null) ? ""+col.size() : "null"));
    return network;
  }

  /**
   *
   * #######################################
   * main() for testing
   * #######################################
   *
   **/
  public static void main(String argv[]){
    if (argv.length != 1) {
      System.err.println("Missing host url argument");
      return;
    }

    Role role = Constants.Role.STRATEGICTRANSPORTATIONPROVIDER;

// TODD'S TESTING!!!!
    /*
    int loopi = 0;
    while (++loopi <= 3) {
      Vector clusters = new Vector();
      clusters.addElement("3-69-ARBN");
      String url_base = "http://tic-31:5555";
      UIModifyOrgActivityState moa = new UIModifyOrgActivityState();
      moa.setTypeText("Deployment");
      if (loopi != 2) {
        moa.setPressedButton(PSP_ModifyOrgActivity.GET_ORG_ACT_BUTTON);
      } else {
        moa.setPressedButton(PSP_ModifyOrgActivity.MODIFY_ORG_ACT_BUTTON);
        moa.setOrgIdLabel("3-69-ARBN");
        moa.setOpTempoText("High");
        moa.setCDayLabel("07/04/2000");
        moa.setStartText("07/04/2000");
        moa.setThruText("08/07/2000");
      }
      Vector results = 
        sendUIModifyOrgActivityState(
          url_base, clusters, moa);
      Enumeration en = results.elements();
      while (en.hasMoreElements()) {
        System.out.println((UIModifyOrgActivityState)en.nextElement());
      }
    }
    if (true) 
      return;
    */
// END TODD'S TESTING!!!!

    String defaultURL = argv[0];
    String defaultHostClusterName = "3-69-ARBN";

    SocietyUISampler sampler =
      new SocietyUISampler(defaultURL); 
    // "http://localhost:5555"); // "http://zermatt.alpine.bbn.com:5555");

    sampler.testGetAllOrgRelationships(defaultHostClusterName);
    //sampler.testGetOrgInfo("GlobalSea");
    sampler.testGetOrgInfo(defaultHostClusterName);
    sampler.testGetOrgItinerary(defaultHostClusterName);
    sampler.testGetTransportationTasks(defaultHostClusterName);
    //sampler.testGetTxNetwork("GlobalSea");
    sampler.testGetUITaskItineraries(
        defaultHostClusterName,PSP_Itinerary.CANNED);
  }

  /** Debugging output **/
  protected static void printResults(PrintStream out, Vector results){
    Enumeration ren = results.elements();
    while (ren.hasMoreElements()) {
      Object obj = ren.nextElement();
      if (obj instanceof UICarrierItinerary) {
        UICarrierItinerary uit = (UICarrierItinerary)obj;
        out.println("CarrierItemNomenclature=" + uit.CarrierItemNomenclature+
                    "CarrierTypeNomenclature=" + uit.CarrierTypeNomenclature+
                    ", CarrierUID=" + uit.CarrierUID);
        Enumeration ren2 = uit.getScheduleElements().elements();
        while (ren2.hasMoreElements()) {
          UIItineraryElement se = (UIItineraryElement)ren2.nextElement();
          out.println("starttime=" + se.getStartDate()+
                      ", endtime=" + se.getEndDate()+
                      ", startloc=" + se.getStartLocation()+
                      ", endloc=" + se.getEndLocation()+
                      "}" );
        }
      }
      else {
        out.println("UNKNOWN TYPE IN RESULTS SET: " + 
                    obj.getClass().toString() );
      }
    }
  }

}
